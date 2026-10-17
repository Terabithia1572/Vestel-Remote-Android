package com.yunusinan.vestelremote.ui.viewmodel

import com.yunusinan.vestelremote.domain.model.TvCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

data class VolumeControlState(
    val displayVolume: Int = 20,
    val estimatedTvVolume: Int = 20,
    val isVolumeReportedByTv: Boolean = false,
    val isUserDragging: Boolean = false,
    val isAdjusting: Boolean = false
)

/**
 * Rate-limited, coalescing volume slider controller for Android TV Remote v2.
 *
 * Since Android TV Remote v2 only supports incremental key events ([TvCommand.VolumeUp] and
 * [TvCommand.VolumeDown]) and reports volume asynchronously via `RemoteSetVolumeLevel`,
 * firing a command on every Compose `Slider` drag frame causes unbounded command queues
 * that keep changing the TV volume after the user releases the slider.
 *
 * This controller guarantees:
 * 1. Single serialized stepping job (coalesces rapid drag frames into a single target).
 * 2. Strict rate limiting ([stepIntervalMs] between incremental steps) and bounded burst size.
 * 3. Immediate cancellation when the user releases the slider ([onSliderInteractionFinished]).
 * 4. Immediate cancellation when the user reverses drag direction.
 * 5. Protection against feedback loops between incoming TV volume updates and active drag state.
 */
class VolumeSliderController(
    private val scope: CoroutineScope,
    private val stepIntervalMs: Long = 140L,
    private val maxStepsPerGesture: Int = 15,
    private val sendVolumeCommand: suspend (TvCommand) -> Unit
) {
    private val _state = MutableStateFlow(VolumeControlState())
    val state: StateFlow<VolumeControlState> = _state.asStateFlow()

    private var steppingJob: Job? = null
    private var targetVolume: Int = 20
    private var lastRawDragVolume: Int? = null
    private var activeStepDirection: Int = 0
    private var stepsSentInCurrentGesture: Int = 0

    /**
     * Called when the repository/TV emits a volume level.
     */
    fun onTvVolumeUpdated(newVolume: Int, isFromRealTvOrMock: Boolean = true) {
        val clamped = newVolume.coerceIn(0, 100)
        _state.update { current ->
            if (current.isUserDragging) {
                current.copy(
                    estimatedTvVolume = clamped,
                    isVolumeReportedByTv = current.isVolumeReportedByTv || isFromRealTvOrMock
                )
            } else {
                targetVolume = clamped
                current.copy(
                    displayVolume = clamped,
                    estimatedTvVolume = clamped,
                    isVolumeReportedByTv = current.isVolumeReportedByTv || isFromRealTvOrMock
                )
            }
        }
    }

    /**
     * Called when the connection resets so we don't pretend unconfirmed state is live TV data.
     */
    fun onConnectionReset() {
        cancelPendingCommands()
        _state.update {
            it.copy(
                isVolumeReportedByTv = false,
                isUserDragging = false,
                isAdjusting = false
            )
        }
    }

    /**
     * Called on Compose Slider `onValueChange`.
     * Coalesces rapid drag frames, detects direction reversals, and steps at a bounded rate.
     */
    fun onSliderValueChanged(newVolume: Int) {
        val clamped = newVolume.coerceIn(0, 100)
        val previousDrag = lastRawDragVolume
        lastRawDragVolume = clamped

        val currentEstimated = _state.value.estimatedTvVolume
        val desiredDirection = (clamped - currentEstimated).sign

        // Detect direction reversal during an active drag
        if (previousDrag != null) {
            val frameDelta = clamped - previousDrag
            if (frameDelta != 0 && activeStepDirection != 0 && frameDelta.sign != activeStepDirection) {
                // User reversed finger movement direction: cancel queued steps in the old direction immediately
                steppingJob?.cancel()
                steppingJob = null
                activeStepDirection = 0
                targetVolume = clamped
                _state.update {
                    it.copy(
                        displayVolume = clamped,
                        isUserDragging = true,
                        isAdjusting = false
                    )
                }
                if (desiredDirection != 0) {
                    startSteppingLoop()
                }
                return
            }
        }

        targetVolume = clamped
        _state.update {
            it.copy(
                displayVolume = clamped,
                isUserDragging = true
            )
        }

        if (desiredDirection == 0) {
            steppingJob?.cancel()
            steppingJob = null
            activeStepDirection = 0
            _state.update { it.copy(isAdjusting = false) }
            return
        }

        if (steppingJob?.isActive != true) {
            startSteppingLoop()
        }
    }

    /**
     * Called on Compose Slider `onValueChangeFinished` (when user lifts finger).
     * Immediately cancels any pending volume steps so the TV volume stops changing right away.
     */
    fun onSliderInteractionFinished() {
        steppingJob?.cancel()
        steppingJob = null
        activeStepDirection = 0
        lastRawDragVolume = null
        stepsSentInCurrentGesture = 0

        _state.update { current ->
            targetVolume = current.estimatedTvVolume
            current.copy(
                displayVolume = current.estimatedTvVolume,
                isUserDragging = false,
                isAdjusting = false
            )
        }
    }

    /**
     * Cancels any in-flight or scheduled volume commands immediately.
     */
    fun cancelPendingCommands() {
        steppingJob?.cancel()
        steppingJob = null
        activeStepDirection = 0
        lastRawDragVolume = null
        stepsSentInCurrentGesture = 0
        _state.update {
            it.copy(
                displayVolume = it.estimatedTvVolume,
                isUserDragging = false,
                isAdjusting = false
            )
        }
    }

    private fun startSteppingLoop() {
        steppingJob?.cancel()
        steppingJob = scope.launch {
            _state.update { it.copy(isAdjusting = true) }
            try {
                while (isActive && stepsSentInCurrentGesture < maxStepsPerGesture) {
                    val currentEstimated = _state.value.estimatedTvVolume
                    val diff = targetVolume - currentEstimated
                    if (diff == 0) break

                    val stepDir = diff.sign
                    activeStepDirection = stepDir

                    val nextEstimated = (currentEstimated + stepDir).coerceIn(0, 100)
                    _state.update { it.copy(estimatedTvVolume = nextEstimated) }
                    stepsSentInCurrentGesture++

                    val command = if (stepDir > 0) TvCommand.VolumeUp else TvCommand.VolumeDown
                    sendVolumeCommand(command)

                    // Wait between incremental commands so the TV and socket are never flooded
                    delay(stepIntervalMs)
                }
            } finally {
                activeStepDirection = 0
                _state.update { it.copy(isAdjusting = false) }
            }
        }
    }
}

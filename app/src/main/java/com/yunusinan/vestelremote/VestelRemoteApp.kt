package com.yunusinan.vestelremote

import android.app.Application
import com.yunusinan.vestelremote.data.repository.TvRepository
import com.yunusinan.vestelremote.data.repository.TvRepositoryImpl

class VestelRemoteApp : Application() {

    lateinit var repository: TvRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TvRepositoryImpl(applicationContext)
    }
}

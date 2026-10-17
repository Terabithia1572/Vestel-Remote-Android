"""Generate public test bytes using the independent reference protobuf runtime.

Usage: python scripts/generate_protocol_fixtures.py REFERENCE_DIR OUTPUT_DIR
REFERENCE_DIR contains tronikos/androidtvremote2's polo_pb2.py and
remotemessage_pb2.py from e664a8e1f14ee458b14c646bfb3ea46f10367051.
Requires protobuf and cryptography. No application Kotlin encoder is used.
"""
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(sys.argv[1]).resolve()))
import polo_pb2 as polo
import remotemessage_pb2 as remote

out = pathlib.Path(sys.argv[2])
out.mkdir(parents=True, exist_ok=True)
fixtures = {}

def save(name, message):
    fixtures[name] = message.SerializeToString().hex()

def pair():
    return polo.OuterMessage(protocol_version=2, status=200)

m = pair()
m.pairing_request.service_name = "atvremote"
m.pairing_request.client_name = "Vestel Remote"
save("pair_request", m)
m = pair()
m.pairing_request_ack.server_name = "Fixture TV"
save("pair_request_ack", m)
m = pair()
m.options.preferred_role = 1
m.options.input_encodings.add(type=3, symbol_length=6)
save("pair_options", m)
m = pair()
m.options.preferred_role = 2
m.options.output_encodings.add(type=3, symbol_length=6)
save("server_options", m)
m = pair()
m.configuration.client_role = 1
m.configuration.encoding.type = 3
m.configuration.encoding.symbol_length = 6
save("pair_configuration", m)
m = pair()
m.configuration_ack.SetInParent()
save("configuration_ack", m)
m = pair()
m.secret.secret = bytes(range(32))
save("pair_secret", m)
m = pair()
m.secret_ack.secret = bytes(range(32))
save("secret_ack", m)
m = pair()
save("status_only", m)
m.status = 402
save("bad_secret", m)

for name, key in {"up": 19, "down": 20, "left": 21, "right": 22, "select": 23,
                  "home": 3, "back": 4, "volume_up": 24, "volume_down": 25, "mute": 164}.items():
    m = remote.RemoteMessage()
    m.remote_key_inject.key_code = key
    m.remote_key_inject.direction = remote.SHORT
    save("key_" + name, m)
for name, value in [("server_configure", 1023), ("client_configure", 611)]:
    m = remote.RemoteMessage()
    m.remote_configure.code1 = value
    if name == "client_configure":
        d = m.remote_configure.device_info
        d.model, d.vendor, d.unknown1, d.unknown2 = "Vestel Remote", "Android", 1, "1"
        d.package_name, d.app_version = "com.yunusinan.vestelremote", "1.0.0"
    save(name, m)
for name, value in [("server_active", 1023), ("client_active", 611)]:
    m = remote.RemoteMessage()
    m.remote_set_active.active = value
    save(name, m)
m = remote.RemoteMessage()
m.remote_start.started = True
save("remote_start", m)
m.remote_start.started = False
save("remote_start_off", m)
m = remote.RemoteMessage()
m.remote_ping_request.val1 = 300
m.remote_ping_request.val2 = 5000
save("ping", m)
m = remote.RemoteMessage()
m.remote_ping_response.val1 = 300
save("pong", m)
m = remote.RemoteMessage()
m.remote_set_volume_level.volume_max = 50
m.remote_set_volume_level.volume_level = 12
m.remote_set_volume_level.volume_muted = True
save("volume", m)
m = remote.RemoteMessage()
m.remote_app_link_launch_request.app_link = "https://example.com/" + "x" * 140
save("long_app_link", m)
m = remote.RemoteMessage()
m.remote_error.value = True
save("remote_error", m)

(out / "wire.properties").write_text(
    "# Generated with reference protobuf, not the application encoder.\n" +
    "\n".join(f"{k}={v}" for k, v in fixtures.items()) + "\n", encoding="ascii")
print(f"Generated {len(fixtures)} fixtures")

# Synthetic public certificates only. Never save private keys or use real device material.
import datetime
import hashlib
from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID

certificates = []
for label in ("client", "server"):
    path = out / f"synthetic-{label}.der"
    if not path.exists():
        key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        name = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, f"Synthetic test {label}")])
        cert = (x509.CertificateBuilder().subject_name(name).issuer_name(name).public_key(key.public_key())
                .serial_number(1).not_valid_before(datetime.datetime(2025, 1, 1))
                .not_valid_after(datetime.datetime(2040, 1, 1)).sign(key, hashes.SHA256()))
        path.write_bytes(cert.public_bytes(serialization.Encoding.DER))
    certificates.append(x509.load_der_x509_certificate(path.read_bytes()))

h = hashlib.sha256()
for cert in certificates:
    numbers = cert.public_key().public_numbers()
    h.update(bytes.fromhex(f"{numbers.n:X}"))
    h.update(bytes.fromhex(f"0{numbers.e:X}"))
nonce = bytes.fromhex("4E19")
h.update(nonce)
digest = h.digest()
(out / "synthetic-crypto.properties").write_text(
    "# Synthetic test vector; no real TV credentials.\n" +
    f"pin={digest[:1].hex()}{nonce.hex()}\nsecret={digest.hex()}\n", encoding="ascii")

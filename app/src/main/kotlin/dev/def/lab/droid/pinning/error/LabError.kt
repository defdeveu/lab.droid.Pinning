package dev.def.lab.droid.pinning.error

sealed class LabError {
    data object InvalidConfiguration : LabError()

    data object PinNotConfigured : LabError()

    data class PinMismatch(val expected: String, val actual: String) : LabError()

    data object TrustFailed : LabError()

    data object InvalidUrl : LabError()

    data object TransportFailed : LabError()

    data class ServerStatus(val status: Int) : LabError()
}

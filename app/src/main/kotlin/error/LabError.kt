package defdev.labdroid.pinning.error

sealed class LabError {
    data object InvalidConfiguration : LabError()

    data object InvalidUrl : LabError()

    data object TransportFailed : LabError()

    data object TrustFailed : LabError()

    data class ServerStatus(val status: Int) : LabError()
}

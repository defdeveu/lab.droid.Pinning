package dev.def.lab.droid.pinning.store

import java.io.File

interface PinStore {
    fun isBroken(): Boolean

    fun setBroken(broken: Boolean)
}

class FilePinStore(private val file: File) : PinStore {
    override fun isBroken(): Boolean =
        file.takeIf { it.exists() }?.readText()?.trim() == BROKEN

    override fun setBroken(broken: Boolean) {
        if (broken) {
            file.writeText(BROKEN)
        } else {
            file.delete()
        }
    }

    private companion object {
        const val BROKEN = "broken"
    }
}

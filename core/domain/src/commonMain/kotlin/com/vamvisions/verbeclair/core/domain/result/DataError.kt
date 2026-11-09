package com.vamvisions.verbeclair.core.domain.result

/** Why a data-layer function failed (N-17). */
sealed interface DataError : Error {

    /** A failure of the on-device databases or files. */
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}

package com.vamvisions.verbeclair.core.domain.result

/** Marker for the typed errors a [Result] can carry (N-17). */
interface Error

/** The outcome of a function that can fail: a value, or a typed error the caller has to handle. */
sealed interface Result<out D, out E : Error> {

    data class Success<out D>(val data: D) : Result<D, Nothing>

    data class Error<out E : com.vamvisions.verbeclair.core.domain.result.Error>(val error: E) : Result<Nothing, E>
}

/** A [Result] with no value, for a write that only succeeds or fails. */
typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> =
    when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
    }

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

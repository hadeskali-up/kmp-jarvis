package com.jarvis.app.core

/**
 * Standardized loading / success / error states for all screens.
 * Replace ad-hoc isLoading + error + data triples with a single state variable.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
}

/**
 * Convenience: fold inline without when-block.
 */
inline fun <T, R> UiState<T>.fold(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (String) -> R
): R = when (this) {
    is UiState.Loading -> onLoading()
    is UiState.Success -> onSuccess(data)
    is UiState.Error -> onError(message)
}

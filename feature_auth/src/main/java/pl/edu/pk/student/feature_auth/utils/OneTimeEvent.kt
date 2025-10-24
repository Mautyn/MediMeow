package pl.edu.pk.student.feature_auth.utils

class OneTimeEvent<out T>(private val content: T) {
    private var hasBeenHandled = false

    fun get(): T? = if (hasBeenHandled) null else {
        hasBeenHandled = true
        content
    }
}

inline fun <T> OneTimeEvent<T>?.consume(action: (T) -> Unit) {
    this?.get()?.let { action(it) }
}
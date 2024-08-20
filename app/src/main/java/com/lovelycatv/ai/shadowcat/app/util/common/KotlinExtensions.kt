package com.lovelycatv.ai.shadowcat.app.util.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

inline fun <T, R> nullOrValue(value: T?, fx: (value: T) -> R?): R? {
    return if (value == null) {
        null
    } else {
        fx(value)
    }
}

fun <T> valueSelector(condition: Boolean, v1: T, v2: T): T = if (condition) v1 else v2

/**
 * This method will run fx() in IO thread unless param dispatcher is specified
 *
 * @param coroutineScope CoroutineScope
 * @param dispatcher Dispatchers (default: Dispatchers.IO)
 * @param onCompleted When fx() finished, the onCompleted() will be called
 * @param fx Actions
 */
@OptIn(DelicateCoroutinesApi::class)
inline fun runAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline fx: suspend () -> Unit
): Job {
    return coroutineScope.launch(dispatcher) {
        fx()
    }
}

@OptIn(DelicateCoroutinesApi::class)
inline fun runOnMain(
    coroutineScope: CoroutineScope = GlobalScope,
    crossinline fx: suspend () -> Unit
): Job {
    return runAsync(coroutineScope, Dispatchers.Main) {
        fx()
    }
}

inline fun Boolean.runIfTrue(fx: () -> Unit) {
    if (this) fx()
}

@OptIn(DelicateCoroutinesApi::class)
inline fun Boolean.runAsyncIfTure(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline fx: suspend () -> Unit
): Job? {
    return if (this) runAsync(coroutineScope, dispatcher) { fx() } else null

}

inline fun Boolean.runIfFalse(fx: () -> Unit) {
    (!this).runIfTrue { fx() }
}

@OptIn(DelicateCoroutinesApi::class)
inline fun Boolean.runAsyncIfFalse(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline fx: suspend () -> Unit
): Job? = (!this).runAsyncIfTure(coroutineScope, dispatcher) { fx() }


inline fun <T> T?.runIfNotNull(fx: (T) -> Unit) {
    if (this != null) fx(this)
}

@OptIn(DelicateCoroutinesApi::class)
inline fun <T> T?.runAsyncIfNotNull(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline fx: suspend (T) -> Unit
): Job? = if (this != null) runAsync(coroutineScope, dispatcher) { fx(this) } else null


fun printlnObviously(message: CharSequence) {
    val s = "=".repeat(message.length)
    println(s)
    println(message)
    println(s)
}

fun <T> Iterable<T>.searchIndex(condition: (t: T) -> Boolean): Int {
    var result = -1
    mutableListOf<T>().apply {
        this.addAll(this@searchIndex)
    }.forEachIndexed { index, t ->
        if (condition(t)) {
            result = index
            return@forEachIndexed
        }
    }
    return result
}

fun CharSequence.println() {
    println(this)
}
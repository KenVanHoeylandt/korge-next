package com.soywiz.korma.math

fun FloatArray.minOrElse(nil: Float): Float {
    if (isEmpty()) return nil
    var out = Float.POSITIVE_INFINITY
    for (i in 0..lastIndex) out = kotlin.math.min(out, this[i])
    return out
}

fun FloatArray.maxOrElse(nil: Float): Float {
    if (isEmpty()) return nil
    var out = Float.NEGATIVE_INFINITY
    for (i in 0..lastIndex) out = kotlin.math.max(out, this[i])
    return out
}

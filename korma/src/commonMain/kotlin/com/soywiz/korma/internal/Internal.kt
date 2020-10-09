package com.soywiz.korma.internal

import com.soywiz.korma.math.*
import kotlin.math.*

internal val Float.niceStr: String get() = if (almostEquals(this.toLong().toFloat(), this)) "${this.toLong()}" else "$this"
internal val Double.niceStr: String get() = if (almostEquals(this.toLong().toDouble(), this)) "${this.toLong()}" else "$this"

internal infix fun Float.umod(other: Float): Float {
    val remainder = this % other
    return when {
        remainder < 0f -> remainder + other
        else -> remainder
    }
}

@PublishedApi
internal fun floorCeil(v: Float): Float = if (v < 0f) ceil(v) else floor(v)

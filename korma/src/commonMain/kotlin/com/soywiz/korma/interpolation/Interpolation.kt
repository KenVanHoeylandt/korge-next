package com.soywiz.korma.interpolation

interface Interpolable<T> {
    fun interpolateWith(ratio: Float, other: T): T
}

interface MutableInterpolable<T> {
    fun setToInterpolated(ratio: Float, l: T, r: T)
}

fun Double.interpolate(l: Double, r: Double): Double = (l + (r - l) * this)
fun Double.interpolate(l: Int, r: Int): Double = (l + (r - l) * this)
fun Float.interpolate(l: Float, r: Float): Float = (l + (r - l) * this)
fun Float.interpolate(l: Int, r: Int): Float = (l + (r - l) * this)

fun <T> Float.interpolate(l: Interpolable<T>, r: Interpolable<T>): T = l.interpolateWith(this, r as T)
fun <T> Double.interpolate(l: Interpolable<T>, r: Interpolable<T>): T = l.interpolateWith(toFloat(), r as T)
fun <T : Interpolable<T>> Float.interpolate(l: T, r: T): T = l.interpolateWith(this, r)

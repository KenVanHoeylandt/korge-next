package com.soywiz.korma.geom

@Suppress("NOTHING_TO_INLINE")
class PointPool(val size: Int) {
    @PublishedApi
    internal val points = Array(size) { com.soywiz.korma.geom.Point() }
    @PublishedApi
    internal var offset = 0

    @PublishedApi
    internal fun alloc() = points[offset++]

    fun Point(x: Float, y: Float) = alloc().apply { setTo(x, y) }
    fun Point(x: Double, y: Double) = alloc().apply { setTo(x.toFloat(), y.toFloat()) }
    fun Point(x: Int, y: Int) = Point(x.toFloat(), y.toFloat())
    fun Point() = Point(0f, 0f)

    operator fun IPoint.plus(other: IPoint): IPoint = alloc().apply { setToAdd(this, other) }
    operator fun IPoint.minus(other: IPoint): IPoint = alloc().apply { setToSub(this, other) }

    operator fun IPoint.times(value: IPoint): IPoint = alloc().apply { setToMul(this, value) }
    operator fun IPoint.times(value: Float): IPoint = alloc().apply { setToMul(this, value) }

    operator fun IPoint.div(value: IPoint): IPoint = alloc().apply { setToDiv(this, value) }
    operator fun IPoint.div(value: Float): IPoint = alloc().apply { setToDiv(this, value) }

    inline operator fun invoke(callback: PointPool.() -> Unit) {
        val oldOffset = offset
        try {
            callback()
        } finally {
            offset = oldOffset
        }
    }
}

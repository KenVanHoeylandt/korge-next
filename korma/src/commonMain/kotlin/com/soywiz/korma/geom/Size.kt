package com.soywiz.korma.geom

import com.soywiz.korma.internal.*
import com.soywiz.korma.interpolation.*

interface ISize {
    val width: Float
    val height: Float

    val area: Float get() = width * height
    val perimeter: Float get() = width * 2 + height * 2
    val min: Float get() = kotlin.math.min(width, height)
    val max: Float get() = kotlin.math.max(width, height)

    companion object {
        operator fun invoke(width: Float, height: Float): ISize = Size(Point(width, height))
    }
}

inline class Size(val p: Point) : MutableInterpolable<Size>, Interpolable<Size>, ISize, Sizeable {
    companion object {
        operator fun invoke(): Size = Size(Point(0f, 0f))
        operator fun invoke(width: Float, height: Float): Size = Size(Point(width, height))
        operator fun invoke(width: Int, height: Int): Size = Size(Point(width.toFloat(), height.toFloat()))
        operator fun invoke(width: Double, height: Double): Size = Size(Point(width.toFloat(), height.toFloat()))
    }

    fun copy() = Size(p.copy())

    override val size: Size get() = this

    override var width: Float
        set(value) = run { p.x = value }
        get() = p.x
    override var height: Float
        set(value) = run { p.y = value }
        get() = p.y

    fun setTo(width: Float, height: Float) {
        this.width = width
        this.height = height
    }
    fun setTo(that: ISize) = setTo(that.width, that.height)

    fun setToScaled(sx: Float, sy: Float) = setTo((width * sx), (height * sy))

    fun clone() = Size(width, height)

    override fun interpolateWith(ratio: Float, other: Size): Size = Size(0f, 0f).apply { setToInterpolated(ratio, this, other) }

    override fun setToInterpolated(ratio: Float, l: Size, r: Size): Unit = setTo(
        ratio.interpolate(l.width, r.width),
        ratio.interpolate(l.height, r.height)
    )

    override fun toString(): String = "Size(width=${width.niceStr}, height=${height.niceStr})"
}

interface ISizeInt {
    val width: Int
    val height: Int
}

inline class SizeInt(val size: Size) : ISizeInt {
    companion object {
        operator fun invoke(): SizeInt = SizeInt(Size(0f, 0f))
        operator fun invoke(x: Int, y: Int): SizeInt = SizeInt(Size(x.toFloat(), y.toFloat()))
    }

    fun clone() = SizeInt(size.clone())

    override var width: Int
        set(value) = run { size.width = value.toFloat() }
        get() = size.width.toInt()
    override var height: Int
        set(value) = run { size.height = value.toFloat() }
        get() = size.height.toInt()

    //override fun toString(): String = "SizeInt($width, $height)"
    override fun toString(): String = "SizeInt(width=$width, height=$height)"
}

fun SizeInt.setTo(width: Int, height: Int) {
    this.width = width
    this.height = height
}

fun SizeInt.setTo(that: SizeInt) = setTo(that.width, that.height)

fun SizeInt.setToScaled(sx: Float, sy: Float): Unit = setTo((width * sx).toInt(), (height * sy).toInt())

fun SizeInt.anchoredIn(container: RectangleInt, anchor: Anchor, out: RectangleInt = RectangleInt()): RectangleInt {
    return out.setTo(
        ((container.width - this.width) * anchor.sx).toInt(),
        ((container.height - this.height) * anchor.sy).toInt(),
        width,
        height
    )
}

operator fun SizeInt.contains(v: SizeInt): Boolean = (v.width <= width) && (v.height <= height)
operator fun SizeInt.times(v: Float) = SizeInt(Size(width.toFloat() * v, height.toFloat() * v))
operator fun SizeInt.times(v: Int) = this * v.toFloat()

fun SizeInt.getAnchorPosition(anchor: Anchor, out: PointInt = PointInt(0, 0)): PointInt =
    out.apply { setTo((width * anchor.sx).toInt(), (height * anchor.sy).toInt()) }

fun Size.toSizeInt(): SizeInt = SizeInt(this)
fun SizeInt.toSize(): Size = this.size

interface Sizeable {
    val size: Size
}

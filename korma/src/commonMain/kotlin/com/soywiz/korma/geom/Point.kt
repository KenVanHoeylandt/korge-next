@file:Suppress("NOTHING_TO_INLINE")

package com.soywiz.korma.geom

import com.soywiz.korma.internal.niceStr
import com.soywiz.korma.interpolation.*
import kotlin.math.*

interface IPoint {
    val x: Float
    val y: Float
    companion object {
        operator fun invoke(): IPoint = Point(0f, 0f)
        operator fun invoke(v: IPoint): IPoint = Point(v.x, v.y)
        operator fun invoke(x: Double, y: Double): IPoint = Point(x.toFloat(), y.toFloat())
        operator fun invoke(x: Float, y: Float): IPoint = Point(x, y)
        operator fun invoke(x: Int, y: Int): IPoint = Point(x.toFloat(), y.toFloat())

        operator fun invoke(v: Point): Point = Point(v.x, v.y)
        operator fun invoke(xy: Int): Point = Point(xy.toFloat())
        operator fun invoke(xy: Float): Point = Point(xy, xy)
        operator fun invoke(xy: Double): Point = Point(xy.toFloat())
    }
}

fun Point.Companion.middle(a: IPoint, b: IPoint): Point = Point((a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f)
fun Point.Companion.angle(a: IPoint, b: IPoint): Angle = Angle.fromRadians(acos((a.dot(b)) / (a.length * b.length)))
fun Point.Companion.compare(l: IPoint, r: IPoint): Int = compare(l.x, l.y, r.x, r.y)
fun Point.Companion.distance(a: IPoint, b: IPoint): Float = distance(a.x, a.y, b.x, b.y)
fun Point.copyFrom(that: IPoint) = setTo(that.x, that.y)
fun Point.setToAdd(p: IPoint) = this.setToAdd(this, p)
fun Point.setToSub(p: IPoint) = this.setToSub(this, p)

operator fun IPoint.plus(that: IPoint): IPoint = IPoint(x + that.x, y + that.y)
operator fun IPoint.minus(that: IPoint): IPoint = IPoint(x - that.x, y - that.y)
operator fun IPoint.times(that: IPoint): IPoint = IPoint(x * that.x, y * that.y)
operator fun IPoint.div(that: IPoint): IPoint = IPoint(x / that.x, y / that.y)

operator fun IPoint.times(scale: Double): IPoint = IPoint(x * scale, y * scale)
operator fun IPoint.div(scale: Double): IPoint = IPoint(x / scale, y / scale)
fun IPoint.distanceTo(x: Double, y: Double): Double = hypot(x - this.x, y - this.y)

operator fun IPoint.times(scale: Int): IPoint = this * scale
operator fun IPoint.div(scale: Int): IPoint = this / scale
fun IPoint.distanceTo(x: Int, y: Int): Double = this.distanceTo(x, y)

operator fun IPoint.times(scale: Float): IPoint = this * scale
operator fun IPoint.div(scale: Float): IPoint = this / scale
fun IPoint.distanceTo(x: Float, y: Float): Float = distanceTo(x, y)

infix fun IPoint.dot(that: IPoint): Float = x * that.x + y * that.y
fun IPoint.distanceTo(that: IPoint): Float = distanceTo(that.x, that.y)
fun IPoint.angleTo(other: IPoint): Angle = Angle.between(x, y, other.x, other.y)
fun IPoint.transformed(mat: Matrix, out: Point = Point()): Point = out.apply { setToTransform(mat, this) }
operator fun IPoint.get(index: Int) = when (index) {
    0 -> x; 1 -> y
    else -> throw IndexOutOfBoundsException("IPoint doesn't have $index component")
}
val IPoint.unit: IPoint get() = this / length
val IPoint.length: Float get() = hypot(x, y)
val IPoint.normalized: IPoint
    get() {
        val length = this.length
        return IPoint(x / length, y / length)
    }
val IPoint.mutable: Point get() = Point(x, y)
val IPoint.immutable: IPoint get() = IPoint(x, y)
fun IPoint.copy() = IPoint(x, y)
fun Point.setToTransform(mat: Matrix, p: IPoint): Unit = setToTransform(mat, p.x, p.y)
fun Point.setToTransform(mat: Matrix, x: Float, y: Float): Unit = setTo(mat.transformX(x, y), mat.transformY(x, y))
fun Point.setToAdd(a: IPoint, b: IPoint): Unit = setTo(a.x + b.x, a.y + b.y)
fun Point.setToSub(a: IPoint, b: IPoint): Unit = setTo(a.x - b.x, a.y - b.y)
fun Point.setToMul(a: IPoint, b: IPoint): Unit = setTo(a.x * b.x, a.y * b.y)
fun Point.setToMul(a: IPoint, s: Float): Unit = setTo(a.x * s, a.y * s)
fun Point.setToDiv(a: IPoint, b: IPoint): Unit = setTo(a.x / b.x, a.y / b.y)
operator fun Point.plusAssign(that: IPoint): Unit = run { setTo(x + that.x, y + that.y) }

data class Point(
    override var x: Float,
    override var y: Float
) : MutableInterpolable<Point>, Interpolable<Point>, Comparable<IPoint>, IPoint {
    override fun compareTo(other: IPoint): Int = compare(x, y, other.x, other.y)
    fun compareTo(other: Point): Int = compare(x, y, other.x, other.y)

    fun setToZero() = setTo(0f, 0f)
    fun setToOne() = setTo(1f, 1f)
    fun setToUp() = setTo(0f, -1f)
    fun setToDown() = setTo(0f, +1f)
    fun setToLeft() = setTo(-1f, 0f)
    fun setToRight() = setTo(+1f, 0f)

    companion object {
        val Zero: Point = Point(0f, 0f)
        val One: Point = Point(1f, 1f)
        val Up: Point = Point(0f, +1f)
        val Down: Point = Point(0f, -1f)
        val Left: Point = Point(-1f, 0f)
        val Right: Point = Point(+1f, 0f)

        //inline operator fun invoke(): Point = Point(0.0, 0.0) // @TODO: // e: java.lang.NullPointerException at org.jetbrains.kotlin.com.google.gwt.dev.js.JsAstMapper.mapFunction(JsAstMapper.java:562) (val pt = Array(1) { Point() })
        operator fun invoke(): Point = Point(0f, 0f)
        operator fun invoke(v: IPoint): Point = Point(v.x, v.y)
        operator fun invoke(x: Double, y: Double): Point = Point(x.toFloat(), y.toFloat())
        operator fun invoke(x: Int, y: Int): Point = Point(x.toFloat(), y.toFloat())
        operator fun invoke(xy: Float): Point = Point(xy, xy)
        operator fun invoke(xy: Double): Point = invoke(xy.toFloat())
        operator fun invoke(xy: Int): Point = invoke(xy.toFloat())

        /** Constructs a point from polar coordinates determined by an [angle] and a [length]. Angle 0 is pointing to the right, and the direction is counter-clock-wise */
        inline operator fun invoke(angle: Angle, length: Float = 1f): Point = fromPolar(angle, length)

        /** Constructs a point from polar coordinates determined by an [angle] and a [length]. Angle 0 is pointing to the right, and the direction is counter-clock-wise */
        fun fromPolar(angle: Angle, length: Float = 1f): Point = Point(angle.cosine * length, angle.sine * length)

        fun middle(a: Point, b: Point): Point = Point((a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f)
        fun angle(a: Point, b: Point): Angle = Angle.fromRadians(acos((a.dot(b)) / (a.length * b.length)))

        fun angle(ax: Float, ay: Float, bx: Float, by: Float): Angle = Angle.between(ax, ay, bx, by)
            //acos(((ax * bx) + (ay * by)) / (hypot(ax, ay) * hypot(bx, by)))

        fun compare(lx: Float, ly: Float, rx: Float, ry: Float): Int {
            val ret = ly.compareTo(ry)
            return if (ret == 0) lx.compareTo(rx) else ret
        }

        fun compare(l: Point, r: Point): Int = compare(l.x, l.y, r.x, r.y)

        fun angle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Angle = Angle.between(x1 - x2, y1 - y2, x1 - x3, y1 - y3)

        fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float = distance(x1, y1, x2, y2)
        fun distance(a: Point, b: Point): Float = distance(a.x, a.y, b.x, b.y)
        fun distance(a: IPointInt, b: IPointInt): Float = distance(a.x.toFloat(), a.y.toFloat(), b.x.toFloat(), b.y.toFloat())

        //val ax = x1 - x2
        //val ay = y1 - y2
        //val al = hypot(ax, ay)
        //val bx = x1 - x3
        //val by = y1 - y3
        //val bl = hypot(bx, by)
        //return acos((ax * bx + ay * by) / (al * bl))
    }

    fun setTo(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /** Updates a point from polar coordinates determined by an [angle] and a [length]. Angle 0 is pointing to the right, and the direction is counter-clock-wise */
    fun setToPolar(angle: Angle, length: Float = 1f): Unit = setTo(angle.cosine * length, angle.sine * length)

    fun neg() = setTo(-x, -y)
    fun mul(s: Float) = setTo(x * s, y * s)

    fun add(p: Point) = setToAdd(this, p)
    fun sub(p: Point) = setToSub(this, p)

    fun copyFrom(that: Point) = setTo(that.x, that.y)

    fun setToTransform(mat: Matrix, p: Point): Unit = setToTransform(mat, p.x, p.y)
    fun setToTransform(mat: Matrix, x: Float, y: Float): Unit = setTo(mat.transformX(x, y), mat.transformY(x, y))
    fun setToAdd(a: Point, b: Point): Unit = setTo(a.x + b.x, a.y + b.y)
    fun setToSub(a: Point, b: Point): Unit = setTo(a.x - b.x, a.y - b.y)
    fun setToMul(a: Point, b: Point): Unit = setTo(a.x * b.x, a.y * b.y)
    fun setToMul(a: Point, s: Float): Unit = setTo(a.x * s, a.y * s)
    fun setToDiv(a: Point, b: Point): Unit = setTo(a.x / b.x, a.y / b.y)
    fun setToDiv(a: Point, s: Float): Unit = setTo(a.x / s, a.y / s)
    operator fun plusAssign(that: Point): Unit = run { setTo(this.x + that.x, this.y + that.y) }

    operator fun plus(that: Point): Point = Point(x + that.x, y + that.y)
    operator fun minus(that: Point): Point = Point(x - that.x, y - that.y)
    operator fun times(that: Point): Point = Point(x * that.x, y * that.y)
    operator fun div(that: Point): Point = Point(x / that.x, y / that.y)
    infix fun dot(that: Point): Float = x * that.x + y * that.y

    operator fun times(scale: Float): Point = this * scale

    operator fun div(scale: Float): Point = this / scale

    fun distanceTo(x: Float, y: Float): Float = distanceTo(x, y)

    fun distanceTo(that: Point): Float = distanceTo(that.x, that.y)
    fun angleTo(other: Point): Angle = Angle.between(x, y, other.x, other.y)
    fun transformed(mat: Matrix, out: Point = Point()): Point = out.apply { setToTransform(mat, this) }
    operator fun get(index: Int) = when (index) {
        0 -> this.x; 1 -> this.y
        else -> throw IndexOutOfBoundsException("IPoint doesn't have $index component")
    }
    fun copy() = Point(this.x, this.y)


    val unit: Point get() = this / length
    val length: Float get() = hypot(x, y)

    fun normalize() {
        val length = this.length
        x /= length
        y /= length
    }

    override fun interpolateWith(ratio: Float, other: Point): Point =
        Point().apply { setToInterpolated(ratio, this, other) }

    override fun setToInterpolated(ratio: Float, l: Point, r: Point): Unit =
        setTo(ratio.interpolate(l.x, r.x), ratio.interpolate(l.y, r.y))

    override fun toString(): String = "(${x.niceStr}, ${y.niceStr})"

    fun rotate(rotation: Angle, out: Point = Point()): Unit =
        out.setToPolar(Angle.between(0f, 0f, x, y) + rotation, length)
}


val Point.unit: IPoint get() = this / length

inline fun Point.setTo(x: Float, y: Float): Unit = setTo(x, y)

interface IPointInt {
    var x: Int
    var y: Int

    companion object {
        operator fun invoke(x: Int, y: Int): IPointInt = PointInt(x, y)
    }
}

inline class PointInt(val p: Point) : IPointInt, Comparable<IPointInt> {
    override fun compareTo(other: IPointInt): Int = compare(this.x, this.y, other.x, other.y)

    companion object {
        operator fun invoke(): PointInt = PointInt(0, 0)
        operator fun invoke(x: Int, y: Int): PointInt = PointInt(Point(x.toFloat(), y.toFloat()))

        fun compare(lx: Int, ly: Int, rx: Int, ry: Int): Int {
            val ret = ly.compareTo(ry)
            return if (ret == 0) lx.compareTo(rx) else ret
        }
    }
    override var x: Int
        set(value) = run { p.x = value.toFloat() }
        get() = p.x.toInt()
    override var y: Int
        set(value) = run { p.y = value.toFloat() }
        get() = p.y.toInt()
    fun setTo(x: Int, y: Int): Unit = let { this.x = x; this.y = y }
    fun setTo(that: IPointInt): Unit = setTo(that.x, that.y)
    override fun toString(): String = "($x, $y)"
}

operator fun IPointInt.plus(that: IPointInt) = PointInt(this.x + that.x, this.y + that.y)
operator fun IPointInt.minus(that: IPointInt) = PointInt(this.x - that.x, this.y - that.y)
operator fun IPointInt.times(that: IPointInt) = PointInt(this.x * that.x, this.y * that.y)
operator fun IPointInt.div(that: IPointInt) = PointInt(this.x / that.x, this.y / that.y)
operator fun IPointInt.rem(that: IPointInt) = PointInt(this.x % that.x, this.y % that.y)

fun PointInt.toPoint(): Point = this.p

fun Point.toPointInt() = PointInt(x.toInt(), y.toInt())
fun IPoint.toPointInt() = PointInt(x.toInt(), y.toInt())
fun IPointInt.toIPoint() = Point(x.toFloat(), y.toFloat())

fun List<Point>.getPolylineLength(): Double {
    var out = 0.0
    var prev: Point? = null
    for (cur in this) {
        if (prev != null) out += prev.distanceTo(cur)
        prev = cur
    }
    return out
}
fun List<Point>.bounds(out: Rectangle = Rectangle(), bb: BoundsBuilder = BoundsBuilder()): Rectangle = bb.add(this).getBounds(out)

fun Iterable<IPoint>.getPolylineLength(): Float {
    var out = 0f
    var prev: IPoint? = null
    for (cur in this) {
        prev?.let { safePrev -> out += safePrev.distanceTo(cur) }
        prev = cur
    }
    return out
}
fun Iterable<IPoint>.bounds(out: Rectangle = Rectangle(), bb: BoundsBuilder = BoundsBuilder()): Rectangle = bb.add(this).getBounds(out)

package com.soywiz.korma.geom

import com.soywiz.korma.internal.*
import com.soywiz.korma.interpolation.*

interface IRectangle {
    val _x: Float
    val _y: Float
    val _width: Float
    val _height: Float

    companion object {
        inline operator fun invoke(x: Float, y: Float, width: Float, height: Float): IRectangle = Rectangle(x, y, width, height)
        inline operator fun invoke(x: Int, y: Int, width: Int, height: Int): IRectangle = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        inline operator fun invoke(x: Double, y: Double, width: Double, height: Double): IRectangle = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
    }
}

val IRectangle.x get() = _x
val IRectangle.y get() = _y
val IRectangle.width get() = _width
val IRectangle.height get() = _height

val IRectangle.left get() = _x
val IRectangle.top get() = _y
val IRectangle.right get() = _x + _width
val IRectangle.bottom get() = _y + _height

data class Rectangle(
    var x: Float, var y: Float,
    var width: Float, var height: Float
) : MutableInterpolable<Rectangle>, Interpolable<Rectangle>, IRectangle, Sizeable {
    val topLeft get() = Point(left, top)
    val topRight get() = Point(right, top)
    val bottomLeft get() = Point(left, bottom)
    val bottomRight get() = Point(right, bottom)

    override val _x: Float get() = x
    override val _y: Float get() = y
    override val _width: Float get() = width
    override val _height: Float get() = height

    companion object {
        operator fun invoke(): Rectangle = Rectangle(0f, 0f, 0f, 0f)
        operator fun invoke(x: Float, y: Float, width: Float, height: Float): Rectangle = Rectangle(x, y, width, height)
        operator fun invoke(x: Int, y: Int, width: Int, height: Int): Rectangle = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        operator fun invoke(x: Double, y: Double, width: Double, height: Double): Rectangle = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        operator fun invoke(topLeft: IPoint, size: ISize): Rectangle = Rectangle(topLeft.x, topLeft.y, size.width, size.height)

        fun fromBounds(left: Float, top: Float, right: Float, bottom: Float): Rectangle = Rectangle().apply { setBounds(left, top, right, bottom) }
        fun fromBounds(left: Int, top: Int, right: Int, bottom: Int): Rectangle = Rectangle().apply { setBounds(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()) }
        fun fromBounds(left: Double, top: Double, right: Double, bottom: Double): Rectangle = Rectangle().apply { setBounds(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()) }
        fun fromBounds(topLeft: Point, bottomRight: Point): Rectangle = Rectangle().apply { setBounds(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y) }

        fun isContainedIn(a: Rectangle, b: Rectangle): Boolean = a.x >= b.x && a.y >= b.y && a.x + a.width <= b.x + b.width && a.y + a.height <= b.y + b.height
    }

    val isEmpty: Boolean get() = area == 0f
    val isNotEmpty: Boolean get() = area != 0f
    val area: Float get() = width * height
    var left: Float; get() = x; set(value) = run { x = value }
    var top: Float; get() = y; set(value) = run { y = value }
    var right: Float; get() = x + width; set(value) = run { width = value - x }
    var bottom: Float; get() = y + height; set(value) = run { height = value - y }

    val position: Point get() = Point(x, y)
    override val size: Size get() = Size(width, height)

    fun setTo(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun setTo(that: Rectangle): Unit = setTo(that.x, that.y, that.width, that.height)

    fun setBounds(left: Float, top: Float, right: Float, bottom: Float) = setTo(left, top, right - left, bottom - top)

    operator fun times(scale: Float) = Rectangle(x * scale, y * scale, width * scale, height * scale)

    operator fun div(scale: Float) = Rectangle(x / scale, y / scale, width / scale, height / scale)

    operator fun contains(that: Rectangle) = isContainedIn(that, this)
    operator fun contains(that: Point) = contains(that.x, that.y)
    operator fun contains(that: IPoint) = contains(that.x, that.y)
    fun contains(x: Float, y: Float) = (x >= left && x < right) && (y >= top && y < bottom)

    infix fun intersects(that: Rectangle): Boolean = intersectsX(that) && intersectsY(that)

    infix fun intersectsX(that: Rectangle): Boolean = that.left <= this.right && that.right >= this.left
    infix fun intersectsY(that: Rectangle): Boolean = that.top <= this.bottom && that.bottom >= this.top

    fun setToIntersection(a: Rectangle, b: Rectangle) = this.apply { a.intersection(b, this) }

    infix fun intersection(that: Rectangle) = intersection(that, Rectangle())

    fun intersection(that: Rectangle, target: Rectangle = Rectangle()) = if (this intersects that) target.apply { setBounds(
        kotlin.math.max(this.left, that.left), kotlin.math.max(this.top, that.top),
        kotlin.math.min(this.right, that.right), kotlin.math.min(this.bottom, that.bottom)
    )} else null

    fun displaced(dx: Float, dy: Float) = Rectangle(this.x + dx, this.y + dy, width, height)

    fun displace(dx: Float, dy: Float) = setTo(this.x + dx, this.y + dy, this.width, this.height)

    fun place(item: Size, anchor: Anchor, scale: ScaleMode, out: Rectangle = Rectangle()): Rectangle =
        place(item.width, item.height, anchor, scale, out)

    fun place(width: Float, height: Float, anchor: Anchor, scale: ScaleMode, out: Rectangle = Rectangle()): Rectangle {
        val ow = scale.transformW(width, height, this.width, this.height)
        val oh = scale.transformH(width, height, this.width, this.height)
        val x = (this.width - ow) * anchor.sx
        val y = (this.height - oh) * anchor.sy
        out.setTo(x, y, ow, oh)
        return out
    }

    fun inflate(dx: Float, dy: Float) {
        x -= dx; width += 2f * dx
        y -= dy; height += 2f * dy
    }

    fun clone() = Rectangle(x, y, width, height)

    fun setToAnchoredRectangle(item: Rectangle, anchor: Anchor, container: Rectangle) = setToAnchoredRectangle(item.size, anchor, container)

    fun setToAnchoredRectangle(item: Size, anchor: Anchor, container: Rectangle) = setTo(
        container.x + anchor.sx * (container.width - item.width),
        container.y + anchor.sy * (container.height - item.height),
        item.width,
        item.height
    )

    //override fun toString(): String = "Rectangle([${left.niceStr}, ${top.niceStr}]-[${right.niceStr}, ${bottom.niceStr}])"
    override fun toString(): String = "Rectangle(x=${x.niceStr}, y=${y.niceStr}, width=${width.niceStr}, height=${height.niceStr})"
    fun toStringBounds(): String = "Rectangle([${left.niceStr},${top.niceStr}]-[${right.niceStr},${bottom.niceStr}])"

    override fun interpolateWith(ratio: Float, other: Rectangle): Rectangle =
        Rectangle().apply { setToInterpolated(ratio, this, other) }

    override fun setToInterpolated(ratio: Float, l: Rectangle, r: Rectangle): Unit = setTo(
        ratio.interpolate(l.x, r.x),
        ratio.interpolate(l.y, r.y),
        ratio.interpolate(l.width, r.width),
        ratio.interpolate(l.height, r.height)
    )

    fun getAnchoredPosition(anchor: Anchor, out: Point = Point()): Point =
        out.apply { setTo(left + width * anchor.sx, top + height * anchor.sy) }
}

//////////// INT

interface IRectangleInt {
    val x: Int
    val y: Int
    val width: Int
    val height: Int

    companion object {
        operator fun invoke(x: Int, y: Int, width: Int, height: Int): IRectangleInt = RectangleInt(x, y, width, height)
    }
}

val IRectangleInt.left get() = x
val IRectangleInt.top get() = y
val IRectangleInt.right get() = x + width
val IRectangleInt.bottom get() = y + height

inline class RectangleInt(val rect: Rectangle) : IRectangleInt {
    override var x: Int
        set(value) = run { rect.x = value.toFloat() }
        get() = rect.x.toInt()

    override var y: Int
        set(value) = run { rect.y = value.toFloat() }
        get() = rect.y.toInt()

    override var width: Int
        set(value) = run { rect.width = value.toFloat() }
        get() = rect.width.toInt()

    override var height: Int
        set(value) = run { rect.height = value.toFloat() }
        get() = rect.height.toInt()

    var left: Int
        set(value) = run { rect.left = value.toFloat() }
        get() = rect.left.toInt()

    var top: Int
        set(value) = run { rect.top = value.toFloat() }
        get() = rect.top.toInt()

    var right: Int
        set(value) = run { rect.right = value.toFloat() }
        get() = rect.right.toInt()

    var bottom: Int
        set(value) = run { rect.bottom = value.toFloat() }
        get() = rect.bottom.toInt()

    companion object {
        operator fun invoke() = RectangleInt(Rectangle())
        operator fun invoke(x: Int, y: Int, width: Int, height: Int) = RectangleInt(Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat()))

        fun fromBounds(left: Int, top: Int, right: Int, bottom: Int): RectangleInt =
            RectangleInt(left, top, right - left, bottom - top)
    }

    override fun toString(): String = "Rectangle(x=$x, y=$y, width=$width, height=$height)"
}

fun RectangleInt.setTo(that: RectangleInt) = setTo(that.x, that.y, that.width, that.height)

fun RectangleInt.setTo(x: Int, y: Int, width: Int, height: Int) = this.apply {
    this.x = x
    this.y = y
    this.width = width
    this.height = height
}

fun RectangleInt.setPosition(x: Int, y: Int) = this.apply { this.x = x; this.y = y }

fun RectangleInt.setSize(width: Int, height: Int) = this.apply {
    this.width = width
    this.height = height
}

fun RectangleInt.getPosition(out: PointInt = PointInt()): PointInt = out.apply { setTo(x, y) }
fun RectangleInt.getSize(out: SizeInt = SizeInt()): SizeInt = out.apply { setTo(width, height) }

val RectangleInt.position get() = getPosition()
val RectangleInt.size get() = getSize()

fun RectangleInt.setBoundsTo(left: Int, top: Int, right: Int, bottom: Int) = setTo(left, top, right - left, bottom - top)

////////////////////

operator fun IRectangleInt.contains(v: SizeInt): Boolean = (v.width <= width) && (v.height <= height)

fun IRectangleInt.anchoredIn(container: RectangleInt, anchor: Anchor, out: RectangleInt = RectangleInt()): RectangleInt =
    out.setTo(
        ((container.width - this.width) * anchor.sx).toInt(),
        ((container.height - this.height) * anchor.sy).toInt(),
        width,
        height
    )

fun IRectangleInt.getAnchorPosition(anchor: Anchor, out: PointInt = PointInt()): PointInt =
    out.apply { setTo((x + width * anchor.sx).toInt(), (y + height * anchor.sy).toInt()) }

fun Rectangle.toRectangleInt() = RectangleInt(this)
fun RectangleInt.toRectangle() = this.rect

fun IRectangle.toRectangleInt() = RectangleInt(_x.toInt(), _y.toInt(), _width.toInt(), _height.toInt())
fun IRectangleInt.toRectangle() = Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun IRectangleInt.anchor(ax: Double, ay: Double): IPointInt =
    PointInt((x + width * ax).toInt(), (y + height * ay).toInt())

inline fun IRectangleInt.anchor(ax: Number, ay: Number): IPointInt = anchor(ax.toDouble(), ay.toDouble())

val IRectangleInt.center get() = anchor(0.5, 0.5)

///////////////////////////

fun Iterable<Rectangle>.bounds(target: Rectangle = Rectangle()): Rectangle {
    var first = true
    var left = 0f
    var right = 0f
    var top = 0f
    var bottom = 0f
    for (r in this) {
        if (first) {
            left = r.left
            right = r.right
            top = r.top
            bottom = r.bottom
            first = false
        } else {
            left = kotlin.math.min(left, r.left)
            right = kotlin.math.max(right, r.right)
            top = kotlin.math.min(top, r.top)
            bottom = kotlin.math.max(bottom, r.bottom)
        }
    }
    target.setBounds(left, top, right, bottom)
    return target
}

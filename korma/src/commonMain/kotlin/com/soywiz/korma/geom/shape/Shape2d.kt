package com.soywiz.korma.geom.shape

import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.bezier.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.internal.*
import kotlin.math.*

abstract class Shape2d {
    abstract val paths: List<IPointArrayList>
    abstract val closed: Boolean
    open fun containsPoint(x: Float, y: Float) = false

    interface WithArea {
        val area: Float
    }

    object Empty : Shape2d(), WithArea {
        override val paths: List<PointArrayList> = listOf(PointArrayList(0))
        override val closed: Boolean = false
        override val area: Float = 0f
        override fun containsPoint(x: Float, y: Float) = false
    }

    data class Line(val x0: Float, val y0: Float, val x1: Float, val y1: Float) : Shape2d(), WithArea {
        companion object {
            operator fun invoke(x0: Double, y0: Double, x1: Double, y1: Double) = Line(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat())
            operator fun invoke(x0: Int, y0: Int, x1: Int, y1: Int) = Line(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat())
        }

        override val paths get() = listOf(PointArrayList(2).apply { add(x0, y0).add(x1, y1) })
        override val closed: Boolean = false
        override val area: Float get() = 0f
        override fun containsPoint(x: Float, y: Float) = false
    }

    data class Circle(val x: Float, val y: Float, val radius: Float, val totalPoints: Int = 32) : Shape2d(), WithArea {
        companion object {
            operator fun invoke(x: Float, y: Float, radius: Float, totalPoints: Int = 32) = Circle(x, y, radius, totalPoints)
            operator fun invoke(x: Double, y: Double, radius: Double, totalPoints: Int = 32) = Circle(x.toFloat(), y.toFloat(), radius.toFloat(), totalPoints)
            operator fun invoke(x: Int, y: Int, radius: Int, totalPoints: Int = 32) = Circle(x.toFloat(), y.toFloat(), radius.toFloat(), totalPoints)
        }

        override val paths by lazy {
            listOf(PointArrayList(totalPoints) {
                for (it in 0 until totalPoints) {
                    add(
                        x + Angle.cos01(it.toFloat() / totalPoints.toFloat()) * radius,
                        y + Angle.sin01(it.toFloat() / totalPoints.toFloat()) * radius
                    )
                }
            })
        }
        override val closed: Boolean = true
        override val area: Float get() = PI.toFloat() * radius * radius
        override fun containsPoint(x: Float, y: Float) = hypot(this.x - x, this.y - y) < radius
    }

    data class Rectangle(val rect: com.soywiz.korma.geom.Rectangle) : Shape2d(), WithArea, IRectangle by rect {
        companion object {
            inline operator fun invoke(x: Float, y: Float, width: Float, height: Float) = Rectangle(com.soywiz.korma.geom.Rectangle(x, y, width, height))
            inline operator fun invoke(x: Double, y: Double, width: Double, height: Double) = Rectangle(com.soywiz.korma.geom.Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat()))
            inline operator fun invoke(x: Int, y: Int, width: Int, height: Int) = Rectangle(com.soywiz.korma.geom.Rectangle(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat()))
        }

        override val paths = listOf(PointArrayList(4) { add(x, y).add(x + width, y).add(x + width, y + height).add(x, y + height) })
        override val closed: Boolean = true
        override val area: Float get() = width * height
        override fun containsPoint(x: Float, y: Float) = (x in this.left..this.right) && (y in this.top..this.bottom)
        override fun toString(): String =
            "Rectangle(x=${x.niceStr}, y=${y.niceStr}, width=${width.niceStr}, height=${height.niceStr})"
    }

    data class Polygon(val points: IPointArrayList) : Shape2d() {
        override val paths = listOf(points)
        override val closed: Boolean = true
        override fun containsPoint(x: Float, y: Float): Boolean = this.points.contains(x, y)
    }

    data class Polyline(val points: IPointArrayList) : Shape2d(), WithArea {
        override val paths = listOf(points)
        override val closed: Boolean = false
        override val area: Float get() = 0f
        override fun containsPoint(x: Float, y: Float) = false
    }

    data class Complex(val items: List<Shape2d>) : Shape2d() {
        override val paths by lazy { items.flatMap { it.paths } }
        override val closed: Boolean = false
        override fun containsPoint(x: Float, y: Float): Boolean = this.getAllPoints().contains(x, y)
    }
}

val List<IPointArrayList>.totalVertices get() = map { it.size }.sum()

fun BoundsBuilder.add(shape: Shape2d) {
    for (path in shape.paths) add(path)
}

val Shape2d.bounds: Rectangle get() = BoundsBuilder().apply { add(this@bounds) }.getBounds()

fun IRectangle.toShape() = Shape2d.Rectangle(x, y, width, height)

// @TODO: Instead of use curveSteps, let's determine the maximum distance between points for the curve, or the maximum angle (so we have a quality factor instead)
inline fun VectorPath.emitPoints(flush: (close: Boolean) -> Unit, emit: (x: Float, y: Float) -> Unit, curveSteps: Int = 20) {
    var lx = 0f
    var ly = 0f
    flush(false)
    visitCmds(
        moveTo = { x, y -> emit(x, y).also { lx = x }.also { ly = y } },
        lineTo = { x, y -> emit(x, y).also { lx = x }.also { ly = y } },
        quadTo = { x0, y0, x1, y1 ->
            val dt = 1f / curveSteps
            for (n in 1 until curveSteps) Bezier.quadCalc(lx, ly, x0, y0, x1, y1, n * dt, emit)
            run { lx = x1 }.also { ly = y1 }
        },
        cubicTo = { x0, y0, x1, y1, x2, y2 ->
            val dt = 1f / curveSteps
            for (n in 1 until curveSteps) Bezier.cubicCalc(lx, ly, x0, y0, x1, y1, x2, y2, n * dt, emit)
            run { lx = x2 }.also { ly = y2 }

        },
        close = { flush(true) }
    )
    flush(false)
}

inline fun VectorPath.emitPoints2(
    crossinline flush: (close: Boolean) -> Unit = {},
    crossinline joint: (close: Boolean) -> Unit = {},
    crossinline emit: (x: Float, y: Float, move: Boolean) -> Unit
) {
    var ix = 0f
    var iy = 0f
    var lx = 0f
    var ly = 0f
    flush(false)
    this.visitCmds(
        moveTo = { x, y ->
            ix = x
            iy = y
            emit(x, y, true).also { lx = x }.also { ly = y }
        },
        lineTo = { x, y ->
            emit(x, y, false).also { lx = x }.also { ly = y }
            joint(false)
        },
        quadTo = { x0, y0, x1, y1 ->
            val sum = Point.distance(lx, ly, x0, y0) + Point.distance(x0, y0, x1, y1)
            approximateCurve(sum.toInt(), { ratio, get -> Bezier.quadCalc(lx, ly, x0, y0, x1, y1, ratio) { x, y -> get(x, y) } }) { x, y -> emit(x, y, false) }
            run { lx = x1 }.also { ly = y1 }
            joint(false)
        },
        cubicTo = { x0, y0, x1, y1, x2, y2 ->
            val sum = Point.distance(lx, ly, x0, y0) + Point.distance(x0, y0, x1, y1) + Point.distance(x1, y1, x2, y2)
            approximateCurve(sum.toInt(), { ratio, get -> Bezier.cubicCalc(lx, ly, x0, y0, x1, y1, x2, y2, ratio) { x, y -> get(x, y) }}) { x, y -> emit(x, y, false) }
            run { lx = x2 }.also { ly = y2 }
            joint(false)
        },
        close = {
            emit(ix, iy, false)
            joint(true)
            flush(true)
        }
    )
    flush(false)
}

fun VectorPath.getPoints2(out: PointArrayList = PointArrayList()): PointArrayList {
    emitPoints2 { x, y, move -> out.add(x, y) }
    return out
}

fun buildPath(out: VectorPath = VectorPath(), block: VectorPath.() -> Unit): VectorPath = out.apply(block)
inline fun buildPath(out: VectorPath = VectorPath(), winding: Winding = Winding.EVEN_ODD, block: VectorPath.() -> Unit): VectorPath = out.also { it.winding = winding }.apply(block)

inline fun approximateCurve(
    curveSteps: Int,
    crossinline compute: (ratio: Float, get: (x: Float, y: Float) -> Unit) -> Unit,
    crossinline emit: (x: Float, y: Float) -> Unit
) {
    val rcurveSteps = max(curveSteps, 20)
    val dt = 1f / rcurveSteps
    var lastX = 0f
    var lastY = 0f
    var prevX = 0f
    var prevY = 0f
    var emittedCount = 0
    compute(0f) { x, y ->
        lastX = x
        lastY = y
    }
    for (n in 1 until rcurveSteps) {
        val ratio = n * dt
        //println("ratio: $ratio")
        compute(ratio) { x, y ->
            //if (emittedCount == 0) {
            run {
                emit(x, y)
                emittedCount++
                lastX = prevX
                lastY = prevY
            }

            prevX = x
            prevY = y
        }
    }
    //println("curveSteps: $rcurveSteps, emittedCount=$emittedCount")
}

fun IPointArrayList.toRectangleOrNull(): Shape2d.Rectangle? {
    if (this.size != 4) return null
    //check there are only unique points
    val points = setOf(getX(0) to getY(0), getX(1) to getY(1), getX(2) to getY(2), getX(3) to getY(3))
    if (points.size != 4) return null
    //check there are exactly two unique x/y coordinates
    val xs = setOf(getX(0), getX(1), getX(2), getX(3))
    val ys = setOf(getY(0), getY(1), getY(2), getY(3))
    if (xs.size != 2 || ys.size != 2) return null
    //get coordinates
    val left = xs.min() ?: return null
    val right = xs.max() ?: return null
    val top = ys.max() ?: return null
    val bottom = ys.min() ?: return null
    return Shape2d.Rectangle(Rectangle.fromBounds(top, left, right, bottom))
}

fun IPointArrayList.toShape2d(closed: Boolean = true): Shape2d {
    if (closed && this.size == 4) {
        val x0 = this.getX(0)
        val y0 = this.getY(0)
        val x1 = this.getX(2)
        val y1 = this.getY(2)
        if (this.getX(1) == x1 && this.getY(1) == y0 && this.getX(3) == x0 && this.getY(3) == y1) {
            return Shape2d.Rectangle(Rectangle.fromBounds(x0, y0, x1, y1))
        }
    }
    return if (closed) Shape2d.Polygon(this) else Shape2d.Polyline(this)
}

fun VectorPath.toShape2d(closed: Boolean = true): Shape2d {
    val items = toPathList().map { it.toShape2d(closed) }
    return when (items.size) {
        0 -> Shape2d.Empty
        1 -> items.first()
        else -> Shape2d.Complex(items)
    }
}

fun VectorPath.toPathList(): List<IPointArrayList> {
    val paths = arrayListOf<IPointArrayList>()
    var path = PointArrayList()
    emitPoints({
        if (path.isNotEmpty()) {
            paths += path
            path = PointArrayList()
        }
    }, { x, y ->
        path.add(x, y)
    })
    return paths
}

fun Shape2d.getAllPoints(out: PointArrayList = PointArrayList()): PointArrayList = out.apply { for (path in this@getAllPoints.paths) add(path) }
fun Shape2d.toPolygon(): Shape2d.Polygon = if (this is Shape2d.Polygon) this else Shape2d.Polygon(this.getAllPoints())

fun List<IPoint>.containsPoint(x: Float, y: Float): Boolean {
    var intersections = 0
    for (n in 0 until this.size - 1) {
        val p1 = this[n + 0]
        val p2 = this[n + 1]
        intersections += HorizontalLine.intersectionsWithLine(x, y, p1.x, p1.y, p2.x, p2.y)
    }
    return (intersections % 2) != 0
}

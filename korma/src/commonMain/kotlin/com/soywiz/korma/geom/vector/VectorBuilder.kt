package com.soywiz.korma.geom.vector

import com.soywiz.korma.annotations.KorDslMarker
import com.soywiz.korma.geom.*
import kotlin.math.*

@KorDslMarker
interface VectorBuilder {
    val totalPoints: Int
    val lastX: Float
    val lastY: Float
    fun moveTo(x: Float, y: Float)
    fun lineTo(x: Float, y: Float)
    fun quadTo(cx: Float, cy: Float, ax: Float, ay: Float) {
        val x1 = lastX
        val y1 = lastY
        val x2 = ax
        val y2 = ay
        val tt = (2f / 3f)
        val cx1 = x1 + (tt * (cx - x1))
        val cy1 = y1 + (tt * (cy - y1))
        val cx2 = x2 + (tt * (cx - x2))
        val cy2 = y2 + (tt * (cy - y2))
        return cubicTo(cx1, cy1, cx2, cy2, x2, y2)
    }
    fun cubicTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float)
    fun close()
}

fun VectorBuilder.isEmpty() = totalPoints == 0
fun VectorBuilder.isNotEmpty() = totalPoints != 0

//fun arcTo(b: Point2d, a: Point2d, c: Point2d, r: Double) {
fun VectorBuilder.arcTo(ax: Float, ay: Float, cx: Float, cy: Float, r: Float) {
    if (isEmpty()) moveTo(ax, ay)
    val bx = lastX
    val by = lastY
    val b = IPoint(bx, by)
    val a = IPoint(ax, ay)
    val c = IPoint(cx, cy)
    val AB = b - a
    val AC = c - a
    val angle = Point.angle(AB, AC).radians * 0.5f
    val x = r * sin((PI.toFloat() / 2f) - angle) / sin(angle)
    val A = a + AB.unit * x
    val B = a + AC.unit * x
    lineTo(A.x, A.y)
    quadTo(a.x, a.y, B.x, B.y)
}
fun VectorBuilder.arcTo(ax: Double, ay: Double, cx: Double, cy: Double, r: Double) = arcTo(ax.toFloat(), ay.toFloat(), cx.toFloat(), cy.toFloat(), r.toFloat())
fun VectorBuilder.arcTo(ax: Int, ay: Int, cx: Int, cy: Int, r: Int) = arcTo(ax.toFloat(), ay.toFloat(), cx.toFloat(), cy.toFloat(), r.toFloat())

fun VectorBuilder.rect(rect: Rectangle) = rect(rect.x, rect.y, rect.width, rect.height)
fun VectorBuilder.rect(x: Float, y: Float, width: Float, height: Float) {
    moveTo(x, y)
    lineTo(x + width, y)
    lineTo(x + width, y + height)
    lineTo(x, y + height)
    close()
}
fun VectorBuilder.rect(x: Double, y: Double, width: Double, height: Double) = rect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
fun VectorBuilder.rect(x: Int, y: Int, width: Int, height: Int) = rect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun VectorBuilder.rectHole(x: Float, y: Float, width: Float, height: Float) {
    moveTo(x, y)
    lineTo(x, y + height)
    lineTo(x + width, y + height)
    lineTo(x + width, y)
    close()
}

fun VectorBuilder.roundRect(x: Float, y: Float, w: Float, h: Float, rx: Float, ry: Float = rx) {
    if (rx == 0f && ry == 0f) {
        rect(x, y, w, h)
    } else {
        val r = if (w < 2f * rx) w / 2f else if (h < 2 * rx) h / 2f else rx
        this.moveTo(x + r, y)
        this.arcTo(x + w, y, x + w, y + h, r)
        this.arcTo(x + w, y + h, x, y + h, r)
        this.arcTo(x, y + h, x, y, r)
        this.arcTo(x, y, x + w, y, r)
    }
}
fun VectorBuilder.roundRect(x: Double, y: Double, w: Double, h: Double, rx: Double, ry: Double = rx) = roundRect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), rx.toFloat(), ry.toFloat())
fun VectorBuilder.roundRect(x: Int, y: Int, w: Int, h: Int, rx: Int, ry: Int = rx) = roundRect(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), rx.toFloat(), ry.toFloat())

fun VectorBuilder.rectHole(x: Double, y: Double, width: Double, height: Double) = rectHole(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
fun VectorBuilder.rectHole(x: Int, y: Int, width: Int, height: Int) = rectHole(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun VectorBuilder.arc(x: Float, y: Float, r: Float, start: Angle, end: Angle) {
    // http://hansmuller-flex.blogspot.com.es/2011/04/approximating-circular-arc-with-cubic.html
    val EPSILON = 0.00001
    val PI_TWO = PI.toFloat() * 2f
    val PI_OVER_TWO = PI.toFloat() / 2f

    val startAngle = start.radians % PI_TWO
    val endAngle = end.radians % PI_TWO
    var remainingAngle = min(PI_TWO, abs(endAngle - startAngle))
    if (remainingAngle == 0f && start != end) remainingAngle = PI_TWO
    val sgn = if (startAngle < endAngle) +1f else -1f
    var a1 = startAngle
    val p1 = Point()
    val p2 = Point()
    val p3 = Point()
    val p4 = Point()
    var index = 0
    while (remainingAngle > EPSILON) {
        val a2 = a1 + sgn * min(remainingAngle, PI_OVER_TWO)

        val k = 0.5522847498f
        val a = (a2 - a1) / 2f
        val x4 = r * cos(a)
        val y4 = r * sin(a)
        val x1 = x4
        val y1 = -y4
        val f = k * tan(a)
        val x2 = x1 + f * y4
        val y2 = y1 + f * x4
        val x3 = x2
        val y3 = -y2
        val ar = a + a1
        val cos_ar = cos(ar)
        val sin_ar = sin(ar)
        p1.setTo(x + r * cos(a1), y + r * sin(a1))
        p2.setTo(x + x2 * cos_ar - y2 * sin_ar, y + x2 * sin_ar + y2 * cos_ar)
        p3.setTo(x + x3 * cos_ar - y3 * sin_ar, y + x3 * sin_ar + y3 * cos_ar)
        p4.setTo(x + r * cos(a2), y + r * sin(a2))

        if (index == 0) moveTo(p1.x, p1.y)
        cubicTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)

        index++
        remainingAngle -= abs(a2 - a1)
        a1 = a2
    }
    if (startAngle == endAngle && index != 0) {
        close()
    }
}
fun VectorBuilder.arc(x: Double, y: Double, r: Double, start: Angle, end: Angle) = arc(x.toFloat(), y.toFloat(), r.toFloat(), start, end)
fun VectorBuilder.arc(x: Int, y: Int, r: Int, start: Angle, end: Angle) = arc(x.toFloat(), y.toFloat(), r.toFloat(), start, end)

fun VectorBuilder.circle(point: Point, radius: Float) = arc(point.x, point.y, radius, 0.degrees, 360.degrees)
fun VectorBuilder.circle(x: Float, y: Float, radius: Float) = arc(x, y, radius, 0.degrees, 360.degrees)
fun VectorBuilder.circle(x: Double, y: Double, radius: Double) = circle(x.toFloat(), y.toFloat(), radius.toFloat())
fun VectorBuilder.circle(x: Int, y: Int, radius: Int) = circle(x.toFloat(), y.toFloat(), radius.toFloat())

fun VectorBuilder.ellipse(x: Float, y: Float, rw: Float, rh: Float) {
    val k = .5522848f
    val ox = (rw / 2f) * k
    val oy = (rh / 2f) * k
    val xe = x + rw
    val ye = y + rh
    val xm = x + rw / 2f
    val ym = y + rh / 2f
    moveTo(x, ym)
    cubicTo(x, ym - oy, xm - ox, y, xm, y)
    cubicTo(xm + ox, y, xe, ym - oy, xe, ym)
    cubicTo(xe, ym + oy, xm + ox, ye, xm, ye)
    cubicTo(xm - ox, ye, x, ym + oy, x, ym)
}
fun VectorBuilder.ellipse(x: Double, y: Double, rw: Double, rh: Double) = ellipse(x.toFloat(), y.toFloat(), rw.toFloat(), rh.toFloat())
fun VectorBuilder.ellipse(x: Int, y: Int, rw: Int, rh: Int) = ellipse(x.toFloat(), y.toFloat(), rw.toFloat(), rh.toFloat())

fun VectorBuilder.moveTo(p: Point) = moveTo(p.x, p.y)
fun VectorBuilder.lineTo(p: Point) = lineTo(p.x, p.y)
fun VectorBuilder.quadTo(c: Point, a: Point) = quadTo(c.x, c.y, a.x, a.y)
fun VectorBuilder.cubicTo(c1: Point, c2: Point, a: Point) = cubicTo(c1.x, c1.y, c2.x, c2.y, a.x, a.y)


fun VectorBuilder.moveToH(x: Float) = moveTo(x, lastY)
fun VectorBuilder.moveToH(x: Double) = moveToH(x.toFloat())
fun VectorBuilder.moveToH(x: Int) = moveToH(x.toDouble())

fun VectorBuilder.rMoveToH(x: Float) = rMoveTo(x, 0f)
fun VectorBuilder.rMoveToH(x: Double) = rMoveToH(x.toFloat())
fun VectorBuilder.rMoveToH(x: Int) = rMoveToH(x.toFloat())

fun VectorBuilder.moveToV(y: Float) = moveTo(lastX, y)
fun VectorBuilder.moveToV(y: Double) = moveToV(y.toFloat())
fun VectorBuilder.moveToV(y: Int) = moveToV(y.toFloat())

fun VectorBuilder.rMoveToV(y: Float) = rMoveTo(0f, y)
fun VectorBuilder.rMoveToV(y: Double) = rMoveToV(y.toFloat())
fun VectorBuilder.rMoveToV(y: Int) = rMoveToV(y.toFloat())

fun VectorBuilder.lineToH(x: Float) = lineTo(x, lastY)
fun VectorBuilder.lineToH(x: Double) = lineToH(x.toFloat())
fun VectorBuilder.lineToH(x: Int) = lineToH(x.toFloat())

fun VectorBuilder.rLineToH(x: Float) = rLineTo(x, 0f)
fun VectorBuilder.rLineToH(x: Double) = rLineToH(x.toFloat())
fun VectorBuilder.rLineToH(x: Int) = rLineToH(x.toFloat())

fun VectorBuilder.lineToV(y: Float) = lineTo(lastX, y)
fun VectorBuilder.lineToV(y: Double) = lineToV(y.toFloat())
fun VectorBuilder.lineToV(y: Int) = lineToV(y.toFloat())

fun VectorBuilder.rLineToV(y: Float) = rLineTo(0f, y)
fun VectorBuilder.rLineToV(y: Double) = rLineToV(y.toFloat())
fun VectorBuilder.rLineToV(y: Int) = rLineToV(y.toFloat())

fun VectorBuilder.rMoveTo(x: Float, y: Float) = moveTo(this.lastX + x, this.lastY + y)
fun VectorBuilder.rMoveTo(x: Double, y: Double) = rMoveTo(x.toFloat(), y.toFloat())
fun VectorBuilder.rMoveTo(x: Int, y: Int) = rMoveTo(x.toFloat(), y.toFloat())

fun VectorBuilder.rLineTo(x: Float, y: Float) = lineTo(this.lastX + x, this.lastY + y)
fun VectorBuilder.rLineTo(x: Double, y: Double) = rLineTo(x.toFloat(), y.toFloat())
fun VectorBuilder.rLineTo(x: Int, y: Int) = rLineTo(x.toFloat(), y.toFloat())

fun VectorBuilder.rQuadTo(cx: Float, cy: Float, ax: Float, ay: Float) = quadTo(this.lastX + cx, this.lastY + cy, this.lastX + ax, this.lastY + ay)
fun VectorBuilder.rQuadTo(cx: Double, cy: Double, ax: Double, ay: Double) = rQuadTo(cx.toFloat(), cy.toFloat(), ax.toFloat(), ay.toFloat())
fun VectorBuilder.rQuadTo(cx: Int, cy: Int, ax: Int, ay: Int) = rQuadTo(cx.toFloat(), cy.toFloat(), ax.toFloat(), ay.toFloat())

fun VectorBuilder.rCubicTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float) = cubicTo(this.lastX + cx1, this.lastY + cy1, this.lastX + cx2, this.lastY + cy2, this.lastX + ax, this.lastY + ay)
fun VectorBuilder.rCubicTo(cx1: Double, cy1: Double, cx2: Double, cy2: Double, ax: Double, ay: Double) = rCubicTo(cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())
fun VectorBuilder.rCubicTo(cx1: Int, cy1: Int, cx2: Int, cy2: Int, ax: Int, ay: Int) = rCubicTo(cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())

fun VectorBuilder.moveTo(x: Double, y: Double) = moveTo(x.toFloat(), y.toFloat())
fun VectorBuilder.moveTo(x: Int, y: Int) = moveTo(x.toFloat(), y.toFloat())

fun VectorBuilder.lineTo(x: Double, y: Double) = lineTo(x.toFloat(), y.toFloat())
fun VectorBuilder.lineTo(x: Int, y: Int) = lineTo(x.toFloat(), y.toFloat())

fun VectorBuilder.quadTo(controlX: Double, controlY: Double, anchorX: Double, anchorY: Double) = quadTo(controlX.toFloat(), controlY.toFloat(), anchorX.toFloat(), anchorY.toFloat())
fun VectorBuilder.quadTo(controlX: Int, controlY: Int, anchorX: Int, anchorY: Int) = quadTo(controlX.toFloat(), controlY.toFloat(), anchorX.toFloat(), anchorY.toFloat())

fun VectorBuilder.cubicTo(cx1: Double, cy1: Double, cx2: Double, cy2: Double, ax: Double, ay: Double) = cubicTo(cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())
fun VectorBuilder.cubicTo(cx1: Int, cy1: Int, cx2: Int, cy2: Int, ax: Int, ay: Int) = cubicTo(cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())

fun VectorBuilder.line(p0: Point, p1: Point) = line(p0.x, p0.y, p1.x, p1.y)
fun VectorBuilder.line(x0: Float, y0: Float, x1: Float, y1: Float) = moveTo(x0, y0).also { lineTo(x1, y1) }
fun VectorBuilder.line(x0: Double, y0: Double, x1: Double, y1: Double) = line(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat())
fun VectorBuilder.line(x0: Int, y0: Int, x1: Int, y1: Int) = line(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat())

fun VectorBuilder.quad(x0: Float, y0: Float, controlX: Float, controlY: Float, anchorX: Float, anchorY: Float) = moveTo(x0, y0).also { quadTo(controlX, controlY, anchorX, anchorY) }
fun VectorBuilder.quad(x0: Double, y0: Double, controlX: Double, controlY: Double, anchorX: Double, anchorY: Double) = quad(x0.toFloat(), y0.toFloat(), controlX.toFloat(), controlY.toFloat(), anchorX.toFloat(), anchorY.toFloat())
fun VectorBuilder.quad(x0: Int, y0: Int, controlX: Int, controlY: Int, anchorX: Int, anchorY: Int) = quad(x0.toFloat(), y0.toFloat(), controlX.toFloat(), controlY.toFloat(), anchorX.toFloat(), anchorY.toFloat())

fun VectorBuilder.cubic(x0: Float, y0: Float, cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float) = moveTo(x0, y0).also { cubicTo(cx1, cy1, cx2, cy2, ax, ay) }
fun VectorBuilder.cubic(x0: Double, y0: Double, cx1: Double, cy1: Double, cx2: Double, cy2: Double, ax: Double, ay: Double) = cubic(x0.toFloat(), y0.toFloat(), cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())
fun VectorBuilder.cubic(x0: Int, y0: Int, cx1: Int, cy1: Int, cx2: Int, cy2: Int, ax: Int, ay: Int) = cubic(x0.toFloat(), y0.toFloat(), cx1.toFloat(), cy1.toFloat(), cx2.toFloat(), cy2.toFloat(), ax.toFloat(), ay.toFloat())

fun VectorBuilder.quad(o: Point, c: Point, a: Point) = quad(o.x, o.y, c.x, c.y, a.x, a.y)
fun VectorBuilder.cubic(o: Point, c1: Point, c2: Point, a: Point) = cubic(o.x, o.y, c1.x, c1.y, c2.x, c2.y, a.x, a.y)

// Variants supporting relative and absolute modes

fun VectorBuilder.rCubicTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float, relative: Boolean) = if (relative) rCubicTo(cx1, cy1, cx2, cy2, ax, ay) else cubicTo(cx1, cy1, cx2, cy2, ax, ay)
fun VectorBuilder.rCubicTo(cx1: Double, cy1: Double, cx2: Double, cy2: Double, ax: Double, ay: Double, relative: Boolean) = if (relative) rCubicTo(cx1, cy1, cx2, cy2, ax, ay) else cubicTo(cx1, cy1, cx2, cy2, ax, ay)
fun VectorBuilder.rCubicTo(cx1: Int, cy1: Int, cx2: Int, cy2: Int, ax: Int, ay: Int, relative: Boolean) = if (relative) rCubicTo(cx1, cy1, cx2, cy2, ax, ay) else cubicTo(cx1, cy1, cx2, cy2, ax, ay)

fun VectorBuilder.rQuadTo(cx: Float, cy: Float, ax: Float, ay: Float, relative: Boolean) = if (relative) rQuadTo(cx, cy, ax, ay) else quadTo(cx, cy, ax, ay)
fun VectorBuilder.rQuadTo(cx: Double, cy: Double, ax: Double, ay: Double, relative: Boolean) = if (relative) rQuadTo(cx, cy, ax, ay) else quadTo(cx, cy, ax, ay)
fun VectorBuilder.rQuadTo(cx: Int, cy: Int, ax: Int, ay: Int, relative: Boolean) = if (relative) rQuadTo(cx, cy, ax, ay) else quadTo(cx, cy, ax, ay)

fun VectorBuilder.rLineTo(ax: Float, ay: Float, relative: Boolean) = if (relative) rLineTo(ax, ay) else lineTo(ax, ay)
fun VectorBuilder.rLineTo(ax: Double, ay: Double, relative: Boolean) = if (relative) rLineTo(ax, ay) else lineTo(ax, ay)
fun VectorBuilder.rLineTo(ax: Int, ay: Int, relative: Boolean) = if (relative) rLineTo(ax, ay) else lineTo(ax, ay)

fun VectorBuilder.rMoveTo(ax: Float, ay: Float, relative: Boolean) = if (relative) rMoveTo(ax, ay) else moveTo(ax, ay)
fun VectorBuilder.rMoveTo(ax: Double, ay: Double, relative: Boolean) = if (relative) rMoveTo(ax, ay) else moveTo(ax, ay)
fun VectorBuilder.rMoveTo(ax: Int, ay: Int, relative: Boolean) = if (relative) rMoveTo(ax, ay) else moveTo(ax, ay)

fun VectorBuilder.rMoveToH(ax: Float, relative: Boolean) = if (relative) rMoveToH(ax) else moveToH(ax)
fun VectorBuilder.rMoveToH(ax: Double, relative: Boolean) = if (relative) rMoveToH(ax) else moveToH(ax)
fun VectorBuilder.rMoveToH(ax: Int, relative: Boolean) = if (relative) rMoveToH(ax) else moveToH(ax)

fun VectorBuilder.rMoveToV(ay: Float, relative: Boolean) = if (relative) rMoveToV(ay) else moveToV(ay)
fun VectorBuilder.rMoveToV(ay: Double, relative: Boolean) = if (relative) rMoveToV(ay) else moveToV(ay)
fun VectorBuilder.rMoveToV(ay: Int, relative: Boolean) = if (relative) rMoveToV(ay) else moveToV(ay)

fun VectorBuilder.rLineToH(ax: Float, relative: Boolean) = if (relative) rLineToH(ax) else lineToH(ax)
fun VectorBuilder.rLineToH(ax: Double, relative: Boolean) = if (relative) rLineToH(ax) else lineToH(ax)
fun VectorBuilder.rLineToH(ax: Int, relative: Boolean) = if (relative) rLineToH(ax) else lineToH(ax)

fun VectorBuilder.rLineToV(ay: Float, relative: Boolean) = if (relative) rLineToV(ay) else lineToV(ay)
fun VectorBuilder.rLineToV(ay: Double, relative: Boolean) = if (relative) rLineToV(ay) else lineToV(ay)
fun VectorBuilder.rLineToV(ay: Int, relative: Boolean) = if (relative) rLineToV(ay) else lineToV(ay)

fun VectorBuilder.transformed(m: Matrix): VectorBuilder {
    val im = m.inverted()
    val parent = this
    return object : VectorBuilder {
        override val lastX: Float get() = im.transformX(parent.lastX, parent.lastY)
        override val lastY: Float get() = im.transformY(parent.lastX, parent.lastY)
        override val totalPoints: Int = parent.totalPoints

        fun tX(x: Float, y: Float) = m.transformX(x, y)
        fun tY(x: Float, y: Float) = m.transformY(x, y)

        override fun close() = parent.close()
        override fun lineTo(x: Float, y: Float) = parent.lineTo(tX(x, y), tY(x, y))
        override fun moveTo(x: Float, y: Float) = parent.lineTo(tX(x, y), tY(x, y))
        override fun quadTo(cx: Float, cy: Float, ax: Float, ay: Float) = parent.quadTo(
            tX(cx, cy), tY(cx, cy),
            tX(ax, ay), tY(ax, ay)
        )
        override fun cubicTo(cx1: Float, cy1: Float, cx2: Float, cy2: Float, ax: Float, ay: Float) = parent.cubicTo(
            tX(cx1, cy1), tY(cx1, cy1),
            tX(cx2, cy2), tY(cx2, cy2),
            tX(ax, ay), tY(ax, ay)
        )
    }
}

fun <T> VectorBuilder.transformed(m: Matrix, block: VectorBuilder.() -> T): T = block(this.transformed(m))

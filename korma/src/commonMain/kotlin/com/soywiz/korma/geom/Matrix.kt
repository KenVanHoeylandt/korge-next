@file:Suppress("NOTHING_TO_INLINE")

package com.soywiz.korma.geom

import com.soywiz.korma.interpolation.Interpolable
import com.soywiz.korma.interpolation.MutableInterpolable
import com.soywiz.korma.interpolation.interpolate
import kotlin.math.*

data class Matrix(
    var a: Float = 1f,
    var b: Float = 0f,
    var c: Float = 0f,
    var d: Float = 1f,
    var tx: Float = 0f,
    var ty: Float = 0f
) : MutableInterpolable<Matrix>, Interpolable<Matrix> {
    companion object {
        inline operator fun invoke(m: Matrix, out: Matrix = Matrix()): Matrix = out.apply { copyFrom(m) }
        inline operator fun invoke(a: Int, b: Int = 0, c: Int = 0, d: Int = 1, tx: Int = 0, ty: Int = 0) =
            Matrix(a.toFloat(), b.toFloat(), c.toFloat(), d.toFloat(), tx.toFloat(), ty.toFloat())
        inline operator fun invoke(a: Double, b: Double = 0.0, c: Double = 0.0, d: Double = 1.0, tx: Double = 0.0, ty: Double = 0.0) =
            Matrix(a.toFloat(), b.toFloat(), c.toFloat(), d.toFloat(), tx.toFloat(), ty.toFloat())
    }
    enum class Type(val id: Int, val hasRotation: Boolean, val hasScale: Boolean, val hasTranslation: Boolean) {
        IDENTITY(1, hasRotation = false, hasScale = false, hasTranslation = false),
        TRANSLATE(2, hasRotation = false, hasScale = false, hasTranslation = true),
        SCALE(3, hasRotation = false, hasScale = true, hasTranslation = false),
        SCALE_TRANSLATE(4, hasRotation = false, hasScale = true, hasTranslation = true),
        COMPLEX(5, hasRotation = true, hasScale = true, hasTranslation = true);
    }

    fun getType(): Type {
        val hasRotation = b != 0f || c != 0f
        val hasScale = a != 1f || d != 1f
        val hasTranslation = tx != 0f || ty != 0f

        return when {
            hasRotation -> Type.COMPLEX
            hasScale && hasTranslation -> Type.SCALE_TRANSLATE
            hasScale -> Type.SCALE
            hasTranslation -> Type.TRANSLATE
            else -> Type.IDENTITY
        }
    }

    fun setTo(a: Float, b: Float, c: Float, d: Float, tx: Float, ty: Float): Unit {
        this.a = a
        this.b = b
        this.c = c
        this.d = d
        this.tx = tx
        this.ty = ty
    }

    fun copyFrom(that: Matrix): Unit = setTo(that.a, that.b, that.c, that.d, that.tx, that.ty)

    fun rotate(angle: Angle) {
        val theta = angle.radians
        val cos = cos(theta)
        val sin = sin(theta)

        val a1 = a * cos - b * sin
        b = (a * sin + b * cos)
        a = a1

        val c1 = c * cos - d * sin
        d = (c * sin + d * cos)
        c = c1

        val tx1 = tx * cos - ty * sin
        ty = (tx * sin + ty * cos)
        tx = tx1
    }

    fun skew(skewX: Angle, skewY: Angle) {
        val sinX = sin(skewX)
        val cosX = cos(skewX)
        val sinY = sin(skewY)
        val cosY = cos(skewY)

        setTo(
            a * cosY - b * sinX,
            a * sinY + b * cosX,
            c * cosY - d * sinX,
            c * sinY + d * cosX,
            tx * cosY - ty * sinX,
            tx * sinY + ty * cosX
        )
    }
    fun scale(sx: Float, sy: Float = sx): Unit = setTo(a * sx, b * sx, c * sy, d * sy, tx * sx, ty * sy)

    fun prescale(sx: Float, sy: Float = sx): Unit = setTo(a * sx, b * sx, c * sy, d * sy, tx, ty)

    fun translate(dx: Float, dy: Float) { tx += dx; ty += dy }

    fun pretranslate(dx: Float, dy: Float) { tx += a * dx + c * dy; ty += b * dx + d * dy }

    fun prerotate(angle: Angle) {
        val m = Matrix()
        m.rotate(angle)
        premultiply(m)
    }

    fun preskew(skewX: Angle, skewY: Angle)  {
        val m = Matrix()
        m.skew(skewX, skewY)
        premultiply(m)
    }

    fun premultiply(m: Matrix) = premultiply(m.a, m.b, m.c, m.d, m.tx, m.ty)
    fun postmultiply(m: Matrix) = multiply(this, m)

    fun premultiply(la: Float, lb: Float, lc: Float, ld: Float, ltx: Float, lty: Float): Unit = setTo(
        la * a + lb * c,
        la * b + lb * d,
        lc * a + ld * c,
        lc * b + ld * d,
        ltx * a + lty * c + tx,
        ltx * b + lty * d + ty
    )

    fun multiply(l: Matrix, r: Matrix): Unit = setTo(
        l.a * r.a + l.b * r.c,
        l.a * r.b + l.b * r.d,
        l.c * r.a + l.d * r.c,
        l.c * r.b + l.d * r.d,
        l.tx * r.a + l.ty * r.c + r.tx,
        l.tx * r.b + l.ty * r.d + r.ty
    )

    /** Transform point without translation */
    fun deltaTransformPoint(point: IPoint) = IPoint(point.x * a + point.y * c, point.x * b + point.y * d)

    fun identity() = setTo(1f, 0f, 0f, 1f, 0f, 0f)

    fun isIdentity() = getType() == Type.IDENTITY

    fun invert(matrixToInvert: Matrix = this): Matrix {
        val src = matrixToInvert
        val dst = this
        val norm = src.a * src.d - src.b * src.c

        if (norm == 0f) {
            dst.setTo(0f, 0f, 0f, 0f, -src.tx, -src.ty)
        } else {
            val inorm = 1f / norm
            val d = src.a * inorm
            val a = src.d * inorm
            val b = src.b * -inorm
            val c = src.c * -inorm
            dst.setTo(a, b, c, d, -a * src.tx - c * src.ty, -b * src.tx - d * src.ty)
        }

        return this
    }

    fun inverted(out: Matrix = Matrix()) = out.invert(this)

    fun setTransform(
        x: Float,
        y: Float,
        scaleX: Float,
        scaleY: Float,
        rotation: Angle,
        skewX: Angle,
        skewY: Angle
    ) {
        if (skewX == 0f.radians && skewY == 0f.radians) {
            if (rotation == 0f.radians) {
                setTo(scaleX, 0f, 0f, scaleY, x, y)
            } else {
                val cos = cos(rotation)
                val sin = sin(rotation)
                setTo(cos * scaleX, sin * scaleY, -sin * scaleX, cos * scaleY, x, y)
            }
        } else {
            this.identity()
            scale(scaleX, scaleY)
            skew(skewX, skewY)
            rotate(rotation)
            translate(x, y)
        }
    }

    fun clone() = Matrix(a, b, c, d, tx, ty)

    operator fun times(that: Matrix): Matrix = Matrix().apply { multiply(this, that) }

    fun toTransform(out: Transform = Transform()): Transform = out.setMatrix(this)

    // Transform points
    fun transform(p: IPoint, out: Point = Point()): Unit = transform(p.x, p.y, out)
    fun transform(px: Float, py: Float, out: Point = Point()): Unit = out.setTo(transformX(px, py), transformY(px, py))

    fun transformX(p: IPoint): Float = transformX(p.x, p.y)
    fun transformX(px: Float, py: Float): Float = a * px + c * py + tx

    fun transformY(p: IPoint): Float = transformY(p.x, p.y)
    fun transformY(px: Float, py: Float): Float = d * py + b * px + ty

    fun transformXf(px: Float, py: Float): Float = transformX(px, py)

    fun transformYf(px: Float, py: Float): Float = transformY(px, py)

    data class Transform(
        var x: Float = 0f, var y: Float = 0f,
        var scaleX: Float = 1f, var scaleY: Float = 1f,
        var skewX: Angle = 0f.radians, var skewY: Angle = 0f.radians,
        var rotation: Angle = 0f.radians
    ) : MutableInterpolable<Transform>, Interpolable<Transform> {
        val scaleAvg get() = (scaleX + scaleY) * 0.5

        override fun interpolateWith(ratio: Float, other: Transform): Transform = Transform().apply { setToInterpolated(ratio, this, other) }

        override fun setToInterpolated(ratio: Float, l: Transform, r: Transform): Unit = setTo(
            ratio.interpolate(l.x, r.x),
            ratio.interpolate(l.y, r.y),
            ratio.interpolate(l.scaleX, r.scaleX),
            ratio.interpolate(l.scaleY, r.scaleY),
            ratio.interpolate(l.rotation, r.rotation),
            ratio.interpolate(l.skewX, r.skewX),
            ratio.interpolate(l.skewY, r.skewY)
        )

        fun identity() {
            x = 0f
            y = 0f
            scaleX = 1f
            scaleY = 1f
            skewX = 0f.radians
            skewY = 0f.radians
            rotation = 0f.radians
        }

        fun setMatrix(matrix: Matrix): Transform {
            val PI_4 = PI.toFloat() / 4f
            this.x = matrix.tx
            this.y = matrix.ty

            this.skewX = atan(-matrix.c / matrix.d).radians
            this.skewY = atan(matrix.b / matrix.a).radians

            // Faster isNaN
            if (this.skewX != this.skewX) this.skewX = 0f.radians
            if (this.skewY != this.skewY) this.skewY = 0f.radians

            this.scaleY =
                if (this.skewX > -PI_4.radians && this.skewX < PI_4.radians) matrix.d / cos(this.skewX) else -matrix.c / sin(this.skewX)
            this.scaleX =
                if (this.skewY > -PI_4.radians && this.skewY < PI_4.radians) matrix.a / cos(this.skewY) else matrix.b / sin(this.skewY)

            if (abs(this.skewX - this.skewY).radians < 0.0001) {
                this.rotation = this.skewX
                this.skewX = 0f.radians
                this.skewY = 0f.radians
            } else {
                this.rotation = 0f.radians
            }

            return this
        }

        fun toMatrix(out: Matrix = Matrix()): Matrix = out.apply { setTransform(x, y, scaleX, scaleY, rotation, skewX, skewY) }
        fun copyFrom(that: Transform) = setTo(that.x, that.y, that.scaleX, that.scaleY, that.rotation, that.skewX, that.skewY)

        fun setTo(x: Float, y: Float, scaleX: Float, scaleY: Float, rotation: Angle, skewX: Angle, skewY: Angle) {
            this.x = x
            this.y = y
            this.scaleX = scaleX
            this.scaleY = scaleY
            this.rotation = rotation
            this.skewX = skewX
            this.skewY = skewY
        }

        fun clone() = Transform().copyFrom(this)
    }

    class Computed(val matrix: Matrix, val transform: Transform) {
        companion object;
        constructor(matrix: Matrix) : this(matrix, Transform().setMatrix(matrix))
        constructor(transform: Transform) : this(transform.toMatrix(), transform)
    }

    override fun setToInterpolated(ratio: Float, l: Matrix, r: Matrix): Unit = setTo(
        a = ratio.interpolate(l.a, r.a),
        b = ratio.interpolate(l.b, r.b),
        c = ratio.interpolate(l.c, r.c),
        d = ratio.interpolate(l.d, r.d),
        tx = ratio.interpolate(l.tx, r.tx),
        ty = ratio.interpolate(l.ty, r.ty)
    )

    override fun interpolateWith(ratio: Float, other: Matrix): Matrix =
        Matrix().apply { setToInterpolated(ratio, this, other) }

    inline fun <T> keep(callback: Matrix.() -> T): T {
        val a = this.a
        val b = this.b
        val c = this.c
        val d = this.d
        val tx = this.tx
        val ty = this.ty
        try {
            return this.callback()
        } finally {
            this.a = a
            this.b = b
            this.c = c
            this.d = d
            this.tx = tx
            this.ty = ty
        }
    }

    override fun toString(): String = "Matrix(a=$a, b=$b, c=$c, d=$d, tx=$tx, ty=$ty)"
}

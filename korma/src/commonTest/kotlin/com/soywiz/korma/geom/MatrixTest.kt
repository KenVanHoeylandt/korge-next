package com.soywiz.korma.geom

import com.soywiz.korma.interpolation.*
import kotlin.test.*

class MatrixTest {
    private val identity: Matrix = Matrix(1f, 0f, 0f, 1f, 0f, 0f)

    @Test
    fun test() {
        val matrix = Matrix()
        matrix.pretranslate(10f, 10f)
        matrix.prescale(2f, 3f)
        matrix.prerotate(90.degrees)
        assertEquals(PointInt(10, 40).toString(), matrix.transform(Point(10, 0)).toString())
    }

    @Test
    fun transform() {
        val matrix = Matrix(2f, 0f, 0f, 3f, 10f, 15f)
        assertEquals(30f, matrix.transformX(10f, 20f))
        assertEquals(75f, matrix.transformY(10f, 20f))
        assertEquals(Point(30f, 75f), matrix.transform(Point(10f, 20f)))
        assertEquals(Point(20f, 60f), matrix.deltaTransformPoint(Point(10f, 20f)))
    }

    @Test
    fun type() {
        assertEquals(Matrix.Type.IDENTITY, Matrix(1, 0, 0, 1, 0, 0).getType())
        assertEquals(Matrix.Type.TRANSLATE, Matrix(1, 0, 0, 1, 10, 0).getType())
        assertEquals(Matrix.Type.TRANSLATE, Matrix(1, 0, 0, 1, 0, 10).getType())
        assertEquals(Matrix.Type.SCALE, Matrix(1, 0, 0, 2, 0, 0).getType())
        assertEquals(Matrix.Type.SCALE, Matrix(2, 0, 0, 1, 0, 0).getType())
        assertEquals(Matrix.Type.SCALE_TRANSLATE, Matrix(2, 0, 0, 2, 10, 0).getType())
        assertEquals(Matrix.Type.COMPLEX, Matrix(1, 1, 0, 1, 0, 0).getType())

        assertEquals(Matrix.Type.IDENTITY, Matrix().getType())
        assertEquals(Matrix.Type.SCALE, Matrix().apply { scale(2f, 1f) }.getType())
        assertEquals(Matrix.Type.SCALE, Matrix().apply { scale(1f, 2f) }.getType())
        assertEquals(Matrix.Type.TRANSLATE, Matrix().apply { translate(1f, 0f) }.getType())
        assertEquals(Matrix.Type.TRANSLATE, Matrix().apply { translate(0f, 1f) }.getType())
        assertEquals(Matrix.Type.SCALE_TRANSLATE, Matrix().apply {
            scale(2f, 1f)
            translate(0f, 1f)
        }.getType())
        assertEquals(Matrix.Type.COMPLEX, Matrix().apply { rotate(90.degrees) }.getType())
    }

    @Test
    fun identity() {
        val m = Matrix()
        assertEquals(1.0, m.a)
        assertEquals(0.0, m.b)
        assertEquals(0.0, m.c)
        assertEquals(1.0, m.d)
        assertEquals(0.0, m.tx)
        assertEquals(0.0, m.ty)
        m.setTo(2f, 2f, 2f, 2f, 2f, 2f)
        assertEquals(Matrix(2f, 2f, 2f, 2f, 2f, 2f), m)
        m.identity()
        assertEquals(identity, m)
    }

    @Test
    fun invert() {
        val a = Matrix(2f, 1f, 1f, 2f, 10f, 10f)
        a.invert()
        assertEquals(Matrix(a = 0.6666666666666666, b = -0.3333333333333333, c = -0.3333333333333333, d = 0.6666666666666666, tx = -3.333333333333333, ty = -3.333333333333333), a)
    }

    @Test
    fun inverted() {
        val a = Matrix(2f, 1f, 1f, 2f, 10f, 10f)
        val b = a.inverted()
        assertEquals(identity, a * b)
    }

    @Test
    fun clone() {
        val mat = Matrix(1f, 2f, 3f, 4f, 5f, 6f)
        assertNotSame(mat, mat.clone())
        assertTrue(mat !== mat.clone())
        assertEquals(mat, mat.clone())
    }

    @Test
    fun keep() {
        val m = Matrix()
        m.keep {
            m.setTo(2f, 3f, 4f, 5f, 6f, 7f)
            assertEquals(Matrix(2, 3, 4, 5, 6, 7), m)
        }
        assertEquals(identity, m)
    }

    @Test
    fun transform2() {
        assertEquals(Matrix(2, 0, 0, 3, 10, 20), Matrix.Transform(10.0f, 20.0f, scaleX = 2.0f, scaleY = 3.0f).toMatrix())

        // @TODO: Kotlin.JS BUG (missing arguments are NaN or undefined but it works fine on JVM)
        //val t1 = Matrix.Transform(10, 20, scaleX = 2, scaleY = 3, rotation = 90.degrees)
        //val t2 = Matrix.Transform(20, 40, scaleX = 4, scaleY = 5, rotation = 180.degrees)

        val t1 = Matrix.Transform(10.0f, 20.0f, scaleX = 2.0f, scaleY = 3.0f, skewX = 0.0.degrees, skewY = 0.0.degrees, rotation = 90.degrees)
        val t2 = Matrix.Transform(20.0f, 40.0f, scaleX = 4.0f, scaleY = 5.0f, skewX = 0.0.degrees, skewY = 0.0.degrees, rotation = 180.degrees)
        assertEquals(
            Matrix.Transform(x = 15.0f, y = 30.0f, scaleX = 3.0f, scaleY = 4.0f, skewX = 0.0.degrees, skewY = 0.0.degrees, rotation = 135.degrees),
            0.5.interpolate(t1, t2)
        )

        val identity = Matrix.Transform()
        val mt = Matrix.Transform(1.0f, 2.0f, 3.0f, 4.0f, 5.0.radians, 6.0.radians, 7.0.radians)
        mt.identity()
        assertEquals(identity, mt)
        assertNotSame(mt, mt.clone())
        assertEquals(mt, mt.clone())
    }
}

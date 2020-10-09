package com.soywiz.korma.geom

import kotlin.test.*

class Vector3DTest {
    @Test
    fun testNormalize() {
        val v = Vector3D(2, 0, 0)
        // Normalized doesn't changes the original vector
        assertEquals(Vector3D(1, 0, 0), v.normalize())
        assertEquals(Vector3D(2, 0, 0), v)

        // Normalize mutates the vector
        assertEquals(Vector3D(1, 0, 0), v.normalize())
        assertEquals(Vector3D(1, 0, 0), v)
    }

    @Test
    fun testCrossProduct() {
        val xInt = Vector3D().setToCross(Vector3D(1, 0, 0), Vector3D(0, 1, 0))
        assertEquals(Vector3D(0, 0, 1), xInt)
        val xDouble = Vector3D().setToCross(Vector3D(1.0, 0.0, 0.0), Vector3D(0.0, 1.0, 0.0))
        assertEquals(Vector3D(0.0, 0.0, 1.0), xDouble)
    }

    @Test
    fun testDotProduct() {
        val dot = Vector3D(0.5, 1.0, 0.0).dot(Vector3D(3.0, 1.0, 1.0))
        assertEquals(2.5f, dot)
    }

    @Test
    fun testBasicMath() {
        val v = Vector3D(0,0,0)
        v.setToAdd(v, Vector3D(1,0,0))
        assertEquals(Vector3D(1, 0, 0), v)
        v.scale(5)
        assertEquals(Vector3D(5, 0 ,0), v)
        v.setToSub(v, Vector3D(2, 1, 0))
        assertEquals(Vector3D(3, -1, 0), v)
    }

    @Test
    fun copy() {
        val original = Vector3D(1.0f, 2.0f, 3.0f)
        val copy = original.copy()
        assertEquals(original.x, copy.x)
        assertEquals(original.y, copy.y)
        assertEquals(original.z, copy.z)
    }

    @Test
    fun multiplicationOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val number = 2.0f

        val result = vector * number
        assertEquals(result.x, vector.x * number)
        assertEquals(result.y, vector.y * number)
        assertEquals(result.z, vector.z * number)
    }

    @Test
    fun selfAssignMultiplicationWithOperatorFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val original = Vector3D().copyFrom(vector)
        val number = 2.0f

        vector *= number
        assertEquals(vector.x, original.x * number)
        assertEquals(vector.y, original.y * number)
        assertEquals(vector.z, original.z * number)
    }

    @Test
    fun divisionOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val number = 2.0f

        val result = vector / number
        assertEquals(result.x, vector.x / number)
        assertEquals(result.y, vector.y / number)
        assertEquals(result.z, vector.z / number)
    }

    @Test
    fun selfAssignDivisionOperatorWithFloat() {
        val vector = Vector3D(1.0f, 2.0f, 3.0f)
        val original = Vector3D().copyFrom(vector)
        val number = 2.0f

        vector /= number
        assertEquals(vector.x, original.x / number)
        assertEquals(vector.y, original.y / number)
        assertEquals(vector.z, original.z / number)
    }

    @Test
    fun additionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        val result = first + second
        assertEquals(result.x, first.x + second.x)
        assertEquals(result.y, first.y + second.y)
        assertEquals(result.z, first.z + second.z)
    }

    @Test
    fun selfAssignAdditionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val firstOriginal = Vector3D().copyFrom(first)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        first += second
        assertEquals(first.x, firstOriginal.x + second.x)
        assertEquals(first.y, firstOriginal.y + second.y)
        assertEquals(first.z, firstOriginal.z + second.z)
    }

    @Test
    fun subtractionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        val result = first - second
        assertEquals(result.x, first.x - second.x)
        assertEquals(result.y, first.y - second.y)
        assertEquals(result.z, first.z - second.z)
    }

    @Test
    fun selfAssignSubtractionOperatorWithVector() {
        val first = Vector3D(1.0f, 2.0f, 3.0f)
        val firstOriginal = Vector3D().copyFrom(first)
        val second = Vector3D(4.0f, 5.0f, 6.0f)

        first -= second
        assertEquals(first.x, firstOriginal.x - second.x)
        assertEquals(first.y, firstOriginal.y - second.y)
        assertEquals(first.z, firstOriginal.z - second.z)
    }
}

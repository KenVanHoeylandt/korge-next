package com.soywiz.korma.geom

import kotlin.test.*

class PointPoolTest {
    @Test
    fun test() {
        var called = false
        val area = PointPool(7)
        area {
            called = true
            assertEquals(Point(30, 30), Point(10f, 10f) + Point(20f, 20f))
            assertEquals(Point(30, 30), Point(10f, 10f) * 3f)
        }
        assertEquals(true, called)
    }
}

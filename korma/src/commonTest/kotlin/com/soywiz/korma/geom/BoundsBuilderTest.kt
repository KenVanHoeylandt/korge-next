package com.soywiz.korma.geom

import kotlin.test.Test
import kotlin.test.assertEquals

class BoundsBuilderTest {
    @Test
    fun name() {
        val bb = BoundsBuilder()
        bb.add(Rectangle(20f, 10f, 200f, 300f))
        bb.add(Rectangle(2000f, 70f, 400f, 50f))
        bb.add(Rectangle(10000f, 10000f, 0f, 0f))
        assertEquals("Rectangle(x=20, y=10, width=2380, height=300)", bb.getBounds().toString())
        bb.reset()
        assertEquals("null", bb.getBoundsOrNull().toString())
        assertEquals("Rectangle(x=0, y=0, width=0, height=0)", bb.getBounds().toString())
        bb.add(Rectangle.fromBounds(0f, 0f, 1f, 1f))
        assertEquals("Rectangle(x=0, y=0, width=1, height=1)", bb.getBoundsOrNull().toString())
        assertEquals("Rectangle(x=0, y=0, width=1, height=1)", bb.getBounds().toString())
    }

    @Test
    fun test2() {
        val bb = BoundsBuilder()
            .add(-100f, 100f)
            .add(-90f, 100f)
            .add(-100f, 110f)
            .add(-90f, 110f)

        assertEquals(Rectangle(-100f, 100f, 10f, 10f), bb.getBounds())
    }
}

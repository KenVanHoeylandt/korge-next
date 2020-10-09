package com.soywiz.korma.geom

import com.soywiz.korma.interpolation.Interpolable
import com.soywiz.korma.interpolation.interpolate

data class Anchor(val sx: Float, val sy: Float) : Interpolable<Anchor> {
    companion object {
        operator fun invoke(sx: Float, sy: Float) = Anchor(sx, sy)
        operator fun invoke(sx: Double, sy: Double) = Anchor(sx.toFloat(), sy.toFloat())
        operator fun invoke(sx: Int, sy: Int) = Anchor(sx.toFloat(), sy.toFloat())

        val TOP_LEFT = Anchor(0f, 0f)
        val TOP_CENTER = Anchor(0.5f, 0f)
        val TOP_RIGHT = Anchor(1f, 0f)

        val MIDDLE_LEFT = Anchor(0f, 0.5f)
        val MIDDLE_CENTER = Anchor(0.5f, 0.5f)
        val MIDDLE_RIGHT = Anchor(1f, 0.5f)

        val BOTTOM_LEFT = Anchor(0f, 1f)
        val BOTTOM_CENTER = Anchor(0.5f, 1f)
        val BOTTOM_RIGHT = Anchor(1f, 1f)
    }

    override fun interpolateWith(ratio: Float, other: Anchor): Anchor = Anchor(
        ratio.interpolate(sx, other.sx),
        ratio.interpolate(sy, other.sy)
    )
}

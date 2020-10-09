package com.soywiz.korma.geom

enum class Orientation(val value: Int) {
    CLOCK_WISE(+1), COUNTER_CLOCK_WISE(-1), COLLINEAR(0);

    companion object {
        private const val EPSILON: Float = 1e-12.toFloat()

        fun orient2d(pa: IPoint, pb: IPoint, pc: IPoint): Orientation = orient2d(pa.x, pa.y, pb.x, pb.y, pc.x, pc.y)

        fun orient2d(paX: Float, paY: Float, pbX: Float, pbY: Float, pcX: Float, pcY: Float): Orientation {
            val detleft: Float = (paX - pcX) * (pbY - pcY)
            val detright: Float = (paY - pcY) * (pbX - pcX)
            val `val`: Float = detleft - detright

            if ((`val` > -EPSILON) && (`val` < EPSILON)) return COLLINEAR
            if (`val` > 0) return COUNTER_CLOCK_WISE
            return CLOCK_WISE
        }
    }
}

package com.soywiz.korma.geom

data class EulerRotation(
    var x: Angle = 0f.degrees,
    var y: Angle = 0f.degrees,
    var z: Angle = 0f.degrees
) {
    companion object {
        fun toQuaternion(roll: Angle, pitch: Angle, yaw: Angle, out: Quaternion = Quaternion()): Quaternion {
            val cr = cos(roll * 0.5f)
            val sr = sin(roll * 0.5f)
            val cp = cos(pitch * 0.5f)
            val sp = sin(pitch * 0.5f)
            val cy = cos(yaw * 0.5f)
            val sy = sin(yaw * 0.5f)
            out.setTo(
                (cy * cp * sr - sy * sp * cr),
                (sy * cp * sr + cy * sp * cr),
                (sy * cp * cr - cy * sp * sr),
                (cy * cp * cr + sy * sp * sr)
            )
            return out
        }
        fun toQuaternion(euler: EulerRotation, out: Quaternion = Quaternion()): Quaternion = toQuaternion(euler.x, euler.y, euler.z, out)
    }

    fun toQuaternion(out: Quaternion = Quaternion()): Quaternion = toQuaternion(this, out)

    fun setQuaternion(x: Double, y: Double, z: Double, w: Double): EulerRotation = Quaternion.toEuler(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat(), this)
    fun setQuaternion(x: Int, y: Int, z: Int, w: Int): EulerRotation = Quaternion.toEuler(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat(), this)
    fun setQuaternion(x: Float, y: Float, z: Float, w: Float): EulerRotation = Quaternion.toEuler(x, y, z, w, this)

    fun setQuaternion(quaternion: Quaternion): EulerRotation = Quaternion.toEuler(quaternion.x, quaternion.y, quaternion.z, quaternion.w, this)
    fun setTo(x: Angle, y: Angle, z: Angle): EulerRotation = this
        .apply { this.x = x }
        .apply { this.y = y }
        .apply { this.z = z }

    fun setTo(other: EulerRotation): EulerRotation = setTo(other.x, other.y, other.z)

    private val tempQuat: Quaternion by lazy { Quaternion() }
    fun toMatrix(out: Matrix3D = Matrix3D()): Matrix3D = tempQuat.setEuler(this).toMatrix(out)
}

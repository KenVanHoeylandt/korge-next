package com.soywiz.korma.geom

fun Matrix3D.copyFrom(that: Matrix): Matrix3D = that.toMatrix3D(this)

fun Matrix.toMatrix3D(out: Matrix3D = Matrix3D()): Matrix3D = out.setRows(
    a, c, 0f, tx,
    b, d, 0f, ty,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f
)

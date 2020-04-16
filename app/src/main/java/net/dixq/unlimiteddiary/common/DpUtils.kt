package net.dixq.unlimiteddiary.utils

import android.content.Context

//pxをdpに置換
fun convertPxToDp(context: Context, px: Int): Int {
    val d: Float = context.resources.displayMetrics.density
    return (px / d + 0.5).toInt()
}

//dpをpxに置換
fun convertDpToPx(context: Context, dp: Int): Int {
    val d: Float = context.resources.displayMetrics.density
    return (dp * d + 0.5).toInt()
}
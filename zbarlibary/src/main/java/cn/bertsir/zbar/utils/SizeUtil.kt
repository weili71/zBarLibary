package cn.bertsir.zbar.utils

import android.content.Context

object SizeUtil{
    fun dip2px(context: Context,dp: Int): Int {
        val density: Float = context.getResources().getDisplayMetrics().density
        return (dp * density + 0.5).toInt()
    }
}

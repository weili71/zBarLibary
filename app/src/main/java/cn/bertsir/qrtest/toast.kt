package cn.bertsir.qrtest

import android.content.Context
import androidx.annotation.StringRes
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

private var toast: Toast? = null

fun toast(context: Context, @StringRes resId: Int, duration: Int = LENGTH_SHORT) {
    toast(context, context.getString(resId), duration)
}

fun toast(context: Context, text: String, duration: Int = LENGTH_SHORT) {
    toast?.cancel()//取消之前的toast
    toast = Toast.makeText(context, text, duration)//创建新toast
    toast!!.show()
}
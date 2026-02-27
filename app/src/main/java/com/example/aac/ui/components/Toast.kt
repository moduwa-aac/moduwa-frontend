package com.example.aac.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast

/**
 * 여러 화면에서 공통으로 사용하기 위한 커스텀 토스트 메시지
 */
fun showCleanToast(context: Context, message: String) {
    val density = context.resources.displayMetrics.density
    val toast = Toast(context)

    val textView = TextView(context).apply {
        text = message
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        setTextColor(AndroidColor.BLACK)
        gravity = Gravity.CENTER
        // 🔥 [수정] 긴 문장을 수용하기 위해 너비를 늘리고 높이를 유연하게 설정
        width = (350 * density).toInt() 
        minHeight = (42 * density).toInt()
        setPadding((24 * density).toInt(), (12 * density).toInt(), (24 * density).toInt(), (12 * density).toInt())
        background = GradientDrawable().apply {
            setColor(AndroidColor.WHITE)
            cornerRadius = 10 * density
            setStroke(1, AndroidColor.parseColor("#D9D9D9"))
        }
    }
    toast.view = textView
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
    toast.duration = Toast.LENGTH_SHORT
    toast.show()
}

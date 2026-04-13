package com.example.map.ui.common

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

fun Activity.enableEdgeToEdgeCompat() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

fun View.applySystemBarPadding(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false
) {
    val initialLeft = paddingLeft
    val initialTop = paddingTop
    val initialRight = paddingRight
    val initialBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialLeft + if (applyLeft) bars.left else 0,
            top = initialTop + if (applyTop) bars.top else 0,
            right = initialRight + if (applyRight) bars.right else 0,
            bottom = initialBottom + if (applyBottom) bars.bottom else 0
        )
        insets
    }
    requestInsetsWhenAttached()
}

fun View.applySystemBarMargin(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false
) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initialLeft = layoutParams.leftMargin
    val initialTop = layoutParams.topMargin
    val initialRight = layoutParams.rightMargin
    val initialBottom = layoutParams.bottomMargin

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = initialLeft + if (applyLeft) bars.left else 0
            topMargin = initialTop + if (applyTop) bars.top else 0
            rightMargin = initialRight + if (applyRight) bars.right else 0
            bottomMargin = initialBottom + if (applyBottom) bars.bottom else 0
        }
        insets
    }
    requestInsetsWhenAttached()
}

private fun View.requestInsetsWhenAttached() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

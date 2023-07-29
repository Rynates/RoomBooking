package com.example.roombooking.helpers

import android.view.View

/**
 * Set visible state of View between VISIBLE and GONE
 *
 * @property show true for VISIBLE, false for GONE
 */
fun View.show(show: Boolean = true) {
    visibility = if (show) View.VISIBLE else View.GONE
}

/**
 * Set visible state of View between GONE and VISIBLE
 *
 * @property hide true for GONE, false for VISIBLE
 */
fun View.hide(hide: Boolean = true) {
    show(!hide)
}

/**
 * Set visible state of View between INVISIBLE and VISIBLE
 *
 * @property invisible true for INVISIBLE, false for VISIBLE
 */
fun View.invisible(invisible: Boolean = true) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

/**
 * Set visible state of View between VISIBLE and INVISIBLE
 *
 * @property visible true for VISIBLE, false for INVISIBLE
 */
fun View.visible(visible: Boolean = true) {
    invisible(!visible)
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
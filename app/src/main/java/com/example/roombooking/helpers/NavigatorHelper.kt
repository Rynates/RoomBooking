package com.example.roombooking.helpers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.roombooking.R

fun AppCompatActivity.changeTo(
    fragment: Fragment,
    withBack: Boolean = false,
    tag: String? = null,
    transaction: Transaction? = null,
    clearStack: Boolean = !withBack,
    reorderingEnabled: Boolean = false
) {
    let {
        val containerId: Int = R.id.container
        val fragmentManager = it.supportFragmentManager
        if (clearStack && !fragmentManager.isStateSaved) {
            fragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        val fragmentTransaction = fragmentManager.beginTransaction()

        when (transaction) {
            Transaction.SLIDE_END -> fragmentTransaction.setCustomAnimations(
                R.anim.fragment_enter_from_right,
                R.anim.fragment_exit_to_left,
                R.anim.fragment_enter_from_left,
                R.anim.fragment_exit_to_right
            )
            Transaction.SLIDE_UP -> fragmentTransaction.setCustomAnimations(
                R.anim.fragment_enter_from_bottom,
                R.anim.fragment_exit_to_top,
                R.anim.fragment_enter_from_top,
                R.anim.fragment_exit_to_bottom
            )
            Transaction.SLIDE_END_WITHOUT_BACK -> fragmentTransaction.setCustomAnimations(
                R.anim.fragment_enter_from_right,
                R.anim.fragment_exit_to_left,
                R.anim.fragment_enter_from_left,
                R.anim.fragment_exit_to_left
            )
            Transaction.NONE -> fragmentTransaction.setCustomAnimations(0, 0, 0, 0)
        }

        if (reorderingEnabled) {
            //so postponeEnterTransition() inside fragment can work
            fragmentTransaction.setReorderingAllowed(true)
        }
        fragmentTransaction.replace(containerId, fragment, tag)

        if (withBack) {
            fragmentTransaction.addToBackStack(tag)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }
}

enum class Transaction {
    SLIDE_END,
    SLIDE_UP,
    NONE,
    SLIDE_END_WITHOUT_BACK
}
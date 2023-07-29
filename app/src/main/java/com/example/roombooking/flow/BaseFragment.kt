package com.example.roombooking.flow

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.roombooking.MainActivity
import com.example.roombooking.data.entity.ErrorType
import com.example.roombooking.helpers.toast

abstract class BaseFragment : Fragment {
    constructor() : super()
    constructor(layout: Int) : super(layout)

    private var activity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            val activity = context as MainActivity?
            this.activity = activity
        }
    }

    override fun onDetach() {
        activity = null
        super.onDetach()
    }

    fun getBaseActivity(): MainActivity? = activity

    protected fun showError(error: ErrorType?) {
        error?.message?.let {
            context?.toast(it)
        }

    }
}
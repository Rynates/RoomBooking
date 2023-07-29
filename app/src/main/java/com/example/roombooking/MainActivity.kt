package com.example.roombooking

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.roombooking.flow.Navigator
import com.example.roombooking.flow.home.HomeFragment
import com.example.roombooking.flow.select_room.SelectRoomFragment
import com.example.roombooking.helpers.Transaction
import com.example.roombooking.helpers.changeTo
import com.example.roombooking.prefs.AppSettings


class MainActivity : AppCompatActivity(), Navigator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            if (AppSettings.roomsSelected.isNullOrEmpty()) {
                showRooms()
            } else {
                showMeetings()
            }
        }
    }

    override fun showRooms(withBack: Boolean) {
        startFragment(fragment = SelectRoomFragment.newInstance(), withBack = withBack)
    }

    override fun showMeetings() {
        startFragment(HomeFragment.newInstance(), false)
    }

    private fun startFragment(fragment: Fragment, withBack: Boolean) {
        changeTo(fragment = fragment, withBack = withBack, transaction = Transaction.SLIDE_END)
    }
}

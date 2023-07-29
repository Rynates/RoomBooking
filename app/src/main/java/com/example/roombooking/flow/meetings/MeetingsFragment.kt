package com.example.roombooking.flow.meetings

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.roombooking.R
import com.example.roombooking.data.entity.Meeting
import com.example.roombooking.data.entity.Room
import com.example.roombooking.flow.BaseFragment
import com.example.roombooking.helpers.*
import com.example.roombooking.network.Resource
import kotlinx.android.synthetic.main.fragment_meetings.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId


class MeetingsFragment : BaseFragment(R.layout.fragment_meetings) {

    companion object {
        private const val KEY_ROOM = "room"
        fun newInstance(room: Room?): MeetingsFragment = MeetingsFragment().apply {
            arguments = Bundle().apply { putParcelable(KEY_ROOM, room) }
        }
    }

    lateinit var sharedPreferences: SharedPreferences

    private var argRoom: Room? = null

    private val viewModel: MeetingsViewModel by viewModel { parametersOf(argRoom?.id) }
    private var mLayut: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        argRoom = arguments?.getParcelable(KEY_ROOM)

        argRoom?.topic?.let { it ->
            lifecycle.addObserver(MeetingsLifeCycleObserver(lifecycle, it))
        }
        argRoom?.id?.let {
            subscribeCancel(it)
        }

        mLayut = view!!.findViewById(R.id.frameMeetings)


        sharedPreferences = activity!!.getSharedPreferences("pref", MODE_PRIVATE)

       // checkIsConfirmShown()
        subscribeOnInfo()
        subscribeOnDb()
        subscribeOnServer()
        subscribeOnConfirm()
        subscribeOnToast()

        btnConfirm.setSafeOnClickListener {
            viewModel.meetingInfo.value?.let { state ->
                when (state) {
                    is MeetingState.InProgress -> state.upcomingMeeting?.let {
                        showConfirmDialog(it)

                    }
                    else -> {
                    }
                }
            }
        }

        swipe_refresh.setOnRefreshListener { updateData() }
    }

    private fun updateData() {
        argRoom?.id?.let {
            viewModel.loadMeetings(it)
        }
    }


    private fun subscribeCancel(_argRoom: Long) =
        viewModel.apiRes.observe(viewLifecycleOwner, Observer<List<Meeting>> { meetings ->
            for (i in meetings.iterator()) {
                val ids = sharedPreferences.getString("id", " ")
                val room = sharedPreferences.getLong("room", 0)
                if (_argRoom == room) {
                    if (i.id != ids) {
                        val timer = object : CountDownTimer(2000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                free_room.text = getString(R.string.canceled)
                            }

                            override fun onFinish() {
                                free_room.text = getString(R.string.free_room)
                            }
                        }
                        timer.start()
                    }
                } else {
                    free_room.text = getString(R.string.free_room)
                }
            }
        })


    private fun subscribeOnInfo() = viewModel.meetingInfo.observe(this, Observer<MeetingState> {
        when (it) {
            is MeetingState.InProgress -> {
                initCurrentMeeting(it.currentMeeting)
                tv_meetings_data.text = it.msg
                btnConfirm.show(it.timeForConfirm > 0)
                it.upcomingMeeting?.let { upcomingMeeting ->
                    if (!upcomingMeeting.confirmed && it.timeForConfirm > 0) {
                        tv_meetings_data.text = getString(R.string.msg_please_confirm, " ", it.timeForConfirm)
                      //  checkIsConfirmShown()
                    } else if (!upcomingMeeting.confirmed && it.currentMeeting!!.startTime.toMillis() > OffsetDateTime.now(
                            ZoneId.systemDefault()
                        ).toEpochSecond()
                    ) {
                        free_room.invisible()
                    }
                }
                initNextMeeting(it.nextMeeting)
            }
            is MeetingState.NoMeetings -> {
                tv_meetings_data.text = it.msg
                btnConfirm.hide()
                ll_current_meeting.invisible()
                free_room.visible()
                free_room.setTextColor(
                    ContextCompat.getColor(context!!, R.color.colorAccent)
                )
                initNextMeeting(it.nextMeeting)
            }
        }
    })

   // private fun checkIsConfirmShown() = hideToolbar(btnConfirm.isShown)


    private fun initCurrentMeeting(meeting: Meeting?) {
        ll_current_meeting.invisible(meeting == null)
        checkIsVisible(meeting)
        free_room.invisible()
        meeting?.let {
            tv_current_meeting_start_time.text = formatDate(it.startTime, formatTo = "HH:mm")
            tv_current_meeting_end_time.text = formatDate(it.endTime, formatTo = "HH:mm")
            tv_current_meeting_location.text = it.location
            tv_current_meeting_members.text = it.members
            tv_current_meeting_confirmed.text = if (it.confirmed) "Confirmed" else "Not Confirmed"
            tv_current_meeting_confirmed.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    if (it.confirmed) android.R.color.holo_green_dark else android.R.color.holo_orange_dark
                )
            )
            val editor = sharedPreferences.edit()
                .putString("id", it.id)
                .putLong("room", it.roomId)
            editor.apply()
        }
    }

    private fun initNextMeeting(meeting: Meeting?) {
        ll_next_meeting.invisible(meeting == null)
        checkIsVisible(meeting)
        meeting?.let {
            tv_next_meeting_start_time.text = formatDate(it.startTime, formatTo = "HH:mm")
            tv_next_meeting_end_time.text = formatDate(it.endTime, formatTo = "HH:mm")
            tv_next_meeting_location.text = it.location
            tv_next_meeting_members.text = it.members
            tv_next_meeting_confirmed.text = if (it.confirmed) "Confirmed" else "Not Confirmed"
            tv_next_meeting_confirmed.setTextColor(
                ContextCompat.getColor(
                    context!!,
                    if (it.confirmed) android.R.color.holo_green_dark else android.R.color.holo_orange_dark
                )
            )

        }
    }

    private fun checkIsVisible(meeting: Meeting?) {
        cv_next_meeting.visible(meeting != null)
        cv_next.visible(meeting != null)
    }

    private fun subscribeOnDb() =
        viewModel.dbResult.observe(viewLifecycleOwner, Observer<List<Meeting>> {
            viewModel.calculateState(it)
        })

    private fun subscribeOnServer() =
        viewModel.apiResult.observe(viewLifecycleOwner, Observer<Resource<List<Meeting>>> { resource ->
            when (resource) {
                is Resource.Loading -> swipe_refresh.isRefreshing = resource.isShow
                is Resource.Error -> viewModel.showErrorMessage(resource.error)
            }
        })
//
//    private fun hideToolbar(flag: Boolean) {
//        if (flag)
//            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
//        else
//            (activity as AppCompatActivity?)!!.supportActionBar!!.show()
//    }


    private fun subscribeOnConfirm() = viewModel.confirmResult.observe(this, Observer { event ->
        event.getContentIfNotHandled()?.let { resource ->
            when (resource) {
                is Resource.Loading -> swipe_refresh.isRefreshing = resource.isShow
                is Resource.Error -> viewModel.showErrorMessage(resource.error)
                is Resource.Success -> {
                    argRoom?.id?.let {
                        viewModel.loadMeetings(it)
                    }
                }
            }
        }
    })


    private fun subscribeOnToast() = viewModel.toastText.observe(this, Observer { event ->
        event.getContentIfNotHandled()?.let {
            context?.toast(it)
        }
    })


    private fun showConfirmDialog(meeting: Meeting) {
        if (!meeting.confirmed) {
            context?.let {
                with(AlertDialog.Builder(it))
                {
                    setTitle(R.string.alert_confirm_title)
                    setMessage(
                        getString(
                            R.string.alert_confirm_text,
                            meeting.location,
                            meeting.members
                        )
                    )
                    setNegativeButton(R.string.back, null)
                    setPositiveButton(android.R.string.yes) { dialog, _ ->
                        viewModel.confirmMeeting(meeting.id)
                        //hideToolbar(flag = false)
                        dialog.dismiss()
                    }
                    show()
                }
            }
        } else {
            viewModel.showToastMessage(getString(R.string.toast_meeting_already_confirmed))
        }
    }

}



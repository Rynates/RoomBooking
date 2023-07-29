package com.example.roombooking.flow.meetings

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

class MeetingsLifeCycleObserver(private val lifeCycle: Lifecycle, private val topic: String) :
    LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener {
            Timber.d("subscribeToTopic - $topic")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unsubscribeFromTopic() {
        if (lifeCycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener {
                Timber.d("unsubscribeFromTopic - $topic")
            }
        }
    }
}
package com.example.arken.fragment.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.arken.R
import com.example.arken.fragment.signup_login.LoginFragment
import com.example.arken.model.Notification
import com.example.arken.model.ListNotification
import com.example.arken.util.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationListDialog : DialogFragment() {
    var notifications: MutableList<Notification> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var notificationAdapter: NotificationAdapter
    var userCookie = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_listnotification, container)
        recyclerView = rootView.findViewById(R.id.notification_recyclerView)
        notificationAdapter = NotificationAdapter(notifications)
        recyclerView.adapter = notificationAdapter

        this.dialog?.setTitle("Notifications")

        userCookie  = activity!!.getSharedPreferences(
            LoginFragment.MY_PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString("user_cookie", "")!!

        initDataset()

        return rootView
    }


    fun initDataset(){

        val callGetNotification: Call<ListNotification> =
            RetroClient.getInstance().apiService.getNotifications(userCookie)

        callGetNotification.enqueue(object : Callback<ListNotification> {
            override fun onResponse(call: Call<ListNotification>, response: Response<ListNotification>) {
                if (response.isSuccessful) {

                    notifications = response.body()?.notifications as MutableList<Notification>
                    notificationAdapter.dataSet = notifications

                    notificationAdapter.notifyDataSetChanged()

                }
            }

            override fun onFailure(call: Call<ListNotification>, t: Throwable) {
            }
        })
    }

}
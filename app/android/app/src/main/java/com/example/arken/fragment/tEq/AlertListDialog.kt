package com.example.arken.fragment.tEq

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.arken.R
import com.example.arken.fragment.signup_login.LoginFragment
import com.example.arken.fragment.tEq.CurrencyFragment
import com.example.arken.model.Alert
import com.example.arken.model.FollowRequest
import com.example.arken.model.ListAlert
import com.example.arken.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlertListDialog(val currency: String, val currentVal: Double) : DialogFragment(),  OnAlarmClickedListener, AddAlertListener{
    lateinit var recyclerView: RecyclerView
    var alerts:MutableList<Alert> = mutableListOf()
    lateinit var alertAdapter: AlertAdapter
    lateinit var addFloatingButton: FloatingActionButton
    var userCookie = ""
    var showAll = false
    lateinit var checkBox: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_alert, container)
        recyclerView = rootView.findViewById(R.id.alert_recyclerView)
        addFloatingButton = rootView.findViewById(R.id.alert_add)

        alertAdapter = AlertAdapter(alerts, this)
        recyclerView.adapter = alertAdapter

        this.dialog?.setTitle("Alerts")
        userCookie  = activity!!.getSharedPreferences(
                LoginFragment.MY_PREFS_NAME,
        Context.MODE_PRIVATE
        ).getString("user_cookie", "")!!
        checkBox = rootView.findViewById(R.id.alert_showAll)
        checkBox.setOnClickListener{
            showAll = !showAll
            initDataset()
        }

        initDataset()

        addFloatingButton.setOnClickListener{
            val dialog = AddAlertDialog(this, currency.substring(0, currency.indexOf('/')), currentVal)
            dialog.show(fragmentManager!!, "alertFragment")
        }

        return rootView
    }


    override fun onDeleteClicked(alarmId: String, position: Int) {
        val callDelete: Call<ResponseBody> =
            RetroClient.getInstance().apiService.deleteeAlert(userCookie, alarmId)

        callDelete.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "You have deleted your alert", Toast.LENGTH_SHORT).show()

                    initDataset()


                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    fun initDataset(){
        val callGetAlert: Call<ListAlert> =
            RetroClient.getInstance().apiService.getAlerts(userCookie)

        callGetAlert.enqueue(object : Callback<ListAlert> {
            override fun onResponse(call: Call<ListAlert>, response: Response<ListAlert>) {
                if (response.isSuccessful) {
                    if(showAll){
                        alerts = response.body()?.alerts as MutableList<Alert>
                        alertAdapter.dataSet = alerts
                    }
                    else{
                        val arr = response.body()?.alerts as MutableList<Alert>
                        alerts.clear()
                        for(alert in arr){
                            var str = alert.currency
                            if(str == "EUR"){
                                str = "EUR/USD"
                            }
                            else{
                                str+="/EUR"
                            }
                            if(str == currency){
                                alerts.add(alert)
                            }
                        }
                        alertAdapter.dataSet = alerts
                    }

                    alertAdapter.notifyDataSetChanged()

                }
            }

            override fun onFailure(call: Call<ListAlert>, t: Throwable) {
            }
        })
    }
    override fun onAddAlert() {
        initDataset()
    }
}
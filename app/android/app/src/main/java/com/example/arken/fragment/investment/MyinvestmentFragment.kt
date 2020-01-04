package com.example.arken.fragment.investment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.example.arken.R
import com.example.arken.fragment.signup_login.LoginFragment
import com.example.arken.model.investment.*
import com.example.arken.util.OnOrderClickedListener
import com.example.arken.util.OrderAdapter
import com.example.arken.util.RetroClient
import com.example.arken.util.TransactionAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyinvestmentFragment : Fragment(), OnOrderClickedListener,DepositClickListener {

    private lateinit var orderRecyclerView: RecyclerView
    private var orderDataset: MutableList<Order> = mutableListOf()
    private lateinit var orderAdapter: OrderAdapter
    private val args: MyinvestmentFragmentArgs by navArgs()

    private lateinit var transactionRecyclerView: RecyclerView
    private var transactionDataset: MutableList<TransactionHistory> = mutableListOf()
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var investment: Investment
    private lateinit var accountValues:TextView
    private lateinit var depositButton: Button
    private lateinit var buysellButton: Button

    private lateinit var orderButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(
            R.layout.fragment_myinvestments,
            container, false
        )
accountValues=rootView.findViewById(R.id.fragment_investment_account_values)
        orderRecyclerView = rootView.findViewById(R.id.fragment_investment_order_recyclerview)
        transactionRecyclerView =
            rootView.findViewById(R.id.fragment_investment_transaction_recyclerview)

        orderAdapter = OrderAdapter(this)
        orderAdapter.dataSet = orderDataset
        orderRecyclerView.adapter = orderAdapter

        transactionAdapter = TransactionAdapter()
        transactionAdapter.dataSet = transactionDataset
        transactionRecyclerView.adapter = transactionAdapter

        initDataset()
        orderRecyclerView.adapter?.notifyDataSetChanged()

depositButton=rootView.findViewById(R.id.fragment_investment_deposit)
        depositButton.setOnClickListener {
            val dialogFragment = DepositDialog(this,args.str)

           dialogFragment.show(fragmentManager!!,"deposit")
        }
        buysellButton=rootView.findViewById(R.id.fragment_investment_buysell)
        buysellButton.setOnClickListener {
            val dialogFragment = BuySellDialog(this,investment.currentRates)

            dialogFragment.show(fragmentManager!!,"buysell")
        }

        orderButton=rootView.findViewById(R.id.fragment_investment_give_order)
        orderButton.setOnClickListener {
            val dialogFragment = OrderDialog(this,investment.currentRates)

            dialogFragment.show(fragmentManager!!,"order")
        }

        return rootView
    }

    override fun onClicked(orderId: String, position: Int) {
        val userCookie = activity!!.getSharedPreferences(
            LoginFragment.MY_PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .getString("user_cookie", "")
        val call: Call<ResponseBody> =
            RetroClient.getInstance().apiService.deleteOrder(userCookie, orderId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Your order is deleted", Toast.LENGTH_SHORT)
                        .show()
                    orderDataset.removeAt(position)
                    orderAdapter.dataSet = orderDataset
                    orderAdapter.notifyDataSetChanged()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    private fun initDataset() {
        val userCookie = activity!!.getSharedPreferences(
            LoginFragment.MY_PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .getString("user_cookie", "")
        val call: Call<Investment> = RetroClient.getInstance().apiService.getInvestment(userCookie)
        call.enqueue(object : Callback<Investment> {
            override fun onResponse(call: Call<Investment>, response: Response<Investment>) {
                if (response.isSuccessful) {
                    investment = response.body()!!
                    transactionDataset=investment.histories
                    transactionAdapter.dataSet = transactionDataset
                    transactionAdapter.notifyDataSetChanged()
                    initAccountValues(investment.account)
                }
            }

            override fun onFailure(call: Call<Investment>, t: Throwable) {
            }
        })
        val call2: Call<ListOrder> = RetroClient.getInstance().apiService.getOrder(userCookie)
        call2.enqueue(object : Callback<ListOrder> {
            override fun onResponse(call: Call<ListOrder>, response: Response<ListOrder>) {
                if (response.isSuccessful) {
                    val listOrder = response.body()!!
                    orderDataset=listOrder.orders as MutableList<Order>
                    orderAdapter.dataSet = orderDataset
                    orderAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ListOrder>, t: Throwable) {
            }
        })
    }


    private fun initAccountValues(account: Account){
        var k  ="Total Profit : ${investment.totalProfit} EUR"
        if (account.eur!=0.0){
            k+="\nEUR : ${account.eur} "
        }
        if (account.tl!=0.0){
            k+="\nTRY : ${account.tl} "
        }
        if (account.usd!=0.0){
            k+="\nUSD : ${account.usd} "
        }
        if (account.aud!=0.0){
            k+="\nAUD : ${account.aud} "
        }
        if (account.cny!=0.0){
            k+="\nCNY : ${account.cny} "
        }
        if (account.hkd!=0.0){
            k+="\nHKD : ${account.hkd} "
        }
        if (account.inr!=0.0){
            k+="\nINR : ${account.inr} "
        }
        if (account.jpy!=0.0){
            k+="\nJPY : ${account.jpy} "
        }
        if (account.aed!=0.0){
            k+="\nAED : ${account.aed} "
        }
        if (account.ltc!=0.0){
            k+="\nLTC : ${account.ltc} "
        }
        if (account.xrp!=0.0){
            k+="\nXRP : ${account.xrp} "
        }
        if (account.eth!=0.0){
            k+="\nETH : ${account.eth} "
        }
        if (account.btc!=0.0){
            k+="\nBTC : ${account.btc} "
        }
        if (account.fb!=0.0){
            k+="\nFB : ${account.fb} "
        }
        if (account.amzn!=0.0){
            k+="\nAMZN : ${account.amzn} "
        }
        if (account.aapl!=0.0){
            k+="\nAAPL : ${account.aapl} "
        }
        if (account.msft!=0.0){
            k+="\nMSFT : ${account.msft} "
        }
        if (account.goog !=0.0){
            k+="\nGOOG : ${account.goog} "
        }
        accountValues.text=k
    }

    override fun onDepositClick() {
        initDataset()
    }
}
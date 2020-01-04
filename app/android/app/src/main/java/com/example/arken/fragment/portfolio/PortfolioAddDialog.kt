package com.example.arken.fragment.portfolio

import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.solver.widgets.ConstraintAnchor
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.anychart.charts.Resource
import com.example.arken.R
import com.example.arken.fragment.signup_login.LoginFragment.MY_PREFS_NAME
import com.example.arken.model.ArticleRateRequest
import com.example.arken.model.Portfolio
import com.example.arken.util.PortfolioTEAdapter
import com.example.arken.util.RetroClient
import com.example.arken.util.TEClickListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PortfolioAddDialog(val listener: PortfolioAddListener, val context2: Context, val portfolio:Portfolio?) : DialogFragment(), TEClickListener{

    lateinit var recyclerView: RecyclerView
    lateinit var editTextTitle: EditText
    lateinit var editTextDefinition: EditText
    lateinit var spinner: Spinner
    lateinit var switch: Switch
    var selectedTEs = ArrayList<String>()
    lateinit var addImageView: ImageView
    lateinit var TEAdapter:PortfolioTEAdapter
    lateinit var text:String
    lateinit var addButton: Button
    lateinit var cancelButton: Button

    override fun onTEClicked(position: Int) {
        val builder = AlertDialog.Builder(context2)
        builder.setTitle("Delete Trading Equipment")

        builder.setMessage("Do you want to delete " + selectedTEs[position] + " from your list?")

        builder.setPositiveButton("YES"){ _, _ ->
            selectedTEs.removeAt(position)
            TEAdapter.nameSet = selectedTEs
            TEAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("No"){ dialog, _ ->
                dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    interface PortfolioAddListener {
        fun onDialogPositiveClick()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_create_portfolio, null)
        recyclerView = view.findViewById(R.id.portfolio_te_list)
        TEAdapter = PortfolioTEAdapter(selectedTEs, this)
        recyclerView.adapter = TEAdapter
        spinner = view.findViewById(R.id.portfolio_spinner)
        addImageView = view.findViewById(R.id.portfolio_spinner_add)
        addImageView.setOnClickListener {
            if(selectedTEs.contains(spinner.selectedItem.toString())){
                Toast.makeText(context2, "You already have "+ spinner.selectedItem.toString() + " on your list", Toast.LENGTH_SHORT).show()
            }
            else{
                selectedTEs.add(spinner.selectedItem.toString())
                TEAdapter.nameSet = selectedTEs
                TEAdapter.notifyDataSetChanged()
            }

        }
        editTextTitle = view.findViewById(R.id.portfolio_title)
        editTextDefinition = view.findViewById(R.id.portfolio_definition)
        switch = view.findViewById(R.id.porfolio_isPrivateSwitch)
        view.setBackgroundColor(Color.BLACK)
        if(portfolio!= null){
            editTextTitle.setText(portfolio.title)
            editTextTitle.inputType = InputType.TYPE_NULL
            editTextDefinition.setText(portfolio.definition)
            switch.isChecked = portfolio.isPrivate
            if(portfolio.tradingEqs!= null){
                val arr = portfolio.tradingEqs as ArrayList<String>
                selectedTEs.clear()
                for(te in arr){
                    if(te == "EUR"){
                        selectedTEs.add("EUR/USD")
                    }
                    else{
                        selectedTEs.add(te+"/EUR")
                    }
                }
                TEAdapter.nameSet = selectedTEs
                TEAdapter.notifyDataSetChanged()
            }
            else{
                selectedTEs = ArrayList()
            }
        }

        val arr = resources.getStringArray(R.array.TeList)
        ArrayAdapter(
            context2,
            R.layout.custom_spinner,
            arr
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        if(portfolio == null){
            text = "ADD"
        }
        else{
            text = "EDIT"
        }

        addButton = view.findViewById(R.id.portfolio_add_button)
        addButton.text = text
        addButton.setOnClickListener{
            if(editTextTitle.text.toString().trim() == ""){
                editTextTitle.error = "Please type a title"
            }
            else if(editTextDefinition.text.toString().trim() == ""){
                editTextDefinition.error = "Please type a definition"
            }
            else if(selectedTEs.isEmpty()){
                Toast.makeText(context2, "Please add a TE to your list", Toast.LENGTH_SHORT).show()
            }
            else if (portfolio == null){

                var arr = arrayListOf<String>()
                for(te in selectedTEs){
                    arr.add(te.substring(0, te.indexOf("/")))
                }
                selectedTEs = arr
                TEAdapter.nameSet = selectedTEs
                TEAdapter.notifyDataSetChanged()

                val prefs = activity!!.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                val portfolio = Portfolio(null, editTextTitle.text.toString(), editTextDefinition.text.toString(), switch.isChecked, selectedTEs, null, null, null)

                val call: Call<ResponseBody> =
                    RetroClient.getInstance().apiService.createPortfolio(
                        prefs.getString("user_cookie", null), portfolio)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(context2, "Your portfolio is added", Toast.LENGTH_SHORT)
                                .show()
                            dialog?.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    }
                })
                listener.onDialogPositiveClick()
            }
            else{
                val prefs = activity!!.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
                val portfolio2 = Portfolio(null , editTextTitle.text.toString(), editTextDefinition.text.toString(), switch.isChecked, selectedTEs, null, null, null)

                val call: Call<ResponseBody> =
                    RetroClient.getInstance().apiService.editPortfolio(
                        prefs.getString("user_cookie", null), portfolio._id, portfolio2)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(context2, "Your portfolio is updated", Toast.LENGTH_SHORT)
                                .show()
                            dialog?.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                    }
                })
                listener.onDialogPositiveClick()
            }
        }

        cancelButton = view.findViewById(R.id.portfolio_cancel_button)
        cancelButton.setOnClickListener{
            dialog?.dismiss()
        }
        return view
    }
}

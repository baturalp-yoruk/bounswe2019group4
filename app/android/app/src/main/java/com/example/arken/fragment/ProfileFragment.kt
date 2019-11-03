package com.example.arken.fragment


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.arken.R
import com.example.arken.activity.MainActivity.IMAGE_PREF
import com.example.arken.model.Profile
import com.example.arken.util.RetroClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//import java.util.jar.Manifest

class ProfileFragment : Fragment() {

    private lateinit var name_textView: TextView
    private lateinit var surname_textView: TextView
    private lateinit var location_value_textView: TextView
    private lateinit var user_type_textView: TextView
    private lateinit var profile_image: ImageButton
    private lateinit var email_value_textView: TextView
    private lateinit var pred_value_textView: TextView
    private lateinit var uri: String
    private lateinit var profile: Profile



   // private var profile: Profile? = null
    // val prefs: SharedPreferences? = activity!!.getSharedPreferences(MY_PREFS_NAME!!, MODE_PRIVATE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        name_textView = view.findViewById(R.id.name_textView)
        surname_textView = view.findViewById(R.id.surname_textView)
        user_type_textView = view.findViewById(R.id.user_type_textView)
        location_value_textView = view.findViewById(R.id.location_value_textView)
        email_value_textView = view.findViewById(R.id.email_value_textView)
        pred_value_textView = view.findViewById(R.id.pred_value_textView)


       val call: Call<Profile> = RetroClient.getInstance().apiService.getProfile(activity!!.getSharedPreferences(
            LoginFragment.MY_PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .getString("userId", "defaultId"))

        call.enqueue(object : Callback<Profile> {
            override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                if (response.isSuccessful) {
                    profile = response.body()!!

                    name_textView.text = profile!!.name
                    surname_textView.text = profile!!.surname
                    pred_value_textView.text = profile!!.predictionRate
                    user_type_textView.text = if (profile?.isTrader!!) {"Trader User"} else {"Basic User"}
                    location_value_textView.text = profile!!.location
                    email_value_textView.text = profile!!.email


                } else {
                    Toast.makeText(context, response.raw().toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Profile>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })

        profile_image = view.findViewById(R.id.profile_image)

        val path = getPreference(context!!, "path" )
        if(path!=null) {
            profile_image.setImageURI(path!!.toUri())
        }
        profile_image.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(context?.let { it1 -> checkSelfPermission(it1,Manifest.permission.READ_EXTERNAL_STORAGE) } ==
                        PackageManager.PERMISSION_DENIED) {

                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                    requestPermissions(permissions, PERMISSION_CODE)

                } else {
                    pickImageFromGallery()
                }

            } else {
                pickImageFromGallery()
            }
        }





        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if(grantResults.size>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {

                }

            }
        }
    }




    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            uri = data!!.data!!.toString()

            setPreference(context!!, uri, "path")

            profile_image.setImageURI(uri!!.toUri())

        }
    }

    private fun getProfileInfo() {


    }


    fun setPreference(c: Context, value: String, key: String): Boolean {
        var settings = c.getSharedPreferences(IMAGE_PREF, 0)
        val editor = settings.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    fun getPreference(c: Context, key: String): String {
        var settings = c.getSharedPreferences(IMAGE_PREF, 0)
        return settings.getString(key, "")!!
    }

    companion object {

        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }

}
package com.example.arken.fragment.article

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.arken.R
import com.example.arken.model.Annotation
import com.example.arken.model.Article
import com.example.arken.model.AnnoCreateRequest
import com.example.arken.util.AnnotationRetroClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TextAnnotationDialogFragment(
    var onAnnoClickListener: AnnoClickListener,
    val toCreate: Boolean,
    val article:Article,
    val start:Int?,
    val end:Int?,
    val str:String?,
    val username:String?
) : DialogFragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var edit_button: Button
    private lateinit var delete_button: Button
    private lateinit var add_button: Button
    private lateinit var author:TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView: View = inflater.inflate(R.layout.dialog_text_annotation, container, false)

        var anno_editText = rootView.findViewById<EditText>(R.id.text_add_annotation_edittext)

        edit_button = rootView.findViewById<Button>(R.id.text_anno_edit_button)
        delete_button = rootView.findViewById<Button>(R.id.text_anno_delete_button)
        add_button = rootView.findViewById<Button>(R.id.text_anno_add_button)
        author=rootView.findViewById((R.id.text_anno_username))
        if (!toCreate) {
            anno_editText.setText(str)
author.text="Author: ${username}"

            edit_button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    dismiss()
                }
            })
            delete_button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    dismiss()
                }
            })
            edit_button.visibility=View.GONE
            delete_button.visibility=View.GONE
            add_button.visibility=View.GONE
        } else {
            author.visibility=View.GONE

            add_button.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val a:Annotation= Annotation()
                    a.annotationText=anno_editText.text.toString()
                    a.articleId=article._id
                    a.userId=prefs.getString("userId",null)
                    a.type="Text"
                    a.username=prefs.getString("annotation_username",null)
                    a.startIndex=start
                    a.finishIndex=end
                    val call: Call<ResponseBody> = AnnotationRetroClient.getInstance().annotationAPIService.createAnnotation(prefs.getString("user_cookie",null),
                        AnnoCreateRequest(
                            "http://www.w3.org/ns/anno.jsonld",
                            "Annotation",
                            a,
                            "http://www.example.com/index.htm",
                            article._id!!,
                            null
                        )
                    )
                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                System.out.println("Successfull")
                                onAnnoClickListener.onAnnoClick()
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        }
                    })
                    dismiss()
                }
            })
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = getActivity()!!.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE)
        setVisibilty(prefs.getString("userId",null),toCreate)
    }
    private fun setVisibilty(k:String?,t:Boolean){
        if(k !=null){
            if(t){
                add_button.visibility=View.VISIBLE
            }
        }else{
           // dismiss()
        }
    }
}




interface AnnoClickListener {
    fun onAnnoClick()
}
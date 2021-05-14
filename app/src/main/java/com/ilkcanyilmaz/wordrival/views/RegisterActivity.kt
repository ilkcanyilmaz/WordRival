package com.ilkcanyilmaz.wordrival.views

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.ilkcanyilmaz.wordrival.FirestoreOperation
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.enums.RegisterType
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "GoogleActivity"
    private var userPhoto = "1";
    private lateinit var firestoreOperation: FirestoreOperation
    private var registerType: RegisterType = RegisterType.MAIL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        LoadSpinner()
        btn_register.setOnClickListener(this)
        firestoreOperation = FirestoreOperation()
        val userName = intent.getStringExtra("userName")
        val userMail = intent.getStringExtra("userMail")
        val photoUrl = intent.getStringExtra("userPhoto")
        if (photoUrl != null) {
            userPhoto = photoUrl
            LoadProfilePhoto(userPhoto)
        }
        val mRegisterType = intent.getStringExtra("registerType")
        if (userName != null && userMail != null) {
            edt_userFullName.setText(userName.capitalizeWords())
            edt_userMail.setText(userMail)
            edt_userMail.isEnabled = false
            edt_userFullName.isEnabled = false
            edt_userFullName.setTextColor(Color.BLACK)
            edt_userMail.setTextColor(Color.BLACK)

        } else {
            edt_password.visibility = View.VISIBLE
        }
        if (mRegisterType == null) {
            registerType = RegisterType.MAIL
        } else if (mRegisterType == "google") {
            registerType = RegisterType.GOOGLE
            this.userPhoto = userPhoto
        } else if (mRegisterType == "facebook") {
            registerType = RegisterType.FACEBOOK
        }
    }

    fun LoadProfilePhoto(urlString: String) {
        Glide.with(this).load(urlString).into(img_profile);
    }

    fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }

    private fun LoadSpinner() {
        val list: MutableList<String> = ArrayList()
        list.add("Az")
        list.add("Orta")
        list.add("İyi")
        val dataAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, list
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spn_level.setAdapter(dataAdapter)
        /*spn_level.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                TODO("Not yet implemented")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }*/
    }

    private fun RegisterWithMailAndPassword(mail: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                override fun onComplete(p0: Task<AuthResult>) {
                    if (p0.isSuccessful) {
                        firestoreOperation.insertNewUserFirestore(
                            edt_userMail.text.toString(),
                            edt_userNickName.text.toString(),
                            edt_userFullName.text.toString(),
                            1,
                            "1",
                            activity = this@RegisterActivity
                        )
                    } else {
                        Toast.makeText(applicationContext, "Bir hata oluştu.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            })
    }

    private fun RegisterWithGoogle() {
        firestoreOperation.insertNewUserFirestore(
            edt_userMail.text.toString(),
            edt_userNickName.text.toString(),
            edt_userFullName.text.toString(),
            1,
            userPhoto,
            activity = this
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_register -> {
                if (registerType == RegisterType.MAIL)
                    RegisterWithMailAndPassword(
                        edt_userMail.text.toString(),
                        edt_password.text.toString()
                    ) else{
                    RegisterWithGoogle()
                }
            }
        }
    }
}
package com.ilkcanyilmaz.wordrival.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.models.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 9001

    private var mAuth: FirebaseAuth? = null

    // [END declare_auth]
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var progressBar: ProgressBar? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
        // Configure Google Sign In
        // Configure Google Sign In
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun init() {
        progressBar = findViewById(R.id.progressBar1)
        btn_signInButton?.setOnClickListener(this)
        txt_register.setOnClickListener(this)
        btn_signInGoogle.setOnClickListener(this)
        db = Firebase.firestore

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account =
                    task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String?) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    val citiesRef = db.collection("Users");
                    val query = citiesRef.whereEqualTo("userMail", user?.email)
                    query.get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                val db: DatabaseManager? = DatabaseManager.getDatabaseManager(this)
                                val mUser: User = User()
                                for (document in documents) {
                                    mUser.userToken = document["userToken"].toString()
                                    mUser.userNickName = document["userNickName"].toString()
                                    mUser.userMail = document["userMail"].toString()
                                    mUser.userName = document["userFullName"].toString()
                                    mUser.userLevel = document["userLevel"].toString().toInt()
                                    mUser.userPhoto = document["userPhoto"].toString()
                                }
                                db?.userDao()?.delete(mUser)
                                db?.userDao()?.insert(user = mUser)
                                finish()
                            } else {
                                val intent =
                                    Intent(this@LoginActivity, RegisterActivity::class.java)
                                intent.putExtra("userName", user?.displayName)
                                intent.putExtra("userMail", user?.email)
                                intent.putExtra("userPhoto", user?.photoUrl.toString())
                                intent.putExtra("registerType", "google")
                                startActivity(intent)
                                finish()
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents: ", exception)
                        }


                    //InsertNewUserFirestore(userName = user?.displayName.toString())

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                }

                // [START_EXCLUDE]
                hideProgressBar()
                // [END_EXCLUDE]
            }
    }

    /**
     * Kullanıcının daha önceden kayıt olup olmadığını kontrol eder.
     */
    private fun UserRegisterControl(mailAddress: String): Boolean {
        val citiesRef = db.collection("Users");
        var isRegister = false
        val query = citiesRef.whereEqualTo("userMailAddress", mailAddress)
        query.get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    isRegister = true
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        return isRegister
    }

    private fun showProgressBar() {
        progressBar!!.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar!!.visibility = View.GONE
    }

    private fun InsertNewUserFirestore(userName: String) {
        val user = hashMapOf(
            "name" to userName,
        )

        db.collection("Database").document("users")
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
    // [END auth_with_google]

    // [END auth_with_google]
    // [START signin]
    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]

    // [END signin]
    private fun signOut() {
        // Firebase sign out
        mAuth!!.signOut()

        // Google sign out
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this,
            OnCompleteListener<Void?> {
                //updateUI(null);
            })
    }

    private fun revokeAccess() {
        // Firebase sign out
        mAuth!!.signOut()

        // Google revoke access
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this,
            OnCompleteListener<Void?> {
                //updateUI(null);
            })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_signInButton -> {
                signInGoogle()
            }
            R.id.txt_register -> {
                val intent = Intent(applicationContext, RegisterActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_signInGoogle -> {
                signInGoogle()
            }
        }
    }
}
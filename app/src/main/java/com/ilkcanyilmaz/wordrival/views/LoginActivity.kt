package com.ilkcanyilmaz.wordrival.views

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.models.User
import com.ilkcanyilmaz.wordrival.repositories.UserLocalDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 9001

    @Inject
    lateinit var userDataSource: UserLocalDataSource

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    // [END declare_auth]
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var progressBar: ProgressBar? = null
    private lateinit var db: FirebaseFirestore
    private val callback: CallbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
        setBackground()
        mAuth = FirebaseAuth.getInstance()
    }

    private fun init() {
        progressBar = findViewById(R.id.progressBar1)
        btn_signInGoogle.setOnClickListener(this)
        cv_signInFacebook.setOnClickListener(this)
        db = Firebase.firestore
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun setBackground() {
        Glide.with(this)
            .asBitmap()
            .load(R.drawable.background)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    val bdrawable = BitmapDrawable(getResources(), resource)
                    layout_login.background = bdrawable
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        var photoUrl = ""
        val params = Bundle()
        params.putBoolean("redirect", false)
        params.putString("height", "200")
        params.putString("type", "normal")
        params.putString("width", "200")

        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/" + token.userId + "/picture",
            params,
            HttpMethod.GET
        ) { response ->
            val jsonData =
                response?.jsonObject?.getJSONObject("data")
            photoUrl = jsonData?.getString("url").toString()
        }.executeAsync()

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    val userid = token.userId
                    val ref = db.collection("Users");
                    val query = ref.whereEqualTo("userMail", user?.email)
                    query.get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                val mUser: User = User()
                                for (document in documents) {
                                    mUser.userToken = document["userToken"].toString()
                                    mUser.userNickName = document["userNickName"].toString()
                                    mUser.userMail = document["userMail"].toString()
                                    mUser.userName = document["userFullName"].toString()
                                    mUser.userPhoto = document["userPhoto"].toString()
                                    mUser.userId = mAuth!!.uid.toString()
                                }
                                userDataSource.deleteUser(mUser)
                                userDataSource.addUser(mUser)
                                finish()
                            } else {
                                val intent =
                                    Intent(this@LoginActivity, RegisterActivity::class.java)
                                intent.putExtra("userName", user?.displayName)
                                intent.putExtra("userMail", user?.email)
                                intent.putExtra("userPhoto", photoUrl)
                                intent.putExtra("registerType", "facebook")
                                startActivity(intent)
                                finish()
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents: ", exception)
                        }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("hatata", "signInWithCredential:failure", task.exception)
                    if (task.exception!!.message == "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.") {
                        Toast.makeText(
                            applicationContext,
                            "Aynı e-posta adresiyle bir hesap zaten mevcut. Bu e-posta adresiyle ilişkili bir sağlayıcıyı kullanarak giriş yapın.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(applicationContext, "Bir sorun oluştu", Toast.LENGTH_LONG)
                            .show()
                    }
                    signOut()
                }
            }
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
        callback.onActivityResult(requestCode, resultCode, data)

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
                    val ref = db.collection("Users");
                    val query = ref.whereEqualTo("userMail", user?.email)
                    query.get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                val mUser: User = User()
                                for (document in documents) {
                                    mUser.userToken = document["userToken"].toString()
                                    mUser.userNickName = document["userNickName"].toString()
                                    mUser.userMail = document["userMail"].toString()
                                    mUser.userName = document["userFullName"].toString()
                                    mUser.userPhoto = document["userPhoto"].toString()
                                    mUser.userId = mAuth!!.uid.toString()
                                }
                                userDataSource.deleteUser(mUser)
                                userDataSource.addUser(mUser)
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
        progressBar1!!.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar1!!.visibility = View.GONE
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
            R.id.btn_signInGoogle -> {
                signInGoogle()
            }
            R.id.cv_signInFacebook -> {
                LoginManager.getInstance().logInWithReadPermissions(
                    this, listOf(
                        "email",
                        "public_profile"
                    )
                )
                LoginManager.getInstance().registerCallback(
                    callback,
                    object : FacebookCallback<LoginResult?> {
                        override fun onSuccess(loginResult: LoginResult?) {
                            val accessToken = AccessToken.getCurrentAccessToken()
                            val isLoggedIn = accessToken != null && !accessToken.isExpired
                            if (isLoggedIn) {
                                handleFacebookAccessToken(accessToken)
                            }
                        }

                        override fun onCancel() {
                            Log.d("FacebookLoginCancel:", "onCancel")
                        }

                        override fun onError(exception: FacebookException) {
                            Log.d("FacebookLoginError:", exception.message.toString())
                            // App code
                        }
                    })
            }
        }
    }
}
package com.ilkcanyilmaz.wordrival.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.viewmodels.SplashActivityViewModel
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    var firestore = FirebaseFirestore.getInstance()
    private lateinit var db: DatabaseManager
    private lateinit var viewModel: SplashActivityViewModel

    init {
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
    }

    private fun questionSync() {
        db.questionDao().delete()
        Firebase.firestore.collection("Questions").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.w("FirebaseQuestionData", "Empty")
                } else {
                    for (document in documents) {
                        val objQuestion = document.toObject(Question::class.java)
                        db.questionDao().insert(objQuestion)
                        /* dialog.ll_friend.visibility = View.VISIBLE
                         dialog.txt_userName.text = friend.userNickName
                         dialog.btn_addFriendRequest.setOnClickListener({
                             SendFriendRequest(friend)
                         })*/
                    }
                }
            }
    }

    private suspend fun syncQuestion() {


    }

    private fun animation() {
        val animLeftToRight = AnimationUtils.loadAnimation(applicationContext, R.anim.left_to_right)
        img_logoSquare1.animation = animLeftToRight
        val animRightToLeft = AnimationUtils.loadAnimation(applicationContext, R.anim.right_to_left)
        img_logoSquare2.animation = animRightToLeft


        val animation1 = AlphaAnimation(0.2f, 1.0f)
        animation1.duration = 1000
        animation1.startOffset = 500
        animation1.fillAfter = true
        txt_appName.startAnimation(animation1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash)

        viewModel = ViewModelProviders.of(this).get(
            SplashActivityViewModel::class.java
        )
        db = DatabaseManager.getDatabaseManager(context = applicationContext)!!
        val localQuestionSize = db.questionDao().getQuestion().size

        viewModel.getQuestionSize()
        viewModel.questionSize.observe(this) {
            if (it > localQuestionSize) {
                GlobalScope.launch {
                    questionSync()
                }
            }
        }

        animation()

        val timerThread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(2000)

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    if (user != null) {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0);

                    } else {
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(0, 0);
                    }
                }
            }
        }
        timerThread.start()


    }
}
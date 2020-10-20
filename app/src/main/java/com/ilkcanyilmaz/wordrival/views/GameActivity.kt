package com.ilkcanyilmaz.wordrival.views

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.R
import com.ilkcanyilmaz.wordrival.enums.GameType
import com.ilkcanyilmaz.wordrival.models.Questions
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore
    private var functions: FirebaseFunctions
    private var getQuestions: ArrayList<Questions> = ArrayList()
    private var questionCounter = 0
    private var trueAnswerCounter = 0
    private var falseAnswerCounter = 0
    private var gameType=GameType.FRIEND
    private var gameId:String=""

    init {
        functions = FirebaseFunctions.getInstance()
        mAuth = FirebaseAuth.getInstance()
        db = Firebase.firestore

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        init()
        gameType = intent.getSerializableExtra("gameType") as GameType
        gameId = intent.getStringExtra("gameId")
       /* functions
            .getHttpsCallable("getListener")
            .call(null)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as String
                result
            }*/

        /*val docRef = db.collection("Pool").document("users")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                *//* if (edt_test.text.toString() != snapshot.data?.get("name")?.toString()) {
                     edt_test.setText(snapshot.data?.get("name")?.toString())
                 }*//*
                Log.d("TAG", "Current data: ${snapshot.data}")
            } else {
                Log.d("TAG", "Current data: null")
            }
        }*/
        getQuestions()

    }
    private fun getQuestions() {
        if (gameId != null) {
            db.collection("Games").document(gameId).collection("Questions")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w("GoogleActivity", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    for (doc in value!!) {
                        val question = Questions(
                            doc.getString("word").toString(),
                            doc.getString("answerTrue").toString(),
                            doc.getString("answerFalse1").toString(),
                            doc.getString("answerFalse2").toString(),
                            doc.getString("answerFalse3").toString(),
                            doc.get("level").toString().toInt(),
                            doc.get("user1Answer").toString().toInt(),
                            doc.get("user2Answer").toString().toInt()
                        )
                        getQuestions.add(question)

                    }
                    LoadQuestion()
                    //Log.d(TAG, "Current cites in CA: $cities")
                }
        }
    }

    private fun UpdateEdittext(name: String) {
        val nameMap = mapOf("name" to name)
        db.collection("Pool").document("users")
            .set(nameMap)
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w("TAG", "Error writing document", e) }
    }

    private fun init() {
        btn_answer1.setOnClickListener(this)
        btn_answer2.setOnClickListener(this)
        btn_answer3.setOnClickListener(this)
        btn_answer4.setOnClickListener(this)

    }

    private fun LoadQuestion() {
        txt_word.text = getQuestions[questionCounter].word
        btn_answer1.text = getQuestions[questionCounter].answerFalse1
        btn_answer2.text = getQuestions[questionCounter].answerFalse2
        btn_answer3.text = getQuestions[questionCounter].answerFalse3
        btn_answer4.text = getQuestions[questionCounter].answerTrue
    }

    private fun ChoiceOfAnswerClick(answer: String) {
        if (answer == getQuestions[questionCounter].answerTrue) {
            trueAnswerCounter++
            user_score1.text = trueAnswerCounter.toString()
        } else {
            falseAnswerCounter++
        }
        questionCounter++
        LoadQuestion()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_answer1 -> {
                ChoiceOfAnswerClick(btn_answer1.text.toString())
            }
            R.id.btn_answer2 -> {
                ChoiceOfAnswerClick(btn_answer2.text.toString())
            }
            R.id.btn_answer3 -> {
                ChoiceOfAnswerClick(btn_answer3.text.toString())
            }
            R.id.btn_answer4 -> {
                ChoiceOfAnswerClick(btn_answer4.text.toString())
            }

        }
    }
}
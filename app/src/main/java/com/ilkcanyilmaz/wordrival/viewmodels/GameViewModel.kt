package com.ilkcanyilmaz.wordrival.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.enums.UserType
import com.ilkcanyilmaz.wordrival.models.Game
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.User

class GameViewModel : ViewModel() {
    private val TAG = "DocSnippets"

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val questions = MutableLiveData<List<Question>>()
    val gameDetail = MutableLiveData<Game>()
    val user1 = MutableLiveData<User>()
    val user2 = MutableLiveData<User>()

    fun getUserById(userId: String, userType: Int) {
        val docRef = Firebase.firestore.collection("Users").document(userId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val objUser = snapshot?.toObject(User::class.java)
            if (userType == UserType.USER1.getTypeID()) {
                user1.value = objUser
            } else if (userType== UserType.USER2.getTypeID()) {
                user2.value = objUser
            }
        }
    }

    fun getGameById(gameId: String) {
        Log.d("RandomGameId", gameId)
        val docRef = Firebase.firestore.collection("Games").document(gameId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            val objGame = snapshot?.toObject(Game::class.java)
            gameDetail.value = objGame
        }

    }

    fun getQuestionByGameId(gameId: String) {
        firestore.collection("Games").document(gameId).collection("Questions")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("GoogleActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                val objQuestion = value?.toObjects(Question::class.java)
                /*for (doc in value!!) {
                val question = Question(
                    doc.getString("word").toString(),
                    doc.getString("answerTrue").toString(),
                    doc.getString("answerFalse1").toString(),
                    doc.getString("answerFalse2").toString(),
                    doc.getString("answerFalse3").toString(),
                    doc.get("level").toString().toInt(),
                    doc.get("user1Answer").toString().toInt(),
                    doc.get("user2Answer").toString().toInt()
                )*/
                questions.value = objQuestion

            }
    }}
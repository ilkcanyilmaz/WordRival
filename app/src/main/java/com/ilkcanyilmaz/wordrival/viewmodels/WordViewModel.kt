package com.ilkcanyilmaz.wordrival.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ilkcanyilmaz.wordrival.adapters.WordListAdapter
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.WordModel
import com.ilkcanyilmaz.wordrival.repositories.FirestoreRepository
import com.ilkcanyilmaz.wordrival.repositories.QuestionLocalDataSource
import com.ilkcanyilmaz.wordrival.repositories.WordLocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.android.synthetic.main.fragment_word.*
import javax.inject.Inject

@HiltViewModel
class WordViewModel @Inject constructor(
    application: Application,
    val firestoreRepository: FirestoreRepository,
    val wordLocalDataSource: WordLocalDataSource,
    val questionLocalDataSource: QuestionLocalDataSource
) :
    AndroidViewModel(application) {

    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var getWords = MutableLiveData<List<Question>>()
    fun wordToQuestionById() {
        wordLocalDataSource.getWord {
            val questionsMap = it.map { word ->
                word.questionId
            }

        }
    }

    fun getWordsFirestore(){
        val docRef = Firebase.firestore.collection("Users").document(FirebaseAuth.getInstance().uid.toString()).collection("Words")
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }

            val objWord = snapshot?.toObjects(WordModel::class.java) as List<WordModel>
            val wordIDs = objWord.map {
                it.questionId
            }
            getWords.postValue(questionLocalDataSource.getQuestionsById(wordIDs))

        }
    }
}
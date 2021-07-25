package com.ilkcanyilmaz.wordrival.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.repositories.FirestoreRepository
import com.ilkcanyilmaz.wordrival.repositories.QuestionLocalDataSource
import com.ilkcanyilmaz.wordrival.repositories.WordLocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
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
            getWords.postValue(questionLocalDataSource.getQuestionsById(questionsMap))
        }
    }
}
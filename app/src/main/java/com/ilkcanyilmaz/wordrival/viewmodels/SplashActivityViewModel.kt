package com.ilkcanyilmaz.wordrival.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivityViewModel(application: Application) : AndroidViewModel(application) {
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val questionSize = MutableLiveData<Int>()

    fun getQuestionSize() {
        firestore.collection("Questions")
            .get().addOnSuccessListener {
                questionSize.value = it.documents.size
            }
    }
}
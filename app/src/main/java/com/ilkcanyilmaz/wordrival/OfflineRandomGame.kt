package com.ilkcanyilmaz.wordrival

import com.ilkcanyilmaz.wordrival.databases.DatabaseManager
import com.ilkcanyilmaz.wordrival.models.Question

fun offlineRandomGame(db:DatabaseManager):ArrayList<Question>{
   val questions=ArrayList<Question>()
    var i=0
    while (i<4){
        val randomQuestion=db.questionDao().getQuestion().random()
        if(!questions.contains(randomQuestion)){
            questions.add(randomQuestion)
            i++
        }
    }

    return questions
}
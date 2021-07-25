package com.ilkcanyilmaz.wordrival

import com.ilkcanyilmaz.wordrival.models.Question

/**
 * Rastgele offline soru üretir
 */

fun offlineRandomGame(questions: List<Question>): ArrayList<Question> {
    val questionCount = 4
    val randomQuestions = ArrayList<Question>()
    var i = 0
    while (i < questionCount) {
        val randomQuestion=questions.random()
            if (!randomQuestions.contains(randomQuestion)) {
                randomQuestions.add(randomQuestion)
                i++
            }
    }

    return randomQuestions
}
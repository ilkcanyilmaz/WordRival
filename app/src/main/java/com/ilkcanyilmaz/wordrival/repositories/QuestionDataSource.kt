package com.ilkcanyilmaz.wordrival.repositories

import com.ilkcanyilmaz.wordrival.models.Question
import com.ilkcanyilmaz.wordrival.models.User

interface QuestionDataSource {
    fun addQuestion(question: Question)
    fun getQuestion():List<Question>
    fun getQuestionsById(filterValues:List<String>): List<Question>
    fun deleteQuestion()
}
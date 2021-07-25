package com.ilkcanyilmaz.wordrival.repositories

import android.os.Handler
import android.os.Looper
import com.ilkcanyilmaz.wordrival.daos.QuestionDao
import com.ilkcanyilmaz.wordrival.models.Question
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class QuestionLocalDataSource @Inject constructor(private val questionDao: QuestionDao) :
    QuestionDataSource {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun addQuestion(question: Question) {
        executorService.execute {
            questionDao.insert(
                question
            )
        }
    }

    override fun getQuestion(): List<Question> {
        return questionDao.getQuestion()
    }

    override fun getQuestionsById(filterValues:List<String>): List<Question> {
        return questionDao.getQuestionsById(filterValues)
    }

    override fun deleteQuestion() {
        executorService.execute {
            questionDao.delete()
        }
    }
}
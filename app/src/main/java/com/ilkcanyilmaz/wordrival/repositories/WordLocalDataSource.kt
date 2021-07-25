package com.ilkcanyilmaz.wordrival.repositories

import com.ilkcanyilmaz.wordrival.models.WordModel

import android.os.Handler
import android.os.Looper
import com.ilkcanyilmaz.wordrival.daos.UserDao
import com.ilkcanyilmaz.wordrival.daos.WordDao
import com.ilkcanyilmaz.wordrival.models.User
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class WordLocalDataSource @Inject constructor(private val wordDao: WordDao) : WordDataSource {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun addWord(word: WordModel) {
        executorService.execute {
            wordDao.insert(
                word
            )
        }
    }

    override fun getWord(callback: (List<WordModel>) -> Unit) {
        executorService.execute {
            val word = wordDao.getWord()
            mainThreadHandler.post { callback(word) }
        }
    }

    override fun deleteWord(word: WordModel) {
        executorService.execute {
            wordDao.delete(word)
        }
    }
}
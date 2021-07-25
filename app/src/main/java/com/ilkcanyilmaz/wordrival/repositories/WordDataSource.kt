package com.ilkcanyilmaz.wordrival.repositories

import com.ilkcanyilmaz.wordrival.models.WordModel

interface WordDataSource {
    fun addWord(word: WordModel)
    fun getWord(callback: (List<WordModel>) -> Unit)
    fun deleteWord(word: WordModel)
}
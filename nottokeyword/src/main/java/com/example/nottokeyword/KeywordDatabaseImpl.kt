package com.example.nottokeyword

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await
import kr.bydelta.koala.hnn.Tagger
import java.util.*
import javax.inject.Inject

internal class KeywordDatabaseImpl @Inject constructor(
    private val context: Context,
    private val database: DatabaseReference
) : KeywordDatabase {

    override suspend fun insertKeyword(content: String): Boolean {
        val keywords = getKeywordsFrom(content)
        return insertKeywords(keywords)
    }

    private fun getKeywordsFrom(text: String): List<String> {
        return Tagger().tagSentence(text)
            .getNouns()
            .map { it.surface }
    }

    private suspend fun insertKeywords(keywords: List<String>): Boolean {
        val newKeywords: MutableMap<String, Any> = keywords.map { it to 1 }.toMap().toMutableMap()

        database.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach {
                val key = it.key!!
                val count = (it.value as Long).toInt()

                if (newKeywords.containsKey(key)) {
                    newKeywords[key] = count + 1
                }
            }
        }.await()

        return updateKeywords(newKeywords)
    }

    private suspend fun updateKeywords(keywords: MutableMap<String, Any>): Boolean {
        var result = false

        database.updateChildren(keywords)
            .addOnSuccessListener { result = true }
            .await()

        return result
    }

    override suspend fun getKeywords(callback: (List<Keyword>) -> Unit) {
        if (isOnline().not()) return

        database.orderByValue().limitToLast(POPULAR_KEYWORD_LIMIT).get().addOnSuccessListener {
            Log.i(TAG, "Got value ${it.value}")

            val list = it.children.mapNotNull { child ->
                child.key?.let{ key ->
                    child.value?.let{ value ->
                        Keyword(key, (value as Long).toInt())
                    }
                }
            }.reversed()

            callback(list)
        }.addOnFailureListener {
            Log.e(TAG, "Error getting data", it)
        }
    }

    private fun isOnline(): Boolean {
        val manager = getSystemService(context, ConnectivityManager::class.java) ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.activeNetwork != null
        } else {
            manager.activeNetworkInfo?.isConnected ?: false
        }
    }

    override suspend fun deleteKeyword(keyword: String): Boolean {
        var result = false

        database.child(keyword).apply {
            get().addOnSuccessListener {
                Log.i(TAG, "Got value ${it.value}")

                val count = it.value?.let { count ->
                    (count as Long).toInt() - 1
                } ?: return@addOnSuccessListener

                when {
                    count == 0 -> removeValue().addOnSuccessListener { result = true }
                    count > 0 -> setValue(count).addOnSuccessListener { result = true }
                }
            }
        }

        return result
    }

    companion object {
        val TAG: String = KeywordDatabaseImpl::class.java.simpleName
    }
}
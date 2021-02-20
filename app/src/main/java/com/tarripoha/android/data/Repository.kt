package com.tarripoha.android.data

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.data.db.WordDatabase
import com.tarripoha.android.data.rest.RetrofitApi
import com.tarripoha.android.util.Utils
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
class Repository(
  private var db: WordDatabase?,
  var api: RetrofitApi,
  private val context: Context
) {

  private val ioExecutor: Executor by lazy { Executors.newSingleThreadExecutor() }

  private val wordRef: DatabaseReference by lazy { Firebase.database.getReference("word") }

  fun addWord(
    word: Word,
    success: () -> Unit,
    failure: (Exception) -> Unit
  ) {

    wordRef.child(word.name)
        .setValue(word)
        .addOnSuccessListener {
          Utils.showToast(context, "Data Added")
          success()
        }
        .addOnFailureListener {
          Utils.showToast(context, "Failed")
          failure(it)
        }
  }

  fun fetchAllWords(
    success: (DataSnapshot) -> Unit,
    failure: (DatabaseError) -> Unit
  ) {
    wordRef.addValueEventListener(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            success(snapshot)
          }

          override fun onCancelled(error: DatabaseError) {
            failure(error)
          }
        }
    )
  }

  companion object {
    private const val TAG = "Repository"
  }
}
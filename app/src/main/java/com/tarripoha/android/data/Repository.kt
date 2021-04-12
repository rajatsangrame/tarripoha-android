package com.tarripoha.android.data

import android.content.Context
import android.os.Handler
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.data.db.WordDatabase
import com.tarripoha.android.data.rest.RetrofitApi
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
  private val commentRef: DatabaseReference by lazy { Firebase.database.getReference("comment") }

  /**
   * Check if the [DatabaseReference] is connected
   *
   * Mandatory to use after every firebase call to ensure the
   * network failure
   */
  private fun checkFirebaseConnection(
    connectionStatus: (Boolean) -> Unit
  ) {
    val fireBase = Firebase.database.getReference(".info/connected")
    fireBase.addValueEventListener(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            Handler().postDelayed({
              val connected = snapshot.getValue(Boolean::class.java)
              Log.d(TAG, "onDataChange: $connected")
              if (connected != null) connectionStatus(connected)
              else connectionStatus(false)
            }, 2000)
          }

          override fun onCancelled(error: DatabaseError) {
            connectionStatus(false)
          }
        }
    )
  }

  fun addNewWord(
    word: Word,
    success: () -> Unit,
    failure: (Exception) -> Unit,
    connectionStatus: (Boolean) -> Unit
  ) {

    wordRef.child(word.name)
        .setValue(word)
        .addOnSuccessListener {
          success()
        }
        .addOnFailureListener {
          failure(it)
        }

    checkFirebaseConnection(
        connectionStatus = {
          connectionStatus(it)
        }
    )
  }

  fun fetchAllWords(
    success: (DataSnapshot) -> Unit,
    failure: (DatabaseError) -> Unit,
    connectionStatus: (Boolean) -> Unit
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
    checkFirebaseConnection(
        connectionStatus = {
          connectionStatus(it)
        }
    )
  }

  fun searchWord(
    word: String,
    success: (DataSnapshot) -> Unit,
    failure: (DatabaseError) -> Unit,
    connectionStatus: (Boolean) -> Unit
  ) {
    Log.d(TAG, "searchWord: ${word.length}")
    val query = if (word.length > 2) {
      wordRef.orderByChild("name")
          .startAt(word)
          .endAt(word + "\uf8ff")
          .limitToFirst(LIMIT_TO_FIRST)
    } else {
      wordRef.orderByChild("name")
          .startAt(word)
          .limitToFirst(LIMIT_TO_FIRST)
    }
    query.addValueEventListener(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            success(snapshot)
          }

          override fun onCancelled(error: DatabaseError) {
            failure(error)
          }
        }
    )
    checkFirebaseConnection(
        connectionStatus = {
          connectionStatus(it)
        }
    )
  }

  fun fetchComments(
    word: String,
    success: (DataSnapshot) -> Unit,
    failure: (DatabaseError) -> Unit,
    connectionStatus: (Boolean) -> Unit
  ) {
    val query = commentRef.orderByChild("word")
        .startAt(word)
        .endAt(word + "\uf8ff")
        .limitToFirst(LIMIT_TO_FIRST)
    query.addValueEventListener(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            success(snapshot)
          }

          override fun onCancelled(error: DatabaseError) {
            failure(error)
          }
        }
    )
    checkFirebaseConnection(
        connectionStatus = {
          connectionStatus(it)
        }
    )
  }

  fun postComment(
    comment: Comment,
    success: () -> Unit,
    failure: (Exception) -> Unit,
    connectionStatus: (Boolean) -> Unit
  ) {

    commentRef.child(comment.id)
        .setValue(comment)
        .addOnSuccessListener {
          success()
        }
        .addOnFailureListener {
          failure(it)
        }

    checkFirebaseConnection(
        connectionStatus = {
          connectionStatus(it)
        }
    )
  }

  companion object {
    private const val LIMIT_TO_FIRST = 10
    private const val TAG = "Repository"
  }
}

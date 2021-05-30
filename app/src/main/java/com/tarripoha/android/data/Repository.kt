package com.tarripoha.android.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.data.db.WordDatabase
import com.tarripoha.android.data.model.User
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
    private val userRef: DatabaseReference by lazy { Firebase.database.getReference("user") }
    private val commentRef: CollectionReference by lazy {
        Firebase.firestore.collection("comment")
    }

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
                    Handler(Looper.getMainLooper()).postDelayed({
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

    fun postComment(
        comment: Comment,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        commentRef.document(comment.id)
            .set(comment)
            .addOnSuccessListener {
                success()
                Log.d(TAG, "DocumentSnapshot added with ID: ${comment.id}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.w(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun deleteComment(
        comment: Comment,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        commentRef.document(comment.id)
            .update("dirty", true)
            .addOnSuccessListener {
                success()
                Log.d(TAG, "DocumentSnapshot added with ID: ${comment.id}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.w(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun fetchUserInfo(
        phone: String,
        success: (DataSnapshot) -> Unit,
        failure: (DatabaseError) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        val query = userRef.child(phone)
        query.addListenerForSingleValueEvent(
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

    fun createUser(
        phone: String,
        user: User,
        success: () -> Unit,
        failure: () -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        userRef.child(phone)
            .setValue(user)
            .addOnSuccessListener {
                success()
            }
            .addOnFailureListener {
                failure()
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

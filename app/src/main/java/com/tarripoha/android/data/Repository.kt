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
import com.tarripoha.android.data.model.Comment
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.data.db.WordDatabase
import com.tarripoha.android.data.model.User
import com.tarripoha.android.data.rest.RetrofitApi
import com.tarripoha.android.firebase.PowerStone
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
    private val wordRef: DatabaseReference by lazy { PowerStone.getWordReference() }
    private val userRef: DatabaseReference by lazy { PowerStone.getUserReference() }
    private val commentRef: CollectionReference by lazy { PowerStone.getCommentReference() }

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
        wordRef.addListenerForSingleValueEvent(
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
                Log.d(TAG, "postComment: DocumentSnapshot added with ID: ${comment.id}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
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
                Log.d(TAG, "deleteComment: DocumentSnapshot added with ID: ${comment.id}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun likeComment(
        comment: Comment,
        like: Boolean,
        userId: String,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        commentRef.document(comment.id)
            .update(mapOf("likes.${userId}" to like))
            .addOnSuccessListener {
                success()
                Log.d(TAG, "likeComment: DocumentSnapshot added with ID: ${comment.id}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
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

    fun likeWord(
        word: Word,
        like: Boolean,
        userId: String,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        wordRef.child(word.name).child("likes").child(userId).setValue(like)
            .addOnSuccessListener {
                success()
                Log.d(TAG, "likeWord: DocumentSnapshot added with ID: ${word.name}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun saveWord(
        word: Word,
        saved: Boolean,
        userId: String,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        wordRef.child(word.name).child("saved").child(userId).setValue(saved)
            .addOnSuccessListener {
                success()
                Log.d(TAG, "saveWord: DocumentSnapshot added with ID: ${word.name}")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun updateViewsCount(
        word: Word,
        views: MutableList<Long?>,
        userId: String,
        success: () -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        wordRef.child(word.name).child("views").child(userId).setValue(views)
            .addOnSuccessListener {
                success()
                Log.d(TAG, "updateViewsCount: DocumentSnapshot added with ID: ${word.name}")
            }
            .addOnFailureListener { e ->
                failure(e)
                PowerStone.recordException(e)
                Log.e(TAG, "Error adding document", e)
            }

        checkFirebaseConnection(
            connectionStatus = {
                connectionStatus(it)
            }
        )
    }

    fun fetchWordDetail(
        word: String,
        success: (DataSnapshot) -> Unit,
        failure: (Exception) -> Unit,
        connectionStatus: (Boolean) -> Unit
    ) {
        wordRef.child(word).get()
            .addOnSuccessListener { snapshot ->
                success(snapshot)
                Log.d(TAG, "fetchWordDetail: $word")
            }
            .addOnFailureListener { e ->
                failure(e)
                Log.e(TAG, "Error adding document", e)
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

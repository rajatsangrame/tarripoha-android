package com.tarripoha.android.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequest.Builder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.tarripoha.android.R
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.firebase.PowerStone
import com.tarripoha.android.ui.word.WordDetailActivity
import java.lang.Exception
import java.util.*

/**
 * Worker thread to upload FCM token
 */
class WordNotificationWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams) {

    private var blockThread = true

    /**
     * This method is called on a background thread synchronously and do your work and return the
     * [Result] from this method.
     */
    override fun doWork(): Result {
        return uploadFcmToken()
    }

    private fun uploadFcmToken(): Result {
        var result = Result.success()
        val wordRef = PowerStone.getWordReference()
        wordRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    result = showNotification(snapshot)
                    blockThread = false
                }

                override fun onCancelled(error: DatabaseError) {
                    result = failureIfMaxLimitReached()
                    blockThread = false
                }
            }
        )

        while (blockThread) {
            Thread.sleep(50)
        }
        return result
    }

    private fun failureIfMaxLimitReached(): Result {

        return if (runAttemptCount > MAX_RETRY_LIMIT) {
            Log.e(TAG, "maximum retry limit reached")
            Result.failure()
        } else Result.retry()
    }

    private fun showNotification(
        snapshot: DataSnapshot,
    ): Result {
        val totalCount = snapshot.childrenCount
        val random = Random().nextInt(totalCount.toInt())
        var count = 0
        snapshot.children.forEach { snap ->
            try {
                while (count == random) {
                    val word: Word? = snap.getValue(Word::class.java)
                    showNotification(word)
                    return@forEach
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
            }
            count++
        }
        return Result.success()
    }

    private fun showNotification(word: Word?) {
        if (word == null) return
        val title = context.getString(R.string.notification_word_title, word.name)
        val desc = context.getString(R.string.notification_word_desc)

        val pendingIntent = WordDetailActivity.getPendingIntent(context, word.name)
        val notification =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_APP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notificationId = Random().nextInt(100)
        manager.notify(notificationId, notification)
    }

    companion object {
        private const val TAG = "WordPeriodic"
        private const val MAX_RETRY_LIMIT = 10
        private const val NOTIFICATION_CHANNEL_ID = "tp_app_channel"
        private const val NOTIFICATION_APP_CHANNEL_NAME = "Tarri Poha Notifications"

        /**
         * Creates one time upload work manager task for FCM token
         *
         * 1. Get FCM token
         * 2. On task complete
         * */
        @JvmStatic
        @WorkerThread
        fun processFcmTokenUpload(context: Context) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()
            val workRequest =
                Builder(WordNotificationWorker::class.java)
                    .setConstraints(constraints)
                    .addTag(TAG)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}

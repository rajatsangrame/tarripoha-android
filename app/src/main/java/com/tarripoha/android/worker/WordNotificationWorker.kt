package com.tarripoha.android.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import androidx.work.NetworkType.CONNECTED
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.tarripoha.android.R
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.firebase.PowerStone
import com.tarripoha.android.ui.word.WordDetailActivity
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Worker thread to show word notifications
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
        return fetchWordAndShowNotification()
    }

    private fun fetchWordAndShowNotification(): Result {
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

        Log.i(TAG, "fetchWordAndShowNotification: sleep started")
        while (blockThread) {
            Thread.sleep(50)
        }
        Log.i(TAG, "fetchWordAndShowNotification: return result")
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
                    val word: Word = snap.getValue(Word::class.java)
                        ?: return@showNotification failureIfMaxLimitReached()

                    val title = getNotificationTitle(word.name)
                    val desc = context.getString(R.string.notification_word_desc)
                    showNotification(word = word.name, title = title, desc = desc)
                    return@showNotification Result.success()
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAllResponse: ${e.localizedMessage}")
            }
            count++
        }
        return failureIfMaxLimitReached()
    }

    private fun getNotificationTitle(word: String): String {
        val number = Random().nextInt(3)
        Log.i(TAG, "getNotificationTitle: $number")
        return when (number) {
            1 -> {
                context.getString(R.string.notification_word_title_2, word)
            }
            2 -> {
                context.getString(R.string.notification_word_title_3, word)
            }
            else -> context.getString(R.string.notification_word_title, word)
        }
    }

    /**
     * Ref:
     * https://developer.android.com/codelabs/advanced-android-kotlin-training-notifications
     * https://github.com/zo0r/react-native-push-notification/issues/1326#issuecomment-616743061
     * https://material.io/design/iconography/product-icons.html#grid-and-keyline-shapes
     */
    private fun showNotification(word: String, title: String, desc: String) {

        val pendingIntent = WordDetailActivity.getPendingIntent(context, word)
        val builder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.color = ContextCompat.getColor(context, R.color.colorGreen)
        }
        val notification = builder.build()
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
        private const val TAG = "WordNotificationWorker"
        private const val MAX_RETRY_LIMIT = 10
        private const val NOTIFICATION_CHANNEL_ID = "tp_app_channel"
        private const val NOTIFICATION_APP_CHANNEL_NAME = "General Notifications"

        /**
         * Creates periodic task for Word notification
         * */
        @JvmStatic
        @WorkerThread
        fun processWork(context: Context) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequest.Builder(
                WordNotificationWorker::class.java,
                18, TimeUnit.HOURS,
                2, TimeUnit.HOURS
            ).setConstraints(constraints).addTag(TAG).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, workRequest)

        }
    }
}

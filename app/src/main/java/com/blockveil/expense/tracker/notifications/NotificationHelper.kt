package com.blockveil.expense.tracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blockveil.expense.tracker.MainActivity
import com.blockveil.expense.tracker.R

private const val CHANNEL_ID = "goal_loan_alerts"
private const val GOAL_NOTIFICATION_ID_BASE = 10_000
private const val LOAN_NOTIFICATION_ID_BASE = 20_000

fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(CHANNEL_ID, "Goals & Loans", NotificationManager.IMPORTANCE_DEFAULT).apply {
        description = "Alerts when a savings goal is reached or a loan is fully repaid"
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

/** Matches the exact wording requested: "Your (goal name) goal successfully reached. Tap to view your goal." */
fun showGoalReachedNotification(context: Context, goalId: Long, goalName: String) {
    showNotification(
        context = context,
        id = GOAL_NOTIFICATION_ID_BASE + (goalId % 10_000).toInt(),
        title = "Goal reached",
        text = "Your $goalName goal successfully reached. Tap to view your goal.",
    )
}

/** Matches the exact wording requested: "Your (loan name) loan successfully repaid. Tap to view your loan." */
fun showLoanRepaidNotification(context: Context, loanId: Long, loanName: String) {
    showNotification(
        context = context,
        id = LOAN_NOTIFICATION_ID_BASE + (loanId % 10_000).toInt(),
        title = "Loan repaid",
        text = "Your $loanName loan successfully repaid. Tap to view your loan.",
    )
}

private fun showNotification(context: Context, id: Int, title: String, text: String) {
    ensureNotificationChannel(context)
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        id,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    // The monochrome adaptive-icon layer (a flat white silhouette) is exactly what a
    // notification small icon needs to be, unlike the full-color foreground/background layers.
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_monochrome)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(id, notification)
    } catch (e: SecurityException) {
        // POST_NOTIFICATIONS not granted; the scheduled delete/hide still runs regardless,
        // this only skips the visual alert itself.
    }
}

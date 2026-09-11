package com.blockveil.expense.tracker.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/** A goal that just hit its target is deleted 24h later, matching the requested behavior exactly. */
fun scheduleGoalDeletion(context: Context, goalId: Long) {
    val request = OneTimeWorkRequestBuilder<DeleteGoalWorker>()
        .setInitialDelay(24, TimeUnit.HOURS)
        .setInputData(workDataOf(DeleteGoalWorker.KEY_GOAL_ID to goalId))
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork("delete_goal_$goalId", ExistingWorkPolicy.REPLACE, request)
}

/** A loan that's just been fully repaid is hidden 44h later, matching the requested behavior exactly. */
fun scheduleLoanHide(context: Context, accountId: Long) {
    val request = OneTimeWorkRequestBuilder<HideLoanWorker>()
        .setInitialDelay(44, TimeUnit.HOURS)
        .setInputData(workDataOf(HideLoanWorker.KEY_ACCOUNT_ID to accountId))
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork("hide_loan_$accountId", ExistingWorkPolicy.REPLACE, request)
}

package com.blockveil.expense.tracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blockveil.expense.tracker.ExpenseTrackerApp
import kotlinx.coroutines.flow.first

class HideLoanWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val accountId = inputData.getLong(KEY_ACCOUNT_ID, -1L)
        if (accountId == -1L) return Result.failure()
        val container = (applicationContext as ExpenseTrackerApp).container
        val account = container.accountRepository.observeAll().first().find { it.id == accountId }
        if (account != null && !account.isHidden) {
            container.accountRepository.setHidden(account, true)
        }
        return Result.success()
    }

    companion object {
        const val KEY_ACCOUNT_ID = "account_id"
    }
}

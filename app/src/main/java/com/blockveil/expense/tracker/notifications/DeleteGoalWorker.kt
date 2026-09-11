package com.blockveil.expense.tracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blockveil.expense.tracker.ExpenseTrackerApp
import kotlinx.coroutines.flow.first

class DeleteGoalWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val goalId = inputData.getLong(KEY_GOAL_ID, -1L)
        if (goalId == -1L) return Result.failure()
        val container = (applicationContext as ExpenseTrackerApp).container
        val goal = container.goalRepository.observeAll().first().find { it.id == goalId }
        if (goal != null) container.goalRepository.delete(goal)
        return Result.success()
    }

    companion object {
        const val KEY_GOAL_ID = "goal_id"
    }
}

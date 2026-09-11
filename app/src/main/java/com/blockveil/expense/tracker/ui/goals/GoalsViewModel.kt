package com.blockveil.expense.tracker.ui.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.datastore.SettingsRepository
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.repository.GoalRepository
import com.blockveil.expense.tracker.di.AppContainer
import com.blockveil.expense.tracker.notifications.scheduleGoalDeletion
import com.blockveil.expense.tracker.notifications.showGoalReachedNotification
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.resolveCurrencyDisplay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.min

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
    private val appContext: Context,
) : ViewModel() {

    val goals: StateFlow<List<GoalEntity>> = goalRepository.observeAll()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val currency: StateFlow<CurrencyDisplay> = settingsRepository.settings
        .map { resolveCurrencyDisplay(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = resolveCurrencyDisplay(AppSettings()),
        )

    /** Matches handleAddGoal exactly: starts at 0 saved. */
    fun onAddGoal(name: String, target: Double, months: Int) {
        viewModelScope.launch {
            goalRepository.insert(GoalEntity(name = name, targetAmount = target, savedAmount = 0.0, targetMonths = months))
        }
    }

    /**
     * Matches handleContributeGoal exactly: caps at the target, never overshoots. A
     * contribution that newly crosses the target (it wasn't already there before this one)
     * fires the goal-reached notification and schedules the goal's 24h auto-delete.
     */
    fun onContribute(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            val newlyReached = goal.savedAmount < goal.targetAmount && goal.savedAmount + amount >= goal.targetAmount
            goalRepository.update(goal.copy(savedAmount = min(goal.savedAmount + amount, goal.targetAmount)))
            if (newlyReached) {
                showGoalReachedNotification(appContext, goal.id, goal.name)
                scheduleGoalDeletion(appContext, goal.id)
            }
        }
    }

    fun onDelete(goal: GoalEntity) {
        viewModelScope.launch { goalRepository.delete(goal) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GoalsViewModel(
                    goalRepository = container.goalRepository,
                    settingsRepository = container.settingsRepository,
                    appContext = container.appContext,
                )
            }
        }
    }
}

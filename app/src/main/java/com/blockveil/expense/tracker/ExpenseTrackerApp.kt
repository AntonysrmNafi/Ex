package com.blockveil.expense.tracker

import android.app.Application
import com.blockveil.expense.tracker.di.AppContainer

class ExpenseTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

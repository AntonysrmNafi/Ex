package com.blockveil.expensetracker

import android.app.Application
import com.blockveil.expensetracker.di.AppContainer

class ExpenseTrackerApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

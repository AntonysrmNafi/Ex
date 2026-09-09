package com.blockveil.expense.tracker.ui.more

import com.blockveil.expense.tracker.data.model.SubscriptionCycle

/** "Day", "3 Day", "Weekly", "Monthly", "Yearly", "One time", matches SUBSCRIPTION_CYCLES exactly. */
val SubscriptionCycle.label: String
    get() = when (this) {
        SubscriptionCycle.DAY -> "Day"
        SubscriptionCycle.THREE_DAY -> "3 Day"
        SubscriptionCycle.WEEKLY -> "Weekly"
        SubscriptionCycle.MONTHLY -> "Monthly"
        SubscriptionCycle.YEARLY -> "Yearly"
        SubscriptionCycle.ONE_TIME -> "One time"
    }

/** Whether this cycle needs an explicit start/end date range. Matches CYCLES_WITH_DATE_RANGE (Day and One time don't). */
val SubscriptionCycle.needsDateRange: Boolean
    get() = this in setOf(SubscriptionCycle.THREE_DAY, SubscriptionCycle.WEEKLY, SubscriptionCycle.MONTHLY, SubscriptionCycle.YEARLY)

/** Minimum days between start and end date for cycles that need a range. Matches MIN_DURATION_DAYS exactly. */
val SubscriptionCycle.minDurationDays: Int
    get() = when (this) {
        SubscriptionCycle.THREE_DAY -> 3
        SubscriptionCycle.WEEKLY -> 28
        SubscriptionCycle.MONTHLY -> 28
        SubscriptionCycle.YEARLY -> 360
        SubscriptionCycle.DAY, SubscriptionCycle.ONE_TIME -> 0
    }

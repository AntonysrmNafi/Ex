package com.blockveil.expense.tracker.ui.settings

import com.blockveil.expense.tracker.data.model.ThemeMode

/** "System", "Light", "Dark", matches the source design's capitalized mode buttons. */
val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

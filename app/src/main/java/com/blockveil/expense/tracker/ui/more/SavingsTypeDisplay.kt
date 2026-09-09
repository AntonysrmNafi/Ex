package com.blockveil.expense.tracker.ui.more

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import com.blockveil.expense.tracker.data.model.SavingsType

/** "Bank", "Mobile Wallet", "Cash", "Investment", matches SAVINGS_TYPES' labels exactly. */
val SavingsType.label: String
    get() = when (this) {
        SavingsType.BANK -> "Bank"
        SavingsType.MOBILE_WALLET -> "Mobile Wallet"
        SavingsType.CASH -> "Cash"
        SavingsType.INVESTMENT -> "Investment"
    }

/** Landmark/Wallet/Coins/TrendingUp in the source design, closest Material equivalents here. */
val SavingsType.icon: ImageVector
    get() = when (this) {
        SavingsType.BANK -> Icons.Filled.AccountBalance
        SavingsType.MOBILE_WALLET -> Icons.Filled.Wallet
        SavingsType.CASH -> Icons.Filled.AttachMoney
        SavingsType.INVESTMENT -> Icons.Filled.TrendingUp
    }

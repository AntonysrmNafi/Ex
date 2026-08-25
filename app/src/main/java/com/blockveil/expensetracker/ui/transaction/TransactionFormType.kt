package com.blockveil.expensetracker.ui.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.blockveil.expensetracker.ui.theme.BrandAccent
import com.blockveil.expensetracker.ui.theme.BrandDanger
import com.blockveil.expensetracker.ui.theme.BrandPrimary
import com.blockveil.expensetracker.ui.theme.BrandRepay

/** The 4 kinds of entry the Transaction page can create. Matches TRANSACTION_TYPES exactly. */
enum class TransactionFormType(val label: String, val color: Color, val icon: ImageVector) {
    INCOME("Income", BrandPrimary, Icons.Filled.ArrowCircleDown),
    EXPENSE("Expense", BrandDanger, Icons.Filled.ArrowCircleUp),
    TRANSFER("Transfer", BrandAccent, Icons.Filled.SwapHoriz),
    REPAY("Repay Loan", BrandRepay, Icons.Filled.Receipt),
}

package com.blockveil.expense.tracker.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.util.accountName
import com.blockveil.expense.tracker.util.formatDateDisplay

/**
 * Everything [TransactionRow] needs for one row. Shared by Home's recent activity, History,
 * Category History, and Account History, all four screens list transactions this same way.
 */
data class TransactionRowUiModel(
    val id: Long,
    val category: String,
    val isIncome: Boolean,
    val icon: ImageVector,
    val iconTint: Color,
    val note: String,
    val metaLine: String,
    val amount: Double,
    val hasReceipt: Boolean,
)

/**
 * Builds a [TransactionRowUiModel] from a raw entity. One shared mapper instead of four
 * near-identical copies across Home/History/Category History/Account History.
 */
fun TransactionEntity.toRowUiModel(
    accounts: List<AccountEntity>,
    customExpenseCategories: List<CustomCategoryEntity>,
    customIncomeCategories: List<CustomCategoryEntity>,
): TransactionRowUiModel {
    val isIncome = type == TransactionType.INCOME
    val metaLine = listOfNotNull(
        formatDateDisplay(date),
        accountName(accounts, accountId),
        location.takeIf { it.isNotBlank() },
    ).joinToString(" · ")

    return TransactionRowUiModel(
        id = id,
        category = category,
        isIncome = isIncome,
        icon = categoryIcon(isIncome, category),
        iconTint = categoryColor(isIncome, category, customExpenseCategories, customIncomeCategories),
        note = note,
        metaLine = metaLine,
        amount = amount,
        hasReceipt = receiptPhotoPath != null,
    )
}

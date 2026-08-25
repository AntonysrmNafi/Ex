package com.blockveil.expensetracker.ui.transaction

import android.net.Uri
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.util.CurrencyDisplay
import com.blockveil.expensetracker.util.EXPENSE_CATEGORIES
import com.blockveil.expensetracker.util.INCOME_CATEGORIES
import java.time.LocalDate

/** Everything the form needs from the repositories, refreshed reactively as they change. */
data class TransactionFormSources(
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val currency: CurrencyDisplay,
)

/**
 * Everything the user has explicitly typed or picked. [category] and [accountId] are left
 * empty/null until the user actually chooses something; [effectiveCategory]/[effectiveAccountId]
 * below fill in a sensible default on the fly, mirroring the source design's default-selection
 * behavior without needing a separate effect to keep them in sync.
 */
data class TransactionFormFields(
    val type: TransactionFormType = TransactionFormType.EXPENSE,
    val amount: String = "",
    val category: String = "",
    val accountId: Long? = null,
    val location: String = "",
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val photoUri: Uri? = null,
    val errorMessage: String? = null,
)

/** The category list for [type]: fixed categories first, then this session's custom ones. */
fun categoryListFor(type: TransactionFormType, sources: TransactionFormSources): List<String> =
    if (type == TransactionFormType.INCOME) {
        INCOME_CATEGORIES + sources.customIncomeCategories.map { it.name }
    } else {
        EXPENSE_CATEGORIES + sources.customExpenseCategories.map { it.name }
    }

/** The category to actually show as selected: the user's pick if still valid, else the first option. */
fun effectiveCategory(fields: TransactionFormFields, sources: TransactionFormSources): String {
    val list = categoryListFor(fields.type, sources)
    return fields.category.takeIf { it.isNotEmpty() && list.contains(it) } ?: list.firstOrNull().orEmpty()
}

/** The account to actually show as selected: the user's pick if it's still a savings account, else the first one. */
fun effectiveAccountId(fields: TransactionFormFields, sources: TransactionFormSources): Long? {
    val savingsAccounts = sources.accounts.filter { it.category == AccountCategory.SAVINGS }
    val current = fields.accountId?.takeIf { id -> savingsAccounts.any { it.id == id } }
    return current ?: savingsAccounts.firstOrNull()?.id
}

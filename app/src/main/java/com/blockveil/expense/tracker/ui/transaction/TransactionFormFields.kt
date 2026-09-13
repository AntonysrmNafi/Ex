package com.blockveil.expense.tracker.ui.transaction

import android.net.Uri
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.EXPENSE_CATEGORIES
import com.blockveil.expense.tracker.util.INCOME_CATEGORIES
import java.time.LocalDate

/** Everything the form needs from the repositories, refreshed reactively as they change. */
data class TransactionFormSources(
    val accounts: List<AccountEntity>,
    val customExpenseCategories: List<CustomCategoryEntity>,
    val customIncomeCategories: List<CustomCategoryEntity>,
    val currency: CurrencyDisplay,
    val hiddenFixedExpenseCategories: Set<String> = emptySet(),
    val hiddenFixedIncomeCategories: Set<String> = emptySet(),
    val deletedFixedExpenseCategories: Set<String> = emptySet(),
    val deletedFixedIncomeCategories: Set<String> = emptySet(),
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
    val fromId: Long? = null,
    val toId: Long? = null,
    val loanId: Long? = null,
)

/**
 * The category list for [type], for the "pick or add new" picker: fixed categories (minus
 * any the user has hidden or deleted) first, then this session's custom ones (minus any
 * hidden). [currentCategory] (the transaction's existing category when editing) is always
 * included even if it's since been hidden/deleted, so opening an existing transaction for
 * edit never silently reassigns it to a different category just because its own was removed
 * from the picker.
 */
fun categoryListFor(type: TransactionFormType, sources: TransactionFormSources, currentCategory: String = ""): List<String> {
    val list = if (type == TransactionFormType.INCOME) {
        val excluded = sources.hiddenFixedIncomeCategories + sources.deletedFixedIncomeCategories
        INCOME_CATEGORIES.filterNot { it in excluded } +
            sources.customIncomeCategories.filterNot { it.isHidden }.map { it.name }
    } else {
        val excluded = sources.hiddenFixedExpenseCategories + sources.deletedFixedExpenseCategories
        EXPENSE_CATEGORIES.filterNot { it in excluded } +
            sources.customExpenseCategories.filterNot { it.isHidden }.map { it.name }
    }
    return if (currentCategory.isNotEmpty() && currentCategory !in list) list + currentCategory else list
}

/** The category to actually show as selected: the user's pick if still valid, else the first option. */
fun effectiveCategory(fields: TransactionFormFields, sources: TransactionFormSources): String {
    val list = categoryListFor(fields.type, sources, currentCategory = fields.category)
    return fields.category.takeIf { it.isNotEmpty() && list.contains(it) } ?: list.firstOrNull().orEmpty()
}

/** The account to actually show as selected: the user's pick if it's still a savings account, else the first one. */
fun effectiveAccountId(fields: TransactionFormFields, sources: TransactionFormSources): Long? {
    val savingsAccounts = sources.accounts.filter { it.category == AccountCategory.SAVINGS }
    val current = fields.accountId?.takeIf { id -> savingsAccounts.any { it.id == id } }
    return current ?: savingsAccounts.firstOrNull()?.id
}

/** Loans still owing money and not manually deactivated. Matches the `loans` derivation in TransactionPage. */
fun activeLoans(sources: TransactionFormSources): List<AccountEntity> =
    sources.accounts.filter {
        it.category == AccountCategory.LOAN && it.active && (it.principal ?: 0.0) - (it.repaid ?: 0.0) > 0
    }

/** The "From" account for a transfer: the user's pick if still valid, else the first savings account. */
fun effectiveFromId(fields: TransactionFormFields, sources: TransactionFormSources): Long? {
    val savingsAccounts = sources.accounts.filter { it.category == AccountCategory.SAVINGS }
    val current = fields.fromId?.takeIf { id -> savingsAccounts.any { it.id == id } }
    return current ?: savingsAccounts.firstOrNull()?.id
}

/** The "To" account for a transfer: the user's pick if valid and not the same as "From", else the first other savings account. */
fun effectiveToId(fields: TransactionFormFields, sources: TransactionFormSources, fromId: Long?): Long? {
    val candidates = sources.accounts.filter { it.category == AccountCategory.SAVINGS && it.id != fromId }
    val current = fields.toId?.takeIf { id -> id != fromId && candidates.any { it.id == id } }
    return current ?: candidates.firstOrNull()?.id
}

/** The loan being repaid: the user's pick if still active, else the first active loan. */
fun effectiveLoanId(fields: TransactionFormFields, sources: TransactionFormSources): Long? {
    val loans = activeLoans(sources)
    val current = fields.loanId?.takeIf { id -> loans.any { it.id == id } }
    return current ?: loans.firstOrNull()?.id
}

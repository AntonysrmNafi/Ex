package com.blockveil.expense.tracker.data.backup

import android.graphics.Color as AndroidColor
import com.blockveil.expense.tracker.data.datastore.AppSettings
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.CategoryBudgetEntity
import com.blockveil.expense.tracker.data.local.entity.CustomCategoryEntity
import com.blockveil.expense.tracker.data.local.entity.GoalEntity
import com.blockveil.expense.tracker.data.local.entity.SubscriptionEntity
import com.blockveil.expense.tracker.data.local.entity.TransactionEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.CurrencyFormat
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.SavingsType
import com.blockveil.expense.tracker.data.model.SubscriptionCycle
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.data.model.TransactionType
import com.blockveil.expense.tracker.data.model.TransferKind
import java.time.LocalDate

/**
 * Turns [CsvBackup.parse]'s raw string-keyed rows into a typed [BackupSnapshot], ready to
 * write into Room (existing ids are preserved, so a transaction's accountId keeps pointing
 * at the right restored account). Returns null if the file is missing the two sections that
 * make it recognizable as a real backup, matching handleRestoreFile's
 * `if (!sections.ACCOUNTS || !sections.TRANSACTIONS)` check exactly.
 *
 * Every field read defensively falls back to a safe default rather than throwing, since this
 * parses a file the user picked, not data this app wrote in the same session.
 */
fun parseBackupSections(sections: Map<String, List<Map<String, String>>>): BackupSnapshot? {
    val accountRows = sections["ACCOUNTS"] ?: return null
    val transactionRows = sections["TRANSACTIONS"] ?: return null

    return BackupSnapshot(
        settings = parseSettingsRow(sections["SETTINGS"]?.firstOrNull()),
        accounts = accountRows.map(::parseAccountRow),
        transactions = transactionRows.mapNotNull(::parseTransactionRowOrNull),
        subscriptions = (sections["SUBSCRIPTIONS"] ?: emptyList()).mapNotNull(::parseSubscriptionRowOrNull),
        goals = (sections["GOALS"] ?: emptyList()).mapNotNull(::parseGoalRowOrNull),
        transfers = (sections["TRANSFERS"] ?: emptyList()).mapNotNull(::parseTransferRowOrNull),
        categoryBudgets = (sections["CATEGORY_BUDGETS"] ?: emptyList()).mapNotNull(::parseCategoryBudgetRowOrNull),
        customExpenseCategories = (sections["CUSTOM_EXPENSE_CATEGORIES"] ?: emptyList()).map { parseCustomCategoryRow(it, isIncome = false) },
        customIncomeCategories = (sections["CUSTOM_INCOME_CATEGORIES"] ?: emptyList()).map { parseCustomCategoryRow(it, isIncome = true) },
    )
}

private fun parseAccountRow(r: Map<String, String>): AccountEntity {
    val id = r["id"]?.toLongOrNull() ?: 0L
    val name = r["name"].orEmpty()
    val category = runCatching { AccountCategory.valueOf(r["category"].orEmpty()) }.getOrDefault(AccountCategory.SAVINGS)
    return if (category == AccountCategory.LOAN) {
        val principal = r["principal"]?.toDoubleOrNull() ?: 0.0
        AccountEntity(
            id = id,
            name = name,
            category = AccountCategory.LOAN,
            principal = principal,
            loanAmount = r["loanAmount"]?.toDoubleOrNull() ?: principal,
            repaid = r["repaid"]?.toDoubleOrNull() ?: 0.0,
            active = r["active"] != "false",
        )
    } else {
        AccountEntity(
            id = id,
            name = name,
            category = AccountCategory.SAVINGS,
            type = r["type"]?.let { t -> runCatching { SavingsType.valueOf(t) }.getOrNull() },
            balance = r["balance"]?.toDoubleOrNull() ?: 0.0,
        )
    }
}

private fun parseTransactionRowOrNull(r: Map<String, String>): TransactionEntity? {
    val type = r["type"]?.let { t -> runCatching { TransactionType.valueOf(t) }.getOrNull() } ?: return null
    val date = r["date"]?.let(::parseLocalDateOrNull) ?: return null
    return TransactionEntity(
        id = r["id"]?.toLongOrNull() ?: 0L,
        type = type,
        amount = r["amount"]?.toDoubleOrNull() ?: 0.0,
        category = r["category"].orEmpty(),
        note = r["note"].orEmpty(),
        date = date,
        accountId = r["accountId"]?.toLongOrNull(),
        location = r["location"].orEmpty(),
        // The photo file itself was never in the backup, only whether one existed.
        receiptPhotoPath = null,
    )
}

private fun parseSubscriptionRowOrNull(r: Map<String, String>): SubscriptionEntity? {
    val cycle = r["cycle"]?.let { c -> runCatching { SubscriptionCycle.valueOf(c) }.getOrNull() } ?: return null
    return SubscriptionEntity(
        id = r["id"]?.toLongOrNull() ?: 0L,
        name = r["name"].orEmpty(),
        category = r["category"].orEmpty(),
        amount = r["amount"]?.toDoubleOrNull() ?: 0.0,
        cycle = cycle,
        accountId = r["accountId"]?.toLongOrNull(),
        startDate = r["startDate"]?.let(::parseLocalDateOrNull),
        endDate = r["endDate"]?.let(::parseLocalDateOrNull),
        active = r["active"] == "true",
        billedDates = r["billedDates"].orEmpty().split(";").mapNotNull { it.takeIf(String::isNotBlank)?.let(::parseLocalDateOrNull) },
    )
}

private fun parseGoalRowOrNull(r: Map<String, String>): GoalEntity? {
    val targetAmount = r["targetAmount"]?.toDoubleOrNull() ?: return null
    return GoalEntity(
        id = r["id"]?.toLongOrNull() ?: 0L,
        name = r["name"].orEmpty(),
        targetAmount = targetAmount,
        savedAmount = r["savedAmount"]?.toDoubleOrNull() ?: 0.0,
        targetMonths = r["targetMonths"]?.toIntOrNull() ?: 1,
    )
}

private fun parseTransferRowOrNull(r: Map<String, String>): TransferEntity? {
    val kind = r["kind"]?.let { k -> runCatching { TransferKind.valueOf(k) }.getOrNull() } ?: return null
    val date = r["date"]?.let(::parseLocalDateOrNull) ?: return null
    return TransferEntity(
        id = r["id"]?.toLongOrNull() ?: 0L,
        kind = kind,
        fromId = r["fromId"]?.toLongOrNull(),
        toId = r["toId"]?.toLongOrNull(),
        amount = r["amount"]?.toDoubleOrNull() ?: 0.0,
        note = r["note"].orEmpty(),
        date = date,
    )
}

private fun parseCategoryBudgetRowOrNull(r: Map<String, String>): CategoryBudgetEntity? {
    val category = r["category"]?.takeIf(String::isNotBlank) ?: return null
    return CategoryBudgetEntity(category = category, limitAmount = r["limit"]?.toDoubleOrNull() ?: 0.0)
}

private fun parseCustomCategoryRow(r: Map<String, String>, isIncome: Boolean): CustomCategoryEntity =
    CustomCategoryEntity(
        name = r["name"].orEmpty(),
        color = r["color"]?.let(::parseHexColorOrNull) ?: DEFAULT_CUSTOM_CATEGORY_COLOR,
        isIncome = isIncome,
    )

private fun parseSettingsRow(r: Map<String, String>?): AppSettings {
    if (r == null) return AppSettings()
    return AppSettings(
        budget = r["budget"]?.toDoubleOrNull() ?: 0.0,
        rollingEnabled = r["rollingEnabled"] == "true",
        themeMode = r["themeMode"]?.let { m -> runCatching { ThemeMode.valueOf(m) }.getOrNull() } ?: ThemeMode.SYSTEM,
        currencyCountry = r["currencyCountry"]?.takeIf(String::isNotBlank) ?: "Bangladesh",
        currencyPosition = r["currencyPosition"]?.let { p -> runCatching { CurrencyPosition.valueOf(p) }.getOrNull() } ?: CurrencyPosition.PREFIX,
        currencyFormat = r["currencyFormat"]?.let { f -> runCatching { CurrencyFormat.valueOf(f) }.getOrNull() } ?: CurrencyFormat.GROUPED,
    )
}

private const val DEFAULT_CUSTOM_CATEGORY_COLOR = 0xFF616161.toInt()

private fun parseHexColorOrNull(hex: String): Int? = runCatching { AndroidColor.parseColor(hex) }.getOrNull()

private fun parseLocalDateOrNull(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()

package com.blockveil.expensetracker.data.model

/** An account is either a savings-type account or a loan (matches ACCOUNTS.category in the source design). */
enum class AccountCategory {
    SAVINGS,
    LOAN,
}

/** Sub-type of a SAVINGS account. Loans have no type (null). Matches SAVINGS_TYPES. */
enum class SavingsType {
    BANK,
    MOBILE_WALLET,
    CASH,
    INVESTMENT,
}

/** A transaction is either money coming in or going out. Transfers and repayments are separate. */
enum class TransactionType {
    INCOME,
    EXPENSE,
}

/** Billing cycle for a recurring subscription. Matches SUBSCRIPTION_CYCLES. */
enum class SubscriptionCycle {
    DAY,
    THREE_DAY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ONE_TIME,
}

/** What a transfer record represents. Matches the "kind" values used in the source design. */
enum class TransferKind {
    TRANSFER,
    REPAYMENT,
    LOAN_DISBURSEMENT,
}

/** User's chosen app appearance. Matches themeMode in SETTINGS. */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

/** Where the currency symbol is placed relative to the amount. Matches currencyPosition. */
enum class CurrencyPosition {
    PREFIX,
    SUFFIX,
}

/** Whether amounts show thousands separators. Matches currencyFormat ("format" vs plain). */
enum class CurrencyFormat {
    GROUPED,
    PLAIN,
}

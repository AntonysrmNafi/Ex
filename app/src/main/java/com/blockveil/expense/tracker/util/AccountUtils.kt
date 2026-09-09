package com.blockveil.expense.tracker.util

import com.blockveil.expense.tracker.data.local.entity.AccountEntity

/**
 * Resolves an account's display name from its id, or "Deleted account" if it no longer
 * exists (or [id] is null, e.g. a transaction whose account was removed). Matches
 * getAccountName in the source design.
 */
fun accountName(accounts: List<AccountEntity>, id: Long?): String =
    accounts.firstOrNull { it.id == id }?.name ?: "Deleted account"

package com.blockveil.expense.tracker.util

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import kotlin.math.abs

/**
 * Resolves an account's display name from its id, or "Deleted account" if it no longer
 * exists (or [id] is null, e.g. a transaction whose account was removed). Matches
 * getAccountName in the source design.
 */
fun accountName(accounts: List<AccountEntity>, id: Long?): String =
    accounts.firstOrNull { it.id == id }?.name ?: "Deleted account"

/** Tolerant of floating-point drift after many Double additions/subtractions on [AccountEntity.balance]
 *  (e.g. landing on 1e-13 instead of an exact 0.0). */
private const val ZERO_BALANCE_TOLERANCE = 0.005

/**
 * A hidden account still belongs in read-only lists (Home's account strip, More's account
 * list) as long as it holds a real balance; it only drops out of those once emptied to zero.
 * This does NOT apply to pickers for new transactions/transfers, which always exclude hidden
 * accounts regardless of balance -- see [com.blockveil.expense.tracker.data.repository.AccountRepository.observeVisible].
 */
fun AccountEntity.isDisplayableWhileHidden(): Boolean =
    !isHidden || abs(balance ?: 0.0) >= ZERO_BALANCE_TOLERANCE

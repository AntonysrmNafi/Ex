package com.blockveil.expense.tracker.ui.components

import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.model.TransferKind
import com.blockveil.expense.tracker.util.accountName
import com.blockveil.expense.tracker.util.formatDateDisplay

/** Everything [TransferRow] needs for one row. Shared by History and Account History. */
data class TransferRowUiModel(
    val id: Long,
    val label: String,
    val fromToLine: String,
    val dateLine: String,
    val amount: Double,
)

/** Builds a [TransferRowUiModel] from a raw entity. One shared mapper for History and Account History. */
fun TransferEntity.toRowUiModel(accounts: List<AccountEntity>): TransferRowUiModel {
    val label = when (kind) {
        TransferKind.REPAYMENT -> "Loan repayment"
        TransferKind.LOAN_DISBURSEMENT -> "Loan received"
        TransferKind.TRANSFER -> "Transfer"
    }
    val fromName = accountName(accounts, fromId)
    val toName = accountName(accounts, toId)
    val fromToLine = "$fromName → $toName" + (note.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")

    return TransferRowUiModel(
        id = id,
        label = label,
        fromToLine = fromToLine,
        dateLine = formatDateDisplay(date),
        amount = amount,
    )
}

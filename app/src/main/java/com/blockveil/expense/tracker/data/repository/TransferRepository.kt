package com.blockveil.expense.tracker.data.repository

import androidx.room.withTransaction
import com.blockveil.expense.tracker.data.local.AppDatabase
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.local.entity.TransferEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.TransferKind
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TransferRepository(private val db: AppDatabase) {

    private val transferDao = db.transferDao()
    private val accountDao = db.accountDao()

    fun observeAll(): Flow<List<TransferEntity>> = transferDao.observeAll()

    /**
     * Moves money from one savings account to another and records the transfer, all in one
     * DB transaction. Matches handleAddTransfer exactly (fromId.balance -= amount,
     * toId.balance += amount).
     */
    suspend fun addTransfer(fromId: Long, toId: Long, amount: Double, note: String, date: LocalDate): Long =
        db.withTransaction {
            adjustBalance(fromId, -amount)
            adjustBalance(toId, amount)
            transferDao.insert(
                TransferEntity(kind = TransferKind.TRANSFER, fromId = fromId, toId = toId, amount = amount, note = note, date = date),
            )
        }

    /**
     * Pays down a loan from a savings account and records the repayment, all in one DB
     * transaction. Matches handleRepayLoan exactly, including auto-settling the loan
     * (active = false) once it's fully repaid.
     */
    suspend fun addRepayment(fromId: Long, loanId: Long, amount: Double, date: LocalDate): Long =
        db.withTransaction {
            adjustBalance(fromId, -amount)

            val loan = accountDao.getById(loanId)
            if (loan != null) {
                val newRepaid = (loan.repaid ?: 0.0) + amount
                accountDao.update(loan.copy(repaid = newRepaid, active = newRepaid < (loan.principal ?: 0.0)))
            }

            transferDao.insert(
                TransferEntity(kind = TransferKind.REPAYMENT, fromId = fromId, toId = loanId, amount = amount, note = "", date = date),
            )
        }

    /** Whether repaying [amount] on this loan would fully settle it, used to pick the right confirmation copy later. */
    fun wouldSettleLoan(loan: AccountEntity, amount: Double): Boolean =
        (loan.repaid ?: 0.0) + amount >= (loan.principal ?: 0.0)

    /**
     * Creates a new loan account and, if a deposit account is chosen, adds the loan amount
     * to its balance and records the disbursement transfer. Matches handleAddAccount's LOAN
     * branch exactly, including that a missing deposit account still creates the loan (just
     * with no money moved), matching the source design's `if (!payload.depositAccountId) return withLoan`.
     */
    suspend fun addLoanAccount(
        name: String,
        loanAmount: Double,
        settlementAmount: Double,
        depositAccountId: Long?,
        date: LocalDate,
    ): Long = db.withTransaction {
        val loanId = accountDao.insert(
            AccountEntity(
                name = name,
                category = AccountCategory.LOAN,
                principal = settlementAmount,
                loanAmount = loanAmount,
                repaid = 0.0,
                active = true,
            ),
        )
        if (depositAccountId != null) {
            adjustBalance(depositAccountId, loanAmount)
            transferDao.insert(
                TransferEntity(
                    kind = TransferKind.LOAN_DISBURSEMENT,
                    fromId = loanId,
                    toId = depositAccountId,
                    amount = loanAmount,
                    note = "Loan received",
                    date = date,
                ),
            )
        }
        loanId
    }

    private suspend fun adjustBalance(accountId: Long, delta: Double) {
        val account = accountDao.getById(accountId) ?: return
        accountDao.update(account.copy(balance = (account.balance ?: 0.0) + delta))
    }

    suspend fun delete(transfer: TransferEntity) = transferDao.delete(transfer)

    suspend fun deleteAll() = transferDao.deleteAll()
}

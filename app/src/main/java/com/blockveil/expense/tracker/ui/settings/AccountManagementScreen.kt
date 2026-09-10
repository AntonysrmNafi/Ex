package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.PageHeader
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.more.icon
import com.blockveil.expense.tracker.ui.more.label
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * Lists every account, savings and loan together, with a delete button. Deleting one is safe:
 * every transaction/subscription/transfer pointing at it falls back to "Deleted account"
 * (SET_NULL foreign keys) instead of failing or taking the rest of that history with it.
 * Renaming/editing isn't here yet, just view + delete, for now.
 */
@Composable
fun AccountManagementScreen(
    accounts: List<AccountEntity>,
    currency: CurrencyDisplay,
    onDelete: (AccountEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageHeader(title = "Account Management", onClose = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Accounts", modifier = Modifier.padding(top = 4.dp))
            if (accounts.isEmpty()) {
                Text(
                    text = "No accounts yet, add one from the More tab.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                    accounts.forEach { account ->
                        AccountManagementRow(
                            account = account,
                            currency = currency,
                            onDeleteRequest = { pendingDelete = account },
                        )
                    }
                }
            }
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        ConfirmDialog(
            title = "Delete account?",
            message = "\"${toDelete.name}\" will be removed. Past transactions on it will show as \"Deleted account\" instead of disappearing.",
            onConfirm = {
                onDelete(toDelete)
                pendingDelete = null
            },
            onCancel = { pendingDelete = null },
        )
    }
}

@Composable
private fun AccountManagementRow(
    account: AccountEntity,
    currency: CurrencyDisplay,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = if (account.category == AccountCategory.SAVINGS) {
        "${account.type?.label ?: "Savings"} · ${formatMoney(account.balance ?: 0.0, currency)}"
    } else {
        "Loan · ${formatMoney(account.repaid ?: 0.0, currency)} of ${formatMoney(account.principal ?: 0.0, currency)} repaid"
    }
    val icon = if (account.category == AccountCategory.SAVINGS) account.type?.icon ?: Icons.Filled.CreditCard else Icons.Filled.CreditCard

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Column {
                    Text(
                        text = account.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete ${account.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp).clickable { onDeleteRequest() },
            )
        }
    }
}

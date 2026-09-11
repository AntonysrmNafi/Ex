package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.more.icon
import com.blockveil.expense.tracker.ui.more.label
import com.blockveil.expense.tracker.util.CurrencyDisplay
import com.blockveil.expense.tracker.util.formatMoney

/**
 * Lists every account, savings and loan together. Tapping the kebab menu on a row offers
 * Hide/Unhide (a hidden account still counts toward net worth but disappears from every list
 * and transaction account picker until unhidden) and Delete Account (blocked for a loan that
 * isn't fully repaid yet, see onDeleteAccount's error return).
 */
@Composable
fun AccountManagementScreen(
    accounts: List<AccountEntity>,
    currency: CurrencyDisplay,
    onDelete: (AccountEntity) -> String?,
    onSetHidden: (AccountEntity, Boolean) -> Unit,
    onDeleteBlocked: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<AccountEntity?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackHeader(title = "Account Management", onBack = onBack)

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
                            onSetHidden = { hidden -> onSetHidden(account, hidden) },
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
                val error = onDelete(toDelete)
                if (error != null) onDeleteBlocked(error)
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
    onSetHidden: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    val subtitle = if (account.category == AccountCategory.SAVINGS) {
        "${account.type?.label ?: "Savings"} \u00b7 ${formatMoney(account.balance ?: 0.0, currency)}"
    } else {
        "Loan \u00b7 ${formatMoney(account.repaid ?: 0.0, currency)} of ${formatMoney(account.principal ?: 0.0, currency)} repaid"
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (account.isHidden) {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = "Hidden",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 6.dp).size(12.dp),
                            )
                        }
                    }
                    Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options for ${account.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).clickable { menuOpen = true },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(if (account.isHidden) "Unhide" else "Hide") },
                        onClick = {
                            onSetHidden(!account.isHidden)
                            menuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Account") },
                        onClick = {
                            onDeleteRequest()
                            menuOpen = false
                        },
                    )
                }
            }
        }
    }
}

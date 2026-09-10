package com.blockveil.expense.tracker.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.local.entity.AccountEntity
import com.blockveil.expense.tracker.data.model.AccountCategory
import com.blockveil.expense.tracker.data.model.SavingsType
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.ChipRow
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary

/**
 * Add-account card. A Savings/Loan category toggle, matching the source design's full
 * 2-button switch, both flows fully wired: Savings (name, type, starting balance) and Loan
 * (name, loan amount, settlement amount, deposit-to account). Matches the add-account Card
 * in MoreScreen and handleAddAccount exactly.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountCard(
    savingsAccounts: List<AccountEntity>,
    onAddSavingsAccount: (name: String, type: SavingsType, balance: Double) -> Unit,
    onAddLoanAccount: (name: String, loanAmount: String, settlementAmount: String, depositAccountId: Long?) -> String?,
    modifier: Modifier = Modifier,
) {
    var category by remember { mutableStateOf(AccountCategory.SAVINGS) }

    // Savings fields
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(SavingsType.BANK) }
    var balance by remember { mutableStateOf("") }

    // Loan fields
    var loanName by remember { mutableStateOf("") }
    var loanAmount by remember { mutableStateOf("") }
    var settlementAmount by remember { mutableStateOf("") }
    var depositAccountId by remember { mutableStateOf(savingsAccounts.firstOrNull()?.id) }
    var loanError by remember { mutableStateOf<String?>(null) }

    val effectiveDepositId = depositAccountId?.takeIf { id -> savingsAccounts.any { it.id == id } } ?: savingsAccounts.firstOrNull()?.id

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Add account", icon = Icons.Filled.AccountBalanceWallet)

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    CategoryToggleButton(
                        label = "Savings",
                        active = category == AccountCategory.SAVINGS,
                        onClick = { category = AccountCategory.SAVINGS },
                        modifier = Modifier.weight(1f),
                    )
                    CategoryToggleButton(
                        label = "Loan",
                        active = category == AccountCategory.LOAN,
                        onClick = { category = AccountCategory.LOAN },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (category == AccountCategory.SAVINGS) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Account name",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        SavingsType.entries.forEach { option ->
                            SavingsTypeChip(type = option, active = option == type, onClick = { type = option })
                        }
                    }

                    AppTextField(
                        value = balance,
                        onValueChange = { balance = it },
                        placeholder = "Starting balance",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    Button(
                        onClick = {
                            val balanceValue = balance.toDoubleOrNull()
                            if (name.isNotBlank() && balanceValue != null && balanceValue >= 0) {
                                onAddSavingsAccount(name.trim(), type, balanceValue)
                                name = ""
                                balance = ""
                                type = SavingsType.BANK
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(text = "Add account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    AppTextField(
                        value = loanName,
                        onValueChange = { loanName = it; loanError = null },
                        placeholder = "Loan name",
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppTextField(
                            value = loanAmount,
                            onValueChange = { loanAmount = it; loanError = null },
                            placeholder = "Loan amount",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                        )
                        AppTextField(
                            value = settlementAmount,
                            onValueChange = { settlementAmount = it; loanError = null },
                            placeholder = "Settlement amount",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (savingsAccounts.isEmpty()) {
                        Text(
                            text = "Add a savings account first to deposit the loan into.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    } else {
                        ChipRow(
                            options = savingsAccounts,
                            getKey = { it.id.toString() },
                            getLabel = { it.name },
                            selectedKey = effectiveDepositId?.toString() ?: "",
                            onSelect = { key -> depositAccountId = key.toLongOrNull(); loanError = null },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    loanError?.let {
                        Text(text = it, fontSize = 10.sp, color = BrandDanger, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = {
                            val result = onAddLoanAccount(loanName, loanAmount, settlementAmount, effectiveDepositId)
                            if (result == null) {
                                loanName = ""
                                loanAmount = ""
                                settlementAmount = ""
                                loanError = null
                            } else {
                                loanError = result
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    ) {
                        Text(text = "Add loan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryToggleButton(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val background = if (active) BrandPrimary else Color.Transparent
    val border = if (active) BrandPrimary else MaterialTheme.colorScheme.outline
    val content = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(12.dp))
            .selectable(selected = active, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = content)
    }
}

@Composable
private fun SavingsTypeChip(type: SavingsType, active: Boolean, onClick: () -> Unit) {
    val background = if (active) BrandPrimary else Color.Transparent
    val border = if (active) BrandPrimary else MaterialTheme.colorScheme.outline
    val content = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .border(width = 1.dp, color = border, shape = RoundedCornerShape(50))
            .selectable(selected = active, onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = type.icon, contentDescription = null, tint = content, modifier = Modifier.size(12.dp))
        Text(text = type.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = content)
    }
}

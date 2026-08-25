package com.blockveil.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blockveil.expensetracker.data.local.entity.AccountEntity
import com.blockveil.expensetracker.data.model.AccountCategory
import com.blockveil.expensetracker.ui.components.AppTextField
import com.blockveil.expensetracker.ui.theme.ExpenseTrackerTheme
import com.blockveil.expensetracker.util.EXPENSE_CATEGORIES

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun TransactionFormPiecesPreview() {
    var type by remember { mutableStateOf(TransactionFormType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var accountId by remember { mutableStateOf<Long?>(1L) }
    val sampleAccounts = listOf(
        AccountEntity(id = 1, name = "City Bank", category = AccountCategory.SAVINGS, balance = 45230.0),
        AccountEntity(id = 2, name = "Cash", category = AccountCategory.SAVINGS, balance = 1200.0),
    )

    ExpenseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                TransactionTypeSelector(
                    selected = type,
                    locked = false,
                    onSelect = { type = it },
                    modifier = Modifier.padding(bottom = 20.dp),
                )
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                CategoryPicker(
                    label = "Category",
                    categories = EXPENSE_CATEGORIES,
                    selected = category,
                    onSelect = { category = it },
                    onAddCustomCategory = {},
                )
                AccountPicker(
                    accounts = sampleAccounts,
                    selectedId = accountId,
                    onSelect = { accountId = it },
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.data.model.CurrencyPosition
import com.blockveil.expense.tracker.data.model.ThemeMode
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.util.CurrencyCatalog

/**
 * The main Settings page: Theme, Currency, Data, and About sections. Matches SettingsPage
 * exactly. Backup icon is Upload and Restore icon is Download in the source design (an
 * intentional pairing there, kept as-is here for fidelity even though it reads unusually).
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onSetThemeMode: (ThemeMode) -> Unit,
    currencyCountry: String,
    currencyPosition: CurrencyPosition,
    onOpenCurrency: () -> Unit,
    onOpenCategoryManagement: () -> Unit,
    onOpenSourceManagement: () -> Unit,
    onOpenAccountManagement: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenInfoPage: (InfoPageKey) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currency = CurrencyCatalog.findByCountry(currencyCountry)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        BackHeader(title = "Settings", onBack = onClose)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionHeader(title = "Theme", modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                ThemeMode.entries.forEach { mode ->
                    ThemeChip(
                        label = mode.label,
                        active = mode == themeMode,
                        onClick = { onSetThemeMode(mode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            SectionHeader(title = "Currency")
            SettingsLinkRow(
                label = if (currency != null) {
                    "${currency.flag} ${currency.country} · ${currency.code}" + if (currencyPosition == CurrencyPosition.SUFFIX) " (suffix)" else ""
                } else {
                    currencyCountry
                },
                onClick = onOpenCurrency,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            SectionHeader(title = "Manage")
            SettingsLinkRow(label = "Category Management", onClick = onOpenCategoryManagement, modifier = Modifier.padding(bottom = 8.dp))
            SettingsLinkRow(label = "Source Management", onClick = onOpenSourceManagement, modifier = Modifier.padding(bottom = 8.dp))
            SettingsLinkRow(label = "Account Management", onClick = onOpenAccountManagement, modifier = Modifier.padding(bottom = 20.dp))

            SectionHeader(title = "Data")
            SettingsActionRow(
                icon = Icons.Filled.Upload,
                title = "Backup",
                description = "Save all your data to a CSV file",
                onClick = onBackup,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SettingsActionRow(
                icon = Icons.Filled.Download,
                title = "Restore",
                description = "Load your data from a backup CSV file",
                onClick = onRestore,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            SettingsActionRow(
                icon = Icons.Filled.Delete,
                title = "Clear All Data",
                description = "Permanently delete everything from this device",
                onClick = onClearAllData,
                danger = true,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            SectionHeader(title = "About")
            SettingsLinkRow(label = "App Support", onClick = { onOpenInfoPage(InfoPageKey.SUPPORT) }, modifier = Modifier.padding(bottom = 8.dp))
            SettingsLinkRow(label = "Privacy Policy", onClick = { onOpenInfoPage(InfoPageKey.PRIVACY) }, modifier = Modifier.padding(bottom = 8.dp))
            SettingsLinkRow(label = "Terms & Conditions", onClick = { onOpenInfoPage(InfoPageKey.TERMS) }, modifier = Modifier.padding(bottom = 8.dp))
            SettingsLinkRow(label = "Donate", onClick = { onOpenInfoPage(InfoPageKey.DONATE) }, modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

@Composable
private fun ThemeChip(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

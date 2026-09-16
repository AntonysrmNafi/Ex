package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppTextField
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.EXPENSE_CATEGORY_ICON_CHOICES
import com.blockveil.expense.tracker.ui.components.INCOME_SOURCE_ICON_CHOICES
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.CustomCategoryPalette

/**
 * Creation form shared by "Add Custom Category" (expense) and "Add Custom Source" (income):
 * a name, a color (one of [CustomCategoryPalette] or a typed-in hex), and an icon from
 * [EXPENSE_CATEGORY_ICON_CHOICES] or [INCOME_SOURCE_ICON_CHOICES]. Reached from the always-visible top button on
 * CategoryManagementScreen / SourceManagementScreen, and from tapping "Custom" in the
 * transaction form's category/source picker (see TransactionFormScreen).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCustomCategoryScreen(
    isIncome: Boolean,
    onCreate: (name: String, color: Int, icon: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CustomCategoryPalette.first()) }
    var hexText by remember { mutableStateOf("") }
    val iconChoices = if (isIncome) INCOME_SOURCE_ICON_CHOICES else EXPENSE_CATEGORY_ICON_CHOICES
    var selectedIcon by remember { mutableStateOf(iconChoices.first().first) }
    var error by remember { mutableStateOf<String?>(null) }

    val noun = if (isIncome) "source" else "category"

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BackHeader(title = if (isIncome) "Add Custom Source" else "Add Custom Category", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            SectionHeader(title = "Name", modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
            AppTextField(
                value = name,
                onValueChange = { name = it; error = null },
                placeholder = if (isIncome) "Source name" else "Category name",
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader(title = "Color", modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomCategoryPalette.forEach { swatch ->
                    ColorSwatch(
                        color = swatch,
                        selected = selectedColor == swatch && hexText.isEmpty(),
                        onClick = { selectedColor = swatch; hexText = "" },
                    )
                }
            }
            AppTextField(
                value = hexText,
                onValueChange = { input ->
                    val cleaned = input.removePrefix("#").take(6)
                    hexText = cleaned
                    parseHexColor(cleaned)?.let { selectedColor = it }
                },
                placeholder = "Or type a hex code, e.g. FF7043",
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )

            SectionHeader(title = "Icon", modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                iconChoices.forEach { (key, icon) ->
                    IconSwatch(
                        icon = icon,
                        color = selectedColor,
                        selected = selectedIcon == key,
                        onClick = { selectedIcon = key },
                    )
                }
            }

            error?.let {
                Text(text = it, fontSize = 12.sp, color = BrandDanger, modifier = Modifier.padding(top = 16.dp))
            }

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        error = "Enter a $noun name"
                    } else {
                        onCreate(trimmed, selectedColor.toArgb(), selectedIcon)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedColor),
            ) {
                Text(text = "Create", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** "FF7043" (or "#FF7043") -> Color, or null if it isn't a valid 6-digit hex. */
private fun parseHexColor(hex: String): Color? {
    if (hex.length != 6 || hex.any { it !in "0123456789abcdefABCDEF" }) return null
    return try {
        Color(android.graphics.Color.parseColor("#$hex"))
    } catch (e: IllegalArgumentException) {
        null
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = if (selected) 2.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun IconSwatch(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
            .border(width = if (selected) 2.dp else 1.dp, color = if (selected) color else MaterialTheme.colorScheme.outline, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

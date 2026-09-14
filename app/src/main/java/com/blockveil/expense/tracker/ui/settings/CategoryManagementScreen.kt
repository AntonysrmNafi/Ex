package com.blockveil.expense.tracker.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.components.ConfirmDialog
import com.blockveil.expense.tracker.ui.components.SectionHeader
import com.blockveil.expense.tracker.ui.theme.BrandPrimary

/** One row in Category/Source Management: a built-in category/source, or a custom one. Unifies both under one UI since they're managed identically except for where hide/delete write to. */
data class ManagedCategoryUiModel(
    val name: String,
    val color: Color,
    val isHidden: Boolean,
    val isBuiltIn: Boolean,
)

/**
 * Lists every expense category, built-in and custom together, each with a Hide/Unhide and
 * Delete menu. Income sources live in their own Source Management screen instead (see
 * [SourceManagementScreen]).
 *
 * Hiding or deleting one only affects the picker for new transactions; past transactions
 * that already used it are completely unaffected, since categoryColor()/categoryIcon()
 * resolve a category's color/icon by name regardless of its hidden/deleted state. Deleting a
 * built-in category has no way back (no Unhide offered afterward), unlike hiding it.
 */
@Composable
fun CategoryManagementScreen(
    categories: List<ManagedCategoryUiModel>,
    onSetHidden: (ManagedCategoryUiModel, Boolean) -> Unit,
    onDelete: (ManagedCategoryUiModel) -> Unit,
    onAddCustom: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<ManagedCategoryUiModel?>(null) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        BackHeader(title = "Category Management", onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Button(
                onClick = onAddCustom,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            ) {
                Text(text = "Add Custom Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            SectionHeader(title = "Expense categories")
            ManagedCategoryList(
                entries = categories,
                emptyText = "No expense categories.",
                onSetHidden = onSetHidden,
                onDeleteRequest = { pendingDelete = it },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        ConfirmDialog(
            title = "Delete category?",
            message = "\"${toDelete.name}\" will no longer appear as a category choice. Past transactions keep their category name.",
            onConfirm = {
                onDelete(toDelete)
                pendingDelete = null
            },
            onCancel = { pendingDelete = null },
        )
    }
}

/** Shared by [CategoryManagementScreen] and [SourceManagementScreen]. */
@Composable
internal fun ManagedCategoryList(
    entries: List<ManagedCategoryUiModel>,
    emptyText: String,
    onSetHidden: (ManagedCategoryUiModel, Boolean) -> Unit,
    onDeleteRequest: (ManagedCategoryUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (entries.isEmpty()) {
            Text(text = emptyText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@Column
        }
        entries.forEach { entry ->
            ManagedCategoryRow(entry = entry, onSetHidden = { hidden -> onSetHidden(entry, hidden) }, onDeleteRequest = { onDeleteRequest(entry) })
        }
    }
}

@Composable
private fun ManagedCategoryRow(
    entry: ManagedCategoryUiModel,
    onSetHidden: (Boolean) -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

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
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(entry.color),
                )
                Text(text = entry.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                if (!entry.isBuiltIn) {
                    Text(text = "Custom", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (entry.isHidden) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = "Hidden",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Box {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options for ${entry.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp).clickable { menuOpen = true },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(if (entry.isHidden) "Unhide" else "Hide") },
                        onClick = {
                            onSetHidden(!entry.isHidden)
                            menuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
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

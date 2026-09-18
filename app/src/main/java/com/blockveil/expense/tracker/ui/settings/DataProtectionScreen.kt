package com.blockveil.expense.tracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockveil.expense.tracker.security.DeviceAdminProtection
import com.blockveil.expense.tracker.ui.components.AppCard
import com.blockveil.expense.tracker.ui.components.BackHeader
import com.blockveil.expense.tracker.ui.theme.BrandDanger
import com.blockveil.expense.tracker.ui.theme.BrandPrimary
import com.blockveil.expense.tracker.ui.theme.faded

/**
 * Explains and toggles the optional device-admin "Data & Deletion Protection" setting. Off
 * by default; nothing here runs unless the user taps the button. Turning it on hands off to
 * Android's own system dialog (can't be skipped, that's the OS, not this screen); turning it
 * off happens immediately since a plain device admin can deactivate itself without one.
 */
@Composable
fun DataProtectionScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(DeviceAdminProtection.isActive(context)) }

    val activationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        // The system dialog doesn't reliably report success/cancel through the result code,
        // so re-check the real admin state instead of trusting the returned Activity result.
        isActive = DeviceAdminProtection.isActive(context)
    }

    Column(modifier = modifier.fillMaxSize()) {
        BackHeader(title = "Data & Deletion Protection", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            StatusRow(isActive = isActive)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This is a device-level protection, completely optional and off by " +
                    "default. Turning it on tells Android to treat BlockVeil as an app that " +
                    "shouldn't be removed or wiped by accident.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "While it's on, Android itself:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            ProtectionPoint("Blocks uninstalling BlockVeil until protection is turned off first.")
            ProtectionPoint("Disables \"Clear storage\" and \"Clear data\" in Android's App Info page.")
            ProtectionPoint("Disables \"Force stop\", so the app can't get killed mid-save by mistake.")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nothing about your data or how the app works changes while this is " +
                    "on -- it only adds this one guard rail. You're always free to turn it " +
                    "back off, either with the button below or from Android Settings > " +
                    "Security > Device admin apps.",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isActive) {
                OutlinedButton(
                    onClick = {
                        DeviceAdminProtection.disable(context)
                        isActive = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandDanger),
                ) {
                    Text(text = "Turn off protection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick = { activationLauncher.launch(DeviceAdminProtection.enableIntent(context)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                ) {
                    Text(text = "Turn on protection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusRow(isActive: Boolean) {
    val tint = if (isActive) BrandPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.RemoveCircle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isActive) "Protection is ON" else "Protection is OFF",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ProtectionPoint(text: String) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 10.dp)
                .size(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandPrimary.faded()),
        )
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

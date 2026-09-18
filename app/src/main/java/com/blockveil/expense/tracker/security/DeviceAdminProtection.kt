package com.blockveil.expense.tracker.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Thin wrapper around [DevicePolicyManager] for the "Data & Deletion Protection" setting.
 * Entirely opt-in: nothing in this file runs unless the user taps "Turn on protection"
 * themselves, and [disable] hands it back just as directly, no system detour required.
 */
object DeviceAdminProtection {

    private fun adminComponent(context: Context): ComponentName =
        ComponentName(context.applicationContext, ProtectionDeviceAdminReceiver::class.java)

    private fun devicePolicyManager(context: Context): DevicePolicyManager =
        context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    /** True while the user has this app active as a device admin. */
    fun isActive(context: Context): Boolean =
        devicePolicyManager(context).isAdminActive(adminComponent(context))

    /** Launch with [android.content.Context.startActivity] (or an activity-result launcher) to
     *  show Android's own "Activate device admin app?" screen. Never call this automatically. */
    fun enableIntent(context: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Prevents BlockVeil from being uninstalled or having its data cleared by " +
                    "accident. Purely optional, and you can turn it off again anytime from " +
                    "inside the app.",
            )
        }

    /** A plain (non device-owner) admin can deactivate itself directly, no extra system
     *  confirmation screen needed -- unlike enabling, which Android requires to go through
     *  [enableIntent]'s system dialog. */
    fun disable(context: Context) {
        val dpm = devicePolicyManager(context)
        val admin = adminComponent(context)
        if (dpm.isAdminActive(admin)) {
            dpm.removeActiveAdmin(admin)
        }
    }
}

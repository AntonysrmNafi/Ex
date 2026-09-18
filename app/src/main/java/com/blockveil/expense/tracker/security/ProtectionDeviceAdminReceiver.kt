package com.blockveil.expense.tracker.security

import android.app.admin.DeviceAdminReceiver

/**
 * Backs the optional "Data & Deletion Protection" setting. Requests zero device-admin
 * policies (see res/xml/device_admin_receiver.xml) -- it never touches passwords, wipes
 * data, or anything else that API can do. The only effect of this being active is a side
 * effect of Android's own OS behavior for any active device admin: the system disables
 * "Force stop" and "Clear storage/data" in this app's App Info page, and blocks a direct
 * uninstall until the admin is deactivated first (from here or from Android Settings >
 * Security > Device admin apps). See [DeviceAdminProtection] for the enable/disable calls.
 */
class ProtectionDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun getDisableWarning(context: android.content.Context): CharSequence =
        "Turning this off removes BlockVeil's protection against accidental uninstall or " +
            "data clearing. Nothing else changes -- your data stays exactly as it is."
}

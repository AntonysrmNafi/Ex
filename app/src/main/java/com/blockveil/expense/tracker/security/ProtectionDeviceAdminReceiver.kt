package com.blockveil.expense.tracker.security

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Backs the optional "Data & Deletion Protection" setting. Requests zero device-admin
 * policies (see res/xml/device_admin_receiver.xml), it never touches passwords, wipes
 * data, or anything else that API can do. The only effect of this being active is a side
 * effect of Android's own OS behavior for any active device admin: the system disables
 * "Force stop" and "Clear storage/data" in this app's App Info page, and blocks a direct
 * uninstall until the admin is deactivated first (from here or from Android Settings >
 * Security > Device admin apps). See [DeviceAdminProtection] for the enable/disable calls.
 */
class ProtectionDeviceAdminReceiver : DeviceAdminReceiver() {

    /** Shown by Android's own confirmation screen when the user tries to disable this admin
     *  from system Settings (this app's in-app "Turn off protection" button skips it, since a
     *  plain admin can deactivate itself directly). */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        "Turning this off removes BlockVeil's protection against accidental uninstall or " +
            "data clearing. Nothing else changes, your data stays exactly as it is."
}

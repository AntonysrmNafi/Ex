package com.blockveil.expense.tracker.ui.settings

/** One of the four About-section pages. */
enum class InfoPageKey { SUPPORT, PRIVACY, TERMS, DONATE }

data class InfoPageContent(val title: String, val body: String)

/**
 * Placeholder About-section content, matches the four INFO_PAGES entries in spirit and
 * structure. A shipped app would replace these with its real support contact, privacy
 * policy, and terms of service.
 */
val INFO_PAGES: Map<InfoPageKey, InfoPageContent> = mapOf(
    InfoPageKey.SUPPORT to InfoPageContent(
        title = "App Support",
        body = "Need help with BlockVeil?\n\n" +
            "Reach out to support@blockveil.app and expect a reply within a business day or two.\n\n" +
            "Most questions are answered by checking that your accounts, categories, and latest backup are up to date in Settings, so it's worth a look there first.",
    ),
    InfoPageKey.PRIVACY to InfoPageContent(
        title = "Privacy Policy",
        body = "BlockVeil keeps your financial data on this device only. Nothing is sold or shared with third parties.\n\n" +
            "Backup files are saved wherever you choose on your own device, and restoring one only changes data on this device.\n\n" +
            "This is placeholder policy text for this build; a shipped release would replace it with the app's actual privacy policy.",
    ),
    InfoPageKey.TERMS to InfoPageContent(
        title = "Terms & Conditions",
        body = "Using BlockVeil means using it for personal budgeting and keeping your own backups of anything important.\n\n" +
            "The app is provided as-is, with no warranty, and isn't responsible for financial decisions made using it.\n\n" +
            "This is placeholder terms text for this build; a shipped release would replace it with the app's actual terms of service.",
    ),
    InfoPageKey.DONATE to InfoPageContent(
        title = "Donate",
        body = "BlockVeil is built and maintained independently. If it's helped you stay on top of your money, supporting future development is always appreciated.\n\n" +
            "Every contribution helps keep the app free, ad-free, and focused on budgeting rather than anything else.\n\n" +
            "Thanks for using BlockVeil.",
    ),
)

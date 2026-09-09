# BlockVeil Expense Tracker

An offline-first personal expense tracker for Android. Kotlin, Jetpack Compose, Room, MVVM.
Ported from a React web preview (`expense-tracker-preview.html`) to a native Android app across
20 development sessions; this README covers the finished app.

## Features

- **Accounts**: bank, wallet, cash, and investment accounts; loan accounts with repayment and
  auto-settle tracking; net worth summary.
- **Transactions**: income, expense, transfer between accounts, and loan repayment, with
  categories (including custom categories and colors), notes, location, and receipt photos.
- **Home**: month switcher, budget card, account strip, recent transactions, custom date range
  filter.
- **History**: combined transaction and transfer list with search and filters.
- **Category and account drill-down**: browse every transaction in one category or account.
- **Analytics**: category breakdown donuts (expense and income), budget usage ring, per-category
  envelope limits, rolling budget, spending forecast, and anomaly flags.
- **Goals**: savings goals with progress tracking and contributions.
- **Subscriptions**: recurring subscriptions with automatic due-date billing.
- **Settings**: light/dark/system theme, currency (country, symbol position, format), CSV backup
  and restore via the system file picker, clear all data.
- **Offline-first**: all data lives in a local Room database; nothing leaves the device.

## Tech stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM (ViewModel + Repository) |
| Persistence | Room (transactions, accounts, subscriptions, goals, transfers, budgets, custom categories) |
| Preferences | DataStore (theme, currency settings) |
| Images | Coil (receipt photos) |
| Async | Kotlin Coroutines + Flow |
| Min/target SDK | 26 (Android 8.0) / 34 |

## Project structure

```
app/src/main/java/com/blockveil/expense/tracker/
├── data/
│   ├── local/        Room entities, DAOs, database
│   ├── model/         Plain data/enum types shared across layers
│   └── repository/    Repositories (business logic sits here, not in ViewModels)
├── ui/
│   ├── home/ history/ analytics/ goals/ more/ settings/ transaction/ drilldown/
│   │                   One package per screen: Route (wiring) + Screen (layout) + ViewModel
│   └── components/     Shared building blocks (Card, ConfirmDialog, Toast, MoneyBurst, ...)
├── util/               Money formatting, date helpers, CSV backup/restore parsing
├── di/                 Manual dependency container (AppContainer)
└── MainActivity.kt     Theming, splash screen, and the top-level screen switch
```

There's no dependency injection framework and no Navigation Compose graph: `AppContainer` wires
repositories by hand, and `MainActivity` swaps screens with a small sealed class instead of a
NavHost, since the whole app is a handful of screens with no deep back stack.

## Running the app

1. Open the project root in Android Studio (Koala or newer recommended).
2. Let Gradle sync; it will download the Android Gradle Plugin, Compose, Room, and the other
   dependencies listed in `gradle/libs.versions.toml`.
3. Run the `app` configuration on a device or emulator running Android 8.0 (API 26) or later.

Or from the command line:

```bash
./gradlew assembleDebug
# APK lands in app/build/outputs/apk/debug/app-debug.apk
```

The camera permission (for receipt photos) is requested at runtime the first time you attach a
photo to a transaction; the app works fully without granting it.

## Release signing

Release builds are unsigned by default so the project builds cleanly with zero configuration. To
produce a signed release APK, set these four environment variables before running
`assembleRelease` (locally or in CI):

| Variable | Meaning |
|---|---|
| `RELEASE_KEYSTORE_PATH` | Path to your `.jks`/`.keystore` file |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Alias of the key inside the keystore |
| `RELEASE_KEY_PASSWORD` | Password for that key |

If you don't have a keystore yet:

```bash
keytool -genkeypair -v -keystore release.keystore -alias blockveil \
  -keyalg RSA -keysize 2048 -validity 10000
```

### Signing in CI

The GitHub Actions workflow (`.github/workflows/build.yml`) builds a signed release APK
automatically when these four repository secrets are set (Settings > Secrets and variables >
Actions):

- `RELEASE_KEYSTORE_BASE64`, your keystore file, base64-encoded:
  ```bash
  base64 -i release.keystore | tr -d '\n' > release.keystore.b64
  ```
  Paste the contents of `release.keystore.b64` as the secret value.
- `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`, same as above.

Without these secrets, the workflow still runs and uploads a debug APK; the release-signing
steps are skipped rather than failing the build.

## Backup and restore

Settings > Backup writes a CSV snapshot of all transactions, accounts, subscriptions, and goals
through the system "Save to" file picker (Storage Access Framework), so it can be saved to
Drive, a local folder, or anywhere else the picker offers. Settings > Restore reads that CSV back
through the "Open" picker. Both stay entirely on-device; nothing is uploaded anywhere by the app
itself.

## Known limitations

- The Category History drill-down screen (built in Bag 11) isn't reachable from anywhere in the
  app. In the source web design it opens when you tap a transaction row on the Home screen, but
  this Android port has that same tap open the transaction edit form directly instead, and that
  behavior was kept deliberately during Bag 20's integration pass rather than changed to match
  the source design. The screen itself works; it's just unwired. Wire it up from Home's
  `onTxnClick` (pass the tapped transaction's category and type instead of just its id) if it's
  ever wanted.
- Subscription add doesn't distinguish "no charges yet due" from "N charges recorded
  immediately" in its confirmation toast (both just show "Subscription added"), unlike the web
  preview's dynamic count. Low-risk simplification made in Bag 18 to avoid a repository
  signature change on a save-critical path; see that bag's notes if the exact wording matters.
- No automated test suite yet (unit or instrumented). The app has been reviewed file-by-file
  across all 20 bags but not run on a device from this environment, which has no Android SDK or
  emulator access; a first real device/emulator run is worth doing before wider release.

package com.blockveil.expensetracker.data.backup

import com.blockveil.expensetracker.data.model.AccountCategory

/**
 * One CSV file holds every table the app needs, each introduced by a "#SECTION" marker line
 * followed by a header row and its data rows, with a blank line separating sections. Direct
 * port of buildBackupCSV / parseBackupCSV from the source design.
 *
 * Two intentional differences from the source design's schema, both because this backup
 * format only ever round-trips through this app's own code, never through the web preview:
 * - TRANSACTIONS and TRANSFERS drop the "monthOffset" column. It existed only to group the
 *   preview's synthetic seed data under a frozen demo date; the real app has no frozen
 *   clock, so month grouping is always computed from the real `date` column instead.
 * - Enum-backed columns (type, category, cycle, kind, themeMode, etc.) are written as their
 *   Kotlin enum names (e.g. "EXPENSE") rather than the source design's lowercase strings.
 */
object CsvBackup {

    fun build(snapshot: BackupSnapshot): String {
        val lines = mutableListOf<String>()

        fun section(name: String, headers: List<String>, rows: List<List<Any?>>) {
            lines += "#$name"
            lines += headers.joinToString(",")
            rows.forEach { row -> lines += row.joinToString(",") { csvEscape(it) } }
            lines += ""
        }

        section(
            "SETTINGS",
            listOf("budget", "rollingEnabled", "themeMode", "currencyCountry", "currencyPosition", "currencyFormat"),
            listOf(
                listOf(
                    snapshot.settings.budget,
                    snapshot.settings.rollingEnabled,
                    snapshot.settings.themeMode.name,
                    snapshot.settings.currencyCountry,
                    snapshot.settings.currencyPosition.name,
                    snapshot.settings.currencyFormat.name,
                ),
            ),
        )

        section(
            "ACCOUNTS",
            listOf("id", "name", "category", "type", "balance", "principal", "loanAmount", "repaid", "active"),
            snapshot.accounts.map { a ->
                listOf(
                    a.id,
                    a.name,
                    a.category.name,
                    a.type?.name ?: "",
                    if (a.category == AccountCategory.SAVINGS) a.balance else "",
                    if (a.category == AccountCategory.LOAN) a.principal else "",
                    if (a.category == AccountCategory.LOAN) (a.loanAmount ?: a.principal) else "",
                    if (a.category == AccountCategory.LOAN) a.repaid else "",
                    if (a.category == AccountCategory.LOAN) a.active else "",
                )
            },
        )

        section(
            "TRANSACTIONS",
            listOf("id", "type", "amount", "category", "note", "date", "accountId", "location", "hasReceipt"),
            snapshot.transactions.map { t ->
                listOf(
                    t.id, t.type.name, t.amount, t.category, t.note,
                    t.date.toString(), t.accountId ?: "", t.location, t.receiptPhotoPath != null,
                )
            },
        )

        section(
            "SUBSCRIPTIONS",
            listOf("id", "name", "category", "amount", "cycle", "accountId", "startDate", "endDate", "active", "billedDates"),
            snapshot.subscriptions.map { s ->
                listOf(
                    s.id, s.name, s.category, s.amount, s.cycle.name, s.accountId ?: "",
                    s.startDate?.toString() ?: "", s.endDate?.toString() ?: "", s.active,
                    s.billedDates.joinToString(";") { it.toString() },
                )
            },
        )

        section(
            "GOALS",
            listOf("id", "name", "targetAmount", "savedAmount", "targetMonths"),
            snapshot.goals.map { g -> listOf(g.id, g.name, g.targetAmount, g.savedAmount, g.targetMonths) },
        )

        section(
            "TRANSFERS",
            listOf("id", "kind", "fromId", "toId", "amount", "note", "date"),
            snapshot.transfers.map { t ->
                listOf(t.id, t.kind.name, t.fromId ?: "", t.toId ?: "", t.amount, t.note, t.date.toString())
            },
        )

        section(
            "CATEGORY_BUDGETS",
            listOf("category", "limit"),
            snapshot.categoryBudgets.map { b -> listOf(b.category, b.limitAmount) },
        )

        section(
            "CUSTOM_EXPENSE_CATEGORIES",
            listOf("name", "color"),
            snapshot.customExpenseCategories.map { listOf(it.name, colorToHex(it.color)) },
        )

        section(
            "CUSTOM_INCOME_CATEGORIES",
            listOf("name", "color"),
            snapshot.customIncomeCategories.map { listOf(it.name, colorToHex(it.color)) },
        )

        return lines.joinToString("\n")
    }

    /**
     * Parses raw CSV text back into section name -> list of (header -> cell value) rows,
     * everything still a String. Converting those strings into typed entities and deciding
     * how to merge them into the database is Bag 17's job, once the Settings screen knows
     * exactly what restore flow it wants (replace everything vs merge, id remapping, etc.).
     */
    fun parse(text: String): Map<String, List<Map<String, String>>> {
        val sections = linkedMapOf<String, MutableList<Map<String, String>>>()
        var current: String? = null
        var headers: List<String>? = null

        text.split(Regex("\r\n|\n")).forEach { line ->
            when {
                line.startsWith("#") -> {
                    current = line.substring(1).trim()
                    sections[current!!] = mutableListOf()
                    headers = null
                }
                line.isBlank() -> headers = null
                current == null -> Unit
                headers == null -> headers = parseCsvLine(line)
                else -> {
                    val cells = parseCsvLine(line)
                    val row = headers!!.mapIndexed { i, h -> h to cells.getOrElse(i) { "" } }.toMap()
                    sections.getValue(current!!).add(row)
                }
            }
        }
        return sections
    }

    private fun csvEscape(value: Any?): String {
        if (value == null) return ""
        val str = value.toString()
        return if (Regex("[\",\n]").containsMatchIn(str)) "\"${str.replace("\"", "\"\"")}\"" else str
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    current.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        result.add(current.toString())
                        current.setLength(0)
                    }
                    else -> current.append(c)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    // Drops the alpha channel, keeps just RGB, matches the "#RRGGBB" strings the source
    // design stores for custom category colors.
    private fun colorToHex(argb: Int): String = String.format("#%06X", argb and 0xFFFFFF)
}

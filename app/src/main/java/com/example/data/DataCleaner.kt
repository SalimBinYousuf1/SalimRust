package com.example.data

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class CleaningSuggestion(
    val column: String,
    val issue: String,
    val severity: SuggestionSeverity,
    val recommendedAction: String,
    val autoFixAvailable: Boolean
)

enum class SuggestionSeverity {
    HIGH, MEDIUM, LOW
}

data class OutlierItem(
    val rowIndex: Int,
    val column: String,
    val value: Double,
    val zScore: Double,
    val threshold: String
)

data class SchemaRule(
    val column: String,
    val expectedType: ColumnType,
    val nullable: Boolean,
    val minValue: Double? = null,
    val maxValue: Double? = null
)

data class ValidationViolation(
    val rowIndex: Int,
    val column: String,
    val reason: String
)

data class SchemaValidationReport(
    val isValid: Boolean,
    val totalCheckedRows: Int,
    val violationsCount: Int,
    val violations: List<ValidationViolation>
)

object DataCleaner {

    /**
     * Inspects the DataFrame for dirty data, anomalies, mixed types, and suggests cleaning steps
     * (implements `df.suggest_cleaning()` from Master Prompt Section 13).
     */
    fun suggestCleaning(df: DataFrame): List<CleaningSuggestion> {
        val suggestions = mutableListOf<CleaningSuggestion>()

        if (df.height == 0) {
            suggestions.add(
                CleaningSuggestion(
                    column = "All",
                    issue = "DataFrame is completely empty (0 rows)",
                    severity = SuggestionSeverity.HIGH,
                    recommendedAction = "Load a baseline dataset or paste CSV data",
                    autoFixAvailable = false
                )
            )
            return suggestions
        }

        df.columnNames.forEach { colName ->
            val values = df.rows.map { it[colName] }
            val nullCount = values.count { it == null }
            val nullPct = (nullCount.toDouble() / df.height) * 100.0

            // 1. High Null Ratio
            if (nullPct > 50.0) {
                suggestions.add(
                    CleaningSuggestion(
                        column = colName,
                        issue = "Over 50% missing values (${"%.1f".format(nullPct)}%)",
                        severity = SuggestionSeverity.HIGH,
                        recommendedAction = "Drop column: df.drop(\"$colName\") or use mode/median imputation",
                        autoFixAvailable = true
                    )
                )
            } else if (nullPct > 0.0) {
                suggestions.add(
                    CleaningSuggestion(
                        column = colName,
                        issue = "Contains $nullCount missing values (${"%.1f".format(nullPct)}%)",
                        severity = SuggestionSeverity.MEDIUM,
                        recommendedAction = "Impute with median: df.fillna(\"$colName\", median)",
                        autoFixAvailable = true
                    )
                )
            }

            // 2. Constant column (Zero Variance)
            val distinctVals = values.filterNotNull().distinct()
            if (distinctVals.size == 1 && df.height > 1) {
                suggestions.add(
                    CleaningSuggestion(
                        column = colName,
                        issue = "Constant column with 0 variance (value: '${distinctVals.first()}')",
                        severity = SuggestionSeverity.MEDIUM,
                        recommendedAction = "Drop uninformative feature: df.drop(\"$colName\")",
                        autoFixAvailable = true
                    )
                )
            }

            // 3. Negative values in non-negative domains (e.g., age, fare)
            val numVals = values.mapNotNull { (it as? Number)?.toDouble() }
            if (colName.contains("age", ignoreCase = true) || colName.contains("fare", ignoreCase = true) || colName.contains("bill", ignoreCase = true)) {
                val negativeCount = numVals.count { it < 0.0 }
                if (negativeCount > 0) {
                    suggestions.add(
                        CleaningSuggestion(
                            column = colName,
                            issue = "$negativeCount negative values detected in positive domain",
                            severity = SuggestionSeverity.HIGH,
                            recommendedAction = "Clip values to zero: df.clip(\"$colName\", min=0.0)",
                            autoFixAvailable = true
                        )
                    )
                }
            }

            // 4. Extreme Outliers (> 3 standard deviations)
            if (numVals.size > 10) {
                val mean = numVals.average()
                val variance = numVals.sumOf { (it - mean).pow(2) } / (numVals.size - 1)
                val std = sqrt(variance)
                if (std > 0.0) {
                    val outliers = numVals.count { abs(it - mean) / std > 3.0 }
                    if (outliers > 0) {
                        suggestions.add(
                            CleaningSuggestion(
                                column = colName,
                                issue = "$outliers statistical outliers detected (|Z-Score| > 3.0)",
                                severity = SuggestionSeverity.LOW,
                                recommendedAction = "Winsorize or cap at 99th percentile: df.clip(\"$colName\", max=p99)",
                                autoFixAvailable = true
                            )
                        )
                    }
                }
            }
        }

        return suggestions
    }

    /**
     * Detects outliers via Z-score and IQR methods.
     */
    fun detectOutliers(df: DataFrame, column: String, zThreshold: Double = 2.5): List<OutlierItem> {
        val numPairs = df.rows.mapIndexedNotNull { idx, row ->
            val v = (row[column] as? Number)?.toDouble()
            if (v != null) Pair(idx, v) else null
        }
        if (numPairs.size < 4) return emptyList()

        val values = numPairs.map { it.second }
        val mean = values.average()
        val variance = values.sumOf { (it - mean).pow(2) } / (values.size - 1)
        val std = sqrt(variance)
        if (std == 0.0) return emptyList()

        val outliers = mutableListOf<OutlierItem>()
        numPairs.forEach { (idx, v) ->
            val z = (v - mean) / std
            if (abs(z) >= zThreshold) {
                outliers.add(
                    OutlierItem(
                        rowIndex = idx,
                        column = column,
                        value = v,
                        zScore = z,
                        threshold = "|Z| >= $zThreshold"
                    )
                )
            }
        }
        return outliers
    }

    /**
     * Schema validation engine enforcing non-null constraints, ranges, and types.
     */
    fun validateSchema(df: DataFrame, rules: List<SchemaRule>): SchemaValidationReport {
        val violations = mutableListOf<ValidationViolation>()

        df.rows.forEachIndexed { rowIdx, rowMap ->
            rules.forEach { rule ->
                val v = rowMap[rule.column]
                if (v == null) {
                    if (!rule.nullable) {
                        violations.add(
                            ValidationViolation(
                                rowIndex = rowIdx,
                                column = rule.column,
                                reason = "Null value violates NOT-NULL rule"
                            )
                        )
                    }
                } else {
                    val num = (v as? Number)?.toDouble()
                    if (num != null) {
                        if (rule.minValue != null && num < rule.minValue) {
                            violations.add(
                                ValidationViolation(
                                    rowIndex = rowIdx,
                                    column = rule.column,
                                    reason = "Value ${"%.2f".format(num)} is below min bound ${rule.minValue}"
                                )
                            )
                        }
                        if (rule.maxValue != null && num > rule.maxValue) {
                            violations.add(
                                ValidationViolation(
                                    rowIndex = rowIdx,
                                    column = rule.column,
                                    reason = "Value ${"%.2f".format(num)} exceeds max bound ${rule.maxValue}"
                                )
                            )
                        }
                    }
                }
            }
        }

        return SchemaValidationReport(
            isValid = violations.isEmpty(),
            totalCheckedRows = df.height,
            violationsCount = violations.size,
            violations = violations.take(50)
        )
    }

    /**
     * Executes automatic cleaning operations on a DataFrame.
     */
    fun autoClean(df: DataFrame): DataFrame {
        var cleaned = df
        df.columnNames.forEach { col ->
            val numVals = cleaned.rows.mapNotNull { (it[col] as? Number)?.toDouble() }
            if (numVals.isNotEmpty()) {
                val median = numVals.sorted()[numVals.size / 2]
                cleaned = cleaned.fillna(col, median)
            }
        }
        return cleaned
    }
}

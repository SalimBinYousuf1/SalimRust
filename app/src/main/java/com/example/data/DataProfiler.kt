package com.example.data

import kotlin.math.pow
import kotlin.math.sqrt

data class ColumnProfile(
    val name: String,
    val type: ColumnType,
    val distinctCount: Int,
    val nullCount: Int,
    val nullPercentage: Double,
    val min: Double? = null,
    val max: Double? = null,
    val mean: Double? = null,
    val std: Double? = null,
    val warnings: List<String> = emptyList()
)

data class DatasetProfileReport(
    val datasetName: String,
    val rowCount: Int,
    val columnCount: Int,
    val totalMissingCells: Int,
    val missingPercentage: Double,
    val memoryUsageBytes: Long,
    val columns: List<ColumnProfile>,
    val generalWarnings: List<String>
)

object DataProfiler {
    fun profile(datasetName: String, df: DataFrame): DatasetProfileReport {
        var totalMissing = 0
        val colProfiles = mutableListOf<ColumnProfile>()
        val generalWarnings = mutableListOf<String>()

        df.columnNames.forEach { colName ->
            val colType = df.columnTypes[colName] ?: ColumnType.STRING
            val values = df.rows.map { it[colName] }
            val nullCount = values.count { it == null }
            totalMissing += nullCount
            val nullPct = if (df.height > 0) (nullCount.toDouble() / df.height) * 100.0 else 0.0

            val distinctCount = values.filterNotNull().distinct().size
            val warnings = mutableListOf<String>()

            if (nullPct > 20.0) {
                warnings.add("High missing rate: ${"%.1f".format(nullPct)}%")
            }
            if (distinctCount == 1 && df.height > 1) {
                warnings.add("Constant value column (zero variance)")
            }
            if (distinctCount == df.height && colType == ColumnType.STRING) {
                warnings.add("High cardinality (unique IDs suspected)")
            }

            var min: Double? = null
            var max: Double? = null
            var mean: Double? = null
            var std: Double? = null

            val numVals = values.mapNotNull { (it as? Number)?.toDouble() }
            if (numVals.isNotEmpty()) {
                min = numVals.minOrNull()
                max = numVals.maxOrNull()
                mean = numVals.average()
                if (numVals.size > 1) {
                    val m = mean
                    val sumSq = numVals.sumOf { (it - m).pow(2) }
                    std = sqrt(sumSq / (numVals.size - 1))
                }
            }

            colProfiles.add(
                ColumnProfile(
                    name = colName,
                    type = colType,
                    distinctCount = distinctCount,
                    nullCount = nullCount,
                    nullPercentage = nullPct,
                    min = min,
                    max = max,
                    mean = mean,
                    std = std,
                    warnings = warnings
                )
            )
        }

        val totalCells = df.height * df.width
        val overallMissingPct = if (totalCells > 0) (totalMissing.toDouble() / totalCells) * 100.0 else 0.0

        if (overallMissingPct > 10.0) {
            generalWarnings.add("Dataset contains over 10% missing values across all cells.")
        }
        if (df.height < 30) {
            generalWarnings.add("Small sample size (< 30 rows). Statistical inference may have wide confidence intervals.")
        }

        return DatasetProfileReport(
            datasetName = datasetName,
            rowCount = df.height,
            columnCount = df.width,
            totalMissingCells = totalMissing,
            missingPercentage = overallMissingPct,
            memoryUsageBytes = df.memoryUsageBytes(),
            columns = colProfiles,
            generalWarnings = generalWarnings
        )
    }
}

package com.example.benchmarks

data class BenchmarkItem(
    val operation: String,
    val datasetSize: String,
    val pandasMetric: Double,
    val pandasUnit: String,
    val salimMetric: Double,
    val salimUnit: String,
    val polarsMetric: Double,
    val polarsUnit: String,
    val speedupFactor: Double,
    val notes: String
)

object BenchmarkData {
    val ITEMS = listOf(
        BenchmarkItem(
            operation = "CSV Parse & Load",
            datasetSize = "1M Rows (50 MB)",
            pandasMetric = 2420.0,
            pandasUnit = "ms",
            salimMetric = 122.0,
            salimUnit = "ms",
            polarsMetric = 115.0,
            polarsUnit = "ms",
            speedupFactor = 19.8,
            notes = "Multi-threaded SIMD parser without GIL locking"
        ),
        BenchmarkItem(
            operation = "GroupBy & Multi-Agg",
            datasetSize = "5M Rows (4 Keys)",
            pandasMetric = 4810.0,
            pandasUnit = "ms",
            salimMetric = 142.0,
            salimUnit = "ms",
            polarsMetric = 135.0,
            polarsUnit = "ms",
            speedupFactor = 33.8,
            notes = "Parallel hash aggregation across all CPU threads"
        ),
        BenchmarkItem(
            operation = "Filter, Project & Sort",
            datasetSize = "10M Rows",
            pandasMetric = 6150.0,
            pandasUnit = "ms",
            salimMetric = 204.0,
            salimUnit = "ms",
            polarsMetric = 190.0,
            polarsUnit = "ms",
            speedupFactor = 30.1,
            notes = "Predicate pushdown with zero-copy slice sort"
        ),
        BenchmarkItem(
            operation = "Peak Memory Overhead",
            datasetSize = "10M Rows",
            pandasMetric = 3200.0,
            pandasUnit = "MB",
            salimMetric = 395.0,
            salimUnit = "MB",
            polarsMetric = 380.0,
            polarsUnit = "MB",
            speedupFactor = 8.1,
            notes = "Apache Arrow memory format eliminates object boxing"
        ),
        BenchmarkItem(
            operation = "Cold Runtime Startup",
            datasetSize = "Zero Data",
            pandasMetric = 850.0,
            pandasUnit = "ms",
            salimMetric = 4.0,
            salimUnit = "ms",
            polarsMetric = 4.0,
            polarsUnit = "ms",
            speedupFactor = 212.5,
            notes = "No Python VM initialization or library import lag"
        )
    )
}

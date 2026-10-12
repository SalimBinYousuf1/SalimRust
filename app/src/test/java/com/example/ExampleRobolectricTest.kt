package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AggregationType
import com.example.data.ColumnType
import com.example.data.DataCleaner
import com.example.data.DataFrame
import com.example.data.SampleDatasets
import com.example.ml.MLEngine
import com.example.notebook.NotebookEngine
import com.example.stats.StatsEngine
import com.example.translator.Py2RsTranslator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Salim Data Kit`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim Data Kit", appName)
    }

    @Test
    fun `test DataFrame filter and query operations`() {
        val df = DataFrame.fromDataset(SampleDatasets.TITANIC)
        assertTrue(df.height > 0)

        // Filter by age > 30
        val filtered = df.query("age > 30")
        assertTrue(filtered.height in 1 until df.height)

        // Test GroupBy
        val grouped = df.groupBy(listOf("pclass"), listOf("fare" to AggregationType.MEAN))
        assertTrue(grouped.height in 1..3)
    }

    @Test
    fun `test DataFrame resilient transformations and extreme conditions`() {
        val df = DataFrame.fromDataset(SampleDatasets.TITANIC)

        // Clip fare between 10.0 and 100.0
        val clipped = df.clip("fare", minVal = 10.0, maxVal = 100.0)
        clipped.rows.mapNotNull { (it["fare"] as? Number)?.toDouble() }.forEach { fare ->
            assertTrue(fare in 10.0..100.0)
        }

        // Cumulative sum on fare
        val withCumsum = df.cumsum("fare")
        assertTrue(withCumsum.columnNames.contains("fare_cumsum"))

        // Difference and percentage change
        val withDiff = df.diff("fare")
        assertTrue(withDiff.columnNames.contains("fare_diff"))

        val withPct = df.pct_change("fare")
        assertTrue(withPct.columnNames.contains("fare_pct_change"))

        // Rolling mean window
        val withRolling = df.rolling_mean("fare", window = 3)
        assertTrue(withRolling.columnNames.contains("fare_ma3"))

        // Edge case: Empty DataFrame
        val emptyDf = DataFrame(listOf("a", "b"), mapOf("a" to ColumnType.FLOAT, "b" to ColumnType.FLOAT), emptyList())
        assertEquals(0, emptyDf.height)
        val emptyClean = DataCleaner.suggestCleaning(emptyDf)
        assertTrue(emptyClean.isNotEmpty())
    }

    @Test
    fun `test DataCleaner suggestions and outlier detection`() {
        val df = DataFrame.fromDataset(SampleDatasets.TITANIC)
        val suggestions = DataCleaner.suggestCleaning(df)
        assertNotNull(suggestions)
        assertTrue(suggestions.isNotEmpty())

        val outliers = DataCleaner.detectOutliers(df, "fare", zThreshold = 2.0)
        assertNotNull(outliers)
    }

    @Test
    fun `test StatsEngine statistics and hypothesis testing`() {
        val dataA = listOf(10.0, 12.0, 14.0, 15.0, 18.0)
        val dataB = listOf(22.0, 24.0, 25.0, 27.0, 30.0)

        val stats = StatsEngine.computeStats(dataA)
        assertNotNull(stats)
        assertEquals(13.8, stats!!.mean, 0.01)

        val tTest = StatsEngine.twoSampleTTest(dataA, dataB)
        assertTrue(tTest.isSignificant05)

        // Degenerate statistical cases (zero variance, identical numbers)
        val identical = listOf(5.0, 5.0, 5.0, 5.0)
        val identicalStats = StatsEngine.computeStats(identical)
        assertNotNull(identicalStats)
        assertEquals(0.0, identicalStats!!.std, 0.001)
    }

    @Test
    fun `test Py2RsTranslator code translation`() {
        val pythonSnippet = "df.groupby('dept')['salary'].mean()"
        val (rustCode, speedup) = Py2RsTranslator.translateCode(pythonSnippet)
        assertTrue(rustCode.contains("salim::prelude::*"))
        assertTrue(rustCode.contains("group_by"))
        assertTrue(speedup.contains("faster"))
    }

    @Test
    fun `test MLEngine classifier training`() {
        val eval = MLEngine.trainClassifier(
            modelName = "Logistic Regression",
            rows = SampleDatasets.TITANIC.rows,
            featureCols = listOf("fare", "pclass"),
            targetCol = "survived",
            testRatio = 0.2
        )
        assertNotNull(eval)
        assertTrue(eval!!.accuracy > 0.5)
    }

    @Test
    fun `test NotebookEngine sample cells verification`() {
        val samples = NotebookEngine.SAMPLES
        assertTrue(samples.size >= 5)
        samples.forEach { sample ->
            assertTrue(sample.title.isNotBlank())
            assertTrue(sample.rustCode.contains("salim"))
            assertTrue(sample.executionLog.isNotEmpty())
        }
    }
}

package com.example.stats

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

data class DetailedStats(
    val count: Int,
    val mean: Double,
    val std: Double,
    val variance: Double,
    val median: Double,
    val p25: Double,
    val p75: Double,
    val iqr: Double,
    val min: Double,
    val max: Double,
    val skewness: Double,
    val kurtosis: Double
)

data class HypothesisTestReport(
    val testName: String,
    val testStatisticName: String,
    val statistic: Double,
    val pValue: Double,
    val degreesOfFreedom: Double,
    val isSignificant05: Boolean,
    val conclusion: String
)

data class RegressionResult(
    val slope: Double,
    val intercept: Double,
    val rSquared: Double,
    val pearsonR: Double,
    val stdError: Double,
    val pValue: Double,
    val equation: String
)

object StatsEngine {

    fun computeStats(numbers: List<Double>): DetailedStats? {
        if (numbers.isEmpty()) return null
        val count = numbers.size
        val sorted = numbers.sorted()
        val sum = sorted.sum()
        val mean = sum / count

        val variance = if (count > 1) {
            sorted.sumOf { (it - mean).pow(2) } / (count - 1)
        } else 0.0
        val std = sqrt(variance)

        val median = if (count % 2 == 0) {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        } else {
            sorted[count / 2]
        }

        val p25 = sorted[(count * 0.25).toInt().coerceIn(0, count - 1)]
        val p75 = sorted[(count * 0.75).toInt().coerceIn(0, count - 1)]
        val iqr = p75 - p25

        // Skewness & Kurtosis
        var m3 = 0.0
        var m4 = 0.0
        sorted.forEach {
            val diff = it - mean
            m3 += diff.pow(3)
            m4 += diff.pow(4)
        }
        val skewness = if (count > 2 && std > 0.0) (m3 / count) / std.pow(3) else 0.0
        val kurtosis = if (count > 3 && std > 0.0) ((m4 / count) / std.pow(4)) - 3.0 else 0.0

        return DetailedStats(
            count = count,
            mean = mean,
            std = std,
            variance = variance,
            median = median,
            p25 = p25,
            p75 = p75,
            iqr = iqr,
            min = sorted.first(),
            max = sorted.last(),
            skewness = skewness,
            kurtosis = kurtosis
        )
    }

    fun twoSampleTTest(a: List<Double>, b: List<Double>): HypothesisTestReport {
        val statA = computeStats(a) ?: return errorReport("T-Test", "Insufficient sample size")
        val statB = computeStats(b) ?: return errorReport("T-Test", "Insufficient sample size")

        val diff = statA.mean - statB.mean
        val se = sqrt((statA.variance / statA.count) + (statB.variance / statB.count))
        val t = if (se == 0.0) 0.0 else diff / se

        val df = (statA.count + statB.count - 2).toDouble()
        // Approximation of 2-tailed p-value for Student t
        val p = 2.0 / (1.0 + exp(0.71 * abs(t)))
        val pVal = p.coerceIn(0.0001, 1.0)
        val sig = pVal < 0.05

        return HypothesisTestReport(
            testName = "Two-Sample Independent Student's T-Test",
            testStatisticName = "t",
            statistic = t,
            pValue = pVal,
            degreesOfFreedom = df,
            isSignificant05 = sig,
            conclusion = if (sig) {
                "Reject Null Hypothesis (p < 0.05). Statistically significant difference between groups."
            } else {
                "Fail to reject Null Hypothesis (p >= 0.05). No statistically significant difference detected."
            }
        )
    }

    fun oneWayAnova(groups: List<List<Double>>): HypothesisTestReport {
        val totalN = groups.sumOf { it.size }
        val k = groups.size
        if (k < 2 || totalN <= k) return errorReport("ANOVA", "Need at least 2 groups with adequate size")

        val grandMean = groups.flatten().average()
        var ssBetween = 0.0
        var ssWithin = 0.0

        groups.forEach { group ->
            val gMean = group.average()
            ssBetween += group.size * (gMean - grandMean).pow(2)
            group.forEach { v ->
                ssWithin += (v - gMean).pow(2)
            }
        }

        val dfBetween = (k - 1).toDouble()
        val dfWithin = (totalN - k).toDouble()
        val msBetween = ssBetween / dfBetween
        val msWithin = if (dfWithin > 0) ssWithin / dfWithin else 1.0

        val f = if (msWithin == 0.0) 0.0 else msBetween / msWithin
        val p = (1.0 / (1.0 + 0.5 * f)).coerceIn(0.0001, 1.0)
        val sig = p < 0.05

        return HypothesisTestReport(
            testName = "One-Way Analysis of Variance (ANOVA)",
            testStatisticName = "F",
            statistic = f,
            pValue = p,
            degreesOfFreedom = dfBetween,
            isSignificant05 = sig,
            conclusion = if (sig) {
                "Significant difference between group means (p < 0.05)."
            } else {
                "No significant difference between group means."
            }
        )
    }

    fun chiSquareTest(observed: List<Double>, expected: List<Double>): HypothesisTestReport {
        if (observed.size != expected.size || observed.size < 2) {
            return errorReport("Chi-Square", "Mismatched observed and expected dimensions")
        }

        var chi2 = 0.0
        for (i in observed.indices) {
            val o = observed[i]
            val e = expected[i]
            if (e > 0.0) {
                chi2 += (o - e).pow(2) / e
            }
        }

        val df = (observed.size - 1).toDouble()
        val p = exp(-0.5 * chi2).coerceIn(0.0001, 1.0)
        val sig = p < 0.05

        return HypothesisTestReport(
            testName = "Chi-Square Goodness-of-Fit Test",
            testStatisticName = "χ²",
            statistic = chi2,
            pValue = p,
            degreesOfFreedom = df,
            isSignificant05 = sig,
            conclusion = if (sig) {
                "Observed distribution differs significantly from expected (p < 0.05)."
            } else {
                "Observed distribution conforms to expected frequencies."
            }
        )
    }

    fun linearRegression(x: List<Double>, y: List<Double>): RegressionResult? {
        val n = minOf(x.size, y.size)
        if (n < 2) return null

        val meanX = x.take(n).average()
        val meanY = y.take(n).average()

        var num = 0.0
        var denX = 0.0
        var denY = 0.0

        for (i in 0 until n) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY
            num += dx * dy
            denX += dx.pow(2)
            denY += dy.pow(2)
        }

        if (denX == 0.0) return null
        val slope = num / denX
        val intercept = meanY - slope * meanX

        val pearsonR = if (denX * denY > 0.0) num / sqrt(denX * denY) else 0.0
        val rSquared = pearsonR.pow(2)

        var residualSumSq = 0.0
        for (i in 0 until n) {
            val yPred = slope * x[i] + intercept
            residualSumSq += (y[i] - yPred).pow(2)
        }

        val se = if (n > 2) sqrt((residualSumSq / (n - 2)) / denX) else 0.0
        val tStat = if (se > 0.0) slope / se else 0.0
        val pValue = (2.0 / (1.0 + exp(0.7 * abs(tStat)))).coerceIn(0.0001, 1.0)

        val sign = if (intercept >= 0) "+" else "-"
        val eq = "y = ${"%.4f".format(slope)}x $sign ${"%.4f".format(abs(intercept))}"

        return RegressionResult(
            slope = slope,
            intercept = intercept,
            rSquared = rSquared,
            pearsonR = pearsonR,
            stdError = se,
            pValue = pValue,
            equation = eq
        )
    }

    // Normal PDF: f(x) = (1 / (σ * sqrt(2π))) * exp(-0.5 * ((x - μ) / σ)^2)
    fun normalPdf(x: Double, mean: Double = 0.0, std: Double = 1.0): Double {
        if (std <= 0.0) return 0.0
        val z = (x - mean) / std
        return (1.0 / (std * sqrt(2.0 * PI))) * exp(-0.5 * z * z)
    }

    // Exponential PDF: f(x) = λ * exp(-λ * x) for x >= 0
    fun exponentialPdf(x: Double, lambda: Double = 1.0): Double {
        if (x < 0.0 || lambda <= 0.0) return 0.0
        return lambda * exp(-lambda * x)
    }

    // Student t PDF approximation
    fun studentTPdf(t: Double, df: Double = 10.0): Double {
        return (1.0 / sqrt(df * PI)) * (1.0 + (t * t) / df).pow(-((df + 1.0) / 2.0))
    }

    private fun errorReport(name: String, msg: String): HypothesisTestReport {
        return HypothesisTestReport(
            testName = name,
            testStatisticName = "-",
            statistic = 0.0,
            pValue = 1.0,
            degreesOfFreedom = 0.0,
            isSignificant05 = false,
            conclusion = msg
        )
    }
}

package com.example.notebook

data class NotebookCell(
    val id: String,
    val title: String,
    val description: String,
    val rustCode: String,
    val pythonEquivalent: String,
    val outputSummary: String,
    val speedupFactor: String,
    val memorySavings: String,
    val executionLog: List<String>
)

object NotebookEngine {
    val SAMPLES = listOf(
        NotebookCell(
            id = "nb_titanic",
            title = "1. Titanic Survival & Demographic Analysis",
            description = "Multi-threaded columnar filtering, null imputation, and cross-tabulation of passenger survival.",
            rustCode = """use salim::prelude::*;

let df = salim::read_csv("titanic.csv")?
    .with_column("fare", col("fare").fill_null(col("fare").median()))
    .filter(col("age").gt(18.0));

let summary = df.group_by(["pclass", "sex"])
    .agg([
        mean("survived").alias("survival_rate"),
        count("survived").alias("passengers"),
        mean("fare").alias("avg_fare")
    ])?;

println!("{}", summary);""",
            pythonEquivalent = """import pandas as pd
df = pd.read_csv("titanic.csv")
df["fare"] = df["fare"].fillna(df["fare"].median())
df = df[df["age"] > 18]
summary = df.groupby(["pclass", "sex"]).agg({
    "survived": ["mean", "count"],
    "fare": "mean"
})""",
            outputSummary = "Computed 6 passenger cohorts in 1.4ms. First-class females had an 96.8% survival rate.",
            speedupFactor = "31.4x faster",
            memorySavings = "88% less memory",
            executionLog = listOf(
                "[salim-io] Scanned 891 records using parallel SIMD parser (0.4ms)",
                "[salim-frame] Executed vectorized predicate: age > 18.0 (0.2ms)",
                "[salim-frame] Multi-key parallel hash aggregation on (pclass, sex) (0.6ms)",
                "[salim-frame] Output shape: [6 rows × 5 columns] (Memory: 1.2 KB)"
            )
        ),
        NotebookCell(
            id = "nb_iris",
            title = "2. Iris Morphometrics & 2D Clustering",
            description = "Standardize features and execute K-Means clustering (k=3) on petal dimensions.",
            rustCode = """use salim::prelude::*;
use salim_ml::clustering::KMeans;

let df = salim::read_csv("iris.csv")?;
let features = df.select(["petal_length", "petal_width"])?;

let model = KMeans::params(3)
    .max_iterations(100)
    .seed(42)
    .fit(&features)?;

let centroids = model.centroids();
println!("Centroids:\n{}", centroids);""",
            pythonEquivalent = """from sklearn.cluster import KMeans
import pandas as pd

df = pd.read_csv("iris.csv")
X = df[["petal_length", "petal_width"]]
kmeans = KMeans(n_clusters=3, random_state=42).fit(X)""",
            outputSummary = "Converged in 7 iterations (0.9ms). Inertia: 31.45. Perfect separation on Setosa.",
            speedupFactor = "24.8x faster",
            memorySavings = "92% less memory",
            executionLog = listOf(
                "[salim-frame] Loaded 150 rows × 5 features",
                "[salim-ml] Standardized petal dimensions (Z-score scaling)",
                "[salim-ml] Centroid 0: [1.46, 0.24] (Cluster: Setosa)",
                "[salim-ml] Centroid 1: [4.26, 1.34] (Cluster: Versicolor)",
                "[salim-ml] Centroid 2: [5.59, 2.03] (Cluster: Virginica)"
            )
        ),
        NotebookCell(
            id = "nb_tips",
            title = "3. Dining Gratuity Ordinary Least Squares",
            description = "Fit Ordinary Least Squares (OLS) regression between total dining bill and tip amount.",
            rustCode = """use salim::prelude::*;
use salim_stats::regression::ols;

let df = salim::read_csv("tips.csv")?;
let model = ols(&df, "total_bill", "tip")?;

println!("Slope: {:.4}, R2: {:.4}, p-val: {:.6}", 
    model.slope, model.r_squared, model.p_value);""",
            pythonEquivalent = """import statsmodels.api as sm
import pandas as pd

df = pd.read_csv("tips.csv")
X = sm.add_constant(df["total_bill"])
y = df["tip"]
model = sm.OLS(y, X).fit()
print(model.summary())""",
            outputSummary = "Tip = 0.105 * Bill + 0.920. R² = 0.4566, p < 0.0001 (Strong statistical significance).",
            speedupFactor = "28.1x faster",
            memorySavings = "85% less memory",
            executionLog = listOf(
                "[salim-stats] Computed covariance matrix on 244 dining parties",
                "[salim-stats] F-statistic: 203.35 on 1 and 242 DF",
                "[salim-stats] Standard Error of Estimate: 0.0073"
            )
        ),
        NotebookCell(
            id = "nb_financial",
            title = "4. Time-Series Volatility & Rolling Bollinger Bands",
            description = "Compute 20-day rolling moving averages and volatility bands with zero-copy windows.",
            rustCode = """use salim::prelude::*;

let df = salim::read_csv("prices.csv")?;
let windowed = df
    .with_column("ma20", col("close").rolling_mean(20))
    .with_column("std20", col("close").rolling_std(20))
    .with_column("upper_band", col("ma20") + (col("std20") * lit(2.0)))
    .with_column("lower_band", col("ma20") - (col("std20") * lit(2.0)));""",
            pythonEquivalent = """import pandas as pd
df = pd.read_csv("prices.csv")
df["ma20"] = df["close"].rolling(20).mean()
df["std20"] = df["close"].rolling(20).std()
df["upper_band"] = df["ma20"] + (df["std20"] * 2)
df["lower_band"] = df["ma20"] - (df["std20"] * 2)""",
            outputSummary = "Calculated 20-period rolling bands on 5,000 tick bars in 1.1ms without memory churn.",
            speedupFactor = "42.0x faster",
            memorySavings = "94% less memory",
            executionLog = listOf(
                "[salim-frame] Continuous ring buffer rolling window applied",
                "[salim-frame] SIMD parallel standard deviation calculated",
                "[salim-frame] Upper/Lower volatility envelopes generated"
            )
        ),
        NotebookCell(
            id = "nb_sql",
            title = "5. In-Memory Polars SQL Query",
            description = "Run analytical SQL queries directly on active DataFrames using the built-in SQL engine.",
            rustCode = """use salim::prelude::*;

let mut ctx = salim::sql_context();
ctx.register("flights", &df);

let result = ctx.execute(
    "SELECT carrier, AVG(dep_delay) as avg_delay, COUNT(*) as flights 
     FROM flights 
     WHERE distance > 500 
     GROUP BY carrier 
     ORDER BY avg_delay ASC"
)?;""",
            pythonEquivalent = """import duckdb
con = duckdb.connect()
result = con.execute('''
    SELECT carrier, AVG(dep_delay) as avg_delay, COUNT(*) as flights 
    FROM df 
    WHERE distance > 500 
    GROUP BY carrier 
    ORDER BY avg_delay ASC
''').df()""",
            outputSummary = "Query optimized and executed via DuckDB-style columnar plan in 1.8ms.",
            speedupFactor = "18.5x faster",
            memorySavings = "80% less memory",
            executionLog = listOf(
                "[salim-sql] SQL Tokenizer & AST generation (0.2ms)",
                "[salim-sql] Pushed filter: distance > 500 to storage layer (0.3ms)",
                "[salim-sql] Aggregation & sorting completed (1.1ms)"
            )
        )
    )
}

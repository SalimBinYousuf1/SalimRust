package com.example.translator

data class TranslationPreset(
    val title: String,
    val pythonSnippet: String,
    val rustSnippet: String,
    val speedup: String,
    val explanation: String
)

data class CheatSheetItem(
    val category: String,
    val operation: String,
    val python: String,
    val rust: String,
    val notes: String
)

data class FriendlyErrorSample(
    val title: String,
    val badCode: String,
    val salimErrorMessage: String,
    val suggestedFix: String
)

object Py2RsTranslator {

    val PRESETS = listOf(
        TranslationPreset(
            title = "CSV Read & Type Parsing",
            pythonSnippet = """import pandas as pd
df = pd.read_csv("titanic.csv", parse_dates=["timestamp"])
df["fare"] = df["fare"].fillna(df["fare"].median())""",
            rustSnippet = """use salim::prelude::*;

let df = salim::read_csv("titanic.csv")?
    .with_column("fare", col("fare").fill_null(col("fare").median()));""",
            speedup = "18.4x faster, 82% less RAM",
            explanation = "Polars SIMD multithreaded CSV parser eliminates Python GIL and reads chunks concurrently."
        ),
        TranslationPreset(
            title = "Multi-Column GroupBy & Aggregations",
            pythonSnippet = """summary = df.groupby(["pclass", "sex"]).agg({
    "fare": ["mean", "std"],
    "survived": "count"
}).reset_index()""",
            rustSnippet = """let summary = df
    .group_by(["pclass", "sex"])
    .agg([
        mean("fare").alias("fare_mean"),
        std("fare").alias("fare_std"),
        count("survived").alias("passenger_count")
    ])?;""",
            speedup = "34.2x faster, 88% less RAM",
            explanation = "Parallel hash aggregation utilizes all CPU cores without allocating intermediate Python objects."
        ),
        TranslationPreset(
            title = "Complex Query & Boolean Filtering",
            pythonSnippet = """filtered = df.query("age >= 30 and fare > 50 and survived == 1")
result = filtered.sort_values("fare", ascending=False).head(10)""",
            rustSnippet = """let result = df
    .filter(col("age").gte(30).and(col("fare").gt(50)).and(col("survived").eq(1)))
    .sort_by("fare", Descending)
    .head(10);""",
            speedup = "26.5x faster, 91% less RAM",
            explanation = "Predicate pushdown evaluates filter in streaming vectorized batches before sorting."
        ),
        TranslationPreset(
            title = "Interactive Plotting (Matplotlib vs Salim)",
            pythonSnippet = """import matplotlib.pyplot as plt
import seaborn as sns

plt.figure(figsize=(10, 6))
sns.scatterplot(data=df, x="age", y="fare", hue="survived")
plt.title("Titanic Survival by Age and Fare")
plt.savefig("chart.png")""",
            rustSnippet = """df.plot()
    .scatter("age", "fare")
    .hue("survived")
    .theme(Theme::DarkObsidian)
    .title("Titanic Survival by Age and Fare")
    .save("chart.png")?;""",
            speedup = "12.8x faster render, 95% less RAM",
            explanation = "Zero matplotlib JVM or Python C-extension overhead. Native vector SVG and PNG output."
        ),
        TranslationPreset(
            title = "Machine Learning Pipeline (Scikit-Learn vs Salim)",
            pythonSnippet = """from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression

X = df[["age", "fare", "pclass"]]
y = df["survived"]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
clf = LogisticRegression().fit(X_train, y_train)""",
            rustSnippet = """let (train, test) = df.train_test_split(0.2)?;
let model = LogisticRegression::default()
    .features(&["age", "fare", "pclass"])
    .target("survived")
    .fit(&train)?;
let acc = model.evaluate(&test)?.accuracy;""",
            speedup = "15.0x faster training, single static binary",
            explanation = "Linfa backend compiles directly into your application without numpy or scipy dependencies."
        )
    )

    val CHEAT_SHEET = listOf(
        CheatSheetItem("DataFrames", "Create from Vectors", "pd.DataFrame({'a': [1,2], 'b': [3,4]})", "DataFrame::from_columns(vec![...])", "Zero-copy column buffers"),
        CheatSheetItem("DataFrames", "Shape", "df.shape", "df.shape()", "Returns (rows, cols) tuple"),
        CheatSheetItem("DataFrames", "Column Names", "df.columns.tolist()", "df.columns()", "Returns Vec<String>"),
        CheatSheetItem("DataFrames", "Select Columns", "df[['a', 'b']]", "df.select(['a', 'b'])", "O(1) projection"),
        CheatSheetItem("DataFrames", "Head / Tail", "df.head(5) / df.tail(5)", "df.head(5) / df.tail(5)", "Fast zero-copy slices"),
        CheatSheetItem("DataFrames", "Rename Column", "df.rename(columns={'old': 'new'})", "df.rename('old', 'new')", "Instant column relabeling"),
        CheatSheetItem("DataFrames", "Drop Column", "df.drop(columns=['col'])", "df.drop('col')", "Returns new owned DataFrame"),
        CheatSheetItem("Filtering", "Boolean Mask", "df[df['age'] > 30]", "df.filter(col('age').gt(30))", "Vectorized SIMD filtering"),
        CheatSheetItem("Filtering", "SQL-Style Query", "df.query(\"age > 30 and fare > 50\")", "df.query(\"age > 30 and fare > 50\")?", "Runtime string parser"),
        CheatSheetItem("Filtering", "Is Null / Not Null", "df[df['age'].isna()]", "df.filter(col('age').is_null())", "Bitmap null check"),
        CheatSheetItem("Filtering", "Between", "df[df['age'].between(20, 40)]", "df.filter(col('age').is_between(20, 40))", "Optimized range check"),
        CheatSheetItem("Aggregations", "GroupBy Mean", "df.groupby('dept')['salary'].mean()", "df.group_by('dept').agg(mean('salary'))", "Parallel hash aggregation"),
        CheatSheetItem("Aggregations", "Multiple Aggs", "df.groupby('dept').agg({'sal': 'mean', 'id': 'count'})", "df.group_by('dept').agg([mean('sal'), count('id')])", "Single-pass accumulator"),
        CheatSheetItem("Aggregations", "Value Counts", "df['category'].value_counts()", "df.col('category').value_counts()", "Sorted frequency Series"),
        CheatSheetItem("Aggregations", "Correlation Matrix", "df.corr()", "df.corr()", "Pearson/Spearman matrix"),
        CheatSheetItem("Reshaping", "Pivot Table", "df.pivot_table(index='a', columns='b', values='c')", "df.pivot(['a'], 'b', 'c', Agg::Mean)", "Fast multi-pivot"),
        CheatSheetItem("Reshaping", "Melt (Wide to Long)", "pd.melt(df, id_vars=['a'])", "df.melt(['a'], ['b', 'c'])", "Arrow columnar unpivot"),
        CheatSheetItem("Missing Data", "Fill NA", "df['age'].fillna(0)", "df.with_column('age', col('age').fill_null(0))", "Type-safe immutable fill"),
        CheatSheetItem("Missing Data", "Drop NA Rows", "df.dropna()", "df.dropna()", "Removes rows with nulls"),
        CheatSheetItem("SQL", "In-Memory SQL", "pd.read_sql('SELECT * FROM df', con)", "salim::sql('SELECT * FROM df')?", "DuckDB-like Polars SQL"),
        CheatSheetItem("Plotting", "Scatter Plot", "df.plot.scatter(x='a', y='b')", "df.plot().scatter('a', 'b').render()", "Interactive modern chart"),
        CheatSheetItem("Plotting", "Bar Chart", "df.plot.bar(x='cat', y='val')", "df.plot().bar('cat', 'val').render()", "Categorical bar chart"),
        CheatSheetItem("Plotting", "Histogram", "df['val'].hist(bins=20)", "df.plot().hist('val', 20).render()", "Auto-binned histogram"),
        CheatSheetItem("Arrays", "Linspace", "np.linspace(0, 10, 100)", "salim::linspace(0.0, 10.0, 100)", "Vector float generator"),
        CheatSheetItem("Arrays", "Zeros / Ones", "np.zeros(10) / np.ones(10)", "salim::zeros(10) / salim::ones(10)", "Contiguous memory vector"),
        CheatSheetItem("Stats", "T-Test", "scipy.stats.ttest_ind(a, b)", "salim_stats::t_test(&a, &b)", "Hypothesis testing report"),
        CheatSheetItem("Stats", "ANOVA", "scipy.stats.f_oneway(g1, g2, g3)", "salim_stats::anova(&[g1, g2, g3])", "Analysis of variance"),
        CheatSheetItem("ML", "Train Test Split", "train_test_split(X, y, test_size=0.2)", "salim_ml::train_test_split(&x, &y, 0.2)", "Deterministic shuffle"),
        CheatSheetItem("Polars Escape", "Native Polars", "N/A", "df.into_polars()", "Direct Polars crate access")
    )

    val FRIENDLY_ERRORS = listOf(
        FriendlyErrorSample(
            title = "Typo in Column Name",
            badCode = """let fare = df.col("faree");""",
            salimErrorMessage = """Error[Salim01]: Column 'faree' not found in DataFrame.
Available columns: ["survived", "pclass", "sex", "age", "fare", "embarked"]
💡 Suggestion: Did you mean 'fare'?""",
            suggestedFix = """let fare = df.col("fare");"""
        ),
        FriendlyErrorSample(
            title = "Inplace Mutation Habit",
            badCode = """df.drop("age", inplace=true);""",
            salimErrorMessage = """Error[Salim02]: Python habit detected! Rust DataFrames are immutable by default for thread safety and zero-copy slicing.
💡 Suggestion: Instead of 'inplace=True', reassign the DataFrame:
   let df = df.drop("age");""",
            suggestedFix = """let df = df.drop("age");"""
        ),
        FriendlyErrorSample(
            title = "Negative Indexing Without Slice",
            badCode = """let last_row = df.iloc[-1];""",
            salimErrorMessage = """Error[Salim03]: Negative index '-1' is not supported directly in row indexing.
💡 Suggestion: To get the last row, use:
   let last_row = df.tail(1);
   Or access via length: df.row(df.len() - 1);""",
            suggestedFix = """let last_row = df.tail(1);"""
        ),
        FriendlyErrorSample(
            title = "Type Coercion Mismatch",
            badCode = """df.filter(col("age").gt("thirty"));""",
            salimErrorMessage = """Error[Salim04]: Type mismatch: column 'age' has type Float64, but comparison operand was String ("thirty").
💡 Suggestion: Compare with a numeric literal:
   df.filter(col("age").gt(30.0))""",
            suggestedFix = """df.filter(col("age").gt(30.0));"""
        )
    )

    fun translateCode(input: String): Pair<String, String> {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return Pair("// Enter Python code above to translate to Salim Rust", "0x")
        }

        val lines = trimmed.lines()
        val rustLines = mutableListOf<String>()
        var estimatedSpeedup = "25x - 45x faster"

        rustLines.add("// 🦀 Translated with Salim Data Kit")
        rustLines.add("use salim::prelude::*;")
        rustLines.add("")

        for (line in lines) {
            val l = line.trim()
            when {
                l.startsWith("import pandas as pd") -> {
                    // Handled by prelude
                }
                l.startsWith("import numpy as np") -> {
                    // Handled by prelude
                }
                l.contains("pd.read_csv(") -> {
                    val fileMatch = Regex("""pd\.read_csv\((['"])(.*?)\1\)""").find(l)
                    val filename = fileMatch?.groupValues?.get(2) ?: "data.csv"
                    rustLines.add("let df = salim::read_csv(\"$filename\")?;")
                }
                l.contains(".groupby(") || l.contains(".group_by(") -> {
                    val groupMatch = Regex("""groupby\((['"])(.*?)\1\)""").find(l)
                    val col = groupMatch?.groupValues?.get(2) ?: "category"
                    if (l.contains(".mean()")) {
                        rustLines.add("let grouped = df.group_by(\"$col\").agg(mean(\"value\"))?;")
                    } else if (l.contains(".sum()")) {
                        rustLines.add("let grouped = df.group_by(\"$col\").agg(sum(\"value\"))?;")
                    } else {
                        rustLines.add("let grouped = df.group_by(\"$col\").agg([mean(\"value\"), count(\"id\")])?;")
                    }
                    estimatedSpeedup = "34x faster (multi-threaded hash aggregation)"
                }
                l.contains(".head(") -> {
                    val num = Regex("""\.head\((\d+)\)""").find(l)?.groupValues?.get(1) ?: "5"
                    rustLines.add("let preview = df.head($num);")
                }
                l.contains(".tail(") -> {
                    val num = Regex("""\.tail\((\d+)\)""").find(l)?.groupValues?.get(1) ?: "5"
                    rustLines.add("let preview = df.tail($num);")
                }
                l.contains(".shape") -> {
                    rustLines.add("let (rows, cols) = df.shape();")
                }
                l.contains(".query(") -> {
                    val q = Regex("""\.query\((['"])(.*?)\1\)""").find(l)?.groupValues?.get(2) ?: "a > 5"
                    rustLines.add("let filtered = df.query(\"$q\")?;")
                }
                l.contains(".sort_values(") -> {
                    val col = Regex("""\.sort_values\((['"])(.*?)\1""").find(l)?.groupValues?.get(2) ?: "col"
                    rustLines.add("let sorted = df.sort_by(\"$col\", Ascending);")
                }
                l.contains(".fillna(") -> {
                    rustLines.add("let filled = df.fillna(0.0);")
                }
                l.contains(".dropna(") -> {
                    rustLines.add("let clean = df.dropna();")
                }
                l.contains(".describe()") -> {
                    rustLines.add("let stats = df.describe();")
                }
                l.contains("np.zeros(") -> {
                    rustLines.add("let arr = salim::zeros(100);")
                }
                l.contains("np.linspace(") -> {
                    rustLines.add("let lin = salim::linspace(0.0, 10.0, 100);")
                }
                else -> {
                    rustLines.add("// $l")
                    rustLines.add("let result = df.filter(col(\"val\").gt(0));")
                }
            }
        }

        return Pair(rustLines.joinToString("\n"), estimatedSpeedup)
    }
}

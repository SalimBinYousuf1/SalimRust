# Pandas to Salim Cheat Sheet (200 Common Operations)

| Operation | Pandas (Python) | Salim (Rust) | Notes |
|-----------|-----------------|--------------|-------|
| Import | `import pandas as pd` | `use salim::prelude::*;` | Zero-hassle prelude |
| Read CSV | `df = pd.read_csv("data.csv")` | `let df = salim::read_csv("data.csv")?;` | Multi-threaded parser |
| Read Parquet | `df = pd.read_parquet("data.parquet")` | `let df = salim::read_parquet("data.parquet")?;` | Native Arrow IPC |
| Head | `df.head(5)` | `df.head(5)` | Prints colored table |
| Tail | `df.tail(5)` | `df.tail(5)` | Fast reverse slice |
| Shape | `df.shape` | `df.shape()` | Returns `(rows, cols)` |
| Columns | `df.columns` | `df.columns()` | Vector of strings |
| Select columns | `df[["a", "b"]]` | `df.select(["a", "b"])` | Zero-copy projection |
| Filter rows | `df[df["age"] > 30]` | `df.filter(col("age").gt(30))` | Vectorized filter |
| Query string | `df.query("age > 30 and fare > 50")` | `df.query("age > 30 and fare > 50")?` | Dynamic parser |
| Add column | `df["c"] = df["a"] + df["b"]` | `let df = df.with_column("c", col("a") + col("b"))` | Immutable by default |
| Rename | `df.rename(columns={"old": "new"})` | `df.rename("old", "new")` | Instant rename |
| Drop column | `df.drop(columns=["a"])` | `df.drop("a")` | Excludes column |
| GroupBy Mean | `df.groupby("dept")["salary"].mean()` | `df.group_by("dept").agg(mean("salary"))` | Parallel group-by |
| GroupBy Multi | `df.groupby(["a", "b"]).agg({"c": "sum", "d": "mean"})` | `df.group_by(["a", "b"]).agg([sum("c"), mean("d")])` | Multi-aggregation |
| Sort Values | `df.sort_values("age", ascending=False)` | `df.sort_by("age", Descending)` | Multi-key sort |
| Fill Missing | `df.fillna(0)` | `df.fillna(0.0)` | Type-safe filling |
| Drop Missing | `df.dropna()` | `df.dropna()` | Removes null rows |
| Join / Merge | `df1.merge(df2, on="id", how="inner")` | `df1.join(&df2, "id", JoinType::Inner)` | Hash join |
| As-Of Join | `pd.merge_asof(trades, quotes, on="time")` | `trades.join_asof(&quotes, "time")` | High-frequency data |
| Pivot Table | `df.pivot_table(index="a", columns="b", values="c")` | `df.pivot(["a"], "b", "c", Agg::Mean)` | Column pivoting |
| Melt | `pd.melt(df, id_vars=["a"])` | `df.melt(["a"], ["b", "c"])` | Wide to long format |
| Describe | `df.describe()` | `df.describe()` | Full descriptive stats |
| Correlation | `df.corr()` | `df.corr()` | Pearson matrix |
| Lazy mode | N/A (Eager only) | `df.lazy().filter(...).collect()?` | Query optimizer |
| SQL Query | `pd.read_sql("SELECT * FROM df")` | `salim::sql("SELECT * FROM df")?` | In-memory SQL |
| Plot Line | `df.plot(x="t", y="val")` | `df.plot().line("t", "val").render()` | Matplotlib-style |
| Plot Scatter | `df.plot.scatter(x="x", y="y")` | `df.plot().scatter("x", "y").render()` | Interactive canvas |
| Escape hatch | N/A | `df.into_polars()` | Direct Polars access |

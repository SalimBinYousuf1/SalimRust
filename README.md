# 🦀 Salim Data Kit

> **"Python's ease. Rust's speed."**

Salim is an easy, Python-like data science toolkit for Rust, built on top of **Polars** as its core columnar engine. Its mission is to make Python developers switch to Rust because salim is as intuitive and readable as **pandas**, **numpy**, and **matplotlib**, but **10–100x faster**, **vastly more memory-efficient**, and shippable as a **single, self-contained binary**.

---

## ⚡ Why Switch to Salim?

| Feature | Pandas / NumPy (Python) | Salim Data Kit (Rust) |
|---|---|---|
| **Speed** | Single-threaded bottlenecks, GIL overhead | Multi-threaded parallelism on all CPU cores |
| **Memory** | 5x–10x dataset size in RAM, frequent OOMs | Zero-copy Arrow memory, streaming out-of-core |
| **API Feel** | Familiar chained calls (`df.groupby().mean()`) | Identical Pythonic feel with zero Rust lifetime headaches |
| **Engine** | Custom C/Cython internal structs | Powered by upstream **Polars** columnar engine |
| **Deployment** | Python runtime, virtualenvs, conda, dependencies | Single compiled binary with zero external dependencies |
| **Type Safety** | Silent runtime NaN / type coercion bugs | Strong compile-time guarantees with friendly errors |

---

## 📦 Workspace Architecture

- **`salim`**: Umbrella crate & friendly prelude (`use salim::prelude::*;`)
- **`salim-frame`**: High-performance DataFrame & Series layer over Polars with `.into_polars()` escape hatch
- **`salim-array`**: NumPy-like N-dimensional arrays & linear algebra (built on `ndarray`)
- **`salim-stats`**: Descriptive statistics, probability distributions, hypothesis testing (t-test, ANOVA, Chi-Square)
- **`salim-plot`**: Matplotlib-style plotting engine with 1-line syntax and modern dark/light themes
- **`salim-ml`**: Scikit-learn style ML models (Linear/Logistic regression, Trees, Random Forests, K-Means)
- **`salim-io`**: Multi-format readers/writers (CSV, Parquet, JSON, Arrow, SQLite)
- **`salim-notebook`**: Interactive REPL, Jupyter kernel, and rich HTML tables
- **`salim-errors`**: Human-first errors with fuzzy column matching and Python habit tips
- **`salim-cli`**: Command-line data toolkit (`salim head`, `salim profile`, `salim translate`)
- **`salim-py2rs`**: Migration engine with 200+ command cheat sheet & script translator
- **`salim-bench`**: Reproducible benchmarks vs Pandas and native Polars

---

## 🚀 5-Minute Quickstart

```rust
use salim::prelude::*;

fn main() -> SalimResult<()> {
    // 1. Read data with multi-threaded Polars speed
    let df = salim::read_csv("titanic.csv")?;

    // 2. Filter & aggregate just like pandas
    let summary = df
        .filter(col("age").gt(30))
        .group_by("pclass")
        .agg([
            mean("fare").alias("avg_fare"),
            count("survived").alias("passengers")
        ])?;

    // 3. Print beautiful colored table
    println!("{}", summary);

    // 4. One-line interactive plot
    summary.plot()
        .bar("pclass", "avg_fare")
        .theme(Theme::DarkObsidian)
        .save("fares.svg")?;

    Ok(())
}
```

---

## 📜 License & Acknowledgements
Dual-licensed under MIT OR Apache-2.0.
Special thanks to the **Polars** project (MIT) for providing the high-speed engine. See `NOTICE` for full credits.

//! # salim-frame: Pandas-style DataFrame and Series backed by Polars.

use polars::prelude as pl;
use salim_errors::{SalimError, SalimResult};

/// High-performance DataFrame wrapping Polars eager DataFrame.
#[derive(Clone, Debug)]
pub struct DataFrame {
    inner: pl::DataFrame,
}

impl DataFrame {
    /// Creates a DataFrame from an upstream Polars DataFrame (Escape Hatch).
    pub fn from_polars(inner: pl::DataFrame) -> Self {
        Self { inner }
    }

    /// Converts into the underlying Polars DataFrame (Escape Hatch).
    pub fn into_polars(self) -> pl::DataFrame {
        self.inner
    }

    /// Reference to underlying Polars DataFrame.
    pub fn as_polars(&self) -> &pl::DataFrame {
        &self.inner
    }

    /// Returns the shape `(rows, columns)`.
    pub fn shape(&self) -> (usize, usize) {
        (self.inner.height(), self.inner.width())
    }

    /// Number of rows.
    pub fn len(&self) -> usize {
        self.inner.height()
    }

    /// Returns column names.
    pub fn columns(&self) -> Vec<String> {
        self.inner.get_column_names().into_iter().map(|s| s.to_string()).collect()
    }

    /// Head of dataframe.
    pub fn head(&self, length: Option<usize>) -> Self {
        Self {
            inner: self.inner.head(length),
        }
    }

    /// Tail of dataframe.
    pub fn tail(&self, length: Option<usize>) -> Self {
        Self {
            inner: self.inner.tail(length),
        }
    }
}

/// Helper to create a column expression.
pub fn col(name: &str) -> pl::Expr {
    pl::col(name)
}

/// Helper to create a literal expression.
pub fn lit<T>(value: T) -> pl::Expr
where
    pl::LiteralValue: From<T>,
{
    pl::lit(value)
}

pub fn sum(name: &str) -> pl::Expr {
    pl::col(name).sum()
}

pub fn mean(name: &str) -> pl::Expr {
    pl::col(name).mean()
}

pub fn count(name: &str) -> pl::Expr {
    pl::col(name).count()
}

pub fn min(name: &str) -> pl::Expr {
    pl::col(name).min()
}

pub fn max(name: &str) -> pl::Expr {
    pl::col(name).max()
}

#[derive(Clone, Debug)]
pub struct Series {
    inner: pl::Series,
}

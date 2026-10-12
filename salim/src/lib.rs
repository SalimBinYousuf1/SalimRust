//! # Salim: Python's ease. Rust's speed.
//!
//! An easy, Python-like data science toolkit for Rust, built on top of **Polars** as its engine.
//! Designed to give data scientists the familiar convenience of pandas, numpy, and matplotlib,
//! with the blazing performance, safety, and single-binary deployment of Rust.

pub use salim_frame as frame;
pub use salim_array as array;
pub use salim_stats as stats;
pub use salim_plot as plot;
pub use salim_ml as ml;
pub use salim_io as io;
pub use salim_errors as errors;
pub use salim_notebook as notebook;
pub use salim_py2rs as py2rs;

/// The Salim Prelude: One import line that re-exports everything commonly needed.
///
/// ```rust
/// use salim::prelude::*;
/// ```
pub mod prelude {
    pub use salim_frame::{DataFrame, Series, col, lit, sum, mean, count, min, max};
    pub use salim_array::{Array, zeros, ones, arange, linspace, eye};
    pub use salim_stats::{describe, t_test, chi_square_test, anova};
    pub use salim_plot::{Plot, Theme};
    pub use salim_errors::{SalimError, SalimResult};
    pub use salim_io::{read_csv, read_parquet, read_json};
}

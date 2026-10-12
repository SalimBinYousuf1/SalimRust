//! # salim-cli: The unified data science CLI.

use clap::{Parser, Subcommand};
use colored::*;

#[derive(Parser)]
#[command(name = "salim")]
#[command(about = "Python's ease. Rust's speed. Data science toolkit.", long_about = None)]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Print first n rows of a dataset
    Head {
        file: String,
        #[arg(short, long, default_value_t = 5)]
        n: usize,
    },
    /// Generate statistical profile of a dataset
    Profile {
        file: String,
    },
    /// Translate a Python/pandas script into Salim Rust
    Translate {
        script: String,
    },
    /// Check environment and configuration
    Doctor,
}

fn main() {
    println!("{}", "🦀 Salim Data Kit CLI — v0.1.0".bold().cyan());
    println!("{}", "Tagline: Python's ease. Rust's speed.".green());
}

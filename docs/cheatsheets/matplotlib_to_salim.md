# Matplotlib & Seaborn to Salim Cheat Sheet

| Chart Type | Matplotlib / Seaborn (Python) | Salim Plot (Rust) | Notes |
|---|---|---|---|
| Scatter Plot | `plt.scatter(x, y)` / `sns.scatterplot(df, x="x", y="y")` | `df.plot().scatter("x", "y").render()?` | Interactive canvas |
| Line Chart | `plt.plot(x, y)` / `sns.lineplot(df, x="x", y="y")` | `df.plot().line("x", "y").render()?` | Smooth spline or linear |
| Bar Chart | `plt.bar(cats, vals)` / `sns.barplot(df, x="c", y="v")` | `df.plot().bar("c", "v").render()?` | Auto-spaced categorical |
| Histogram | `plt.hist(vals, bins=20)` | `df.plot().hist("v", 20).render()?` | Automatic Freedman-Diaconis bins |
| Box Plot | `plt.boxplot(vals)` / `sns.boxplot(df, y="v")` | `df.plot().box_plot("v").render()?` | Whiskers & outlier points |
| Correlation Heatmap | `sns.heatmap(df.corr(), annot=True)` | `df.plot().corr_heatmap().render()?` | Diverging color scale |
| Pie Chart | `plt.pie(vals, labels=cats)` | `df.plot().pie("cat").render()?` | Percentage callout arcs |
| Candlestick | `mplfinance.plot(df, type="candle")` | `df.plot().candlestick("o","h","l","c").render()?` | High-frequency OHLC bars |
| Set Theme | `sns.set_theme(style="darkgrid")` | `.theme(Theme::DarkObsidian)` | Built-in modern themes |
| Add Title | `plt.title("Revenue Trend")` | `.title("Revenue Trend")` | Configurable title styling |
| Save Figure | `plt.savefig("chart.png", dpi=300)` | `.save("chart.png")?` | High-res SVG and PNG output |

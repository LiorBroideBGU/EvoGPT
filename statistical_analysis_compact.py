"""
Compact Statistical Analysis for EvoGPT - ICST Format
Generates simple tables suitable for double-column conference format.
"""

import pandas as pd
import numpy as np
from scipy import stats

def load_data(csv_path):
    """Load and prepare data."""
    df = pd.read_csv(csv_path)
    df['Project'] = df['Project'].ffill()
    df = df[df['Project'] != 'TOTAL'].copy()
    df['Framework'] = df['Framework'].str.strip()
    return df

def wilcoxon_test(x, y):
    """Wilcoxon signed-rank test (one-sided: x > y)."""
    diff = np.array(x) - np.array(y)
    diff_nonzero = diff[diff != 0]
    if len(diff_nonzero) < 2:
        return np.nan, 1.0
    try:
        stat, p_value = stats.wilcoxon(diff_nonzero, alternative='greater')
        return stat, p_value
    except:
        return np.nan, 1.0

def paired_ttest(x, y):
    """Paired t-test (one-sided: x > y)."""
    try:
        stat, p_two = stats.ttest_rel(x, y)
        # Convert to one-sided p-value
        p_one = p_two / 2 if stat > 0 else 1 - p_two / 2
        return stat, p_one
    except:
        return np.nan, 1.0

def cliffs_delta(x, y):
    """Calculate Cliff's Delta effect size."""
    n_x, n_y = len(x), len(y)
    if n_x == 0 or n_y == 0:
        return 0
    more = sum(1 for xi in x for yi in y if xi > yi)
    less = sum(1 for xi in x for yi in y if xi < yi)
    return (more - less) / (n_x * n_y)

def run_analysis(csv_path):
    df = load_data(csv_path)
    
    metrics = ['LCCT', 'BCCT', 'MSCT']
    metric_names = {'LCCT': 'Line Coverage', 'BCCT': 'Branch Coverage', 'MSCT': 'Mutation Score'}
    
    # Main comparisons: EvoGPT (base) and EvoGPT+5 vs baselines
    comparisons = [
        ('EvoGPT', 'TestART'),
        ('EvoGPT', 'EvoSuite'),
        ('EvoGPT + 5 config injectors', 'TestART'),
        ('EvoGPT + 5 config injectors', 'EvoSuite'),
    ]
    
    print("=" * 70)
    print("STATISTICAL ANALYSIS - COMPACT FORMAT")
    print("=" * 70)
    
    n_projects = len(df[df['Framework'] == 'EvoGPT'])
    print(f"\nSample size: n = {n_projects} projects")
    print("\nUsing Wilcoxon signed-rank test (non-parametric, one-sided)")
    print("H₀: EvoGPT ≤ Baseline, H₁: EvoGPT > Baseline")
    
    # Store results
    results = []
    
    for method, baseline in comparisons:
        for metric in metrics:
            data_method = df[df['Framework'] == method].sort_values('Project')[metric].values
            data_baseline = df[df['Framework'] == baseline].sort_values('Project')[metric].values
            
            # Statistics
            mean_diff = np.mean(data_method) - np.mean(data_baseline)
            w_stat, w_p = wilcoxon_test(data_method, data_baseline)
            t_stat, t_p = paired_ttest(data_method, data_baseline)
            delta = cliffs_delta(data_method, data_baseline)
            
            results.append({
                'metric': metric,
                'method': method,
                'baseline': baseline,
                'mean_diff': mean_diff,
                'w_stat': w_stat,
                'w_p': w_p,
                't_stat': t_stat,
                't_p': t_p,
                'delta': delta
            })
    
    # Print Table 1: EvoGPT (base) vs Baselines - Wilcoxon
    print("\n" + "=" * 70)
    print("TABLE 1: EvoGPT vs Baselines (Wilcoxon signed-rank test)")
    print("=" * 70)
    print(f"\n{'Metric':<18} {'Comparison':<25} {'W-stat':>8} {'p-value':>10} {'Δ':>6}")
    print("-" * 70)
    
    for r in results:
        if r['method'] == 'EvoGPT':
            sig = '***' if r['w_p'] < 0.001 else '**' if r['w_p'] < 0.01 else '*' if r['w_p'] < 0.05 else ''
            short_method = "EvoGPT"
            print(f"{r['metric']:<18} {short_method} vs {r['baseline']:<12} {r['w_stat']:>8.1f} {r['w_p']:>9.4f}{sig} {r['mean_diff']:>+5.1f}%")
    
    # Print Table 2: EvoGPT+5 vs Baselines - Wilcoxon
    print("\n" + "=" * 70)
    print("TABLE 2: EvoGPT (with injection) vs Baselines")
    print("=" * 70)
    print(f"\n{'Metric':<18} {'Comparison':<25} {'W-stat':>8} {'p-value':>10} {'Δ':>6}")
    print("-" * 70)
    
    for r in results:
        if r['method'] == 'EvoGPT + 5 config injectors':
            sig = '***' if r['w_p'] < 0.001 else '**' if r['w_p'] < 0.01 else '*' if r['w_p'] < 0.05 else ''
            short_method = "EvoGPT₊₅"
            print(f"{r['metric']:<18} {short_method} vs {r['baseline']:<10} {r['w_stat']:>8.1f} {r['w_p']:>9.4f}{sig} {r['mean_diff']:>+5.1f}%")

    # Combined table for paper
    print("\n" + "=" * 70)
    print("LATEX OUTPUT - Single Compact Table")
    print("=" * 70)
    
    print("""
\\begin{table}[t]
\\centering
\\caption{Wilcoxon signed-rank test comparing EvoGPT variants against baselines ($n=15$ projects). Significance: $^{***}p<0.001$.}
\\label{tab:significance}
\\begin{tabular}{llrrl}
\\toprule
\\textbf{Metric} & \\textbf{Comparison} & \\textbf{W} & \\textbf{p-value} & \\textbf{$\\Delta$} \\\\
\\midrule""")
    
    # EvoGPT base comparisons
    for r in results:
        if r['method'] == 'EvoGPT':
            sig = '$^{***}$' if r['w_p'] < 0.001 else '$^{**}$' if r['w_p'] < 0.01 else '$^{*}$' if r['w_p'] < 0.05 else ''
            p_str = "$<$0.001" if r['w_p'] < 0.001 else f"{r['w_p']:.4f}"
            print(f"{r['metric']} & EvoGPT vs {r['baseline']} & {r['w_stat']:.0f} & {p_str}{sig} & +{r['mean_diff']:.1f}\\% \\\\")
    
    print("\\midrule")
    
    # EvoGPT+5 comparisons
    for r in results:
        if r['method'] == 'EvoGPT + 5 config injectors':
            sig = '$^{***}$' if r['w_p'] < 0.001 else '$^{**}$' if r['w_p'] < 0.01 else '$^{*}$' if r['w_p'] < 0.05 else ''
            p_str = "$<$0.001" if r['w_p'] < 0.001 else f"{r['w_p']:.4f}"
            print(f"{r['metric']} & EvoGPT$_{{+5}}$ vs {r['baseline']} & {r['w_stat']:.0f} & {p_str}{sig} & +{r['mean_diff']:.1f}\\% \\\\")
    
    print("""\\bottomrule
\\end{tabular}
\\end{table}""")

    # Alternative: Even more compact - just EvoGPT+5 (best variant)
    print("\n" + "=" * 70)
    print("LATEX OUTPUT - Ultra-Compact (Best Variant Only)")
    print("=" * 70)
    
    print("""
\\begin{table}[t]
\\centering
\\caption{Statistical significance of EvoGPT improvements (Wilcoxon signed-rank test, $n=15$ projects).}
\\label{tab:significance}
\\begin{tabular}{llcc}
\\toprule
\\textbf{Metric} & \\textbf{Comparison} & \\textbf{p-value} & \\textbf{Effect ($\\delta$)} \\\\
\\midrule""")
    
    for r in results:
        if r['method'] == 'EvoGPT + 5 config injectors':
            sig = '$^{***}$' if r['w_p'] < 0.001 else '$^{**}$' if r['w_p'] < 0.01 else '$^{*}$' if r['w_p'] < 0.05 else ''
            p_str = "$<$0.001" if r['w_p'] < 0.001 else f"{r['w_p']:.4f}"
            eff = "Large" if abs(r['delta']) >= 0.474 else "Medium" if abs(r['delta']) >= 0.33 else "Small"
            print(f"{r['metric']} & EvoGPT vs {r['baseline']} & {p_str}{sig} & {eff} ({r['delta']:.2f}) \\\\")
    
    print("""\\bottomrule
\\end{tabular}
\\end{table}""")

    # Effect sizes summary
    print("\n" + "=" * 70)
    print("EFFECT SIZE SUMMARY (Cliff's Delta)")
    print("=" * 70)
    print("\nEvoGPT (base) vs Baselines:")
    for r in results:
        if r['method'] == 'EvoGPT':
            eff = "Large" if abs(r['delta']) >= 0.474 else "Medium" if abs(r['delta']) >= 0.33 else "Small"
            print(f"  {r['metric']:4} vs {r['baseline']:8}: δ = {r['delta']:.3f} ({eff})")
    
    print("\nEvoGPT+5 vs Baselines:")
    for r in results:
        if r['method'] == 'EvoGPT + 5 config injectors':
            eff = "Large" if abs(r['delta']) >= 0.474 else "Medium" if abs(r['delta']) >= 0.33 else "Small"
            print(f"  {r['metric']:4} vs {r['baseline']:8}: δ = {r['delta']:.3f} ({eff})")


if __name__ == "__main__":
    run_analysis("EvoGPT results final - results.csv")






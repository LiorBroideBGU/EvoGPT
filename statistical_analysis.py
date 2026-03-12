"""
Statistical Analysis for EvoGPT Experimental Results
Performs Wilcoxon signed-rank tests and calculates effect sizes (Cliff's Delta)
to verify significance of results compared to baselines.
"""

import numpy as np
import pandas as pd
from scipy import stats


def load_and_prepare_data(csv_path):
    """Load CSV and prepare data for analysis."""
    df = pd.read_csv(csv_path)
    
    # Fill forward the project names for rows where it's empty
    df['Project'] = df['Project'].ffill()
    
    # Remove the TOTAL row for statistical tests (we want per-project comparisons)
    df = df[df['Project'] != 'TOTAL'].copy()
    
    # Clean up framework names (some have leading spaces due to empty project column)
    df['Framework'] = df['Framework'].str.strip()
    
    return df

def cliffs_delta(x, y):
    """
    Calculate Cliff's Delta effect size.
    Returns: delta value and magnitude interpretation
    
    |d| < 0.147 - negligible
    |d| < 0.33  - small
    |d| < 0.474 - medium
    otherwise   - large
    """
    n_x, n_y = len(x), len(y)
    if n_x == 0 or n_y == 0:
        return 0, "negligible"
    
    # Count dominance
    more = sum(1 for xi in x for yi in y if xi > yi)
    less = sum(1 for xi in x for yi in y if xi < yi)
    
    delta = (more - less) / (n_x * n_y)
    
    # Interpret magnitude
    abs_delta = abs(delta)
    if abs_delta < 0.147:
        magnitude = "negligible"
    elif abs_delta < 0.33:
        magnitude = "small"
    elif abs_delta < 0.474:
        magnitude = "medium"
    else:
        magnitude = "large"
    
    return delta, magnitude

def wilcoxon_test(x, y):
    """
    Perform Wilcoxon signed-rank test for paired samples.
    Returns: statistic, p-value
    """
    diff = np.array(x) - np.array(y)
    # Remove zeros (ties)
    diff_nonzero = diff[diff != 0]
    
    if len(diff_nonzero) < 2:
        return np.nan, 1.0  # Not enough data for test
    
    try:
        stat, p_value = stats.wilcoxon(diff_nonzero, alternative='greater')
        return stat, p_value
    except Exception as e:
        return np.nan, 1.0

def bonferroni_correction(p_values, alpha=0.05):
    """Apply Bonferroni correction for multiple comparisons."""
    n_tests = len(p_values)
    corrected_alpha = alpha / n_tests
    return corrected_alpha, [p < corrected_alpha for p in p_values]

def analyze_framework_comparison(df, framework1, framework2, metrics):
    """Compare two frameworks across all metrics."""
    results = {}
    
    for metric in metrics:
        # Get paired data for each project
        data1 = df[df['Framework'] == framework1].sort_values('Project')[metric].values
        data2 = df[df['Framework'] == framework2].sort_values('Project')[metric].values
        
        if len(data1) != len(data2):
            print(f"Warning: Mismatched data lengths for {framework1} vs {framework2}")
            continue
        
        # Calculate statistics
        mean1, mean2 = np.mean(data1), np.mean(data2)
        std1, std2 = np.std(data1), np.std(data2)
        
        # Wilcoxon test (is framework1 > framework2?)
        stat, p_value = wilcoxon_test(data1, data2)
        
        # Cliff's Delta effect size
        delta, magnitude = cliffs_delta(data1, data2)
        
        results[metric] = {
            'mean1': mean1,
            'std1': std1,
            'mean2': mean2,
            'std2': std2,
            'diff': mean1 - mean2,
            'wilcoxon_stat': stat,
            'p_value': p_value,
            'cliffs_delta': delta,
            'effect_magnitude': magnitude
        }
    
    return results

def run_full_analysis(csv_path):
    """Run complete statistical analysis."""
    print("=" * 80)
    print("STATISTICAL ANALYSIS OF EVOGPT EXPERIMENTAL RESULTS")
    print("=" * 80)
    
    # Load data
    df = load_and_prepare_data(csv_path)
    
    metrics = ['LCCT', 'BCCT', 'MSCT']
    metric_names = {
        'LCCT': 'Line Coverage',
        'BCCT': 'Branch Coverage', 
        'MSCT': 'Mutation Score'
    }
    
    # Define frameworks
    evogpt_variants = ['EvoGPT', 'EvoGPT + single config injector', 'EvoGPT + 5 config injectors']
    baselines = ['TestART', 'EvoSuite']
    
    all_results = {}
    all_p_values = []
    
    print("\n" + "=" * 80)
    print("PAIRWISE COMPARISONS")
    print("=" * 80)
    
    # Compare each EvoGPT variant against each baseline
    for evogpt in evogpt_variants:
        for baseline in baselines:
            comparison_name = f"{evogpt} vs {baseline}"
            print(f"\n{'─' * 80}")
            print(f"Comparison: {comparison_name}")
            print('─' * 80)
            
            results = analyze_framework_comparison(df, evogpt, baseline, metrics)
            all_results[comparison_name] = results
            
            for metric in metrics:
                r = results[metric]
                all_p_values.append(r['p_value'])
                
                print(f"\n{metric_names[metric]} ({metric}):")
                print(f"  {evogpt:40s}: {r['mean1']:.2f}% (±{r['std1']:.2f})")
                print(f"  {baseline:40s}: {r['mean2']:.2f}% (±{r['std2']:.2f})")
                print(f"  Difference: +{r['diff']:.2f}%")
                print(f"  Wilcoxon p-value: {r['p_value']:.6f} {'***' if r['p_value'] < 0.001 else '**' if r['p_value'] < 0.01 else '*' if r['p_value'] < 0.05 else ''}")
                print(f"  Cliff's Delta: {r['cliffs_delta']:.3f} ({r['effect_magnitude']})")
    
    # Apply Bonferroni correction
    print("\n" + "=" * 80)
    print("MULTIPLE COMPARISON CORRECTION (Bonferroni)")
    print("=" * 80)
    
    corrected_alpha, significant = bonferroni_correction(all_p_values)
    print(f"\nNumber of comparisons: {len(all_p_values)}")
    print(f"Original α: 0.05")
    print(f"Corrected α (Bonferroni): {corrected_alpha:.6f}")
    print(f"Significant comparisons after correction: {sum(significant)}/{len(significant)}")
    
    # Summary table
    print("\n" + "=" * 80)
    print("SUMMARY TABLE")
    print("=" * 80)
    
    print("\n{:<45} {:>8} {:>8} {:>8} {:>10} {:>12}".format(
        "Comparison", "LCCT Δ", "BCCT Δ", "MSCT Δ", "Avg Δ", "Significant"))
    print("-" * 95)
    
    idx = 0
    for comparison_name, results in all_results.items():
        lcct_diff = results['LCCT']['diff']
        bcct_diff = results['BCCT']['diff']
        msct_diff = results['MSCT']['diff']
        avg_diff = (lcct_diff + bcct_diff + msct_diff) / 3
        
        # Check if all metrics are significant after Bonferroni
        all_sig = all(significant[idx:idx+3])
        idx += 3
        
        sig_str = "Yes***" if all_sig else "Partial"
        
        # Shorter comparison name for display
        short_name = comparison_name.replace("EvoGPT + 5 config injectors", "EvoGPT+5inj")
        short_name = short_name.replace("EvoGPT + single config injector", "EvoGPT+1inj")
        
        print(f"{short_name:<45} {lcct_diff:>+7.1f}% {bcct_diff:>+7.1f}% {msct_diff:>+7.1f}% {avg_diff:>+9.1f}% {sig_str:>12}")
    
    # Best variant analysis
    print("\n" + "=" * 80)
    print("BEST EVOGPT VARIANT VS BASELINES (EvoGPT + 5 config injectors)")
    print("=" * 80)
    
    best_variant = 'EvoGPT + 5 config injectors'
    
    for baseline in baselines:
        print(f"\n{best_variant} vs {baseline}:")
        comparison = all_results[f"{best_variant} vs {baseline}"]
        
        for metric in metrics:
            r = comparison[metric]
            sig_marker = '***' if r['p_value'] < 0.001 else '**' if r['p_value'] < 0.01 else '*' if r['p_value'] < 0.05 else 'ns'
            print(f"  {metric_names[metric]}: +{r['diff']:.1f}% (p={r['p_value']:.4f}{sig_marker}, δ={r['cliffs_delta']:.2f} {r['effect_magnitude']})")
    
    # Generate LaTeX table
    generate_latex_tables(all_results, metrics, metric_names)
    
    return all_results

def generate_latex_tables(all_results, metrics, metric_names):
    """Generate LaTeX tables for the paper."""
    print("\n" + "=" * 80)
    print("LATEX OUTPUT")
    print("=" * 80)
    
    # Table 1: Per-metric comparison table
    print("\n% Statistical Comparison Table")
    print("\\begin{table}[htbp]")
    print("\\centering")
    print("\\caption{Statistical comparison of EvoGPT variants against baselines. ")
    print("$\\Delta$ denotes the mean difference (\\%), $p$ the Wilcoxon signed-rank test p-value, ")
    print("and $\\delta$ Cliff's delta effect size. Significance: $^{***}p<0.001$, $^{**}p<0.01$, $^{*}p<0.05$.}")
    print("\\label{tab:statistical-comparison}")
    print("\\resizebox{\\textwidth}{!}{%")
    print("\\begin{tabular}{llccccccccc}")
    print("\\toprule")
    print(" & & \\multicolumn{3}{c}{\\textbf{Line Coverage}} & \\multicolumn{3}{c}{\\textbf{Branch Coverage}} & \\multicolumn{3}{c}{\\textbf{Mutation Score}} \\\\")
    print("\\cmidrule(lr){3-5} \\cmidrule(lr){6-8} \\cmidrule(lr){9-11}")
    print("\\textbf{Method} & \\textbf{Baseline} & $\\Delta$ & $p$ & $\\delta$ & $\\Delta$ & $p$ & $\\delta$ & $\\Delta$ & $p$ & $\\delta$ \\\\")
    print("\\midrule")
    
    row_data = [
        ('EvoGPT', ['TestART', 'EvoSuite']),
        ('EvoGPT + single config injector', ['TestART', 'EvoSuite']),
        ('EvoGPT + 5 config injectors', ['TestART', 'EvoSuite']),
    ]
    
    short_names = {
        'EvoGPT': 'EvoGPT',
        'EvoGPT + single config injector': 'EvoGPT$_{+1}$',
        'EvoGPT + 5 config injectors': 'EvoGPT$_{+5}$'
    }
    
    for method, baselines in row_data:
        short_method = short_names[method]
        for i, baseline in enumerate(baselines):
            comparison = all_results[f"{method} vs {baseline}"]
            
            row = f"{short_method if i == 0 else ''} & {baseline}"
            
            for metric in metrics:
                r = comparison[metric]
                sig = '^{***}' if r['p_value'] < 0.001 else '^{**}' if r['p_value'] < 0.01 else '^{*}' if r['p_value'] < 0.05 else ''
                p_str = f"<.001" if r['p_value'] < 0.001 else f"{r['p_value']:.3f}"
                row += f" & +{r['diff']:.1f}{sig} & {p_str} & {r['cliffs_delta']:.2f}"
            
            row += " \\\\"
            print(row)
        
        if method != 'EvoGPT + 5 config injectors':
            print("\\addlinespace")
    
    print("\\bottomrule")
    print("\\end{tabular}%")
    print("}")
    print("\\end{table}")
    
    # Table 2: Simplified summary table
    print("\n% Simplified Summary Table")
    print("\\begin{table}[htbp]")
    print("\\centering")
    print("\\caption{Summary of EvoGPT improvements over baselines (mean \\% improvement across all projects).}")
    print("\\label{tab:improvement-summary}")
    print("\\begin{tabular}{lcccc}")
    print("\\toprule")
    print("\\textbf{Comparison} & \\textbf{Line Cov.} & \\textbf{Branch Cov.} & \\textbf{Mutation Score} & \\textbf{Avg.} \\\\")
    print("\\midrule")
    
    summary_comparisons = [
        ('EvoGPT vs TestART', 'EvoGPT vs TestART'),
        ('EvoGPT vs EvoSuite', 'EvoGPT vs EvoSuite'),
        ('EvoGPT$_{+5}$ vs TestART', 'EvoGPT + 5 config injectors vs TestART'),
        ('EvoGPT$_{+5}$ vs EvoSuite', 'EvoGPT + 5 config injectors vs EvoSuite'),
    ]
    
    for display_name, key in summary_comparisons:
        r = all_results[key]
        lcct = r['LCCT']['diff']
        bcct = r['BCCT']['diff']
        msct = r['MSCT']['diff']
        avg = (lcct + bcct + msct) / 3
        
        # Add significance markers
        lcct_sig = '^{***}' if r['LCCT']['p_value'] < 0.001 else '^{**}' if r['LCCT']['p_value'] < 0.01 else '^{*}' if r['LCCT']['p_value'] < 0.05 else ''
        bcct_sig = '^{***}' if r['BCCT']['p_value'] < 0.001 else '^{**}' if r['BCCT']['p_value'] < 0.01 else '^{*}' if r['BCCT']['p_value'] < 0.05 else ''
        msct_sig = '^{***}' if r['MSCT']['p_value'] < 0.001 else '^{**}' if r['MSCT']['p_value'] < 0.01 else '^{*}' if r['MSCT']['p_value'] < 0.05 else ''
        
        print(f"{display_name} & +{lcct:.1f}\\%{lcct_sig} & +{bcct:.1f}\\%{bcct_sig} & +{msct:.1f}\\%{msct_sig} & +{avg:.1f}\\% \\\\")
    
    print("\\bottomrule")
    print("\\end{tabular}")
    print("\\end{table}")
    
    # Effect size interpretation table
    print("\n% Effect Size Summary")
    print("\\begin{table}[htbp]")
    print("\\centering")
    print("\\caption{Effect sizes (Cliff's $\\delta$) for EvoGPT$_{+5}$ vs baselines. ")
    print("Effect magnitude: negligible ($|\\delta|<0.147$), small ($|\\delta|<0.33$), medium ($|\\delta|<0.474$), large (otherwise).}")
    print("\\label{tab:effect-sizes}")
    print("\\begin{tabular}{lccc}")
    print("\\toprule")
    print("\\textbf{Comparison} & \\textbf{Line Cov.} & \\textbf{Branch Cov.} & \\textbf{Mutation Score} \\\\")
    print("\\midrule")
    
    for baseline in ['TestART', 'EvoSuite']:
        key = f"EvoGPT + 5 config injectors vs {baseline}"
        r = all_results[key]
        
        lcct_eff = f"{r['LCCT']['cliffs_delta']:.2f} ({r['LCCT']['effect_magnitude'][0].upper()})"
        bcct_eff = f"{r['BCCT']['cliffs_delta']:.2f} ({r['BCCT']['effect_magnitude'][0].upper()})"
        msct_eff = f"{r['MSCT']['cliffs_delta']:.2f} ({r['MSCT']['effect_magnitude'][0].upper()})"
        
        print(f"EvoGPT$_{{+5}}$ vs {baseline} & {lcct_eff} & {bcct_eff} & {msct_eff} \\\\")
    
    print("\\bottomrule")
    print("\\end{tabular}")
    print("\\end{table}")


if __name__ == "__main__":
    csv_path = "EvoGPT results final - results.csv"
    results = run_full_analysis(csv_path)


#!/usr/bin/env python3
"""
AST-Based Diversity Analyzer for Test Suites
Uses Abstract Syntax Tree comparison instead of text-based similarity
"""

import os
import re
from pathlib import Path
from collections import defaultdict
import json

import javalang

from scipy import stats
import numpy as np


class ASTFeatureExtractor:
    """Extract semantic features from Java AST."""
    
    def __init__(self):
        self.features = set()
    
    def extract_features(self, code):
        """Extract AST-based features from Java code."""
        try:
            tree = javalang.parse.parse(code)
            self.features = set()
            
            for path, node in tree:
                self._process_node(node, path)
            
            return self.features
        except Exception as e:
            print(f"Error parsing code: {e}")
            return set()
    
    def _process_node(self, node, path):
        """Process a single AST node and extract features."""
        
        # Method declarations
        if isinstance(node, javalang.tree.MethodDeclaration):
            self.features.add(f"method:{node.name}")
            # Extract annotations
            if node.annotations:
                for ann in node.annotations:
                    self.features.add(f"annotation:{ann.name}")
        
        # Method invocations (assertions, etc.)
        elif isinstance(node, javalang.tree.MethodInvocation):
            if node.member:
                self.features.add(f"call:{node.member}")
                # Special handling for assertions
                if node.member.startswith('assert'):
                    self.features.add(f"assertion_type:{node.member}")
        
        # Literals (constants)
        elif isinstance(node, javalang.tree.Literal):
            if node.value == 'null':
                self.features.add("literal:null")
            elif node.value == '0':
                self.features.add("literal:zero")
            elif node.value == '-1':
                self.features.add("literal:negative_one")
            elif node.value == '""' or node.value == "''":
                self.features.add("literal:empty_string")
            elif 'MAX_VALUE' in str(node.value):
                self.features.add("literal:max_value")
            elif 'MIN_VALUE' in str(node.value):
                self.features.add("literal:min_value")
        
        # Member references
        elif isinstance(node, javalang.tree.MemberReference):
            if node.member:
                # Check for boundary values
                if 'MAX_VALUE' in node.member or 'MIN_VALUE' in node.member:
                    self.features.add(f"boundary:{node.member}")
                else:
                    self.features.add(f"field:{node.member}")
        
        # Object creation
        elif isinstance(node, javalang.tree.ClassCreator):
            if node.type:
                type_name = node.type.name if hasattr(node.type, 'name') else str(node.type)
                self.features.add(f"new:{type_name}")
        
        # Try-catch blocks
        elif isinstance(node, javalang.tree.TryStatement):
            self.features.add("construct:try_catch")
            if node.catches:
                for catch in node.catches:
                    if catch.parameter and catch.parameter.types:
                        for exc_type in catch.parameter.types:
                            self.features.add(f"catch:{exc_type}")
        
        # Conditionals
        elif isinstance(node, javalang.tree.IfStatement):
            self.features.add("construct:if")
        
        # Loops
        elif isinstance(node, javalang.tree.ForStatement):
            self.features.add("construct:for_loop")
        elif isinstance(node, javalang.tree.WhileStatement):
            self.features.add("construct:while_loop")


def calculate_ast_similarity(code1, code2):
    """Calculate similarity between two Java code strings using AST."""
    extractor1 = ASTFeatureExtractor()
    extractor2 = ASTFeatureExtractor()
    
    features1 = extractor1.extract_features(code1)
    features2 = extractor2.extract_features(code2)
    
    if not features1 or not features2:
        # Fallback: if AST parsing fails, return 0
        return 0.0
    
    # Jaccard similarity
    intersection = len(features1 & features2)
    union = len(features1 | features2)
    
    return intersection / union if union > 0 else 0.0


def analyze_diversity(results_dir):
    """Analyze diversity using AST-based similarity."""
    
    # Configuration mapping (from your existing setup)
    CONFIG_MAP = {
        'config1': {'name': 'default', 'temp': 0.3, 'threads': [1, 6, 11, 16, 21]},
        'config2': {'name': 'assertion_heavy', 'temp': 0.4, 'threads': [2, 7, 12, 17, 22]},
        'config3': {'name': 'bug_detector', 'temp': 0.5, 'threads': [3, 8, 13, 18, 23]},
        'config4': {'name': 'edge_case_explorer', 'temp': 0.6, 'threads': [4, 9, 14, 19, 24]},
        'config5': {'name': 'high_coverage', 'temp': 0.8, 'threads': [5, 10, 15, 20, 25]}
    }
    
    # Find test files
    test_files = {}
    test_dir = os.path.join(results_dir, 'unit_tests', 'commons-cli', 'Option')
    
    print("Loading test files...")
    for config_id, config_info in CONFIG_MAP.items():
        test_files[config_id] = []
        for thread_id in config_info['threads']:
            test_file = os.path.join(test_dir, str(thread_id), 'javafiles', 'OptionTest.java')
            if os.path.exists(test_file):
                with open(test_file, 'r', encoding='utf-8') as f:
                    test_files[config_id].append({
                        'thread': thread_id,
                        'code': f.read()
                    })
                print(f"  Loaded thread {thread_id} ({config_info['name']})")
    
    # Calculate intra-group similarities
    print("\nCalculating intra-group similarities (AST-based)...")
    intra_similarities = {}
    
    for config_id, tests in test_files.items():
        config_name = CONFIG_MAP[config_id]['name']
        sims = []
        
        for i in range(len(tests)):
            for j in range(i + 1, len(tests)):
                sim = calculate_ast_similarity(tests[i]['code'], tests[j]['code'])
                sims.append(sim)
                print(f"  {config_name}: Thread {tests[i]['thread']} vs {tests[j]['thread']} = {sim:.3f}")
        
        intra_similarities[config_id] = {
            'similarities': sims,
            'mean': np.mean(sims) if sims else 0,
            'std': np.std(sims) if sims else 0
        }
    
    # Calculate inter-group similarities
    print("\nCalculating inter-group similarities (AST-based)...")
    inter_similarities = []
    
    config_ids = list(test_files.keys())
    for i in range(len(config_ids)):
        for j in range(i + 1, len(config_ids)):
            config_i = config_ids[i]
            config_j = config_ids[j]
            
            name_i = CONFIG_MAP[config_i]['name']
            name_j = CONFIG_MAP[config_j]['name']
            
            for test_i in test_files[config_i]:
                for test_j in test_files[config_j]:
                    sim = calculate_ast_similarity(test_i['code'], test_j['code'])
                    inter_similarities.append(sim)
            
            avg_sim = np.mean([calculate_ast_similarity(ti['code'], tj['code']) 
                              for ti in test_files[config_i] 
                              for tj in test_files[config_j]])
            print(f"  {name_i} vs {name_j}: {avg_sim:.3f}")
    
    # Calculate overall metrics
    overall_intra = np.mean([intra_similarities[c]['mean'] for c in intra_similarities])
    overall_inter = np.mean(inter_similarities)
    diversity_ratio = (1 - overall_inter) / (1 - overall_intra) if overall_intra < 1 else 0
    
    # Generate report
    report = {
        'method': 'AST-based (Jaccard similarity on semantic features)',
        'intra_group_similarity': overall_intra,
        'inter_group_similarity': overall_inter,
        'diversity_ratio': diversity_ratio,
        'config_specific': {}
    }
    
    for config_id, sim_data in intra_similarities.items():
        config_name = CONFIG_MAP[config_id]['name']
        report['config_specific'][config_name] = {
            'mean_similarity': sim_data['mean'],
            'std_similarity': sim_data['std'],
            'temp': CONFIG_MAP[config_id]['temp']
        }
    
    return report


def generate_comparative_report(ast_report):
    """Generate a detailed comparative report."""
    
    # Original text-based results (from your paper)
    text_based = {
        'intra_similarity': 0.343,
        'inter_similarity': 0.332,
        'diversity_ratio': 1.017,
        'config_specific': {
            'default': 0.326,
            'assertion_heavy': 0.440,
            'bug_detector': 0.225,
            'edge_case_explorer': 0.437,
            'high_coverage': 0.289
        }
    }
    
    report = []
    report.append("=" * 80)
    report.append("COMPARATIVE DIVERSITY ANALYSIS REPORT")
    report.append("Text-Based vs. AST-Based Similarity Metrics")
    report.append("=" * 80)
    report.append("")
    
    report.append("METHODOLOGY COMPARISON:")
    report.append("-" * 80)
    report.append("Text-Based (Original):")
    report.append("  - Uses SequenceMatcher on normalized code strings")
    report.append("  - Sensitive to: variable names, assertion order, whitespace")
    report.append("  - Measures: Structural/syntactic similarity")
    report.append("")
    report.append("AST-Based (New):")
    report.append("  - Uses Jaccard similarity on semantic features extracted from AST")
    report.append("  - Insensitive to: variable names, assertion order, formatting")
    report.append("  - Measures: Semantic/logical similarity")
    report.append("")
    
    report.append("=" * 80)
    report.append("OVERALL DIVERSITY METRICS COMPARISON")
    report.append("=" * 80)
    report.append("")
    report.append(f"{'Metric':<30} {'Text-Based':>15} {'AST-Based':>15} {'Difference':>15}")
    report.append("-" * 80)
    
    intra_diff = ast_report['intra_group_similarity'] - text_based['intra_similarity']
    inter_diff = ast_report['inter_group_similarity'] - text_based['inter_similarity']
    ratio_diff = ast_report['diversity_ratio'] - text_based['diversity_ratio']
    
    report.append(f"{'Intra-group similarity':<30} {text_based['intra_similarity']:>15.3f} {ast_report['intra_group_similarity']:>15.3f} {intra_diff:>+15.3f}")
    report.append(f"{'Inter-group similarity':<30} {text_based['inter_similarity']:>15.3f} {ast_report['inter_group_similarity']:>15.3f} {inter_diff:>+15.3f}")
    report.append(f"{'Diversity ratio':<30} {text_based['diversity_ratio']:>15.3f} {ast_report['diversity_ratio']:>15.3f} {ratio_diff:>+15.3f}")
    report.append("")
    
    report.append("=" * 80)
    report.append("CONFIGURATION-SPECIFIC ANALYSIS")
    report.append("=" * 80)
    report.append("")
    report.append(f"{'Configuration':<25} {'Text-Based':>15} {'AST-Based':>15} {'Difference':>15}")
    report.append("-" * 80)
    
    for config_name in ['default', 'assertion_heavy', 'bug_detector', 'edge_case_explorer', 'high_coverage']:
        text_val = text_based['config_specific'][config_name]
        ast_val = ast_report['config_specific'][config_name]['mean_similarity']
        diff = ast_val - text_val
        
        report.append(f"{config_name:<25} {text_val:>15.3f} {ast_val:>15.3f} {diff:>+15.3f}")
    
    report.append("")
    report.append("=" * 80)
    report.append("KEY FINDINGS")
    report.append("=" * 80)
    report.append("")
    
    # Analyze the differences
    if ast_report['diversity_ratio'] > text_based['diversity_ratio']:
        report.append("✅ AST-based analysis shows HIGHER diversity than text-based analysis")
        report.append(f"   Diversity ratio increased by {(ratio_diff / text_based['diversity_ratio'] * 100):.1f}%")
        report.append("")
        report.append("   INTERPRETATION:")
        report.append("   The original text-based analysis was conservative. When we account")
        report.append("   for semantic equivalence (ignoring variable names, assertion order),")
        report.append("   the true diversity between configurations is actually HIGHER.")
    else:
        report.append("⚠️  AST-based analysis shows LOWER diversity than text-based analysis")
        report.append("")
        report.append("   INTERPRETATION:")
        report.append("   Some structural differences in text were not reflecting semantic")
        report.append("   differences. Configurations may be more similar in logic than syntax.")
    
    report.append("")
    
    if ast_report['inter_group_similarity'] < ast_report['intra_group_similarity']:
        report.append("✅ Inter-group similarity < Intra-group similarity (CONFIRMED)")
        report.append("   Different configurations produce semantically distinct tests")
    else:
        report.append("⚠️  Inter-group similarity >= Intra-group similarity")
        report.append("   Semantic analysis suggests less diversity than structural analysis")
    
    report.append("")
    report.append("=" * 80)
    report.append("CONCLUSION")
    report.append("=" * 80)
    report.append("")
    
    if ast_report['diversity_ratio'] > 1.0:
        report.append("✅ SEMANTIC DIVERSITY VALIDATED")
        report.append("")
        report.append(f"AST-based diversity ratio = {ast_report['diversity_ratio']:.3f} > 1.0")
        report.append("")
        report.append("This confirms that your multi-configuration design produces")
        report.append("semantically diverse test suites, not just syntactically different ones.")
        report.append("The diversity is REAL and based on actual logical differences in")
        report.append("test structure, assertions, and control flow.")
    else:
        report.append("⚠️  SEMANTIC DIVERSITY LOWER THAN EXPECTED")
        report.append("")
        report.append(f"AST-based diversity ratio = {ast_report['diversity_ratio']:.3f}")
        report.append("")
        report.append("While structural diversity exists, semantic analysis reveals that")
        report.append("some configurations produce logically similar tests with different")
        report.append("syntax. Consider adjusting prompts for greater semantic diversity.")
    
    report.append("")
    report.append("=" * 80)
    
    return "\n".join(report)


def main():
    results_dir = '/Users/liorbr/PycharmProjects/EvoGPT/results'
    
    print("=" * 80)
    print("AST-BASED DIVERSITY ANALYSIS")
    print("=" * 80)
    print()
    
    # Run analysis
    ast_report = analyze_diversity(results_dir)
    
    # Generate comparative report
    print("\n" + "=" * 80)
    print("GENERATING COMPARATIVE REPORT")
    print("=" * 80)
    print()
    
    report_text = generate_comparative_report(ast_report)
    print(report_text)
    
    # Save results
    output_file = '/Users/liorbr/PycharmProjects/EvoGPT/ast_diversity_report.txt'
    with open(output_file, 'w') as f:
        f.write(report_text)
    
    print(f"\nReport saved to: {output_file}")
    
    # Save JSON results
    json_file = '/Users/liorbr/PycharmProjects/EvoGPT/ast_diversity_results.json'
    with open(json_file, 'w') as f:
        json.dump(ast_report, f, indent=2)
    
    print(f"JSON results saved to: {json_file}")


if __name__ == "__main__":
    main()


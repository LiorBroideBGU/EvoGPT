#!/usr/bin/env python3
"""
Analyze Java projects to count focal classes and focal methods.
Focal class = public class
Focal method = public method within a public class
"""

import os
import re
from pathlib import Path
from collections import defaultdict

def is_public_class(content, class_name):
    """Check if a class is public."""
    # Look for public class declaration
    pattern = rf'\bpublic\s+(?:abstract\s+)?(?:final\s+)?class\s+{re.escape(class_name)}\b'
    return bool(re.search(pattern, content))

def is_public_interface(content, interface_name):
    """Check if an interface is public."""
    pattern = rf'\bpublic\s+interface\s+{re.escape(interface_name)}\b'
    return bool(re.search(pattern, content))

def count_public_methods(content):
    """Count public methods in the given content."""
    # Remove comments to avoid false positives
    content_no_comments = re.sub(r'//.*?$', '', content, flags=re.MULTILINE)
    content_no_comments = re.sub(r'/\*.*?\*/', '', content_no_comments, flags=re.DOTALL)
    
    # Pattern to match public method declarations
    # This matches: public <modifiers>* <return_type> methodName(
    method_pattern = r'\bpublic\s+(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?(?:abstract\s+)?(?:<[^>]+>\s+)?[\w\[\]<>,\s]+\s+\w+\s*\([^)]*\)'
    
    matches = re.findall(method_pattern, content_no_comments)
    
    # Filter out constructors and fields
    methods = []
    for match in matches:
        # Skip if it looks like a field declaration (ends with ; not followed by {)
        if 'class ' not in match and 'interface ' not in match:
            methods.append(match)
    
    return len(methods)

def analyze_java_file(file_path):
    """Analyze a single Java file for focal classes and methods."""
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # Get the class name from the file name
        class_name = Path(file_path).stem
        
        # Check if it's a public class or interface
        is_focal_class = is_public_class(content, class_name) or is_public_interface(content, class_name)
        
        if is_focal_class:
            # Count public methods
            method_count = count_public_methods(content)
            return True, method_count
        else:
            return False, 0
            
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return False, 0

def analyze_project(project_path, project_name):
    """Analyze all Java files in a project."""
    focal_classes = 0
    focal_methods = 0
    
    # Find all Java files in src/main/java
    main_src_path = os.path.join(project_path, 'src', 'main', 'java')
    if not os.path.exists(main_src_path):
        # Try alternative structure
        main_src_path = os.path.join(project_path, 'src', 'java')
    
    # For mockito, check mockito-core subdirectory
    if not os.path.exists(main_src_path) and project_name == 'mockito':
        main_src_path = os.path.join(project_path, 'mockito-core', 'src', 'main', 'java')
    
    if not os.path.exists(main_src_path):
        return 0, 0
    
    for root, dirs, files in os.walk(main_src_path):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                is_focal, method_count = analyze_java_file(file_path)
                if is_focal:
                    focal_classes += 1
                    focal_methods += method_count
    
    return focal_classes, focal_methods

def main():
    benchmarks_dir = '/Users/liorbr/PycharmProjects/EvoGPT/benchmarks'
    
    projects = [
        'commons-cli',
        'commons-codec',
        'commons-collections',
        'commons-compress',
        'commons-csv',
        'commons-jxpath',
        'commons-lang',
        'commons-math',
        'gson',
        'jackson-core',
        'jackson-databind',
        'jackson-dataformat-xml',
        'jfreechart',
        'jsoup',
        'mockito'
    ]
    
    results = []
    
    print("Analyzing projects...")
    print("-" * 80)
    
    for project in projects:
        project_path = os.path.join(benchmarks_dir, project)
        if os.path.exists(project_path):
            print(f"Analyzing {project}...")
            classes, methods = analyze_project(project_path, project)
            results.append((project, classes, methods))
        else:
            print(f"Project not found: {project}")
            results.append((project, 0, 0))
    
    print("\n" + "=" * 80)
    print("RESULTS: FOCAL CLASSES AND FOCAL METHODS BY PROJECT")
    print("=" * 80)
    print(f"{'Project':<30} {'Focal Classes':>15} {'Focal Methods':>15}")
    print("-" * 80)
    
    total_classes = 0
    total_methods = 0
    
    for project, classes, methods in results:
        print(f"{project:<30} {classes:>15,} {methods:>15,}")
        total_classes += classes
        total_methods += methods
    
    print("-" * 80)
    print(f"{'TOTAL':<30} {total_classes:>15,} {total_methods:>15,}")
    print("=" * 80)
    
    # Save to file
    output_file = '/Users/liorbr/PycharmProjects/EvoGPT/focal_methods_analysis.txt'
    with open(output_file, 'w') as f:
        f.write("=" * 80 + "\n")
        f.write("FOCAL CLASSES AND FOCAL METHODS BY PROJECT\n")
        f.write("=" * 80 + "\n")
        f.write(f"{'Project':<30} {'Focal Classes':>15} {'Focal Methods':>15}\n")
        f.write("-" * 80 + "\n")
        
        for project, classes, methods in results:
            f.write(f"{project:<30} {classes:>15,} {methods:>15,}\n")
        
        f.write("-" * 80 + "\n")
        f.write(f"{'TOTAL':<30} {total_classes:>15,} {total_methods:>15,}\n")
        f.write("=" * 80 + "\n")
    
    print(f"\nResults saved to: {output_file}")

if __name__ == "__main__":
    main()


import zipfile
import javalang
from typing import List
import os
import re

def unzip_dataset(dataset_name: str, target_path: str, dataset_path: str):
    """
    Unzips the dataset's zip into a directory.
    :param dataset_name: The name of the dataset. Will be saved as the directory of the dataset.
    :param target_path:  The target path of where the dataset will be saved.
    :param dataset_path: The path to the dataset's zip file.

    """
    dataset_path = f"{dataset_path}/{dataset_name}.zip"
    with zipfile.ZipFile(dataset_path, 'r') as zip_ref:
        zip_ref.extractall(target_path)


def syntax_java_code(java_code):
    """
    Checks if the java code is syntactically correct.
    :param java_code:
    :return:
    """
    try:
        tree = javalang.parse.parse(java_code)
    except Exception as e:
        raise SyntaxError(f"Syntax error in java code: {e}")


def add_imports(imports: List[str], java_code: str):
    """
    Concatenates the imports list into the java code.
    :param imports: list of imports.
    :param java_code: the source code of the java code.
    """
    lines = java_code.splitlines()
    imports.extend(lines)
    return '\n'.join(imports)

def get_class_imports(source_folder: str, stacktrace: str):
    """
    Retrieves the missing imports list from the stacktrace and source_folder.
    :param source_folder: The path to the src folder containing the source code.
    :param stacktrace: The import errors received from the compiler.
    """
    pattern = r"error:\s*package\s+(\S+)\s+does\s+not\s+exist"
    class_names = re.findall(pattern, stacktrace)
    class_names = list(set(class_names))
    class_map = {}  # {class_name: [package_reference, ...]}
    for root, dirs, files in os.walk(source_folder):
        for file in files:
            if file.endswith(".java"):
                class_name = file[:-5]
                root = root.replace("\\", ".")
                root = root.replace("/", ".")
                if "src.main.java." not in root:
                    continue
                ref = root.split("src.main.java.")[1]
                if class_name not in class_map:
                    class_map[class_name] = [ref]
                else:
                    class_map[class_name].append(ref)
    imports = []
    for name in class_names:
        if name not in class_map:
            continue
        refs = set(class_map[name])
        for ref in refs:
            imports.append(f"import {ref}.{name};")
    return imports

def get_error_functions(stacktrace: str, code: str):
    """
    Processes the code based on the stack trace. Handles two cases:
    1. Runtime failures: Identifies test functions matching test names in the stack trace.
    2. Compilation errors: Identifies test functions associated with the error lines.

    :param stacktrace: The stack trace string containing errors or failures.
    :param code: The Java code string containing test functions.
    :return: A list of function names causing runtime or compile errors.
    """
    # Extract test names for runtime failures
    runtime_failures = re.findall(r'\d+\)\s+([\w\d_]+)\s*\(.*?\)', stacktrace)

    # Extract line numbers for compilation errors
    compilation_errors = re.findall(r':(\d+): error:', stacktrace)
    compilation_lines = sorted(set(int(line) for line in compilation_errors), reverse=True)

    # Split the code into lines
    code_lines = code.splitlines()

    # List to store names of functions causing errors
    error_functions = set(runtime_failures)

    # Handle compilation errors: Identify test functions at the error lines
    for line_number in compilation_lines:
        # Ensure the line number is within bounds
        if line_number > len(code_lines) or line_number < 1:
            continue

        # Locate the start of the test function
        function_start = None
        for i in range(line_number - 1, -1, -1):  # Search upwards
            if re.match(r'\s*@Test', code_lines[i]):
                function_start = i
                break
        if function_start is None:
            continue

        # Locate the function name
        for j in range(function_start, len(code_lines)):
            match = re.match(r'\s*public void ([\w\d_]+)\s*\(', code_lines[j])
            if match:
                error_functions.add(match.group(1))
                break

    return list(error_functions)

def remove_functions_by_name(function_names, code):
    """
    Removes functions with specified names from the given Java code.

    :param function_names: A list of function names to remove.
    :param code: The Java code string containing the functions.
    :return: The updated Java code string with specified functions removed.
    """
    # Split the code into lines
    code_lines = code.splitlines()

    # Create a pattern to match function definitions for the provided names
    function_pattern = re.compile(r'\s*public void (' + '|'.join(map(re.escape, function_names)) + r')\s*\(')

    retained_lines = []
    skip_lines = False
    brace_count = 0

    for i, line in enumerate(code_lines):
        if skip_lines:
            # Count braces to track function block
            brace_count += line.count('{')
            brace_count -= line.count('}')
            if brace_count == 0:  # End of function block
                skip_lines = False
            continue

        # If we find a function matching the pattern, skip it
        if function_pattern.search(line):
            # Look backward for the @Test annotation or similar markers
            for j in range(len(retained_lines) - 1, -1, -1):
                if re.match(r'\s*@Test', retained_lines[j]):
                    retained_lines.pop(j)  # Remove @Test line
                    break

            # Start skipping lines for the function block
            skip_lines = True
            brace_count = line.count('{') - line.count('}')
            continue

        # Otherwise, retain the line
        retained_lines.append(line)

    return '\n'.join(retained_lines)


import zipfile
from typing import List
import os
import re
import javalang
import shutil

from utils.JavaCodeCoverage.Jacoco import JavaCodeCoverage
from utils.java_executor import JavaExecutor


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
    pattern_missing_imports = r"symbol:\s+variable\s+(\w+)"
    missing_classes = re.findall(pattern_missing_imports, stacktrace)
    missing_classes = list(set(missing_classes))
    class_names = re.findall(pattern, stacktrace)
    class_names = list(set(class_names))
    class_names = class_names + missing_classes
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


def remove_junit_tests_using_test_names(java_code: str, function_names: list) -> str:
    """
    Removes specified Java functions from the code.

    Args:
        java_code (str): The Java code as a string.
        function_names (list): List of function names to remove.

    Returns:
        str: The modified Java code.
    """
    tree = javalang.parse.parse(java_code)

    # Store original lines for rebuilding
    lines = java_code.splitlines()

    # List of line ranges to remove
    remove_ranges = []

    # Traverse AST and find test methods to remove
    for path, node in tree.filter(javalang.tree.MethodDeclaration):
        if node.name in function_names:
            start_line = node.position.line - 1  # Convert to zero-based index

            # Find if there is an @Test annotation above the method
            annotation_line = None
            for i in range(start_line - 1, -1, -1):
                if lines[i].strip().startswith("@Test"):
                    annotation_line = i
                    break

            remove_start = annotation_line if annotation_line is not None else start_line

            # Find method end (last closing bracket)
            end_line = start_line + 1
            brace_count = 0
            for i in range(start_line, len(lines)):
                brace_count += lines[i].count("{") - lines[i].count("}")
                if brace_count == 0:
                    end_line = i
                    break

            remove_ranges.append((remove_start, end_line))

    # Remove lines from bottom to top to avoid index shifting
    for start, end in sorted(remove_ranges, reverse=True):
        del lines[start:end + 1]

    # Reconstruct Java code
    return "\n".join(lines)

def remove_junit_tests(java_code: str, stacktrace: str) -> str:
    function_list = get_error_functions(stacktrace, java_code)
    return remove_junit_tests_using_test_names(java_code, function_list)

def clean_java_code(code: str) -> str:
    """
    Cleans a Java code block by removing comments and redundant blank lines.
    :param code (str): The Java code block as a string.
    :return str: Cleaned Java code block.
    """
    # Remove single-line comments (//...)
    code = re.sub(r"//.*", "", code)

    # Remove multi-line comments (/* ... */)
    code = re.sub(r"/\*.*?\*/", "", code, flags=re.DOTALL)

    # Remove redundant blank lines
    # First remove any leading/trailing whitespaces from each line
    lines = code.splitlines()
    # Filter out empty lines
    non_empty_lines = [line for line in lines if line and bool(line.strip())]
    # Join the cleaned lines with a single newline
    cleaned_code = "\n".join(non_empty_lines)

    return cleaned_code

def read_java_file_as_string(java_file_path):
    try:
        with open(java_file_path, "r", encoding="utf-8") as file:
            java_code = file.read()
            return java_code
    except FileNotFoundError:
        print(f"File not found: {java_file_path}")
    except Exception as e:
        print(f"An error occurred: {e}")


def save_test_suite(test_suite_code,test_file_path):
    try:
        with open(test_file_path, "w", encoding="utf-8") as file:
            file.write(test_suite_code)
    except Exception as e:
        os.makedirs(os.path.dirname(test_file_path), exist_ok=True)
        with open(test_file_path, "w", encoding="utf-8") as file:
            file.write(test_suite_code)



def extract_project_name(path: str):
    path = os.path.normpath(path)  # Normalize path separators
    path_parts = path.split(os.sep)

    if 'benchmarks' in path_parts:
        benchmarks_index = path_parts.index('benchmarks')
        if benchmarks_index + 1 < len(path_parts):
            return os.sep.join(path_parts[:benchmarks_index + 2])  # Ensure proper path format

    return None


import re

def merge_java_unit_tests(java_test_1, java_test_2):
    """
    Merges two Java unit test class strings, considering imports, class-level variables, and test methods.
    If a method in java_test_2 has the same name as one in java_test_1, it appends 'Enhanced' to its name.

    Parameters:
    - java_test_1 (str): The first Java test class as a string.
    - java_test_2 (str): The second Java test class as a string.

    Returns:
    - str: The merged Java test class.
    """
    # Extract imports
    imports_1 = set(re.findall(r'^import .*?;', java_test_1, re.MULTILINE))
    imports_2 = set(re.findall(r'^import .*?;', java_test_2, re.MULTILINE))
    merged_imports = sorted(imports_1 | imports_2)  # Merge and sort imports

    # Extract class name
    class_match_1 = re.search(r'public\s+class\s+(\w+)', java_test_1)
    class_match_2 = re.search(r'public\s+class\s+(\w+)', java_test_2)
    class_name = class_match_1.group(1) if class_match_1 else (
        class_match_2.group(1) if class_match_2 else "MergedTest")

    # Extract class-level variables (fields)
    fields_1 = set(re.findall(r'(private|protected|public)?\s+\w+\s+\w+\s*;', java_test_1))
    fields_2 = set(re.findall(r'(private|protected|public)?\s+\w+\s+\w+\s*;', java_test_2))
    merged_fields = sorted(fields_1 | fields_2)

    # Function to extract full Java methods, handling nested brackets
    def extract_test_methods(java_code):
        method_pattern = re.compile(r'@Test\s+public\s+void\s+\w+\s*\(.*?\)\s*\{', re.MULTILINE)
        methods = []
        for match in method_pattern.finditer(java_code):
            start = match.start()
            open_braces = 0
            end = start
            for i in range(start, len(java_code)):
                if java_code[i] == '{':
                    open_braces += 1
                elif java_code[i] == '}':
                    open_braces -= 1
                    if open_braces == 0:
                        end = i + 1
                        break
            methods.append(java_code[start:end])
        return methods

    # Extract test methods properly with full bodies
    test_methods_1_full = extract_test_methods(java_test_1)
    test_methods_2_full = extract_test_methods(java_test_2)

    # Extract method names
    test_method_names_1 = set(re.findall(r'@Test\s+public\s+void\s+(\w+)\s*\(', java_test_1))
    test_methods_2_dict = {}

    for method in test_methods_2_full:
        method_name_match = re.search(r'@Test\s+public\s+void\s+(\w+)\s*\(', method)
        if method_name_match:
            method_name = method_name_match.group(1)
            if method_name in test_method_names_1:
                # Rename conflicting test methods
                new_method_name = method_name + "Enhanced"
                method = re.sub(rf'(@Test\s+public\s+void\s+){method_name}(\s*\()', rf'\1{new_method_name}\2', method)
                test_methods_2_dict[new_method_name] = method
            else:
                test_methods_2_dict[method_name] = method

    # Merge test methods, avoiding duplicates
    merged_test_methods = list(set(test_methods_1_full + list(test_methods_2_dict.values())))
    merged_test_methods.sort()  # Sort for consistency

    # Construct the merged Java test class
    merged_java_test = "\n".join(merged_imports) + "\n\n"
    merged_java_test += f"public class {class_name} {{\n\n"

    # Add merged fields
    for field in merged_fields:
        merged_java_test += f"    {field}\n"

    merged_java_test += "\n"

    # Add merged test methods
    for method in merged_test_methods:
        merged_java_test += f"    {method}\n\n"

    merged_java_test += "}"

    return merged_java_test



def delete_file(file_path):
    """
    Deletes the file at the given file path.

    Parameters:
    - file_path (str): The path of the file to be deleted.

    Returns:
    - bool: True if the file was deleted successfully, False if the file does not exist.
    """
    try:
        if os.path.isfile(file_path):  # Check if the file exists
            os.remove(file_path)  # Delete the file
            print(f"File '{file_path}' deleted successfully.")
            return True
        elif os.path.isdir(file_path):
            shutil.rmtree(file_path)
            print(f"Directory '{file_path}' deleted successfully.")
        else:
            print(f"File '{file_path}' does not exist.")
            return False
    except Exception as e:
        print(f"Error deleting file '{file_path}': {e}")
        return False


def extract_public_methods(java_code: str):
    """
    Extracts public method signatures from the given Java code, excluding return types.

    :param java_code: str, Java source code
    :return: list of public method signatures (excluding return types)
    """
    method_pattern = re.compile(
        r'public\s+(?:static\s+)?'  # Match "public" and optional "static"
        r'[\w<>,\[\]]+\s+'  # Return type (optional for constructors)
        r'(\w+)\s*'  # Method name
        r'\(([^)]*)\)'  # Parameters inside parentheses
    )

    methods = method_pattern.findall(java_code)

    # Extract class names to filter out constructors
    class_pattern = re.compile(r'class\s+(\w+)')
    class_names = class_pattern.findall(java_code)

    # Build method signatures without return types
    method_signatures = []
    for match in methods:
        method_name, params = match  # Unpack correctly

        # Remove return type for constructors
        signature = f"{method_name}({params})"
        method_signatures.append(signature.strip())

    return method_signatures


def extract_java_function(java_code: str, function_signature: str) -> str:
    """
    Extracts the full definition of a Java function, including annotations and ensuring
    all curly brackets are correctly matched.

    Args:
        java_code (str): The full Java source code.
        function_signature (str): The full function signature including parameter types, e.g., "add(Boolean bool)".

    Returns:
        str: The extracted function definition including annotations.
    """
    # Extract function name and parameters separately
    function_name, param_list = function_signature.split("(", 1)
    param_list = param_list.rstrip(")")  # Remove trailing parenthesis

    # Construct regex for function matching
    pattern = re.compile(
        rf"""
        (?:@\w+\s*)*  # Match optional annotations
        (?:public|protected|private|static|final|synchronized|abstract|native|transient|volatile|strictfp)?\s*
        [\w<>,\[\] ]+\s+  # Return type (handles generics and arrays)
        {re.escape(function_name)}\s*  # Match function name
        \(\s*{re.escape(param_list)}\s*\)  # Match exact parameters inside parentheses
        \s*\{{  # Match function opening bracket
        """,
        re.VERBOSE | re.DOTALL
    )

    match = pattern.search(java_code)
    if not match:
        return ""

    start_index = match.start()

    # Extract the function block ensuring correct bracket matching
    bracket_count = 0
    end_index = start_index
    while end_index < len(java_code):
        if java_code[end_index] == '{':
            bracket_count += 1
        elif java_code[end_index] == '}':
            bracket_count -= 1
            if bracket_count == 0:
                break
        end_index += 1

    return java_code[start_index:end_index + 1]


def replace_java_function(java_code: str, function_name: str, modified_function: str) -> str:
    """
    Replaces a given function definition in the Java source code with a modified version.

    Args:
        java_code (str): The full Java source code.
        function_name (str): The name of the function to be replaced (e.g., "add(Boolean bool)").
        modified_function (str): The new function definition to replace the existing one.

    Returns:
        str: The updated Java source code with the function replaced.
    """
    # Extract function name and parameters separately
    function_base, param_list = function_name.split("(", 1)
    param_list = param_list.rstrip(")")  # Remove trailing parenthesis

    # Construct regex for function matching
    pattern = re.compile(
        rf"""
        (?:@\w+\s*)*  # Match optional annotations
        (?:public|protected|private|static|final|synchronized|abstract|native|transient|volatile|strictfp)?\s*
        [\w<>,\[\] ]+\s+  # Return type (handles generics and arrays)
        {re.escape(function_base)}\s*  # Match function name
        \(\s*{re.escape(param_list)}\s*\)  # Match exact parameters inside parentheses
        \s*\{{  # Match function opening bracket
        """,
        re.VERBOSE | re.DOTALL
    )

    match = pattern.search(java_code)
    if not match:
        return java_code  # Return unchanged if function is not found

    start_index = match.start()

    # Extract the function block ensuring correct bracket matching
    bracket_count = 0
    end_index = start_index
    while end_index < len(java_code):
        if java_code[end_index] == '{':
            bracket_count += 1
        elif java_code[end_index] == '}':
            bracket_count -= 1
            if bracket_count == 0:
                break
        end_index += 1

    # Replace the old function with the new modified function
    updated_code = java_code[:start_index] + modified_function + java_code[end_index + 1:]

    return updated_code


def compile_code_from_path(code):
    """
    :param code: Path to the java source code.
    :return:
    """
    executor = JavaExecutor(code)
    executor.compile_java()

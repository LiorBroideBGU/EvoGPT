import zipfile
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


def remove_junit_tests_using_test_names(java_code: str, test_names: list) -> str:
    """
    Removes specified JUnit test functions (including @Test annotations) from a Java code block.

    Args:
        java_code (str): The Java code as a string.
        test_names (list): List of test case names (function names) to remove.

    Returns:
        str: The Java code with the specified test functions removed.
    """
    if not test_names:
        return clean_java_code(java_code)
    # Create a set for faster lookup of test names
    test_names_set = set(test_names)

    # Split the code into lines
    lines = java_code.splitlines()
    result = []
    skip_block = False

    for line in lines:
        stripped_line = line.strip()

        # Check if this line marks the start of a test function with @Test annotation
        if stripped_line.startswith("@Test"):
            skip_block = True  # Start skipping this block
            continue  # Skip the @Test line

        # Check if the function definition is part of the test names
        if skip_block and re.match(r"public\s+void\s+(\w+)", stripped_line):
            match = re.search(r"public\s+void\s+(\w+)", stripped_line)
            if match and match.group(1) in test_names_set:
                continue  # Skip this line and keep skipping
            else:
                skip_block = False  # Function not in test names, stop skipping

        # Skip lines inside the function block
        if skip_block:
            if stripped_line == "}":
                skip_block = False  # End of function block
            continue

        # Otherwise, keep the line
        result.append(line)

    # Join the cleaned lines back together
    code = "\n".join(result)
    return clean_java_code(code)


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
        raise Exception(f"An error occurred: {e}")


def extract_project_name(path: str):
    path = os.path.normpath(path)
    path_parts = path.split(os.sep)
    if 'benchmarks' in path_parts:
        benchmarks_index = path_parts.index('benchmarks')
        if benchmarks_index + 1 < len(path_parts):
            return os.path.join(*path_parts[:benchmarks_index + 2])
    return None

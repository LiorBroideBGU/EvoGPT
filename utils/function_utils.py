from pathlib import Path
import zipfile
from typing import List
import os
import re
import javalang
import shutil
from utils.java_executor import JavaExecutor


def unzip_dataset(dataset_name: str, target_path: Path, dataset_path: Path):
    """
    Unzips the dataset's zip into a directory.
    :param dataset_name: The name of the dataset. Will be saved as the directory of the dataset.
    :param target_path:  The target path of where the dataset will be saved.
    :param dataset_path: The path to the dataset's zip file.

    """
    dataset_path = dataset_path / f"{dataset_name}.zip"
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

def get_class_imports(source_folder: Path, stacktrace: str):
    """
    Retrieves the missing imports list from the stacktrace and source_folder.
    :param source_folder: The path to the src folder containing the source code.
    :param stacktrace: The import errors received from the compiler.
    """
    EXCEPTIONS_REQUIRING_IMPORT = {
        'IOException': 'java.io.IOException',
        'SQLException': 'java.sql.SQLException',
        'ParseException': 'java.text.ParseException',
        'FileNotFoundException': 'java.io.FileNotFoundException',
        'IllegalStateException': 'java.lang.IllegalStateException',
        'IllegalArgumentException': 'java.lang.IllegalArgumentException',
        'NullPointerException': 'java.lang.NullPointerException',
        'IndexOutOfBoundsException': 'java.lang.IndexOutOfBoundsException',
        'ArithmeticException': 'java.lang.ArithmeticException',
        'NumberFormatException': 'java.lang.NumberFormatException',
        'UnsupportedOperationException': 'java.lang.UnsupportedOperationException'
    }

    pattern = r"error:\s*package\s+(\S+)\s+does\s+not\s+exist"
    pattern_missing_imports = r"symbol:\s+variable\s+(\w+)"
    missing_classes = re.findall(pattern_missing_imports, stacktrace)
    missing_classes = list(set(missing_classes))
    class_names = re.findall(pattern, stacktrace)
    class_names = list(set(class_names))
    class_names = class_names + missing_classes
    class_map = {}  # {class_name: [package_reference, ...]}
    for java_file in source_folder.glob("**/*.java"):
        class_name = java_file.stem
        package_name = java_file.parent.as_posix().replace("\\", ".").replace("/", ".")
        if "src.main.java." not in package_name:
            continue

        ref = package_name.split("src.main.java.")[1]
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

    # Add known exception class imports if found in stacktrace
    for exc, full_import in EXCEPTIONS_REQUIRING_IMPORT.items():
        if exc in stacktrace:
            imports.append(f"import {full_import};")

    return list(sorted(set(imports)))

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
    Removes specified @Test annotated Java functions from the code safely.

    Args:
        java_code (str): The Java code as a string.
        function_names (list): List of test function names to remove.

    Returns:
        str: The modified Java code with specified test methods removed.
    """
    lines = java_code.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i].strip()

        # Check if this is the start of a test method to remove
        if line.startswith("@Test"):
            j = i + 1
            # Find the method signature
            while j < len(lines) and not re.search(r'public\s+void\s+(\w+)\s*\(', lines[j]):
                j += 1

            if j >= len(lines):
                i += 1
                continue

            method_line = lines[j]
            match = re.search(r'public\s+void\s+(\w+)\s*\(', method_line)
            if match:
                method_name = match.group(1)
                if method_name in function_names:
                    # Start removing from `i` (including @Test) until matching brace
                    start = i
                    brace_count = 0
                    found_open = False
                    k = j
                    while k < len(lines):
                        brace_count += lines[k].count('{')
                        brace_count -= lines[k].count('}')
                        if "{" in lines[k]:
                            found_open = True
                        if found_open and brace_count <= 0:
                            break
                        k += 1
                    # Remove block
                    del lines[start:k + 1]
                    i = start  # Don't increment; we have a new line here now
                    continue
        i += 1

    # Fix unbalanced braces if needed
    open_braces = sum(line.count("{") for line in lines)
    close_braces = sum(line.count("}") for line in lines)
    if close_braces > open_braces:
        lines.append("}")

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


def save_test_suite(test_suite_code: str, test_file_path: Path | str):
    path = Path(test_file_path)
    try:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(test_suite_code, encoding="utf-8")
    except Exception:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(test_suite_code, encoding="utf-8")



def extract_project_name(path: Path, root_hint: str | Path | None = None) -> Path:
    """
    Given a path to a Java file inside a project, return the project root directory
    (e.g. benchmarks/<project> or output_dir/<project>) as a Path.

    :param path: Path to a Java file.
    :param root_hint: Root segment to search for (e.g. "benchmarks" or output dir).
                     If None, uses config.PROJECT_ROOT or "benchmarks".
    :return: Path to the project root (root + project name).
    """
    if root_hint is None:
        try:
            from config.config_loader import get_config
            root_hint = get_config().PROJECT_ROOT
        except Exception:
            root_hint = "benchmarks"

    segment = Path(root_hint).name
    parts = path.parts
    if segment in parts:
        idx = parts.index(segment)
        if idx + 1 < len(parts):
            return Path(*parts[: idx + 2])

    # Fallback: just return the parent directory
    return path.parent



def merge_java_unit_tests(java_test_1: str, java_test_2: str, class_name: str) -> str:
    """
    Merges two Java unit tests into a single Java unit test.
    :param java_test_1: The first Java unit test.
    :param java_test_2: The second Java unit test.
    :param class_name: The name of the class.
    :return: The merged Java unit test.
    """
    def get_package(code: str):
        match = re.search(r'^package\s+[\w.]+\s*;', code, re.MULTILINE)
        return match.group(0) if match else None

    def get_imports(code: str):
        return set(re.findall(r'^import\s+.*?;', code, re.MULTILINE))

    def get_fields(code: str):
        pattern = re.compile(
            r'^\s*(private|protected|public)\s+[\w\<\>\[\]]+\s+\w+\s*;',
            re.MULTILINE
        )
        fields = []
        for match in pattern.finditer(code):
            field_line = match.group(0).strip()
            normalized = ' '.join(field_line.split())
            fields.append(normalized)
        return fields

    def extract_test_methods(code: str):
        method_pattern = re.compile(r'@Test\s+public\s+void\s+(\w+)\s*\([^)]*\)\s*\{', re.MULTILINE)
        methods = []
        for match in method_pattern.finditer(code):
            start = match.start()
            braces = 0
            end = start
            for i in range(start, len(code)):
                if code[i] == '{':
                    braces += 1
                elif code[i] == '}':
                    braces -= 1
                    if braces == 0:
                        end = i + 1
                        break
            methods.append(code[start:end])
        return methods

    def extract_static_methods(code: str):
        """
        Extracts static methods, including those with generic declarations and varargs.
        Returns a dict of method name -> full method source.
        """
        pattern = re.compile(
            r'''
            ^\s*
            (public|protected|private)?\s+         # optional access modifier
            static\s+                              # static keyword
            (<[^>]+>\s+)?                          # optional generic declaration, e.g. <T>
            [\w\[\]<>?,\s]+?\s+                    # return type (e.g., T, List<T>, Map<K, V>, etc.)
            (\w+)\s*                               # method name
            \([^)]*\)                              # argument list (no multiline args)
            (\s*throws\s+[^{]+)?                   # optional throws clause
            \s*\{                                  # method opening brace
            ''',
            re.MULTILINE | re.VERBOSE
        )

        methods = {}
        for match in pattern.finditer(code):
            method_name = match.group(3)
            start = match.start()

            # Extract full method body with brace matching
            brace_count = 0
            for i in range(start, len(code)):
                if code[i] == '{':
                    brace_count += 1
                elif code[i] == '}':
                    brace_count -= 1
                    if brace_count == 0:
                        end = i + 1
                        methods[method_name] = code[start:end]
                        break

        return methods

    def extract_lifecycle_methods(code: str):
        """Extract @Before, @After, @BeforeClass, @AfterClass methods."""
        annotations = r'@(?:Before|After|BeforeClass|AfterClass)'
        pattern = re.compile(
            rf'({annotations})\s+public\s+(?:static\s+)?void\s+(\w+)\s*\([^)]*\)\s*\{{',
            re.MULTILINE
        )
        methods = {}
        for match in pattern.finditer(code):
            annotation = match.group(1)
            method_name = match.group(2)
            start = match.start()
            braces = 0
            end = start
            for i in range(start, len(code)):
                if code[i] == '{':
                    braces += 1
                elif code[i] == '}':
                    braces -= 1
                    if braces == 0:
                        end = i + 1
                        break
            methods[(annotation, method_name)] = code[start:end]
        return methods

    def extract_static_classes(code: str):
        pattern = re.compile(r'\bstatic\b\s+class\s+(\w+)\s*\{', re.MULTILINE)
        classes = {}
        for match in pattern.finditer(code):
            class_name = match.group(1)
            start = match.start()
            braces = 0
            for i in range(start, len(code)):
                if code[i] == '{':
                    braces += 1
                elif code[i] == '}':
                    braces -= 1
                    if braces == 0:
                        end = i + 1
                        classes[class_name] = code[start:end]
                        break
        return classes

    def rename_conflicts(source_code: str, names_to_rename: list, suffix="_2"):
        for name in names_to_rename:
            pattern = re.compile(rf'\b{name}\b')
            source_code = pattern.sub(f"{name}{suffix}", source_code)
        return source_code

    # Step 1: Gather elements
    package_stmt = get_package(java_test_1) or get_package(java_test_2)
    imports = sorted(get_imports(java_test_1) | get_imports(java_test_2))
    fields = sorted(set(get_fields(java_test_1)) | set(get_fields(java_test_2)))

    test_methods_1 = extract_test_methods(java_test_1)
    test_methods_2 = extract_test_methods(java_test_2)
    test_method_names_1 = set(re.findall(r'@Test\s+public\s+void\s+(\w+)', java_test_1))

    static_methods_1 = extract_static_methods(java_test_1)
    static_methods_2 = extract_static_methods(java_test_2)
    static_classes_1 = extract_static_classes(java_test_1)
    static_classes_2 = extract_static_classes(java_test_2)
    lifecycle_1 = extract_lifecycle_methods(java_test_1)
    lifecycle_2 = extract_lifecycle_methods(java_test_2)

    # Step 2: Rename conflicts in java_test_2
    method_conflicts = set(static_methods_1.keys()) & set(static_methods_2.keys())
    class_conflicts = set(static_classes_1.keys()) & set(static_classes_2.keys())
    test_method_conflicts = {name for name in test_method_names_1}

    java_test_2 = rename_conflicts(java_test_2, list(method_conflicts | class_conflicts))

    # Re-extract from updated java_test_2
    test_methods_2 = extract_test_methods(java_test_2)
    static_methods_2 = extract_static_methods(java_test_2)
    static_classes_2 = extract_static_classes(java_test_2)

    # Merge lifecycle methods: keep suite 1, add unique ones from suite 2
    merged_lifecycle = dict(lifecycle_1)
    for key, body in lifecycle_2.items():
        if key not in merged_lifecycle:
            annotation, name = key
            if any(name == n for (_, n) in merged_lifecycle):
                new_name = name + "Enhanced"
                body = re.sub(rf'\b{name}\b', new_name, body, count=1)
            merged_lifecycle[key] = body

    # Step 3: Merge everything
    merged_code = ""
    if package_stmt:
        merged_code += package_stmt + "\n\n"
    merged_code += "\n".join(imports) + "\n\n"
    merged_code += f"public class {class_name} {{\n\n"

    # Fields
    for field in fields:
        merged_code += f"    {field}\n"

    merged_code += "\n"

    # Lifecycle methods (@Before, @After, etc.)
    for lc_code in merged_lifecycle.values():
        merged_code += f"    {lc_code}\n\n"

    # Static classes
    for cls_code in list(static_classes_1.values()) + list(static_classes_2.values()):
        merged_code += f"    {cls_code}\n\n"

    # Static methods
    for meth_code in list(static_methods_1.values()) + list(static_methods_2.values()):
        merged_code += f"    {meth_code}\n\n"

    # Test methods (rename test method collisions)
    for method in test_methods_2:
        method_name = re.search(r'@Test\s+public\s+void\s+(\w+)', method).group(1)
        if method_name in test_method_conflicts:
            method = re.sub(rf'(@Test\s+public\s+void\s+){method_name}(\s*\()', rf'\1{method_name}Enhanced\2', method)
        merged_code += f"    {method}\n\n"

    for method in test_methods_1:
        merged_code += f"    {method}\n\n"

    merged_code += "}"

    return merged_code




def delete_paths(paths_to_delete: list[Path]) -> bool:
    """
    Deletes the file at the given file path.

    Parameters:
    - file_path (str): The path of the file to be deleted.

    Returns:
    - bool: True if the file was deleted successfully, False if the file does not exist.
    """
    try:
        for path in paths_to_delete:
            if path.is_file():
                path.unlink()

            elif path.is_dir():
                shutil.rmtree(path)
        
        return True
    except Exception as e:
        print(f"An error occurred: {e}")
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


def get_java_import_path(java_file_path: str) -> str:
    """
    Given a Java source file path, extract the correct import path.
    Assumes the path contains a segment like 'src/main/java' before the package structure begins.
    """
    parts = os.path.normpath(java_file_path).split(os.sep)

    try:
        java_index = parts.index('java')
        package_parts = parts[java_index + 1:-1]
        return '.'.join(package_parts)
    except ValueError:
        raise ValueError("'java' directory not found in path. Ensure it's a standard src/main/java path.")


def extract_test_context(test_file_code: str) -> dict:
    """
    Extracts compilation context from a Java test file for LLM injection.
    This ensures generated test methods will compile with the same imports and setup.
    
    Args:
        test_file_code: The full Java test file source code
        
    Returns:
        dict with keys:
            - 'imports': List of import statements
            - 'class_name': The test class name
            - 'fields': List of class-level field declarations
            - 'setup_methods': List of @Before/@BeforeEach setup method bodies
            - 'context_string': Formatted string ready to include in LLM prompt
    """
    context = {
        'imports': [],
        'class_name': '',
        'fields': [],
        'setup_methods': [],
        'context_string': ''
    }
    
    # Extract imports
    import_pattern = re.compile(r'^import\s+.*?;', re.MULTILINE)
    context['imports'] = import_pattern.findall(test_file_code)
    
    # Extract class name
    class_pattern = re.compile(r'public\s+class\s+(\w+)')
    class_match = class_pattern.search(test_file_code)
    if class_match:
        context['class_name'] = class_match.group(1)
    
    # Extract field declarations (class-level variables)
    field_pattern = re.compile(
        r'^\s*(private|protected|public)?\s*(?:static\s+)?(?:final\s+)?[\w<>\[\],\s]+\s+\w+\s*(?:=\s*[^;]+)?;',
        re.MULTILINE
    )
    for match in field_pattern.finditer(test_file_code):
        field = match.group(0).strip()
        # Exclude method-level declarations (inside braces)
        field_pos = match.start()
        # Count braces before this position to check if it's at class level
        code_before = test_file_code[:field_pos]
        open_braces = code_before.count('{') - code_before.count('}')
        if open_braces == 1:  # Only at class level (after class opening brace)
            context['fields'].append(field)
    
    # Extract @Before/@BeforeEach/@BeforeAll setup methods
    setup_pattern = re.compile(
        r'(@(?:Before|BeforeEach|BeforeAll|BeforeClass)\s+(?:public\s+)?(?:static\s+)?void\s+\w+\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\s*\{)',
        re.MULTILINE
    )
    for match in setup_pattern.finditer(test_file_code):
        start = match.start()
        # Find the matching closing brace
        brace_count = 0
        end = start
        for i in range(start, len(test_file_code)):
            if test_file_code[i] == '{':
                brace_count += 1
            elif test_file_code[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end = i + 1
                    break
        context['setup_methods'].append(test_file_code[start:end])
    
    # Build context string for LLM prompt
    context_parts = []
    
    # Add imports
    if context['imports']:
        context_parts.append("// Existing imports (use these in your test methods):")
        context_parts.extend(context['imports'])
        context_parts.append("")
    
    # Add class info
    if context['class_name']:
        context_parts.append(f"// Test class: {context['class_name']}")
        context_parts.append("")
    
    # Add fields
    if context['fields']:
        context_parts.append("// Available class fields:")
        context_parts.extend([f"    {f}" for f in context['fields']])
        context_parts.append("")
    
    # Add setup methods
    if context['setup_methods']:
        context_parts.append("// Setup methods (these run before each test):")
        for method in context['setup_methods']:
            context_parts.append(f"    {method}")
        context_parts.append("")
    
    context['context_string'] = '\n'.join(context_parts)
    
    return context


def extract_test_method_names(test_file_code: str) -> set:
    """
    Extracts all @Test method names from a Java test file.
    
    Args:
        test_file_code: The Java test file source code
        
    Returns:
        Set of test method names
    """
    pattern = re.compile(r'@Test\s+(?:public\s+)?void\s+(\w+)\s*\(', re.MULTILINE)
    return set(pattern.findall(test_file_code))


def parse_generated_test_methods(llm_response: str) -> list:
    """
    Parses LLM response to extract generated @Test method blocks.
    
    Args:
        llm_response: The raw LLM response containing test methods
        
    Returns:
        List of tuples: (method_name, method_code)
    """
    methods = []
    
    # Pattern to find @Test methods
    method_pattern = re.compile(r'@Test\s+(?:public\s+)?void\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\s*\{', re.MULTILINE)
    
    for match in method_pattern.finditer(llm_response):
        method_name = match.group(1)
        start = match.start()
        
        # Find the matching closing brace
        brace_count = 0
        end = start
        for i in range(start, len(llm_response)):
            if llm_response[i] == '{':
                brace_count += 1
            elif llm_response[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end = i + 1
                    break
        
        method_code = llm_response[start:end]
        methods.append((method_name, method_code))
    
    return methods


def inject_test_methods(test_file_code: str, new_methods: list, existing_names: set = None) -> str:
    """
    Injects new @Test methods into an existing Java test file.
    Handles name collisions by adding suffixes.
    
    Args:
        test_file_code: The existing Java test file source code
        new_methods: List of tuples (method_name, method_code) to inject
        existing_names: Optional set of existing method names (extracted if not provided)
        
    Returns:
        The modified test file code with injected methods
    """
    if existing_names is None:
        existing_names = extract_test_method_names(test_file_code)
    
    # Find the position to inject (before the last closing brace of the class)
    # We need to find the class's closing brace, not a method's
    brace_count = 0
    class_end_pos = -1
    
    for i, char in enumerate(test_file_code):
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
            if brace_count == 0:
                class_end_pos = i
                break
    
    if class_end_pos == -1:
        # Fallback: find the last closing brace
        class_end_pos = test_file_code.rfind('}')
    
    if class_end_pos == -1:
        raise ValueError("Could not find class closing brace in test file")
    
    # Build the injection string
    injection_parts = ["\n    // ===== CodaMosa-style LLM Injected Tests =====\n"]
    
    used_names = set(existing_names)
    for method_name, method_code in new_methods:
        # Handle name collisions
        final_name = method_name
        suffix_counter = 1
        while final_name in used_names:
            final_name = f"{method_name}_injected{suffix_counter}"
            suffix_counter += 1
        
        # Replace method name in code if we had to rename
        if final_name != method_name:
            method_code = re.sub(
                rf'(void\s+){re.escape(method_name)}(\s*\()',
                rf'\g<1>{final_name}\g<2>',
                method_code
            )
        
        used_names.add(final_name)
        injection_parts.append(f"    {method_code}\n")
    
    injection_string = '\n'.join(injection_parts)
    
    # Insert before the closing brace
    modified_code = test_file_code[:class_end_pos] + injection_string + "\n" + test_file_code[class_end_pos:]
    
    return modified_code


def get_public_method_signatures(java_code: str) -> list:
    """
    Extracts public method signatures from Java source code.
    
    Args:
        java_code: The Java source code
        
    Returns:
        List of method signature strings
    """
    method_pattern = re.compile(
        r'public\s+(?:static\s+)?'  # Match "public" and optional "static"
        r'[\w<>,\[\]]+\s+'  # Return type
        r'(\w+)\s*'  # Method name
        r'\(([^)]*)\)'  # Parameters
    )
    
    signatures = []
    for match in method_pattern.finditer(java_code):
        method_name, params = match.groups()
        signatures.append(f"{method_name}({params})")
    
    return signatures

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
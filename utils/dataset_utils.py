import javalang
from javalang.tree import MethodDeclaration, ClassDeclaration
from utils.function_utils import *

def get_type_string(type):
    if type is None:
        return 'void'
    type_str = ''
    if isinstance(type, javalang.tree.ReferenceType):
        type_str = type.name
        if type.arguments:
            args = ', '.join(get_type_string(arg.type) for arg in type.arguments if arg.type is not None)
            type_str += f"<{args}>"
    elif isinstance(type, javalang.tree.BasicType):
        type_str = type.name
    else:
        type_str = str(type)

    dimensions = ''.join('[]' for _ in range(len(type.dimensions))) if hasattr(type, 'dimensions') else ''
    return f"{type_str}{dimensions}"

def get_public_method_signatures(java_code: str):
    try:
        tree = javalang.parse.parse(java_code)
    except javalang.parser.JavaSyntaxError as e:
        print("Parse error:", e)
        return []

    public_method_signatures = []

    for _, class_decl in tree.filter(ClassDeclaration):
        for method in class_decl.methods:
            if 'public' in method.modifiers:
                return_type = get_type_string(method.return_type)
                params = []
                for param in method.parameters:
                    param_type = get_type_string(param.type)
                    if param.varargs:
                        param_type += "..."
                    params.append(param_type)
                signature = f"{method.name}({', '.join(params)})"
                public_method_signatures.append(signature)

    return public_method_signatures

from collections import defaultdict
from typing import List, Tuple
import re
import javalang
from javalang.tree import TryStatement, MethodDeclaration, ClassDeclaration

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

def etree_to_dict(t):
    d = {t.tag: {} if t.attrib else None}
    children = list(t)
    if children:
        dd = defaultdict(list)
        for dc in map(etree_to_dict, children):
            for k, v in dc.items():
                dd[k].append(v)
        d = {t.tag: {k: v for k, v in dd.items()}}
    if t.attrib:
        d[t.tag].update(('@' + k, v) for k, v in t.attrib.items())
    if t.text:
        text = t.text.strip()
        if children or t.attrib:
            if text:
                d[t.tag]['#text'] = text
        else:
            d[t.tag] = text
    return d

def modify_exception_type(code_lines: List[str], error_loc: int, exception_type: str):
    def get_method_name_by_loc(methods: List[MethodDeclaration]):
        for i in range(len(methods)):
            method1 = methods[i]
            method2 = methods[i + 1] if i + 1 < len(methods) else None
            if method2 is None or method1.position[0] <= error_loc < method2.position[0]:
                return method1.name

    def get_try_block_by_loc(try_blocks: List[TryStatement]):
        for tb in try_blocks:
            for statement in tb.block:
                if error_loc == statement.position[0]:
                    return tb
        return None

    def get_catch_position(start: int, types: List[str]):
        for i in range(start, len(code_lines)):
            line = code_lines[i]
            if 'catch' in line and all(t in line for t in types):
                return i

    tree = javalang.parse.parse("\n".join(code_lines))
    clazz = [x for x in tree.types if isinstance(x, ClassDeclaration) and x.name.endswith("Test")][0]
    methods = clazz.methods
    target_method = get_method_name_by_loc(methods)
    body = [x.body for x in methods if target_method == x.name][0]
    try_blocks = [x for x in body if isinstance(x, TryStatement)]

    if not try_blocks:
        return False

    try_block = get_try_block_by_loc(try_blocks)
    if try_block is None:
        return False

    catch_loc = get_catch_position(try_block.position[0], try_block.catches[0].parameter.types)
    catch_statement = re.sub("catch.*\\((.*) .*\\)",
                             lambda x: x.group(0).replace(x.group(1), exception_type),
                             code_lines[catch_loc], count=1)

    indent = len(catch_statement) - len(catch_statement.lstrip())
    indent_space = ' ' * indent
    indent_space_tab = ' ' * (indent + 4)
    code_lines[catch_loc - 1: catch_loc] = [
        f'{catch_statement}',
        f'{indent_space_tab}// Expected',
    ]
    return True

def get_local_variable_declaration(line):
    from javalang.tree import LocalVariableDeclaration
    template = f"""
    public class Example {{
        private Boolean a;
        public void example() {{
            {line}
        }}
    }}
    """
    tree = javalang.parse.parse(template)
    method = tree.types[0].methods[0]
    statement = method.body[0]
    if isinstance(statement, LocalVariableDeclaration):
        return statement.type.name, statement.declarators[0].name, len(statement.type.dimensions)
    else:
        return None

def get_first_param(code: str, delete_param_gt3=True) -> Tuple[str, str]:
    import javalang
    template = f"""
    public class Temp{{
        public void temp(){{
            {code}
        }}
    }}
    """
    try:
        tree = javalang.parse.parse(template)
        method = tree.types[0].methods[0]
        num = len(method.body[0].expression.arguments)
        arg_0 = method.body[0].expression.arguments[0]
        arg_1 = method.body[0].expression.arguments[1]
        if num == 3 and delete_param_gt3:
            regex_param1 = re.escape(arg_0.value)
            code = re.sub(fr'\(\s*?{regex_param1}\s*?,', "(", code, count=1)
            arg_0 = arg_1
        if isinstance(arg_0, javalang.tree.Literal) and '"' in arg_0.value:
            param1 = arg_0.value
        else:
            param1 = re.search(r'\(([^\n]+?),\s', code).group(1)
        return code, param1
    except Exception as e:
        print(e)
        return code, ""

def fix_unit_test(java_unit_test: str, stacktrace: str) -> str:
    test_lines = java_unit_test.split('\n')
    stacktrace_lines = stacktrace.split('\n')
    locs = [i+1 for i, line in enumerate(test_lines) if any(assertion in line for assertion in ["assert", "=", "fail"]) and not line.strip().startswith("//")]
    lines = [test_lines[loc-1] for loc in locs]

    if 'AssertionError' in stacktrace or 'org.junit.ComparisonFailure' in stacktrace:
        for loc, code in zip(locs, lines):
            if 'assertFalse' in code:
                fixed_code = code.replace('assertFalse', 'assertTrue')
            elif 'assertTrue' in code:
                fixed_code = code.replace('assertTrue', 'assertFalse')
            elif 'assertNull' in code:
                fixed_code = code.replace('assertNull', 'assertNotNull')
            elif 'assertNotNull' in code:
                fixed_code = code.replace('assertNotNull', 'assertNull')
            elif 'assertArrayEquals' in code:
                fixed_code = code
            elif 'assertEquals' in code:
                code, param1 = get_first_param(code)
                regex_param1 = re.escape(param1)
                param1_with_bracket = fr'\(\s*?{regex_param1}\s*?,'
                result = re.search("expected:(.*) but was:(.*)", stacktrace)
                if not result:
                    return java_unit_test
                expected = result.group(1).strip()
                was = result.group(2).strip()

                def replace_brackets(string):
                    if string.startswith("<["):
                        string = string.replace("<[", "")
                    elif string.startswith("<..."):
                        string = string.replace("<...", "").replace("[", "")
                    elif string.startswith("<"):
                        string = string.replace("<", "").replace("[", "")
                    if string.endswith("]>"):
                        string = string.replace("]>", "")
                    elif string.endswith("...>"):
                        string = string.replace("...>", "").replace("]", "")
                    elif string.endswith(">"):
                        string = string.replace(">", "").replace("]", "")
                    return string

                expected = replace_brackets(expected)
                was = replace_brackets(was)

                was = was.replace("\\u", "\\\\u")
                value = re.search("<(.*)>", was)
                if value:
                    value = value.group(1)
                    if was.startswith("java.lang.Integer") or was.startswith("java.lang.Boolean") or was.startswith("java.lang.Float") or was.startswith("java.lang.Double") or was.startswith("java.lang.Byte"):
                        replace_str = fr"({value},"
                    elif was.startswith("java.lang.String"):
                        value = f'"{value}"'
                        replace_str = fr"({value},"
                    elif was.startswith("java.lang.Long"):
                        replace_str = fr"({value}L,"
                elif was.startswith("null"):
                    replace_str = fr"(null,"
                else:
                    if expected and expected in param1:
                        value = param1.replace(expected, was)
                        replace_str = fr"({value},"
                    else:
                        if not expected.isnumeric():
                            was = f'"{was}"'
                        replace_str = fr"({was},"
                try:
                    fixed_code = re.sub(param1_with_bracket, replace_str, code, count=1)
                except Exception as e:
                    fixed_code = code
            else:
                fixed_code = code
            test_lines[loc - 1] = fixed_code
    else:
        error_type = stacktrace_lines[0].split(':')[0].strip()
        target_loc = [loc for loc, line in zip(locs, lines) if 'public void' not in line][0]
        if modify_exception_type(test_lines, target_loc, error_type):
            pass
        else:
            target_line = test_lines[target_loc - 1]
            def_statement = None
            dinfo = get_local_variable_declaration(target_line)
            if dinfo is not None:
                dtype, dname, dimensions = dinfo
                type_map = {
                    'int': 'Integer', 'float': 'Float', 'double': 'Double', 'boolean': 'Boolean',
                    'char': 'Character', 'byte': 'Byte', 'short': 'Short', 'long': 'Long'
                }
                boxed_dtype = type_map.get(dtype) if dimensions == 0 else None
                dtype = f"{dtype}{'[]' * dimensions}"
                boxed_dtype = f"{boxed_dtype}{'[]' * dimensions}" if boxed_dtype else None
                def_statement = f'{boxed_dtype or dtype} {dname} = null;'
                target_line = re.sub(f"{dtype}\\s*?{dname}".replace("[", "\\["), dname, target_line, count=1)

            indent = len(target_line) - len(target_line.lstrip())
            indent_space = ' ' * indent
            indent_space_tab = ' ' * (indent + 4)
            new_lines = [
                f'{indent_space}{def_statement or ""}',
                f'{indent_space}try {{',
                f'{indent_space_tab}{target_line.strip()}',
                f'{indent_space_tab}fail("Expected {error_type}");',
                f'{indent_space}}} catch ({error_type} e) {{',
                f'{indent_space_tab}// Expected',
                f'{indent_space}}}'
            ]
            test_lines[target_loc - 1:target_loc] = new_lines

        if error_type in EXCEPTIONS_REQUIRING_IMPORT:
            import_statement = f"import {EXCEPTIONS_REQUIRING_IMPORT[error_type]};"
            if import_statement not in java_unit_test:
                insert_index = 0
                for idx, line in enumerate(test_lines):
                    if line.startswith('import'):
                        insert_index = idx + 1
                test_lines.insert(insert_index, import_statement)

    return '\n'.join(test_lines)


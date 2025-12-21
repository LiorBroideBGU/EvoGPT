"""
Programmatic Mutation for Java Test Suites
Implements EvoSuite-inspired mutation operators for test case evolution.

This module provides structural mutations to Java test methods without using LLMs,
including statement deletion, primitive value modification, and assertion changes.
"""

from utils.function_utils import *
from utils.java_executor import JavaExecutor
import random
import re


class ProgrammaticMutator:
    """
    EvoSuite-inspired programmatic mutation for Java test suites.
    
    Applies random structural mutations to test methods including:
    - Statement deletion (remove random non-assertion statements)
    - Primitive value modification (change numeric/string literals)
    - Assertion modification (change assertion types)
    
    Attributes:
        unit_test_path: Path to the Java test file to mutate
        source_code_path: Path to the Java source code under test
        mutation_probability: Probability of mutating each test method (default: 0.3)
    """
    
    def __init__(self, unit_test_path, source_code_path, mutation_probability=0.3):
        """
        Initialize the programmatic mutator.
        
        Args:
            unit_test_path: Path to test file to mutate
            source_code_path: Path to source code under test
            mutation_probability: Probability of mutating each test method (0.0-1.0)
        """
        self.unit_test_path = unit_test_path
        self.source_code_path = source_code_path
        self.mutation_probability = mutation_probability
        self.unit_test_java_executor = JavaExecutor(java_file_path=unit_test_path)
    
    def extract_test_methods(self, code: str):
        """
        Extract individual test methods from test suite.
        
        Args:
            code: Full Java test suite code as string
            
        Returns:
            List of tuples: [(method_code, method_name), ...]
        """
        method_pattern = re.compile(r'@Test\s+public\s+void\s+(\w+)\s*\([^)]*\)\s*\{', re.MULTILINE)
        methods = []
        for match in method_pattern.finditer(code):
            method_name = match.group(1)
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
            method_code = code[start:end]
            methods.append((method_code, method_name))
        return methods
    
    def replace_test_method(self, original_code: str, new_method_code: str, method_name: str) -> str:
        """
        Replace a specific test method in the test suite.
        
        Args:
            original_code: Original full test suite code
            new_method_code: New method code to replace with
            method_name: Name of the method to replace
            
        Returns:
            Updated test suite code with replaced method
        """
        method_pattern = re.compile(
            rf'@Test\s+public\s+void\s+{method_name}\s*\([^)]*\)\s*\{{',
            re.MULTILINE
        )
        match = method_pattern.search(original_code)
        if not match:
            return original_code
        
        start_index = match.start()
        brace_count = 0
        end_index = start_index
        for i in range(start_index, len(original_code)):
            if original_code[i] == '{':
                brace_count += 1
            elif original_code[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    end_index = i + 1
                    break
        
        updated_code = original_code[:start_index] + new_method_code.strip() + original_code[end_index:]
        return updated_code
    
    # === MUTATION OPERATORS ===
    
    def _delete_statement(self, method_code: str) -> str:
        """
        Delete a random non-assertion statement from the method.
        
        This operator removes setup code or intermediate computations to simplify tests.
        Assertions are preserved to maintain test semantics.
        
        Args:
            method_code: Test method code as string
            
        Returns:
            Method code with one statement deleted (if applicable)
        """
        lines = method_code.split('\n')
        
        # Find non-assertion, non-declaration statements that can be safely deleted
        statement_indices = [
            i for i, line in enumerate(lines)
            if ';' in line 
            and not line.strip().startswith('assert')
            and not line.strip().startswith('//')
            and not line.strip().startswith('*')
            and 'void' not in line
            and '@Test' not in line
            and line.strip()  # Not empty
        ]
        
        # Keep at least 2 statements to maintain test structure
        # Also avoid deleting variable declarations that might be used later
        safe_to_delete = [
            idx for idx in statement_indices
            if '=' not in lines[idx] or 'new ' in lines[idx]  # Don't delete assignments unless it's object creation
        ]
        
        if safe_to_delete and len(statement_indices) > 2:
            del_idx = random.choice(safe_to_delete)
            del lines[del_idx]
        
        return '\n'.join(lines)
    
    def _modify_primitive(self, method_code: str) -> str:
        """
        Modify numeric or string literals in the method.
        
        This operator changes primitive values to explore different input spaces:
        - Numeric literals: small increments/decrements
        - String literals: append/remove characters, change case
        
        Args:
            method_code: Test method code as string
            
        Returns:
            Method code with one primitive value modified (if applicable)
        """
        # 50% chance: modify numeric literals
        if random.random() < 0.5:
            pattern = r'\b(\d+)\b'
            matches = list(re.finditer(pattern, method_code))
            if matches:
                match = random.choice(matches)
                old_value = int(match.group(1))
                # Small random change to avoid breaking tests too much
                delta = random.choice([-2, -1, 1, 2])
                new_value = max(0, old_value + delta)
                method_code = method_code[:match.start()] + str(new_value) + method_code[match.end():]
        
        # 50% chance: modify string literals
        else:
            pattern = r'"([^"]*)"'
            matches = list(re.finditer(pattern, method_code))
            if matches:
                match = random.choice(matches)
                old_str = match.group(1)
                
                # Various string mutation strategies
                mutations = []
                if old_str:  # Non-empty string
                    mutations.extend([
                        old_str + "X",           # Append character
                        old_str[:-1],            # Remove last char
                        old_str.upper(),         # Change case
                        old_str.lower(),
                    ])
                else:  # Empty string
                    mutations.append("X")
                
                new_str = random.choice(mutations) if mutations else old_str
                method_code = method_code[:match.start()] + f'"{new_str}"' + method_code[match.end():]
        
        return method_code
    
    def _modify_assertion(self, method_code: str) -> str:
        """
        Remove an assertion to create weaker test (standard EvoSuite operator).
        
        EvoSuite's assertion minimization operator removes assertions to find
        the minimal set needed. This is safer than flipping assertions.
        
        Args:
            method_code: Test method code as string
            
        Returns:
            Method code with one assertion removed (if applicable)
        """
        lines = method_code.split('\n')
        
        # Find assertion lines
        assertion_indices = [
            i for i, line in enumerate(lines)
            if line.strip().startswith('assert') and ';' in line
        ]
        
        # Keep at least one assertion to maintain test validity
        if len(assertion_indices) > 1:
            # Remove one assertion (comment it out to preserve line structure)
            remove_idx = random.choice(assertion_indices)
            lines[remove_idx] = lines[remove_idx].replace(
                lines[remove_idx].strip(), 
                '// ' + lines[remove_idx].strip()
            )
        
        return '\n'.join(lines)
    
    def _mutate_method(self, method_code: str) -> str:
        """
        Apply a random mutation operator to a test method.
        
        Randomly selects one of the available mutation operators and applies it.
        
        Args:
            method_code: Test method code as string
            
        Returns:
            Mutated method code
        """
        operators = [
            self._delete_statement,
            self._modify_primitive,
            self._modify_assertion,
        ]
        
        operator = random.choice(operators)
        try:
            return operator(method_code)
        except Exception as e:
            print(f"Mutation operator failed: {e}")
            return method_code  # Return original on failure
    
    async def assertion_generation(self):
        """
        Main entry point - applies programmatic mutations incrementally.
        
        EvoSuite-style incremental mutation:
        1. Apply mutation to one method
        2. Validate (compile + execute)
        3. Keep if valid, rollback if not
        4. Repeat for next method
        
        This prevents losing good mutations when one fails.
        """
        original_unit_test = read_java_file_as_string(self.unit_test_path)
        test_methods = self.extract_test_methods(original_unit_test)
        
        if not test_methods:
            print(f"No test methods found in {self.unit_test_path}")
            return
        
        num_methods = len(test_methods)
        mutations_applied = 0
        mutations_kept = 0
        current_test_code = original_unit_test
        
        print(f"ProgrammaticMutator: Processing {num_methods} test methods with p={self.mutation_probability}")
        
        # Apply mutations incrementally (EvoSuite approach)
        for method_code, method_name in test_methods:
            if random.random() <= self.mutation_probability:
                mutations_applied += 1
                
                # Save state before mutation
                before_mutation = current_test_code
                
                try:
                    # Apply mutation
                    mutated_method = self._mutate_method(method_code)
                    current_test_code = self.replace_test_method(
                        current_test_code, 
                        mutated_method, 
                        method_name
                    )
                    
                    # Save and validate
                    save_test_suite(current_test_code, self.unit_test_path)
                    result, stacktrace = self.unit_test_java_executor.run_java()
                    
                    if result:
                        # Mutation successful - keep it
                        mutations_kept += 1
                        print(f"  ✓ Mutated method: {method_name}")
                    else:
                        # Mutation broke tests - rollback this one only
                        current_test_code = before_mutation
                        save_test_suite(current_test_code, self.unit_test_path)
                        # Silent rollback - this is expected behavior
                        
                except Exception as e:
                    # Mutation operation failed - rollback
                    current_test_code = before_mutation
                    save_test_suite(current_test_code, self.unit_test_path)
                    # Silent rollback
        
        if mutations_applied > 0:
            print(f"  ⚙ Mutations: {mutations_kept}/{mutations_applied} successful")
        else:
            print(f"  • No mutations applied (probability-based)")

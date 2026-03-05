"""
CodaMosa-style Plateau Escape Agent for EvoGPT.

This agent is triggered when the evolutionary algorithm stagnates (no fitness improvement 
for STAGNATION_THRESHOLD iterations). It generates targeted test methods using diverse 
LLM configurations and injects them into the best chromosome's test suite.
"""

import os
import asyncio
from typing import List, Tuple, Dict, Optional
from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage
from config.config import API_KEY, MODEL
from utils.function_utils import (
    extract_test_context,
    parse_generated_test_methods,
    read_java_file_as_string
)


# Strategy configurations: (prompt_file_suffix, temperature)
# These mirror the diverse prompts used in initial test generation
INJECTION_STRATEGIES = [
    ('high_coverage', 0.4),
    ('assertion_heavy', 0.6),
    ('edge_case_explorer', 0.5),
    ('bug_detector', 0.8),
    ('default', 0.3),
]


def get_all_injection_strategies() -> List[Tuple[str, float]]:
    """
    Return all injection strategies for maximum diversity.
    
    This mirrors the diverse prompts used in initial test generation,
    using all 5 strategy configurations to maximize coverage exploration.
        
    Returns:
        List of all (strategy_name, temperature) tuples
    """
    return INJECTION_STRATEGIES.copy()


class PlateauEscapeAgent:
    """
    Agent for generating targeted test methods when evolutionary search stagnates.
    
    This implements a CodaMosa-style approach where:
    1. Coverage gaps are identified from the best chromosome
    2. Multiple LLM agents with diverse configurations run in parallel
    3. Generated test methods are injected into the existing test suite
    """
    
    # Maps strategy names to their corresponding system prompt files
    # These are the same diverse prompts used in initial test generation
    STRATEGY_PROMPT_MAP = {
        'high_coverage': 'system_prompt_high_coverage.txt',
        'assertion_heavy': 'system_prompt_assertion_heavy.txt',
        'edge_case_explorer': 'system_prompt_edge_case_explorer.txt',
        'bug_detector': 'system_prompt_bug_detector.txt',
        'default': 'system_prompt_default.txt',
    }
    
    def __init__(self, api_key: str = None, model: str = None):
        """
        Initialize the PlateauEscapeAgent.
        
        Args:
            api_key: OpenAI API key (defaults to config.API_KEY)
            model: LLM model name (defaults to config.MODEL)
        """
        self.api_key = api_key or API_KEY
        self.model = model or MODEL
        
        # Load prompts
        self.plateau_prompts_dir = os.path.abspath(os.path.join("prompts", "plateau_escape"))
        self.unit_test_prompts_dir = os.path.abspath(os.path.join("prompts", "unit_test_generator"))
        
        # Load the injection-specific input prompt template
        self.input_prompt_template = self._load_prompt(os.path.join(self.plateau_prompts_dir, "input_prompt.txt"))
        
        # Pre-load all strategy-specific system prompts
        self.strategy_system_prompts = {}
        for strategy_name, prompt_file in self.STRATEGY_PROMPT_MAP.items():
            prompt_path = os.path.join(self.unit_test_prompts_dir, prompt_file)
            if os.path.exists(prompt_path):
                self.strategy_system_prompts[strategy_name] = self._load_prompt(prompt_path)
            else:
                # Fallback to default if specific prompt not found
                print(f"Warning: Prompt file {prompt_file} not found, using default")
                default_path = os.path.join(self.unit_test_prompts_dir, 'system_prompt_default.txt')
                self.strategy_system_prompts[strategy_name] = self._load_prompt(default_path)
        
        # Load the injection-specific system prompt (used as a wrapper/modifier)
        self.injection_context_prompt = self._load_prompt(
            os.path.join(self.plateau_prompts_dir, "system_prompt.txt")
        )
    
    def _load_prompt(self, path: str) -> str:
        """Load a prompt file."""
        with open(path, 'r') as f:
            return f.read()
    
    def _get_combined_system_prompt(self, strategy_name: str) -> str:
        """
        Combine the strategy-specific prompt with injection-specific instructions.
        
        Args:
            strategy_name: The name of the strategy (e.g., 'high_coverage')
            
        Returns:
            Combined system prompt string
        """
        base_prompt = self.strategy_system_prompts.get(
            strategy_name, 
            self.strategy_system_prompts['default']
        )
        
        # Combine: base strategy prompt + injection-specific modifications
        combined = f"""{base_prompt}

=== ADDITIONAL INJECTION CONTEXT ===
{self.injection_context_prompt}"""
        
        return combined
    
    async def _generate_with_strategy(
        self,
        strategy_name: str,
        temperature: float,
        context_string: str,
        source_code: str,
        branch_coverage: float,
        line_coverage: float,
        mutation_score: float,
        missed_branches: str,
        methods_count: int = 2
    ) -> List[Tuple[str, str]]:
        """
        Generate test methods using a specific strategy configuration.
        
        Args:
            strategy_name: Name of the prompt strategy (determines which system prompt to use)
            temperature: LLM temperature setting
            context_string: Extracted test file context
            source_code: Source code under test
            branch_coverage: Current branch coverage
            line_coverage: Current line coverage
            mutation_score: Current mutation score
            missed_branches: Description of missed branches
            methods_count: Number of methods to request
            
        Returns:
            List of (method_name, method_code) tuples
        """
        try:
            # Create LLM with specific temperature
            llm = ChatOpenAI(
                api_key=self.api_key,
                model=self.model,
                temperature=temperature,
                max_retries=10
            )
            
            # Get the strategy-specific system prompt combined with injection context
            system_prompt = self._get_combined_system_prompt(strategy_name)
            
            # Format the input prompt
            input_prompt = self.input_prompt_template.format(
                methods_count,           # Number of methods to generate
                context_string,          # Existing test class context
                source_code,             # Source code under test
                branch_coverage,         # Current branch coverage %
                line_coverage,           # Current line coverage %
                mutation_score,          # Current mutation score %
                missed_branches,         # Missed branches details
                methods_count            # Number of methods again for instructions
            )
            
            # Create messages with strategy-specific system prompt
            messages = [
                SystemMessage(content=system_prompt),
                HumanMessage(content=input_prompt)
            ]
            
            # Invoke LLM
            response = await llm.ainvoke(messages)
            
            # Parse generated methods
            methods = parse_generated_test_methods(response.content)
            
            print(f"    Strategy '{strategy_name}' (temp={temperature}): Generated {len(methods)} methods")
            return methods
            
        except Exception as e:
            print(f"    Strategy '{strategy_name}' failed: {e}")
            return []
    
    async def generate_targeted_tests(
        self,
        test_file_path: str,
        source_code_path: str,
        branch_coverage: float,
        line_coverage: float,
        mutation_score: float,
        missed_branches: str,
        methods_per_agent: int = 2
    ) -> List[Tuple[str, str]]:
        """
        Generate targeted test methods using multiple parallel agents.
        
        Args:
            test_file_path: Path to the existing test file
            source_code_path: Path to the source code under test
            branch_coverage: Current branch coverage percentage
            line_coverage: Current line coverage percentage
            mutation_score: Current mutation score percentage
            missed_branches: Description of missed branches from JaCoCo
            methods_per_agent: Number of methods each agent should generate
            
        Returns:
            List of all generated (method_name, method_code) tuples from all agents
        """
        print(f"\n  [PlateauEscapeAgent] Generating targeted tests...")
        print(f"    Coverage gaps: branch={branch_coverage:.1f}%, line={line_coverage:.1f}%, mutation={mutation_score:.1f}%")
        
        # Read test file and extract context
        test_file_code = read_java_file_as_string(test_file_path)
        if test_file_code is None:
            print(f"    ERROR: Could not read test file at {test_file_path}")
            return []
        
        context = extract_test_context(test_file_code)
        context_string = context['context_string']
        
        # Read source code
        source_code = read_java_file_as_string(source_code_path)
        if source_code is None:
            print(f"    ERROR: Could not read source code at {source_code_path}")
            return []
        
        # Use all strategies for maximum diversity (like initial generation)
        strategies = get_all_injection_strategies()
        
        print(f"    Using all {len(strategies)} strategies: {[s[0] for s in strategies]}")
        
        # Run all agents in parallel
        tasks = [
            self._generate_with_strategy(
                strategy_name=strategy_name,
                temperature=temperature,
                context_string=context_string,
                source_code=source_code,
                branch_coverage=branch_coverage,
                line_coverage=line_coverage,
                mutation_score=mutation_score,
                missed_branches=missed_branches,
                methods_count=methods_per_agent
            )
            for strategy_name, temperature in strategies
        ]
        
        results = await asyncio.gather(*tasks)
        
        # Merge all generated methods
        all_methods = []
        for methods in results:
            all_methods.extend(methods)
        
        print(f"    Total methods generated: {len(all_methods)}")
        return all_methods
    
    def deduplicate_methods(
        self,
        methods: List[Tuple[str, str]],
        existing_names: set
    ) -> List[Tuple[str, str]]:
        """
        Deduplicate generated methods and resolve name conflicts.
        
        Args:
            methods: List of (method_name, method_code) tuples
            existing_names: Set of existing method names in the test file
            
        Returns:
            Deduplicated list with unique method names
        """
        seen_names = set(existing_names)
        unique_methods = []
        
        for method_name, method_code in methods:
            # Skip if we already have this exact method name from another agent
            final_name = method_name
            suffix_counter = 1
            
            while final_name in seen_names:
                final_name = f"{method_name}_v{suffix_counter}"
                suffix_counter += 1
            
            # Update method code with new name if renamed
            if final_name != method_name:
                import re
                method_code = re.sub(
                    rf'(void\s+){re.escape(method_name)}(\s*\()',
                    rf'\g<1>{final_name}\g<2>',
                    method_code
                )
            
            seen_names.add(final_name)
            unique_methods.append((final_name, method_code))
        
        return unique_methods


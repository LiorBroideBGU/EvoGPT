API_KEY = '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'
CLASS_PATH = 'benchmarks/gson/src/main/java/com/google/gson/JsonArray.java'
TEMPERATURE = 0.5
MODEL = 'gpt-4o-mini'
EVO_GENERATIONS = 25 # Change to your desired number of generations
EVO_POPULATION = 25 # Change to your desired population size
PROJECT = 'gson'
JAVAC_BIN = '/opt/homebrew/opt/openjdk@11/bin/javac'
JAVA_BIN = '/opt/homebrew/opt/openjdk@11/bin/java'

# Mutation Strategy Configuration
# Options: 'llm' (uses MutationAssertionGenerator with LLM) or 'programmatic' (uses EvoSuite-style mutations)
MUTATION_STRATEGY = 'programmatic'

# Programmatic Mutation Settings (only used when MUTATION_STRATEGY='programmatic')
PROGRAMMATIC_MUTATION_PROBABILITY = 0.3  # Probability of mutating each test method (0.0-1.0)
# Performance Optimizations
PARALLEL_FITNESS_EVALUATION = True  # Compute offspring fitness in parallel
PITEST_THREADS = 2  # Number of threads for PITest
PITEST_TIMEOUT = 60  # Reduced timeout for PITest (seconds)

# Diversity Analysis Settings
PRESERVE_INITIAL_POOL = False  # Keep all initial test suites for diversity analysis

# CodaMosa-style LLM Injection Configuration
# When evolutionary algorithm stagnates, inject LLM-generated test methods into best chromosome
LLM_INJECTION_ENABLED = True         # Toggle to enable/disable injection feature
STAGNATION_THRESHOLD = 5              # Iterations without fitness improvement before injection
MIN_FITNESS_IMPROVEMENT = 0.5         # Minimum fitness delta to reset stagnation counter
INJECTION_AGENTS_COUNT = 3            # Number of parallel agents per injection (1-5)
MAX_INJECTIONS = 3                    # Maximum injection attempts per evolutionary run


API_KEY = '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'
CLASS_PATH = '/Users/liorbr/PycharmProjects/EvoGPT/benchmarks/gson/src/main/java/com/google/gson/JsonArray.java'
TEMPERATURE = 0.5
MODEL = 'gpt-4o-mini'
EVO_GENERATIONS = 25
EVO_POPULATION = 25
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
PRESERVE_INITIAL_POOL = True  # Keep all initial test suites for diversity analysis

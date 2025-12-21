# EvoGPT 🧬🤖

**EvoGPT** is a hybrid framework that integrates LLM-based test generation with evolutionary search techniques to create diverse, fault-revealing Java unit tests. The system uses genetic programming principles combined with multi-agent LLM architectures to evolve test suites that maximize code coverage and mutation testing scores.

## 🚀 Key Features

- **Evolutionary Test Generation**: Uses genetic algorithms to evolve unit test suites over multiple generations with EvoSuite-style crossover and mutation operators
- **Multi-Agent LLM System**: Employs 5 specialized LLM agents with distinct "personalities" (temperature + system prompt combinations) for diverse test generation:
  - **Default** (temp 0.3): Balanced approach
  - **High Coverage** (temp 0.4): Focuses on maximizing code coverage
  - **Edge Case Explorer** (temp 0.5): Targets boundary conditions and edge cases
  - **Assertion Heavy** (temp 0.6): Emphasizes comprehensive assertion coverage
  - **Bug Detector** (temp 0.8): Aggressive mutation-killing tests
- **Generation-Repair Loop**: Automatic syntax and compilation error repair through iterative LLM feedback
- **Coverage-Guided Enhancement**: JaCoCo integration for targeted test enhancement based on missed branches
- **Mutation Testing**: PITest integration for mutation score calculation and test quality assessment
- **CodaMosa-Inspired Plateau Escape**: LLM injection mechanism to escape evolutionary stagnation
- **Async Parallel Execution**: Concurrent chromosome generation and fitness evaluation for improved performance

## 🛠️ Installation

### Prerequisites

- Python 3.8+
- Java Development Kit (JDK) 11+
- OpenAI API key

### Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd EvoGPT
   ```

2. **Create and activate virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install Python dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure the system**:
   Edit `config/config.py` with your settings:
   ```python
   API_KEY = 'your-openai-api-key'
   JAVAC_BIN = '/path/to/javac'
   JAVA_BIN = '/path/to/java'
   CLASS_PATH = '/path/to/your/java/source/File.java'
   PROJECT = 'project-name'
   ```

## 📋 Configuration

The main configuration file is located at `config/config.py`:

### Core Settings
| Parameter | Default | Description |
|-----------|---------|-------------|
| `API_KEY` | - | OpenAI API key |
| `MODEL` | `'gpt-4o-mini'` | LLM model to use |
| `TEMPERATURE` | `0.5` | Base temperature for generation |
| `CLASS_PATH` | - | Path to Java source file under test |
| `PROJECT` | - | Project/benchmark name |
| `JAVA_BIN` / `JAVAC_BIN` | - | Paths to Java executables |

### Evolutionary Parameters
| Parameter | Default | Description |
|-----------|---------|-------------|
| `EVO_GENERATIONS` | `25` | Number of offspring pairs to generate |
| `EVO_POPULATION` | `25` | Initial population size |

### Mutation Strategy
| Parameter | Default | Description |
|-----------|---------|-------------|
| `MUTATION_STRATEGY` | `'programmatic'` | `'llm'` or `'programmatic'` |
| `PROGRAMMATIC_MUTATION_PROBABILITY` | `0.3` | Probability of mutating each test method |

### Performance Optimizations
| Parameter | Default | Description |
|-----------|---------|-------------|
| `PARALLEL_FITNESS_EVALUATION` | `True` | Compute offspring fitness in parallel |
| `PITEST_THREADS` | `2` | Number of threads for PITest |
| `PITEST_TIMEOUT` | `60` | Timeout for PITest (seconds) |

### CodaMosa-Style LLM Injection
| Parameter | Default | Description |
|-----------|---------|-------------|
| `LLM_INJECTION_ENABLED` | `True` | Enable plateau escape mechanism |
| `STAGNATION_THRESHOLD` | `5` | Iterations without improvement before injection |
| `MIN_FITNESS_IMPROVEMENT` | `0.5` | Minimum fitness delta to reset stagnation |
| `INJECTION_AGENTS_COUNT` | `3` | Parallel agents per injection |
| `MAX_INJECTIONS` | `3` | Maximum injection attempts per run |

## 🎯 Usage

### Basic Usage

Run the evolutionary test generation:

```bash
python main.py
```

This will:
1. Generate an initial population of test suites using diverse LLM agents (5 temperature/personality combinations)
2. Apply generation-repair loops to fix syntax and compilation errors
3. Enhance tests with coverage-guided assertions
4. Evaluate fitness based on coverage and mutation scores
5. Evolve the population through crossover and mutation operations
6. Output the best test suite to `results/unit_tests/{project}/{class}/`

### Working with Benchmarks

The project includes Java benchmarks in the `benchmarks/` directory (as zip files):

**Apache Commons:**
- `commons-cli`, `commons-csv`, `commons-codec`, `commons-collections`
- `commons-compress`, `commons-jxpath`, `commons-lang`, `commons-math`

**JSON/Data Processing:**
- `gson`, `jackson-core`, `jackson-databind`, `jackson-dataformat-xml`

**Other Libraries:**
- `jfreechart`, `joda-time`, `jsoup`, `mockito`, `closure-compiler`

To use a benchmark:
1. Extract the desired benchmark zip file
2. Update `CLASS_PATH` in `config/config.py` to point to a Java source file
3. Update `PROJECT` to match the benchmark name
4. Run `python main.py`

## 📁 Project Structure

```
EvoGPT/
├── app/                              # Core evolutionary algorithm
│   ├── chromosome.py                 # Chromosome representation and fitness
│   └── chromosomes_generator.py      # Population generation and evolution loop
├── benchmarks/                       # Java project benchmarks (zip files)
├── config/
│   └── config.py                     # Configuration parameters
├── lib/jars/                         # Java dependencies (JUnit, Mockito, etc.)
├── llm_agents/                       # LLM agent implementations
│   ├── llm_agent.py                  # Base LLM agent with memory
│   ├── unit_test_generator.py        # Initial test generation
│   ├── coverage_enhancement_agent.py # Coverage-guided enhancement
│   ├── mutation_assertion_generation_agent.py  # LLM mutation operator
│   ├── plateau_escape_agent.py       # CodaMosa-style injection agent
│   └── chat_message_history.py       # Conversation history management
├── prompts/                          # LLM prompt templates
│   ├── unit_test_generator/          # 5 personality-based system prompts
│   ├── coverage_enhancement_generator/
│   ├── mutation_assertion_generator/
│   └── plateau_escape/
├── utils/
│   ├── java_executor.py              # Java compilation and execution
│   ├── function_utils.py             # General utilities
│   ├── dataset_utils.py              # Dataset processing
│   ├── programmatic_mutator.py       # EvoSuite-style mutation operators
│   ├── JavaCodeCoverage/             # JaCoCo integration
│   │   └── Jacoco.py
│   └── MutationScoreGenerator/       # PITest integration
│       └── PITest.py
├── results/                          # Generated test suites
├── main.py                           # Entry point
├── requirements.txt                  # Python dependencies
└── README.md
```

## 🧬 How It Works

### 1. Chromosome Representation
Each chromosome represents a complete Java test suite file with:
- Test methods with `@Test` annotations
- Import statements and class fields
- Fitness metrics (branch coverage, line coverage, mutation score)

### 2. Initial Population Generation
- **N chromosomes** generated in parallel using async execution
- Each uses one of 5 temperature/personality combinations
- Generation-repair loop fixes compilation errors
- Coverage enhancement adds assertions for missed branches

### 3. Fitness Evaluation
Fitness is computed as a weighted combination:
```
Fitness = 0.3 × Branch Coverage + 0.2 × Line Coverage + 0.5 × Mutation Score
```
This design emphasizes mutation score, the primary indicator of fault detection capability.

### 4. Evolutionary Operations

**Selection**: Rank-based probabilistic selection favoring higher fitness chromosomes

**Crossover** (EvoSuite-style):
- Extract test methods from two parent chromosomes
- Split at random crossover point α
- Child 1: Parent1[0:α] + Parent2[α:end]
- Child 2: Parent2[0:α] + Parent1[α:end]
- Resolve method name collisions with `_co` suffix

**Mutation** (configurable strategy):
- **Programmatic** (EvoSuite-inspired):
  - Statement deletion (remove non-assertion statements)
  - Primitive modification (change numeric/string literals)
  - Assertion removal (weaken tests to find minimal set)

### 5. Plateau Escape (Optional)
When evolution stagnates (no fitness improvement for N iterations):
1. All 5 LLM agent personalities generate targeted test methods in parallel
2. Methods focus on uncovered branches identified by JaCoCo
3. Generated methods are injected into the best chromosome
4. Fitness is recomputed

## 🔧 Dependencies

### Python Packages
- **langchain** / **langchain-openai**: LLM integration framework
- **javalang**: Java code parsing and analysis
- **numpy**: Scientific computing utilities
- **pytest** / **pytest-asyncio**: Testing framework

### Java Tools (included in `lib/jars/`)
- **JaCoCo**: Code coverage analysis
- **PITest**: Mutation testing framework
- **JUnit 4**: Unit testing framework
- **Mockito**: Mocking framework
- **Byte Buddy**: Runtime code generation (for Mockito)

## 📊 Metrics and Evaluation

The system provides comprehensive metrics for each test suite:

| Metric | Description |
|--------|-------------|
| **Line Coverage** | Percentage of source code lines executed |
| **Branch Coverage** | Percentage of conditional branches taken |
| **Mutation Score** | Percentage of artificial defects (mutants) detected |
| **Test Strength** | Overall test quality indicator from PITest |
| **Fitness Score** | Weighted combination used for evolution |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔬 Research

EvoGPT combines evolutionary computation with large language models for automated test generation. The approach demonstrates how AI can enhance traditional search-based software testing through intelligent generation and mutation strategies.

### 📄 Publication

TODO
---

**Note**: Ensure your OpenAI API key and Java environment are properly configured before running the system. The API key should have sufficient credits for LLM calls during test generation.

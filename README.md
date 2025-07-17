# EvoGPT 🧬🤖

**EvoGPT** is an evolutionary algorithm-based framework that leverages Large Language Models (LLMs) to automatically generate high-quality Java unit tests. The system uses genetic programming principles to evolve test suites that maximize code coverage and mutation testing scores.

## 🚀 Features

- **Evolutionary Test Generation**: Uses genetic algorithms to evolve unit test suites over multiple generations
- **LLM-Powered**: Integrates with OpenAI's GPT models for intelligent test case generation
- **Multi-Agent System**: Employs specialized LLM agents for different tasks:
  - Unit test generation
  - Coverage enhancement
  - Mutation assertion generation
- **Comprehensive Coverage Analysis**: Integrates with JaCoCo for detailed code coverage metrics
- **Mutation Testing**: Uses PITest for mutation score calculation and test quality assessment
- **Multi-threaded Execution**: Supports parallel chromosome generation for improved performance
- **Java Code Analysis**: Advanced parsing and analysis of Java source code using javalang

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

2. **Install Python dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

3. **Configure the system**:
   Edit `config/config.py` with your settings:
   ```python
   API_KEY = 'your-openai-api-key'
   JAVAC_BIN = '/path/to/javac'
   JAVA_BIN = '/path/to/java'
   CLASS_PATH = '/path/to/your/java/class'
   PROJECT = 'java-benchmark-name'
   ```

## 📋 Configuration

The main configuration file is located at `config/config.py`. Key settings include:

- **API_KEY**: Your OpenAI API key
- **MODEL**: GPT model to use (default: 'gpt-4o-mini')
- **TEMPERATURE**: Model temperature for generation variety
- **EVO_TIME_LIMIT**: Maximum evolution time in seconds
- **EVO_POPULATION**: Size of the chromosome population
- **JAVA_BIN** / **JAVAC_BIN**: Paths to Java executables

## 🎯 Usage

### Basic Usage

Run the evolutionary test generation process:

```bash
python main.py
```

This will:
1. Generate an initial population of test chromosomes
2. Evaluate fitness based on coverage and mutation scores
3. Evolve the population through crossover and mutation
4. Output the best test suite

### Customizing Generation

The system supports different LLM agent personalities with varying temperatures:
- **0.3**: Default balanced approach
- **0.4**: High coverage focused
- **0.5**: Edge case explorer
- **0.6**: Assertion-heavy tests
- **0.8**: Bug detector mode

### Working with Benchmarks

The project includes several Java benchmarks in the `benchmarks/` directory:
- **commons-cli**: Apache Commons CLI library
- **commons-csv**: Apache Commons CSV library
- **gson**: Google's JSON library
- **jfreechart**: Java chart library
- **lang**: Apache Commons Lang library

## 📁 Project Structure

```
EvoGPT/
├── app/                          # Core application logic
│   ├── chromosome.py             # Chromosome representation and operations
│   └── chromosomes_generator.py  # Population generation and evolution
├── benchmarks/                   # Test benchmarks (Java projects)
│   ├── commons-cli/
│   ├── commons-csv/
│   ├── gson/
│   └── jfreechart/
├── config/                       # Configuration files
│   └── config.py
├── lib/jars/                     # Java dependencies (JAR files)
├── llm_agents/                   # LLM agent implementations
│   ├── llm_agent.py             # Base LLM agent class
│   ├── unit_test_generator.py   # Unit test generation agent
│   ├── coverage_enhancement_agent.py
│   └── mutation_assertion_generation_agent.py
├── prompts/                      # LLM prompts for different tasks
├── utils/                        # Utility modules
│   ├── java_executor.py         # Java compilation and execution
│   ├── function_utils.py        # General utility functions
│   ├── dataset_utils.py         # Dataset processing utilities
│   ├── JavaCodeCoverage/        # JaCoCo integration
│   └── MutationScoreGenerator/   # PITest integration
├── main.py                       # Main entry point
└── requirements.txt              # Python dependencies
```

## 🧬 How It Works

### 1. Chromosome Representation
Each chromosome represents a complete Java unit test file with:
- Test methods
- Import statements
- Class fields
- Fitness metrics (coverage, mutation score)

### 2. Fitness Evaluation
Fitness is calculated using:
- **Branch Coverage** (30% weight)
- **Line Coverage** (20% weight)  
- **Mutation Score** (50% weight)

### 3. Evolutionary Operations
- **Selection**: Ranked selection based on fitness
- **Crossover**: Intelligent merging of test methods from parent chromosomes
- **Mutation**: LLM-guided test enhancement and assertion generation

### 4. Multi-Agent Collaboration
Different LLM agents specialize in:
- Generating diverse test cases
- Enhancing coverage for missed branches
- Adding stronger assertions for mutation testing

## 🔧 Dependencies

### Python Packages
- **langchain**: LLM integration framework
- **langchain-openai**: OpenAI API integration
- **javalang**: Java code parsing and analysis
- **numpy**: Scientific computing utilities

### Java Tools
- **JaCoCo**: Code coverage analysis
- **PITest**: Mutation testing framework
- **JUnit**: Unit testing framework

## 📊 Metrics and Evaluation

The system provides comprehensive metrics:
- **Line Coverage**: Percentage of code lines executed
- **Branch Coverage**: Percentage of code branches taken
- **Mutation Score**: Percentage of mutants killed by tests
- **Test Strength**: Overall quality assessment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔬 Research

EvoGPT combines evolutionary computation with large language models for automated test generation. The approach demonstrates how AI can be used to solve complex software engineering problems through intelligent search and generation strategies.

### 📄 Publication

This work is described in the research paper:

**[EvoGPT: Enhancing Test Suite Robustness via LLM-Based Generation and Genetic Optimization](https://arxiv.org/abs/2505.12424)**  
*Lior Broide, Roni Stern*  
arXiv:2505.12424 [cs.SE]

> **Abstract**: Large Language Models (LLMs) have recently emerged as promising tools for automated unit test generation. We introduce a hybrid framework called EvoGPT that integrates LLM-based test generation with evolutionary search techniques to create diverse, fault-revealing unit tests. Unit tests are initially generated with diverse temperature sampling to maximize behavioral and test suite diversity, followed by a generation-repair loop and coverage-guided assertion enhancement. The resulting test suites are evolved using genetic algorithms, guided by a fitness function prioritizing mutation score over traditional coverage metrics. This design emphasizes the primary objective of unit testing-fault detection. Evaluated on multiple open-source Java projects, EvoGPT achieves an average improvement of 10% in both code coverage and mutation score compared to LLMs and traditional search-based software testing baselines.

## 📞 Support

For questions, issues, or contributions, please open an issue in the GitHub repository.

---

**Note**: Make sure to configure your OpenAI API key and Java environment before running the system. 
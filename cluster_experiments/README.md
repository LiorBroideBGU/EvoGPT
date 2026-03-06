# Cluster Experiments

This directory contains scripts to run EvoGPT experiments on a SLURM cluster. You can run **main.py** (evolutionary test generation on a single class) or **run_comparison.py** (EvoGPT vs EvoSuite vs TestART vs Hybrid comparison across projects).

---

## Prerequisites

- **SLURM** cluster with `sbatch` available
- **Anaconda** module loaded (`module load anaconda`)
- **Conda environment** named `evogpt` (or override with `--conda-env`)
- **Benchmarks** in `benchmarks/` as `.zip` files (e.g. `benchmarks/gson.zip`)

---

## Overview

| Script | Purpose |
|--------|---------|
| `run_single_project.py` | Generate a job that runs **main.py** on one benchmark class |
| `run_comparison_batch.py` | Generate jobs that run **run_comparison.py** on one or more projects |

Both scripts:

1. Create project-specific JSON config files
2. Generate `.sbatch` files from `template.sbatch`
3. Optionally submit jobs with `sbatch`

Generated files go to `cluster_experiments/jobs/` by default (configs in `jobs/configs/`, sbatch files in `jobs/`).

---

## Running main.py (Single Project / Single Class)

**main.py** runs EvoGPT's evolutionary test generation on a single Java class. It uses a config file to specify the project, class path, and evolution parameters.

### Assumption: Benchmarks Are Zipped

Before execution, benchmarks are expected to be zipped (e.g. `benchmarks/gson.zip`). The job config sets `run.mode="local"` so **main.py** extracts the benchmark at runtime before running. Because sources are zipped, you must provide the class path explicitly—auto-discovery is not possible.

### Step 1: Create the Job

From the **EvoGPT project root**:

```bash
python cluster_experiments/run_single_project.py \
  --project gson \
  --class-path "benchmarks/gson/src/main/java/com/google/gson/JsonArray.java"
```

This creates:

- `cluster_experiments/jobs/configs/config_gson.json` — project config with `run.mode=local`
- `cluster_experiments/jobs/run_gson.sbatch` — SLURM job script

### Step 2: Submit the Job (Optional)

Add `--submit` to submit immediately:

```bash
python cluster_experiments/run_single_project.py \
  --project gson \
  --class-path "benchmarks/gson/src/main/java/com/google/gson/JsonArray.java" \
  --submit
```

Or submit manually:

```bash
cd /path/to/EvoGPT
sbatch cluster_experiments/jobs/run_gson.sbatch
```

**Important:** Submit from the EvoGPT project root so `SLURM_SUBMIT_DIR` points to the correct directory.

### Class Path Format

The `--class-path` must:

- Start with `benchmarks/` (e.g. `benchmarks/gson/src/main/java/.../X.java`)
- Match the structure inside the zip (extract and inspect if unsure)

Example for gson:

```
benchmarks/gson/src/main/java/com/google/gson/JsonArray.java
```

### Options for run_single_project.py

| Option | Default | Description |
|--------|---------|-------------|
| `--project` | (required) | Benchmark project name (e.g. gson, commons-lang) |
| `--class-path` | (required) | Path to the Java class under `benchmarks/` |
| `--config` | `config/config.json` | Base config file |
| `--output-dir` | `cluster_experiments/jobs` | Directory for generated files |
| `--conda-env` | `evogpt` | Conda environment name |
| `--submit` | — | Submit the job with sbatch after generating |
| `--cpus` | 4 | CPUs per task |
| `--mem` | 16G | Memory allocation |

---

## Running run_comparison.py (Comparison Across Projects)

**run_comparison.py** compares EvoGPT, EvoSuite, TestART, and the EvoGPT+EvoSuite hybrid on focal classes. It discovers focal classes in each project, runs all tools under the same budgets, and produces coverage/mutation metrics.

### Step 1: Create Jobs

From the **EvoGPT project root**:

**All projects in benchmarks/:**

```bash
python cluster_experiments/run_comparison_batch.py
```

**Specific projects:**

```bash
python cluster_experiments/run_comparison_batch.py --projects gson,commons-lang,commons-math
```

This creates, for each project:

- `cluster_experiments/jobs/configs/config_comparison_<project>.json`
- `cluster_experiments/jobs/comparison_<project>.sbatch`

### Step 2: Submit Jobs (Optional)

Add `--submit` to submit all jobs:

```bash
python cluster_experiments/run_comparison_batch.py --projects gson,commons-lang --submit
```

Or submit manually:

```bash
cd /path/to/EvoGPT
sbatch cluster_experiments/jobs/comparison_gson.sbatch
sbatch cluster_experiments/jobs/comparison_commons-lang.sbatch
```

### Sequential Execution

To run jobs one after another (e.g. to avoid resource contention), use `--sequential`:

```bash
python cluster_experiments/run_comparison_batch.py --projects gson,commons-lang --sequential --submit
```

Each job will depend on the previous one completing successfully.

### Options for run_comparison_batch.py

| Option | Default | Description |
|--------|---------|-------------|
| `--projects` | (all in benchmarks/) | Comma-separated project names |
| `--config` | `config/config.json` | Base config file |
| `--output-dir` | `cluster_experiments/jobs` | Directory for generated files |
| `--comparison-output-dir` | `comparison_results` | Base dir for comparison results (per-project subdirs) |
| `--conda-env` | `evogpt` | Conda environment name |
| `--submit` | — | Submit all jobs with sbatch after generating |
| `--sequential` | — | Add job dependencies so jobs run one after another |
| `--cpus` | 4 | CPUs per task |
| `--mem` | 16G | Memory allocation |

---

## What main.py Does

**main.py** runs evolutionary unit test generation:

1. Loads config (project, class path, evolution parameters)
2. If `run.mode="local"`: extracts `benchmarks/<project>.zip` to `evogpt_extracted/`
3. Runs `ChromosomesGenerator` to evolve tests for the target class
4. Writes results to `results/unit_tests/<project>/<class>/`

**Usage (direct, not via cluster):**

```bash
python main.py --config config/config.json
```

For cluster jobs, the generated config sets `run.mode="local"` and `run.output_dir="evogpt_extracted"` so extraction happens automatically.

---

## What run_comparison.py Does

**run_comparison.py** runs a full comparison:

1. Loads config and resolves targets (from `comparison.project`, `comparison.batch_config`, or `comparison.class_path`)
2. For each target class: runs TestART, EvoGPT, EvoSuite, and EvoGPT+EvoSuite hybrid
3. Evaluates each test with JaCoCo (coverage) and PITest (mutation)
4. Appends results to `comparison_results/<project>/results.csv`
5. Prints a summary table

**Usage (direct, not via cluster):**

```bash
python run_comparison.py --config config/config.json
```

For cluster jobs, each project gets its own config with `comparison.project` set and `comparison.output_dir` pointing to `comparison_results/<project>/`.

---

## Template and Generated Jobs

The `template.sbatch` file defines the job structure. Scripts substitute placeholders such as:

- `job_name`, `cpus_per_task`, `mem`
- `conda_env`, `work_dir` (default: `${SLURM_SUBMIT_DIR}`)
- `script`, `arguments` (e.g. `main.py --config ...`)
- `cluster_temp_logs_path`, `logs_dir`

Jobs:

1. Load anaconda and activate the conda environment
2. Set `PYTHONPATH` to the project root
3. Run the Python script
4. Copy logs from the cluster temp dir to `logs_dir`

---

## Workflow Summary

### main.py (single class)

```text
1. run_single_project.py --project X --class-path benchmarks/X/.../Y.java
   → Creates jobs/configs/config_X.json, jobs/run_X.sbatch

2. sbatch jobs/run_X.sbatch  (from project root)

3. Job runs: main.py --config jobs/configs/config_X.json
   → Extracts benchmark, runs EvoGPT, writes to results/
```

### run_comparison.py (full comparison)

```text
1. run_comparison_batch.py [--projects A,B,C]
   → Creates jobs/configs/config_comparison_A.json, jobs/comparison_A.sbatch, etc.

2. sbatch jobs/comparison_A.sbatch  (from project root)
   (or use --submit to submit all)

3. Job runs: run_comparison.py --config jobs/configs/config_comparison_A.json
   → Discovers focal classes, runs all tools, writes to comparison_results/A/
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `No such file or directory` for config | Submit from EvoGPT project root; paths are relative |
| `ModuleNotFoundError` | Ensure conda env is activated and has all dependencies |
| Benchmarks not found | Ensure `benchmarks/<project>.zip` exists |
| Wrong class path | Inspect zip contents; path must match structure inside zip |
| Job fails immediately | Check `slurm-<JOBID>.out` and `slurm-error-<JOBID>.out` |

---

## File Layout

```text
cluster_experiments/
├── README.md              # This file
├── template.sbatch        # SLURM template
├── sbatch_utils.py        # Shared template substitution
├── run_single_project.py  # Jobs for main.py
├── run_comparison_batch.py # Jobs for run_comparison.py
└── jobs/                  # Generated (gitignored)
    ├── configs/
    │   ├── config_<project>.json
    │   └── config_comparison_<project>.json
    ├── run_<project>.sbatch
    ├── comparison_<project>.sbatch
    └── logs/
```

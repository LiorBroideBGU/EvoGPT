"""
Config loader: loads configuration from JSON and provides a mutable Config instance.
Must call load(path) before any module that uses config is imported.
"""

import json
from pathlib import Path
from typing import Any


def _get(data: dict, key: str, default: Any) -> Any:
    """Helper to safely get a value, handling nested keys."""
    return data.get(key, default)


class Config:
    """Mutable config loaded from JSON. All fields have defaults."""

    def __init__(self, data: dict):
        # Core settings
        self.API_KEY = _get(data, "API_KEY", "<your-api-key>")
        self.CLASS_PATH = _get(
            data, "CLASS_PATH",
            "benchmarks/gson/gson/src/main/java/com/google/gson/JsonArray.java"
        )
        self.PROJECT = _get(data, "PROJECT", "gson")
        self.PROJECT_ROOT = _get(data, "PROJECT_ROOT", "benchmarks")
        self.TEMPERATURE = float(_get(data, "TEMPERATURE", 0.5))
        self.MODEL = _get(data, "MODEL", "gpt-4o-mini")
        self.EVO_GENERATIONS = int(_get(data, "EVO_GENERATIONS", 25))
        self.EVO_POPULATION = int(_get(data, "EVO_POPULATION", 25))
        self.JAVAC_BIN = _get(data, "JAVAC_BIN", "/usr/bin/javac")
        self.JAVA_BIN = _get(data, "JAVA_BIN", "/usr/bin/java")
        self.MUTATION_STRATEGY = _get(data, "MUTATION_STRATEGY", "programmatic")
        self.PROGRAMMATIC_MUTATION_PROBABILITY = float(
            _get(data, "PROGRAMMATIC_MUTATION_PROBABILITY", 0.3)
        )
        self.PARALLEL_FITNESS_EVALUATION = bool(
            _get(data, "PARALLEL_FITNESS_EVALUATION", True)
        )
        self.PITEST_THREADS = int(_get(data, "PITEST_THREADS", 2))
        self.PITEST_TIMEOUT = int(_get(data, "PITEST_TIMEOUT", 60))
        self.PRESERVE_INITIAL_POOL = bool(_get(data, "PRESERVE_INITIAL_POOL", False))
        self.LLM_INJECTION_ENABLED = bool(_get(data, "LLM_INJECTION_ENABLED", True))
        self.STAGNATION_THRESHOLD = int(_get(data, "STAGNATION_THRESHOLD", 5))
        self.MIN_FITNESS_IMPROVEMENT = float(_get(data, "MIN_FITNESS_IMPROVEMENT", 0.5))
        self.INJECTION_AGENTS_COUNT = int(_get(data, "INJECTION_AGENTS_COUNT", 3))
        self.MAX_INJECTIONS = int(_get(data, "MAX_INJECTIONS", 3))

        # main.py run section
        run_data = _get(data, "run", {}) or {}
        self.run = {
            "mode": run_data.get("mode"),
            "url": run_data.get("url"),
            "output_dir": run_data.get("output_dir"),
            "class_path": run_data.get("class_path"),
        }

        # run_comparison.py comparison section
        comp_data = _get(data, "comparison", {}) or {}
        self.comparison = {
            "project": comp_data.get("project"),
            "class_path": comp_data.get("class_path"),
            "limit": comp_data.get("limit"),
            "batch_config": comp_data.get("batch_config"),
            "budget_type": comp_data.get("budget_type", "generations"),
            "budgets": comp_data.get("budgets", [5, 10, 25]),
            "evogpt_population": comp_data.get("evogpt_population", 25),
            "output_dir": comp_data.get("output_dir", "comparison_results"),
            "skip_evogpt": comp_data.get("skip_evogpt", False),
            "skip_evosuite": comp_data.get("skip_evosuite", False),
            "skip_testart": comp_data.get("skip_testart", False),
            "skip_hybrid": comp_data.get("skip_hybrid", False),
            "hybrid_population": comp_data.get("hybrid_population", 5),
            "download_evosuite": comp_data.get("download_evosuite", False),
        }


_config: Config | None = None


def load(path: str | Path) -> Config:
    """Load config from JSON. Must be called before any module that uses config is imported."""
    global _config
    with open(path, encoding="utf-8") as f:
        _config = Config(json.load(f))
    return _config


def get() -> Config:
    """Return the loaded config. Raises if load() was not called."""
    if _config is None:
        raise RuntimeError(
            "Config not loaded. Call config.config_loader.load(path) first."
        )
    return _config


# Alias for clarity
get_config = get

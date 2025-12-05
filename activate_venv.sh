#!/bin/bash
# EvoGPT Virtual Environment Activation Script
# Usage: source activate_venv.sh

if [ -d "venv" ]; then
    echo "🚀 Activating EvoGPT virtual environment..."
    source venv/bin/activate
    echo "✅ Virtual environment activated!"
    echo "📍 You can now run your Python scripts with all dependencies available."
    echo ""
    echo "Available key modules:"
    echo "  - langchain (for LLM integration)"  
    echo "  - javalang (for Java code parsing)"
    echo "  - numpy (for scientific computing)"
    echo "  - pytest (for testing)"
    echo ""
    echo "To deactivate, simply run: deactivate"
else
    echo "❌ Virtual environment not found. Please run 'python3 -m venv venv' first."
fi

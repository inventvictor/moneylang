#!/bin/bash

# MoneyLang Test Runner
# This script runs all tests for the MoneyLang project

set -e

echo "🧪 Running MoneyLang Tests..."
echo "=================================="

# Run all unit tests

echo "Running DSL tests..."
bazel test //app/test:moneylang_dsl_tests --test_output=errors

echo "Running integration tests..."
bazel test //app/test:moneylang_integration_tests --test_output=errors

echo "Running transaction flow tests..."
bazel test //app/test:moneylang_processor_tests --test_output=errors

echo ""
echo "✅ All tests completed successfully!"
echo ""
echo "💡 You can also run all tests at once with:"
echo "   bazel test //app/test:all_tests"
echo ""
echo "🔍 For more verbose output, add --test_output=all"
echo "   bazel test //app/test:all_tests --test_output=all"
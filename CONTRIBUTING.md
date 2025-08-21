# Contributing to MoneyLang

Thank you for your interest in contributing to MoneyLang! This guide will help you get set up and running with the development environment.

## Prerequisites

Before you begin, make sure you have the following installed on your system:

### Required
- **Java 11 or higher** - MoneyLang is built with Kotlin/JVM
- **Bazelisk** - For managing Bazel versions automatically
- **Git** - For version control

### Installation

#### 1. Install Java
Make sure you have Java 11 or higher installed:
```bash
# Check your Java version
java -version

# On macOS with Homebrew
brew install openjdk@17

# On Ubuntu/Debian
sudo apt update && sudo apt install openjdk-17-jdk

# On Windows
# Download and install from https://adoptium.net/
```

#### 2. Install Bazelisk (Recommended)
Bazelisk automatically downloads and manages the correct Bazel version for the project:

```bash
# On macOS with Homebrew
brew install bazelisk

# On Linux
curl -L https://github.com/bazelbuild/bazelisk/releases/latest/download/bazelisk-linux-amd64 -o /usr/local/bin/bazel
chmod +x /usr/local/bin/bazel

# On Windows
# Download bazelisk-windows-amd64.exe from https://github.com/bazelbuild/bazelisk/releases
# Rename to bazel.exe and add to your PATH
```

#### 3. Verify Installation
```bash
bazel version
```

## Setting Up the Development Environment

### 1. Fork and Clone the Repository
```bash
# Fork the repo on GitHub first, then:
git clone https://github.com/yourusername/moneylang.git
cd moneylang
```

### 2. Build the Project
```bash
# Build everything
bazel build //...

# Build just the main application
bazel build //app:moneylang
```

### 3. Run Tests
```bash
# Run all tests
bazel test //...

# Run linting
bazel test //app:lint_test
```

### 4. Run the Application
```bash
# Run the main MoneyLang processor
bazel run //app:moneylang
```

## Development Workflow

### Project Structure
```
moneylang/
├── app/                    # Main application code
│   ├── BUILD.bazel        # Build configuration
│   ├── MoneyLang.kt       # Main entry point
│   ├── context/           # Context and input handling
│   ├── dsl/               # DSL implementation
│   ├── processor/         # Transaction processing
│   ├── results/           # Result handling
│   ├── stages/            # DSL stages
│   └── utils/             # Utility classes
├── BUILD.bazel            # Root build file
├── MODULE.bazel           # Bazel module configuration
├── WORKSPACE.bazel        # Workspace configuration
└── .editorconfig          # Code formatting rules
```

### Making Changes

1. **Create a Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Write Code Following Project Standards**
   - Use 2-space indentation for Kotlin files
   - Maximum line length of 100 characters
   - Follow existing naming conventions
   - Add tests for new functionality

3. **Format Your Code**
   ```bash
   # Auto-fix formatting issues
   bazel run //app:lint_fix
   ```

4. **Build and Test**
   ```bash
   # Make sure everything builds
   bazel build //...
   
   # Run tests
   bazel test //...
   
   # Check linting
   bazel test //app:lint_test
   ```

5. **Commit and Push**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Open a PR against the `main` branch
   - Provide a clear description of your changes
   - Include examples if adding new DSL features

### Useful Bazel Commands

```bash
# Build everything
bazel build //...

# Run specific target
bazel run //app:moneylang

# Run tests with output
bazel test //... --test_output=all

# Clean build cache
bazel clean

# Query build targets
bazel query //...

# Fix code formatting
bazel run //app:lint_fix

# Check code style
bazel test //app:lint_test
```

## Understanding the Codebase

### Key Components

- **MoneyLang.kt** - Main entry point and CLI interface
- **MoneyLangDsl.kt** - Core DSL implementation and builders
- **TransactionProcessor.kt** - Processes transactions and handles money movements
- **MoneyLangContext.kt** - Manages execution context and metadata
- **Transaction/Posting/Destination** - Core transaction modeling

### Dependencies

The project uses minimal external dependencies:
- **Jackson Kotlin Module** - JSON processing
- **Logback** - Logging

All dependencies are managed through Bazel's Maven integration in `WORKSPACE.bazel`.

## Code Style

The project follows these conventions:
- Kotlin coding standards with KtLint enforcement
- 2-space indentation
- 100-character line limit
- Trailing commas allowed
- Clear, descriptive naming

## Testing

When adding new features:
1. Add unit tests for core logic
2. Add integration tests for DSL functionality
3. Include edge cases and error handling
4. Test with various currencies and amounts

## Documentation

If you're adding new DSL features:
1. Update the README.md examples
2. Add code comments explaining complex logic
3. Include usage examples in your PR description

## Getting Help

- Check existing issues on GitHub
- Look at the README.md for DSL usage examples
- Review existing code for patterns and conventions
- Ask questions in your PR or create a discussion

## License

By contributing to MoneyLang, you agree that your contributions will be licensed under the same license as the project.

---

Happy coding! 🚀
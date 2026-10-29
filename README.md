# Logic Gate Simulator & Boolean Expression Lab — Java & JavaFX Desktop Application

A comprehensive digital logic desktop laboratory built in **Java 21 LTS** and **JavaFX 21** using **Maven**.

This application features two integrated simulation environments:
1. **Individual Gate Simulator:** An interactive simulation workbench modeling the 7 fundamental logic gates with vector wire graphics, dynamic LED logic levels, and file persistence.
2. **Boolean Expression Lab:** A Boolean logic evaluation and analysis laboratory powered by an independent, recursive-descent parser, dynamic $2^n$ truth table generation, step-by-step evaluation traces, and mathematical equation rendering.

---

## 1. Project Overview

The **Logic Gate Simulator** is designed for digital electronics students, computer engineering educators, and software developers:

- **Individual Gate Simulator:** Models **AND**, **OR**, **NOT**, **NAND**, **NOR**, **XOR**, and **XNOR** gates. Evaluates outputs in real time, synchronizes toggle switches, renders glowing vector wires and ANSI standard gate symbols, and maintains an active truth-table row indicator.
- **Boolean Expression Lab:** Enables arbitrary Boolean expression input over variables **A**, **B**, and **C** using 7 operator keywords (`AND`, `OR`, `NOT`, `NAND`, `NOR`, `XOR`, `XNOR`). Automatically discovers distinct variables, validates syntax with exact error positions, builds complete $2^n$ truth tables, and displays step-by-step mathematical reductions.

The application follows textbook Object-Oriented Programming (OOP) principles in Java, maintaining strict architectural separation between pure Java domain logic (`backend`) and JavaFX desktop UI (`frontend`).

---

## 2. Technology Stack

- **Language:** Java 21 (LTS)
- **GUI Framework:** JavaFX 21 (`javafx-controls`, `javafx-graphics`, `javafx-base`)
- **Build System:** Apache Maven 3.9+
- **Persistence:** Standard Java File I/O (`java.nio.file.Files`, `BufferedReader`, `BufferedWriter`)
- **Collections:** Java Standard Collections (`ArrayList`, `LinkedHashMap`, `TreeSet`, `Map`, `List`)
- **Architecture:** Recursive-descent AST Parser + Custom JavaFX UI Components
- **Target OS:** Windows 10/11 (cross-platform compatible with Linux and macOS)

---

## 3. Project Structure

```text
LogicGateSimulatorJava/
├── pom.xml                               # Maven project configuration and JavaFX 21 dependencies
├── README.md                             # Comprehensive technical documentation & user guide
├── .gitignore                            # Git ignore rules for build and state artifacts
├── build.bat                             # 1-click Windows compilation script
├── run.bat                               # 1-click Windows launch script
├── saved_state.txt                       # Persisted gate simulator state file
│
├── backend/                              # Pure Java logic engine (zero UI dependencies)
│   ├── GateType.java                     # Enum for all 7 standard logic gates
│   ├── TruthTableRow.java                # Row model for single-gate truth tables
│   ├── LogicGate.java                    # Abstract base class demonstrating OOP principles
│   ├── AndGate.java                      # Derived class: AND gate logic (a && b)
│   ├── OrGate.java                       # Derived class: OR gate logic (a || b)
│   ├── NotGate.java                      # Derived class: NOT gate logic (!a)
│   ├── NandGate.java                     # Derived class: NAND gate logic (!(a && b))
│   ├── NorGate.java                      # Derived class: NOR gate logic (!(a || b))
│   ├── XorGate.java                      # Derived class: XOR gate logic (a != b)
│   ├── XnorGate.java                     # Derived class: XNOR gate logic (a == b)
│   ├── GateFactory.java                  # Factory pattern for polymorphic gate creation
│   ├── GateStorage.java                  # State persistence and rigorous data validation
│   ├── BooleanExpression.java            # Recursive-descent parser, AST nodes & truth table engine
│   ├── ExpressionTruthTableRow.java      # Model class representing multi-variable truth table rows
│   ├── ExpressionParseException.java     # Checked exception with exact syntax error position
│   └── BackendTest.java                  # Verification test suite (179 automated tests)
│
└── frontend/                             # JavaFX graphical user interface
    ├── Main.java                         # Standard Java application bootstrap launcher
    ├── MainWindow.java                   # Main window, top navigation tabs, gate canvas & controls
    └── BooleanExpressionLabView.java     # Boolean Expression Lab UI, truth table, and step trace
```

---

## 4. Feature Guide: Boolean Expression Lab

### 4.1 Supported Syntax & Operators

The Boolean Expression Lab parses arbitrary expressions containing variables `A`, `B`, and `C`, standard parentheses `( ... )`, and arbitrary whitespace. Operator keywords are strictly case-insensitive.

| Operator | Keyword | Boolean Logic | Algebraic Symbol | Description |
|:---:|:---:|:---:|:---:|:---|
| **Parentheses** | `( ... )` | Precedence override | `( ... )` | Groups sub-expressions to control evaluation order |
| **NOT** | `NOT` | `!A` | `¬A` | Inverts the subsequent operand or sub-expression |
| **AND** | `AND` | `A && B` | `A · B` | True only when both operands are 1 |
| **NAND** | `NAND` | `!(A && B)` | `A ⊼ B` | Inverted AND: False only when both operands are 1 |
| **XOR** | `XOR` | `A != B` | `A ⊕ B` | True when inputs have differing logic levels |
| **XNOR** | `XNOR` | `A == B` | `A ⊙ B` | True when inputs are identical (Equivalence) |
| **OR** | `OR` | `A || B` | `A + B` | True when at least one operand is 1 |
| **NOR** | `NOR` | `!(A || B)` | `A ⊽ B` | Inverted OR: True only when both operands are 0 |

### 4.2 Operator Precedence

When parentheses are omitted, operators bind according to standard digital logic precedence:

```text
Level 1 (Highest): Parentheses  ( ... )
Level 2:           Unary NOT    NOT A, NOT (A OR B)   [Prefix, right-associative]
Level 3:           AND, NAND    A AND B, A NAND B     [Binary, left-associative]
Level 4:           XOR, XNOR    A XOR B, A XNOR B     [Binary, left-associative]
Level 5 (Lowest):  OR, NOR      A OR B, A NOR C       [Binary, left-associative]
```

#### Precedence Examples
- `A OR B AND C` is evaluated as `A OR (B AND C)`
- `NOT A AND B` is evaluated as `(NOT A) AND B`
- `(A OR B) AND C` forces `(A OR B)` to evaluate first
- `A XOR B AND C` is evaluated as `A XOR (B AND C)`
- `A XOR B XOR C` is evaluated as `(A XOR B) XOR C`

### 4.3 Parser & Evaluator Architecture

The expression engine in [`BooleanExpression.java`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/BooleanExpression.java) operates through a three-stage pipeline:

```
[Raw String Input]
        │
        ▼
   [Tokenizer]  ──► Produces Token stream (VARIABLE, OPERATOR, LPAREN, RPAREN, EOF)
        │            Tracks exact 0-indexed character offsets
        ▼
 [Recursive-Descent Parser] ──► Validates grammar against precedence levels
        │                         Rejects unknown symbols, consecutive tokens, unclosed parens
        ▼
[Abstract Syntax Tree (AST)]
        ├── VariableNode      (Evaluates against input Map<String, Boolean>)
        ├── NotNode           (Inverts child evaluation, records NOT step)
        └── BinaryOpNode      (Evaluates left & right, applies Boolean op, records reduction step)
        │
        ├──► evaluate(Map<String, Boolean> inputs)
        ├──► generateTruthTable() ──► 2^n rows dynamically generated
        ├──► toMathematicalString() ──► e.g. Y = (A · B) + C
        └──► getEvaluationSteps()   ──► Step-by-step reduction trace
```

### 4.4 Dynamic Truth Table Generation

1. **Automatic Variable Discovery:** The AST collects distinct variables used in the expression and sorts them alphabetically (e.g. `{A, B}`, `{A, B, C}`, or `{B, C}`).
2. **Variable Limits:** Supports 1 to 3 distinct variables. Empty expressions or expressions without variables trigger a clear validation error.
3. **Combinations:** Generates all $2^n$ truth table rows:
   - 1 variable: $2^1 = 2$ rows
   - 2 variables: $2^2 = 4$ rows
   - 3 variables: $2^3 = 8$ rows
4. **Active Row Highlighting:** As input switches (A, B, C) are toggled, the matching row in the truth table is automatically selected and highlighted.
5. **Interactive Table Inspection:** Clicking any row in the truth table immediately applies that row's binary inputs to the switches and recalculates the output.

### 4.5 Pedagogical Step-by-Step Evaluation

For any input assignment, the lab generates a clear trace of reduction steps:
```text
Expression: (A AND B) OR C   with inputs A=1, B=0, C=1
Step 1: A = 1
Step 2: B = 0
Step 3: C = 1
Step 4: (A AND B) = 1 AND 0 = 0
Step 5: (A AND B) OR C = 0 OR 1 = 1
```

---

## 5. Architectural Separation: Backend vs Frontend

### Backend (`backend` package)
- Pure Java domain logic; **zero dependency on JavaFX, AWT, or GUI packages**.
- Enforces strict encapsulation, polymorphism, and input validation.
- Completely testable in headless CI/CD environments via [`BackendTest.java`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/BackendTest.java).

### Frontend (`frontend` package)
- JavaFX desktop client responsible for presentation, user interaction, and event routing.
- Interacts with the backend exclusively through public domain methods (`BooleanExpression.parse()`, `LogicGate.evaluate()`, `GateStorage.saveSimulatorState()`).
- Seamlessly swaps between the **Individual Gate Simulator** and **Boolean Expression Lab** views using top navigation segmented buttons without tearing down application state.

---

## 6. Logic Gates and Verified Truth Tables

### 1. AND Gate (`Y = A · B`)
Output is `1` only when both inputs are `1`.

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | 0 |
| 0 | 1 | 0 |
| 1 | 0 | 0 |
| **1** | **1** | **1** |

### 2. OR Gate (`Y = A + B`)
Output is `1` when at least one input is `1`.

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | 0 |
| 0 | 1 | **1** |
| 1 | 0 | **1** |
| 1 | 1 | **1** |

### 3. NOT Gate (`Y = ¬A`)
Inverts single input A. Input B is disabled and hidden.

| A | Y |
|:---:|:---:|
| 0 | **1** |
| 1 | 0 |

### 4. NAND Gate (`Y = ¬(A · B)`)
Output is `0` only when both inputs are `1` (Inverted AND).

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | **1** |
| 0 | 1 | **1** |
| 1 | 0 | **1** |
| 1 | 1 | 0 |

### 5. NOR Gate (`Y = ¬(A + B)`)
Output is `1` only when both inputs are `0` (Inverted OR).

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | **1** |
| 0 | 1 | 0 |
| 1 | 0 | 0 |
| 1 | 1 | 0 |

### 6. XOR Gate (`Y = A ⊕ B`)
Output is `1` when inputs are different (Exclusive OR).

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | 0 |
| 0 | 1 | **1** |
| 1 | 0 | **1** |
| 1 | 1 | 0 |

### 7. XNOR Gate (`Y = ¬(A ⊕ B)`)
Output is `1` when inputs are identical (Equivalence).

| A | B | Y |
|:---:|:---:|:---:|
| 0 | 0 | **1** |
| 0 | 1 | 0 |
| 1 | 0 | 0 |
| 1 | 1 | **1** |

---

## 7. Object-Oriented Programming (OOP) Concepts Demonstrated

| # | Concept | Java Implementation | Viva / Architectural Explanation |
|---|---|---|---|
| 1 | **Classes & Objects** | [`LogicGate`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/LogicGate.java), [`BooleanExpression`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/BooleanExpression.java), [`MainWindow`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/frontend/MainWindow.java) | Real-world entities and UI elements modeled as objects with state and behavior. |
| 2 | **Encapsulation** | `private` / `protected` fields with getters | State is hidden from direct external modification; accessed via methods. |
| 3 | **Data Abstraction** | [`abstract class LogicGate`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/LogicGate.java) & `ExpressionNode` interface | Defines contracts without exposing internal Boolean mechanics or AST traversal. |
| 4 | **Inheritance** | `class AndGate extends LogicGate` | Concrete gates inherit attributes and truth table capabilities from base class. |
| 5 | **Runtime Polymorphism** | `currentGate.evaluate(a, b)` and `node.evaluate(inputs)` | Dynamic method dispatch evaluates concrete gate or AST node at runtime. |
| 6 | **Method Overriding** | `@Override public boolean evaluate(...)` | Each derived gate provides its specific Boolean evaluation implementation. |
| 7 | **Method Overloading** | `evaluate(boolean a, boolean b)` vs `evaluate(boolean a)` | Same method name with different parameter signatures for single vs two-input gates. |
| 8 | **Constructors & `super`** | `super(GateType.AND, ...)` | Derived class constructors pass specialized parameters to base constructor. |
| 9 | **Dynamic Binding** | `LogicGate.generateTruthTable()` | Base method invokes `this.evaluate(...)`, binding polymorphically to the derived class. |
| 10 | **Factory Pattern** | [`GateFactory.createGate(GateType)`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/GateFactory.java) | Centralizes polymorphic instantiation, decoupling callers from constructors. |
| 11 | **Enums** | [`GateType`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/GateType.java), `OperatorType`, `TokenType` | Type-safe enumerations preventing invalid states at compile time. |
| 12 | **File I/O & Exception Handling** | [`GateStorage`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/GateStorage.java), [`ExpressionParseException`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/ExpressionParseException.java) | Safe resource management (`try-with-resources`) and custom checked exceptions. |
| 13 | **Composition & Association** | `MainWindow` aggregates `CircuitCanvas`, `BooleanExpressionLabView` | Cohesive architectural components combined cleanly into a main controller. |

---

## 8. File Persistence (`GateStorage`)

The application saves and restores gate simulation state using a clean, human-readable text format:

```text
GATE: AND
INPUT_A: 1
INPUT_B: 0
```

### Validation & Error Handling Rules
1. **Missing Files:** Returns a `StorageResult` with `success = false` and descriptive message without altering the current UI state.
2. **Invalid Gate Names:** If a file contains an unrecognized gate (e.g. `GATE: INVALID`), the parser rejects it and returns an error. It **never silently defaults to AND**.
3. **Invalid Values:** Non-binary strings (e.g. `INPUT_A: hello`) are rejected with an explicit error.
4. **Single-Input vs Two-Input Requirements:** For two-input gates, `INPUT_B` is strictly required. For the NOT gate, `INPUT_B` is optional.
5. **Atomic Application:** If any validation error occurs during load, the existing active state in the simulator remains completely untouched.

---

## 9. Build and Execution Instructions (Windows)

### Prerequisites
- JDK 21+ installed (e.g., JetBrains Runtime `jbr` or Eclipse Temurin OpenJDK 21).
- Apache Maven 3.9+ installed.

### Option A: 1-Click Batch Scripts (Recommended)

1. **Build:**
   Double-click `build.bat` or run in terminal:
   ```cmd
   build.bat
   ```
2. **Run:**
   Double-click `run.bat` or run in terminal:
   ```cmd
   run.bat
   ```

*(The batch scripts automatically detect Maven and Java from your system environment or bundled IDE directories).*

### Option B: Standard Maven Commands (PowerShell / Terminal)

```powershell
# Set Java 21 environment if not permanently in PATH
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr"
$env:PATH = "$env:JAVA_HOME\bin;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin;$env:PATH"

# Navigate to project directory
cd "d:\Gate Simulator\LogicGateSimulatorJava"

# Compile and package
mvn clean compile
mvn package -DskipTests

# Run Backend Test Suite
java -cp target/classes backend.BackendTest

# Launch JavaFX application
mvn javafx:run
```

---

## 10. Automated Verification & Test Suite

A standalone backend test suite in [`BackendTest.java`](file:///d:/Gate%20Simulator/LogicGateSimulatorJava/backend/BackendTest.java) executes **179 automated verification assertions** covering:

1. **Individual Gate Logic:** All 7 gates across every input combination (AND, OR, NOT, NAND, NOR, XOR, XNOR).
2. **Polymorphic Truth Table Generation:** 2 rows for NOT, 4 rows for 2-input gates.
3. **Gate Factory & String Parsing:** Bidirectional conversion, strict rejection of invalid names.
4. **File Persistence & Storage Validation:** State saving, reloading, corruption detection, and missing file handling.
5. **Boolean Expression Operator Coverage:** All 7 operators evaluated with known truth values.
6. **Precedence & Parentheses:**
   - Multiplicative precedence (`AND` over `OR`, `AND` over `XOR`)
   - Unary precedence (`NOT` over `AND`)
   - Parentheses override: `(A OR B) AND C`, `NOT (A AND B)`, `(A XOR B) AND C`
   - Complex nested expressions: `(A OR B) AND (NOT C)`, `NOT (A OR (B AND (NOT C)))`, `A XOR B XOR C`
7. **Multi-Variable Dynamic Truth Tables:**
   - 1 variable: 2 rows (`NOT A`)
   - 2 variables: 4 rows (`A AND B`)
   - 3 variables: 8 rows (`(A AND B) OR C`)
   - Active input row matching validation
8. **Case-Insensitivity:** Lowercase (`a and b`, `not (a or b)`), mixed case (`(a xor B) AnD c`, `a nOR b`).
9. **Syntax Validation & Error Detection:**
   - Empty/whitespace expressions
   - Missing left/right operands (`A AND`, `AND B`, `NOT`)
   - Unknown operators / invalid symbols (`A + B`, `A & B`, `!A`, `A FOO B`)
   - Unknown variables (`D`, `X`, `Z`)
   - Unbalanced / unclosed parentheses (`(A AND B`, `A AND B)`, `()`)
   - Consecutive variables / missing operators (`A B`, `A (B OR C)`)
10. **Evaluation Steps & Mathematical Output:**
   - Step trace verification
   - Mathematical string generation (`Y = (A · B) + C`)

### Test Execution Command:
```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr\bin\java.exe" -cp target/classes backend.BackendTest
```

### Result:
```text
=================================================
  LOGIC GATE SIMULATOR - BACKEND TEST SUITE
=================================================
  [PASS] All 69 Individual Gate Simulator tests
  [PASS] All 110 Boolean Expression Lab tests
=================================================
  TEST RESULTS: 179 / 179 PASSED (100%)
=================================================
```

package backend;

import backend.GateStorage.SimulatorState;
import backend.GateStorage.StorageResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backend verification suite for Logic Gate Simulator.
 * Validates all Boolean evaluation, truth-table generation, factory creation,
 * persistence/validation edge cases, and the Boolean Expression Lab recursive-descent
 * parser and truth-table evaluator without requiring external testing frameworks.
 */
public class BackendTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  LOGIC GATE SIMULATOR - BACKEND TEST SUITE");
        System.out.println("=================================================");

        // 1. Existing Individual Gate Simulator Backend Tests
        testAndGate();
        testOrGate();
        testNotGate();
        testNandGate();
        testNorGate();
        testXorGate();
        testXnorGate();
        testGateFactory();
        testTruthTableGeneration();
        testGateStorage();

        // 2. New Boolean Expression Lab Backend Tests
        testExpressionOperators();
        testExpressionPrecedenceAndParentheses();
        testExpressionNested();
        testExpressionVariablesAndTruthTable();
        testExpressionCaseInsensitivity();
        testExpressionValidationAndErrors();
        testExpressionEvaluationStepsAndMath();

        System.out.println("=================================================");
        System.out.printf("  TEST RESULTS: %d / %d PASSED\n", testsPassed, testsTotal);
        System.out.println("=================================================");

        if (testsPassed != testsTotal) {
            System.exit(1);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            System.err.println("  [FAIL] " + message);
        }
    }

    private static void testAndGate() {
        System.out.println("\nTesting AND Gate:");
        LogicGate gate = new AndGate();
        assertTrue("AND 0,0 -> 0", !gate.evaluate(false, false));
        assertTrue("AND 0,1 -> 0", !gate.evaluate(false, true));
        assertTrue("AND 1,0 -> 0", !gate.evaluate(true, false));
        assertTrue("AND 1,1 -> 1", gate.evaluate(true, true));
        assertTrue("AND inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testOrGate() {
        System.out.println("\nTesting OR Gate:");
        LogicGate gate = new OrGate();
        assertTrue("OR 0,0 -> 0", !gate.evaluate(false, false));
        assertTrue("OR 0,1 -> 1", gate.evaluate(false, true));
        assertTrue("OR 1,0 -> 1", gate.evaluate(true, false));
        assertTrue("OR 1,1 -> 1", gate.evaluate(true, true));
        assertTrue("OR inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testNotGate() {
        System.out.println("\nTesting NOT Gate:");
        LogicGate gate = new NotGate();
        assertTrue("NOT 0 -> 1", gate.evaluate(false));
        assertTrue("NOT 1 -> 0", !gate.evaluate(true));
        assertTrue("NOT 0, ignored -> 1", gate.evaluate(false, true));
        assertTrue("NOT 1, ignored -> 0", !gate.evaluate(true, true));
        assertTrue("NOT inputCount == 1", gate.getInputCount() == 1);
    }

    private static void testNandGate() {
        System.out.println("\nTesting NAND Gate:");
        LogicGate gate = new NandGate();
        assertTrue("NAND 0,0 -> 1", gate.evaluate(false, false));
        assertTrue("NAND 0,1 -> 1", gate.evaluate(false, true));
        assertTrue("NAND 1,0 -> 1", gate.evaluate(true, false));
        assertTrue("NAND 1,1 -> 0", !gate.evaluate(true, true));
        assertTrue("NAND inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testNorGate() {
        System.out.println("\nTesting NOR Gate:");
        LogicGate gate = new NorGate();
        assertTrue("NOR 0,0 -> 1", gate.evaluate(false, false));
        assertTrue("NOR 0,1 -> 0", !gate.evaluate(false, true));
        assertTrue("NOR 1,0 -> 0", !gate.evaluate(true, false));
        assertTrue("NOR 1,1 -> 0", !gate.evaluate(true, true));
        assertTrue("NOR inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testXorGate() {
        System.out.println("\nTesting XOR Gate:");
        LogicGate gate = new XorGate();
        assertTrue("XOR 0,0 -> 0", !gate.evaluate(false, false));
        assertTrue("XOR 0,1 -> 1", gate.evaluate(false, true));
        assertTrue("XOR 1,0 -> 1", gate.evaluate(true, false));
        assertTrue("XOR 1,1 -> 0", !gate.evaluate(true, true));
        assertTrue("XOR inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testXnorGate() {
        System.out.println("\nTesting XNOR Gate:");
        LogicGate gate = new XnorGate();
        assertTrue("XNOR 0,0 -> 1", gate.evaluate(false, false));
        assertTrue("XNOR 0,1 -> 0", !gate.evaluate(false, true));
        assertTrue("XNOR 1,0 -> 0", !gate.evaluate(true, false));
        assertTrue("XNOR 1,1 -> 1", gate.evaluate(true, true));
        assertTrue("XNOR inputCount == 2", gate.getInputCount() == 2);
    }

    private static void testGateFactory() {
        System.out.println("\nTesting Gate Factory:");
        assertTrue("createGate(AND) is AndGate", GateFactory.createGate(GateType.AND) instanceof AndGate);
        assertTrue("createGate(OR) is OrGate", GateFactory.createGate(GateType.OR) instanceof OrGate);
        assertTrue("createGate(NOT) is NotGate", GateFactory.createGate(GateType.NOT) instanceof NotGate);
        assertTrue("createGate(NAND) is NandGate", GateFactory.createGate(GateType.NAND) instanceof NandGate);
        assertTrue("createGate(NOR) is NorGate", GateFactory.createGate(GateType.NOR) instanceof NorGate);
        assertTrue("createGate(XOR) is XorGate", GateFactory.createGate(GateType.XOR) instanceof XorGate);
        assertTrue("createGate(XNOR) is XnorGate", GateFactory.createGate(GateType.XNOR) instanceof XnorGate);
        assertTrue("createGate(null) returns null", GateFactory.createGate(null) == null);

        assertTrue("stringToGateType('AND') is AND", GateFactory.stringToGateType("AND") == GateType.AND);
        assertTrue("stringToGateType('and') is AND", GateFactory.stringToGateType("and") == GateType.AND);
        assertTrue("stringToGateType('XNOR') is XNOR", GateFactory.stringToGateType("XNOR") == GateType.XNOR);
        assertTrue("stringToGateType('INVALID') returns null", GateFactory.stringToGateType("INVALID") == null);
        assertTrue("stringToGateType('') returns null", GateFactory.stringToGateType("") == null);
    }

    private static void testTruthTableGeneration() {
        System.out.println("\nTesting Truth Table Generation:");

        // Test 2-input gate (AND)
        LogicGate andGate = new AndGate();
        List<TruthTableRow> andTable = andGate.generateTruthTable();
        assertTrue("AND table has 4 rows", andTable.size() == 4);
        assertTrue("AND row 0: 0,0 -> 0", !andTable.get(0).isInputA() && !andTable.get(0).isInputB() && !andTable.get(0).isOutput());
        assertTrue("AND row 1: 0,1 -> 0", !andTable.get(1).isInputA() && andTable.get(1).isInputB() && !andTable.get(1).isOutput());
        assertTrue("AND row 2: 1,0 -> 0", andTable.get(2).isInputA() && !andTable.get(2).isInputB() && !andTable.get(2).isOutput());
        assertTrue("AND row 3: 1,1 -> 1", andTable.get(3).isInputA() && andTable.get(3).isInputB() && andTable.get(3).isOutput());

        // Test 1-input gate (NOT)
        LogicGate notGate = new NotGate();
        List<TruthTableRow> notTable = notGate.generateTruthTable();
        assertTrue("NOT table has 2 rows", notTable.size() == 2);
        assertTrue("NOT row 0: 0 -> 1", !notTable.get(0).isInputA() && notTable.get(0).isOutput());
        assertTrue("NOT row 1: 1 -> 0", notTable.get(1).isInputA() && !notTable.get(1).isOutput());
    }

    private static void testGateStorage() {
        System.out.println("\nTesting Gate Storage (Save/Load & Validation):");
        String testFile = "target/test_save_state.txt";

        // 1. Save and Reload Valid State
        SimulatorState state = new SimulatorState(GateType.XOR, true, false);
        StorageResult saveRes = GateStorage.saveSimulatorState(state, testFile);
        assertTrue("Save state success", saveRes.isSuccess());

        StorageResult loadRes = GateStorage.loadSimulatorState(testFile);
        assertTrue("Load state success", loadRes.isSuccess());
        assertTrue("Loaded gate is XOR", loadRes.getState().getGateType() == GateType.XOR);
        assertTrue("Loaded inputA is 1", loadRes.getState().isInputA());
        assertTrue("Loaded inputB is 0", !loadRes.getState().isInputB());

        // 2. Load Missing File
        StorageResult missingRes = GateStorage.loadSimulatorState("target/non_existent_file.txt");
        assertTrue("Missing file fails gracefully", !missingRes.isSuccess());
        assertTrue("Missing file returns null state", missingRes.getState() == null);

        // 3. Load Corrupted File (invalid gate name)
        String badGateFile = "target/test_bad_gate.txt";
        try {
            Files.writeString(Paths.get(badGateFile), "GATE: UNKNOWN_GATE\nINPUT_A: 1\nINPUT_B: 0\n");
            StorageResult badGateRes = GateStorage.loadSimulatorState(badGateFile);
            assertTrue("Invalid gate name fails validation", !badGateRes.isSuccess());
        } catch (IOException e) {
            assertTrue("File write error", false);
        }

        // 4. Load Corrupted File (missing input B for 2-input gate)
        String missingBFile = "target/test_missing_b.txt";
        try {
            Files.writeString(Paths.get(missingBFile), "GATE: AND\nINPUT_A: 1\n");
            StorageResult missingBRes = GateStorage.loadSimulatorState(missingBFile);
            assertTrue("Missing input B for 2-input gate fails validation", !missingBRes.isSuccess());
        } catch (IOException e) {
            assertTrue("File write error", false);
        }

        // 5. Load NOT gate without input B (valid for NOT gate)
        String validNotFile = "target/test_valid_not.txt";
        try {
            Files.writeString(Paths.get(validNotFile), "GATE: NOT\nINPUT_A: 1\n");
            StorageResult notRes = GateStorage.loadSimulatorState(validNotFile);
            assertTrue("Valid NOT gate without B succeeds", notRes.isSuccess());
            assertTrue("Loaded NOT gate type is NOT", notRes.getState().getGateType() == GateType.NOT);
            assertTrue("Loaded NOT input A is 1", notRes.getState().isInputA());
        } catch (IOException e) {
            assertTrue("File write error", false);
        }

        // 6. Load Invalid Boolean Value
        String badBoolFile = "target/test_bad_bool.txt";
        try {
            Files.writeString(Paths.get(badBoolFile), "GATE: OR\nINPUT_A: maybe\nINPUT_B: 1\n");
            StorageResult badBoolRes = GateStorage.loadSimulatorState(badBoolFile);
            assertTrue("Invalid boolean string fails validation", !badBoolRes.isSuccess());
        } catch (IOException e) {
            assertTrue("File write error", false);
        }
    }

    // ========================================================================
    // Boolean Expression Lab Tests
    // ========================================================================

    private static void testExpressionOperators() {
        System.out.println("\nTesting Boolean Expression Lab - All Operators:");
        try {
            // AND
            BooleanExpression andExpr = BooleanExpression.parse("A AND B");
            assertTrue("Expr AND 0,0 -> 0", !andExpr.evaluate(false, false, false));
            assertTrue("Expr AND 0,1 -> 0", !andExpr.evaluate(false, true, false));
            assertTrue("Expr AND 1,0 -> 0", !andExpr.evaluate(true, false, false));
            assertTrue("Expr AND 1,1 -> 1", andExpr.evaluate(true, true, false));

            // OR
            BooleanExpression orExpr = BooleanExpression.parse("A OR B");
            assertTrue("Expr OR 0,0 -> 0", !orExpr.evaluate(false, false, false));
            assertTrue("Expr OR 0,1 -> 1", orExpr.evaluate(false, true, false));
            assertTrue("Expr OR 1,0 -> 1", orExpr.evaluate(true, false, false));
            assertTrue("Expr OR 1,1 -> 1", orExpr.evaluate(true, true, false));

            // NOT
            BooleanExpression notExpr = BooleanExpression.parse("NOT A");
            assertTrue("Expr NOT 0 -> 1", notExpr.evaluate(false, false, false));
            assertTrue("Expr NOT 1 -> 0", !notExpr.evaluate(true, false, false));

            // NAND
            BooleanExpression nandExpr = BooleanExpression.parse("A NAND B");
            assertTrue("Expr NAND 0,0 -> 1", nandExpr.evaluate(false, false, false));
            assertTrue("Expr NAND 0,1 -> 1", nandExpr.evaluate(false, true, false));
            assertTrue("Expr NAND 1,0 -> 1", nandExpr.evaluate(true, false, false));
            assertTrue("Expr NAND 1,1 -> 0", !nandExpr.evaluate(true, true, false));

            // NOR
            BooleanExpression norExpr = BooleanExpression.parse("A NOR B");
            assertTrue("Expr NOR 0,0 -> 1", norExpr.evaluate(false, false, false));
            assertTrue("Expr NOR 0,1 -> 0", !norExpr.evaluate(false, true, false));
            assertTrue("Expr NOR 1,0 -> 0", !norExpr.evaluate(true, false, false));
            assertTrue("Expr NOR 1,1 -> 0", !norExpr.evaluate(true, true, false));

            // XOR
            BooleanExpression xorExpr = BooleanExpression.parse("A XOR B");
            assertTrue("Expr XOR 0,0 -> 0", !xorExpr.evaluate(false, false, false));
            assertTrue("Expr XOR 0,1 -> 1", xorExpr.evaluate(false, true, false));
            assertTrue("Expr XOR 1,0 -> 1", xorExpr.evaluate(true, false, false));
            assertTrue("Expr XOR 1,1 -> 0", !xorExpr.evaluate(true, true, false));

            // XNOR
            BooleanExpression xnorExpr = BooleanExpression.parse("A XNOR B");
            assertTrue("Expr XNOR 0,0 -> 1", xnorExpr.evaluate(false, false, false));
            assertTrue("Expr XNOR 0,1 -> 0", !xnorExpr.evaluate(false, true, false));
            assertTrue("Expr XNOR 1,0 -> 0", !xnorExpr.evaluate(true, false, false));
            assertTrue("Expr XNOR 1,1 -> 1", xnorExpr.evaluate(true, true, false));
        } catch (ExpressionParseException e) {
            assertTrue("Operator test unexpected parse failure: " + e.getMessage(), false);
        }
    }

    private static void testExpressionPrecedenceAndParentheses() {
        System.out.println("\nTesting Boolean Expression Lab - Precedence & Parentheses:");
        try {
            // Precedence: AND over OR
            // "A OR B AND C" should parse as A OR (B AND C)
            // If A=0, B=1, C=0: A OR (1 AND 0) = 0 OR 0 = 0
            // If (A OR B) AND C: (0 OR 1) AND 0 = 1 AND 0 = 0, but if A=1, B=0, C=0: (1 OR 0) AND 0 = 0 vs 1 OR (0 AND 0) = 1
            BooleanExpression p1 = BooleanExpression.parse("A OR B AND C");
            assertTrue("Precedence A OR B AND C with A=1, B=0, C=0 -> 1", p1.evaluate(true, false, false));
            assertTrue("Precedence A OR B AND C with A=0, B=1, C=0 -> 0", !p1.evaluate(false, true, false));
            assertTrue("Precedence A OR B AND C with A=0, B=1, C=1 -> 1", p1.evaluate(false, true, true));

            // Parentheses override precedence: (A OR B) AND C
            BooleanExpression p2 = BooleanExpression.parse("(A OR B) AND C");
            assertTrue("Override (A OR B) AND C with A=1, B=0, C=0 -> 0", !p2.evaluate(true, false, false));
            assertTrue("Override (A OR B) AND C with A=1, B=0, C=1 -> 1", p2.evaluate(true, false, true));

            // Precedence: NOT over AND
            // "NOT A AND B" should parse as (NOT A) AND B
            BooleanExpression p3 = BooleanExpression.parse("NOT A AND B");
            assertTrue("Precedence NOT A AND B with A=0, B=1 -> 1", p3.evaluate(false, true, false));
            assertTrue("Precedence NOT A AND B with A=1, B=1 -> 0", !p3.evaluate(true, true, false));

            // Parentheses override NOT: NOT (A AND B)
            BooleanExpression p4 = BooleanExpression.parse("NOT (A AND B)");
            assertTrue("Override NOT (A AND B) with A=0, B=1 -> 1", p4.evaluate(false, true, false));
            assertTrue("Override NOT (A AND B) with A=1, B=1 -> 0", !p4.evaluate(true, true, false));
            assertTrue("Override NOT (A AND B) with A=0, B=0 -> 1", p4.evaluate(false, false, false));

            // Precedence: AND over XOR
            // "A XOR B AND C" should parse as A XOR (B AND C)
            BooleanExpression p5 = BooleanExpression.parse("A XOR B AND C");
            // A=1, B=1, C=1 -> 1 XOR (1 AND 1) = 1 XOR 1 = 0
            assertTrue("Precedence A XOR B AND C with 1,1,1 -> 0", !p5.evaluate(true, true, true));
            // A=1, B=1, C=0 -> 1 XOR (1 AND 0) = 1 XOR 0 = 1
            assertTrue("Precedence A XOR B AND C with 1,1,0 -> 1", p5.evaluate(true, true, false));

            // Parentheses override: (A XOR B) AND C
            BooleanExpression p6 = BooleanExpression.parse("(A XOR B) AND C");
            assertTrue("(A XOR B) AND C with 1,0,1 -> 1", p6.evaluate(true, false, true));
            assertTrue("(A XOR B) AND C with 1,1,1 -> 0", !p6.evaluate(true, true, true));

            // Required user example: (A OR B) AND (NOT C)
            BooleanExpression p7 = BooleanExpression.parse("(A OR B) AND (NOT C)");
            assertTrue("(A OR B) AND (NOT C) with 1,0,0 -> 1", p7.evaluate(true, false, false));
            assertTrue("(A OR B) AND (NOT C) with 1,0,1 -> 0", !p7.evaluate(true, false, true));
            assertTrue("(A OR B) AND (NOT C) with 0,0,0 -> 0", !p7.evaluate(false, false, false));
        } catch (ExpressionParseException e) {
            assertTrue("Precedence test unexpected parse failure: " + e.getMessage(), false);
        }
    }

    private static void testExpressionNested() {
        System.out.println("\nTesting Boolean Expression Lab - Nested Expressions:");
        try {
            // Double NOT: NOT NOT A == A
            BooleanExpression dNot = BooleanExpression.parse("NOT NOT A");
            assertTrue("Double NOT 0 -> 0", !dNot.evaluate(false, false, false));
            assertTrue("Double NOT 1 -> 1", dNot.evaluate(true, false, false));

            // Triple NOT: NOT NOT NOT A == NOT A
            BooleanExpression tNot = BooleanExpression.parse("NOT NOT NOT A");
            assertTrue("Triple NOT 0 -> 1", tNot.evaluate(false, false, false));
            assertTrue("Triple NOT 1 -> 0", !tNot.evaluate(true, false, false));

            // Complex nesting: NOT (A OR (B AND (NOT C)))
            BooleanExpression comp = BooleanExpression.parse("NOT (A OR (B AND (NOT C)))");
            // If A=0, B=1, C=0: B AND NOT C = 1 AND 1 = 1; A OR 1 = 1; NOT 1 = 0
            assertTrue("Nested comp with 0,1,0 -> 0", !comp.evaluate(false, true, false));
            // If A=0, B=0, C=0: B AND NOT C = 0; A OR 0 = 0; NOT 0 = 1
            assertTrue("Nested comp with 0,0,0 -> 1", comp.evaluate(false, false, false));

            // Multi-operator chain: A XOR B XOR C
            BooleanExpression chain = BooleanExpression.parse("A XOR B XOR C");
            // 0,0,0 -> 0; 0,0,1 -> 1; 0,1,1 -> 0; 1,1,1 -> 1
            assertTrue("Chained XOR 0,0,0 -> 0", !chain.evaluate(false, false, false));
            assertTrue("Chained XOR 0,0,1 -> 1", chain.evaluate(false, false, true));
            assertTrue("Chained XOR 0,1,1 -> 0", !chain.evaluate(false, true, true));
            assertTrue("Chained XOR 1,1,1 -> 1", chain.evaluate(true, true, true));
        } catch (ExpressionParseException e) {
            assertTrue("Nested expression parse failure: " + e.getMessage(), false);
        }
    }

    private static void testExpressionVariablesAndTruthTable() {
        System.out.println("\nTesting Boolean Expression Lab - Variables & Truth Table Rows:");
        try {
            // 1 Variable: NOT A (2 rows)
            BooleanExpression expr1 = BooleanExpression.parse("NOT A");
            assertTrue("1 var count == 1", expr1.getVariableCount() == 1);
            List<ExpressionTruthTableRow> table1 = expr1.generateTruthTable();
            assertTrue("1 var generates 2 rows", table1.size() == 2);
            assertTrue("Row 0: A=0 -> 1", table1.get(0).getInputString("A").equals("0") && table1.get(0).isOutput());
            assertTrue("Row 1: A=1 -> 0", table1.get(1).getInputString("A").equals("1") && !table1.get(1).isOutput());

            // 2 Variables: A AND B (4 rows)
            BooleanExpression expr2 = BooleanExpression.parse("A AND B");
            assertTrue("2 var count == 2", expr2.getVariableCount() == 2);
            List<ExpressionTruthTableRow> table2 = expr2.generateTruthTable();
            assertTrue("2 vars generate 4 rows", table2.size() == 4);
            assertTrue("Row 00 -> 0", !table2.get(0).isOutput());
            assertTrue("Row 01 -> 0", !table2.get(1).isOutput());
            assertTrue("Row 10 -> 0", !table2.get(2).isOutput());
            assertTrue("Row 11 -> 1", table2.get(3).isOutput());

            // 3 Variables: (A AND B) OR C (8 rows)
            BooleanExpression expr3 = BooleanExpression.parse("(A AND B) OR C");
            assertTrue("3 var count == 3", expr3.getVariableCount() == 3);
            List<ExpressionTruthTableRow> table3 = expr3.generateTruthTable();
            assertTrue("3 vars generate 8 rows", table3.size() == 8);

            // Row 0: 0,0,0 -> 0
            assertTrue("3-var row 0: 0,0,0 -> 0", !table3.get(0).isOutput());
            // Row 1: 0,0,1 -> 1
            assertTrue("3-var row 1: 0,0,1 -> 1", table3.get(1).isOutput());
            // Row 2: 0,1,0 -> 0
            assertTrue("3-var row 2: 0,1,0 -> 0", !table3.get(2).isOutput());
            // Row 3: 0,1,1 -> 1
            assertTrue("3-var row 3: 0,1,1 -> 1", table3.get(3).isOutput());
            // Row 4: 1,0,0 -> 0
            assertTrue("3-var row 4: 1,0,0 -> 0", !table3.get(4).isOutput());
            // Row 5: 1,0,1 -> 1
            assertTrue("3-var row 5: 1,0,1 -> 1", table3.get(5).isOutput());
            // Row 6: 1,1,0 -> 1
            assertTrue("3-var row 6: 1,1,0 -> 1", table3.get(6).isOutput());
            // Row 7: 1,1,1 -> 1
            assertTrue("3-var row 7: 1,1,1 -> 1", table3.get(7).isOutput());

            // Active row matching test
            Map<String, Boolean> active = new HashMap<>();
            active.put("A", true);
            active.put("B", true);
            active.put("C", false);
            assertTrue("Row 6 matches active inputs A=1, B=1, C=0", table3.get(6).matchesInputs(active));
            assertTrue("Row 0 does not match active inputs", !table3.get(0).matchesInputs(active));
        } catch (ExpressionParseException e) {
            assertTrue("Truth table test parse failure: " + e.getMessage(), false);
        }
    }

    private static void testExpressionCaseInsensitivity() {
        System.out.println("\nTesting Boolean Expression Lab - Case Insensitivity:");
        try {
            BooleanExpression e1 = BooleanExpression.parse("a and b");
            assertTrue("Lowercase 'a and b' parses and evaluates 1,1 -> 1", e1.evaluate(true, true, false));

            BooleanExpression e2 = BooleanExpression.parse("not (a or b)");
            assertTrue("Lowercase 'not (a or b)' parses and evaluates 0,0 -> 1", e2.evaluate(false, false, false));

            BooleanExpression e3 = BooleanExpression.parse("(a xor B) AnD c");
            assertTrue("Mixed case '(a xor B) AnD c' parses and evaluates 1,0,1 -> 1", e3.evaluate(true, false, true));

            BooleanExpression e4 = BooleanExpression.parse("a nOR b");
            assertTrue("Mixed case 'a nOR b' parses and evaluates 0,0 -> 1", e4.evaluate(false, false, false));

            BooleanExpression e5 = BooleanExpression.parse("a xnOr b");
            assertTrue("Mixed case 'a xnOr b' parses and evaluates 1,1 -> 1", e5.evaluate(true, true, false));
        } catch (ExpressionParseException e) {
            assertTrue("Case insensitivity test failed: " + e.getMessage(), false);
        }
    }

    private static void testExpressionValidationAndErrors() {
        System.out.println("\nTesting Boolean Expression Lab - Syntax Validation & Error Detection:");

        // 1. Empty expressions
        assertThrowsParse("Empty string", "");
        assertThrowsParse("Whitespace only", "    ");
        assertThrowsParse("Tabs and newlines", "\t \n ");

        // 2. Missing operands
        assertThrowsParse("Missing right operand for AND", "A AND");
        assertThrowsParse("Missing left operand for AND", "AND B");
        assertThrowsParse("Missing operand for NOT", "NOT");
        assertThrowsParse("Missing operand inside parens", "(A AND)");
        assertThrowsParse("Consecutive binary operators", "A AND OR B");

        // 3. Unknown operators and unsupported characters
        assertThrowsParse("Unknown word operator", "A FOO B");
        assertThrowsParse("Plus symbol", "A + B");
        assertThrowsParse("Ampersand symbol", "A & B");
        assertThrowsParse("Exclamation symbol", "!A");

        // 4. Unknown variables
        assertThrowsParse("Variable D", "D AND A");
        assertThrowsParse("Variable X", "NOT X");
        assertThrowsParse("Variable Z", "A OR Z");

        // 5. Unbalanced parentheses
        assertThrowsParse("Unclosed parenthesis", "(A AND B");
        assertThrowsParse("Extra closing parenthesis", "A AND B)");
        assertThrowsParse("Multiple unclosed parens", "((A OR B)");
        assertThrowsParse("Empty parentheses", "()");
        assertThrowsParse("Empty parens with space", "(  )");

        // 6. Missing operators between variables
        assertThrowsParse("Consecutive variables", "A B");
        assertThrowsParse("Variable followed by paren without op", "A (B OR C)");
        assertThrowsParse("Paren followed by variable without op", "(A OR B) C");
    }

    private static void assertThrowsParse(String testName, String expression) {
        try {
            BooleanExpression.parse(expression);
            assertTrue(testName + " should fail validation but succeeded", false);
        } catch (ExpressionParseException e) {
            assertTrue(testName + " rejected: \"" + e.getMessage() + "\"", true);
        }
    }

    private static void testExpressionEvaluationStepsAndMath() {
        System.out.println("\nTesting Boolean Expression Lab - Evaluation Steps & Mathematical Output:");
        try {
            BooleanExpression expr = BooleanExpression.parse("(A AND B) OR C");

            // Check Mathematical string
            String math = expr.toMathematicalString();
            assertTrue("Mathematical string contains Y =", math.startsWith("Y = "));
            assertTrue("Mathematical string contains · or +", math.contains("·") && math.contains("+"));

            // Check Evaluation steps
            Map<String, Boolean> inputs = new HashMap<>();
            inputs.put("A", true);
            inputs.put("B", false);
            inputs.put("C", true);

            List<String> steps = expr.getEvaluationSteps(inputs);
            assertTrue("Evaluation steps list is non-empty", steps != null && steps.size() >= 4);
            assertTrue("Step contains A = 1", steps.stream().anyMatch(s -> s.contains("A = 1")));
            assertTrue("Step contains B = 0", steps.stream().anyMatch(s -> s.contains("B = 0")));
            assertTrue("Step contains C = 1", steps.stream().anyMatch(s -> s.contains("C = 1")));
            assertTrue("Sub-expression evaluated in steps", steps.stream().anyMatch(s -> s.contains("AND") || s.contains("OR")));
        } catch (ExpressionParseException e) {
            assertTrue("Steps and math test parse failure: " + e.getMessage(), false);
        }
    }
}

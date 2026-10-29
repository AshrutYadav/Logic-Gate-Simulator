package backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a generic Digital Logic Gate.
 * Demonstrates Core OOP Concepts:
 * - Data Abstraction (abstract evaluate method and generic gate contract)
 * - Encapsulation (protected fields with public accessors)
 * - Dynamic Binding (truth table calls this.evaluate polymorphically)
 */
public abstract class LogicGate {
    protected GateType type;
    protected String name;
    protected String description;
    protected String expression;
    protected int inputCount;

    /**
     * Parameterized constructor for logic gate properties.
     */
    public LogicGate(GateType type, String name, String description, String expression, int inputCount) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.expression = expression;
        this.inputCount = inputCount;
    }

    // Encapsulation: Public getters
    public GateType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExpression() {
        return expression;
    }

    public int getInputCount() {
        return inputCount;
    }

    /**
     * Pure abstract method: Evaluates the Boolean output for given inputs A and B.
     * Concrete logic is provided by derived classes.
     *
     * @param a Input A (boolean)
     * @param b Input B (boolean, ignored for 1-input gates)
     * @return boolean calculated output
     */
    public abstract boolean evaluate(boolean a, boolean b);

    /**
     * Overloaded method convenience for single-input gates like NOT.
     * Demonstrates Method Overloading.
     *
     * @param a Input A (boolean)
     * @return boolean calculated output
     */
    public boolean evaluate(boolean a) {
        return evaluate(a, false);
    }

    /**
     * Generates all truth table combinations using dynamic binding.
     * Calls this.evaluate(...) so that each derived class evaluates its specific logic.
     *
     * @return List of TruthTableRow
     */
    public List<TruthTableRow> generateTruthTable() {
        List<TruthTableRow> table = new ArrayList<>();

        if (inputCount == 1) {
            // NOT gate: 2 rows (A=0, A=1)
            table.add(new TruthTableRow(false, false, this.evaluate(false), false));
            table.add(new TruthTableRow(true, false, this.evaluate(true), false));
        } else {
            // Two-input gates: 4 rows (00, 01, 10, 11)
            table.add(new TruthTableRow(false, false, this.evaluate(false, false), true));
            table.add(new TruthTableRow(false, true, this.evaluate(false, true), true));
            table.add(new TruthTableRow(true, false, this.evaluate(true, false), true));
            table.add(new TruthTableRow(true, true, this.evaluate(true, true), true));
        }

        return table;
    }
}

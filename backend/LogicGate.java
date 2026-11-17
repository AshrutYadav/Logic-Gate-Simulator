package backend;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicGate {
    protected GateType type;
    protected String name;
    protected String description;
    protected String expression;
    protected int inputCount;

    public LogicGate(GateType type, String name, String description, String expression, int inputCount) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.expression = expression;
        this.inputCount = inputCount;
    }

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

    // Evaluates output for inputs A and B.
    public abstract boolean evaluate(boolean a, boolean b);

    // Convenience evaluation for single-input gates.
    public boolean evaluate(boolean a) {
        return evaluate(a, false);
    }

    // Generates all truth table rows for this gate.
    public List<TruthTableRow> generateTruthTable() {
        List<TruthTableRow> table = new ArrayList<>();

        if (inputCount == 1) {
            table.add(new TruthTableRow(false, false, this.evaluate(false), false));
            table.add(new TruthTableRow(true, false, this.evaluate(true), false));
        } else {
            table.add(new TruthTableRow(false, false, this.evaluate(false, false), true));
            table.add(new TruthTableRow(false, true, this.evaluate(false, true), true));
            table.add(new TruthTableRow(true, false, this.evaluate(true, false), true));
            table.add(new TruthTableRow(true, true, this.evaluate(true, true), true));
        }

        return table;
    }
}

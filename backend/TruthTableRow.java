package backend;

/**
 * Represents a single row in a logic gate truth table.
 */
public class TruthTableRow {
    private final boolean inputA;
    private final boolean inputB;
    private final boolean output;
    private final boolean hasInputB;

    public TruthTableRow(boolean inputA, boolean inputB, boolean output, boolean hasInputB) {
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;
        this.hasInputB = hasInputB;
    }

    public boolean isInputA() {
        return inputA;
    }

    public boolean isInputB() {
        return inputB;
    }

    public boolean isOutput() {
        return output;
    }

    public boolean hasInputB() {
        return hasInputB;
    }

    public String getInputAString() {
        return inputA ? "1" : "0";
    }

    public String getInputBString() {
        return hasInputB ? (inputB ? "1" : "0") : "-";
    }

    public String getOutputString() {
        return output ? "1" : "0";
    }

    @Override
    public String toString() {
        if (hasInputB) {
            return String.format("A=%s, B=%s -> Y=%s", getInputAString(), getInputBString(), getOutputString());
        }
        return String.format("A=%s -> Y=%s", getInputAString(), getOutputString());
    }
}

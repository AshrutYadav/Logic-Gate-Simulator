package backend;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single row in a Boolean expression truth table.
 * Supports arbitrary variable assignments (A, B, C) and calculated output.
 */
public class ExpressionTruthTableRow {
    private final Map<String, Boolean> inputs;
    private final boolean output;

    public ExpressionTruthTableRow(Map<String, Boolean> inputs, boolean output) {
        this.inputs = Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
        this.output = output;
    }

    public Map<String, Boolean> getInputs() {
        return inputs;
    }

    public boolean isOutput() {
        return output;
    }

    public boolean getInputValue(String variable) {
        if (variable == null) return false;
        return inputs.getOrDefault(variable.toUpperCase(), false);
    }

    public String getInputString(String variable) {
        if (variable == null) return "-";
        Boolean val = inputs.get(variable.toUpperCase());
        return val == null ? "-" : (val ? "1" : "0");
    }

    public String getOutputString() {
        return output ? "1" : "0";
    }

    /**
     * Checks if this row's input combination matches the given active inputs.
     */
    public boolean matchesInputs(Map<String, Boolean> currentInputs) {
        if (currentInputs == null) return false;
        for (Map.Entry<String, Boolean> entry : inputs.entrySet()) {
            Boolean currentVal = currentInputs.get(entry.getKey());
            if (currentVal == null || !currentVal.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        inputs.forEach((k, v) -> sb.append(k).append("=").append(v ? "1" : "0").append(" "));
        sb.append("-> Y=").append(getOutputString());
        return sb.toString();
    }
}

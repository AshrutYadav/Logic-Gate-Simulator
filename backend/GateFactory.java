package backend;

/**
 * Factory class for instantiating logic gates based on GateType.
 * Demonstrates: Factory Method Pattern, Runtime Polymorphism, Static Utility Methods.
 */
public class GateFactory {

    /**
     * Creates a concrete LogicGate instance polymorphically.
     *
     * @param type GateType enum value
     * @return LogicGate reference to the newly created gate, or null if unknown
     */
    public static LogicGate createGate(GateType type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case AND  -> new AndGate();
            case OR   -> new OrGate();
            case NOT  -> new NotGate();
            case NAND -> new NandGate();
            case NOR  -> new NorGate();
            case XOR  -> new XorGate();
            case XNOR -> new XnorGate();
        };
    }

    /**
     * Converts a GateType enum to its canonical string representation.
     *
     * @param type GateType enum
     * @return String representation (e.g., "AND", "OR")
     */
    public static String gateTypeToString(GateType type) {
        if (type == null) {
            return "UNKNOWN";
        }
        return type.name();
    }

    /**
     * Converts a string representation to a GateType enum.
     * Rejects invalid gate names strictly by returning null (does not silently default).
     *
     * @param str Case-insensitive string name
     * @return GateType if valid, or null if unrecognized
     */
    public static GateType stringToGateType(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        String normalized = str.trim().toUpperCase();
        return switch (normalized) {
            case "AND"  -> GateType.AND;
            case "OR"   -> GateType.OR;
            case "NOT"  -> GateType.NOT;
            case "NAND" -> GateType.NAND;
            case "NOR"  -> GateType.NOR;
            case "XOR"  -> GateType.XOR;
            case "XNOR" -> GateType.XNOR;
            default     -> null;
        };
    }
}

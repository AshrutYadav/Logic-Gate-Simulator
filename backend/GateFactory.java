package backend;

// Factory for instantiating logic gates.
public class GateFactory {

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

    // Converts GateType to string.
    public static String gateTypeToString(GateType type) {
        if (type == null) {
            return "UNKNOWN";
        }
        return type.name();
    }

    // Converts string to GateType, or returns null if invalid.
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
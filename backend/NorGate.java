package backend;

public class NorGate extends LogicGate {

    public NorGate() {
        super(GateType.NOR, "NOR Gate",
              "Output is 1 only when all inputs are 0 (Inverted OR).",
              "Y = ¬(A + B)", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return !(a || b);
    }
}

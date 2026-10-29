package backend;

public class XorGate extends LogicGate {

    public XorGate() {
        super(GateType.XOR, "XOR Gate",
              "Output is 1 when inputs are different (Exclusive OR).",
              "Y = A ⊕ B", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return a != b;
    }
}

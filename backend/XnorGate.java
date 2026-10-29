package backend;

public class XnorGate extends LogicGate {

    public XnorGate() {
        super(GateType.XNOR, "XNOR Gate",
              "Output is 1 when inputs are identical (Equivalence).",
              "Y = ¬(A ⊕ B)", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return a == b;
    }
}

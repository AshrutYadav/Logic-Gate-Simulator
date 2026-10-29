package backend;

public class NandGate extends LogicGate {

    public NandGate() {
        super(GateType.NAND, "NAND Gate",
              "Output is 0 only when all inputs are 1 (Inverted AND).",
              "Y = ¬(A · B)", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return !(a && b);
    }
}

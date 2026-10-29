package backend;

public class AndGate extends LogicGate {

    public AndGate() {
        super(GateType.AND, "AND Gate",
              "Output is 1 only when all inputs are 1.",
              "Y = A · B", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return a && b;
    }
}

package backend;
public class OrGate extends LogicGate {

    public OrGate() {
        super(GateType.OR, "OR Gate",
              "Output is 1 when at least one input is 1.",
              "Y = A + B", 2);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return a || b;
    }
}

package backend;

public class NotGate extends LogicGate {

    public NotGate() {
        super(GateType.NOT, "NOT Gate",
              "Inverts the input (Output is opposite of input).",
              "Y = ¬A", 1);
    }

    @Override
    public boolean evaluate(boolean a, boolean b) {
        return !a;
    }

    @Override
    public boolean evaluate(boolean a) {
        return !a;
    }
}

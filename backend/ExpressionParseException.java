package backend;

// Exception thrown when Boolean expression parsing fails.
public class ExpressionParseException extends Exception {
    private final int position;

    public ExpressionParseException(String message, int position) {
        super(message);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}

package backend;

/**
 * Checked exception thrown when Boolean expression tokenization or syntax parsing fails.
 * Captures the exact character position of the syntax error to provide actionable feedback.
 */
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

package backend;

import java.util.*;

/**
 * Robust Boolean expression tokenizer, recursive-descent parser, and evaluator.
 * Evaluates arbitrary valid Boolean expressions over variables A, B, and C.
 *
 * Supported Operators (in order of precedence):
 * 1. Parentheses: ( ... )
 * 2. NOT: Unary prefix inversion (NOT A, NOT (A OR B))
 * 3. AND, NAND: Multiplicative binary logic
 * 4. XOR, XNOR: Exclusive binary logic
 * 5. OR, NOR: Additive binary logic
 *
 * Features:
 * - Strict syntax validation (unknown tokens, missing operands, unclosed parentheses)
 * - Case-insensitive operator keywords and variable names
 * - Automatic distinct variable discovery (max 3 variables: A, B, C)
 * - Dynamic 2^n truth table generation
 * - Step-by-step evaluation trace generation for pedagogical display
 * - Mathematical notation translation (e.g. Y = (A · B) + C)
 */
public class BooleanExpression {

    // ========================================================================
    // Operator & Token Definitions
    // ========================================================================

    public enum OperatorType {
        AND, OR, NOT, NAND, NOR, XOR, XNOR
    }

    public enum TokenType {
        VARIABLE,
        NOT,
        AND, NAND,
        XOR, XNOR,
        OR, NOR,
        LPAREN, RPAREN,
        EOF
    }

    public static class Token {
        public final TokenType type;
        public final String text;
        public final int position;

        public Token(TokenType type, String text, int position) {
            this.type = type;
            this.text = text;
            this.position = position;
        }

        @Override
        public String toString() {
            return type + "('" + text + "'@" + position + ")";
        }
    }

    // ========================================================================
    // AST (Abstract Syntax Tree) Hierarchy
    // ========================================================================

    public interface ExpressionNode {
        boolean evaluate(Map<String, Boolean> inputs);
        boolean evaluateWithSteps(Map<String, Boolean> inputs, List<String> steps);
        void collectVariables(Set<String> variables);
        String toExpressionString();
        String toMathematicalString();
    }

    public static class VariableNode implements ExpressionNode {
        private final String name;

        public VariableNode(String name) {
            this.name = name.toUpperCase();
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> inputs) {
            return inputs != null && inputs.getOrDefault(name, false);
        }

        @Override
        public boolean evaluateWithSteps(Map<String, Boolean> inputs, List<String> steps) {
            return evaluate(inputs);
        }

        @Override
        public void collectVariables(Set<String> variables) {
            variables.add(name);
        }

        @Override
        public String toExpressionString() {
            return name;
        }

        @Override
        public String toMathematicalString() {
            return name;
        }
    }

    public static class NotNode implements ExpressionNode {
        private final ExpressionNode child;

        public NotNode(ExpressionNode child) {
            this.child = child;
        }

        public ExpressionNode getChild() {
            return child;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> inputs) {
            return !child.evaluate(inputs);
        }

        @Override
        public boolean evaluateWithSteps(Map<String, Boolean> inputs, List<String> steps) {
            boolean childVal = child.evaluateWithSteps(inputs, steps);
            boolean res = !childVal;
            String exprStr = toExpressionString();
            steps.add(exprStr + " = NOT " + (childVal ? "1" : "0") + " = " + (res ? "1" : "0"));
            return res;
        }

        @Override
        public void collectVariables(Set<String> variables) {
            child.collectVariables(variables);
        }

        @Override
        public String toExpressionString() {
            if (child instanceof VariableNode) {
                return "NOT " + child.toExpressionString();
            }
            return "NOT (" + child.toExpressionString() + ")";
        }

        @Override
        public String toMathematicalString() {
            if (child instanceof VariableNode) {
                return "¬" + child.toMathematicalString();
            }
            return "¬(" + child.toMathematicalString() + ")";
        }
    }

    public static class BinaryOpNode implements ExpressionNode {
        private final ExpressionNode left;
        private final OperatorType op;
        private final ExpressionNode right;

        public BinaryOpNode(ExpressionNode left, OperatorType op, ExpressionNode right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public ExpressionNode getLeft() {
            return left;
        }

        public OperatorType getOp() {
            return op;
        }

        public ExpressionNode getRight() {
            return right;
        }

        @Override
        public boolean evaluate(Map<String, Boolean> inputs) {
            boolean l = left.evaluate(inputs);
            boolean r = right.evaluate(inputs);
            return applyOp(l, op, r);
        }

        @Override
        public boolean evaluateWithSteps(Map<String, Boolean> inputs, List<String> steps) {
            boolean l = left.evaluateWithSteps(inputs, steps);
            boolean r = right.evaluateWithSteps(inputs, steps);
            boolean res = applyOp(l, op, r);

            String stepStr = toExpressionString() + " = " + (l ? "1" : "0") + " " + op.name() + " " + (r ? "1" : "0") + " = " + (res ? "1" : "0");
            steps.add(stepStr);
            return res;
        }

        private static boolean applyOp(boolean l, OperatorType op, boolean r) {
            return switch (op) {
                case AND  -> l && r;
                case OR   -> l || r;
                case NAND -> !(l && r);
                case NOR  -> !(l || r);
                case XOR  -> l ^ r;
                case XNOR -> !(l ^ r);
                default   -> false;
            };
        }

        @Override
        public void collectVariables(Set<String> variables) {
            left.collectVariables(variables);
            right.collectVariables(variables);
        }

        @Override
        public String toExpressionString() {
            return "(" + left.toExpressionString() + " " + op.name() + " " + right.toExpressionString() + ")";
        }

        @Override
        public String toMathematicalString() {
            String sym = switch (op) {
                case AND  -> " · ";
                case OR   -> " + ";
                case NAND -> " ⊼ ";
                case NOR  -> " ⊽ ";
                case XOR  -> " ⊕ ";
                case XNOR -> " ⊙ ";
                default   -> " ? ";
            };
            return "(" + left.toMathematicalString() + sym + right.toMathematicalString() + ")";
        }
    }

    // ========================================================================
    // BooleanExpression Instance Fields
    // ========================================================================

    private final String rawExpression;
    private final ExpressionNode rootNode;
    private final List<String> variables;

    private BooleanExpression(String rawExpression, ExpressionNode rootNode, List<String> variables) {
        this.rawExpression = rawExpression;
        this.rootNode = rootNode;
        this.variables = Collections.unmodifiableList(variables);
    }

    public String getRawExpression() {
        return rawExpression;
    }

    public ExpressionNode getRootNode() {
        return rootNode;
    }

    public List<String> getVariables() {
        return variables;
    }

    public int getVariableCount() {
        return variables.size();
    }

    /**
     * Evaluates the expression using a variable assignment map.
     */
    public boolean evaluate(Map<String, Boolean> inputs) {
        return rootNode.evaluate(inputs);
    }

    /**
     * Convenience evaluation with up to three variables A, B, C.
     */
    public boolean evaluate(boolean a, boolean b, boolean c) {
        Map<String, Boolean> map = new HashMap<>();
        map.put("A", a);
        map.put("B", b);
        map.put("C", c);
        return evaluate(map);
    }

    /**
     * Formats the parsed expression in canonical mathematical notation:
     * e.g., "Y = (A · B) + C"
     */
    public String toMathematicalString() {
        String math = rootNode.toMathematicalString();
        // Strip outermost enclosing parentheses if present
        if (math.startsWith("(") && math.endsWith(")")) {
            // Only strip if parentheses match across the whole expression
            int depth = 0;
            boolean canStrip = true;
            for (int i = 0; i < math.length() - 1; i++) {
                char ch = math.charAt(i);
                if (ch == '(') depth++;
                else if (ch == ')') depth--;
                if (depth == 0) {
                    canStrip = false;
                    break;
                }
            }
            if (canStrip) {
                math = math.substring(1, math.length() - 1);
            }
        }
        return "Y = " + math;
    }

    /**
     * Generates a step-by-step evaluation trace for pedagogical explanation.
     */
    public List<String> getEvaluationSteps(Map<String, Boolean> inputs) {
        List<String> steps = new ArrayList<>();

        // Step 1: Base variable values
        for (String var : variables) {
            boolean val = (inputs != null && inputs.getOrDefault(var, false));
            steps.add(var + " = " + (val ? "1" : "0"));
        }

        // Sub-expression evaluation steps
        rootNode.evaluateWithSteps(inputs, steps);

        return steps;
    }

    /**
     * Generates all 2^n truth table rows dynamically for detected variables.
     */
    public List<ExpressionTruthTableRow> generateTruthTable() {
        List<ExpressionTruthTableRow> table = new ArrayList<>();
        int n = variables.size();
        int rowCount = 1 << n; // 2^n rows

        for (int i = 0; i < rowCount; i++) {
            Map<String, Boolean> rowInputs = new LinkedHashMap<>();
            // MSB first for standard digital logic truth table ordering:
            // e.g. for A, B, C: 000, 001, 010, 011, 100, 101, 110, 111
            for (int bit = 0; bit < n; bit++) {
                String varName = variables.get(bit);
                int shift = n - 1 - bit;
                boolean isSet = ((i >> shift) & 1) == 1;
                rowInputs.put(varName, isSet);
            }

            boolean out = evaluate(rowInputs);
            table.add(new ExpressionTruthTableRow(rowInputs, out));
        }

        return table;
    }

    // ========================================================================
    // Static Parsing Entry Point & Tokenizer
    // ========================================================================

    /**
     * Parses and strictly validates a Boolean expression string.
     *
     * @param expression Raw Boolean expression
     * @return Validated BooleanExpression object
     * @throws ExpressionParseException if expression has syntax errors or unsupported variables
     */
    public static BooleanExpression parse(String expression) throws ExpressionParseException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionParseException("Expression cannot be empty.", 0);
        }

        List<Token> tokens = tokenize(expression);
        Parser parser = new Parser(tokens, expression);
        ExpressionNode root = parser.parse();

        // Collect and validate variables
        Set<String> varSet = new TreeSet<>();
        root.collectVariables(varSet);

        if (varSet.isEmpty()) {
            throw new ExpressionParseException("Expression must contain at least one variable (A, B, or C).", 0);
        }

        if (varSet.size() > 3) {
            throw new ExpressionParseException("Expression contains " + varSet.size() +
                    " distinct variables, exceeding the maximum supported limit of 3.", 0);
        }

        List<String> sortedVars = new ArrayList<>(varSet);
        return new BooleanExpression(expression.trim(), root, sortedVars);
    }

    /**
     * Lexer / Tokenizer: Converts raw string into token stream with position tracking.
     */
    public static List<Token> tokenize(String input) throws ExpressionParseException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = input.length();

        while (i < len) {
            char ch = input.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            if (ch == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", i));
                i++;
                continue;
            }

            if (ch == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", i));
                i++;
                continue;
            }

            if (Character.isLetter(ch)) {
                int start = i;
                while (i < len && Character.isLetterOrDigit(input.charAt(i))) {
                    i++;
                }
                String word = input.substring(start, i);
                String upper = word.toUpperCase();

                // Check for single-character variables A, B, C
                if (upper.equals("A") || upper.equals("B") || upper.equals("C")) {
                    tokens.add(new Token(TokenType.VARIABLE, upper, start));
                    continue;
                }

                // Check for operator keywords
                TokenType opType = switch (upper) {
                    case "NOT"  -> TokenType.NOT;
                    case "AND"  -> TokenType.AND;
                    case "NAND" -> TokenType.NAND;
                    case "XOR"  -> TokenType.XOR;
                    case "XNOR" -> TokenType.XNOR;
                    case "OR"   -> TokenType.OR;
                    case "NOR"  -> TokenType.NOR;
                    default     -> null;
                };

                if (opType != null) {
                    tokens.add(new Token(opType, upper, start));
                } else {
                    if (upper.length() == 1) {
                        throw new ExpressionParseException(
                                "Unknown variable '" + word + "' at position " + start + ". Only variables A, B, and C are supported.",
                                start
                        );
                    } else {
                        throw new ExpressionParseException(
                                "Unrecognized operator or identifier '" + word + "' at position " + start +
                                        ". Supported operators: AND, OR, NOT, NAND, NOR, XOR, XNOR.",
                                start
                        );
                    }
                }
                continue;
            }

            // Check for common symbolic shorthand attempts to give friendly error guidance
            if (ch == '!' || ch == '~') {
                throw new ExpressionParseException("Symbol '" + ch + "' is not supported. Use 'NOT' keyword instead.", i);
            }
            if (ch == '&' || ch == '*') {
                throw new ExpressionParseException("Symbol '" + ch + "' is not supported. Use 'AND' keyword instead.", i);
            }
            if (ch == '|' || ch == '+') {
                throw new ExpressionParseException("Symbol '" + ch + "' is not supported. Use 'OR' keyword instead.", i);
            }
            if (ch == '^') {
                throw new ExpressionParseException("Symbol '^' is not supported. Use 'XOR' keyword instead.", i);
            }

            throw new ExpressionParseException("Unexpected character '" + ch + "' at position " + i + ".", i);
        }

        tokens.add(new Token(TokenType.EOF, "", len));
        return tokens;
    }

    // ========================================================================
    // Recursive-Descent Parser
    // ========================================================================

    private static class Parser {
        private final List<Token> tokens;
        private final String rawExpression;
        private int current = 0;

        public Parser(List<Token> tokens, String rawExpression) {
            this.tokens = tokens;
            this.rawExpression = rawExpression;
        }

        public ExpressionNode parse() throws ExpressionParseException {
            if (isAtEnd()) {
                throw new ExpressionParseException("Expression cannot be empty.", 0);
            }

            ExpressionNode expr = parseOr();

            if (!isAtEnd()) {
                Token token = peek();
                Token prev = previous();
                if (token.type == TokenType.VARIABLE) {
                    throw new ExpressionParseException(
                            "Unexpected variable '" + token.text + "' at position " + token.position +
                                    " after '" + prev.text + "'. Did you forget an operator?",
                            token.position
                    );
                }
                if (token.type == TokenType.LPAREN) {
                    throw new ExpressionParseException(
                            "Unexpected '(' at position " + token.position +
                                    " after '" + prev.text + "'. Expected an operator.",
                            token.position
                    );
                }
                if (token.type == TokenType.RPAREN) {
                    throw new ExpressionParseException(
                            "Unexpected closing parenthesis ')' at position " + token.position + " without matching '('.",
                            token.position
                    );
                }
                throw new ExpressionParseException(
                        "Unexpected token '" + token.text + "' at position " + token.position + ".",
                        token.position
                );
            }

            return expr;
        }

        // Level 5: OR, NOR (lowest precedence)
        private ExpressionNode parseOr() throws ExpressionParseException {
            ExpressionNode left = parseXor();

            while (match(TokenType.OR, TokenType.NOR)) {
                Token opToken = previous();
                OperatorType op = (opToken.type == TokenType.OR) ? OperatorType.OR : OperatorType.NOR;
                if (isAtEnd() || check(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Missing operand after '" + opToken.text + "' at position " + opToken.position + ".", opToken.position);
                }
                ExpressionNode right = parseXor();
                left = new BinaryOpNode(left, op, right);
            }

            return left;
        }

        // Level 4: XOR, XNOR
        private ExpressionNode parseXor() throws ExpressionParseException {
            ExpressionNode left = parseAnd();

            while (match(TokenType.XOR, TokenType.XNOR)) {
                Token opToken = previous();
                OperatorType op = (opToken.type == TokenType.XOR) ? OperatorType.XOR : OperatorType.XNOR;
                if (isAtEnd() || check(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Missing operand after '" + opToken.text + "' at position " + opToken.position + ".", opToken.position);
                }
                ExpressionNode right = parseAnd();
                left = new BinaryOpNode(left, op, right);
            }

            return left;
        }

        // Level 3: AND, NAND
        private ExpressionNode parseAnd() throws ExpressionParseException {
            ExpressionNode left = parseUnary();

            while (match(TokenType.AND, TokenType.NAND)) {
                Token opToken = previous();
                OperatorType op = (opToken.type == TokenType.AND) ? OperatorType.AND : OperatorType.NAND;
                if (isAtEnd() || check(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Missing operand after '" + opToken.text + "' at position " + opToken.position + ".", opToken.position);
                }
                ExpressionNode right = parseUnary();
                left = new BinaryOpNode(left, op, right);
            }

            return left;
        }

        // Level 2: NOT (unary prefix, right-associative)
        private ExpressionNode parseUnary() throws ExpressionParseException {
            if (match(TokenType.NOT)) {
                Token notToken = previous();
                if (isAtEnd() || check(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Missing operand after 'NOT' at position " + notToken.position + ".", notToken.position);
                }
                ExpressionNode operand = parseUnary();
                return new NotNode(operand);
            }

            return parsePrimary();
        }

        // Level 1: Primary (Variables, Parentheses)
        private ExpressionNode parsePrimary() throws ExpressionParseException {
            Token token = peek();

            if (match(TokenType.VARIABLE)) {
                return new VariableNode(token.text);
            }

            if (match(TokenType.LPAREN)) {
                Token lparen = previous();
                if (check(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Empty parentheses '()' at position " + lparen.position + " are not allowed.", lparen.position);
                }
                ExpressionNode expr = parseOr();
                if (!match(TokenType.RPAREN)) {
                    throw new ExpressionParseException("Unmatched opening parenthesis '(' at position " + lparen.position + ". Missing closing ')'.", lparen.position);
                }
                return expr;
            }

            if (token.type == TokenType.RPAREN) {
                throw new ExpressionParseException("Unexpected closing parenthesis ')' at position " + token.position + " without matching '('.", token.position);
            }

            if (isBinaryOp(token.type)) {
                throw new ExpressionParseException("Unexpected binary operator '" + token.text + "' at position " + token.position + ". Missing left-hand operand.", token.position);
            }

            if (token.type == TokenType.EOF) {
                Token prev = (current > 0) ? tokens.get(current - 1) : token;
                throw new ExpressionParseException("Expected expression after '" + prev.text + "' at position " + prev.position + ".", prev.position);
            }

            throw new ExpressionParseException("Unexpected token '" + token.text + "' at position " + token.position + ".", token.position);
        }

        private boolean match(TokenType... types) {
            for (TokenType type : types) {
                if (check(type)) {
                    advance();
                    return true;
                }
            }
            return false;
        }

        private boolean check(TokenType type) {
            if (isAtEnd()) return type == TokenType.EOF;
            return peek().type == type;
        }

        private Token advance() {
            if (!isAtEnd()) current++;
            return previous();
        }

        private boolean isAtEnd() {
            return peek().type == TokenType.EOF;
        }

        private Token peek() {
            return tokens.get(current);
        }

        private Token previous() {
            return tokens.get(current - 1);
        }

        private boolean isBinaryOp(TokenType type) {
            return type == TokenType.AND || type == TokenType.OR ||
                   type == TokenType.NAND || type == TokenType.NOR ||
                   type == TokenType.XOR || type == TokenType.XNOR;
        }
    }
}

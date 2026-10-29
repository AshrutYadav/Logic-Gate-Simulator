package frontend;

import backend.BooleanExpression;
import backend.ExpressionParseException;
import backend.ExpressionTruthTableRow;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX View component for the Boolean Expression Lab.
 * Faithfully matches the Digital Logic Laboratory UI theme:
 * - Expression input editor with syntax validation
 * - Dynamic 2^n truth table with real-time active row highlighting
 * - Interactive input switches for detected variables (A, B, C)
 * - Pedagogical step-by-step evaluation trace
 * - Mathematical equation renderer
 * - Example expressions library
 */
public class BooleanExpressionLabView extends HBox {

    // Preset examples matching screenshot and lab curriculum
    private static final String[] PRESET_EXAMPLES = {
        "(A AND B) OR C",
        "(A OR B) AND (NOT C)",
        "NOT (A XOR B)",
        "(A NAND B) OR C",
        "(A NOR B) AND C",
        "(A XNOR B)",
        "NOT A AND B",
        "A XOR B XOR C"
    };

    // State
    private BooleanExpression currentExpression;
    private String lastEvaluatedText = "";
    private boolean inputA = true;
    private boolean inputB = false;
    private boolean inputC = true;
    private boolean isSyncingInputs = false;

    // UI Elements - Left Sidebar
    private final List<Button> exampleButtons = new ArrayList<>();

    // UI Elements - Main Area
    private TextField txtExpression;
    private Label lblModifiedBadge;
    private HBox errorBanner;
    private Label lblErrorMessage;

    // Input Cards (A, B, C)
    private MainWindow.ToggleSwitch switchA;
    private MainWindow.ToggleSwitch switchB;
    private MainWindow.ToggleSwitch switchC;
    private Label badgeA;
    private Label badgeB;
    private Label badgeC;
    private VBox colVarA;
    private VBox colVarB;
    private VBox colVarC;

    // Output Indicator
    private Circle outputLedCircle;
    private Label outputValueLabel;
    private Label outputPillLabel;

    // Parsed Expression
    private Label lblParsedMath;
    private HBox varChipsContainer;

    // Truth Table
    private TableView<ExpressionTruthTableRow> truthTable;
    private Label lblTableInfo;

    // Evaluation Steps
    private VBox stepsContainer;

    // Callback for status bar updates
    private Consumer<String> statusCallback;

    public BooleanExpressionLabView(Consumer<String> statusCallback) {
        super(14);
        this.statusCallback = statusCallback;
        setPadding(new Insets(0, 0, 0, 0));
        VBox.setVgrow(this, Priority.ALWAYS);

        // 1. Left Sidebar (Examples and Supported Operators)
        Region sidebar = buildSidebar();

        // 2. Main Right Content Area
        Region mainArea = buildMainArea();
        HBox.setHgrow(mainArea, Priority.ALWAYS);

        getChildren().addAll(sidebar, mainArea);

        // Load initial default expression
        loadExpression("(A AND B) OR C");
    }

    // ========================================================================
    // UI Construction - Left Sidebar
    // ========================================================================

    private Region buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("panel-card");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);
        sidebar.setPadding(new Insets(14, 12, 14, 12));

        Label title = new Label("Examples");
        title.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 0 0 4 2;");
        sidebar.getChildren().add(title);

        VBox exampleList = new VBox(6);
        for (String expr : PRESET_EXAMPLES) {
            Button btn = createExampleButton(expr);
            exampleButtons.add(btn);
            exampleList.getChildren().add(btn);
        }
        sidebar.getChildren().add(exampleList);

        Region spring = new Region();
        VBox.setVgrow(spring, Priority.ALWAYS);
        sidebar.getChildren().add(spring);

        // Bottom Card: Supported Operators
        VBox operatorsCard = new VBox(6);
        operatorsCard.getStyleClass().add("sub-card");
        operatorsCard.setPadding(new Insets(10, 10, 10, 10));

        Label opTitle = new Label("Supported Operators");
        opTitle.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label opList1 = new Label("AND    OR    NOT    NAND");
        opList1.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label opList2 = new Label("NOR    XOR   XNOR");
        opList2.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label opNote = new Label("Use parentheses ( ) to group expressions. Operators are case-insensitive.");
        opNote.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 10px;");
        opNote.setWrapText(true);

        operatorsCard.getChildren().addAll(opTitle, opList1, opList2, opNote);
        sidebar.getChildren().add(operatorsCard);

        return sidebar;
    }

    private Button createExampleButton(String expr) {
        Button btn = new Button(expr);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 10, 0, 10));
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setCursor(Cursor.HAND);
        btn.setStyle("-fx-background-color: #0B192B; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                     "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        btn.setOnMouseEntered(e -> {
            if (!expr.equalsIgnoreCase(txtExpression.getText().trim())) {
                btn.setStyle("-fx-background-color: #10223A; -fx-text-fill: #F1F6FF; -fx-border-color: #35C8FF; " +
                             "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!expr.equalsIgnoreCase(txtExpression.getText().trim())) {
                btn.setStyle("-fx-background-color: #0B192B; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                             "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            }
        });

        btn.setOnAction(e -> loadExpression(expr));
        return btn;
    }

    private void updateExampleButtonStyles(String activeExpr) {
        for (Button btn : exampleButtons) {
            if (btn.getText().equalsIgnoreCase(activeExpr)) {
                btn.setStyle("-fx-background-color: #087BFF; -fx-text-fill: #FFFFFF; -fx-border-color: #35C8FF; " +
                             "-fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            } else {
                btn.setStyle("-fx-background-color: #0B192B; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                             "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
            }
        }
    }

    // ========================================================================
    // UI Construction - Main Content Area
    // ========================================================================

    private Region buildMainArea() {
        VBox main = new VBox(10);
        VBox.setVgrow(main, Priority.ALWAYS);

        // 1. Top Section: Header & Expression Editor Card
        Region editorCard = buildEditorCard();

        // 2. Middle Section: Input Values, Output, and Parsed Expression Cards
        Region middleRow = buildMiddleCardsRow();

        // 3. Bottom Section: Truth Table and Step-by-Step Evaluation Cards
        Region bottomRow = buildBottomCardsRow();
        VBox.setVgrow(bottomRow, Priority.ALWAYS);

        main.getChildren().addAll(editorCard, middleRow, bottomRow);
        return main;
    }

    private Region buildEditorCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(14, 16, 12, 16));

        // Header Row: Title & Subtitle on left, Example Expressions Guide on right
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Boolean Expression Lab");
        title.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitle = new Label("Enter a boolean expression using variables A, B and C.");
        subtitle.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Syntax Guide Sub-card (top right)
        VBox guideCard = new VBox(3);
        guideCard.getStyleClass().add("sub-card");
        guideCard.setPadding(new Insets(6, 12, 6, 12));
        guideCard.setPrefWidth(210);

        Label guideHeader = new Label("Example Expressions");
        guideHeader.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label ex1 = new Label("• A AND B");
        Label ex2 = new Label("• (A AND B) OR C");
        Label ex3 = new Label("• NOT (A OR B)");
        Label ex4 = new Label("• (A XOR B) AND C");
        Label ex5 = new Label("• (A NAND B) OR C");

        String bulletStyle = "-fx-text-fill: #A8C4EA; -fx-font-size: 10px;";
        ex1.setStyle(bulletStyle);
        ex2.setStyle(bulletStyle);
        ex3.setStyle(bulletStyle);
        ex4.setStyle(bulletStyle);
        ex5.setStyle(bulletStyle);

        guideCard.getChildren().addAll(guideHeader, ex1, ex2, ex3, ex4, ex5);
        topRow.getChildren().addAll(titleBox, guideCard);
        card.getChildren().add(topRow);

        // Expression Label & Modified Badge
        HBox labelRow = new HBox(8);
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label lblExpr = new Label("Expression");
        lblExpr.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px; -fx-font-weight: bold;");

        lblModifiedBadge = new Label("● Modified — press Evaluate or Enter");
        lblModifiedBadge.setStyle("-fx-text-fill: #FFB020; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblModifiedBadge.setVisible(false);
        lblModifiedBadge.setManaged(false);

        labelRow.getChildren().addAll(lblExpr, lblModifiedBadge);
        card.getChildren().add(labelRow);

        // Text Field Input
        txtExpression = new TextField("(A AND B) OR C");
        txtExpression.setPrefHeight(40);
        txtExpression.setStyle("-fx-background-color: #06111F; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                               "-fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
                               "-fx-padding: 8px 14px; -fx-font-size: 15px; -fx-font-weight: bold;");

        txtExpression.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean modified = !newVal.trim().equalsIgnoreCase(lastEvaluatedText);
            lblModifiedBadge.setVisible(modified);
            lblModifiedBadge.setManaged(modified);
            if (modified) {
                // Clear error banner on new user typing
                hideError();
            }
        });

        txtExpression.setOnAction(e -> evaluateAndGenerate());
        card.getChildren().add(txtExpression);

        // Action Buttons Row
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        actionsRow.setPadding(new Insets(2, 0, 0, 0));

        Button btnEval = new Button("▷ Evaluate");
        btnEval.setCursor(Cursor.HAND);
        btnEval.setStyle("-fx-background-color: #087BFF; -fx-text-fill: #FFFFFF; -fx-border-color: #35C8FF; " +
                         "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; " +
                         "-fx-padding: 8px 20px; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnEval.setOnAction(e -> evaluateAndGenerate());

        Button btnTruthTable = new Button("▦ Generate Truth Table");
        btnTruthTable.getStyleClass().add("action-btn");
        btnTruthTable.setOnAction(e -> evaluateAndGenerate());

        Button btnClear = new Button("🗑 Clear");
        btnClear.getStyleClass().add("action-btn");
        btnClear.setOnAction(e -> clearExpression());

        MenuButton btnLoadExample = new MenuButton("📄 Load Example");
        btnLoadExample.getStyleClass().add("action-btn");
        for (String preset : PRESET_EXAMPLES) {
            MenuItem item = new MenuItem(preset);
            item.setOnAction(e -> loadExpression(preset));
            btnLoadExample.getItems().add(item);
        }

        actionsRow.getChildren().addAll(btnEval, btnTruthTable, btnClear, btnLoadExample);
        card.getChildren().add(actionsRow);

        // Validation Error Banner (initially hidden)
        errorBanner = new HBox(10);
        errorBanner.setAlignment(Pos.CENTER_LEFT);
        errorBanner.setStyle("-fx-background-color: #2E1418; -fx-border-color: #FF4D4D; -fx-border-width: 1px; " +
                             "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 8px 14px;");
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);

        Label errIcon = new Label("⚠");
        errIcon.setStyle("-fx-text-fill: #FF4D4D; -fx-font-size: 16px; -fx-font-weight: bold;");

        lblErrorMessage = new Label();
        lblErrorMessage.setStyle("-fx-text-fill: #FFA8A8; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblErrorMessage.setWrapText(true);

        errorBanner.getChildren().addAll(errIcon, lblErrorMessage);
        card.getChildren().add(errorBanner);

        return card;
    }

    private Region buildMiddleCardsRow() {
        HBox row = new HBox(12);
        row.setPrefHeight(120);
        row.setMinHeight(115);
        row.setMaxHeight(135);

        // 1. Input Values Card
        VBox inputCard = new VBox(6);
        inputCard.getStyleClass().add("panel-card");
        inputCard.setPadding(new Insets(10, 14, 10, 14));
        HBox.setHgrow(inputCard, Priority.ALWAYS);

        Label inTitle = new Label("Input Values");
        inTitle.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");
        inputCard.getChildren().add(inTitle);

        HBox inputsBox = new HBox(16);
        inputsBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(inputsBox, Priority.ALWAYS);

        // Variable A Container
        colVarA = createVariableControl("A", inputA, val -> {
            inputA = val;
            onInputSwitchesChanged();
        });
        switchA = (MainWindow.ToggleSwitch) colVarA.getUserData();
        badgeA = (Label) colVarA.getChildren().get(2);

        // Variable B Container
        colVarB = createVariableControl("B", inputB, val -> {
            inputB = val;
            onInputSwitchesChanged();
        });
        switchB = (MainWindow.ToggleSwitch) colVarB.getUserData();
        badgeB = (Label) colVarB.getChildren().get(2);

        // Variable C Container
        colVarC = createVariableControl("C", inputC, val -> {
            inputC = val;
            onInputSwitchesChanged();
        });
        switchC = (MainWindow.ToggleSwitch) colVarC.getUserData();
        badgeC = (Label) colVarC.getChildren().get(2);

        inputsBox.getChildren().addAll(colVarA, colVarB, colVarC);
        inputCard.getChildren().add(inputsBox);

        // 2. Output Card
        VBox outputCard = new VBox(6);
        outputCard.getStyleClass().add("panel-card");
        outputCard.setPadding(new Insets(10, 16, 10, 16));
        outputCard.setPrefWidth(160);
        outputCard.setMinWidth(150);
        outputCard.setMaxWidth(180);
        outputCard.setAlignment(Pos.TOP_CENTER);

        Label outTitle = new Label("Output");
        outTitle.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Glowing circle indicator
        HBox circleBox = new HBox();
        circleBox.setAlignment(Pos.CENTER);
        circleBox.setPrefHeight(48);

        outputValueLabel = new Label("1");
        outputValueLabel.setPrefSize(42, 42);
        outputValueLabel.setAlignment(Pos.CENTER);
        outputValueLabel.setStyle("-fx-background-color: #06111F; -fx-text-fill: #19E69A; " +
                                  "-fx-border-color: #19E69A; -fx-border-width: 2.5px; -fx-border-radius: 21px; " +
                                  "-fx-background-radius: 21px; -fx-font-weight: bold; -fx-font-size: 19px;");

        circleBox.getChildren().add(outputValueLabel);

        outputPillLabel = new Label("HIGH");
        outputPillLabel.setPrefSize(56, 20);
        outputPillLabel.setAlignment(Pos.CENTER);
        outputPillLabel.setStyle("-fx-background-color: #0B2C20; -fx-text-fill: #19E69A; " +
                                 "-fx-border-color: #19E69A; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                 "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");

        outputCard.getChildren().addAll(outTitle, circleBox, outputPillLabel);

        // 3. Parsed Expression Card
        VBox parsedCard = new VBox(6);
        parsedCard.getStyleClass().add("panel-card");
        parsedCard.setPadding(new Insets(10, 14, 10, 14));
        HBox.setHgrow(parsedCard, Priority.ALWAYS);

        Label parsedTitle = new Label("Parsed Expression");
        parsedTitle.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");

        lblParsedMath = new Label("Y = (A · B) + C");
        lblParsedMath.setAlignment(Pos.CENTER);
        lblParsedMath.setMaxWidth(Double.MAX_VALUE);
        lblParsedMath.setPrefHeight(34);
        lblParsedMath.setStyle("-fx-background-color: #06111F; -fx-text-fill: #35C8FF; -fx-border-color: #203956; " +
                               "-fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                               "-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox varsDetectedRow = new HBox(8);
        varsDetectedRow.setAlignment(Pos.CENTER_LEFT);
        Label lblVarDet = new Label("Variables Detected:");
        lblVarDet.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 11px;");

        varChipsContainer = new HBox(6);
        varChipsContainer.setAlignment(Pos.CENTER_LEFT);
        varsDetectedRow.getChildren().addAll(lblVarDet, varChipsContainer);

        parsedCard.getChildren().addAll(parsedTitle, lblParsedMath, varsDetectedRow);

        row.getChildren().addAll(inputCard, outputCard, parsedCard);
        return row;
    }

    private VBox createVariableControl(String varName, boolean initialVal, Consumer<Boolean> onValChanged) {
        VBox cont = new VBox(4);
        cont.setAlignment(Pos.CENTER);
        cont.getStyleClass().add("sub-card");
        cont.setPadding(new Insets(6, 12, 6, 12));
        cont.setPrefWidth(95);

        Label lblVar = new Label(varName);
        lblVar.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold; -fx-font-size: 13px;");

        MainWindow.ToggleSwitch toggle = new MainWindow.ToggleSwitch();
        toggle.setSelected(initialVal);

        Label badge = new Label(initialVal ? "1" : "0");
        badge.setPrefSize(28, 22);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: #06111F; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                       "-fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                       "-fx-font-weight: bold; -fx-font-size: 12px;");

        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            badge.setText(newVal ? "1" : "0");
            if (!isSyncingInputs) {
                onValChanged.accept(newVal);
            }
        });

        cont.getChildren().addAll(lblVar, toggle, badge);
        cont.setUserData(toggle);
        return cont;
    }

    private Region buildBottomCardsRow() {
        HBox row = new HBox(12);
        VBox.setVgrow(row, Priority.ALWAYS);

        // 1. Truth Table Card
        VBox tableCard = new VBox(8);
        tableCard.getStyleClass().add("panel-card");
        tableCard.setPadding(new Insets(12, 14, 12, 14));
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label tableTitle = new Label("Truth Table");
        tableTitle.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 14px; -fx-font-weight: bold;");

        lblTableInfo = new Label("(8 rows)");
        lblTableInfo.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 11px;");
        tableHeader.getChildren().addAll(tableTitle, lblTableInfo);
        tableCard.getChildren().add(tableHeader);

        // TableView for Truth Table
        truthTable = new TableView<>();
        truthTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        truthTable.widthProperty().addListener((obs, oldVal, newVal) -> updateTruthTableColumnWidths());
        VBox.setVgrow(truthTable, Priority.ALWAYS);

        // Clicking a row in the truth table synchronizes the input switches
        truthTable.setOnMouseClicked(e -> {
            ExpressionTruthTableRow selected = truthTable.getSelectionModel().getSelectedItem();
            if (selected != null && currentExpression != null) {
                applyInputsFromTableRow(selected);
            }
        });

        tableCard.getChildren().add(truthTable);

        Label tableNote = new Label("Active input combination is highlighted. Click any row to apply inputs.");
        tableNote.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 11px; -fx-font-style: italic;");
        tableCard.getChildren().add(tableNote);

        // 2. Evaluation Steps Card
        VBox stepsCard = new VBox(8);
        stepsCard.getStyleClass().add("panel-card");
        stepsCard.setPadding(new Insets(12, 14, 12, 14));
        stepsCard.setPrefWidth(380);
        stepsCard.setMinWidth(320);
        stepsCard.setMaxWidth(440);

        Label stepsTitle = new Label("Evaluation Steps (for current inputs)");
        stepsTitle.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 13px; -fx-font-weight: bold;");
        stepsCard.getChildren().add(stepsTitle);

        ScrollPane stepsScroll = new ScrollPane();
        stepsScroll.setFitToWidth(true);
        stepsScroll.setStyle("-fx-background: #0B192B; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(stepsScroll, Priority.ALWAYS);

        stepsContainer = new VBox(6);
        stepsContainer.setPadding(new Insets(4, 4, 4, 4));
        stepsScroll.setContent(stepsContainer);

        stepsCard.getChildren().add(stepsScroll);

        row.getChildren().addAll(tableCard, stepsCard);
        return row;
    }

    // ========================================================================
    // Application Logic & State Synchronization
    // ========================================================================

    public void loadExpression(String expr) {
        txtExpression.setText(expr);
        evaluateAndGenerate();
    }

    public void evaluateAndGenerate() {
        String raw = txtExpression.getText().trim();
        if (raw.isEmpty()) {
            showError("Expression cannot be empty.");
            return;
        }

        try {
            // Backend recursive-descent parse
            BooleanExpression parsed = BooleanExpression.parse(raw);
            this.currentExpression = parsed;
            this.lastEvaluatedText = raw;

            hideError();
            lblModifiedBadge.setVisible(false);
            lblModifiedBadge.setManaged(false);

            // Update UI
            updateExampleButtonStyles(raw);
            updateParsedExpressionDisplay();
            updateVariableInputControlsVisibility();
            generateAndDisplayTruthTable();
            evaluateCurrentInputs();

            if (statusCallback != null) {
                statusCallback.accept("Boolean Expression Lab  |  Evaluated: " + parsed.getRawExpression() +
                        " (" + parsed.generateTruthTable().size() + " rows)");
            }
        } catch (ExpressionParseException e) {
            showError(e.getMessage());
            if (statusCallback != null) {
                statusCallback.accept("Boolean Expression Lab  |  Validation Error");
            }
        }
    }

    private void onInputSwitchesChanged() {
        if (currentExpression == null) return;
        evaluateCurrentInputs();
        highlightActiveTableRow();
    }

    private void evaluateCurrentInputs() {
        if (currentExpression == null) return;

        Map<String, Boolean> activeInputs = getCurrentInputsMap();

        // 1. Evaluate with backend
        boolean result = currentExpression.evaluate(activeInputs);

        // 2. Update Output Indicator
        outputValueLabel.setText(result ? "1" : "0");
        if (result) {
            outputValueLabel.setStyle("-fx-background-color: #06111F; -fx-text-fill: #19E69A; " +
                                      "-fx-border-color: #19E69A; -fx-border-width: 2.5px; -fx-border-radius: 21px; " +
                                      "-fx-background-radius: 21px; -fx-font-weight: bold; -fx-font-size: 19px;");
            outputPillLabel.setText("HIGH");
            outputPillLabel.setStyle("-fx-background-color: #0B2C20; -fx-text-fill: #19E69A; " +
                                     "-fx-border-color: #19E69A; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                     "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");
        } else {
            outputValueLabel.setStyle("-fx-background-color: #06111F; -fx-text-fill: #A8C4EA; " +
                                      "-fx-border-color: #203956; -fx-border-width: 2.5px; -fx-border-radius: 21px; " +
                                      "-fx-background-radius: 21px; -fx-font-weight: bold; -fx-font-size: 19px;");
            outputPillLabel.setText("LOW");
            outputPillLabel.setStyle("-fx-background-color: #10223A; -fx-text-fill: #A8C4EA; " +
                                     "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                     "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");
        }

        // 3. Update Step-by-Step Evaluation Trace
        updateEvaluationSteps(activeInputs);
    }

    private void updateParsedExpressionDisplay() {
        if (currentExpression == null) return;

        lblParsedMath.setText(currentExpression.toMathematicalString());

        varChipsContainer.getChildren().clear();
        for (String var : currentExpression.getVariables()) {
            Label chip = new Label(var);
            chip.setStyle("-fx-background-color: #10223A; -fx-text-fill: #35C8FF; -fx-border-color: #203956; " +
                          "-fx-border-width: 1px; -fx-border-radius: 12px; -fx-background-radius: 12px; " +
                          "-fx-padding: 2px 10px; -fx-font-weight: bold; -fx-font-size: 11px;");
            varChipsContainer.getChildren().add(chip);
        }
    }

    private void updateVariableInputControlsVisibility() {
        if (currentExpression == null) return;

        List<String> vars = currentExpression.getVariables();
        boolean hasA = vars.contains("A");
        boolean hasB = vars.contains("B");
        boolean hasC = vars.contains("C");

        setControlDimState(colVarA, hasA);
        setControlDimState(colVarB, hasB);
        setControlDimState(colVarC, hasC);
    }

    private void setControlDimState(VBox col, boolean active) {
        col.setOpacity(active ? 1.0 : 0.35);
        col.setDisable(!active);
    }

    private void generateAndDisplayTruthTable() {
        if (currentExpression == null) return;

        truthTable.getColumns().clear();
        List<String> vars = currentExpression.getVariables();

        for (String var : vars) {
            TableColumn<ExpressionTruthTableRow, String> col = new TableColumn<>(var);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInputString(var)));
            col.setStyle("-fx-alignment: CENTER;");
            col.setReorderable(false);
            truthTable.getColumns().add(col);
        }

        TableColumn<ExpressionTruthTableRow, String> colOut = new TableColumn<>("Output (Y)");
        colOut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOutputString()));
        colOut.setStyle("-fx-alignment: CENTER;");
        colOut.setReorderable(false);
        colOut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("1".equals(item)) {
                        setStyle("-fx-text-fill: #19E69A; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #A8C4EA; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    }
                }
            }
        });
        truthTable.getColumns().add(colOut);

        updateTruthTableColumnWidths();
        Platform.runLater(this::updateTruthTableColumnWidths);

        List<ExpressionTruthTableRow> rows = currentExpression.generateTruthTable();
        truthTable.setItems(FXCollections.observableArrayList(rows));
        lblTableInfo.setText("(" + rows.size() + " rows)");

        highlightActiveTableRow();
    }

    private void updateTruthTableColumnWidths() {
        if (truthTable == null) return;
        List<TableColumn<ExpressionTruthTableRow, ?>> cols = truthTable.getColumns();
        if (cols.isEmpty()) return;

        double tableWidth = truthTable.getWidth();
        if (tableWidth <= 0) {
            tableWidth = 460.0;
        }

        // Reserve space for potential vertical scrollbar (18px) and borders
        double available = Math.max(260.0, tableWidth - 18.0);

        int totalColumns = cols.size();
        int varCount = totalColumns - 1; // Last column is Output (Y)
        if (varCount <= 0) return;

        // Weights: Variables = 1.0 each, Output (Y) = 1.45 so "Output (Y)" has ample width
        double varWeight = 1.0;
        double outWeight = 1.45;
        double totalWeight = (varCount * varWeight) + outWeight;

        double varWidth = Math.floor(available * (varWeight / totalWeight));
        double outWidth = Math.max(120.0, available - (varWidth * varCount));

        for (int i = 0; i < varCount; i++) {
            TableColumn<?, ?> col = cols.get(i);
            col.setMinWidth(60.0);
            col.setPrefWidth(varWidth);
            col.setMaxWidth(Double.MAX_VALUE);
            col.setReorderable(false);
        }

        TableColumn<?, ?> colOut = cols.get(totalColumns - 1);
        colOut.setMinWidth(120.0);
        colOut.setPrefWidth(outWidth);
        colOut.setMaxWidth(Double.MAX_VALUE);
        colOut.setReorderable(false);
    }

    private void highlightActiveTableRow() {
        if (currentExpression == null || truthTable.getItems().isEmpty()) return;

        Map<String, Boolean> active = getCurrentInputsMap();
        List<ExpressionTruthTableRow> items = truthTable.getItems();

        int targetIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).matchesInputs(active)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex >= 0) {
            truthTable.getSelectionModel().select(targetIndex);
            truthTable.scrollTo(targetIndex);
        }
    }

    private void applyInputsFromTableRow(ExpressionTruthTableRow row) {
        isSyncingInputs = true;
        try {
            if (row.getInputs().containsKey("A")) {
                inputA = row.getInputValue("A");
                switchA.setSelected(inputA);
                badgeA.setText(inputA ? "1" : "0");
            }
            if (row.getInputs().containsKey("B")) {
                inputB = row.getInputValue("B");
                switchB.setSelected(inputB);
                badgeB.setText(inputB ? "1" : "0");
            }
            if (row.getInputs().containsKey("C")) {
                inputC = row.getInputValue("C");
                switchC.setSelected(inputC);
                badgeC.setText(inputC ? "1" : "0");
            }
            evaluateCurrentInputs();
        } finally {
            isSyncingInputs = false;
        }
    }

    private void updateEvaluationSteps(Map<String, Boolean> activeInputs) {
        stepsContainer.getChildren().clear();
        if (currentExpression == null) return;

        List<String> steps = currentExpression.getEvaluationSteps(activeInputs);
        for (int i = 0; i < steps.size(); i++) {
            String stepText = steps.get(i);
            HBox stepRow = new HBox(10);
            stepRow.setAlignment(Pos.CENTER_LEFT);
            stepRow.setPadding(new Insets(4, 6, 4, 6));
            stepRow.setStyle("-fx-background-color: #06111F; -fx-border-color: #162B46; -fx-border-width: 1px; " +
                             "-fx-border-radius: 6px; -fx-background-radius: 6px;");

            // Circle step badge
            Label numBadge = new Label(String.valueOf(i + 1));
            numBadge.setPrefSize(22, 22);
            numBadge.setMinSize(22, 22);
            numBadge.setMaxSize(22, 22);
            numBadge.setAlignment(Pos.CENTER);
            numBadge.setStyle("-fx-background-color: #10223A; -fx-text-fill: #35C8FF; -fx-border-color: #203956; " +
                              "-fx-border-width: 1px; -fx-border-radius: 11px; -fx-background-radius: 11px; " +
                              "-fx-font-weight: bold; -fx-font-size: 11px;");

            Label textLabel = new Label(stepText);
            textLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            textLabel.setStyle("-fx-text-fill: #F1F6FF;");

            // Highlight result ending with = 1 in green
            if (stepText.endsWith("= 1")) {
                textLabel.setStyle("-fx-text-fill: #19E69A; -fx-font-weight: bold;");
            } else if (stepText.endsWith("= 0")) {
                textLabel.setStyle("-fx-text-fill: #A8C4EA; -fx-font-weight: bold;");
            }

            stepRow.getChildren().addAll(numBadge, textLabel);
            stepsContainer.getChildren().add(stepRow);
        }
    }

    private Map<String, Boolean> getCurrentInputsMap() {
        Map<String, Boolean> map = new HashMap<>();
        map.put("A", inputA);
        map.put("B", inputB);
        map.put("C", inputC);
        return map;
    }

    private void clearExpression() {
        txtExpression.clear();
        currentExpression = null;
        lastEvaluatedText = "";
        hideError();
        lblModifiedBadge.setVisible(false);
        lblModifiedBadge.setManaged(false);

        lblParsedMath.setText("—");
        varChipsContainer.getChildren().clear();
        truthTable.getColumns().clear();
        truthTable.getItems().clear();
        lblTableInfo.setText("(0 rows)");
        stepsContainer.getChildren().clear();

        outputValueLabel.setText("0");
        outputValueLabel.setStyle("-fx-background-color: #06111F; -fx-text-fill: #A8C4EA; -fx-border-color: #203956; " +
                                  "-fx-border-width: 2.5px; -fx-border-radius: 21px; -fx-background-radius: 21px; " +
                                  "-fx-font-weight: bold; -fx-font-size: 19px;");
        outputPillLabel.setText("IDLE");
        outputPillLabel.setStyle("-fx-background-color: #10223A; -fx-text-fill: #A8C4EA; -fx-border-color: #203956; " +
                                 "-fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                                 "-fx-font-weight: bold; -fx-font-size: 10px;");

        for (Button btn : exampleButtons) {
            btn.setStyle("-fx-background-color: #0B192B; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                         "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        }

        if (statusCallback != null) {
            statusCallback.accept("Boolean Expression Lab  |  Expression cleared");
        }
    }

    private void showError(String message) {
        lblErrorMessage.setText(message);
        errorBanner.setVisible(true);
        errorBanner.setManaged(true);
    }

    private void hideError() {
        errorBanner.setVisible(false);
        errorBanner.setManaged(false);
    }
}

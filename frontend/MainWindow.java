package frontend;

import backend.GateFactory;
import backend.GateStorage;
import backend.GateStorage.SimulatorState;
import backend.GateStorage.StorageResult;
import backend.GateType;
import backend.LogicGate;
import backend.TruthTableRow;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main application window for Logic Gate Simulator.
 * Faithfully reproduces the Qt 6 Widgets desktop interface using JavaFX.
 * Demonstrates: Object-Oriented UI Architecture, Event Handling, JavaFX Custom Controls,
 * Vector Graphics rendering, and Separation of Concerns.
 */
public class MainWindow extends Application {

    // ========================================================================
    // Application State
    // ========================================================================
    private LogicGate currentGate;
    private GateType currentGateType = GateType.AND;
    private boolean inputA = true;
    private boolean inputB = false;
    private boolean currentOutput = false;
    private boolean isUpdatingState = false; // Prevents recursive listener cascades

    // ========================================================================
    // UI Controls
    // ========================================================================
    private final List<GateCardButton> gateButtons = new ArrayList<>();
    private Label lblTitle;
    private Label lblSubtitle;
    private CircuitCanvas canvasWidget;

    // Information labels
    private Label lblInfoName;
    private Label lblInfoInputs;
    private Label lblInfoDesc;
    private Label lblBoolExpr;

    // Truth Table
    private TableView<TruthTableRow> truthTable;
    private TableColumn<TruthTableRow, String> colB;

    // Quick Test Controls
    private ToggleSwitch quickSwitchA;
    private ToggleSwitch quickSwitchB;
    private Label quickBadgeA;
    private Label quickBadgeB;
    private HBox quickContainerB;
    private Label quickOutputVal;
    private Label quickOutputPill;

    // Status Bar Labels
    private Label statusMsg;
    private Label statusRightMsg;

    private Stage primaryStage;
    private BorderPane mainRoot;
    private Region individualGateView;
    private BooleanExpressionLabView expressionLabView;
    private Button navTabIndividual;
    private Button navTabExpressionLab;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Logic Gate Simulator — Digital Logic Laboratory");
        stage.setMinWidth(1080);
        stage.setMinHeight(720);

        BorderPane root = new BorderPane();
        this.mainRoot = root;
        root.setStyle("-fx-background-color: #06111F; -fx-font-family: 'Segoe UI', sans-serif;");
        root.setPadding(new Insets(14, 18, 10, 18));

        // 1. Top Navbar
        root.setTop(buildNavbar());

        // 2. Status Bar pinned at bottom (initialized first so callbacks have statusMsg)
        root.setBottom(buildStatusBar());

        // 3. Center Content Area (Main Simulation Row + Quick Test Bar)
        VBox centerArea = new VBox(12);
        centerArea.setPadding(new Insets(10, 0, 10, 0));

        HBox mainRow = new HBox(14);
        VBox.setVgrow(mainRow, Priority.ALWAYS);

        Region sidebar = buildSidebar();
        Region centerSim = buildCenter();
        Region truthTableCard = buildTruthTable();

        HBox.setHgrow(centerSim, Priority.ALWAYS);

        mainRow.getChildren().addAll(sidebar, centerSim, truthTableCard);

        // Quick Test Bar
        Region quickTestBar = buildQuickTest();

        centerArea.getChildren().addAll(mainRow, quickTestBar);
        this.individualGateView = centerArea;
        this.expressionLabView = new BooleanExpressionLabView(this::updateStatusFromLab);

        root.setCenter(individualGateView);

        Scene scene = new Scene(root, 1440, 900);
        applyGlobalStyles(scene);

        stage.setScene(scene);
        stage.show();

        // Initial gate selection
        selectGate(GateType.AND);
    }

    // ========================================================================
    // CSS Stylesheet Setup
    // ========================================================================
    private void applyGlobalStyles(Scene scene) {
        String css = """
            .panel-card {
                -fx-background-color: #0B192B;
                -fx-border-color: #203956;
                -fx-border-width: 1px;
                -fx-border-radius: 12px;
                -fx-background-radius: 12px;
            }
            .sub-card {
                -fx-background-color: #10223A;
                -fx-border-color: #203956;
                -fx-border-width: 1px;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
            }
            .action-btn {
                -fx-background-color: #10223A;
                -fx-text-fill: #F1F6FF;
                -fx-border-color: #203956;
                -fx-border-width: 1px;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
                -fx-padding: 8px 18px;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
            }
            .action-btn:hover {
                -fx-background-color: #087BFF;
                -fx-border-color: #35C8FF;
            }
            .table-view {
                -fx-background-color: #0B192B;
                -fx-border-color: #203956;
                -fx-border-width: 1px;
                -fx-border-radius: 10px;
                -fx-background-radius: 10px;
            }
            .table-view .column-header-background {
                -fx-background-color: #10223A;
                -fx-border-color: #203956;
                -fx-border-width: 0 0 1px 0;
            }
            .table-view .column-header {
                -fx-background-color: #10223A;
                -fx-padding: 8px;
            }
            .table-view .column-header .label {
                -fx-text-fill: #A8C4EA;
                -fx-font-weight: bold;
                -fx-font-size: 13px;
                -fx-alignment: CENTER;
            }
            .table-view .table-row-cell {
                -fx-background-color: #0B192B;
                -fx-border-color: #162B46;
                -fx-border-width: 0 0 1px 0;
                -fx-cell-size: 38px;
                -fx-cursor: hand;
            }
            .table-view .table-row-cell:filled:selected {
                -fx-background-color: #133863;
                -fx-border-color: #35C8FF;
                -fx-border-width: 1px 0 1px 0;
            }
            .table-view .table-row-cell:empty {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
            }
            .table-view .table-row-cell:empty .table-cell {
                -fx-border-color: transparent;
            }
            .table-view .placeholder {
                -fx-background-color: transparent;
            }
            .table-view .table-cell {
                -fx-text-fill: #F1F6FF;
                -fx-alignment: CENTER;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
            }
            .scroll-bar:vertical, .scroll-bar:horizontal {
                -fx-background-color: #0B192B;
            }
            .scroll-bar .thumb {
                -fx-background-color: #203956;
                -fx-background-radius: 4px;
            }
        """;
        scene.getStylesheets().add("data:text/css," + css.replace("\n", " "));
    }

    // ========================================================================
    // UI Builders
    // ========================================================================

    private Region buildNavbar() {
        HBox nav = new HBox(12);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0, 0, 4, 0));

        // IC Chip Logo Icon
        Canvas logo = new Canvas(36, 36);
        drawIcLogo(logo.getGraphicsContext2D());
        nav.getChildren().add(logo);

        // Titles
        VBox titleBox = new VBox(1);
        Label title = new Label("Logic Gate Simulator");
        title.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #F1F6FF;");
        Label subtitle = new Label("Digital Logic Laboratory");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #A8C4EA;");
        titleBox.getChildren().addAll(title, subtitle);
        nav.getChildren().add(titleBox);

        Region spacerLeft = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);

        // Segmented Tab Switcher (Individual Gate Simulator vs. Boolean Expression Lab)
        HBox tabContainer = new HBox(4);
        tabContainer.setAlignment(Pos.CENTER);
        tabContainer.setStyle("-fx-background-color: #06111F; -fx-padding: 3px; -fx-border-color: #203956; " +
                              "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        navTabIndividual = new Button("Individual Gate Simulator");
        navTabIndividual.setCursor(Cursor.HAND);
        navTabIndividual.setOnAction(e -> switchToTab(false));

        navTabExpressionLab = new Button("Boolean Expression Lab");
        navTabExpressionLab.setCursor(Cursor.HAND);
        navTabExpressionLab.setOnAction(e -> switchToTab(true));

        tabContainer.getChildren().addAll(navTabIndividual, navTabExpressionLab);
        updateNavTabStyles(false);

        Region spacerRight = new Region();
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        // Save & Load Buttons
        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("action-btn");
        btnSave.setOnAction(e -> saveState());

        Button btnLoad = new Button("Load");
        btnLoad.getStyleClass().add("action-btn");
        btnLoad.setOnAction(e -> loadState());

        nav.getChildren().addAll(spacerLeft, tabContainer, spacerRight, btnSave, btnLoad);
        return nav;
    }

    private void switchToTab(boolean isLab) {
        updateNavTabStyles(isLab);
        if (isLab) {
            mainRoot.setCenter(expressionLabView);
            statusMsg.setText("Ready  |  Boolean Expression Lab");
            statusRightMsg.setText("ℹ Enter a Boolean expression, explore the truth table, and inspect evaluation steps.");
        } else {
            mainRoot.setCenter(individualGateView);
            updateSimulation();
            statusRightMsg.setText("ℹ Select a gate, change inputs, and explore the truth table.");
        }
    }

    private void updateNavTabStyles(boolean isLab) {
        String activeStyle = "-fx-background-color: #087BFF; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; " +
                             "-fx-font-size: 13px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 7px 18px;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #A8C4EA; -fx-font-weight: bold; " +
                               "-fx-font-size: 13px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 7px 18px;";

        if (isLab) {
            navTabIndividual.setStyle(inactiveStyle);
            navTabExpressionLab.setStyle(activeStyle);
        } else {
            navTabIndividual.setStyle(activeStyle);
            navTabExpressionLab.setStyle(inactiveStyle);
        }
    }

    private void updateStatusFromLab(String msg) {
        if (statusMsg != null) {
            statusMsg.setText(msg);
        }
    }

    private Region buildSidebar() {
        VBox card = new VBox(6);
        card.getStyleClass().add("panel-card");
        card.setPrefWidth(220);
        card.setMinWidth(220);
        card.setMaxWidth(220);
        card.setPadding(new Insets(14, 12, 14, 12));

        Label title = new Label("Select Logic Gate");
        title.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 0 0 4 2;");
        card.getChildren().add(title);

        GateType[] types = {
            GateType.AND, GateType.OR, GateType.NOT, GateType.NAND, GateType.NOR, GateType.XOR, GateType.XNOR
        };

        for (GateType type : types) {
            GateCardButton btn = new GateCardButton(type, type.name());
            gateButtons.add(btn);
            btn.setOnAction(e -> selectGate(type));
            card.getChildren().add(btn);
        }

        if (!gateButtons.isEmpty()) {
            gateButtons.get(0).setSelected(true);
        }

        Region bottomSpring = new Region();
        VBox.setVgrow(bottomSpring, Priority.ALWAYS);
        card.getChildren().add(bottomSpring);

        return card;
    }

    private Region buildCenter() {
        VBox card = new VBox(12);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(16, 16, 16, 16));

        Label tag = new Label("Gate Simulation");
        tag.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 11px; -fx-font-weight: bold;");

        lblTitle = new Label("AND Gate");
        lblTitle.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 22px; -fx-font-weight: bold;");

        lblSubtitle = new Label("Output is 1 only when all inputs are 1.");
        lblSubtitle.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px;");

        card.getChildren().addAll(tag, lblTitle, lblSubtitle);

        // Simulation Canvas
        canvasWidget = new CircuitCanvas();
        VBox.setVgrow(canvasWidget, Priority.ALWAYS);
        canvasWidget.setOnInputAChanged(this::setInputA);
        canvasWidget.setOnInputBChanged(this::setInputB);
        card.getChildren().add(canvasWidget);

        // Subcards: Gate Information & Boolean Expression
        HBox split = new HBox(12);

        // Left info subcard
        VBox infoCard = new VBox(6);
        infoCard.getStyleClass().add("sub-card");
        infoCard.setPadding(new Insets(12, 14, 12, 14));
        HBox.setHgrow(infoCard, Priority.ALWAYS);

        Label infoHeader = new Label("Gate Information");
        infoHeader.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");
        infoCard.getChildren().add(infoHeader);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);

        Label lblNameTag = new Label("Name:");
        lblNameTag.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblInfoName = new Label("AND Gate");
        lblInfoName.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lblInputsTag = new Label("Inputs:");
        lblInputsTag.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblInfoInputs = new Label("2");
        lblInfoInputs.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lblDescTag = new Label("Description:");
        lblDescTag.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px; -fx-font-weight: bold;");
        lblInfoDesc = new Label("Output is 1 only when all inputs are 1.");
        lblInfoDesc.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 12px;");
        lblInfoDesc.setWrapText(true);

        grid.add(lblNameTag, 0, 0);
        grid.add(lblInfoName, 1, 0);
        grid.add(lblInputsTag, 0, 1);
        grid.add(lblInfoInputs, 1, 1);
        grid.add(lblDescTag, 0, 2);
        grid.add(lblInfoDesc, 1, 2);

        infoCard.getChildren().add(grid);
        split.getChildren().add(infoCard);

        // Right expression subcard
        VBox exprCard = new VBox(6);
        exprCard.getStyleClass().add("sub-card");
        exprCard.setPadding(new Insets(12, 14, 12, 14));
        HBox.setHgrow(exprCard, Priority.ALWAYS);

        Label exprHeader = new Label("Boolean Expression");
        exprHeader.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px; -fx-font-weight: bold;");
        exprCard.getChildren().add(exprHeader);

        lblBoolExpr = new Label("Y = A · B");
        lblBoolExpr.setAlignment(Pos.CENTER);
        lblBoolExpr.setMaxWidth(Double.MAX_VALUE);
        lblBoolExpr.setStyle("-fx-background-color: #06111F; -fx-text-fill: #35C8FF; " +
                             "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                             "-fx-background-radius: 6px; -fx-padding: 10px; -fx-font-size: 17px; -fx-font-weight: bold;");
        exprCard.getChildren().add(lblBoolExpr);
        split.getChildren().add(exprCard);

        card.getChildren().add(split);
        return card;
    }

    private Region buildTruthTable() {
        VBox card = new VBox(10);
        card.getStyleClass().add("panel-card");
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.setMaxWidth(300);
        card.setPadding(new Insets(14, 14, 14, 14));

        Label title = new Label("Truth Table");
        title.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 15px; -fx-font-weight: bold;");
        card.getChildren().add(title);

        truthTable = new TableView<>();
        truthTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        truthTable.setPrefHeight(195);
        truthTable.setMaxHeight(195);

        TableColumn<TruthTableRow, String> colA = new TableColumn<>("A");
        colA.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInputAString()));
        colA.setStyle("-fx-alignment: CENTER;");

        colB = new TableColumn<>("B");
        colB.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInputBString()));
        colB.setStyle("-fx-alignment: CENTER;");

        TableColumn<TruthTableRow, String> colOut = new TableColumn<>("Output (Y)");
        colOut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOutputString()));
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

        truthTable.getColumns().addAll(List.of(colA, colB, colOut));

        // Interactive row clicking: clicking a row sets the simulator inputs to that row
        truthTable.setOnMouseClicked(e -> {
            TruthTableRow selected = truthTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                setInputA(selected.isInputA());
                if (currentGate != null && currentGate.getInputCount() > 1) {
                    setInputB(selected.isInputB());
                }
            }
        });

        card.getChildren().add(truthTable);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        Label note = new Label("Active input combination is highlighted.");
        note.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 11px; -fx-font-style: italic;");
        note.setMaxWidth(Double.MAX_VALUE);
        note.setAlignment(Pos.CENTER);
        card.getChildren().add(note);

        return card;
    }

    private Region buildQuickTest() {
        HBox card = new HBox(18);
        card.getStyleClass().add("panel-card");
        card.setPrefHeight(76);
        card.setMinHeight(76);
        card.setMaxHeight(76);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 18, 8, 18));

        VBox titleBox = new VBox(1);
        Label title = new Label("Quick Test");
        title.setStyle("-fx-text-fill: #F1F6FF; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label sub = new Label("Change inputs to see the output in real time.");
        sub.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 11px;");
        titleBox.getChildren().addAll(title, sub);
        card.getChildren().add(titleBox);

        // Input A
        HBox contA = new HBox(6);
        contA.setAlignment(Pos.CENTER_LEFT);
        Label lblA = new Label("A:");
        lblA.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold;");
        quickSwitchA = new ToggleSwitch();
        quickSwitchA.setSelected(inputA);
        quickSwitchA.selectedProperty().addListener((obs, oldVal, newVal) -> setInputA(newVal));

        quickBadgeA = new Label("1");
        quickBadgeA.setPrefSize(28, 24);
        quickBadgeA.setAlignment(Pos.CENTER);
        quickBadgeA.setStyle("-fx-background-color: #10223A; -fx-text-fill: #F1F6FF; " +
                             "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                             "-fx-background-radius: 6px; -fx-font-weight: bold;");
        contA.getChildren().addAll(lblA, quickSwitchA, quickBadgeA);
        card.getChildren().add(contA);

        // Input B
        quickContainerB = new HBox(6);
        quickContainerB.setAlignment(Pos.CENTER_LEFT);
        Label lblB = new Label("B:");
        lblB.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold;");
        quickSwitchB = new ToggleSwitch();
        quickSwitchB.setSelected(inputB);
        quickSwitchB.selectedProperty().addListener((obs, oldVal, newVal) -> setInputB(newVal));

        quickBadgeB = new Label("0");
        quickBadgeB.setPrefSize(28, 24);
        quickBadgeB.setAlignment(Pos.CENTER);
        quickBadgeB.setStyle("-fx-background-color: #10223A; -fx-text-fill: #F1F6FF; " +
                             "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                             "-fx-background-radius: 6px; -fx-font-weight: bold;");
        quickContainerB.getChildren().addAll(lblB, quickSwitchB, quickBadgeB);
        card.getChildren().add(quickContainerB);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        // Output Display
        HBox outBox = new HBox(8);
        outBox.setAlignment(Pos.CENTER_LEFT);
        Label lblOut = new Label("Output:");
        lblOut.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold;");

        quickOutputVal = new Label("0");
        quickOutputVal.setPrefSize(30, 30);
        quickOutputVal.setAlignment(Pos.CENTER);
        quickOutputVal.setStyle("-fx-background-color: #06111F; -fx-text-fill: #A8C4EA; " +
                                "-fx-border-color: #203956; -fx-border-width: 2px; -fx-border-radius: 15px; " +
                                "-fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 15px;");

        quickOutputPill = new Label("LOW");
        quickOutputPill.setPrefSize(50, 22);
        quickOutputPill.setAlignment(Pos.CENTER);
        quickOutputPill.setStyle("-fx-background-color: #10223A; -fx-text-fill: #A8C4EA; " +
                                 "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                 "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");

        outBox.getChildren().addAll(lblOut, quickOutputVal, quickOutputPill);
        card.getChildren().add(outBox);

        return card;
    }

    private Region buildStatusBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 4, 2, 4));

        Circle dot = new Circle(4.5, Color.web("#19E69A"));
        bar.getChildren().add(dot);

        statusMsg = new Label("Ready  |  AND gate selected");
        statusMsg.setStyle("-fx-text-fill: #A8C4EA; -fx-font-size: 12px;");
        bar.getChildren().add(statusMsg);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().add(spacer);

        statusRightMsg = new Label("ℹ Select a gate, change inputs, and explore the truth table.");
        statusRightMsg.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px;");
        bar.getChildren().add(statusRightMsg);

        return bar;
    }

    // ========================================================================
    // State Synchronization & Handlers
    // ========================================================================

    public void selectGate(GateType type) {
        this.currentGateType = type;
        this.currentGate = GateFactory.createGate(type);

        for (GateCardButton btn : gateButtons) {
            btn.setSelected(btn.getGateType() == type);
        }

        boolean isNot = (type == GateType.NOT);
        quickContainerB.setVisible(!isNot);
        quickContainerB.setManaged(!isNot);
        colB.setVisible(!isNot);

        updateSimulation();
    }

    public void setInputA(boolean val) {
        if (isUpdatingState) return;
        isUpdatingState = true;
        try {
            this.inputA = val;
            quickSwitchA.setSelected(val);
            quickBadgeA.setText(val ? "1" : "0");
            canvasWidget.syncInputA(val);
            updateSimulation();
        } finally {
            isUpdatingState = false;
        }
    }

    public void setInputB(boolean val) {
        if (isUpdatingState) return;
        isUpdatingState = true;
        try {
            this.inputB = val;
            quickSwitchB.setSelected(val);
            quickBadgeB.setText(val ? "1" : "0");
            canvasWidget.syncInputB(val);
            updateSimulation();
        } finally {
            isUpdatingState = false;
        }
    }

    private void updateSimulation() {
        if (currentGate == null) return;

        // Polymorphic evaluation call
        currentOutput = currentGate.evaluate(inputA, inputB);

        // Update labels
        lblTitle.setText(currentGate.getName());
        lblSubtitle.setText(currentGate.getDescription());
        lblInfoName.setText(currentGate.getName());
        lblInfoInputs.setText(String.valueOf(currentGate.getInputCount()));
        lblInfoDesc.setText(currentGate.getDescription());
        lblBoolExpr.setText(currentGate.getExpression());

        // Update simulation canvas
        canvasWidget.updateState(currentGateType, inputA, inputB, currentOutput, currentGate.getInputCount());

        // Update quick test bar
        quickOutputVal.setText(currentOutput ? "1" : "0");
        if (currentOutput) {
            quickOutputVal.setStyle("-fx-background-color: #06111F; -fx-text-fill: #19E69A; " +
                                    "-fx-border-color: #19E69A; -fx-border-width: 2px; -fx-border-radius: 15px; " +
                                    "-fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 15px;");
            quickOutputPill.setText("HIGH");
            quickOutputPill.setStyle("-fx-background-color: #0B2C20; -fx-text-fill: #19E69A; " +
                                     "-fx-border-color: #19E69A; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                     "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");
        } else {
            quickOutputVal.setStyle("-fx-background-color: #06111F; -fx-text-fill: #A8C4EA; " +
                                    "-fx-border-color: #203956; -fx-border-width: 2px; -fx-border-radius: 15px; " +
                                    "-fx-background-radius: 15px; -fx-font-weight: bold; -fx-font-size: 15px;");
            quickOutputPill.setText("LOW");
            quickOutputPill.setStyle("-fx-background-color: #10223A; -fx-text-fill: #A8C4EA; " +
                                     "-fx-border-color: #203956; -fx-border-width: 1px; -fx-border-radius: 6px; " +
                                     "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-font-size: 10px;");
        }

        // Update truth table
        updateTruthTable();

        statusMsg.setText("Ready  |  " + currentGate.getName() + " selected");
    }

    private void updateTruthTable() {
        if (currentGate == null) return;

        List<TruthTableRow> rows = currentGate.generateTruthTable();
        truthTable.setItems(FXCollections.observableArrayList(rows));

        boolean isNot = (currentGate.getInputCount() == 1);
        int activeIndex = -1;

        for (int r = 0; r < rows.size(); ++r) {
            TruthTableRow row = rows.get(r);
            boolean match = isNot ? (row.isInputA() == inputA) : (row.isInputA() == inputA && row.isInputB() == inputB);
            if (match) {
                activeIndex = r;
                break;
            }
        }

        if (activeIndex >= 0) {
            truthTable.getSelectionModel().select(activeIndex);
        }
    }

    private void saveState() {
        SimulatorState state = new SimulatorState(currentGateType, inputA, inputB);
        StorageResult res = GateStorage.saveSimulatorState(state);

        if (res.isSuccess()) {
            statusRightMsg.setText("✔ " + res.getMessage());
            statusRightMsg.setStyle("-fx-text-fill: #19E69A; -fx-font-size: 12px; -fx-font-weight: bold;");
            showAlert(Alert.AlertType.INFORMATION, "Save Successful", res.getMessage());
        } else {
            showAlert(Alert.AlertType.ERROR, "Save Failed", res.getMessage());
        }

        PauseTransition resetStatus = new PauseTransition(Duration.seconds(5));
        resetStatus.setOnFinished(e -> {
            statusRightMsg.setText("ℹ Select a gate, change inputs, and explore the truth table.");
            statusRightMsg.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px;");
        });
        resetStatus.play();
    }

    private void loadState() {
        StorageResult res = GateStorage.loadSimulatorState();
        if (res.isSuccess()) {
            SimulatorState loaded = res.getState();
            setInputA(loaded.isInputA());
            setInputB(loaded.isInputB());
            selectGate(loaded.getGateType());

            statusRightMsg.setText("✔ " + res.getMessage());
            statusRightMsg.setStyle("-fx-text-fill: #19E69A; -fx-font-size: 12px; -fx-font-weight: bold;");
            showAlert(Alert.AlertType.INFORMATION, "Load Successful", res.getMessage());
        } else {
            showAlert(Alert.AlertType.WARNING, "Load Failed", res.getMessage());
        }

        PauseTransition resetStatus = new PauseTransition(Duration.seconds(5));
        resetStatus.setOnFinished(e -> {
            statusRightMsg.setText("ℹ Select a gate, change inputs, and explore the truth table.");
            statusRightMsg.setStyle("-fx-text-fill: #35C8FF; -fx-font-size: 12px;");
        });
        resetStatus.play();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    // ========================================================================
    // Helper Graphics: IC Chip Logo
    // ========================================================================
    private void drawIcLogo(GraphicsContext gc) {
        gc.setFill(Color.web("#087BFF"));
        gc.setStroke(Color.web("#35C8FF"));
        gc.setLineWidth(1.5);
        gc.fillRoundRect(7, 7, 22, 22, 6, 6);
        gc.strokeRoundRect(7, 7, 22, 22, 6, 6);

        gc.setFill(Color.web("#06111F"));
        gc.fillRoundRect(12, 12, 12, 12, 4, 4);

        gc.setStroke(Color.web("#35C8FF"));
        gc.setLineWidth(1.8);
        // Pins left & right
        gc.strokeLine(3, 13, 7, 13);
        gc.strokeLine(3, 18, 7, 18);
        gc.strokeLine(3, 23, 7, 23);
        gc.strokeLine(29, 13, 33, 13);
        gc.strokeLine(29, 18, 33, 18);
        gc.strokeLine(29, 23, 33, 23);
        // Pins top & bottom
        gc.strokeLine(13, 3, 13, 7);
        gc.strokeLine(23, 3, 23, 7);
        gc.strokeLine(13, 29, 13, 33);
        gc.strokeLine(23, 29, 23, 33);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // ========================================================================
    // Custom Component: ToggleSwitch
    // ========================================================================
    public static class ToggleSwitch extends Region {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final Rectangle track;
        private final Circle thumb;

        public ToggleSwitch() {
            setPrefSize(54, 28);
            setMinSize(54, 28);
            setMaxSize(54, 28);
            setCursor(Cursor.HAND);

            track = new Rectangle(54, 28);
            track.setArcWidth(28);
            track.setArcHeight(28);

            thumb = new Circle(11);
            thumb.setFill(Color.web("#F1F6FF"));
            thumb.setCenterY(14);

            updateVisualState();

            getChildren().addAll(track, thumb);

            setOnMouseClicked(e -> setSelected(!isSelected()));

            selected.addListener((obs, oldVal, newVal) -> updateVisualState());
        }

        private void updateVisualState() {
            if (selected.get()) {
                track.setFill(Color.web("#087BFF"));
                track.setStroke(Color.web("#35C8FF"));
                track.setStrokeWidth(1.2);
                thumb.setCenterX(40);
            } else {
                track.setFill(Color.web("#10223A"));
                track.setStroke(Color.web("#203956"));
                track.setStrokeWidth(1.2);
                thumb.setCenterX(14);
            }
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean val) {
            selected.set(val);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }
    }

    // ========================================================================
    // Custom Component: GateCardButton (Sidebar gate card)
    // ========================================================================
    public static class GateCardButton extends Button {
        private final GateType gateType;
        private boolean isCurrentSelected = false;
        private final Canvas iconCanvas;
        private final Label nameLabel;

        public GateCardButton(GateType type, String name) {
            this.gateType = type;
            setPrefHeight(52);
            setMinHeight(52);
            setMaxHeight(52);
            setMaxWidth(Double.MAX_VALUE);
            setCursor(Cursor.HAND);

            HBox content = new HBox(14);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(0, 12, 0, 12));

            iconCanvas = new Canvas(38, 26);
            nameLabel = new Label(name);
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

            content.getChildren().addAll(iconCanvas, nameLabel);
            setGraphic(content);

            updateCardStyle(false, false);

            setOnMouseEntered(e -> {
                if (!isCurrentSelected) updateCardStyle(false, true);
            });
            setOnMouseExited(e -> {
                if (!isCurrentSelected) updateCardStyle(false, false);
            });
        }

        public GateType getGateType() {
            return gateType;
        }

        public void setSelected(boolean selected) {
            this.isCurrentSelected = selected;
            updateCardStyle(selected, false);
        }

        private void updateCardStyle(boolean selected, boolean hovered) {
            if (selected) {
                setStyle("-fx-background-color: #087BFF; -fx-border-color: #35C8FF; " +
                         "-fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                nameLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
                drawIcon(Color.web("#FFFFFF"), Color.color(1, 1, 1, 0.15));
            } else if (hovered) {
                setStyle("-fx-background-color: #10223A; -fx-border-color: #35C8FF; " +
                         "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                nameLabel.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold;");
                drawIcon(Color.web("#35C8FF"), Color.web("#0B192B"));
            } else {
                setStyle("-fx-background-color: #0B192B; -fx-border-color: #203956; " +
                         "-fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
                nameLabel.setStyle("-fx-text-fill: #F1F6FF; -fx-font-weight: bold;");
                drawIcon(Color.web("#A8C4EA"), Color.web("#0B192B"));
            }
        }

        private void drawIcon(Color stroke, Color fill) {
            GraphicsContext gc = iconCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, 38, 26);
            GateSymbolRenderer.draw(gc, gateType, 2, 2, 34, 22, stroke, fill, 1.8);
        }
    }

    // ========================================================================
    // Custom Component: CircuitCanvas (Interactive Simulation with Wires & LED)
    // ========================================================================
    public static class CircuitCanvas extends Region {
        private GateType gateType = GateType.AND;
        private boolean inputA = true;
        private boolean inputB = false;
        private boolean output = false;
        private int inputCount = 2;

        private final Canvas canvas;
        private final HBox rowA;
        private final HBox rowB;
        private final ToggleSwitch switchA;
        private final ToggleSwitch switchB;
        private final Label badgeA;
        private final Label badgeB;

        private Consumer<Boolean> onInputAChanged;
        private Consumer<Boolean> onInputBChanged;

        public CircuitCanvas() {
            setPrefHeight(270);
            setMinHeight(240);

            canvas = new Canvas();

            // Row A
            rowA = new HBox(8);
            rowA.setAlignment(Pos.CENTER_LEFT);
            Label lblA = new Label("Input A");
            lblA.setPrefWidth(56);
            lblA.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #F1F6FF;");
            switchA = new ToggleSwitch();
            switchA.setSelected(inputA);
            badgeA = new Label("1");
            badgeA.setPrefSize(30, 28);
            badgeA.setAlignment(Pos.CENTER);
            badgeA.setStyle("-fx-background-color: #10223A; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                            "-fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px;");
            rowA.getChildren().addAll(lblA, switchA, badgeA);

            // Row B
            rowB = new HBox(8);
            rowB.setAlignment(Pos.CENTER_LEFT);
            Label lblB = new Label("Input B");
            lblB.setPrefWidth(56);
            lblB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #F1F6FF;");
            switchB = new ToggleSwitch();
            switchB.setSelected(inputB);
            badgeB = new Label("0");
            badgeB.setPrefSize(30, 28);
            badgeB.setAlignment(Pos.CENTER);
            badgeB.setStyle("-fx-background-color: #10223A; -fx-text-fill: #F1F6FF; -fx-border-color: #203956; " +
                            "-fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; " +
                            "-fx-font-weight: bold; -fx-font-size: 13px;");
            rowB.getChildren().addAll(lblB, switchB, badgeB);

            getChildren().addAll(canvas, rowA, rowB);

            switchA.selectedProperty().addListener((obs, oldVal, newVal) -> {
                badgeA.setText(newVal ? "1" : "0");
                if (onInputAChanged != null) onInputAChanged.accept(newVal);
            });

            switchB.selectedProperty().addListener((obs, oldVal, newVal) -> {
                badgeB.setText(newVal ? "1" : "0");
                if (onInputBChanged != null) onInputBChanged.accept(newVal);
            });

            widthProperty().addListener(e -> requestLayout());
            heightProperty().addListener(e -> requestLayout());
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) return;

            canvas.setWidth(w);
            canvas.setHeight(h);
            canvas.relocate(0, 0);

            double cy = h / 2.0;
            double leftX = 30.0;
            double rowW = 156.0;
            double rowH = 28.0;

            if (inputCount == 1) {
                // NOT Gate: Center row A exactly at cy
                rowA.setVisible(true);
                rowA.setManaged(true);
                rowB.setVisible(false);
                rowB.setManaged(false);

                rowA.resizeRelocate(leftX, cy - rowH / 2.0, rowW, rowH);
            } else {
                // 2-Input Gates: Center row A at cy - 38 and row B at cy + 38
                rowA.setVisible(true);
                rowA.setManaged(true);
                rowB.setVisible(true);
                rowB.setManaged(true);

                rowA.resizeRelocate(leftX, cy - 38.0 - rowH / 2.0, rowW, rowH);
                rowB.resizeRelocate(leftX, cy + 38.0 - rowH / 2.0, rowW, rowH);
            }

            redraw();
        }

        public void setOnInputAChanged(Consumer<Boolean> handler) {
            this.onInputAChanged = handler;
        }

        public void setOnInputBChanged(Consumer<Boolean> handler) {
            this.onInputBChanged = handler;
        }

        public void syncInputA(boolean val) {
            inputA = val;
            switchA.setSelected(val);
            badgeA.setText(val ? "1" : "0");
            redraw();
        }

        public void syncInputB(boolean val) {
            inputB = val;
            switchB.setSelected(val);
            badgeB.setText(val ? "1" : "0");
            redraw();
        }

        public void updateState(GateType type, boolean inA, boolean inB, boolean out, int count) {
            this.gateType = type;
            this.inputA = inA;
            this.inputB = inB;
            this.output = out;
            this.inputCount = count;

            switchA.setSelected(inA);
            badgeA.setText(inA ? "1" : "0");

            if (count == 1) {
                rowB.setVisible(false);
                rowB.setManaged(false);
            } else {
                rowB.setVisible(true);
                rowB.setManaged(true);
                switchB.setSelected(inB);
                badgeB.setText(inB ? "1" : "0");
            }

            requestLayout();
            redraw();
        }

        private void redraw() {
            double w = getWidth();
            double h = getHeight();
            if (w <= 0 || h <= 0) return;

            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, w, h);

            // Container background
            gc.setFill(Color.web("#0B192B"));
            gc.setStroke(Color.web("#203956"));
            gc.setLineWidth(1.0);
            gc.fillRoundRect(1, 1, w - 2, h - 2, 12, 12);
            gc.strokeRoundRect(1, 1, w - 2, h - 2, 12, 12);

            double cy = h / 2.0;
            double gateW = 110.0;
            double gateH = 74.0;
            double gateX = Math.max(260.0, w * 0.44);
            double gateY = cy - gateH / 2.0;

            // Wire drawing helper with glowing aesthetics
            Consumer<WireCoord> drawWire = coord -> {
                boolean isHigh = coord.isHigh;
                if (isHigh) {
                    gc.setStroke(Color.color(0.208, 0.784, 1.0, 0.25));
                    gc.setLineWidth(5.5);
                    gc.strokeLine(coord.x1, coord.y1, coord.x2, coord.y2);
                }
                gc.setStroke(isHigh ? Color.web("#35C8FF") : Color.web("#223C5E"));
                gc.setLineWidth(2.6);
                gc.strokeLine(coord.x1, coord.y1, coord.x2, coord.y2);
            };

            // Wire starting point is flush with the right edge of Badge A & B (leftX 30 + rowW 156 = 186)
            double wireStartX = 186.0;

            // Compute gate entry X coordinate (XOR/XNOR pins pass through the back arc into the body)
            double gateEntryX = gateX;
            if (gateType == GateType.XOR || gateType == GateType.XNOR) {
                double bw = (gateType == GateType.XNOR) ? (gateW - 8.0) : gateW;
                gateEntryX = gateX + bw * 0.18;
            }

            if (inputCount == 1) {
                // NOT Gate: Single straight wire directly from badge A to gate input
                drawWire.accept(new WireCoord(wireStartX, cy, gateEntryX, cy, inputA));
            } else {
                double yA = cy - 38.0;
                double yB = cy + 38.0;
                double termYA = gateY + gateH * 0.25;
                double termYB = gateY + gateH * 0.75;
                double midX = (wireStartX + gateX) / 2.0;

                // Wire A (from badge A to upper terminal)
                drawWire.accept(new WireCoord(wireStartX, yA, midX, yA, inputA));
                drawWire.accept(new WireCoord(midX, yA, midX, termYA, inputA));
                drawWire.accept(new WireCoord(midX, termYA, gateEntryX, termYA, inputA));

                // Wire B (from badge B to lower terminal)
                drawWire.accept(new WireCoord(wireStartX, yB, midX, yB, inputB));
                drawWire.accept(new WireCoord(midX, yB, midX, termYB, inputB));
                drawWire.accept(new WireCoord(midX, termYB, gateEntryX, termYB, inputB));
            }

            // Gate Symbol centered
            Color gateStroke = output ? Color.web("#35C8FF") : Color.web("#087BFF");
            GateSymbolRenderer.draw(gc, gateType, gateX, gateY, gateW, gateH, gateStroke, Color.web("#10223A"), 2.6);

            // Gate Name centered inside gate symbol
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#F1F6FF"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);

            double labelOffsetX = (gateType == GateType.NOT || gateType == GateType.NAND ||
                                   gateType == GateType.NOR || gateType == GateType.XNOR) ? -5.0 : 0.0;
            gc.fillText(gateType.name(), gateX + (gateW / 2.0) + labelOffsetX, cy);

            // Output Wire
            double outStartX = gateX + gateW;
            double ledX = Math.max(outStartX + 90.0, w - 85.0);
            double ledRadius = 22.0;

            if (output) {
                gc.setStroke(Color.color(0.098, 0.902, 0.604, 0.28));
                gc.setLineWidth(5.5);
                gc.strokeLine(outStartX, cy, ledX - ledRadius, cy);
            }
            gc.setStroke(output ? Color.web("#19E69A") : Color.web("#223C5E"));
            gc.setLineWidth(2.8);
            gc.strokeLine(outStartX, cy, ledX - ledRadius, cy);

            // Output LED Ring Label
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setFill(Color.web("#A8C4EA"));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText("Output Y", ledX, cy - 35.0);

            // Output LED Glow & Body
            if (output) {
                RadialGradient glow = new RadialGradient(
                    0, 0, ledX, cy, 36.0, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.color(0.098, 0.902, 0.604, 0.45)),
                    new Stop(0.6, Color.color(0.098, 0.902, 0.604, 0.15)),
                    new Stop(1.0, Color.color(0.098, 0.902, 0.604, 0.0))
                );
                gc.setFill(glow);
                gc.fillOval(ledX - 36.0, cy - 36.0, 72.0, 72.0);
            }

            gc.setFill(Color.web("#06111F"));
            gc.setStroke(output ? Color.web("#19E69A") : Color.web("#203956"));
            gc.setLineWidth(3.0);
            gc.fillOval(ledX - ledRadius, cy - ledRadius, ledRadius * 2.0, ledRadius * 2.0);
            gc.strokeOval(ledX - ledRadius, cy - ledRadius, ledRadius * 2.0, ledRadius * 2.0);

            // Numeric value inside LED
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
            gc.setFill(output ? Color.web("#19E69A") : Color.web("#F1F6FF"));
            gc.fillText(output ? "1" : "0", ledX, cy);

            // HIGH / LOW Status Pill
            double pillW = 60.0;
            double pillH = 22.0;
            double pillX = ledX - pillW / 2.0;
            double pillY = cy + 32.0;

            if (output) {
                gc.setFill(Color.web("#0B2C20"));
                gc.setStroke(Color.web("#19E69A"));
                gc.setLineWidth(1.2);
                gc.fillRoundRect(pillX, pillY, pillW, pillH, 11, 11);
                gc.strokeRoundRect(pillX, pillY, pillW, pillH, 11, 11);

                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.setFill(Color.web("#19E69A"));
                gc.fillText("HIGH", ledX, pillY + pillH / 2.0);
            } else {
                gc.setFill(Color.web("#10223A"));
                gc.setStroke(Color.web("#203956"));
                gc.setLineWidth(1.2);
                gc.fillRoundRect(pillX, pillY, pillW, pillH, 11, 11);
                gc.strokeRoundRect(pillX, pillY, pillW, pillH, 11, 11);

                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                gc.setFill(Color.web("#A8C4EA"));
                gc.fillText("LOW", ledX, pillY + pillH / 2.0);
            }
        }

        private static class WireCoord {
            double x1, y1, x2, y2;
            boolean isHigh;

            WireCoord(double x1, double y1, double x2, double y2, boolean isHigh) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.isHigh = isHigh;
            }
        }
    }

    // ========================================================================
    // Logic Gate Vector Drawing (ANSI / IEEE standard symbols)
    // ========================================================================
    public static class GateSymbolRenderer {
        public static void draw(GraphicsContext gc, GateType type, double x, double y,
                                double w, double h, Color stroke, Color fill, double lineWidth) {
            gc.save();
            gc.setStroke(stroke);
            gc.setFill(fill);
            gc.setLineWidth(lineWidth);

            double bubbleRadius = 4.0;
            double bubbleDiameter = bubbleRadius * 2.0;
            double cy = y + h / 2.0;

            switch (type) {
                case AND -> {
                    double midX = x + w * 0.5;
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.lineTo(midX, y);
                    gc.bezierCurveTo(x + w, y, x + w, y + h, midX, y + h);
                    gc.lineTo(x, y + h);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();
                }
                case OR -> {
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.quadraticCurveTo(x + w * 0.28, cy, x, y + h);
                    gc.quadraticCurveTo(x + w * 0.6, y + h, x + w, cy);
                    gc.quadraticCurveTo(x + w * 0.6, y, x, y);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();
                }
                case NOT -> {
                    double tipX = x + w - bubbleDiameter;
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.lineTo(x, y + h);
                    gc.lineTo(tipX, cy);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();

                    gc.fillOval(tipX, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                    gc.strokeOval(tipX, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                }
                case NAND -> {
                    double bw = w - bubbleDiameter;
                    double midX = x + bw * 0.5;
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.lineTo(midX, y);
                    gc.bezierCurveTo(x + bw, y, x + bw, y + h, midX, y + h);
                    gc.lineTo(x, y + h);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();

                    gc.fillOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                    gc.strokeOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                }
                case NOR -> {
                    double bw = w - bubbleDiameter;
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.quadraticCurveTo(x + bw * 0.28, cy, x, y + h);
                    gc.quadraticCurveTo(x + bw * 0.6, y + h, x + bw, cy);
                    gc.quadraticCurveTo(x + bw * 0.6, y, x, y);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();

                    gc.fillOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                    gc.strokeOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                }
                case XOR -> {
                    // Back arc
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.quadraticCurveTo(x + w * 0.28, cy, x, y + h);
                    gc.stroke();

                    // Main body with offset
                    double offset = w * 0.18;
                    double bx = x + offset;
                    double bw = w - offset;

                    gc.beginPath();
                    gc.moveTo(bx, y);
                    gc.quadraticCurveTo(bx + bw * 0.28, cy, bx, y + h);
                    gc.quadraticCurveTo(bx + bw * 0.6, y + h, x + w, cy);
                    gc.quadraticCurveTo(bx + bw * 0.6, y, bx, y);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();
                }
                case XNOR -> {
                    double bw = w - bubbleDiameter;

                    // Back arc
                    gc.beginPath();
                    gc.moveTo(x, y);
                    gc.quadraticCurveTo(x + bw * 0.28, cy, x, y + h);
                    gc.stroke();

                    // Main body with offset
                    double offset = bw * 0.18;
                    double bx = x + offset;
                    double bodyW = bw - offset;

                    gc.beginPath();
                    gc.moveTo(bx, y);
                    gc.quadraticCurveTo(bx + bodyW * 0.28, cy, bx, y + h);
                    gc.quadraticCurveTo(bx + bodyW * 0.6, y + h, x + bw, cy);
                    gc.quadraticCurveTo(bx + bodyW * 0.6, y, bx, y);
                    gc.closePath();
                    gc.fill();
                    gc.stroke();

                    gc.fillOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                    gc.strokeOval(x + bw, cy - bubbleRadius, bubbleDiameter, bubbleDiameter);
                }
            }

            gc.restore();
        }
    }
}

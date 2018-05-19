/**
 *
 * @author David Doo
 */
package ui;

import actions.AppActions;
import algorithms.base.Algorithm;
import data.DataSet;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import vilij.propertymanager.PropertyManager;

import static java.io.File.separator;
import java.io.IOException;
import static java.lang.Integer.MAX_VALUE;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import static settings.AppPropertyTypes.*;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

//import java.lang.reflect.
/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private String scrnshoticonPath;
    private String settingsiconPath;

    private ScatterChart<Number, Number> scatterChart;               // the chart where data will be displayed
    private LineChart<Number, Number> lineChart;
//    private Button displayButton;  
    private Button toggleButton;

    private TextArea textArea;       // text area for new data input

    private boolean hasNewText;     // whether or not the text area has any new data since last display

//    private String storedData;
    private VBox vPane;
    private StackPane charts;
    private Text fileInfo;

    private String dataSource;
    private int numLabels;
    private Set<String> labels;
    private final String NEW_LINE = "\n";
    private final String TAB = "\t";
    private VBox algorithmList;
    private VBox algorithmPane;
    private VBox algorithmTable;
    private ArrayList<RadioButton> algorithmRadioButtons;
    private Button runAlgorithm;
    private Stage algorithmConfigWindow;
    private ConfigState currentSettings;
    private ArrayList<Button> configButtons;
    private ArrayList<ConfigState> cachedSettings;
    private TextField clustersField;
    private TextField iterationsField;
    private TextField intervalsField;
    private CheckBox runOption;
    private RadioButton currentAlgorithmTypeSelection;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Label iterationLabel;

    private Button currentSettingsButton;
    private Button returnButton;
    private Pane space;
    private ToggleGroup algorithmToggle;
    private Set<String> algorithmTypes;
    private ToggleGroup listOfAlgorithmTypes;
    private Image settingsImage;

    /**
     *
     * @param primaryStage
     * @param applicationTemplate
     */
    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        algorithmTypes = new HashSet<>();
    }

    /**
     *
     * @param applicationTemplate
     */
    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnshoticonPath = String.join(separator, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        settingsiconPath = String.join(separator, iconsPath, manager.getPropertyValue(SETTINGS_ICON.name()));
    }

    /**
     *
     * @param applicationTemplate
     */
    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        PropertyManager manager = applicationTemplate.manager;
        super.setToolBar(applicationTemplate);
        scrnshotButton = setToolbarButton(scrnshoticonPath, manager.getPropertyValue(SCREENSHOT_TOOLTIP.name()), true);
        super.toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
            vPane.getChildren().remove(toggleButton);
            vPane.getChildren().remove(fileInfo);

            clearCachedSettings();
            algorithmPane.getChildren().remove(runAlgorithm);
            vPane.getChildren().remove(algorithmPane);
            vPane.setVisible(false);
            applicationTemplate.getActionComponent().handleNewRequest();

            if (!((AppActions) applicationTemplate.getActionComponent()).getFlag()) {
//                toggleButton.setVisible(true);
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));

                vPane.getChildren().add(toggleButton);
                vPane.setVisible(true);
            }

        });

        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> {

            applicationTemplate.getActionComponent().handleLoadRequest();

            if (!((AppActions) applicationTemplate.getActionComponent()).getFlag()) {
                vPane.setVisible(false);
                algorithmPane.getChildren().remove(runAlgorithm);
                algorithmPane.getChildren().remove(iterationLabel);
                runAlgorithm.setVisible(false);
                vPane.getChildren().remove(fileInfo);
                algorithmList.getChildren().clear();
                resetToggleOptions();
                clearCachedSettings();
                vPane.getChildren().remove(algorithmPane);

                vPane.getChildren().remove(toggleButton);
                getTextArea().setDisable(true);
                getSaveButton().setDisable(true);

                setFileMetaData();
                vPane.getChildren().add(fileInfo);
                if (!algorithmPane.getChildren().contains(algorithmTable)) {
                    algorithmPane.getChildren().add(algorithmTable);
                }

                algorithmPane.getChildren().add(runAlgorithm);
                algorithmPane.getChildren().add(iterationLabel);
                runAlgorithm.setId("active");
                runAlgorithm.setVisible(true);
                runAlgorithm.setDisable(true);
                vPane.getChildren().add(algorithmPane);

                vPane.setVisible(true);
            }
            return;
        });

        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();

    }

    @Override
    public void clear() {
//        storedData = "";
        scatterChart.getData().clear();
        lineChart.getData().clear();
        fileInfo.setText("");
        numLabels = 0;
        currentSettings = null;
        runAlgorithm.setDisable(true);
        runAlgorithm.setStyle(null);
        runAlgorithm.setText("Run");

    }

    private void layout() {
        HBox pane = new HBox();

        pane.prefWidthProperty().bind(appPane.widthProperty());
        pane.setPadding(new Insets(10, 10, 10, 10));

        vPane = new VBox(10);
        vPane.setSpacing(10);
        vPane.setPrefHeight(applicationTemplate.getUIComponent().getPrimaryScene().getHeight());
        Label boxTitle = new Label(applicationTemplate.manager.getPropertyValue(TEXTBOX_TITLE.name()));
        boxTitle.setFont(Font.font(applicationTemplate.manager.getPropertyValue(FONT.name()), 18));

        textArea = new TextArea();
        textArea.setPrefWidth(300);
        textArea.setMinHeight(250);
//        displayButton = new Button(applicationTemplate.manager.getPropertyValue(DISPLAY_NAME.name()));
//        tickBox = new CheckBox(applicationTemplate.manager.getPropertyValue(READ_ONLY.name()));
        toggleButton = new Button(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));

//        Region region = new Region();
//        HBox.setHgrow(region, Priority.ALWAYS);
        vPane.getChildren().add(toggleButton);

        fileInfo = new Text();
        fileInfo.setWrappingWidth(300);

        algorithmPane = new VBox(10);
        algorithmTable = new VBox(10);
        iterationLabel = new Label("Iteration number: ");
        Label listHeader = new Label(applicationTemplate.manager.getPropertyValue(ALGORITHM_TYPES_TITLE.name()));
        listHeader.setId(applicationTemplate.manager.getPropertyValue(ALGORITHM_LIST_ID.name()));

        algorithmTable.getChildren().addAll(listHeader);

        pane.setAlignment(Pos.BOTTOM_CENTER);

        space = new Pane();
        space.setPrefHeight(MAX_VALUE);
        vPane.getChildren().addAll(boxTitle, textArea);

        vPane.setVgrow(space, Priority.ALWAYS);
        vPane.setVisible(false);

        algorithmList = new VBox(10);

        algorithmRadioButtons = new ArrayList<>();
        cachedSettings = new ArrayList<>();
        algorithmToggle = new ToggleGroup();

        settingsImage = new Image(getClass().getResourceAsStream(settingsiconPath));
        returnButton = new Button("Back");
        algorithmPane.getChildren().addAll(space, algorithmTable, algorithmList, iterationLabel);
        vPane.getChildren().addAll(algorithmPane);

        try {
            List<String> names = new ArrayList<>();
            String pkg = "algorithms";
            URI resources;
            try {
                resources = this.getClass().getResource("/" + pkg).toURI();
                File[] files = new File(resources).listFiles();

                for (File f : files) {
                    if (f.isFile()) {
                        String filename = pkg + "." + f.getName().replaceFirst("[.][^.]+$", "");
                        names.add(filename);
                    }
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (String n : names) {
                try {
                    Class<?> klass = Class.forName(n);
                    Algorithm o = (Algorithm) klass.newInstance();
                    algorithmTypes.add(o.getClass().getSuperclass().getSimpleName());

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(AppUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            listOfAlgorithmTypes = new ToggleGroup();
            for (String type : algorithmTypes) {
                RadioButton algoType = new RadioButton(type);
                algoType.setToggleGroup(listOfAlgorithmTypes);
                algorithmTable.getChildren().add(algoType);

                algoType.disableProperty().addListener(listener -> {

                    if (type.equals("Classifier") && ((AppData) applicationTemplate.getDataComponent()).getProcessor().getData().getLabels().size() != 2) {
                        algoType.setDisable(true);
                    }

                });
                algoType.setOnAction(e -> {

                    algorithmPane.getChildren().remove(algorithmTable);

                    loadAlgorithms(type, names);
                });

            }
            //clear
//                    algorithmTypes.getSelectionModel().selectedItemProperty().addListener(listener -> {
//            if (algorithmTypes.getSelectionModel().getSelectedItem() == null) {
//                return;
//            }
//            else{
//                algorithmList.getChildren().addAll(returnButton, algorithmContainer);
//            }
//            });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
        }

        runAlgorithm = new Button(applicationTemplate.manager.getPropertyValue(RUN_BUTTON_NAME.name()));
        runAlgorithm.setDisable(true);
        runAlgorithm.setId("active");
        this.getPrimaryScene().getStylesheets().add(getClass().getClassLoader().getResource(applicationTemplate.manager.getPropertyValue(CSS_PATH.name())).toExternalForm());

        xAxis = new NumberAxis();
        yAxis = new NumberAxis();

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_TITLE.name()));
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);
        lineChart.setAnimated(false);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.getXAxis().setVisible(false);
        lineChart.getXAxis().setAutoRanging(false);
        lineChart.getYAxis().setVisible(false);
        lineChart.getYAxis().setAutoRanging(false);

//        lineChart.setVisible(false);
//        lineChart.getStylesheets().addAll(getClass().getResource("chart.css").toExternalForm());
        lineChart.setId("line");
        
        scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(applicationTemplate.manager.getPropertyValue(CHART_TITLE.name()));
        scatterChart.setMaxHeight((2 * appPane.getHeight()) / 3);
//        scatterChart.setLegendVisible(false);
        scatterChart.setAnimated(false);
        scatterChart.getXAxis().setVisible(true);
        scatterChart.getYAxis().setVisible(true);
        scatterChart.setId("scatter");
        
        lineChart.setMaxHeight(scatterChart.getMaxHeight());
        
        final RowConstraints row = new RowConstraints();
        row.setPrefHeight(scatterChart.getMaxHeight());
        final ColumnConstraints textColumn = new ColumnConstraints();
        final ColumnConstraints chartColumn = new ColumnConstraints();
        chartColumn.setHgrow(Priority.ALWAYS);
//        pane.getRowConstraints().add(row);
//        pane.getColumnConstraints().addAll(textColumn, chartColumn);

//        scatterChart.prefWidthProperty().bind(lineChart.widthProperty());
//        lineChart.minWidthProperty().bind(scatterChart.widthProperty());
//        lineChart.maxWidthProperty().bind(scatterChart.widthProperty());
        pane.getChildren().add(vPane);
//        vPane.prefWidthProperty().bind(pane.widthProperty());
        charts = new StackPane();
        charts.getChildren().addAll(lineChart, scatterChart);
        charts.setAlignment(Pos.TOP_RIGHT);
        pane.getChildren().add(charts);
        charts.prefWidthProperty().bind(pane.widthProperty().subtract(280));
        
        appPane.getChildren().add(pane);
    }

    /**
     *
     * @return
     */
    public TextArea getTextArea() {
        return textArea;
    }

    /**
     *
     * @return
     */
    public Button getSaveButton() {
        return saveButton;
    }

    /**
     *
     * @return
     */
    public VBox getVBox() {
        return vPane;
    }

//    public String getStoredData() {
//        return storedData;
//    }
    /**
     *
     * @return
     */
    public LineChart getLineChart() {
        return lineChart;
    }

//    public void setStoredData(String input) {
//        storedData = input;
//    }
    private void setWorkspaceActions() {
        newButton.setDisable(false);

        textArea.textProperty().addListener(e -> {

            if (!textArea.getText().isEmpty()) {
//                newButton.setDisable(false);
                saveButton.setDisable(false);
                hasNewText = true;
            } else {
//                newButton.setDisable(true);
                saveButton.setDisable(true);
                hasNewText = false;
            }
            /*            storedData usage: if user is allowed to edit data loaded into program     */

//            if (textArea.getText().equals(storedData)) {
//                hasNewText = false;
//                saveButton.setDisable(true);
//            } else {
//                hasNewText = true;
//            }
        });

        toggleButton.setOnAction((ActionEvent e) -> {
            textArea.setDisable(!textArea.disableProperty().get());
            vPane.getChildren().remove(fileInfo);
            if (textArea.disableProperty().get()) {
                toggleButton.setText(applicationTemplate.manager.getPropertyValue(EDIT_BUTTON_NAME.name()));
                if (!processData()) {
                    return;
                }
                vPane.getChildren().add(fileInfo);
                algorithmPane.getChildren().clear();
                algorithmPane.getChildren().addAll(space, algorithmTable, algorithmList, runAlgorithm, iterationLabel);
                vPane.getChildren().add(algorithmPane);

            } else {
                if (!algorithmPane.getChildren().contains(algorithmTable)) {
                    algorithmPane.getChildren().add(algorithmTable);
                }
                algorithmPane.getChildren().remove(runAlgorithm);
                runAlgorithm.setDisable(true);
                algorithmList.getChildren().clear();
                resetToggleOptions();
                toggleOff();

            }

        });
//        displayButton.setOnAction(e -> {
//            try {
//                    clear();
//                    String currentText = textArea.getText();
//                    ArrayList<String> newDataEntries = new ArrayList<>(Arrays.asList(currentText.split(NEW_LINE)));
//                    
//                    ArrayList<String> fileDataEntries = ((AppData)applicationTemplate.getDataComponent()).getFileData();
//                    
//                    if(fileDataEntries != null){
//                        for(String j : fileDataEntries ){
//                            if(!newDataEntries.contains(j)){
//                                newDataEntries.add(j);
//                            }
//                        }
//                    }
//                    
//                    if(((AppActions)applicationTemplate.getActionComponent()).getDataPath() != null){
//                        storedData = String.join(NEW_LINE, newDataEntries);
//                    }
//                    else storedData = textArea.getText();
//                    
//                    
//                    ((AppData)applicationTemplate.getDataComponent()).loadData(storedData);
//                    
//                    if(hasNewText){
//                        ((AppData)applicationTemplate.getDataComponent()).displayData();
//                    }
//               }
//            catch(InvalidDataNameException | ArrayIndexOutOfBoundsException error){
//                ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
//                    err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), error.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
//            }
//            catch(Exception ex) {
//                ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
//                err.show(applicationTemplate.manager.getPropertyValue(DATA_DISPLAY_FAIL_TITLE.name()), ex.getMessage());
//           }
//        });
        lineChart.getData().addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                if (lineChart.getData().isEmpty()) {
                    scrnshotButton.setDisable(true);
                }

            }
        });

    }

    /**
     *
     * @param path
     */
    public void setFilePath(String path) {
        dataSource = path;
    }

    /**
     *
     */
    public void setFileMetaData() {
        numLabels = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getNumLabels();
        labels = ((AppData) applicationTemplate.getDataComponent()).getProcessor().getLabels();
        fileInfo.setText(applicationTemplate.manager.getPropertyValue(INSTANCE_COUNT_INFO.name()) + ((AppData) applicationTemplate.getDataComponent()).getProcessor().getNumInstances() + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(LABEL_COUNT_INFO.name()) + numLabels + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(LABEL_NAME_INFO.name()) + NEW_LINE + TAB + "• " + String.join(NEW_LINE + TAB + "• ", labels) + NEW_LINE
                + applicationTemplate.manager.getPropertyValue(SOURCE_INFO.name()) + dataSource);
    }

    /**
     *
     * @return
     */
    public boolean processData() {
        try {
            clear();
//            String currentText = textArea.getText();
//            ArrayList<String> newDataEntries = new ArrayList<>(Arrays.asList(currentText.split(NEW_LINE)));
//            ArrayList<String> fileDataEntries = ((AppData) applicationTemplate.getDataComponent()).getFileData();
//
//            if (fileDataEntries != null) {
//                for (String j : fileDataEntries) {
//                    if (!newDataEntries.contains(j)) {
//                        newDataEntries.add(j);
//                    }
//                }
//            }
//
//            if (((AppActions) applicationTemplate.getActionComponent()).getDataPath() != null) {
//                storedData = String.join(NEW_LINE, newDataEntries);
//            } else {
//                storedData = textArea.getText();
//            }
//
//            if (dataSource == null) {
//                dataSource = "";
//            }

            ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());

            setFileMetaData();
            return true;
        } catch (InvalidDataNameException | ArrayIndexOutOfBoundsException error) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), error.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
            textArea.setDisable(!textArea.disableProperty().get());
            toggleOff();
            return false;
        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DATA_DISPLAY_FAIL_TITLE.name()), ex.getMessage());
//                    textArea.setDisable(!textArea.disableProperty().get());
            toggleOff();
            return false;
        }
    }

    private void toggleOff() {
        toggleButton.setText(applicationTemplate.manager.getPropertyValue(DONE_BUTTON_NAME.name()));

        vPane.getChildren().remove(algorithmPane);
    }

    private void resetToggleOptions() {
        listOfAlgorithmTypes.getToggles() .forEach((b) -> {
            b.selectedProperty().set(false);
        });
    }

//    private void resetComboOptions() {
//        algorithmTypes.getSelectionModel().clearSelection();
//    }
    private ArrayList<RadioButton> getToggleGroups() {
        return algorithmRadioButtons;
    }

    private void clearCachedSettings() {
        cachedSettings.clear();
    }

    /**
     *
     * @return
     */
    public ScatterChart<Number, Number> getChart() {
        return scatterChart;
    }

    /**
     *
     * @return
     */
    public StackPane getChartPane() {
        return charts;
    }

    /**
     *
     * @return
     */
    public Button getRunButton() {
        return runAlgorithm;
    }

    /**
     *
     * @return
     */
    public Button getScreenshotButton() {
        return scrnshotButton;
    }

    /**
     *
     * @return
     */
    public NumberAxis getXAxis() {
        return xAxis;
    }

    /**
     *
     * @return
     */
    public NumberAxis getYAxis() {
        return yAxis;
    }

    /**
     *
     * @return
     */
    public Label getIterationLabel() {
        return iterationLabel;
    }

    /**
     *
     * @param count
     */
    public void setIterationLabelCount(int count) {
        iterationLabel.setText("Iteration number: " + count);
    }

    /**
     *
     * @return
     */
    public boolean getHasNewText() {
        return hasNewText;
    }

    /**
     *
     * @return
     */
    public Button getToggleButton() {
        return toggleButton;
    }

    /**
     *
     * @return
     */
    public Button getReturnButton() {
        return returnButton;
    }

    private void loadAlgorithms(String type, List<String> names) {
                configButtons = new ArrayList<>();
        for (String n : names) {
            try {
                Class<?> klass = Class.forName(n);
                Algorithm o = (Algorithm) klass.newInstance();
                final Button settingsButton = new Button();
                settingsButton.getStyleClass().add(applicationTemplate.manager.getPropertyValue(SETTINGS_CSS_CLASS.name()));
                settingsButton.setGraphic(new ImageView(settingsImage));
                
                if(configButtons.size() < names.size()) {
                    configButtons.add(settingsButton);
                }

                HBox algorithmContainer = new HBox();
                algorithmContainer.setAlignment(Pos.CENTER_LEFT);
//                System.out.println(klass.getCanonicalName());
                RadioButton algorithmSelection = new RadioButton(applicationTemplate.manager.getPropertyValue(klass.getSimpleName()));
                if(algorithmRadioButtons.size() < names.size()) {
                    algorithmRadioButtons.add(algorithmSelection);
                }
                algorithmContainer.getChildren().addAll(algorithmSelection, settingsButton);

                algorithmSelection.setToggleGroup(algorithmToggle);

                if (o.getClass().getSuperclass().getSimpleName().equals(type)) {
//                        algorithmPane.getChildren().add(returnButton);

                    algorithmList.getChildren().add(algorithmContainer);
                    algorithmPane.getChildren().clear();
                    algorithmPane.getChildren().addAll(space, returnButton, algorithmList, runAlgorithm, iterationLabel);
                }

                settingsButton.setOnAction(handler -> {
                    
                    algorithmConfigWindow = new Stage();
                    algorithmConfigWindow.initModality(Modality.APPLICATION_MODAL);
                    algorithmConfigWindow.setTitle(applicationTemplate.manager.getPropertyValue(ALGO_SETTINGS_TITLE.name()));
                    VBox mainPane = new VBox(20);
                    mainPane.setPadding(new Insets(10));
                    Label paneTitle = new Label(applicationTemplate.manager.getPropertyValue(ALGO_SETTINGS_TITLE.name()));
                    GridPane content = new GridPane();
                    content.setHgap(20);
                    content.setVgap(50);
                    content.setPadding(new Insets(10));
                    Label iterationsTitle = new Label(applicationTemplate.manager.getPropertyValue(ITERATIONS_TITLE.name()));
                    content.add(iterationsTitle, 0, 0);
                    Label intervalsTitle = new Label(applicationTemplate.manager.getPropertyValue(INTERVALS_TITLE.name()));
                    content.add(intervalsTitle, 0, 1);

                    if (o.getClass().getSuperclass().getSimpleName().equals("Clusterer")) {

                        Label numClustersTitle = new Label(applicationTemplate.manager.getPropertyValue(LABEL_COUNT_INFO.name()));
                        content.add(numClustersTitle, 0, 2);

                        clustersField = new TextField();
                        clustersField.setPrefWidth(65);
                        clustersField.setPromptText("2");
                        clustersField.setFocusTraversable(false);
                        content.add(clustersField, 1, 2);
                        if (!cachedSettings.isEmpty() && cachedSettings.get(configButtons.indexOf(settingsButton)) != null ) {
                            clustersField.setText("" + cachedSettings.get(configButtons.indexOf(settingsButton)).getLabels());
                        }
                    }

                    Label runTitle = new Label("Continuous Run? ");
                    content.add(runTitle, 0, 3);

                    iterationsField = new TextField();
                    iterationsField.setPrefWidth(65);
                    iterationsField.setPromptText("1000");
                    iterationsField.setFocusTraversable(false);
                    content.add(iterationsField, 1, 0);
                    if (!cachedSettings.isEmpty() 
                            && cachedSettings.get(configButtons.indexOf(settingsButton)) != null) {
                        iterationsField.setText("" + cachedSettings.get(configButtons.indexOf(settingsButton)).getIterations());
                    }
                    intervalsField = new TextField();
                    intervalsField.setPrefWidth(65);
                    intervalsField.setPromptText("5");
                    intervalsField.setFocusTraversable(false);
                    content.add(intervalsField, 1, 1);

                    runOption = new CheckBox();
                    content.add(runOption, 1, 3);

                    if (!cachedSettings.isEmpty() && cachedSettings.get(configButtons.indexOf(settingsButton)) != null ) {
                        iterationsField.setText("" + cachedSettings.get(configButtons.indexOf(settingsButton)).getIterations());
                        intervalsField.setText("" + cachedSettings.get(configButtons.indexOf(settingsButton)).getIntervals());
                        runOption.setSelected(cachedSettings.get(configButtons.indexOf(settingsButton)).isContinuousState());
                    }

                    currentSettings = new ConfigState();
                    currentSettings.setBtn(settingsButton);
                    mainPane.getChildren().addAll(paneTitle, content);

                    Scene subscene = new Scene(mainPane);
                    algorithmConfigWindow.setScene(subscene);
                    algorithmConfigWindow.showAndWait();

                    currentSettingsButton = settingsButton;
                    /* Initializing User Config Settings */
                    Integer clusters;
                    Integer intervals;
                    Integer iterations;

                    if (clustersField == null) {
                        clusters = 2;
                    } else if (clustersField.textProperty().get().equals("") || Integer.parseInt(clustersField.textProperty().get()) <= 2) {
                        clusters = 2;
                    } else if (Integer.parseInt(clustersField.textProperty().get()) >= 4) {
                        clusters = 4;
                    } else {
                        clusters = Integer.parseInt(clustersField.textProperty().get());
                    }

                    if (iterationsField.textProperty().get().equals("") || Integer.parseInt(iterationsField.textProperty().get()) <= 0 || Integer.parseInt(iterationsField.textProperty().get()) > 5000) {
                        iterations = 1000;
                    } else {
                        iterations = Integer.parseInt(iterationsField.textProperty().get());
                    }
                    if (intervalsField.textProperty().get().equals("") || Integer.parseInt(intervalsField.textProperty().get()) <= 0 || Integer.parseInt(intervalsField.textProperty().get()) > Integer.parseInt(iterationsField.textProperty().get())) {
                        intervals = 5;
                    } else {
                        intervals = Integer.parseInt(intervalsField.textProperty().get());
                    }

                    boolean continuousState = runOption.isSelected();
                    currentSettings.setIntervals(intervals);
                    currentSettings.setIterations(iterations);
                    currentSettings.setLabels(clusters);
                    currentSettings.setContinuousState(continuousState);
                    if (cachedSettings.size() < configButtons.size()) {
                        for (int i = 0; i < configButtons.size(); i++) {
                            cachedSettings.add(null);
                        }
                    }
                    cachedSettings.set(configButtons.indexOf(settingsButton), currentSettings);
                    if (currentAlgorithmTypeSelection != null
                            && cachedSettings.get(configButtons.indexOf(settingsButton)).equals(cachedSettings.get(algorithmRadioButtons.indexOf(currentAlgorithmTypeSelection)))
                            && !((AppData) applicationTemplate.getDataComponent()).getProcessor().getAlgorithmIsRunning()) {
                        runAlgorithm.setDisable(false);
                    } else {
                        runAlgorithm.setDisable(true);
                    }
                });

                for (RadioButton rb : getToggleGroups()) {
                    rb.selectedProperty().addListener(l -> {
                        currentAlgorithmTypeSelection = rb;
                        try{
                        if (!cachedSettings.isEmpty()
                                && cachedSettings.get(configButtons.indexOf(currentSettingsButton)).equals(cachedSettings.get(algorithmRadioButtons.indexOf(currentAlgorithmTypeSelection)))
                                && !((AppData) applicationTemplate.getDataComponent()).getProcessor().getAlgorithmIsRunning()) {
                            runAlgorithm.setDisable(false);
                        } else {
                            runAlgorithm.setDisable(true);
                        }
                        }
                        catch(ArrayIndexOutOfBoundsException e){
                            
                        }
                    });
                }

                runAlgorithm.setOnAction(e -> {
                    lineChart.getData().clear();
                    scatterChart.getData().clear();
                    ((AppData) applicationTemplate.getDataComponent()).displayData();
                    returnButton.setDisable(true);
                    if (((RadioButton) (currentAlgorithmTypeSelection)).getText().equals("Random Classification")) {
                        ((AppData) applicationTemplate.getDataComponent()).getProcessor().runClassificationAlgorithm(currentSettings);
                    }
                    if (((RadioButton) (currentAlgorithmTypeSelection)).getText().equals("Random Clustering")) {
                        ((AppData) applicationTemplate.getDataComponent()).getProcessor().runRandomClusteringAlgorithm(currentSettings);
                    }
                    if (((RadioButton) (currentAlgorithmTypeSelection)).getText().equals("K-Means Clustering")) {
                        ((AppData) applicationTemplate.getDataComponent()).getProcessor().runKMeansClusteringAlgorithm(currentSettings);
                    }
                });
                returnButton.setOnAction(listener -> {
                    algorithmList.getChildren().clear();
                    algorithmPane.getChildren().clear();

                    algorithmPane.getChildren().addAll(space, algorithmTable, algorithmList, runAlgorithm, iterationLabel);
                    resetToggleOptions();
                    runAlgorithm.setDisable(true);
                });

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | IndexOutOfBoundsException ex) {
            }
        }
    }

}

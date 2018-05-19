/**
 *
 * @author David Doo
 */
package actions;

import java.io.File;
import vilij.components.ActionComponent;
import vilij.templates.ApplicationTemplate;
import vilij.components.ConfirmationDialog;
import vilij.components.ConfirmationDialog.Option;
import vilij.components.Dialog;

import java.io.IOException;
import java.nio.file.Path;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import dataprocessors.AppData;
import java.io.FileWriter;
;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javax.imageio.ImageIO;
import ui.AppUI;
import vilij.components.ErrorDialog;
import static settings.AppPropertyTypes.*;

/**
 * This is the concrete implementation of the action handlers required by the
 * application.
 *
 * @author Ritwik Banerjee
 */


public final class AppActions implements ActionComponent {

    /**
     * The application to which this class of actions belongs.
     */
    private ApplicationTemplate applicationTemplate;

    /**
     * Path to the data file currently active.
     */
    Path dataFilePath;
    File currentFile;
    private boolean errorFlag;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        errorFlag = false;
    }

    @Override
    public void handleNewRequest() {
//        try {
        errorFlag = false;
//            if(dataFilePath == null){
//                promptToSave();
//            }
        if (((AppData) applicationTemplate.getDataComponent()).getFileData() != null) {
            ((AppData) applicationTemplate.getDataComponent()).resetData();
        }
        ((AppUI) applicationTemplate.getUIComponent()).clear();
        ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
        ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setDisable(false);
        currentFile = null;
        dataFilePath = null;
        ((AppUI) applicationTemplate.getUIComponent()).setFilePath("");
//        } catch (IOException ex) {
//            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
//            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(NEW_FILE_ERROR.name()));
//            errorFlag = true;
//        }
    }

    @Override
    public void handleSaveRequest() {
        errorFlag = false;
        try {
            ((AppData) applicationTemplate.getDataComponent()).getProcessor().processString(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());

        } catch (Exception e) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()),
                    e.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
            return;
        }
        if (dataFilePath == null) {

            FileChooser fc = new FileChooser();
            initializeFileChooser(fc, false);
            currentFile = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (currentFile == null) {
                errorFlag = true;
                return;
            }
        }
        try {
            dataFilePath = currentFile.toPath().toAbsolutePath();
            ((AppData) applicationTemplate.getDataComponent()).saveData(dataFilePath);
            ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);

        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_FILE_ERROR.name()));
            errorFlag = true;
        }

    }

    @Override
    public void handleLoadRequest() {
        errorFlag = false;
        FileChooser fc = new FileChooser();
        initializeFileChooser(fc, false);
        currentFile = fc.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (currentFile != null) {
            dataFilePath = currentFile.toPath();
            ConfirmationDialog existingData = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            existingData.show(applicationTemplate.manager.getPropertyValue(CLEAR_INTERFACE_TITLE.name()), applicationTemplate.manager.getPropertyValue(CLEAR_INTERFACE_DESC.name()));
            Option userSelection = existingData.getSelectedOption();
            if (userSelection == Option.YES) {
                ((AppUI) applicationTemplate.getUIComponent()).clear();
                ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
                ((AppUI) applicationTemplate.getUIComponent()).setFilePath(dataFilePath.toString());
                ((AppData) applicationTemplate.getDataComponent()).loadData(dataFilePath);
            } else {
                errorFlag = true;
            }
        } else {
            errorFlag = true;
        }
    }

    @Override
    public void handleExitRequest() {
        errorFlag = false;
        try {
            if (((AppUI) applicationTemplate.getUIComponent()).getHasNewText() && !((AppData) applicationTemplate.getDataComponent()).isDataLoaded()) {
                ConfirmationDialog saveDataRequest = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);

                saveDataRequest.show("Unsaved work", "Want to save changes in text area?");
                Option userSelection = saveDataRequest.getSelectedOption();
                
                if (userSelection == Option.CANCEL) {
                    return;
                } else if (userSelection == Option.YES) {
                    try {
                        ((AppData) applicationTemplate.getDataComponent()).getProcessor().processString(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());

                    } catch (Exception e) {
                        ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()),
                                e.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
                        return;
                    }
                    FileChooser fc = new FileChooser();
                    initializeFileChooser(fc, false);
                    currentFile = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    if(currentFile == null) return;
                    try {
                        dataFilePath = currentFile.toPath().toAbsolutePath();
                        ((AppData) applicationTemplate.getDataComponent()).saveData(dataFilePath);
                    } catch (Exception ex) {
                        ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                        err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_FILE_ERROR.name()));
                        errorFlag = true;
                    }
                }
            }
            if (((AppData) applicationTemplate.getDataComponent()).getProcessor().getAlgorithmIsRunning()) {
                ConfirmationDialog exitRequest = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                exitRequest.show(applicationTemplate.manager.getPropertyValue(EXIT_TITLE.name()), applicationTemplate.manager.getPropertyValue(EXIT_WHILE_RUNNING_WARNING.name()));
                Option userSelection = exitRequest.getSelectedOption();
                if (userSelection == Option.YES) {
                    applicationTemplate.stop();
                    System.exit(0);
                }
            } else {
                applicationTemplate.stop();
                System.exit(0);

            }

        } catch (Exception ex) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(EXIT_APP_ERROR.name()));
            errorFlag = true;
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        errorFlag = false;
        try {
            if (!(((AppData) applicationTemplate.getDataComponent()).getProcessor().isChartEmpty())) {
                StackPane pane = ((AppUI) applicationTemplate.getUIComponent()).getChartPane();
                WritableImage image = pane.snapshot(null, null);

                FileChooser fc = new FileChooser();
                initializeFileChooser(fc, true);

                File file = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

                ImageIO.write(SwingFXUtils.fromFXImage(image, null), applicationTemplate.manager.getPropertyValue(IMAGE_FILE_TYPE_PARAM.name()), file);
            }

        } catch (IOException e) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SCREENSHOT_FILE_ERROR.name()));
            errorFlag = true;
        } catch (IllegalArgumentException e) {
            ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SCREENSHOT_OUTPUT_ERROR.name()));
            errorFlag = true;
        }
    }

    /**
     * This helper method verifies that the user really wants to save their
     * unsaved work, which they might not want to do. The user will be presented
     * with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and
     * continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the
     * action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to
     * continue with the action, but also does not want to save the work at this
     * point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and
     * <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        ConfirmationDialog save = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        save.show(applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_UNSAVED_WORK.name()));
        Option userSelection = save.getSelectedOption();
        if (userSelection == Option.YES) {

            FileChooser fc = new FileChooser();
            initializeFileChooser(fc, false);
            currentFile = fc.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (currentFile != null) {
                try {
                    FileWriter writer = new FileWriter(currentFile);
                    writer.write(((AppUI) applicationTemplate.getUIComponent()).getTextArea().getText());
                    writer.close();
                    dataFilePath = currentFile.toPath().toAbsolutePath();
                } catch (IOException ex) {
                    ErrorDialog err = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    err.show(applicationTemplate.manager.getPropertyValue(DEFAULT_ERROR_TITLE.name()), applicationTemplate.manager.getPropertyValue(SAVE_FILE_ERROR.name()));
                }
            }
            ((AppUI) applicationTemplate.getUIComponent()).clear();
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
            return true;
        } else if (userSelection == Option.NO) {
            ((AppUI) applicationTemplate.getUIComponent()).clear();
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().clear();
            return true;
        } else {
            return false;
        }
    }

    public Path getDataPath() {
        return dataFilePath;
    }

    private void initializeFileChooser(FileChooser fc, boolean isScreenshot) {
        File dir = new File(applicationTemplate.manager.getPropertyValue(USER_DIRECTORY.name()));
        File workingDirectory = new File(dir.getAbsolutePath());
        fc.setInitialDirectory(workingDirectory);
        fc.setInitialFileName(applicationTemplate.manager.getPropertyValue(DEFAULT_FILE_NAME.name()));
        if (isScreenshot) {
            fc.setTitle(applicationTemplate.manager.getPropertyValue(SCREENSHOT_DIALOG_TITLE.name()));
            fc.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(IMAGE_FILE_TYPE_NAME.name()),
                    applicationTemplate.manager.getPropertyValue(SCREENSHOT_FILE_EXT.name())));
        } else {
            fc.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_DIALOG_TITLE.name()));
            fc.getExtensionFilters().add(new ExtensionFilter(applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT_DESC.name()),
                    applicationTemplate.manager.getPropertyValue(DATA_FILE_EXT.name())));
        }
    }

    public boolean getFlag() {
        return errorFlag;
    }

    public void setFlag() {
        errorFlag = !errorFlag;
    }
}

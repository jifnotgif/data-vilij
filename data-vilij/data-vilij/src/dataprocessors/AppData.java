/**
 *
 * @author David Doo
 */
package dataprocessors;

import actions.AppActions;
import java.io.FileWriter;
import java.nio.file.Files;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.TextArea;
import static settings.AppPropertyTypes.INPUT;
import static settings.AppPropertyTypes.INPUT_TITLE;
import static settings.AppPropertyTypes.LOADED_MANY_LINES_DESC_1;
import static settings.AppPropertyTypes.LOADED_MANY_LINES_DESC_2;
import static settings.AppPropertyTypes.LOADED_MANY_LINES_DIALOG_TITLE;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private ArrayList<String>   dataEntries;
    private final String        NEW_LINE = "\n";
    private boolean             isDataLoaded;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor(applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        this.isDataLoaded = false;
    }

    @Override
    public void loadData(Path dataFilePath) {
        try{
            clear();
            if(dataEntries != null && !dataEntries.isEmpty()) resetData();
            
            isDataLoaded = true;
            
            AtomicInteger index = new AtomicInteger(0);
            TextArea textbox = ((AppUI)applicationTemplate.getUIComponent()).getTextArea();
            String data = new String(Files.readAllBytes(dataFilePath));
            processor.processString(data);
//            ((AppUI)applicationTemplate.getUIComponent()).setStoredData(data);
            
            int len = data.split(NEW_LINE).length;
            dataEntries = new ArrayList<>(Arrays.asList(data.split(NEW_LINE)));
            String output = new String();
              
            if(len >10){
                for(int i =0; i< 10; i++){
                    output += dataEntries.get(0) + NEW_LINE;
                    dataEntries.remove(0);
                } 
                ErrorDialog manyLines = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                manyLines.show(applicationTemplate.manager.getPropertyValue(LOADED_MANY_LINES_DIALOG_TITLE.name()), 
                        applicationTemplate.manager.getPropertyValue(LOADED_MANY_LINES_DESC_1.name())
                                + len 
                                    + applicationTemplate.manager.getPropertyValue(LOADED_MANY_LINES_DESC_2.name()));
                
            }
            else{
                for(int i =0; i< len; i++){
                    output += dataEntries.get(0) + NEW_LINE;
                    dataEntries.remove(0);
                } 
            }
            textbox.setText(output);
            
            textbox.textProperty().addListener(e ->{
                if(textbox.getText().isEmpty()) {
                    dataEntries.clear();
                    return;
                }
                if(textbox.getText().split(NEW_LINE).length < 10 && index.get() < dataEntries.size()){
                    textbox.appendText(dataEntries.get(index.getAndIncrement())+ NEW_LINE);
                    dataEntries.remove(0);   
                }
                index.set(0);
            });
            
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), 
                    e.getMessage() +applicationTemplate.manager.getPropertyValue(INPUT.name()));
            ((AppActions)applicationTemplate.getActionComponent()).setFlag();
        }
    }

    public void loadData(String dataString) throws Exception {
        try{
//            if(dataEntries != null && !dataEntries.isEmpty()) resetData();
            clear();
            processor.processString(dataString);
        }
        catch(Exception e){
            throw e;
        }
        
       
    }

    @Override
    public void saveData(Path dataFilePath) {
        try{
//            String dataString =((AppUI)applicationTemplate.getUIComponent()).getStoredData();
//            if(dataString.equals("")) {
//                dataString = ((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText();
//            }
            try (FileWriter writer = new FileWriter(dataFilePath.toFile())) {
                writer.write(((AppUI)applicationTemplate.getUIComponent()).getTextArea().getText());
            }
        }
        catch(Exception e){
            ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            err.show(applicationTemplate.manager.getPropertyValue(INPUT_TITLE.name()), 
                    e.getMessage() + applicationTemplate.manager.getPropertyValue(INPUT.name()));
        }
        
    }

    @Override
    public void clear() {
        processor.clear();
    }

    
    public void displayData() {
            processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
    
    public TSDProcessor getProcessor(){
        return processor;
    }

    public ArrayList<String> getFileData(){
        return dataEntries;
    }
    
    public void resetData(){
        dataEntries.clear();
    }
    
    public boolean isDataLoaded(){
        return isDataLoaded;
    }
    
}

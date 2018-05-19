package dataprocessors;

import algorithms.base.Algorithm;
import algorithms.RandomClassifier;
import algorithms.KMeansClusterer;
import algorithms.RandomClusterer;
import data.DataSet;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import static java.lang.Thread.sleep;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Line;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    private RandomClassifier currentRandomClassifier;
    private RandomClusterer currentRandomClusterer;
    private KMeansClusterer currentKMeansClusterer;
    private Thread currentAlgorithm_thread;

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s" + NAME_ERROR_MSG , name));
        }
    }
    private DataSet              data;
    private Set<String>          labels;
    private Set<Thread>          runningAlgorithms;
    private final String         NEW_LINE = "\n";
    private final String         NAME_SYMBOL ="@";
    private final String         TAB = "\t";
    private boolean              algorithmIsRunning;
    ApplicationTemplate applicationTemplate;
    
    public TSDProcessor(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
//        this.runningAlgorithms = new HashSet<Thread>();
        this.algorithmIsRunning = false;
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws InvalidDataNameException {
        this.data = new DataSet();
        AtomicBoolean containsDuplicates = new AtomicBoolean(false);
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        AtomicInteger lineCount    = new AtomicInteger(1);
        
        ArrayList<Integer> linesWithError = new ArrayList<>();
        ArrayList<String> duplicateNames = new ArrayList<>();
        
        StringBuilder errorMessage = new StringBuilder();
        
        Stream.of(tsdString.split(NEW_LINE))
              .map(line -> Arrays.asList(line.split(TAB)))
              .forEach(list -> {
                  try {
                      if(tsdString.trim().equals("")) throw new InvalidDataNameException("test");
                      String   name  = checkedname(list.get(0));
                      if(duplicateNames.contains(name)){
                          linesWithError.add((Integer)lineCount.get());
                          errorMessage.setLength(0);
                          errorMessage.append(list.get(0));
                          containsDuplicates.set(true);
                      }
                      duplicateNames.add(name);
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      data.getLabels().put(name, label);
                      data.getLocations().put(name, point);
                  } catch (Exception e) {
                      linesWithError.add((Integer)lineCount.get());
                      errorMessage.setLength(0);
                      errorMessage.append(list.get(0)).append("'.\nError on line(s): ").append(linesWithError.toString().substring(1,linesWithError.toString().length()-1)).append(NEW_LINE);  
                      hadAnError.set(true);
                  }
                  lineCount.incrementAndGet();
              });
        if (containsDuplicates.get()) {
            errorMessage.append("'.\nDuplicate found on line(s): ").append(linesWithError.toString().substring(1, linesWithError.toString().length() - 1)).append(NEW_LINE);
            hadAnError.set(true);

        }
        if (errorMessage.length() > 0)
            throw new InvalidDataNameException(errorMessage.toString());
        
    }

    public DataSet getData() {
        return data;
    }

    public boolean isChartEmpty(){
        return data.getLabels().isEmpty();
    }
    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        AtomicInteger counter = new AtomicInteger(0);
        labels = new HashSet<>(data.getLabels().values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            data.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = data.getLocations().get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
            for(XYChart.Data<Number,Number> d : series.getData()){
                data.getLabels().entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                    if(counter.get() < series.getData().size()) {
                        Tooltip.install(series.getData().get(counter.get()).getNode(), new Tooltip(entry.getKey()));
                        series.getData().get(counter.getAndIncrement()).getNode().setCursor(Cursor.HAND);
                    }
                });
            counter.set(0);
            }
        }
    }
    

    
    void clear() {
//        if(isAlgorithmRunning()){
//            for(Thread t : runningAlgorithms){
//                
//            }
//        }
        if(data != null){
            data.getLocations().clear();
            data.getLabels().clear();
        }
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith(NAME_SYMBOL))
            throw new InvalidDataNameException(name);
        return name;
    }
    
    public int getNumInstances(){
        return data.getLabels().size();
    }
    public int getNumLabels(){
        labels = new HashSet<>(data.getLabels().values());
        return labels.size();
    }
    
    public Set<String> getLabels(){
        labels = new HashSet<>(data.getLabels().values());
        return labels;
    }
    
    //better way to implement by checking hashset
//    public boolean isAlgorithmRunning(){
//        if(currentRandomClassifier == null) return false;
//        return currentRandomClassifier.isAlgorithmActive();
//    }
    
    public void runClassificationAlgorithm(ConfigState settings){
        if(currentRandomClassifier == null) {
            currentRandomClassifier = new RandomClassifier(settings, data, applicationTemplate);
            currentAlgorithm_thread = new Thread(currentRandomClassifier);
            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else if(!currentRandomClassifier.isAlgorithmActive()) {
            currentRandomClassifier = new RandomClassifier(settings, data, applicationTemplate);

            currentAlgorithm_thread = new Thread(currentRandomClassifier);
            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else{
            
            synchronized(currentAlgorithm_thread){
                currentAlgorithm_thread.notify();
            }
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
         
    }
    
    public void runRandomClusteringAlgorithm(ConfigState settings){
        
        if(currentRandomClusterer == null) {
            currentRandomClusterer = new RandomClusterer(settings, data, applicationTemplate);
            
            currentAlgorithm_thread = new Thread(currentRandomClusterer);
            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else if(!currentRandomClusterer.isAlgorithmActive()) {
            currentRandomClusterer = new RandomClusterer(settings, data, applicationTemplate);

            currentAlgorithm_thread = new Thread(currentRandomClusterer);
            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else{
            
            synchronized(currentAlgorithm_thread){
                currentAlgorithm_thread.notify();
            }
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
    }
    
    public void runKMeansClusteringAlgorithm(ConfigState settings){
        if(currentKMeansClusterer == null) {
            currentKMeansClusterer = new KMeansClusterer(settings, data, applicationTemplate);
            currentAlgorithm_thread = new Thread(currentKMeansClusterer);

            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else if(!currentKMeansClusterer.isAlgorithmActive()) {
            currentKMeansClusterer = new KMeansClusterer(settings, data, applicationTemplate);
            currentAlgorithm_thread = new Thread(currentKMeansClusterer);
            currentAlgorithm_thread.start();
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
        }
        else{
            synchronized(currentAlgorithm_thread){
                currentAlgorithm_thread.notify();
            }
            this.algorithmIsRunning = currentAlgorithm_thread.isAlive();
            }
        
    }
    public boolean getAlgorithmIsRunning(){
        return this.algorithmIsRunning;
    }
    
    public void setAlgorithmIsRunning(boolean newState){
        this.algorithmIsRunning = newState;
    }
    // create a method to set algorithm running state
    // call the method inside the algorithm classes
    // to change the state to false
}
    
    


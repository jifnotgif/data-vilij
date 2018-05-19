/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import static java.lang.Thread.sleep;
import algorithms.base.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author David
 */
public class RandomClusterer extends Clusterer {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    ApplicationTemplate applicationTemplate;

    private DataSet dataset;
//    private XYChart<Number, Number> chart;

    private final AtomicInteger counter;
    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean algorithmActiveState;
    private final AtomicBoolean tocontinue;
    private ArrayList<String> labels;
    
    public RandomClusterer(){
        this.counter = null;
        this.dataset = null;
        this.applicationTemplate = null;
        this.maxIterations = 0;
        this.updateInterval = 0;
        this.tocontinue = null;
        
        this.algorithmActiveState = null;
    }
    public RandomClusterer(ConfigState settings, DataSet dataset, ApplicationTemplate ui) {
        super(settings);
        this.dataset = dataset;
        this.applicationTemplate = ui;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(0);
        this.algorithmActiveState = new AtomicBoolean(false);
        intializeLabels();
        setChartBounds();
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public int getIterationCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.addAndGet(updateInterval);
    }

    public boolean isAlgorithmActive() {
        return algorithmActiveState.get();
    }

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
        algorithmActiveState.set(true);
        if (tocontinue()) {
            for (int i = 0; i < maxIterations; i++) {
                counter.getAndIncrement();
                if (i % updateInterval == 0) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("disabled");
                        setNewLabels();
                    });
                }

                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(false);
                    });

                    counter.set(0);

                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().setAlgorithmIsRunning(false);

                    algorithmActiveState.set(false);
                    break;
                }
                try {
                    sleep(150);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            for (int i = counter.get(); i < maxIterations; i++) {
                Platform.runLater(()->{
                    ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                });
                counter.getAndIncrement();
                
                if (counter.get() % updateInterval == 0) {
                    Platform.runLater(() -> {

                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("busy");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Resume");

                        setNewLabels();

                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    });
                    synchronized (Thread.currentThread()) {
                        try {
                            Thread.currentThread().wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }

                if (counter.get() > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Run");
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(false);
                        setNewLabels();
                    });

                    counter.set(0);
                    ((AppData) applicationTemplate.getDataComponent()).getProcessor().setAlgorithmIsRunning(false);

                    algorithmActiveState.set(false);
                    break;
                }
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(false);

    }

    public void setNewLabels() {

        for (String instance : dataset.getLabels().keySet()) {
            Random rand = new Random();
            int labelNum = rand.nextInt(this.getNumberOfClusters());

            dataset.getLabels().put(instance, labels.get(labelNum));
        }
        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
        ((AppData) applicationTemplate.getDataComponent()).displayData();
    }

    private void intializeLabels() {
        labels = new ArrayList<>();
        for (int i = 1; i <= this.getNumberOfClusters(); i++) {
            labels.add("" + i);
        }

    }

    private void setChartBounds() {

        int x1 = dataset.getMinX();
        int x2 = dataset.getMaxX();
        int yMinBound = dataset.getMinY() - 5;
        int yMaxBound = dataset.getMaxY() + 5;

        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setLowerBound(x1 - 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setUpperBound(x2 + 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setForceZeroInRange(false);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setAutoRanging(false);

        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setLowerBound(yMinBound);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setUpperBound(yMaxBound);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setAutoRanging(false);

    }
    
    public String getName(){
        return this.getClass().getName();
    }

}

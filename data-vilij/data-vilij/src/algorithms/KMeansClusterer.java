/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

import algorithms.base.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import static java.lang.Thread.sleep;
import javafx.geometry.Point2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import sun.security.tools.keytool.Main;
import ui.AppUI;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    @SuppressWarnings("FieldCanBeLocal")

    ApplicationTemplate applicationTemplate;

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean continuousState;
    private final AtomicBoolean algorithmActiveState;
    private final AtomicInteger counter;
      
    public KMeansClusterer(){
        this.counter = null;
        this.dataset = null;
        this.applicationTemplate = null;
        this.maxIterations = 0;
        this.updateInterval = 0;
        this.tocontinue = null;
        this.continuousState = null;
        
        this.algorithmActiveState = null;
    }

    public KMeansClusterer(ConfigState settings, DataSet data, ApplicationTemplate ui) {
        super(settings);
        this.dataset = data;
        this.applicationTemplate = ui;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(false);
        this.continuousState = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(0);
        this.algorithmActiveState = new AtomicBoolean(false);
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

    @Override
    public void run() {
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(true);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(true);
        algorithmActiveState.set(true);
        initializeCentroids();
        int iteration = 0;
        if (continuousState.get()) {
            while (iteration++ < maxIterations ) {
                counter.incrementAndGet();
                if(tocontinue()){
                    assignLabels();
                    recomputeCentroids();
                }
                if(iteration % updateInterval == 0) {
                    Platform.runLater(() -> {

                        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                        ((AppData) applicationTemplate.getDataComponent()).displayData();
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("disabled");
                ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(true);
                    });   
                }
                try {
                    sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Platform.runLater(() -> {
                ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);
                ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(false);
            });

            counter.set(0);

            ((AppData) applicationTemplate.getDataComponent()).getProcessor().setAlgorithmIsRunning(false);

            algorithmActiveState.set(false);
        } else {
            while (counter.get() < maxIterations) {
                Platform.runLater(()->{
                    ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                });
                counter.getAndIncrement();
                assignLabels();
                recomputeCentroids();
                if(counter.get() % updateInterval == 0) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear();
                        ((AppData) applicationTemplate.getDataComponent()).displayData();

                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("busy");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Resume");
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(false);

                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
                    });
                    synchronized(Thread.currentThread()){
                        try {
                            Thread.currentThread().wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(KMeansClusterer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Platform.runLater(() -> {
                ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Run");

            });

            counter.set(0);
            ((AppData) applicationTemplate.getDataComponent()).getProcessor().setAlgorithmIsRunning(false);

            algorithmActiveState.set(false);
        }
        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(false);
        ((AppUI) applicationTemplate.getUIComponent()).getToggleButton().setDisable(false);
    }

    private void initializeCentroids() {
        Set<String> chosen = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random r = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i))) {
                ++i;
            }
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

    public boolean isAlgorithmActive() {
        return algorithmActiveState.get();
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

package algorithms;

import algorithms.base.Classifier;
import data.DataSet;
import dataprocessors.AppData;

import static java.lang.Thread.sleep;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    ApplicationTemplate applicationTemplate;

    private DataSet dataset;

    private final AtomicInteger counter;
    private final int maxIterations;
    private final int updateInterval;

    private final AtomicBoolean tocontinue;
    private List<Integer> output;
    private XYChart.Series<Number, Number> algorithmLine;
    private AtomicBoolean algorithmActiveState;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int yMinBound;
    private int yMaxBound;

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
    
    
    public RandomClassifier(ConfigState settings, DataSet dataset, ApplicationTemplate ui) {
        this.dataset = dataset;
        this.applicationTemplate = ui;
        this.maxIterations = settings.getIterations();
        this.updateInterval = settings.getIntervals();
        this.tocontinue = new AtomicBoolean(settings.isContinuousState());
        this.counter = new AtomicInteger(0);
        this.algorithmActiveState = new AtomicBoolean(false);
    }

    public RandomClassifier(){
        this.counter = null;
        this.dataset = null;
        this.applicationTemplate =  null;
        this.maxIterations = 0;
        this.updateInterval = 0;
        this.tocontinue = null;
        
        this.algorithmActiveState = null;
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
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                if (i % updateInterval == 0) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("disabled");
                        calculateLineOutput();
                        ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);

                    });
                }

                if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("active");
                        calculateLineOutput();
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
                Platform.runLater(() -> {
                    ((AppUI) applicationTemplate.getUIComponent()).getScreenshotButton().setDisable(true);
                    ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setDisable(true);
                });
                counter.getAndIncrement();
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                if (counter.get() % updateInterval == 0) {
                    Platform.runLater(() -> {
                        ((AppUI) applicationTemplate.getUIComponent()).setIterationLabelCount(counter.get());
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setId("busy");
                        ((AppUI) applicationTemplate.getUIComponent()).getRunButton().setText("Resume");
                        calculateLineOutput();
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
                        calculateLineOutput();
                        ((AppUI) applicationTemplate.getUIComponent()).getReturnButton().setDisable(false);

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

    public int getIterationCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.addAndGet(updateInterval);
    }

    private void calculateLineOutput() {
        if (algorithmLine != null) {
            ((AppUI)applicationTemplate.getUIComponent()).getLineChart().getData().remove(algorithmLine);
        }

        algorithmLine = new XYChart.Series<>();
        algorithmLine.setName("Classification Line");

        x1 = dataset.getMinX();
        x2 = dataset.getMaxX();
        yMinBound = dataset.getMinY() - 5;
        yMaxBound = dataset.getMaxY() + 5;

        y1 = (int) ((-output.get(2) - output.get(0) * x1) / output.get(1));
        y2 = (int) ((-output.get(2) - output.get(0) * x2) / output.get(1));

        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setLowerBound(x1 - 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setUpperBound(x2 + 1);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setForceZeroInRange(false);
        ((AppUI) applicationTemplate.getUIComponent()).getXAxis().setAutoRanging(false);
//        double yLowerBound = dataset.getLocations().values().stream().mapToDouble(Point2D::getY).min().orElse(0.0);

        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setLowerBound(yMinBound);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setUpperBound(yMaxBound);
//        ((AppUI)applicationTemplate.getUIComponent()).getYAxis().setForceZeroInRange(false);
        ((AppUI) applicationTemplate.getUIComponent()).getYAxis().setAutoRanging(false);

        algorithmLine.getData().add(new XYChart.Data<>(x1, y1));
        algorithmLine.getData().add(new XYChart.Data<>(x2, y2));
        ((AppUI)applicationTemplate.getUIComponent()).getLineChart().getData().add(algorithmLine);

        algorithmLine.getNode().setId("algorithm");

    }

    public boolean isAlgorithmActive() {
        return algorithmActiveState.get();
    }
    
    public String getName(){
        return this.getClass().getName();
    }
}

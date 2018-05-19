/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.base;

import algorithms.base.Algorithm;
import data.DataSet;
import javafx.scene.chart.XYChart;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;
/**
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    protected final int numberOfClusters;

    public int getNumberOfClusters() { return numberOfClusters; }

    public Clusterer(){
        numberOfClusters =0;
    }
    public Clusterer(ConfigState s) {
        if ( s.getLabels() < 2)
            s.setLabels(2); 
        else if (s.getLabels() > 4)
            s.setLabels(4);
        numberOfClusters = s.getLabels();
    }
}
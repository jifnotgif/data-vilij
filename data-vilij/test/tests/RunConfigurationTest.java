/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import ui.ConfigState;

/**
 *
 * @author David
 */
public class RunConfigurationTest {

    private ConfigState settings;
    private Integer clusters;
    private Integer updateInterval;
    private Integer maxIterations;
    private boolean iscontinuous;
    
    private int[] listofclusters;
    private int[] listofintervals;
    private int[] listofiterations;
    
    public RunConfigurationTest() {
    }
    
    @Before
    public void setUpClass() {
        settings = new ConfigState();
        
        listofclusters = new int[] {-1, 0, 2, 3, 4, 5};
        listofintervals = new int[] {-1, 0, 1, 5, 100, 200};
        listofiterations = new int[] {-1, 0, 100, 5000, 5001};
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void setRunConfigValues_pass(){
        setConfigValues(3,5,100, true);
        Assert.assertEquals(new ConfigState(null, listofiterations[2],listofintervals[3] , listofclusters[3], new AtomicBoolean(true)), settings);
        setConfigValues(3,100,5000, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[3],listofintervals[4] , listofclusters[3], new AtomicBoolean(false)), settings);   
    }
    
    @Test
    public void setRunConfigValues_modified(){
        /*
        boundary value: updateInterval > maxIterations, set updateInterval to 1
        boundary value: clusters <2, clusters = 2
        */
        setConfigValues(-1,200,100, true);
        Assert.assertEquals(new ConfigState(null, listofiterations[2],listofintervals[2] , listofclusters[2], new AtomicBoolean(true)), settings);
        /*
        boundary value: clusters >4, clusters = 4
        */
        setConfigValues(5,1,100, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[2], listofintervals[2], listofclusters[4], new AtomicBoolean(false)), settings);
        /*
        boundary value: updateInterval == 0 & maxIterations == 0, updateInterval = 1 & maxIterations = 100
        */
        setConfigValues(2, 0, 0, false);
        Assert.assertEquals(new ConfigState(null, listofiterations[2], listofintervals[2], listofclusters[2], new AtomicBoolean(false)), settings);      
    }
    

    private void setConfigValues(int clustersInput, int updateIntervalInput, int maxIterInput, boolean iscontinuous){
        if(clustersInput < 2) clusters = 2;
        else if(clustersInput > 4) clusters = 4;
        else clusters = clustersInput;
        settings.setLabels(clusters);
        
        if(maxIterInput <= 0 || maxIterInput > 5000) maxIterations = 100;
        else maxIterations = maxIterInput;
        settings.setIterations(maxIterations);
        
        if(updateIntervalInput <=0 || updateIntervalInput > maxIterations) updateInterval = 1;
        else updateInterval = updateIntervalInput;
        settings.setIntervals(updateInterval);
        
        this.iscontinuous = iscontinuous;
        settings.setContinuousState(iscontinuous);
    }
}

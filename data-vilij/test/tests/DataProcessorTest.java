/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import data.DataSet;
import dataprocessors.TSDProcessor;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import ui.ConfigState;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author David
 */
public class DataProcessorTest {

    private ApplicationTemplate applicationTemplate;
    private String  tsdString;
    private TSDProcessor processor;
    public DataProcessorTest() {
    }
    
    @Before
    public void setUp() {
        processor = new TSDProcessor(applicationTemplate);
        
    }
    
    @After
    public void tearDown() {
    }

    /*
    boundary value: incorrect data format
    */
    @Test(expected = InvalidDataNameException.class)
    public void testProcessString_invalidFormat() throws Exception {
        tsdString = "@instancelabel0,0";
        processor.processString(tsdString);
    }
    /*
    boundary value: multiple instances with same name
    */
    @Test(expected = InvalidDataNameException.class)
    public void testProcessString_duplicates() throws Exception {
        tsdString = "@instance\tlabel\t0,0\n@instance\tcategory\t5,5\n";
        processor.processString(tsdString);
        
    }
    
    /*
    boundary value: no string is given
    */
    @Test(expected = NullPointerException.class)
    public void testProcessString_null() throws Exception {
        tsdString = null;
        processor.processString(tsdString);
    }
    
    @Test
    public void testProcessString_pass() throws Exception{
        tsdString = "@instance\tlabel\t0,0\n";
        processor.processString(tsdString);
        Assert.assertEquals("label", processor.getData().getLabels().get("@instance"));
        Assert.assertEquals(new Point2D(0,0), processor.getData().getLocations().get("@instance"));
        
    }
}

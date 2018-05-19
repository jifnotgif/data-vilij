/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import dataprocessors.TSDProcessor;
import dataprocessors.TSDProcessor.InvalidDataNameException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author David
 */
public class SaveDataToFileTest {

    private ApplicationTemplate applicationTemplate;
    private Path fp;
    private String filepath;
    private String textArea;
    private TSDProcessor processor;

    public SaveDataToFileTest() {
    }

    @Before
    public void setUpClass() {
        filepath = "./data-vilij/resources/data/sample-data.tsd";
        fp = Paths.get(filepath);
        processor = new TSDProcessor(applicationTemplate);
    }

    @After
    public void tearDownClass() {
    }

    /**
     * Test of handleSaveRequest method, of class AppActions.
     *
     * @throws dataprocessors.TSDProcessor.InvalidDataNameException
     */
    @Test
    public void testHandleSaveRequest_pass() throws InvalidDataNameException {
        textArea = "@instance\tlabel\t1,1\n";
        processor.processString(textArea);

        try (FileWriter writer = new FileWriter(fp.toFile())) {
            writer.write(textArea);
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Assert.assertEquals("text saved successfully", textArea, new String(Files.readAllBytes(Paths.get(filepath))));
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test(expected = TSDProcessor.InvalidDataNameException.class)
    public void testHandleSaveRequest_invalidData() throws InvalidDataNameException {
        textArea = "@instancelabel1,1";
        processor.processString(textArea);

        try (FileWriter writer = new FileWriter(fp.toFile())) {
            writer.write(textArea);
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Assert.assertEquals("text saved successfully", textArea, new String(Files.readAllBytes(Paths.get(filepath))));
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test(expected = TSDProcessor.InvalidDataNameException.class)
    public void testHandleSaveRequest_noData() throws InvalidDataNameException {
        textArea = "";
        processor.processString(textArea);

        try (FileWriter writer = new FileWriter(fp.toFile())) {
            writer.write(textArea);
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Assert.assertEquals("text saved successfully", textArea, new String(Files.readAllBytes(Paths.get(filepath))));
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test(expected = NullPointerException.class)
    public void testHandleSaveRequest_nullData() throws InvalidDataNameException {
        textArea = null;
        processor.processString(textArea);

        try (FileWriter writer = new FileWriter(fp.toFile())) {
            writer.write(textArea);
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Assert.assertEquals("text saved successfully", textArea, new String(Files.readAllBytes(Paths.get(filepath))));
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test(expected = NullPointerException.class)
    public void testHandleSaveRequest_nullPath() throws InvalidDataNameException {
        fp = null;
        textArea = "@instance\tlabel\t1,1\n";
        processor.processString(textArea);

        try (FileWriter writer = new FileWriter(fp.toFile())) {
            writer.write(textArea);
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Assert.assertEquals("text saved successfully", textArea, new String(Files.readAllBytes(Paths.get(filepath))));
        } catch (IOException ex) {
            Logger.getLogger(SaveDataToFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

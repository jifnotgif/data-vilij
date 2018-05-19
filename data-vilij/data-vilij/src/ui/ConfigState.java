
package ui;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.control.Button;

/**
 *
 * @author David Doo
 */
public class ConfigState {
    
    private Button btn;
    private int iterations;
    private int intervals;
    private int labels;
    private AtomicBoolean continuousState;
    
    public ConfigState(Button btn, int iterations, int intervals, int labels, AtomicBoolean continuousState){
        this.btn = btn;
        this.iterations = iterations;
        this.intervals = intervals;
        this.labels = labels;
        this.continuousState = continuousState;
    }
    public ConfigState(Button btn, int iterations, int intervals, AtomicBoolean continuousState){
        this.btn = btn;
        this.iterations = iterations;
        this.intervals = intervals;
        this.continuousState = continuousState;
        this.labels = 0;
    }
    public ConfigState(){
        this.btn = null;
        this.iterations = 1000;
        this.intervals = 5;
        this.continuousState = new AtomicBoolean(true);
        this.labels = 0;
    }
    public Button getBtn() {
        return btn;
    }

    public void setBtn(Button btn) {
        this.btn = btn;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getIntervals() {
        return intervals;
    }

    public void setIntervals(int intervals) {
        this.intervals = intervals;
    }

    public int getLabels() {
        return labels;
    }

    public void setLabels(int labels) {
        this.labels = labels;
    }

    public boolean isContinuousState() {
        return continuousState.get();
    }

    public void setContinuousState(boolean continuousState) {
        this.continuousState.set(continuousState);
    }
    
    @Override
    public boolean equals(Object otherObject){
       if(this == otherObject) return true;
       if(otherObject == null) return false;
       if(this.getClass() != otherObject.getClass()) return false;
       return this.getIntervals() == ((ConfigState)otherObject).getIntervals() 
               && this.getIterations() == ((ConfigState)otherObject).getIterations()
               && this.getLabels() == ((ConfigState)otherObject).getLabels()
               && this.isContinuousState() == ((ConfigState)otherObject).isContinuousState();
    }
    
}

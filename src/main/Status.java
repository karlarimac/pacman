package main;

import java.io.Serializable;

/**
 *  Java PAcman Project - Chat App
*  @author Luka Djogas & Rimac Karla
 */

public class Status implements Serializable {
    private int iD;
    private int sliderStatus;
    

    public Status(int iD, int sliderStatus) {
        this.iD= iD;
        this.sliderStatus=sliderStatus;
    }

    public int getID() {
        return iD;
    }

    public int getSliderStatus() {
        return sliderStatus;
    }

    public String toString(){
        return "Status ID = " + iD + "sliderstatus= " + sliderStatus;
    }


    
}

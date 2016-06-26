/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hdl;

/**
 *
 * @author POWERUSER
 */
public class InOut {

    public String name, value, arrayName;
    public int index = -1;
    
    InOut(){    
    }
    
    InOut(String name, String value) {
        this.name = name;
        this.value = value;
        //check if name is an array. then set the length and the name of the array
        if(this.name.contains("[")) {
            index = Integer.valueOf(this.name.replaceAll("\\D*", ""));
            arrayName = name.replaceAll("\\[\\d*\\]", "");
        }
        else {
            arrayName = this.name;
        }
    }

    
    public String toString() {
        return name;
    }
    
}

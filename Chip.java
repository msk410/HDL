/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hdl;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author POWERUSER
 */
public class Chip {
    public String chipName;
    Map<Integer, Chip> chipParts = new HashMap<>();
    List<String> builtIns = new ArrayList<>(); //built in chips are and/or/not
    boolean canEval= false;
    
    Map<String, InOut> inputs = new HashMap<>();
    Map<String, InOut> outputs = new HashMap<>();
    Chip() {
        builtIns.add("And");
        builtIns.add("Or");
        builtIns.add("Not");
    }
    public void evalChip() {
        int chipNum = 0, size = this.chipParts.size();
        while(this.chipParts.size() != 0) {
            Chip tempChip;
            while(!this.chipParts.containsKey(chipNum)) {
                chipNum = (chipNum + 1) % size;
            }
            tempChip = this.chipParts.get(chipNum);
            tempChip.canEval = canEval(tempChip);
            if(tempChip.canEval) {
                eval(tempChip);
                this.chipParts.remove(chipNum);
            }
            else {
                chipNum++;
            }
        }
    }

    private boolean canEval(Chip tempChip) {
        //check to see if each input is assigned
        boolean evaluatable = false;
        for(Map.Entry<String,InOut> entry : tempChip.inputs.entrySet()) {
            InOut tempIO = entry.getValue();
            //first check in inputs
            if(this.inputs.containsKey(tempIO.arrayName)) {
                evaluatable = true;
            }//check in outputs
            else if(this.outputs.containsKey(tempIO.arrayName)) {
                evaluatable = true;
            }
            else {
                evaluatable = false;
                break;
            }
        }
        return evaluatable;
    }

    private void eval(Chip chip) {
        //eval and/or
        String outputValue = "";
        if(chip.chipName.equals("And") || chip.chipName.equals("Or")) {
            String a, b;
            a = getValue(chip.inputs.get("a").arrayName, "a", chip);
            b = getValue(chip.inputs.get("b").arrayName, "b", chip);
            if(chip.chipName.equals("And")) { //eval and
                outputValue = String.valueOf(Integer.parseInt(a) 
                        & Integer.parseInt(b));  
            }
            else if(chip.chipName.equals("Or")) { //eval or
                outputValue = String.valueOf(Integer.parseInt(a) 
                        | Integer.parseInt(b));          
            }
            builtInPutInOutputs(chip, outputValue, "out");
        }
        else if(chip.chipName.equals("Not")) { //eval not
            String in;
            in = getValue(chip.inputs.get("in").arrayName, "in", chip);
            outputValue = in.equals("1") ? "0":"1";    
            builtInPutInOutputs(chip, outputValue, "out");
        }
        else { //eval user def chip
            Chip userChip = new Chip(); //make a new chip
            Parser p = new Parser();
            String code = p.readFile(userChip, chip.chipName + ".hdl");
            p.initNameInOut(code, userChip);
            p.initParts(code, userChip);
            userChip.chipName = chip.chipName; 
            int outPosition = 0, inPosition = 0;
            //set the inputs for the user chip
            for(InOut userInput : userChip.inputs.values()) {
                
                if(userInput.index == -1) { //single bit
                    userInput.value = getValue(chip.inputs.
                            get(userInput.arrayName).name, userInput.name, chip);
                }
                else {
                    String value = "";
                    outPosition = userInput.index - chip.inputs.get(userInput.arrayName).index -1;
                    System.out.println(chip.inputs);
                    System.out.println(inPosition + " here");
                    if(userInput.value.equals("temp")) {
                        userInput.value = new String(new char[userInput.index]).replace("\0", "0");
                        char[] temp = userInput.value.toCharArray();
                        if(this.inputs.containsKey(userInput.arrayName)) {
                            System.out.println(this.inputs.get(userInput.arrayName).value);
                        }
                        
                        
                        //temp[inPosition] = getValue(chip.inputs.get(userInput.arrayName).name, userInput.arrayName, chip).charAt(0);
  
                    }
                }
            }/*              
                else { //multibit
                    char[] temp;
                    int inPosition = userInput.index -
                            chip.inputs.get(userInput.arrayName).index -1;
                    if(userInput.value.equals("temp")) {
                        userInput.value = new String(new char[userInput.index]).replace("\0", "0");
                        temp = userInput.value.toCharArray();
                        temp[inPosition] = getValue(chip.inputs.get(userInput.arrayName).name, userInput.arrayName, chip).charAt(0);
                        userInput.value = String.valueOf(temp);
                        System.out.println(userInput.arrayName + ": " + userInput.value);
                        userChip.inputs.put("sel", userInput);
                    }
                    else {
                        System.out.println("wtf?");
                        temp = userInput.value.toCharArray();
                        temp[inPosition] = getValue(chip.inputs.get(userInput.arrayName).name, userInput.arrayName, chip).charAt(0);
                        userInput.value = String.valueOf(temp);
                       // userChip.outputs.put(userInput.name, userInput);
                    }
                }
            }*/
            
            List<String> originalOutputs = new ArrayList<>();
            for(String ogOut : chip.outputs.keySet()) { //get original outputs and leave out any intermediate outputs
                originalOutputs.add(ogOut);
            }
            userChip.evalChip();
            Iterator it = userChip.outputs.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,InOut> entry = (Map.Entry)it.next();
                InOut temp = entry.getValue();
                if(originalOutputs.contains(temp.name)) {
                   temp.name = chip.outputs.get(temp.name).arrayName;
                   this.outputs.put(temp.name, temp);
                }
            }
        }

    }
    public void builtInPutInOutputs(Chip chip, String outputValue, String name) {
        InOut out = null;
        //set the output
        if(!this.outputs.containsKey(chip.outputs.get(name).arrayName)) {
            out = new InOut(chip.outputs.get(name).arrayName, outputValue);
            this.outputs.put(chip.outputs.get(name).arrayName, out);
        }
        else if(this.outputs.get(chip.outputs.get(name).arrayName).value.equals("temp")) { 
            String pad;
            char[] charOut = outputValue.toCharArray();
            if(chip.outputs.get(name).index != -1) { //check if its an array
                int bit = this.outputs.get(name).index - chip.outputs.get(name).index -1;
                pad =  new String(new char[this.outputs.get(chip.outputs.get(name).arrayName).index]).replace("\0", "0");
                charOut = pad.toCharArray();
                charOut[bit] = outputValue.charAt(0);
            }
            out = new InOut(chip.outputs.get(name).arrayName, new String(charOut));
            out.index = this.outputs.get(chip.outputs.get(name).arrayName).index;
            this.outputs.put(chip.outputs.get(name).arrayName, out);
        }
        else {
            InOut temp = new InOut();
            int bit = this.outputs.get(name).index - chip.outputs.get(name).index -1;
            temp = this.outputs.get(chip.outputs.get(name).arrayName);
            char[] charOut = temp.value.toCharArray();
            charOut[bit] = outputValue.charAt(0);
            temp.value = new String(charOut);
            this.outputs.put(chip.outputs.get(name).arrayName, temp);
        }
        
    }
    public String getValue(String outputName, String inputName, Chip chip) {
        String value = "";
        int bitPosition = 0;  
        //try to get from this.inputs
        if(this.inputs.containsKey(outputName.split("\\[")[0])) {
            //check if input is 1 bit from array
            if(this.inputs.get(outputName.split("\\[")[0]).name.contains("[") 
                    && this.inputs.get(outputName.split("\\[")[0]).index > chip.inputs.get(inputName).index) {
                bitPosition = this.inputs.get(outputName.split("\\[")[0]).index - chip.inputs.get(inputName).index -1;
                
                value = String.valueOf(this.inputs.get(outputName.split("\\[")[0]).value.charAt(
                        bitPosition));
            }
            else {
                value = this.inputs.get(outputName.split("\\[")[0]).value;
            }
        }
        //try to get from this.outputs
        else {
            if(this.outputs.get(outputName.split("\\[")[0]).name.contains("[")
                    && this.outputs.get(outputName.split("\\[")[0]).index > chip.outputs.get(inputName).index) {
                bitPosition = this.inputs.get(outputName.split("\\[")[0]).index - chip.inputs.get(inputName).index -1;  
                value = String.valueOf(this.outputs.get(outputName.split("\\[")[0]).value.charAt(
                        bitPosition));
            }
            else {
                value = this.outputs.get(outputName.split("\\[")[0]).value;
            }
        }
        return value;
    }
}
    
    
    
    
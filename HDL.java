/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hdl;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author POWERUSER
 */
public class HDL {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        Scanner kb = new Scanner(System.in);
        System.out.print("Enter name of chip: ");
        String chipName = kb.nextLine();
        Chip myChip = new Chip();
        Parser myParser = new Parser();
        String code = myParser.readFile(myChip, chipName);
        myParser.initNameInOut(code, myChip);
        myParser.initParts(code, myChip);
        getInputs(myChip);
  //      System.out.println(myChip.chipParts.get(0).inputs);
        myChip.evalChip();
        System.out.println("***OUTPUTS***");
        Iterator it = myChip.outputs.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String,InOut> entry = (Map.Entry)it.next();
            InOut temp = entry.getValue();
            System.out.println(temp.name + " " + temp.value);
        }
        
    }
    public static void getInputs(Chip myChip) {
        Iterator it = myChip.inputs.entrySet().iterator();
        Scanner kb = new Scanner(System.in);
        while(it.hasNext()) {
            Map.Entry<String,InOut> entry = (Map.Entry)it.next();
            InOut temp = new InOut();
            temp = entry.getValue();
            System.out.print("enter input " + temp.name + ": ");
            String input = kb.nextLine();
            input = pad(input, temp.index, myChip);
            InOut in = new InOut(temp.name, input);
            myChip.inputs.put(entry.getKey(), in);
        }
        
    }
    //pad input with 0's
    public static String pad(String input, int length, Chip myChip) { 
        if(input.length() > length && length !=-1) {
            System.out.println("Too many bits. Enter again.");
            getInputs(myChip);
        }
        else if(length != -1) {
            input =  new String(new char[length - input.length()]).replace("\0", "0") + input;
        }
        return input;
    }
}
  
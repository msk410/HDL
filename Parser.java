package hdl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author POWERUSER
 */
public class Parser {
    public String readFile(Chip myChip, String name) { //reads the file
        URL path = HDL.class.getResource(name);
        
        String line = "";
        String text = "";
        try {
            FileReader fr = new FileReader(path.getFile()); //reads file at given path
            BufferedReader br = new BufferedReader(fr);
            while((line = br.readLine()) != null ) {
                text = text.concat(line + "\n");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        String cleanCode = text.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "" ); //remove comments to make my life easier
        
        return cleanCode;
    }
    
    public static void initNameInOut(String text, Chip chip) {  //initializes chips in and outs, name
        String namePattern = "\\s*[A-Z]+[a-z0-9]*\\s+([A-Z][a-z0-9]+)\\s+[{]";
        Pattern r = Pattern.compile(namePattern, Pattern.DOTALL);
        Matcher nameMatch = r.matcher(text);
        if(nameMatch.find()) {
            chip.chipName = nameMatch.group(1).trim();
        }
        String inOutPattern = "[A-Z]+\\s*[a-zA-Z0-9]+(\\[(\\d+)\\]){0,1}\\s*(,\\s*\\w+(\\[[1-9]+\\d*\\]){0,1})*\\s*;";
        
        r = Pattern.compile(inOutPattern, Pattern.DOTALL);
        Matcher m = r.matcher(text);
        while (m.find()) {
            if(m.group().contains("IN")) { //initializes chips ins
                String temp = m.group().replaceAll("IN|OUT|\\s*", ""); //removes in, out, spaces
                String[] ins = temp.replaceAll(";", "").split(","); //removes ; and splits string by ,
                for(String input : ins) {
                    InOut inOut = new InOut(input, "temp");
                   // chip.inputs.put(input.replaceAll("\\[\\d*\\]", ""), inOut);
                   String inName = input.replaceAll("\\[\\d*\\]", "");
                   chip.inputs.put(inName, inOut);
                }
            }
            if(m.group().contains("OUT")) {
                String temp = m.group().replaceAll("IN|OUT|\\s*", ""); //removes in, out, spaces
                String[] outs = temp.replaceAll(";", "").split(","); //removes ; and splits string by ,
                for(String output : outs) {
                    InOut inOut = new InOut(output, "temp");
                    String outName = output.replaceAll("\\[\\d*\\]", "");
                    //chip.outputs.put(output.replaceAll("\\[\\d*\\]", ""), inOut);
                    chip.outputs.put(outName, inOut);
                }
            }
        }
    }

    public static void initParts(String text, Chip myChip) {
        String partsList = "PARTS:\\s*([^}]*)"; //get list of parts group 1
        String partsPattern = "([A-Z]+[A-Za-z0-9]+)\\(([A-Za-z=,\\s\\d\\[\\]]+)\\)\\s*;"; //get chip name(group 1) and chip parts(group 2)
        String[] temp;
        Pattern r = Pattern.compile(partsList, Pattern.DOTALL);
        Matcher m = r.matcher(text);
        if(m.find()) {
            String partsParts = m.group(1).replace(" ", "");
            r = Pattern.compile(partsPattern, Pattern.DOTALL);
            m = r.matcher(partsParts);
            Integer num = 0;
            while(m.find()) {
                Chip tempChip = new Chip();
                Chip userChip = null;
                tempChip.chipName = m.group(1);
                temp = m.group(2).split(",");
                Pattern re;
                Matcher ma;
                if(!(tempChip.chipName.equals("And") || tempChip.chipName.equals("Or") || tempChip.chipName.equals("Not"))) {
                    userChip = new Chip();
                    userChip.chipName = tempChip.chipName;
                    Parser p = new Parser();
                    String code = p.readFile(userChip, tempChip.chipName + ".hdl");
                    p.initNameInOut(code, userChip);
                }
                for(String t: temp) { //initialize each chips ins  
                    if(tempChip.chipName.equals("And") || tempChip.chipName.equals("Or") ) { //and/or chip
                      String inPattern = "((a|b)(\\[\\d+\\])?)\\s*=((\\w+)(\\[\\d+\\])?)";
                        re = Pattern.compile(inPattern);
                        ma = re.matcher(t);
                        while(ma.find()) {
                            InOut inOut = new InOut(ma.group(4), "temp");
                            tempChip.inputs.put(ma.group(1), inOut);
                        }
                        String outPattern = "out\\s*=((\\w+)(\\[\\d+\\])?)";
                        re = Pattern.compile(outPattern);
                        ma = re.matcher(t);
                        while(ma.find()) {
                            InOut inOut = new InOut(ma.group(1), "temp");
                            tempChip.outputs.put("out", inOut);
                            
                        }
                    }
                    else if(tempChip.chipName.equals("Not")) { //not chip
                        String inPattern = "((in)(\\[\\d+\\])?)=((\\w+)(\\[\\d+\\])?)";
                        re = Pattern.compile(inPattern);
                        ma = re.matcher(t);
                        while(ma.find()) {
                          //  InOut inOut = new InOut(ma.group(1), ma.group(4));
                          InOut inOut = new InOut(ma.group(4), "temp");
                          tempChip.inputs.put("in", inOut);
                        }
                        String outPattern = "out=((\\w+)(\\[\\d+\\])?)";
                        re = Pattern.compile(outPattern);
                        ma = re.matcher(t);
                        while(ma.find()) {
                            InOut inOut = new InOut(ma.group(1), "temp");
                            tempChip.outputs.put("out", inOut);
                        }
                    }
                    else { //user chip
                        InOut inOut;
                        String var = t.split("=")[0];
                        String assign = t.split("=")[1];
                        //if(userChip.inputs.containsKey(t.split("=")[0].trim())) {
                        if(userChip.inputs.containsKey(var.split("\\[")[0])) {
                            inOut = new InOut(assign, "temp");
                            tempChip.inputs.put(var, inOut);
                        }
                        else if(userChip.outputs.containsKey(var.split("\\[")[0].trim())) {
                            inOut = new InOut(assign, "temp");
                            tempChip.outputs.put(var, inOut);
                        }
                        
                    }
                }
                myChip.chipParts.put(num, tempChip);
                
                num++;
            }
        }
    }
    
}

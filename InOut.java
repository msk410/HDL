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

package platis.solutions.smartcarmqttcontroller.Model;

import java.io.Serializable;

public class Item implements Serializable {
    private String input;
    private int itemID;
    private static final long serialVersionUID = 10L;

    public Item(String input, int itemID) {
        this.input = input;
        this.itemID = itemID;
    }

    public Item() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }
}

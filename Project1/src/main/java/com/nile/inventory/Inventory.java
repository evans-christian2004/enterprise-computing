package com.nile.inventory;
import java.util.HashMap;
import com.nile.storeItem.storeItem;

import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Inventory {
    String path;
    HashMap<String, storeItem> inventory;
    int size;

    public Inventory(String path){
        this.path = path;
        String line = "";
        inventory = new HashMap<String, storeItem>();

        // reads a given csv file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                // splits each line by comma, trims it, and casts each string to its respective type
                String ID = line.split(",")[0].trim();
                String description = line.split(",")[1];
                boolean isInStock = Boolean.parseBoolean(line.split(",")[2].trim());
                int stock = Integer.parseInt(line.split(",")[3].trim());
                double price = Double.parseDouble(line.split(",")[4].trim());

                // creates an item using the parsed values, then adds it to the inventory
                storeItem item = new storeItem(ID, description, isInStock, stock, price);
                inventory.put(item.ID, item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns an item from the inventory
    public storeItem getByID(String ID) {
        return inventory.get(ID);
    }
}


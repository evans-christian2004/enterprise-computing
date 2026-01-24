package com.nile;
import com.nile.inventory.Inventory;

public class Main {
    public static void main(String[] args) {
        Inventory nileInventory = new Inventory("src/inventory.csv");

        System.out.println(nileInventory.getByID("4452").description);
    }
}
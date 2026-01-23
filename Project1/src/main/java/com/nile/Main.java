package com.nile;
import com.nile.inventory.Inventory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Inventory nileInventory = new Inventory("src/inventory.csv");
    }
}
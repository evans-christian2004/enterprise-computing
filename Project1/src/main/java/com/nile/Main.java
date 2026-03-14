package com.nile;
import com.nile.Cart.Cart;
import com.nile.GUI.GUI;
import com.nile.inventory.Inventory;

/*
    Name: Christian Evans
    Course: CNT 4714 - Spring 2026
    Assignment Title: Project 1 - An Event-driven Enterprise Simulation
    Date: Saturday January 31, 2026
    THIS IS A MAVERN PROJECT, ALL PACKAGES ARE MANAGED THROUGH MAVERN
*/

public class Main {
    public static void main(String[] args) {
        double[] discounts = {0.0, .1, .15, .2};
        double taxRate = 0.06;
        Inventory nileInventory = new Inventory("src/inventory.csv", discounts);
        Cart userCart = new Cart(5, nileInventory);

        GUI gui = new GUI(nileInventory, userCart, taxRate);
    }
}

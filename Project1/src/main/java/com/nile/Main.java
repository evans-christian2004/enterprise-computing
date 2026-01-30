package com.nile;
import com.nile.Cart.Cart;
import com.nile.GUI.GUI;
import com.nile.inventory.Inventory;

public class Main {
    public static void main(String[] args) {
        double[] discounts = {0.0, .1, .15, .2};
        Inventory nileInventory = new Inventory("src/inventory.csv", discounts);
        Cart userCart = new Cart(5, nileInventory);

        GUI gui = new GUI(nileInventory, userCart);
    }
}
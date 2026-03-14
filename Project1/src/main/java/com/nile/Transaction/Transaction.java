package com.nile.Transaction;

import com.nile.Cart.Cart;
import com.nile.Cart.CartItem;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {
    LocalDateTime time;
    Inventory inventory;
    Cart cart;
    long transactionID;
    double taxRate;

    public Transaction(Cart cart, Inventory inventory, double taxRate){
        this.time = LocalDateTime.now();
        this.inventory = inventory;
        this.cart = cart;
        this.taxRate = taxRate;
        this.transactionID = Long.parseLong(String.format("%d%d%d%d%d%d", time.getDayOfMonth(), time.getMonthValue(), time.getYear(), time.getHour(), time.getMinute(), time.getSecond()));
    }

    public long getTransactionID() {
        return transactionID;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String[] getTransactionString(){
        int len = cart.getItems().length;
        String[] ret = new String[len];
        for (int i = 0; i < len; i++){
            CartItem item = cart.getItems()[i];
            StoreItem product = item.getProduct();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, h:mm:ss a z", Locale.ENGLISH).withZone(ZoneId.systemDefault());
            ret[i] = transactionID + ", " + product.getID() + ", " +
                    product.description + ", " + product.price + ", " +
                    item.getQuantity() + ", " + item.getDiscount(product) + ", " +
                    item.formatPrice() + ", " + time.format(formatter);
        }
        return ret;
    }
}

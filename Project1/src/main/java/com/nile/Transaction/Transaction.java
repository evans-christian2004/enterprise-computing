package com.nile.Transaction;

import com.nile.Cart.Cart;
import com.nile.Cart.CartItem;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {
    LocalDateTime time;
    Inventory inventory;
    Cart cart;
    int transactionID;

    Transaction(Cart cart, Inventory inventory){
        this.time = LocalDateTime.now();
        this.inventory = inventory;
        this.cart = cart;
        this.transactionID = Integer.parseInt(String.format("%d%d%d%d%d%d", time.getDayOfMonth(), time.getMonthValue(), time.getYear(), time.getHour(), time.getMinute(), time.getSecond()));
    }

    public int getTransactionID() {
        return transactionID;
    }

    public String[] getTransactionString(){
        int len = cart.getItems().length;
        String[] ret = new String[len];
        for (int i = 0; i < len; i++){
            CartItem item = cart.getItems()[i];
            StoreItem product = item.getProduct();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, h:mm:ss a z", Locale.ENGLISH);
            ret[i] = transactionID + ", " + product.getID() + ", " +
                    product.description + ", " + product.price + ", " +
                    item.getQuantity() + ", " + item.getDiscount(product) + ", " +
                    item.formatPrice() + ", " + time.getMonth() + " " +
                    time.getDayOfMonth() + ", " + time.getYear() + ", " +
                    time.format(formatter);
        }
        return ret;
    }
}

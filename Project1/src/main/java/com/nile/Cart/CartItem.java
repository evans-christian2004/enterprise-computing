package com.nile.Cart;
import com.nile.inventory.StoreItem;

public class CartItem {
    StoreItem product;
    int quantity;
    double itemPrice;

    public CartItem(StoreItem product, int quantity){
        if(!product.isInStock){
            throw new RuntimeException("this item is not in stock and cannot be turned into a cart item");
        } else {
            this.product = product;
            this.quantity = quantity;
            this.itemPrice = quantity * product.price;
        }
    }

    public String toString(){
       return("\"" + product.description + "\", price ea. " + Double.toString(product.price) + ", qty: " + Integer.toString(quantity));
    }
}



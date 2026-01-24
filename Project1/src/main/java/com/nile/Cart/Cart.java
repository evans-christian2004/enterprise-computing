package com.nile.Cart;
import com.nile.inventory.StoreItem;

import java.util.Stack;

public class Cart {
    private Stack<CartItem> cartItems;
    private double cartTotal;
    private int maxCartSize;

    Cart(int maxCartSize){
        cartItems = new Stack<CartItem>();
        cartTotal = 0.00;
        this.maxCartSize = maxCartSize;
    }

    public boolean addToCart(StoreItem selectedItem, int quantity){
        if(!selectedItem.isInStock || cartItems.size() >= maxCartSize){
            return false;
        } else {
            CartItem item = new CartItem(selectedItem, quantity);
            cartItems.push(item);
            cartTotal+=item.itemPrice;
            return true;
        }
    }

    public double getCartTotal(){
        return cartTotal;
    }

    public CartItem[] getItems(){
        return (CartItem[]) cartItems.toArray();
    }
}

package com.nile.Cart;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

import java.util.Stack;

public class Cart {
    private Stack<CartItem> cartItems;
    private double cartTotal;
    private int maxCartSize;

    public Cart(int maxCartSize){
        cartItems = new Stack<CartItem>();
        cartTotal = 0.00;
        this.maxCartSize = maxCartSize;
    }

    public boolean addToCart(Inventory inventory, StoreItem selectedItem, int quantity){
        if(!selectedItem.isInStock || cartItems.size() >= maxCartSize){
            return false;
        } else {
            CartItem item = new CartItem(inventory, selectedItem, quantity);
            cartItems.push(item);
            cartTotal+=item.itemPrice;
            return true;
        }
    }

    public boolean removeFromCart(){
        if (cartItems.size() <= 0){
            return false;
        } else {
            CartItem temp = cartItems.pop();
            cartTotal -= temp.itemPrice;
            return true;
        }
    }

    public double getCartTotal(){
        return cartTotal;
    }

    public CartItem[] getItems(){
        return (CartItem[])cartItems.toArray();
    }
}

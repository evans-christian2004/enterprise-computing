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

    public boolean addToCart(CartItem itemToAdd){
        if(!itemToAdd.product.isInStock || cartItems.size() >= maxCartSize){
            return false;
        } else {
            cartItems.push(itemToAdd);
            cartTotal+=itemToAdd.product.price;
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
        return cartItems.toArray(new CartItem[0]);
    }
}

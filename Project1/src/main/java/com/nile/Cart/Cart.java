package com.nile.Cart;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

public class Cart {
    private Inventory inventory;
    private Stack<CartItem> cartItems;
    private double cartTotal;
    private int maxCartSize;

    public Cart(int maxCartSize, Inventory inventory){
        this.inventory = inventory;
        cartItems = new Stack<CartItem>();
        cartTotal = 0.00;
        this.maxCartSize = maxCartSize;
    }

    public boolean addToCart(CartItem itemToAdd){
        if(!itemToAdd.product.isInStock || cartItems.size() >= maxCartSize){
            return false;
        } else {
            cartItems.push(itemToAdd);
            cartTotal = 0.00;
            for (CartItem c: cartItems){
                cartTotal += c.itemPrice;
            }
            return true;
        }
    }

    public boolean removeFromCart(){
        if (cartItems.size() <= 0){
            return false;
        } else {
            CartItem temp = cartItems.pop();
            StoreItem updateStock = inventory.getByID(temp.product.getID());
            updateStock.setStock(updateStock.getStock() + temp.getQuantity());
            cartTotal = 0.00;
            for (CartItem c : cartItems){
                cartTotal += c.itemPrice;

            }
            return true;
        }
    }

    public void emptyCart(boolean resetInventory){
        if (resetInventory){
            for (int i = 0; i < cartItems.size(); i++){
                removeFromCart();
            }
        } else {
            cartItems.clear();
            cartTotal = 0.00;
        }

    }


    public BigDecimal getCartTotal(){
        BigDecimal format = new BigDecimal(cartTotal);
        return format.setScale(2, RoundingMode.HALF_UP);
    }
    public int getMaxCartSize() {
        return maxCartSize;
    }

    public CartItem[] getItems(){
        return cartItems.toArray(new CartItem[0]);
    }
}

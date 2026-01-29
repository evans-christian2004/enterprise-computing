package com.nile.GUI;

import com.nile.Cart.Cart;
import com.nile.Cart.CartItem;

public class State {
    private CartItem pendingItem;

    public State() {
        this.pendingItem = null;
    }

    public void setState(CartItem selectedItem){
        pendingItem = selectedItem;
    }

    public CartItem getState(){
        return pendingItem;
    }
}

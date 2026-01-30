package com.nile.Cart;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

public class CartItem {
    Inventory inventory;
    StoreItem product;
    int quantity;
    double itemPrice;

    public CartItem(Inventory inventory, StoreItem product, int quantity){
        if(!product.isInStock){
            throw new RuntimeException("this item is not in stock and cannot be turned into a cart item");
        } else {
            this.inventory = inventory;
            this.product = product;
            this.quantity = quantity;
            this.itemPrice = quantity * product.price * (1 - getDiscount(product));
        }
    }

    // implimenting a cloneing constructor so only copies are stored in the state, idk if im using it yet
    public CartItem(CartItem original){
        this.inventory = original.inventory;
        this.product = original.product;
        this.quantity = original.quantity;
        this.itemPrice = original.itemPrice;
    }

    public double getDiscount(StoreItem product){
        if (quantity >= 1 && quantity <= 4) {
            return inventory.getDiscounts()[0];
        } else if (quantity >= 5 && quantity <= 9) {
            return inventory.getDiscounts()[1];
        } else if (quantity >= 10 && quantity <= 14) {
            return inventory.getDiscounts()[2];
        } else {
            return inventory.getDiscounts()[3];
        }
    }

    public String toPreviewString(){
       return (product.getID() + " " + product.description + " " + Double.toString(product.price) + " " + Integer.toString(quantity) + " " + (int)(100 * getDiscount(product)) + "% $" + itemPrice);
    }

    public StoreItem getProduct(){
        return product;
    }

    public String toCartItemString() {
        return ("SKU: " + product.getID() + ", Desc: " + product.description + ", Price Ea. " + Double.toString(product.price) + ", Qty: " + Integer.toString(quantity) + ", Total: $" + itemPrice);
    }

    public int getQuantity(){
        return quantity;
    }
}



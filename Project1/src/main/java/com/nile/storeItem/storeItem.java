package com.nile.storeItem;

public class storeItem {
   public String ID;
   public String description;
   public boolean isInStock;
   public int stock;
   public double price;

   public storeItem(String ID, String description,boolean isInStock, int stock,double price){
       this.ID = ID;
       this.description = description;
       this.isInStock = isInStock;
       this.stock = stock;
       this.price = price;
   }
}
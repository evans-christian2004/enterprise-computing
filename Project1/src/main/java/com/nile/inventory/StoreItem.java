package com.nile.inventory;

public class StoreItem {
   private String ID;
   public String description;
   public boolean isInStock;
   private int stock;
   public double price;

   public StoreItem(String ID, String description, boolean isInStock, int stock, double price){
       this.ID = ID;
       this.description = description;
       this.isInStock = isInStock;
       this.stock = stock;
       this.price = price;
   }

   public String getID(){
       return ID;
   }
}
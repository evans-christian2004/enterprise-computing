# Enterprise Computing projects

---

These are all the projects and assigments I'm doing in my Enterprise Computing class. I'm taking this opportunity to get better at writing java applications.

## Project 1: Nile.com
This projects is to basically create a storefront that allows the user to search items in a CSV file given with an item ID, name, price, etc.

### Concepts that I need to learn for the projects:
- GUIs with Java standard lib
- File IO
- object oriented programming overall because I didn't actually make anything in my object oriented programming class (thanks college education)

# Accomplishments
Successfully created the GUI. Most of the difficulty was making sure to keep track of the storeItems once they were loaded into the inventory data structure and managing that when the user adds or deletes items from their cart.

The user cannot allow items to continuously be added to the cart after the inventory is out of stock for the item, but the inventory is not actually out of those items because the user hasn't checked out yet.
- I created a `state` variable that keeps track of the current inventory, whether the user has checked out, and what the current items are in the users cart
- Functions were created for clearing the cart without updating inventory after items are removed from the inventory and added to the cart (for when the user checks out and wants to make another transaction), and for clearing the cart and updating the inventory to "put items back" if the user decides to clear the cart without checking out.

I implimented an inventory tracking system I created and `Inventory` class that:
1. sources data from the CSV file in the project
2. Creates a 

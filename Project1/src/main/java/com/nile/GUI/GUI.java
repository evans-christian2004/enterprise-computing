package com.nile.GUI;

import com.nile.Cart.Cart;
import com.nile.Cart.CartItem;
import com.nile.inventory.Inventory;
import com.nile.inventory.StoreItem;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GUI extends JFrame implements ActionListener {
    private final int FEILD_WIDTH = 35;
    private Cart cart;
    private Inventory inventory;
    private State state;

    // --- components ---
    private JLabel itemInputLabel = new JLabel("Enter item ID for Item #1:");
    private JTextField itemInputField = new JTextField(FEILD_WIDTH);

    private JLabel itemQuantityInputLabel = new JLabel("Enter quantity for Item #1:");
    private JTextField itemQuantityInputField = new JTextField(FEILD_WIDTH);

    private JLabel itemDetailsLabel = new JLabel("Details for Item #1:");
    private JTextField itemDetailsField = new JTextField(FEILD_WIDTH);

    private JLabel subtotalLabel = new JLabel("Current Subtotal for 0 item(s):");
    private JTextField subtotalField = new JTextField(FEILD_WIDTH);

    private JLabel cartHeader = new JLabel("Your Shopping Cart Is Currently Empty", SwingConstants.CENTER);

    private JTextField cartField1 = new JTextField();
    private JTextField cartField2 = new JTextField();
    private JTextField cartField3 = new JTextField();
    private JTextField cartField4 = new JTextField();
    private JTextField cartField5 = new JTextField();

    private JButton searchButton = new JButton("Search For Item #1");
    private JButton addToCartButton = new JButton("Add Item #1 To Cart");
    private JButton deleteButton = new JButton("Delete Last Item From Cart");
    private JButton checkoutButton = new JButton("Check Out");
    private JButton emptyCartButton = new JButton("Empty Cart – Start A New Order");
    private JButton exitButton = new JButton("Exit (Close App)");

    public GUI(Inventory inventory, Cart cart) {
        this.inventory = inventory;
        this.cart = cart;
        this.state = new State();

        super("Nile.Com - SPRING 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Parent layout: top / center / bottom
        setLayout(new BorderLayout(0, 0));

        add(buildTopInputsPanel(), BorderLayout.NORTH);
        add(buildCartPanel(), BorderLayout.CENTER);
        add(buildControlsPanel(), BorderLayout.SOUTH);

        // initial states similar to screenshot
        addToCartButton.setEnabled(false);
        deleteButton.setEnabled(false);
        checkoutButton.setEnabled(false);

        // listeners
        searchButton.addActionListener(this);
        addToCartButton.addActionListener(this);
        deleteButton.addActionListener(this);
        checkoutButton.addActionListener(this);
        emptyCartButton.addActionListener(this);
        exitButton.addActionListener(this);

        setSize(1100, 800);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildTopInputsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(55, 55, 55));
        p.setBorder(new EmptyBorder(25, 40, 25, 40));

        // label colors like screenshot
        itemInputLabel.setForeground(new Color(230, 210, 0));      // yellow
        itemQuantityInputLabel.setForeground(new Color(230, 210, 0));
        itemDetailsLabel.setForeground(new Color(0, 180, 220));    // cyan
        subtotalLabel.setForeground(new Color(0, 180, 220));

        // fields styling
        itemDetailsField.setEditable(false);
        subtotalField.setEditable(false);
        itemDetailsField.setBackground(new Color(210, 210, 210));
        subtotalField.setBackground(new Color(210, 210, 210));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // row 1
        gbc.gridx = 0; gbc.gridy = 0;
        p.add(itemInputLabel, gbc);
        gbc.gridx = 1;
        p.add(itemInputField, gbc);

        // row 2
        gbc.gridx = 0; gbc.gridy = 1;
        p.add(itemQuantityInputLabel, gbc);
        gbc.gridx = 1;
        p.add(itemQuantityInputField, gbc);

        // row 3
        gbc.gridx = 0; gbc.gridy = 2;
        p.add(itemDetailsLabel, gbc);
        gbc.gridx = 1;
        p.add(itemDetailsField, gbc);

        // row 4
        gbc.gridx = 0; gbc.gridy = 3;
        p.add(subtotalLabel, gbc);
        gbc.gridx = 1;
        p.add(subtotalField, gbc);

        return p;
    }

    private JPanel buildCartPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(Color.BLACK);

        cartHeader.setForeground(Color.RED);
        cartHeader.setFont(cartHeader.getFont().deriveFont(Font.BOLD, 24f));
        cartHeader.setBorder(new EmptyBorder(20, 10, 20, 10));
        p.add(cartHeader, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Color.BLACK);
        list.setBorder(new EmptyBorder(10, 20, 20, 20));

        JTextField[] fields = { cartField1, cartField2, cartField3, cartField4, cartField5 };
        for (JTextField f : fields) {
            f.setEditable(false);
            f.setPreferredSize(new Dimension(900, 45));
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            f.setBorder(new LineBorder(Color.DARK_GRAY, 1));
            list.add(f);
            list.add(Box.createRigidArea(new Dimension(0, 14))); // black gap like screenshot
        }

        p.add(list, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildControlsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(120, 80, 10)); // brown
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("USER CONTROLS");
        title.setForeground(new Color(230, 210, 0));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 40f));
        title.setBorder(new EmptyBorder(0, 10, 15, 10));
        p.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 16));
        grid.setOpaque(false);

        // Make buttons feel “wide”
        Dimension btnSize = new Dimension(420, 45);
        for (JButton b : new JButton[]{searchButton, addToCartButton, deleteButton, checkoutButton, emptyCartButton, exitButton}) {
            b.setPreferredSize(btnSize);
            b.setFocusPainted(false);
            b.setFont(b.getFont().deriveFont(Font.PLAIN, 18f));
        }

        grid.add(searchButton);
        grid.add(addToCartButton);
        grid.add(deleteButton);
        grid.add(checkoutButton);
        grid.add(emptyCartButton);
        grid.add(exitButton);

        p.add(grid, BorderLayout.CENTER);
        return p;
    }

   public JTextField[] getCartFields(){
       JTextField[] fields = { cartField1, cartField2, cartField3, cartField4, cartField5 };
       return fields;
   }

   public void updateFields(){
       CartItem[] currentCart = cart.getItems();

       // updating item input text
       itemInputLabel.setText("Enter item ID for item #" + (currentCart.length+1) + ":");
       itemInputField.setText("");
       itemQuantityInputLabel.setText("Enter quantity for item #" + (currentCart.length+1) + ":");
       itemQuantityInputField.setText("");

       // updating subtotal text
       subtotalLabel.setText("Current Subtotal for " + currentCart.length + " Item(s)");
       subtotalField.setText("$" + cart.getCartTotal());

       // updating Cart header text
       if(currentCart.length > 0){
           cartHeader.setText("Your Shopping Cart Currently Contains " + currentCart.length + " Item(s)");
       } else {
           cartHeader.setText("Your Shopping Cart is Currently Empty");
       }

       // updating cart display
       for (int i = 0; i < cart.getMaxCartSize(); i++){
           if (i < currentCart.length){
               getCartFields()[i].setText("  Item " + (i+1) + " - " + currentCart[i].toCartItemString());
           } else {
               getCartFields()[i].setText("");
           }

       }

       // updating buttons
       searchButton.setText("Search for Item #" + (currentCart.length+1));
       addToCartButton.setText("Add Item #" + (currentCart.length+1) + " To Cart");
       if (currentCart.length > 0){
           deleteButton.setEnabled(true);
           checkoutButton.setEnabled(true);
       } else {
           deleteButton.setEnabled(false);
           checkoutButton.setEnabled(false);
       }
       if (state.getState() == null){
           searchButton.setEnabled(true);
           addToCartButton.setEnabled(false);
       }
   }

   public void reset(){
        state.setState(null);
        cart.emptyCart(true);
        updateFields();
   }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exitButton) {
            dispose();
        }

        if (e.getSource() == searchButton) {
           String IDinput = itemInputField.getText();
           String quantInput = itemQuantityInputField.getText();

           if (IDinput.isEmpty() || quantInput.isEmpty()){
               JOptionPane.showMessageDialog(
                       this,
                       "Please Enter an Item ID and a Quantity"
               );
           } else if (Integer.parseInt(quantInput) < 0) {
               JOptionPane.showMessageDialog(
                       this,
                       "quantities have to be positive"
               );
           } else {
               StoreItem selectedItem = inventory.getByID(IDinput);

               if (selectedItem == null) {
                   JOptionPane.showMessageDialog(
                           this,
                           "Item ID " + IDinput + " is not on file"
                   );
               } else if(!selectedItem.isInStock) {
                   JOptionPane.showMessageDialog(
                           this,
                           "Sorry... That item is out of stock, please try another item."
                   );
               } else if (selectedItem.getStock() < Integer.parseInt(quantInput)) {
                   JOptionPane.showMessageDialog(
                           this,
                           "Insufficient stock, only " + selectedItem.getStock() + " on hand. Please reduce the quantity."
                   );
               } else {

                   CartItem itemToDisplay = new CartItem(inventory, selectedItem, Integer.parseInt(quantInput));
                   state.setState(itemToDisplay);
                   itemToDisplay.getProduct().setStock(itemToDisplay.getProduct().getStock() - itemToDisplay.getQuantity());
                   itemDetailsLabel.setText("Details for item #" + (cart.getItems().length+1) + ":");
                   itemDetailsField.setText(itemToDisplay.toPreviewString());

                   addToCartButton.setEnabled(true);
                   searchButton.setEnabled(false);
               }

           }
        }

        if (e.getSource() == addToCartButton) {
            cart.addToCart(state.getState());
            state.setState(null);

            updateFields();
        }

        if (e.getSource() == emptyCartButton){
            reset();
        }

        if (e.getSource() == deleteButton){
            cart.removeFromCart();

            updateFields();
        }


        // hook up the rest of your logic (delete/checkout/empty)
    }
}

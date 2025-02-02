package view;

import model.entities.User;
import util.FirebaseInitializer;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private JPanel cards;
    private CardLayout cardLayout;
    private User currentUser;

    public MainApp() {
        super("Notes Vault");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setLayout(cardLayout);

        //Agregamos los paneles
        cards.add(new LoginPanel(this), "login");
        cards.add(new RegisterPanel(this), "register");
        cards.add(new DashboardPanel(this), "dashboard");

        add(cards);
        setVisible(true);
    }

    public void showPanel(String panelName){
        cardLayout.show(cards, panelName);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        FirebaseInitializer.initialize();
        SwingUtilities.invokeLater(MainApp::new);
    }
}

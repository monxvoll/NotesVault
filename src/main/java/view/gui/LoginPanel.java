package view.gui;

import controller.auth.LoginController;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private MainApp mainApp;
    private LoginController loginController;
    private JTextField userName;
    private JPasswordField password;

    private JLabel userLabel;
    private JLabel passwordLabel;

    private JButton loginButton;

    public LoginPanel(MainApp mainApp) {

        super();
        this.mainApp = mainApp;
        loginController = new LoginController();

        mainApp.showPanel("login");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        userName = new JTextField();
        password = new JPasswordField();
        loginButton = new JButton("Login");

        userLabel = new JLabel("Usuario:");
        passwordLabel = new JLabel("Contrasena:");

        loginButton.addActionListener(this::handleLogin);

        initializedDesign();
        setVisible(true);
    }

    public void initializedDesign(){
        add(userLabel);
        add(userName);

        add(passwordLabel);
        add(password);

        add(loginButton);
    }

    public void handleLogin(ActionEvent e){
        if(loginController.login.validateInputs(userName.getText(), new String(password.getPassword()))){
            mainApp.setCurrentUser(loginController.getActualUser());
            mainApp.showPanel("dashboard");
        }else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas");
        }
    }
}

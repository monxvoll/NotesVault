package view.gui;

import controller.auth.LoginController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private MainApp mainApp;
    private LoginController loginController;
    private GridBagConstraints gbc;

    private JTextField userName;
    private JPasswordField password;

    private  JLabel titleLabel;
    private JLabel resetLabel;
    private JLabel doYouHaveAnAccountLabel;
    private JLabel singUpLabel;

    private JButton loginButton;

    public LoginPanel(MainApp mainApp) {

        super();
        setBackground(new Color(255, 255, 255));
        this.mainApp = mainApp;
       // loginController = new LoginController();

        mainApp.showPanel("login");
        setLayout(new BorderLayout());

        userName = new JTextField();
        password = new JPasswordField();

        resetLabel = new JLabel();
        doYouHaveAnAccountLabel = new JLabel();

        singUpLabel = new JLabel("Sing-Up");
        resetLabel.setText("Reset password");
        doYouHaveAnAccountLabel.setText("Do you have an account?");
        userName.setText("email@gmail.com");
        userName.setFont(new Font("Alatsi", Font.PLAIN, 12));
        password.setText("Password");
        password.setFont(new Font("Alatsi", Font.PLAIN, 12));
        loginButton = new JButton("Sign in");
        titleLabel = new JLabel("NotesVault");

        Dimension textFieldDimension = new Dimension(200, 28);
        userName.setPreferredSize(textFieldDimension);
        userName.setMinimumSize(textFieldDimension);
        password.setPreferredSize(textFieldDimension);
        password.setMinimumSize(textFieldDimension);

        loginButton.addActionListener(this::handleLogin);

        initializedDesign();
        setStyles();
        setVisible(true);
    }

    public void initializedDesign(){

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(getBackground());
        titlePanel.add(titleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(getBackground());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 0, 8, 0);

        addCenteredComponent(formPanel, userName);
        addCenteredComponent(formPanel, password);
        addComponent(formPanel,  resetLabel);
        addCenteredComponent(formPanel,  loginButton);

        JPanel accountPanel = new JPanel();
        accountPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 0));
        accountPanel.setOpaque(false);
        accountPanel.add(doYouHaveAnAccountLabel);
        accountPanel.add(singUpLabel);
        addComponent(formPanel, accountPanel);

        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);

    }

    public void addComponent (JPanel panel, Component component){
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    public void addCenteredComponent (JPanel panel, Component component){
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(component);
        panel.add(wrapper, gbc);
    }

    public void setStyles(){
        titleLabel.setFont(new Font("Aleo", Font.PLAIN, 32));
        loginButton.setBackground(new Color(57, 56, 56));
        loginButton.setForeground(new Color(229, 229, 229));
        resetLabel.setForeground(new Color(39, 39, 39));
        doYouHaveAnAccountLabel.setForeground(new Color(168, 168, 168));
        singUpLabel.setForeground(new Color(57, 57, 57));
        userName.setForeground(new Color(57, 57, 57));
        password.setForeground(new Color(57, 57, 57));

        userName.setOpaque(true);
        userName.setBackground(Color.WHITE);
        password.setOpaque(true);
        password.setBackground(Color.WHITE);
    }

    public void handleLogin(ActionEvent e){
       /* if(loginController.login.validateInputs(userName.getText(), new String(password.getPassword()))){
            mainApp.setCurrentUser(loginController.getActualUser());
            mainApp.showPanel("dashboard");
        }else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas");
        }*/
    }
}

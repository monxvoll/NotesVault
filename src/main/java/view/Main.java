package view;

import controller.LoginController;
import controller.RegisterController;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Inicializar Firebase
        FirebaseInitializer.initialize();
        RegisterController registerController = new RegisterController();
        LoginController loginController = new LoginController();

        while (true) {
        System.out.println("1.Iniciar Sesion");
        System.out.println("2.Registrarse");
        String opt = scanner.nextLine();
            switch (opt) {
                case "1":
                    loginController.login();
                    break;
                case "2":
                    registerController.registerUser();
                    break;
                default:
                    System.err.println("Digite una opcion valida");
            }
        }


    }
}

package view;

import controller.auth.LoginController;
import controller.auth.RegisterController;
import model.entities.User;

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
                    if(loginController.login()){
                        showNotesMenu(loginController.getActualUser());
                        System.exit(0);
                    }
                    break;
                case "2":
                    registerController.register();
                    break;
                default:
                    System.err.println("Digite una opcion valida");
            }
        }


    }

    public static void showNotesMenu(User user){
       //En construccion
        executeOption(0,user);
    }

    private static void executeOption(int opt,User user){
        //En construccion
    }
}

package view;

import controller.auth.LoginController;
import controller.auth.RegisterController;
import controller.note.CreateController;
import controller.note.ReadController;
import model.entities.User;
import java.util.Scanner;


public class Main {

   private static CreateController createController ;
   private static ReadController readController;

   private static boolean flag = true;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Inicializar Firebase
        FirebaseInitializer.initialize();
        RegisterController registerController = new RegisterController();
        LoginController loginController = new LoginController();



        while (flag) {
        System.out.println("1.Iniciar Sesion");
        System.out.println("2.Registrarse");
        String opt = scanner.nextLine();
            switch (opt) {
                case "1":
                    if(loginController.login()){
                        setFlagFalse();
                        showNotesMenu(loginController.getActualUser(),scanner);
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

    public static void showNotesMenu(User user,Scanner scanner){
        while (!flag) {
            System.out.println("1. Crear una nota nueva");
            System.out.println("2. Ver notas actuales");
            System.out.println("3. Editar una nota");
            System.out.println("4. Eliminar una nota");
            System.out.println("5. Cerrar Sesion");
            String opt = scanner.nextLine();
            executeOption(opt, user);
        }
    }

    private static void executeOption(String opt,User user){
        switch (opt){
            case "1":
                createController = new CreateController();
                createController.createNote(user);
                break;
            case "2":
                readController = new ReadController();
                readController.readNotes(user);
                break;
            case "3":
                break;
            case "4":
                break;
            case "5":
                setFlagTrue();
                break;
            default:
                System.out.println("Por favor digite una opcion valida");
        }
    }

    public static void setFlagTrue(){
        flag = true;
    }
    public static void setFlagFalse(){
        flag = false;
    }
}

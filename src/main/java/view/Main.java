package view;

import controller.auth.LoginController;
import controller.auth.RegisterController;
import controller.crud.CreateController;
import controller.crud.DeleteController;
import controller.crud.ReadController;
import controller.crud.UpdateControlller;
import model.entities.User;
import util.FirebaseInitializer;

import java.util.Scanner;


public class Main {

   private static CreateController createController ;
   private static ReadController readController;
   private static DeleteController deleteController;
   private static UpdateControlller updateControlller;
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
            executeOption(opt, user,scanner);
        }
    }

    private static void executeOption(String opt,User user,Scanner scanner){
        switch (opt){
            case "1":
                createController = new CreateController();
                createController.createNote(user,scanner);
                break;
            case "2":
                readController = new ReadController();
                readController.readNotes(user);
                break;
            case "3":
                updateControlller = new UpdateControlller();
                updateControlller.updateNote(user,scanner);
                break;
            case "4":
                deleteController = new DeleteController();
                deleteController.deleteNote(user,scanner);
                break;
            case "5":
                setFlagTrue();
                break;
            default:
                System.out.println("Por favor digite una opcion valida");
        }
    }

    private static void setFlagTrue(){
        flag = true;
    }
    private static void setFlagFalse(){
        flag = false;
    }
}

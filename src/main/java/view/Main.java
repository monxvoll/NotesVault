package view;

import controller.RegisterController;

public class Main {
    public static void main(String[] args) {
        // Inicializar Firebase
        FirebaseInitializer.initialize();
        RegisterController registerController = new RegisterController();

        // Registrar usuarios (ejemplo de prueba)
        for (int i = 0; i < 5; i++) {
            registerController.registerUser();
        }

        // Cerrar el scanner una vez que todos los registros hayan terminado
        registerController.closeScanner();
    }
}

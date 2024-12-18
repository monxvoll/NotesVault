package view;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {

    // Método para inicializar Firebase
    public static void initialize() {
        try {
            // Crea un flujo de entrada para leer el archivo de claves JSON
            FileInputStream serviceAccount = new FileInputStream("resources/key.json"); //Ruta al json con las credenciales

            // Construye las opciones de Firebase con las credenciales obtenidas del archivo JSON y la URL de la base de datos
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount)) // Establece las credenciales
                    .setDatabaseUrl("https://todotaskapp-d2cf1.firebaseio.com") // Establece la URL de la base de datos
                    .build();

            // Inicializa la app de Firebase con las opciones especificadas
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            // Maneja cualquier excepción de entrada/salida que pueda ocurrir
            e.printStackTrace();
        }
    }
}

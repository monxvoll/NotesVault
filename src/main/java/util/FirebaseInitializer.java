package util;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {

    // Método para inicializar Firebase
    public static void initialize() {

        try {
            // Obtener la ruta del archivo JSON desde la variable de entorno
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

            if (credentialsPath == null || credentialsPath.isEmpty()) {
                throw new RuntimeException("No se encontró la variable de entorno GOOGLE_APPLICATION_CREDENTIALS");
            }

            // Leer el archivo JSON y obtener las credenciales
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));

            // Construye las opciones de Firebase con las credenciales obtenidas y la URL de la base de datos
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials) // Establece las credenciales
                    .setDatabaseUrl("https://todotaskapp-d2cf1.firebaseio.com") // URL de la base de datos
                    .build();
            // Inicializa la app de Firebase si aún no está inicializada
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase inicializado correctamente!");
            } else {
                System.out.println("Firebase ya estaba inicializado.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

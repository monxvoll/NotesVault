package com.notesvault.util;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;


@Configuration //Le decimos a spring que esta clase es una fuente de beans (Un bean es un objeto listo para ser injectado)
public class FirebaseInitializer {

    @Bean //Se añade esta anotacion para que springboot puede hacer la injeccion de dependencias de firestore de forma automatica
    public Firestore firestore() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-config.json");


            if (serviceAccount == null) {
                throw new RuntimeException("No se encontró el archivo de credenciales en resources/firebase-config.json");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl("https://todotaskapp-d2cf1.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase inicializado correctamente!");
            } else {
                System.out.println("Firebase ya estaba inicializado.");
            }

            return FirestoreClient.getFirestore();

        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar Firebase", e);
        }
    }

}

package com.notesvault.util;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseInitializer {

    public static void initialize() {
        try {
           
            InputStream serviceAccount = FirebaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("firebase/key.json");

            if (serviceAccount == null) {
                throw new RuntimeException("No se encontró el archivo de credenciales en resources/firebase/key.json");
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

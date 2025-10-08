package com.notesvault.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseInitializer {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-config.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://todotaskapp-d2cf1.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Initializing Firebase app...");
            return FirebaseApp.initializeApp(options);
        } else {
            System.out.println("Firebase app already initialized.");
            return FirebaseApp.getInstance();
        }
    }

    //To create firestore, we need to create firebaseApp first
    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
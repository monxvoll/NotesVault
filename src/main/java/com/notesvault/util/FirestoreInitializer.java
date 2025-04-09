package com.notesvault.util;

import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

public class FirestoreInitializer {
    private static Firestore firestore;

    static {
        firestore = FirestoreClient.getFirestore();
    }

    public static Firestore getFirestore() {
        return firestore;
    }
}

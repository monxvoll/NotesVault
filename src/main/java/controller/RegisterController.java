package controller;

import model.User;
import org.passay.*;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class RegisterController {
    private Scanner scanner;
    private Firestore db;

    public RegisterController() {
        this.db = FirestoreClient.getFirestore(); // Inicializa Firestore
        this.scanner = new Scanner(System.in);
    }

    public void registerUser() {
        User user = new User(recieveName(), recievePassword());
        System.out.println("Registro exitoso");
        saveUserToFirestore(user); // Guarda el usuario en Firestore
    }

    private String recieveName() {
        while (true) {
            System.out.println("Por favor digite un nombre de usuario");
            String name = scanner.nextLine();
            if (existsUserByName(name)) {
                System.err.println("El usuario ya existe");
            } else if (isEmpty(name)) {
                System.err.println("El nombre de usuario es obligatorio");
            } else {
                return name;
            }
        }
    }

    private String recievePassword() {
        while (true) {
            System.out.println("Por favor digite una contraseña");
            String password = scanner.nextLine();
            if (!validatePassword(password)) {
                System.err.println("Por favor digite una contraseña valida");
            } else {
                return password;
            }
        }
    }

    private boolean validatePassword(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30), // Longitud minima de 8 y max de 30 caracteres
                new CharacterRule(EnglishCharacterData.UpperCase, 1), // Al menos una mayuscula
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1), // Al menos un digito
                new CharacterRule(EnglishCharacterData.Special, 1), // Al menos un caracter especial
                new WhitespaceRule()));
        RuleResult result = validator.validate(new PasswordData(password));

        return result.isValid();
    }

    private boolean existsUserByName(String name) {
        try {
            ApiFuture<com.google.cloud.firestore.DocumentSnapshot> future = db.collection("users").document(name).get();
            com.google.cloud.firestore.DocumentSnapshot document = future.get();
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al verificar el usuario en Firestore: " + e);
            return false;
        }
    }

    private boolean isEmpty(String name) {
        return name == null || name.isEmpty();
    }

    private void saveUserToFirestore(User user) {
        // Guarda el documento del usuario en la colección "users" en Firestore
        ApiFuture<WriteResult> future = db.collection("users").document(user.getUserName()).set(user);
        try {
            // Obtiene el resultado de la operación de escritura
            WriteResult result = future.get();
            System.out.println("Usuario guardado exitosamente en Firestore en: " + result.getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al guardar el usuario en Firestore: " + e);
        }
    }

    public void closeScanner() {
        scanner.close();
    }
}

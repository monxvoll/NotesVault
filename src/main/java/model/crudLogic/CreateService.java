package model.crudLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.authlogic.LoginService;
import model.entities.Note;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class CreateService {

    private static final Logger logger = LoggerFactory.getLogger(CreateService.class);
    private LocalDateTime localDateTime;
    private String exclusiveId;
    private final Firestore firestore;

    public CreateService(Firestore firestore){
        this.localDateTime = LocalDateTime.now();
        this.exclusiveId = UUID.randomUUID().toString(); //Genera un ID unico utilizando UUID y lo convierte a String
        this.firestore = firestore;
    }

    public String createNote(User user, String title, String content){
        if(validateNotEmpty(title, content)) {
            logger.info("Iniciando creación de nota para usuario: {}", user.getEmail());
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String date = localDateTime.format(format);
            Note note = new Note(title, content, date, exclusiveId);
             return addNote(user, note);
        }
        return "Error en la validación de datos";
    }

    private String addNote(User user, Note note) {
        try {
            if (user == null || user.getUserName() == null || note == null || note.getId() == null) {
                logger.warn("Error: Usuario o nota no válidos.");
                throw new IllegalArgumentException("Error: Usuario o nota no válidos.");
            }

            DocumentReference userRef = firestore.collection("users").document(user.getEmail());
            CollectionReference notesRef = userRef.collection("notesList");

            ApiFuture<WriteResult> future = notesRef.document(note.getId()).set(note);
            WriteResult result = future.get();

            logger.info("Nota guardada exitosamente en Firestore en: {}", result.getUpdateTime());
            return "Nota creada exitosamente";

        } catch (InterruptedException e) {
            logger.error("Error al guardar la nota (interrupción del hilo): {}", e.getMessage());
            Thread.currentThread().interrupt();
            return "Error al guardar la nota: interrupción del hilo";
        } catch (ExecutionException e) {
            logger.error("Error al guardar la nota en Firestore: {}", e.getMessage());
            return "Error al guardar la nota en Firestore";
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            return "Error inesperado al guardar la nota";
        }
    }

    public static boolean validateNotEmpty(String title, String content) {
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Por favor, digite un campo válido");
        }
        return true;
    }
}

package com.notesvault.controller.crud;

import com.notesvault.model.crudLogic.ReadService;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;

@RestController
@RequestMapping("/note")
//Por ahora haremos consultas  de lectura generales, usando el email
public class ReadController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteController.class);
    private final ReadService readService;

    public ReadController(ReadService readService) {
        this.readService = readService;
    }

    @GetMapping("/read")
    public ResponseEntity<String> readNote(@RequestParam String userEmail){

    }
}

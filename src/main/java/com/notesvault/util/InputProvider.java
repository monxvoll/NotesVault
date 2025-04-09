package com.notesvault.util;

import java.util.Scanner;

public class InputProvider {
    private Scanner scanner;

    public InputProvider(Scanner scanner) {
        this.scanner = scanner;
    }

    public String nextLine() {
        return scanner.nextLine();
    }

    public int nextInt() {
        return scanner.nextInt();
    }
}


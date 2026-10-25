package org.example.gestioninscripciones.config;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoggerSingleton {

    private static LoggerSingleton instance;
    private final List<String> logsMemory = new ArrayList<>();
    private static final String LOG_FILE = "inscripciones.log";

    private LoggerSingleton() {}

    public static synchronized LoggerSingleton getInstance() {
        if (instance == null) {
            instance = new LoggerSingleton();
        }
        return instance;
    }

    public synchronized void log(String mensaje) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + mensaje;
        logsMemory.add(logEntry);

        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error al escribir el log: " + e.getMessage());
        }
    }

    public List<String> getLogsMemory() {
        return new ArrayList<>(logsMemory);
    }
}
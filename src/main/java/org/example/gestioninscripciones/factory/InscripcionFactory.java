package org.example.gestioninscripciones.factory;

import org.example.gestioninscripciones.model.Carrera;
import org.example.gestioninscripciones.model.EstadoInscripcion;
import org.example.gestioninscripciones.model.Inscripcion;
import org.example.gestioninscripciones.model.TipoInscripcion;

public class InscripcionFactory {

    public static Inscripcion crearInscripcion(String estudiante, Carrera carrera, TipoInscripcion tipo, EstadoInscripcion estado) {
        if (estudiante == null || estudiante.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estudiante no puede estar vacío");
        }
        if (carrera == null) {
            throw new IllegalArgumentException("La carrera es requerida");
        }
        return new Inscripcion(estudiante, carrera, tipo, estado);
    }
}
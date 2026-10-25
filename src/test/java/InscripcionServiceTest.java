package org.example.gestioninscripciones;

import org.example.gestioninscripciones.config.LoggerSingleton;
import org.example.gestioninscripciones.exceptions.*;
import org.example.gestioninscripciones.factory.InscripcionFactory;
import org.example.gestioninscripciones.model.*;
import org.example.gestioninscripciones.service.InscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InscripcionServiceTest {

    private InscripcionService service;
    private Carrera carreraPrueba;
    private String codigoUnico;

    @BeforeEach
    void setUp() {
        service = new InscripcionService();
        // Generamos un código único para no violar la clave única de H2/JPA entre tests
        codigoUnico = "TEST-" + UUID.randomUUID().toString().substring(0, 5);
        carreraPrueba = new Carrera(codigoUnico, "Carrera de Prueba", 2); // Cupo limite = 2
        service.registrarCarrera(carreraPrueba);
    }

    // 1. Test Factory
    @Test
    void testInscripcionFactoryCreacionCorrecta() {
        Inscripcion inscripcion = InscripcionFactory.crearInscripcion(
                "Carlos Perez", carreraPrueba, TipoInscripcion.REGULAR, EstadoInscripcion.CONFIRMADA
        );

        assertNotNull(inscripcion);
        assertEquals("Carlos Perez", inscripcion.getEstudiante());
        assertEquals(TipoInscripcion.REGULAR, inscripcion.getTipo());
        assertEquals(EstadoInscripcion.CONFIRMADA, inscripcion.getEstado());
    }

    // 2. Test Logger Singleton
    @Test
    void testLoggerSingletonInstanciaUnica() {
        LoggerSingleton logger1 = LoggerSingleton.getInstance();
        LoggerSingleton logger2 = LoggerSingleton.getInstance();

        assertNotNull(logger1);
        assertSame(logger1, logger2, "LoggerSingleton debe devolver siempre la misma instancia");
    }

    // 3. Test Caso Borde: Inscripción hasta el último cupo
    @Test
    void testInscripcionHastaUltimoCupo() throws Exception {
        service.inscribir("Estudiante 1", codigoUnico, TipoInscripcion.REGULAR);
        Inscripcion segunda = service.inscribir("Estudiante 2", codigoUnico, TipoInscripcion.CONDICIONAL);

        assertEquals(2, service.getInscriptos(codigoUnico).size());
        assertEquals(EstadoInscripcion.CONFIRMADA, segunda.getEstado());
        assertTrue(service.getListaEspera(codigoUnico).isEmpty(), "La lista de espera debe estar vacía");
    }

    // 4. Test Caso Borde: Cupo Lleno -> Pasa a Lista de Espera
    @Test
    void testCupoLlenoPasaAListaDeEspera() throws Exception {
        service.inscribir("Estudiante 1", codigoUnico, TipoInscripcion.REGULAR);
        service.inscribir("Estudiante 2", codigoUnico, TipoInscripcion.REGULAR);

        // Tercer estudiante sobrepasa el cupo de 2
        assertThrows(CupoLlenoException.class, () -> {
            service.inscribir("Estudiante 3", codigoUnico, TipoInscripcion.OYENTE);
        });

        assertEquals(2, service.getInscriptos(codigoUnico).size());
        assertEquals(1, service.getListaEspera(codigoUnico).size());
        assertEquals("Estudiante 3", service.getListaEspera(codigoUnico).get(0).getEstudiante());
    }

    // 5. Test Excepción por Inscripción Duplicada
    @Test
    void testInscripcionDuplicadaLanzaExcepcion() throws Exception {
        service.inscribir("Juan Gomez", codigoUnico, TipoInscripcion.REGULAR);

        assertThrows(InscripcionDuplicadaException.class, () -> {
            service.inscribir("Juan Gomez", codigoUnico, TipoInscripcion.CONDICIONAL);
        });
    }
}
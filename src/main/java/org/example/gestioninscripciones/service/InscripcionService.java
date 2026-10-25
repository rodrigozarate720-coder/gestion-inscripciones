package org.example.gestioninscripciones.service;

import org.example.gestioninscripciones.config.JPAUtil;
import org.example.gestioninscripciones.config.LoggerSingleton;
import org.example.gestioninscripciones.exceptions.*;
import org.example.gestioninscripciones.factory.InscripcionFactory;
import org.example.gestioninscripciones.model.*;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InscripcionService {

    private final Map<String, List<Inscripcion>> inscriptosPorCarrera = new HashMap<>();
    private final Map<String, List<Inscripcion>> listaEsperaPorCarrera = new HashMap<>();
    private final Map<String, Carrera> mapaCarreras = new HashMap<>();
    private final LoggerSingleton logger = LoggerSingleton.getInstance();

    // ==========================================
    // 1. CRUD DE CARRERAS
    // ==========================================

    // CREATE / REGISTER
    public void registrarCarrera(Carrera carrera) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(carrera);
            em.getTransaction().commit();

            mapaCarreras.put(carrera.getCodigo(), carrera);
            inscriptosPorCarrera.putIfAbsent(carrera.getCodigo(), new ArrayList<>());
            listaEsperaPorCarrera.putIfAbsent(carrera.getCodigo(), new ArrayList<>());
            logger.log("CARRERA CREADA: " + carrera.getNombre() + " (Cupo: " + carrera.getCupo() + ")");
        } finally {
            em.close();
        }
    }

    // READ
    public List<Carrera> getCarreras() {
        return new ArrayList<>(mapaCarreras.values());
    }

    public Carrera getCarreraPorCodigo(String codigo) {
        return mapaCarreras.get(codigo);
    }

    // UPDATE
    public void actualizarCarrera(String codigo, String nuevoNombre, int nuevoCupo) throws CarreraNoEncontradaException {
        Carrera carrera = mapaCarreras.get(codigo);
        if (carrera == null) {
            throw new CarreraNoEncontradaException("Carrera no encontrada con código: " + codigo);
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrera cEnBD = em.find(Carrera.class, carrera.getId());
            if (cEnBD != null) {
                cEnBD.setNombre(nuevoNombre);
                cEnBD.setCupo(nuevoCupo);
            }
            em.getTransaction().commit();

            carrera.setNombre(nuevoNombre);
            carrera.setCupo(nuevoCupo);
            logger.log("CARRERA ACTUALIZADA: " + codigo + " -> Nuevo Cupo: " + nuevoCupo);
        } finally {
            em.close();
        }
    }

    // DELETE
    public void eliminarCarrera(String codigo) throws CarreraNoEncontradaException {
        Carrera carrera = mapaCarreras.get(codigo);
        if (carrera == null) {
            throw new CarreraNoEncontradaException("Carrera no encontrada con código: " + codigo);
        }

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Carrera cEnBD = em.find(Carrera.class, carrera.getId());
            if (cEnBD != null) {
                em.remove(cEnBD);
            }
            em.getTransaction().commit();

            mapaCarreras.remove(codigo);
            inscriptosPorCarrera.remove(codigo);
            listaEsperaPorCarrera.remove(codigo);
            logger.log("CARRERA ELIMINADA: " + codigo);
        } finally {
            em.close();
        }
    }

    // ==========================================
    // 2. CRUD DE INSCRIPCIONES
    // ==========================================

    // CREATE
    public Inscripcion inscribir(String estudiante, String codigoCarrera, TipoInscripcion tipo)
            throws CarreraNoEncontradaException, InscripcionDuplicadaException, CupoLlenoException {

        Carrera carrera = mapaCarreras.get(codigoCarrera);
        if (carrera == null) {
            throw new CarreraNoEncontradaException("La carrera con código " + codigoCarrera + " no existe.");
        }

        List<Inscripcion> confirmados = inscriptosPorCarrera.get(codigoCarrera);
        List<Inscripcion> espera = listaEsperaPorCarrera.get(codigoCarrera);

        boolean yaInscripto = confirmados.stream().anyMatch(i -> i.getEstudiante().equalsIgnoreCase(estudiante))
                || espera.stream().anyMatch(i -> i.getEstudiante().equalsIgnoreCase(estudiante));

        if (yaInscripto) {
            throw new InscripcionDuplicadaException("El estudiante " + estudiante + " ya está inscripto en " + carrera.getNombre());
        }

        EntityManager em = JPAUtil.getEntityManager();
        Inscripcion inscripcion;

        try {
            em.getTransaction().begin();

            if (confirmados.size() < carrera.getCupo()) {
                inscripcion = InscripcionFactory.crearInscripcion(estudiante, carrera, tipo, EstadoInscripcion.CONFIRMADA);
                confirmados.add(inscripcion);
                logger.log("INSCRIPCIÓN CONFIRMADA: " + estudiante + " en " + carrera.getNombre());
            } else {
                inscripcion = InscripcionFactory.crearInscripcion(estudiante, carrera, tipo, EstadoInscripcion.EN_ESPERA);
                espera.add(inscripcion);
                logger.log("LISTA DE ESPERA: " + estudiante + " agregado en " + carrera.getNombre());
            }

            em.persist(inscripcion);
            em.getTransaction().commit();

            if (inscripcion.getEstado() == EstadoInscripcion.EN_ESPERA) {
                throw new CupoLlenoException("Cupo lleno para " + carrera.getNombre() + ". Pasó a lista de espera.");
            }

            return inscripcion;
        } finally {
            em.close();
        }
    }

    // READ
    public List<Inscripcion> getInscriptos(String codigoCarrera) {
        return inscriptosPorCarrera.getOrDefault(codigoCarrera, new ArrayList<>());
    }

    public List<Inscripcion> getListaEspera(String codigoCarrera) {
        return listaEsperaPorCarrera.getOrDefault(codigoCarrera, new ArrayList<>());
    }

    // UPDATE INSCRIPCIÓN (Tipo o Estado)
    public void actualizarTipoInscripcion(Inscripcion inscripcion, TipoInscripcion nuevoTipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Inscripcion iBD = em.find(Inscripcion.class, inscripcion.getId());
            if (iBD != null) {
                iBD.setTipo(nuevoTipo);
            }
            em.getTransaction().commit();
            inscripcion.setTipo(nuevoTipo);
            logger.log("INSCRIPCIÓN MODIFICADA: " + inscripcion.getEstudiante() + " -> Nuevo Tipo: " + nuevoTipo);
        } finally {
            em.close();
        }
    }

    // DELETE / CANCELAR INSCRIPCIÓN (Promueve automáticamente de lista de espera si hay cupo libre)
    public void cancelarInscripcion(String codigoCarrera, Inscripcion inscripcion) {
        List<Inscripcion> confirmados = inscriptosPorCarrera.get(codigoCarrera);
        List<Inscripcion> espera = listaEsperaPorCarrera.get(codigoCarrera);

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Inscripcion iBD = em.find(Inscripcion.class, inscripcion.getId());

            if (confirmados.remove(inscripcion)) {
                if (iBD != null) em.remove(iBD);
                logger.log("INSCRIPCIÓN CANCELADA: " + inscripcion.getEstudiante() + " de " + codigoCarrera);

                // Reorganización: Si había alguien en espera, pasa a confirmado
                if (!espera.isEmpty() && confirmados.size() < mapaCarreras.get(codigoCarrera).getCupo()) {
                    Inscripcion promovido = espera.remove(0);
                    promovido.setEstado(EstadoInscripcion.CONFIRMADA);
                    confirmados.add(promovido);

                    Inscripcion promBD = em.find(Inscripcion.class, promovido.getId());
                    if (promBD != null) promBD.setEstado(EstadoInscripcion.CONFIRMADA);

                    logger.log("PROMOVIDO DE ESPERA A CONFIRMADO: " + promovido.getEstudiante());
                }
            } else {
                espera.remove(inscripcion);
                if (iBD != null) em.remove(iBD);
                logger.log("ELIMINADO DE LISTA DE ESPERA: " + inscripcion.getEstudiante());
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
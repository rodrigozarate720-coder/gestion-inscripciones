package org.example.gestioninscripciones.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String estudiante;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoInscripcion estado;

    @Enumerated(EnumType.STRING)
    private TipoInscripcion tipo;

    public Inscripcion() {}

    public Inscripcion(String estudiante, Carrera carrera, TipoInscripcion tipo, EstadoInscripcion estado) {
        this.estudiante = estudiante;
        this.carrera = carrera;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }
    public Carrera getCarrera() { return carrera; }
    public void setCarrera(Carrera carrera) { this.carrera = carrera; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoInscripcion getEstado() { return estado; }
    public void setEstado(EstadoInscripcion estado) { this.estado = estado; }
    public TipoInscripcion getTipo() { return tipo; }
    public void setTipo(TipoInscripcion tipo) { this.tipo = tipo; }
}
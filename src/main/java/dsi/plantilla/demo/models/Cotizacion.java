package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotizaciones")
@Data
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero; // Ej: COT-2026-001

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaValidez;

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Relación Opcional: Si la cotización pertenece a un proyecto existente
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    private Double subtotal = 0.0;
    private Double iva = 0.0;
    private Double total = 0.0;

    // "Borrador", "Enviada", "Aprobada", "Rechazada", "Cerrada"
    @Column(nullable = false)
    private String estado = "Borrador";

    // orphanRemoval = true permite borrar filas dinámicas fácilmente
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCotizacion> detalles = new ArrayList<>();
}
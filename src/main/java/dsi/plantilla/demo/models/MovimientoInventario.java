package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "movimientos_inventario")
@Data
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "herramienta_id", nullable = false)
    private Herramienta herramienta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto; // Destino de la herramienta

    private String responsable; // Persona a la que se le entregó

    private String tipo; // "ASIGNACION" (resta stock) o "DEVOLUCION" (suma stock)

    private Integer cantidad;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor; // Auditoría
}
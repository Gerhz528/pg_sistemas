package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas_generadas")
@Data
public class FacturaGenerada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate rangoInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate rangoFin;

    private LocalDateTime fechaGeneracion;

    private String formato; // "EXCEL" o "JSON"

    private String archivoUrl; // Nombre del archivo guardado en /uploads/

    private String generadoPor; // Nombre del usuario que lo generó
}
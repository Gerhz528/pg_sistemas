package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "planillas")
@Data
public class Planilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime fechaGeneracion;
    private Double totalPagar;

    @OneToMany(mappedBy = "planilla", cascade = CascadeType.ALL)
    private List<DetallePlanilla> detalles;
}
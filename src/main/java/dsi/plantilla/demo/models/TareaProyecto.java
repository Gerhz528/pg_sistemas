package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tareas_proyecto")
@Data
public class TareaProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private Integer progreso = 0; // 0 a 100%

    // ID de la tarea de la que depende (Para las flechas en el diagrama)
    private String dependenciaStr; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // NUEVO: Campo para guardar la bitácora acumulativa de cambios
    @Column(columnDefinition = "TEXT")
    private String bitacora; 

    // LÓGICA DE NOTIFICACIONES (Días faltantes)
    public long getDiasFaltantes() {
        if (fechaFin == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
    }
}
package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asistencias")
@Data
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Debe seleccionar un trabajador")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trabajador_id")
    private Trabajador trabajador;

    @NotNull(message = "La fecha es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La hora de entrada es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horaSalida;

    private Double horasTotales = 0.0;

    @Column(nullable = false)
    private String estado = "PENDIENTE";

    private String observacionesJefe;
    private LocalDateTime fechaRevision;
    private String revisadoPor;

    @OneToMany(mappedBy = "asistencia", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<HoraExtra> horasExtras = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calcularHoras() {
        if (horaEntrada != null && horaSalida != null) {
            long minutos = java.time.Duration.between(horaEntrada, horaSalida).toMinutes();
            this.horasTotales = Math.max(0, minutos / 60.0);
        }
        if (this.estado == null) this.estado = "PENDIENTE";
    }
    
    public Double getTotalHorasExtra() {
        if (horasExtras == null || horasExtras.isEmpty()) return 0.0;
        return horasExtras.stream()
                .mapToDouble(h -> h.getCantidad() != null ? h.getCantidad() : 0.0)
                .sum();
    }

    // --- MÉTODOS PARA SUMATORIAS DE VISTA ---

    // Aprobadas
    public Double getHorasBaseSiAprobado() {
        return "APROBADO".equals(this.estado) ? (horasTotales != null ? horasTotales : 0.0) : 0.0;
    }
    public Double getHorasDiurnasAprobadas() {
        return "APROBADO".equals(this.estado) ? horasExtras.stream().filter(h -> "DIURNA".equals(h.getTipo())).mapToDouble(HoraExtra::getCantidad).sum() : 0.0;
    }
    public Double getHorasNocturnasAprobadas() {
        return "APROBADO".equals(this.estado) ? horasExtras.stream().filter(h -> "NOCTURNA".equals(h.getTipo())).mapToDouble(HoraExtra::getCantidad).sum() : 0.0;
    }

    // RECHAZADAS (Nueva Solicitud)
    public Double getHorasBaseSiRechazado() {
        return "RECHAZADO".equals(this.estado) ? (horasTotales != null ? horasTotales : 0.0) : 0.0;
    }
    public Double getHorasExtraSiRechazado() {
        return "RECHAZADO".equals(this.estado) ? getTotalHorasExtra() : 0.0;
    }

    // Soporte para iconos en la lista (Totales por registro)
    public Double getHorasDiurnasTotales() { return horasExtras.stream().filter(h -> "DIURNA".equals(h.getTipo())).mapToDouble(HoraExtra::getCantidad).sum(); }
    public Double getHorasNocturnasTotales() { return horasExtras.stream().filter(h -> "NOCTURNA".equals(h.getTipo())).mapToDouble(HoraExtra::getCantidad).sum(); }
}
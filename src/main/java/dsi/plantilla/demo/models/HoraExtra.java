package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "horas_extra")
@Data
public class HoraExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asistencia_id")
    private Asistencia asistencia;

    @NotNull(message = "La cantidad de horas es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Double cantidad;

    @NotBlank(message = "El tipo de hora extra es obligatorio")
    private String tipo; // DIURNA o NOCTURNA

    @NotBlank(message = "Debe indicar el motivo de las horas extra")
    private String motivo;
}
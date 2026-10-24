package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "puestos")
@Data
public class Puesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del puesto es obligatorio")
    @Column(unique = true)
    private String nombre;

    // NUEVO: Tarifa por hora para calcular planillas
    @Column(nullable = false)
    private Double salarioPorHora = 0.0; 
}
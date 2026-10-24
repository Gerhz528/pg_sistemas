package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "herramientas")
@Data
public class Herramienta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank(message = "El nombre de la herramienta es obligatorio")
    private String nombre;

    private String marca;

    // Ej: "Nuevo", "Usado", "Dañado"
    private String condicion = "Nuevo"; 

    @NotNull
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    @NotNull
    @Min(value = 0)
    private Integer stockMinimo = 0;

    private String imagen; // Ruta de la foto

    // Método virtual para saber el estado de disponibilidad (HU-24)
    public String getDisponibilidad() {
        return stock > 0 ? "Disponible" : "Agotado";
    }
}
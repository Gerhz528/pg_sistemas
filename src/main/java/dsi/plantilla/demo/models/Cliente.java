package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombre;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp = "\\d{8}-\\d", message = "El DUI debe tener formato 00000000-0")
    @Column(unique = true, nullable = false)
    private String dui;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String direccion;
}
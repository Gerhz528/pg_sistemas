package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "trabajadores")
@Data
public class Trabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp = "\\d{8}-\\d", message = "Formato: 00000000-0")
    @Column(unique = true, nullable = false)
    private String dui;

    @NotBlank(message = "El NSS es obligatorio")
    private String nss;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "puesto_id")
    private Puesto puesto;

    private boolean activo = true;

    // --- NUEVOS CAMPOS (HU Mejorada) ---
    private String telefono;
    private String direccion;
    private String contactoEmergencia;
    
    // Archivos
    private String foto;
    private String documentoDui;
    private String documentoCv;
}
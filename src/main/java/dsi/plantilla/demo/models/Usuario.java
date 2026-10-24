package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    // Se quita @NotBlank para permitir ediciones sin cambiar la contraseña. 
    // La validación en la creación se hará en el controlador/servicio.
    private String password;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe tener un formato válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
}
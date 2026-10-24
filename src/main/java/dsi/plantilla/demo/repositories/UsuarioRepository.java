package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // NUEVO: Consulta para el buscador de usuarios
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Usuario> buscarPorTermino(@Param("term") String term);
}
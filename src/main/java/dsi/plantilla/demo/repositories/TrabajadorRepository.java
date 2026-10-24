package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    boolean existsByDui(String dui);
    boolean existsByNss(String nss);

    // NUEVO: Para encontrar al trabajador exacto leyendo el Excel
    Optional<Trabajador> findByDui(String dui);

    @Query("SELECT t FROM Trabajador t WHERE " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "t.dui LIKE CONCAT('%', :term, '%')")
    List<Trabajador> buscarPorNombreODui(@Param("term") String term);

    @Query("SELECT DISTINCT t FROM Trabajador t JOIN Asistencia a ON a.trabajador.id = t.id")
    List<Trabajador> findTrabajadoresConAsistencia();
}
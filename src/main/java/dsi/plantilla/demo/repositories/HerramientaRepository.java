package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Herramienta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HerramientaRepository extends JpaRepository<Herramienta, Long> {
    boolean existsByCodigo(String codigo);

    @Query("SELECT h FROM Herramienta h WHERE LOWER(h.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(h.codigo) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Herramienta> buscarPorFiltro(@Param("term") String term);
}
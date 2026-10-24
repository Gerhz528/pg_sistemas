package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    
    @Query("SELECT p FROM Proyecto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(p.cliente.nombre) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Proyecto> buscarPorNombreOCliente(@Param("term") String term);
}
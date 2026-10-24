package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.TareaProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TareaProyectoRepository extends JpaRepository<TareaProyecto, Long> {
    List<TareaProyecto> findByProyectoIdOrderByFechaInicioAsc(Long proyectoId);
}
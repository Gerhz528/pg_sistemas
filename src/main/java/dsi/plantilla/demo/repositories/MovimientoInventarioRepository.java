package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByHerramientaIdOrderByFechaDesc(Long herramientaId);
    
    // NUEVO: Movimientos de un proyecto específico
    List<MovimientoInventario> findByProyectoIdOrderByFechaDesc(Long proyectoId);
}
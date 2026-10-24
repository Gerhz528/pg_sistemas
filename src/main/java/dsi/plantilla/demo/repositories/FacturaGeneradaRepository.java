package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.FacturaGenerada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaGeneradaRepository extends JpaRepository<FacturaGenerada, Long> {
    List<FacturaGenerada> findAllByOrderByFechaGeneracionDesc();
}
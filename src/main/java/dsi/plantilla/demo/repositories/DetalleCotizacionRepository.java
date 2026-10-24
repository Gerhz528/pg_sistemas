package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.DetalleCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Long> {
}
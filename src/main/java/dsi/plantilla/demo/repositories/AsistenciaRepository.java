package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Asistencia;
import dsi.plantilla.demo.models.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    boolean existsByTrabajadorAndFecha(Trabajador trabajador, LocalDate fecha);
    
    // Buscar historial por ID de trabajador ordenado por fecha más reciente
    List<Asistencia> findByTrabajadorIdOrderByFechaDesc(Long trabajadorId);
}
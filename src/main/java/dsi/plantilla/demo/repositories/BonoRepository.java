package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Bono;
import dsi.plantilla.demo.models.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface BonoRepository extends JpaRepository<Bono, Long> {
    List<Bono> findByTrabajadorIdOrderByFechaDesc(Long trabajadorId);
    List<Bono> findByTrabajadorAndFechaBetween(Trabajador trabajador, LocalDate inicio, LocalDate fin);
}
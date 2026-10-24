package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.DiaCerrado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface DiaCerradoRepository extends JpaRepository<DiaCerrado, LocalDate> {
}
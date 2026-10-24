package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {
}
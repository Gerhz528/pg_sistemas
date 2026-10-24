package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Planilla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanillaRepository extends JpaRepository<Planilla, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Planilla p WHERE p.fechaInicio <= :fin AND p.fechaFin >= :inicio")
    boolean existsOverlappingPlanilla(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT p FROM Planilla p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.trabajador t LEFT JOIN FETCH t.puesto WHERE p.id = :id")
    Optional<Planilla> findByIdWithDetalles(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(p.totalPagar), 0) FROM Planilla p WHERE p.fechaInicio >= :inicio AND p.fechaFin <= :fin")
    Double sumarTotalPlanillasEnRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
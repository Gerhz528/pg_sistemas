package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.HoraExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HoraExtraRepository extends JpaRepository<HoraExtra, Long> {
    List<HoraExtra> findByAsistenciaId(Long asistenciaId);

    @Query("SELECT COALESCE(SUM(h.cantidad), 0) FROM HoraExtra h WHERE h.asistencia.estado = 'APROBADO' AND h.asistencia.fecha BETWEEN :inicio AND :fin")
    Double sumarHorasExtrasAprobadasEnRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
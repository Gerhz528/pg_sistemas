package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {

    List<Bitacora> findAllByOrderByFechaHoraDesc();

    @Query("SELECT b FROM Bitacora b WHERE " +
           "(:usuario IS NULL OR LOWER(b.usuario) LIKE LOWER(CONCAT('%', :usuario, '%'))) AND " +
           "(:modulo IS NULL OR b.modulo = :modulo) AND " +
           "(b.fechaHora BETWEEN :desde AND :hasta) " +
           "ORDER BY b.fechaHora DESC")
    List<Bitacora> filtrarLogs(
            @Param("usuario") String usuario,
            @Param("modulo") String modulo,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );
}
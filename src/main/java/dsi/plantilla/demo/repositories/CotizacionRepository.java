package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    
    @Query("SELECT COUNT(c) FROM Cotizacion c")
    long countCotizaciones();

    @Query("SELECT c FROM Cotizacion c WHERE LOWER(c.numero) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(c.cliente.nombre) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY c.id DESC")
    List<Cotizacion> buscarPorNumeroOCliente(@Param("term") String term);

    @Query("SELECT c FROM Cotizacion c WHERE c.cliente.id = :clienteId AND c.fecha >= :inicio AND c.fecha <= :fin AND c.estado IN ('Aprobada', 'Cerrada')")
    List<Cotizacion> buscarParaFacturacion(@Param("clienteId") Long clienteId, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Cotizacion c WHERE c.proyecto.id = :proyectoId AND c.estado IN ('Aprobada', 'Cerrada')")
    Double sumarTotalCotizadoPorProyecto(@Param("proyectoId") Long proyectoId);
}
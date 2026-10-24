package dsi.plantilla.demo.repositories;

import dsi.plantilla.demo.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    boolean existsByDui(String dui);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR c.dui LIKE CONCAT('%', :term, '%')")
    List<Cliente> buscarPorNombreODui(@Param("term") String term);
}
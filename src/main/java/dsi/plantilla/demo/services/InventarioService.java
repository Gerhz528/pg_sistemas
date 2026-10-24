package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Herramienta;
import dsi.plantilla.demo.models.MovimientoInventario;
import dsi.plantilla.demo.repositories.HerramientaRepository;
import dsi.plantilla.demo.repositories.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventarioService {

    @Autowired private HerramientaRepository herramientaRepository;
    @Autowired private MovimientoInventarioRepository movimientoRepository;

    @Transactional(readOnly = true)
    public List<Herramienta> listar(String term) {
        if (term != null && !term.trim().isEmpty()) return herramientaRepository.buscarPorFiltro(term);
        return herramientaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Herramienta> buscarPorId(Long id) { return herramientaRepository.findById(id); }

    public boolean existeCodigo(String codigo) { return herramientaRepository.existsByCodigo(codigo); }

    @Transactional
    public Herramienta guardar(Herramienta h) {
        if (h.getId() != null) {
            Herramienta exist = herramientaRepository.findById(h.getId()).get();
            exist.setNombre(h.getNombre());
            exist.setMarca(h.getMarca());
            exist.setCondicion(h.getCondicion());
            exist.setStock(h.getStock());
            exist.setStockMinimo(h.getStockMinimo());
            if (h.getImagen() != null) exist.setImagen(h.getImagen());
            return herramientaRepository.save(exist);
        }
        return herramientaRepository.save(h);
    }

    @Transactional
    public void registrarMovimiento(MovimientoInventario mov) throws Exception {
        Herramienta h = herramientaRepository.findById(mov.getHerramienta().getId()).orElseThrow(() -> new Exception("Herramienta no encontrada"));
        
        if ("ASIGNACION".equals(mov.getTipo())) {
            if (h.getStock() < mov.getCantidad()) {
                throw new Exception("STOCK_INSUFICIENTE"); 
            }
            h.setStock(h.getStock() - mov.getCantidad());
        } else if ("DEVOLUCION".equals(mov.getTipo())) {
            h.setStock(h.getStock() + mov.getCantidad());
        }
        
        herramientaRepository.save(h); 
        movimientoRepository.save(mov); 
    }

    // NUEVO
    @Transactional(readOnly = true)
    public List<MovimientoInventario> listarMovimientosPorProyecto(Long id) {
        return movimientoRepository.findByProyectoIdOrderByFechaDesc(id);
    }
}
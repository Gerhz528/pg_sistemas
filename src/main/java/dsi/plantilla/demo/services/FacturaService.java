package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Factura;
import dsi.plantilla.demo.repositories.CotizacionRepository;
import dsi.plantilla.demo.repositories.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private CotizacionRepository cotizacionRepository;

    @Transactional(readOnly = true)
    public List<Factura> buscar(String termino) {
        if (termino != null && !termino.trim().isEmpty()) {
            return facturaRepository.buscarPorProveedorOProyecto(termino);
        }
        return facturaRepository.findAll();
    }

    @Transactional
    public Factura guardar(Factura factura) {
        return facturaRepository.save(factura);
    }

    @Transactional(readOnly = true)
    public Double calcularGastoAcumulado(Long proyectoId) {
        return facturaRepository.sumarGastosPorProyecto(proyectoId);
    }

    @Transactional(readOnly = true)
    public List<Factura> listarPorProyecto(Long id) {
        return facturaRepository.findByProyectoIdOrderByFechaDesc(id);
    }

    // HU-32 Tarea 1: Verificación de regla del 90% del presupuesto
    @Transactional(readOnly = true)
    public boolean superaUmbralAlerta(Long proyectoId, Double montoNuevo) {
        Double cotizado = cotizacionRepository.sumarTotalCotizadoPorProyecto(proyectoId);
        if (cotizado == null || cotizado <= 0) return false;

        Double gastoActual = calcularGastoAcumulado(proyectoId);
        if (gastoActual == null) gastoActual = 0.0;

        double gastoProyectado = gastoActual + (montoNuevo != null ? montoNuevo : 0.0);
        return gastoProyectado >= (cotizado * 0.90);
    }
}
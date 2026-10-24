package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Cotizacion;
import dsi.plantilla.demo.repositories.CotizacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CotizacionService {

    @Autowired private CotizacionRepository cotizacionRepository;

    @Transactional(readOnly = true)
    public List<Cotizacion> buscar(String termino) {
        if (termino != null && !termino.trim().isEmpty()) return cotizacionRepository.buscarPorNumeroOCliente(termino);
        return cotizacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cotizacion> buscarPorId(Long id) {
        return cotizacionRepository.findById(id);
    }

    @Transactional
    public Cotizacion guardar(Cotizacion cotizacion) {
        // Generar Número correlativo automático si es nueva
        if (cotizacion.getId() == null) {
            long total = cotizacionRepository.countCotizaciones() + 1;
            int year = LocalDate.now().getYear();
            cotizacion.setNumero(String.format("COT-%d-%03d", year, total));
        }

        // Amarrar la cotización padre a cada uno de sus detalles para la base de datos
        if (cotizacion.getDetalles() != null) {
            cotizacion.getDetalles().forEach(detalle -> detalle.setCotizacion(cotizacion));
        }

        return cotizacionRepository.save(cotizacion);
    }
}
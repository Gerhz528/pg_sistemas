package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Proyecto;
import dsi.plantilla.demo.repositories.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Transactional(readOnly = true)
    public List<Proyecto> buscar(String termino) {
        if (termino != null && !termino.trim().isEmpty()) {
            return proyectoRepository.buscarPorNombreOCliente(termino);
        }
        return proyectoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Proyecto> buscarPorId(Long id) {
        return proyectoRepository.findById(id);
    }

    @Transactional
    public Proyecto guardar(Proyecto proyecto) {
        if (proyecto.getId() != null) {
            Proyecto existente = proyectoRepository.findById(proyecto.getId()).get();
            existente.setNombre(proyecto.getNombre());
            existente.setDescripcion(proyecto.getDescripcion());
            existente.setFechaInicio(proyecto.getFechaInicio());
            existente.setFechaFin(proyecto.getFechaFin());
            existente.setEstado(proyecto.getEstado());
            existente.setCliente(proyecto.getCliente());
            return proyectoRepository.save(existente);
        }
        return proyectoRepository.save(proyecto);
    }
}
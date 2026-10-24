package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Trabajador;
import dsi.plantilla.demo.repositories.TrabajadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class TrabajadorService {
    @Autowired private TrabajadorRepository trabajadorRepository;

    @Transactional(readOnly = true)
    public List<Trabajador> listarTodos() { return trabajadorRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Trabajador> buscar(String termino) {
        if (termino != null && !termino.isEmpty()) return trabajadorRepository.buscarPorNombreODui(termino);
        return trabajadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Trabajador> buscarPorId(Long id) { return trabajadorRepository.findById(id); }

    @Transactional
    public Trabajador guardar(Trabajador trabajador) {
        if (trabajador.getId() != null) {
            Trabajador exist = trabajadorRepository.findById(trabajador.getId()).get();
            exist.setNombre(trabajador.getNombre());
            exist.setNss(trabajador.getNss());
            exist.setPuesto(trabajador.getPuesto());
            exist.setActivo(trabajador.isActivo());
            exist.setFechaIngreso(trabajador.getFechaIngreso());
            
            // Nuevos campos de contacto
            exist.setTelefono(trabajador.getTelefono());
            exist.setDireccion(trabajador.getDireccion());
            exist.setContactoEmergencia(trabajador.getContactoEmergencia());

            // Conservar archivos si no se subió uno nuevo
            if (trabajador.getFoto() != null) exist.setFoto(trabajador.getFoto());
            if (trabajador.getDocumentoDui() != null) exist.setDocumentoDui(trabajador.getDocumentoDui());
            if (trabajador.getDocumentoCv() != null) exist.setDocumentoCv(trabajador.getDocumentoCv());
            
            return trabajadorRepository.save(exist);
        }
        return trabajadorRepository.save(trabajador);
    }

    public boolean existeDui(String dui) { return trabajadorRepository.existsByDui(dui); }
}
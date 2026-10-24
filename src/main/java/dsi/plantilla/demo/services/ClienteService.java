package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Cliente;
import dsi.plantilla.demo.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> buscar(String termino) {
        if (termino != null && !termino.trim().isEmpty()) {
            return clienteRepository.buscarPorNombreODui(termino);
        }
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() != null) {
            Cliente existente = clienteRepository.findById(cliente.getId()).get();
            existente.setNombre(cliente.getNombre());
            // El DUI generalmente no se edita, pero lo dejamos protegido en la validación
            existente.setTelefono(cliente.getTelefono());
            existente.setEmail(cliente.getEmail());
            existente.setDireccion(cliente.getDireccion());
            return clienteRepository.save(existente);
        }
        return clienteRepository.save(cliente);
    }

    public boolean existeDui(String dui) {
        return clienteRepository.existsByDui(dui);
    }
}
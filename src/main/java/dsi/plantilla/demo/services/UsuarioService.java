package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Usuario;
import dsi.plantilla.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscar(String termino) {
        if (termino != null && !termino.isEmpty()) {
            return usuarioRepository.buscarPorTermino(termino);
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // ESTE ERA EL MÉTODO FALTANTE QUE HACÍA CRASHEAR LA APLICACIÓN
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() != null) {
            Usuario exist = usuarioRepository.findById(usuario.getId()).get();
            exist.setNombreCompleto(usuario.getNombreCompleto());
            exist.setEmail(usuario.getEmail());
            exist.setRoles(usuario.getRoles());
            exist.setActivo(usuario.isActivo());
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                exist.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
            return usuarioRepository.save(exist);
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}
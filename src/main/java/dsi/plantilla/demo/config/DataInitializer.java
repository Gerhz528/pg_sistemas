package dsi.plantilla.demo.config;

import dsi.plantilla.demo.models.Rol;
import dsi.plantilla.demo.models.Usuario;
import dsi.plantilla.demo.models.Puesto;
import dsi.plantilla.demo.repositories.RolRepository;
import dsi.plantilla.demo.repositories.UsuarioRepository;
import dsi.plantilla.demo.repositories.PuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PuestoRepository puestoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Sembrar Roles
        List<String> nombresRoles = Arrays.asList("ADMINISTRADOR", "JEFE", "SUPERVISOR", "CONTADORA");
        for (String nombre : nombresRoles) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre(nombre);
                rolRepository.save(nuevoRol);
            }
        }

        // 2. Sembrar Puesto Inicial (Para evitar errores de nulos)
        if (puestoRepository.findAll().isEmpty()) {
            Puesto p = new Puesto();
            p.setNombre("OPERARIO GENERAL");
            puestoRepository.save(p);
        }

        // 3. Sembrar Usuario Admin
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario adminUser = new Usuario();
            adminUser.setUsername("admin");
            adminUser.setNombreCompleto("Administrador");
            adminUser.setEmail("admin@constructora.com");
            adminUser.setPassword(passwordEncoder.encode("1234"));
            adminUser.setActivo(true);
            Rol adminRol = rolRepository.findByNombre("ADMINISTRADOR").get();
            adminUser.setRoles(new HashSet<>(Arrays.asList(adminRol)));
            usuarioRepository.save(adminUser);
        }
    }
}
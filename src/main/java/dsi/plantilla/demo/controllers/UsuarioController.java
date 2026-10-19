package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Usuario;
import dsi.plantilla.demo.services.RolService;
import dsi.plantilla.demo.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    // HU-31 Tarea 3: Mínimo 8 caracteres, al menos 1 número, 1 mayúscula, 1 minúscula y 1 símbolo
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("usuarios", usuarioService.buscar(q));
        model.addAttribute("q", q);
        model.addAttribute("nuevoUsuario", new Usuario());
        model.addAttribute("allRoles", rolService.listarTodos());
        return "usuarios/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Usuario usuario, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/usuarios";
        }

        // Validación de política de contraseñas robustas (HU-31 Tarea 3)
        if (usuario.getId() == null) {
            if (usuario.getPassword() == null || !PATTERN.matcher(usuario.getPassword()).matches()) {
                flash.addFlashAttribute("error", "Seguridad: La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y al menos un símbolo especial (@#$%^&+=!._-).");
                return "redirect:/usuarios";
            }
            if (usuarioService.existeUsername(usuario.getUsername())) {
                flash.addFlashAttribute("error", "Error: El nombre de usuario ya está en uso.");
                return "redirect:/usuarios";
            }
            if (usuarioService.existeEmail(usuario.getEmail())) {
                flash.addFlashAttribute("error", "Error: El correo electrónico ya está registrado.");
                return "redirect:/usuarios";
            }
        } else {
            // Si está editando y decidió cambiar contraseña
            if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
                if (!PATTERN.matcher(usuario.getPassword()).matches()) {
                    flash.addFlashAttribute("error", "Seguridad: La nueva contraseña no cumple con la política de seguridad (mínimo 8 caracteres con números, mayúsculas y símbolos).");
                    return "redirect:/usuarios";
                }
            }
        }

        usuarioService.guardar(usuario);
        flash.addFlashAttribute("success", "Usuario guardado exitosamente cumpliendo las políticas de seguridad.");
        return "redirect:/usuarios";
    }
}
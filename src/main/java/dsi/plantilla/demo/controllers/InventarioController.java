package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Herramienta;
import dsi.plantilla.demo.models.MovimientoInventario;
import dsi.plantilla.demo.models.Usuario;
import dsi.plantilla.demo.repositories.MovimientoInventarioRepository;
import dsi.plantilla.demo.services.FileService;
import dsi.plantilla.demo.services.InventarioService;
import dsi.plantilla.demo.services.ProyectoService;
import dsi.plantilla.demo.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/inventario")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE', 'SUPERVISOR')")
public class InventarioController {

    @Autowired private InventarioService inventarioService;
    @Autowired private ProyectoService proyectoService;
    @Autowired private FileService fileService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private MovimientoInventarioRepository movimientoRepository; // <-- NUEVO

    @GetMapping
    public String catalogo(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("herramientas", inventarioService.listar(q));
        model.addAttribute("proyectos", proyectoService.buscar(null));
        model.addAttribute("q", q);
        model.addAttribute("nuevaHerramienta", new Herramienta());
        
        MovimientoInventario mov = new MovimientoInventario();
        mov.setFecha(LocalDate.now());
        model.addAttribute("nuevoMovimiento", mov);
        
        return "inventario/catalogo";
    }

    // NUEVO ENDPOINT: Historial de la herramienta
    @GetMapping("/historial/{id}")
    public String historialHerramienta(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Herramienta> hOpt = inventarioService.buscarPorId(id);
        if (hOpt.isEmpty()) {
            flash.addFlashAttribute("error", "Herramienta no encontrada en la base de datos.");
            return "redirect:/inventario";
        }
        model.addAttribute("herramienta", hOpt.get());
        model.addAttribute("historial", movimientoRepository.findByHerramientaIdOrderByFechaDesc(id));
        return "inventario/historial";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("nuevaHerramienta") Herramienta herramienta, BindingResult result,
                          @RequestParam(value = "fileFoto", required = false) MultipartFile fileFoto,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Revise los campos requeridos."); return "redirect:/inventario";
        }
        if (herramienta.getId() == null && inventarioService.existeCodigo(herramienta.getCodigo())) {
            flash.addFlashAttribute("error", "El código de herramienta ya está en uso."); return "redirect:/inventario";
        }

        if (fileFoto != null && !fileFoto.isEmpty()) herramienta.setImagen(fileService.guardarArchivo(fileFoto));
        
        inventarioService.guardar(herramienta);
        flash.addFlashAttribute("success", "Herramienta registrada exitosamente en el catálogo.");
        return "redirect:/inventario";
    }

    @PostMapping("/movimiento")
    public String registrarMovimiento(@ModelAttribute MovimientoInventario mov, Authentication auth, RedirectAttributes flash) {
        try {
            Optional<Usuario> u = usuarioService.buscarPorUsername(auth.getName());
            u.ifPresent(mov::setRegistradoPor);
            
            inventarioService.registrarMovimiento(mov);
            flash.addFlashAttribute("success", "Movimiento de inventario procesado correctamente.");
        } catch (Exception e) {
            if ("STOCK_INSUFICIENTE".equals(e.getMessage())) {
                flash.addFlashAttribute("error", "Operación denegada: No hay stock suficiente para realizar esta asignación.");
            } else {
                flash.addFlashAttribute("error", "Error al procesar el movimiento.");
            }
        }
        return "redirect:/inventario";
    }
}
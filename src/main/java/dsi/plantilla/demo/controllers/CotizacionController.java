package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Cotizacion;
import dsi.plantilla.demo.services.ClienteService;
import dsi.plantilla.demo.services.CotizacionService;
import dsi.plantilla.demo.services.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/cotizaciones")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE')") // Solo Jefe y Admin gestionan esto
public class CotizacionController {

    @Autowired private CotizacionService cotizacionService;
    @Autowired private ClienteService clienteService;
    @Autowired private ProyectoService proyectoService;

    // HU-17: Consultar cotizaciones
    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("cotizaciones", cotizacionService.buscar(q));
        model.addAttribute("q", q);
        return "cotizaciones/lista";
    }

    // HU-15: Interfaz para Crear
    @GetMapping("/nueva")
    public String nuevaCotizacion(Model model) {
        model.addAttribute("cotizacion", new Cotizacion());
        model.addAttribute("clientes", clienteService.buscar(null));
        model.addAttribute("proyectos", proyectoService.buscar(null));
        return "cotizaciones/formulario";
    }

    // HU-16: Interfaz para Editar
    @GetMapping("/editar/{id}")
    public String editarCotizacion(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Cotizacion> c = cotizacionService.buscarPorId(id);
        if (c.isEmpty()) {
            flash.addFlashAttribute("error", "La cotización no existe.");
            return "redirect:/cotizaciones";
        }
        // Validar que no se edite si ya está Cerrada/Aprobada
        if ("Cerrada".equals(c.get().getEstado()) || "Aprobada".equals(c.get().getEstado())) {
            flash.addFlashAttribute("error", "No puedes editar una cotización que ya fue Aprobada o Cerrada.");
            return "redirect:/cotizaciones";
        }
        
        model.addAttribute("cotizacion", c.get());
        model.addAttribute("clientes", clienteService.buscar(null));
        model.addAttribute("proyectos", proyectoService.buscar(null));
        return "cotizaciones/formulario";
    }

    // Guarda tanto Nuevas (HU-15) como Ediciones (HU-16)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cotizacion cotizacion, RedirectAttributes flash) {
        cotizacionService.guardar(cotizacion);
        flash.addFlashAttribute("success", "Cotización guardada exitosamente.");
        return "redirect:/cotizaciones";
    }

    // HU-15 Tarea 4: Vista para imprimir el PDF de la cotización
    @GetMapping("/imprimir/{id}")
    public String imprimirCotizacion(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Cotizacion> c = cotizacionService.buscarPorId(id);
        if (c.isEmpty()) {
            flash.addFlashAttribute("error", "La cotización no existe.");
            return "redirect:/cotizaciones";
        }
        model.addAttribute("cot", c.get());
        return "cotizaciones/imprimir";
    }
}
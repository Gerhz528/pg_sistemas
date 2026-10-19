package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Factura;
import dsi.plantilla.demo.models.Usuario;
import dsi.plantilla.demo.services.BitacoraService;
import dsi.plantilla.demo.services.FacturaService;
import dsi.plantilla.demo.services.FileService;
import dsi.plantilla.demo.services.ProyectoService;
import dsi.plantilla.demo.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/facturas")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR', 'CONTADORA', 'JEFE')")
public class FacturaController {

    @Autowired private FacturaService facturaService;
    @Autowired private ProyectoService proyectoService;
    @Autowired private FileService fileService;
    @Autowired private UsuarioService usuarioService;
    @Autowired private BitacoraService bitacoraService;

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("facturas", facturaService.buscar(q));
        model.addAttribute("proyectos", proyectoService.buscar(null));
        model.addAttribute("q", q);
        model.addAttribute("nuevaFactura", new Factura());
        return "facturas/lista";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String guardar(@Valid @ModelAttribute("nuevaFactura") Factura factura, BindingResult result,
                          @RequestParam(value = "fileImagen", required = false) MultipartFile fileImagen,
                          Authentication auth, HttpServletRequest request, RedirectAttributes flash) {
        
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Error: Verifique los datos de la factura.");
            return "redirect:/facturas";
        }

        // HU-29 Tarea 2: No permitir fechas futuras en facturas/gastos
        if (factura.getFecha() != null && factura.getFecha().isAfter(LocalDate.now())) {
            flash.addFlashAttribute("error", "Regla de Negocio: No se permite registrar facturas con fecha posterior al día de hoy.");
            return "redirect:/facturas";
        }

        // HU-29 Tarea 1: Validación estricta de monto positivo
        if (factura.getMonto() == null || factura.getMonto() <= 0) {
            flash.addFlashAttribute("error", "Regla de Negocio: El monto de la factura debe ser mayor a $0.00.");
            return "redirect:/facturas";
        }

        if (fileImagen != null && !fileImagen.isEmpty()) {
            factura.setArchivoImagen(fileService.guardarArchivo(fileImagen));
        } else {
            flash.addFlashAttribute("error", "La fotografía del comprobante es obligatoria.");
            return "redirect:/facturas";
        }

        Optional<Usuario> supervisor = usuarioService.buscarPorUsername(auth.getName());
        supervisor.ifPresent(factura::setSupervisor);

        // HU-32 Tarea 1: Detección proactiva de sobrecosto (> 90%)
        Long proyId = factura.getProyecto().getId();
        boolean alertaSuperada = facturaService.superaUmbralAlerta(proyId, factura.getMonto());

        facturaService.guardar(factura);

        if (alertaSuperada) {
            String alertaMsg = "¡ALERTA FINANCIERA! El proyecto seleccionado ha superado el 90% del presupuesto cotizado.";
            flash.addFlashAttribute("warning", alertaMsg);
            bitacoraService.registrar("ALERTAS", "SOBRECOSTO_90", alertaMsg + " [Gasto: $" + factura.getMonto() + "]", request);
        } else {
            flash.addFlashAttribute("success", "Gasto registrado correctamente.");
        }

        return "redirect:/facturas";
    }
}
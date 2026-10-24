package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.repositories.FacturaGeneradaRepository;
import dsi.plantilla.demo.services.ClienteService;
import dsi.plantilla.demo.services.FacturacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/facturacion")
@PreAuthorize("hasRole('CONTADORA')") // Solo la contadora usa la integración con Llama
public class FacturacionController {

    @Autowired private FacturacionService facturacionService;
    @Autowired private FacturaGeneradaRepository facturaGeneradaRepository;
    @Autowired private ClienteService clienteService;

    // HU-18: Vista de Generación
    @GetMapping("/generar")
    public String generarVista(Model model) {
        model.addAttribute("clientes", clienteService.buscar(null));
        model.addAttribute("fechaInicio", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("fechaFin", LocalDate.now());
        return "facturacion/generar";
    }

    @PostMapping("/generar")
    public String generarArchivo(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            @RequestParam String formato,
            Authentication auth, RedirectAttributes flash) {
        
        try {
            facturacionService.generarArchivoFacturacion(clienteId, inicio, fin, formato, auth.getName());
            flash.addFlashAttribute("success", "Archivo " + formato + " generado exitosamente listo para Software Llama.");
            return "redirect:/facturacion/historial";
        } catch (Exception e) {
            if ("NO_DATA".equals(e.getMessage())) {
                flash.addFlashAttribute("info", "No hay datos financieros aprobados para este cliente en el rango seleccionado. No se generó archivo.");
            } else {
                flash.addFlashAttribute("error", "Ocurrió un error al generar el archivo.");
            }
            return "redirect:/facturacion/generar";
        }
    }

    // HU-19: Consultar Historial
    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("archivos", facturaGeneradaRepository.findAllByOrderByFechaGeneracionDesc());
        return "facturacion/historial";
    }
}
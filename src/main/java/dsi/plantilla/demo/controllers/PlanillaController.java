package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.dto.PlanillaResumenDTO;
import dsi.plantilla.demo.models.Planilla;
import dsi.plantilla.demo.services.PlanillaService;
import dsi.plantilla.demo.repositories.PlanillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/planillas")
@PreAuthorize("hasRole('CONTADORA')") 
public class PlanillaController {

    @Autowired private PlanillaService planillaService;
    @Autowired private PlanillaRepository planillaRepository;

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("fechaInicio", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("fechaFin", LocalDate.now());
        return "planillas/generar";
    }

    @PostMapping("/generar")
    public String generarPlanilla(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model, RedirectAttributes flash) {

        if (inicio.isAfter(fin)) { flash.addFlashAttribute("error", "La fecha de inicio no puede ser mayor a la fecha de fin."); return "redirect:/planillas"; }
        if (planillaRepository.existsOverlappingPlanilla(inicio, fin)) { flash.addFlashAttribute("error", "Ya existe una planilla guardada que choca con este rango de fechas. Revisa el historial."); return "redirect:/planillas"; }

        List<PlanillaResumenDTO> resumen = planillaService.simularPlanilla(inicio, fin);
        if (resumen.isEmpty()) { flash.addFlashAttribute("error", "No se encontraron asistencias en estado APROBADO en este período."); return "redirect:/planillas"; }

        model.addAttribute("resumen", resumen);
        model.addAttribute("fInicio", inicio);
        model.addAttribute("fFin", fin);
        
        // SUMATORIAS GLOBALES PARA EL PIE DE LA TABLA
        model.addAttribute("totalNomina", resumen.stream().mapToDouble(PlanillaResumenDTO::getSalarioNeto).sum());
        model.addAttribute("totalDeducciones", resumen.stream().mapToDouble(PlanillaResumenDTO::getDeducciones).sum());
        model.addAttribute("totalBonos", resumen.stream().mapToDouble(PlanillaResumenDTO::getBonos).sum()); // <-- NUEVO
        
        return "planillas/resultado";
    }

    @PostMapping("/guardar")
    public String guardarPlanilla(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            RedirectAttributes flash) {
        
        if (planillaRepository.existsOverlappingPlanilla(inicio, fin)) { flash.addFlashAttribute("error", "Intento de duplicidad bloqueado. Ya existe una planilla en este rango."); return "redirect:/planillas"; }
        planillaService.guardarPlanilla(inicio, fin);
        flash.addFlashAttribute("success", "¡Planilla registrada y cerrada exitosamente!");
        return "redirect:/planillas/historial";
    }

    @GetMapping("/historial")
    public String historialPlanillas(Model model) {
        model.addAttribute("planillasGuardadas", planillaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaGeneracion")));
        return "planillas/historial";
    }

    @GetMapping("/historial/{id}")
    public String verDetallePlanilla(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Planilla> planillaOpt = planillaRepository.findByIdWithDetalles(id);
        if (planillaOpt.isEmpty()) { flash.addFlashAttribute("error", "La planilla no existe."); return "redirect:/planillas/historial"; }
        model.addAttribute("planilla", planillaOpt.get());
        return "planillas/detalle";
    }
}
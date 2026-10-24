package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Proyecto;
import dsi.plantilla.demo.models.TareaProyecto;
import dsi.plantilla.demo.services.ClienteService;
import dsi.plantilla.demo.services.FacturaService;
import dsi.plantilla.demo.services.InventarioService;
import dsi.plantilla.demo.services.ProyectoService;
import dsi.plantilla.demo.services.TareaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/proyectos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE')")
public class ProyectoController {

    @Autowired private ProyectoService proyectoService;
    @Autowired private ClienteService clienteService;
    @Autowired private FacturaService facturaService;
    @Autowired private TareaService tareaService;
    @Autowired private InventarioService inventarioService;

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        List<Proyecto> proyectos = proyectoService.buscar(q);
        
        Map<Long, Double> gastosAcumulados = new HashMap<>();
        for (Proyecto p : proyectos) {
            gastosAcumulados.put(p.getId(), facturaService.calcularGastoAcumulado(p.getId()));
        }

        model.addAttribute("proyectos", proyectos);
        model.addAttribute("gastos", gastosAcumulados); 
        model.addAttribute("clientes", clienteService.buscar(null));
        model.addAttribute("q", q);
        model.addAttribute("nuevoProyecto", new Proyecto());
        return "proyectos/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("nuevoProyecto") Proyecto proyecto, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Error en el formulario: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/proyectos";
        }
        if (proyecto.getFechaFin() != null && proyecto.getFechaFin().isBefore(proyecto.getFechaInicio())) {
            flash.addFlashAttribute("error", "Error: La fecha de finalización no puede ser anterior a la fecha de inicio.");
            return "redirect:/proyectos";
        }

        proyectoService.guardar(proyecto);
        flash.addFlashAttribute("success", "Proyecto guardado exitosamente.");
        return "redirect:/proyectos";
    }

    // ==========================================
    // SECCIÓN DE GANTT Y TAREAS (HU-13 / HU-14)
    // ==========================================

    // ==========================================
    // DASHBOARD DEL PROYECTO (Gantt + Recursos)
    // ==========================================
    @GetMapping("/{id}/gantt")
    public String verGantt(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Proyecto> p = proyectoService.buscarPorId(id);
        if (p.isEmpty()) { 
            flash.addFlashAttribute("error", "Proyecto no encontrado."); 
            return "redirect:/proyectos"; 
        }
        
        model.addAttribute("proyecto", p.get());
        model.addAttribute("tareas", tareaService.listarPorProyecto(id));
        model.addAttribute("nuevaTarea", new TareaProyecto());
        
        // Agregamos los datos financieros y de inventario a la vista
        model.addAttribute("facturasProyecto", facturaService.listarPorProyecto(id));
        model.addAttribute("movimientosProyecto", inventarioService.listarMovimientosPorProyecto(id));
        
        // CORRECCIÓN: Si el cálculo da null (no hay facturas), enviamos un 0.0 explícito
        Double gastoCalculado = facturaService.calcularGastoAcumulado(id);
        model.addAttribute("gastoTotal", gastoCalculado != null ? gastoCalculado : 0.0);
        
        return "proyectos/gantt";
    }

    // ACTUALIZADO: Manejo de Bitácora
    @PostMapping("/tareas/guardar")
    public String guardarTarea(@ModelAttribute TareaProyecto tarea, 
                               @RequestParam(value = "nuevoComentario", required = false) String nuevoComentario,
                               Authentication auth, RedirectAttributes flash) {
        
        if (tarea.getFechaFin().isBefore(tarea.getFechaInicio())) {
            flash.addFlashAttribute("error", "La fecha fin no puede ser menor a la de inicio.");
        } else {
            // Lógica de Bitácora
            if (nuevoComentario != null && !nuevoComentario.trim().isEmpty()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"));
                String registro = "🔹 [" + timestamp + " - " + auth.getName() + "]: " + nuevoComentario + "\n";
                
                // Si ya tenía bitácora, la acumulamos. Si no, empieza de cero.
                String bitacoraActual = tarea.getBitacora() != null ? tarea.getBitacora() : "";
                tarea.setBitacora(registro + bitacoraActual);
            }

            tareaService.guardar(tarea);
            flash.addFlashAttribute("success", "Cronograma actualizado.");
        }
        return "redirect:/proyectos/" + tarea.getProyecto().getId() + "/gantt";
    }

    @PostMapping("/tareas/eliminar")
    public String eliminarTarea(@RequestParam Long idTarea, @RequestParam Long idProyecto) {
        tareaService.eliminar(idTarea);
        return "redirect:/proyectos/" + idProyecto + "/gantt";
    }

    @PostMapping("/{id}/gantt/autogenerar")
    public String autoGenerarGantt(@PathVariable Long id) {
        Proyecto p = proyectoService.buscarPorId(id).get();
        tareaService.autoGenerarCronogramaInteligente(p);
        return "redirect:/proyectos/" + id + "/gantt";
    }
}
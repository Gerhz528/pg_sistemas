package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Bono;
import dsi.plantilla.demo.models.Trabajador;
import dsi.plantilla.demo.models.Puesto;
import dsi.plantilla.demo.repositories.BonoRepository;
import dsi.plantilla.demo.services.FileService;
import dsi.plantilla.demo.services.TrabajadorService;
import dsi.plantilla.demo.repositories.PuestoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/trabajadores")
@PreAuthorize("hasRole('SUPERVISOR')") 
public class TrabajadorController {

    @Autowired private TrabajadorService trabajadorService;
    @Autowired private PuestoRepository puestoRepository;
    @Autowired private FileService fileService; 
    @Autowired private BonoRepository bonoRepository;

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("trabajadores", trabajadorService.buscar(q));
        model.addAttribute("q", q);
        model.addAttribute("puestos", puestoRepository.findAll());
        model.addAttribute("nuevoTrabajador", new Trabajador());
        return "trabajadores/lista";
    }

    // NUEVO ENDPOINT: Perfil Completo del Trabajador
    @GetMapping("/perfil/{id}")
    public String perfilCompleto(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Trabajador> t = trabajadorService.buscarPorId(id);
        if (t.isEmpty()) {
            flash.addFlashAttribute("error", "El trabajador no existe.");
            return "redirect:/trabajadores";
        }
        model.addAttribute("trabajador", t.get());
        model.addAttribute("bonos", bonoRepository.findByTrabajadorIdOrderByFechaDesc(id));
        model.addAttribute("nuevoBono", new Bono());
        model.addAttribute("puestos", puestoRepository.findAll());
        return "trabajadores/perfil";
    }

    // NUEVO ENDPOINT: Guardar Bono
    @PostMapping("/bonos/guardar")
    public String guardarBono(@ModelAttribute Bono bono, RedirectAttributes flash) {
        bonoRepository.save(bono);
        flash.addFlashAttribute("success", "Bono registrado exitosamente.");
        return "redirect:/trabajadores/perfil/" + bono.getTrabajador().getId();
    }

    @GetMapping("/puestos")
    public String listarPuestos(Model model) {
        model.addAttribute("puestos", puestoRepository.findAll());
        model.addAttribute("nuevoPuesto", new Puesto());
        return "trabajadores/puestos";
    }

    @PostMapping("/puestos/guardar")
    public String guardarPuesto(@ModelAttribute Puesto puesto, RedirectAttributes flash) {
        puestoRepository.save(puesto);
        flash.addFlashAttribute("success", "Puesto de trabajo registrado.");
        return "redirect:/trabajadores/puestos";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Trabajador trabajador, BindingResult result, 
                          @RequestParam(value = "fileFoto", required = false) MultipartFile fileFoto,
                          @RequestParam(value = "fileDui", required = false) MultipartFile fileDui,
                          @RequestParam(value = "fileCv", required = false) MultipartFile fileCv,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Error en el formulario: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/trabajadores";
        }
        if (trabajador.getId() == null && trabajadorService.existeDui(trabajador.getDui())) {
            flash.addFlashAttribute("error", "El DUI ingresado ya está registrado.");
            return "redirect:/trabajadores";
        }

        // Subida de Archivos
        if (fileFoto != null && !fileFoto.isEmpty()) trabajador.setFoto(fileService.guardarArchivo(fileFoto));
        if (fileDui != null && !fileDui.isEmpty()) trabajador.setDocumentoDui(fileService.guardarArchivo(fileDui));
        if (fileCv != null && !fileCv.isEmpty()) trabajador.setDocumentoCv(fileService.guardarArchivo(fileCv));

        trabajadorService.guardar(trabajador);
        flash.addFlashAttribute("success", "Datos del trabajador guardados correctamente.");
        
        // Si se editó desde el perfil, lo devolvemos ahí
        if (trabajador.getId() != null) return "redirect:/trabajadores/perfil/" + trabajador.getId();
        return "redirect:/trabajadores";
    }
}
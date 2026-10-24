package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.dto.AsistenciaDiariaDTO;
import dsi.plantilla.demo.models.*;
import dsi.plantilla.demo.repositories.*;
import dsi.plantilla.demo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/asistencias")
public class AsistenciaController {

    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private TrabajadorRepository trabajadorRepository;
    @Autowired private TrabajadorService trabajadorService;
    @Autowired private AsistenciaService asistenciaService;
    @Autowired private HoraExtraRepository horaExtraRepository;
    @Autowired private DiaCerradoRepository diaCerradoRepository;
    @Autowired private BonoRepository bonoRepository; // <-- NUEVA INYECCIÓN

    @GetMapping
    public String listar(Model model) {
        List<Asistencia> todas = asistenciaRepository.findAll();
        TreeMap<LocalDate, List<Asistencia>> agrupadas = todas.stream().collect(Collectors.groupingBy(Asistencia::getFecha, TreeMap::new, Collectors.toList()));
        model.addAttribute("asistenciasPorDia", agrupadas.descendingMap());
        model.addAttribute("totalActivos", trabajadorRepository.findAll().stream().filter(Trabajador::isActivo).count());
        model.addAttribute("diasCerrados", diaCerradoRepository.findAll().stream().map(DiaCerrado::getFecha).collect(Collectors.toList())); 
        return "asistencias/lista";
    }

    @PostMapping("/importar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'SUPERVISOR')")
    public String importarExcel(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes flash) {
        if (archivo.isEmpty()) { flash.addFlashAttribute("error", "Seleccione un archivo."); return "redirect:/asistencias"; }
        try {
            int registrados = asistenciaService.importarDesdeExcel(archivo.getInputStream());
            flash.addFlashAttribute("success", "Importación exitosa. Se registraron " + registrados + " jornadas.");
        } catch (Exception e) { flash.addFlashAttribute("error", "Error al procesar el archivo."); }
        return "redirect:/asistencias";
    }

    @PostMapping("/cerrar-dia")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String cerrarDia(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha, RedirectAttributes flash) {
        diaCerradoRepository.save(new DiaCerrado(fecha));
        flash.addFlashAttribute("success", "El día ha sido Finalizado.");
        return "redirect:/asistencias";
    }

    @GetMapping("/nueva")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String elegirFecha() { return "asistencias/elegir-fecha"; }

    @GetMapping("/diaria")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String reporteDiario(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha, Model model) {
        List<Trabajador> activos = trabajadorRepository.findAll().stream().filter(Trabajador::isActivo).collect(Collectors.toList());
        List<Asistencia> existentes = asistenciaRepository.findAll().stream().filter(a -> a.getFecha().equals(fecha)).collect(Collectors.toList());
        List<Asistencia> listaFinal = new ArrayList<>();
        Map<Long, Asistencia> ultimosRegistros = new HashMap<>(); 

        for (Trabajador t : activos) {
            Optional<Asistencia> asisOpt = existentes.stream().filter(a -> a.getTrabajador().getId().equals(t.getId())).findFirst();
            if (asisOpt.isPresent()) listaFinal.add(asisOpt.get()); 
            else { Asistencia nueva = new Asistencia(); nueva.setTrabajador(t); nueva.setFecha(fecha); nueva.setEstado("PENDIENTE"); listaFinal.add(nueva); }

            List<Asistencia> hist = asistenciaRepository.findByTrabajadorIdOrderByFechaDesc(t.getId());
            if (!hist.isEmpty()) ultimosRegistros.put(t.getId(), hist.get(0));
        }
        model.addAttribute("fecha", fecha);
        model.addAttribute("dto", new AsistenciaDiariaDTO(listaFinal));
        model.addAttribute("ultimos", ultimosRegistros);
        return "asistencias/registrar-masivo";
    }

    @PostMapping("/guardar-masivo")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String guardarMasivo(@ModelAttribute AsistenciaDiariaDTO dto, RedirectAttributes flash) {
        List<Asistencia> aGuardar = dto.getRegistros().stream().filter(a -> a.getId() == null && a.getHoraEntrada() != null && a.getHoraSalida() != null).collect(Collectors.toList());
        if(!aGuardar.isEmpty()) { asistenciaRepository.saveAll(aGuardar); flash.addFlashAttribute("success", "Se agregaron " + aGuardar.size() + " registros."); }
        return "redirect:/asistencias";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Optional<Trabajador> trabajadorOpt = trabajadorService.buscarPorId(id);
        if (trabajadorOpt.isEmpty()) { flash.addFlashAttribute("error", "Trabajador no encontrado."); return "redirect:/asistencias"; }
        List<Asistencia> historial = asistenciaService.historialPorTrabajador(id);
        Map<String, List<Asistencia>> jornadasAgrupadas = historial.stream().collect(Collectors.groupingBy(a -> a.getFecha().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase() + " " + a.getFecha().getYear(), LinkedHashMap::new, Collectors.toList()));
        
        // CÁLCULO DE BONOS TOTALES PARA LA VISTA
        List<Bono> bonos = bonoRepository.findByTrabajadorIdOrderByFechaDesc(id);
        double totalBonos = bonos.stream().mapToDouble(Bono::getMonto).sum();

        model.addAttribute("trabajador", trabajadorOpt.get());
        model.addAttribute("jornadasPorMes", jornadasAgrupadas);
        model.addAttribute("totalBaseGlobal", String.format("%.2f", historial.stream().mapToDouble(Asistencia::getHorasBaseSiAprobado).sum()));
        model.addAttribute("totalDiurnaGlobal", String.format("%.2f", historial.stream().mapToDouble(Asistencia::getHorasDiurnasAprobadas).sum()));
        model.addAttribute("totalNocturnaGlobal", String.format("%.2f", historial.stream().mapToDouble(Asistencia::getHorasNocturnasAprobadas).sum()));
        model.addAttribute("totalBonosGlobal", String.format("%.2f", totalBonos)); // <-- NUEVO

        return "asistencias/detalle";
    }

    @PostMapping("/revisar")
    @PreAuthorize("hasRole('JEFE')")
    public String revisarAsistencia(@RequestParam Long id, @RequestParam String accion, @RequestParam(required = false) String observaciones, Authentication auth, RedirectAttributes flash) {
        Asistencia asistencia = asistenciaRepository.findById(id).orElse(null);
        if (asistencia != null) {
            asistencia.setEstado(accion.equals("APROBAR") ? "APROBADO" : "RECHAZADO");
            asistencia.setObservacionesJefe(observaciones);
            asistencia.setFechaRevision(LocalDateTime.now());
            asistencia.setRevisadoPor(auth.getName());
            asistenciaRepository.save(asistencia);
        }
        return "redirect:/asistencias/detalle/" + (asistencia != null ? asistencia.getTrabajador().getId() : "");
    }

    @PostMapping("/horas-extra/guardar")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public String guardarHoraExtra(@ModelAttribute HoraExtra horaExtra, RedirectAttributes flash) {
        Asistencia asis = asistenciaRepository.findById(horaExtra.getAsistencia().getId()).orElse(null);
        if (asis != null && "PENDIENTE".equals(asis.getEstado())) {
            horaExtraRepository.save(horaExtra);
            flash.addFlashAttribute("success", "Horas extra añadidas.");
        }
        return "redirect:/asistencias/detalle/" + (asis != null ? asis.getTrabajador().getId() : "");
    }
}
package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.dto.ReporteKpisDTO;
import dsi.plantilla.demo.dto.ReporteProyectoDTO;
import dsi.plantilla.demo.repositories.ProyectoRepository;
import dsi.plantilla.demo.repositories.TrabajadorRepository;
import dsi.plantilla.demo.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    @Autowired private ReporteService reporteService;
    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private TrabajadorRepository trabajadorRepository;

    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate hoy = LocalDate.now();

        List<ReporteProyectoDTO> proyectosData = reporteService.generarReporteProyectos(inicioMes, hoy, null, null);
        ReporteKpisDTO kpis = reporteService.calcularKpis(proyectosData, inicioMes, hoy);

        long totalActivos = trabajadorRepository.findAll().stream().filter(t -> t.isActivo()).count();
        long obrasEnCurso = proyectoRepository.findAll().stream().filter(p -> "En Curso".equalsIgnoreCase(p.getEstado())).count();

        model.addAttribute("kpis", kpis);
        model.addAttribute("proyectosReporte", proyectosData);
        model.addAttribute("totalPersonalActivo", totalActivos);
        model.addAttribute("obrasActivas", obrasEnCurso);
        model.addAttribute("mesActual", inicioMes.getMonth().name());

        return "home";
    }
}
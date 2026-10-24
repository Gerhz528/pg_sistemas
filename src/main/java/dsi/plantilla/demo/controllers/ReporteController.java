package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.dto.ReporteKpisDTO;
import dsi.plantilla.demo.dto.ReporteProyectoDTO;
import dsi.plantilla.demo.services.ClienteService;
import dsi.plantilla.demo.services.ProyectoService;
import dsi.plantilla.demo.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE', 'CONTADORA')")
public class ReporteController {

    @Autowired private ReporteService reporteService;
    @Autowired private ClienteService clienteService;
    @Autowired private ProyectoService proyectoService;

    @GetMapping
    public String dashboardReportes(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(name = "fin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "proyectoId", required = false) Long proyectoId,
            Model model) {

        if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        List<ReporteProyectoDTO> tabla = reporteService.generarReporteProyectos(inicio, fin, clienteId, proyectoId);
        ReporteKpisDTO kpis = reporteService.calcularKpis(tabla, inicio, fin);

        model.addAttribute("proyectosReporte", tabla);
        model.addAttribute("kpis", kpis);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("proyectoId", proyectoId);
        model.addAttribute("clientes", clienteService.buscar(null));
        model.addAttribute("proyectos", proyectoService.buscar(null));

        return "reportes/dashboard";
    }

    @GetMapping("/imprimir")
    public String imprimirReporte(
            @RequestParam(name = "inicio") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(name = "fin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "proyectoId", required = false) Long proyectoId,
            Model model) {

        List<ReporteProyectoDTO> tabla = reporteService.generarReporteProyectos(inicio, fin, clienteId, proyectoId);
        ReporteKpisDTO kpis = reporteService.calcularKpis(tabla, inicio, fin);

        model.addAttribute("proyectosReporte", tabla);
        model.addAttribute("kpis", kpis);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        return "reportes/imprimir";
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(name = "inicio") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(name = "fin") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "proyectoId", required = false) Long proyectoId) {

        try {
            List<ReporteProyectoDTO> tabla = reporteService.generarReporteProyectos(inicio, fin, clienteId, proyectoId);
            ReporteKpisDTO kpis = reporteService.calcularKpis(tabla, inicio, fin);
            byte[] bytes = reporteService.exportarExcel(tabla, kpis, inicio, fin);

            String fileName = "Reporte_Gerencial_" + inicio + "_al_" + fin + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
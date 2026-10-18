package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Bitacora;
import dsi.plantilla.demo.services.BitacoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/auditoria")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE')")
public class AuditoriaController {

    @Autowired
    private BitacoraService bitacoraService;

    @GetMapping
    public String listarLogs(
            @RequestParam(name = "usuario", required = false) String usuario,
            @RequestParam(name = "modulo", required = false) String modulo,
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
            @RequestParam(name = "fin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fin,
            Model model) {

        if (inicio == null) inicio = LocalDate.now().minusDays(15);
        if (fin == null) fin = LocalDate.now();

        List<Bitacora> logs = bitacoraService.consultarHistorial(usuario, modulo, inicio, fin);

        model.addAttribute("logs", logs);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modulo", modulo);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);

        return "auditoria/historial";
    }
}
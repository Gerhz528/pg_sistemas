package dsi.plantilla.demo.controllers;

import dsi.plantilla.demo.models.Cliente;
import dsi.plantilla.demo.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'JEFE')")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("clientes", clienteService.buscar(q));
        model.addAttribute("q", q);
        model.addAttribute("nuevoCliente", new Cliente());
        return "clientes/lista";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("nuevoCliente") Cliente cliente, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Error en el formulario: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/clientes";
        }

        // Validación de negocio (HU-12 Tarea 3): Evitar duplicados de DUI al crear
        if (cliente.getId() == null && clienteService.existeDui(cliente.getDui())) {
            flash.addFlashAttribute("error", "El DUI ingresado ya pertenece a un cliente registrado.");
            return "redirect:/clientes";
        }

        clienteService.guardar(cliente);
        flash.addFlashAttribute("success", "Cliente guardado exitosamente.");
        return "redirect:/clientes";
    }
}
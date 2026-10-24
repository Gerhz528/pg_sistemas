package dsi.plantilla.demo.services;

import dsi.plantilla.demo.dto.PlanillaResumenDTO;
import dsi.plantilla.demo.models.Asistencia;
import dsi.plantilla.demo.models.Bono;
import dsi.plantilla.demo.models.DetallePlanilla;
import dsi.plantilla.demo.models.Planilla;
import dsi.plantilla.demo.models.Trabajador;
import dsi.plantilla.demo.repositories.AsistenciaRepository;
import dsi.plantilla.demo.repositories.BonoRepository;
import dsi.plantilla.demo.repositories.PlanillaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanillaService {

    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private PlanillaRepository planillaRepository;
    @Autowired private BonoRepository bonoRepository; // <-- INYECTADO

    private static final double TASA_DESCUENTOS = 0.1025;

    public List<PlanillaResumenDTO> simularPlanilla(LocalDate inicio, LocalDate fin) {
        List<Asistencia> aprobadas = asistenciaRepository.findAll().stream()
                .filter(a -> "APROBADO".equals(a.getEstado()))
                .filter(a -> !a.getFecha().isBefore(inicio) && !a.getFecha().isAfter(fin))
                .collect(Collectors.toList());

        Map<Trabajador, List<Asistencia>> porTrabajador = aprobadas.stream()
                .collect(Collectors.groupingBy(Asistencia::getTrabajador));

        List<PlanillaResumenDTO> resumen = new ArrayList<>();
        porTrabajador.forEach((t, lista) -> {
            double hBase = lista.stream().mapToDouble(Asistencia::getHorasTotales).sum();
            double hDiurnas = lista.stream().mapToDouble(Asistencia::getHorasDiurnasTotales).sum();
            double hNocturnas = lista.stream().mapToDouble(Asistencia::getHorasNocturnasTotales).sum();

            double tarifaHora = (t.getPuesto().getSalarioPorHora() != null) ? t.getPuesto().getSalarioPorHora() : 0.0;
            
            double salarioBase = hBase * tarifaHora;
            double pagoDiurnas = hDiurnas * (tarifaHora * 2.0); 
            double pagoNocturnas = hNocturnas * (tarifaHora * 2.5); 
            double totalExtras = pagoDiurnas + pagoNocturnas;

            // BÚSQUEDA Y CÁLCULO DE BONOS
            List<Bono> bonosDelPeriodo = bonoRepository.findByTrabajadorAndFechaBetween(t, inicio, fin);
            double totalBonos = bonosDelPeriodo.stream().mapToDouble(Bono::getMonto).sum();

            // Los bonos normalmente no aplican a descuentos de ley, se suman directo al neto
            double deducciones = (salarioBase + totalExtras) * TASA_DESCUENTOS;
            double neto = (salarioBase + totalExtras + totalBonos) - deducciones;

            resumen.add(new PlanillaResumenDTO(t, hBase, hDiurnas, hNocturnas, salarioBase, totalExtras, totalBonos, deducciones, neto));
        });

        resumen.sort(Comparator.comparing(r -> r.getTrabajador().getNombre()));
        return resumen;
    }

    @Transactional
    public Planilla guardarPlanilla(LocalDate inicio, LocalDate fin) {
        List<PlanillaResumenDTO> resumen = simularPlanilla(inicio, fin);
        if (resumen.isEmpty()) return null;

        Planilla p = new Planilla();
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        p.setFechaGeneracion(LocalDateTime.now());
        p.setTotalPagar(resumen.stream().mapToDouble(PlanillaResumenDTO::getSalarioNeto).sum());

        List<DetallePlanilla> detalles = resumen.stream().map(r -> {
            DetallePlanilla d = new DetallePlanilla();
            d.setPlanilla(p);
            d.setTrabajador(r.getTrabajador());
            d.setHorasBase(r.getHorasBase());
            d.setHorasExtrasDiurnas(r.getHorasExtrasDiurnas());
            d.setHorasExtrasNocturnas(r.getHorasExtrasNocturnas());
            d.setSalarioBase(r.getSalarioBase());
            d.setMontoExtras(r.getMontoExtras());
            d.setBonos(r.getBonos()); // GUARDA EL BONO EN LA BD
            d.setDeducciones(r.getDeducciones());
            d.setSalarioNeto(r.getSalarioNeto());
            return d;
        }).collect(Collectors.toList());

        p.setDetalles(detalles);
        return planillaRepository.save(p);
    }
}
package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Proyecto;
import dsi.plantilla.demo.models.TareaProyecto;
import dsi.plantilla.demo.repositories.TareaProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TareaService {

    @Autowired private TareaProyectoRepository tareaRepository;

    public List<TareaProyecto> listarPorProyecto(Long id) {
        return tareaRepository.findByProyectoIdOrderByFechaInicioAsc(id);
    }

    @Transactional
    public void guardar(TareaProyecto tarea) {
        tareaRepository.save(tarea);
    }
    
    @Transactional
    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }

    // ==========================================
    // ASISTENTE INTELIGENTE: AUTO-CRONOGRAMA
    // ==========================================
    @Transactional
    public void autoGenerarCronogramaInteligente(Proyecto p) {
        // Solo genera si no hay tareas
        if (!tareaRepository.findByProyectoIdOrderByFechaInicioAsc(p.getId()).isEmpty()) return;

        LocalDate inicio = p.getFechaInicio();
        LocalDate fin = p.getFechaFin() != null ? p.getFechaFin() : inicio.plusMonths(4); // 4 meses por defecto
        
        long diasTotales = ChronoUnit.DAYS.between(inicio, fin);
        long duracionFase = diasTotales / 5; // Dividimos en 5 fases lógicas de construcción

        String[] nombresFases = {
            "1. Limpieza y Excavación", 
            "2. Cimentación y Estructura", 
            "3. Obra Negra (Muros y Techos)", 
            "4. Instalaciones (Eléctrica/Plomería)", 
            "5. Obra Blanca y Acabados"
        };

        String lastId = "";
        LocalDate fechaFaseActual = inicio;

        for (int i = 0; i < 5; i++) {
            TareaProyecto t = new TareaProyecto();
            t.setProyecto(p);
            t.setNombre(nombresFases[i]);
            t.setProgreso(0);
            t.setFechaInicio(fechaFaseActual);
            
            // La última fase termina exactamente el día del fin del proyecto
            if (i == 4) t.setFechaFin(fin);
            else t.setFechaFin(fechaFaseActual.plusDays(duracionFase));

            t.setDependenciaStr(lastId); // Conecta la flecha con la tarea anterior
            
            tareaRepository.save(t);
            lastId = t.getId().toString();
            fechaFaseActual = t.getFechaFin().plusDays(1); // La sig fase inicia al día siguiente
        }
    }
}
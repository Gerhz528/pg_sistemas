package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Asistencia;
import dsi.plantilla.demo.models.Trabajador;
import dsi.plantilla.demo.repositories.AsistenciaRepository;
import dsi.plantilla.demo.repositories.TrabajadorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired private AsistenciaRepository asistenciaRepository;
    @Autowired private TrabajadorRepository trabajadorRepository;

    @Transactional(readOnly = true)
    public List<Asistencia> listarTodas() { return asistenciaRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Asistencia> historialPorTrabajador(Long trabajadorId) {
        return asistenciaRepository.findByTrabajadorIdOrderByFechaDesc(trabajadorId);
    }

    @Transactional
    public Asistencia guardar(Asistencia asistencia) { return asistenciaRepository.save(asistencia); }

    // NUEVO MOTOR: PROCESADOR DE EXCEL
    @Transactional
    public int importarDesdeExcel(InputStream is) throws Exception {
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0); // Leer la primera hoja
        DataFormatter formatter = new DataFormatter();
        List<Asistencia> asistenciasNuevas = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Saltar la fila 0 (Cabeceras)

            Cell cellDui = row.getCell(0);
            if (cellDui == null || formatter.formatCellValue(cellDui).trim().isEmpty()) continue;
            
            String dui = formatter.formatCellValue(cellDui).trim();
            Optional<Trabajador> tOpt = trabajadorRepository.findByDui(dui);
            if (tOpt.isEmpty()) continue; // Si el DUI no existe en BD, saltamos la fila

            Trabajador trabajador = tOpt.get();
            LocalDate fecha = parseDate(row.getCell(1));
            LocalTime entrada = parseTime(row.getCell(2));
            LocalTime salida = parseTime(row.getCell(3));

            if (fecha != null && entrada != null && salida != null) {
                // Validación para no meter duplicados si el empleado ya tenía registro ese día
                if (!asistenciaRepository.existsByTrabajadorAndFecha(trabajador, fecha)) {
                    Asistencia asis = new Asistencia();
                    asis.setTrabajador(trabajador);
                    asis.setFecha(fecha);
                    asis.setHoraEntrada(entrada);
                    asis.setHoraSalida(salida);
                    asis.setEstado("PENDIENTE");
                    asistenciasNuevas.add(asis);
                }
            }
        }
        workbook.close();
        asistenciaRepository.saveAll(asistenciasNuevas);
        return asistenciasNuevas.size(); // Retornamos cuántas se guardaron con éxito
    }

    // Funciones Helper para evitar que el Excel rompa las Fechas
    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String str = new DataFormatter().formatCellValue(cell).trim();
        try { return LocalDate.parse(str); } catch(Exception e) {}
        try { return LocalDate.parse(str, DateTimeFormatter.ofPattern("dd/MM/yyyy")); } catch(Exception e) {}
        return null;
    }

    private LocalTime parseTime(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        }
        String str = new DataFormatter().formatCellValue(cell).trim();
        try { return LocalTime.parse(str); } catch(Exception e) {}
        try { return LocalTime.parse(str + ":00"); } catch(Exception e) {} // Por si en excel pusieron "06:00" en lugar de "06:00:00"
        return null;
    }
}
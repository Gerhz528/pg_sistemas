package dsi.plantilla.demo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dsi.plantilla.demo.models.Cliente;
import dsi.plantilla.demo.models.Cotizacion;
import dsi.plantilla.demo.models.FacturaGenerada;
import dsi.plantilla.demo.repositories.ClienteRepository;
import dsi.plantilla.demo.repositories.CotizacionRepository;
import dsi.plantilla.demo.repositories.FacturaGeneradaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FacturacionService {

    @Autowired private FacturaGeneradaRepository facturaGeneradaRepository;
    @Autowired private CotizacionRepository cotizacionRepository;
    @Autowired private ClienteRepository clienteRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public FacturaGenerada generarArchivoFacturacion(Long clienteId, LocalDate inicio, LocalDate fin, String formato, String usuario) throws Exception {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() -> new Exception("Cliente no encontrado"));
        List<Cotizacion> cotizaciones = cotizacionRepository.buscarParaFacturacion(clienteId, inicio, fin);

        if (cotizaciones.isEmpty()) {
            throw new Exception("NO_DATA"); // Para mostrar mensaje informativo si está vacío
        }

        // Crear carpeta si no existe
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String nombreArchivo = "Facturacion_" + cliente.getDui() + "_" + System.currentTimeMillis() + (formato.equals("EXCEL") ? ".xlsx" : ".json");
        File archivoFisico = new File(UPLOAD_DIR + nombreArchivo);

        if (formato.equals("EXCEL")) {
            generarExcel(cotizaciones, archivoFisico);
        } else {
            generarJson(cliente, cotizaciones, archivoFisico);
        }

        // Guardar registro en BD (HU-18 Tarea 3)
        FacturaGenerada fg = new FacturaGenerada();
        fg.setCliente(cliente);
        fg.setRangoInicio(inicio);
        fg.setRangoFin(fin);
        fg.setFechaGeneracion(LocalDateTime.now());
        fg.setFormato(formato);
        fg.setArchivoUrl(nombreArchivo);
        fg.setGeneradoPor(usuario);

        return facturaGeneradaRepository.save(fg);
    }

    private void generarExcel(List<Cotizacion> cotizaciones, File archivo) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Datos_Sistema_Llama");

        // Cabeceras
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Numero_Cotizacion", "Fecha", "Proyecto", "Subtotal", "IVA", "Total_Facturar"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Llenar Datos
        int rowNum = 1;
        for (Cotizacion c : cotizaciones) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getNumero());
            row.createCell(1).setCellValue(c.getFecha().toString());
            row.createCell(2).setCellValue(c.getProyecto() != null ? c.getProyecto().getNombre() : "N/A");
            row.createCell(3).setCellValue(c.getSubtotal());
            row.createCell(4).setCellValue(c.getIva());
            row.createCell(5).setCellValue(c.getTotal());
        }

        FileOutputStream fileOut = new FileOutputStream(archivo);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private void generarJson(Cliente cliente, List<Cotizacion> cotizaciones, File archivo) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("cliente_nombre", cliente.getNombre());
        data.put("cliente_dui", cliente.getDui());
        
        List<Map<String, Object>> listaCot = new ArrayList<>();
        for (Cotizacion c : cotizaciones) {
            Map<String, Object> cot = new HashMap<>();
            cot.put("numero", c.getNumero());
            cot.put("fecha", c.getFecha().toString());
            cot.put("proyecto", c.getProyecto() != null ? c.getProyecto().getNombre() : null);
            cot.put("total", c.getTotal());
            listaCot.add(cot);
        }
        data.put("facturas_a_generar", listaCot);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON Bonito y formateado
        mapper.writeValue(archivo, data);
    }
}
package dsi.plantilla.demo.services;

import dsi.plantilla.demo.dto.ReporteKpisDTO;
import dsi.plantilla.demo.dto.ReporteProyectoDTO;
import dsi.plantilla.demo.models.Proyecto;
import dsi.plantilla.demo.repositories.CotizacionRepository;
import dsi.plantilla.demo.repositories.FacturaRepository;
import dsi.plantilla.demo.repositories.HoraExtraRepository;
import dsi.plantilla.demo.repositories.PlanillaRepository;
import dsi.plantilla.demo.repositories.ProyectoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired private ProyectoRepository proyectoRepository;
    @Autowired private CotizacionRepository cotizacionRepository;
    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PlanillaRepository planillaRepository;
    @Autowired private HoraExtraRepository horaExtraRepository;

    @Transactional(readOnly = true)
    public List<ReporteProyectoDTO> generarReporteProyectos(LocalDate inicio, LocalDate fin, Long clienteId, Long proyectoId) {
        List<Proyecto> proyectos = proyectoRepository.findAll();

        if (proyectoId != null) {
            proyectos = proyectos.stream().filter(p -> p.getId().equals(proyectoId)).collect(Collectors.toList());
        } else if (clienteId != null) {
            proyectos = proyectos.stream().filter(p -> p.getCliente() != null && p.getCliente().getId().equals(clienteId)).collect(Collectors.toList());
        }

        List<ReporteProyectoDTO> lista = new ArrayList<>();
        Double totalNominaGlobal = planillaRepository.sumarTotalPlanillasEnRango(inicio, fin);
        double totalProyectosCount = Math.max(1, proyectos.size());
        double nominaProrrateada = totalNominaGlobal / totalProyectosCount;

        for (Proyecto p : proyectos) {
            Double cotizado = cotizacionRepository.sumarTotalCotizadoPorProyecto(p.getId());
            Double facturas = facturaRepository.sumarGastosPorProyectoYFechas(p.getId(), inicio, fin);
            Double manoObra = nominaProrrateada;
            Double gastoTotal = facturas + manoObra;
            Double saldo = cotizado - gastoTotal;

            double porcentaje = cotizado > 0 ? (gastoTotal / cotizado) * 100.0 : 0.0;
            boolean alerta = cotizado > 0 && gastoTotal >= (cotizado * 0.90);

            lista.add(new ReporteProyectoDTO(
                    p.getId(),
                    p.getNombre(),
                    p.getCliente() != null ? p.getCliente().getNombre() : "Sin Cliente",
                    p.getEstado(),
                    cotizado,
                    facturas,
                    manoObra,
                    gastoTotal,
                    saldo,
                    Math.round(porcentaje * 100.0) / 100.0,
                    alerta
            ));
        }

        return lista;
    }

    @Transactional(readOnly = true)
    public ReporteKpisDTO calcularKpis(List<ReporteProyectoDTO> proyectosReporte, LocalDate inicio, LocalDate fin) {
        ReporteKpisDTO kpis = new ReporteKpisDTO();

        double totCot = proyectosReporte.stream().mapToDouble(ReporteProyectoDTO::getPresupuestoCotizado).sum();
        double totFact = proyectosReporte.stream().mapToDouble(ReporteProyectoDTO::getGastoFacturas).sum();
        double totNom = planillaRepository.sumarTotalPlanillasEnRango(inicio, fin);
        double totGasto = totFact + totNom;

        kpis.setTotalCotizado(totCot);
        kpis.setTotalFacturasGastos(totFact);
        kpis.setTotalNominaPagada(totNom);
        kpis.setTotalGastoAcumulado(totGasto);
        kpis.setBalanceGlobal(totCot - totGasto);
        kpis.setTotalHorasExtras(horaExtraRepository.sumarHorasExtrasAprobadasEnRango(inicio, fin));

        proyectosReporte.stream()
                .max((p1, p2) -> Double.compare(p1.getGastoTotal(), p2.getGastoTotal()))
                .ifPresent(p -> {
                    kpis.setProyectoMasCostoso(p.getProyectoNombre());
                    kpis.setMontoProyectoMasCostoso(p.getGastoTotal());
                });

        return kpis;
    }

    public byte[] exportarExcel(List<ReporteProyectoDTO> datos, ReporteKpisDTO kpis, LocalDate inicio, LocalDate fin) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resumen Ejecutivo");

        // Estilos
        Font fontTitulo = workbook.createFont();
        fontTitulo.setBold(true);
        fontTitulo.setFontHeightInPoints((short) 14);

        CellStyle styleTitulo = workbook.createCellStyle();
        styleTitulo.setFont(fontTitulo);

        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setColor(IndexedColors.WHITE.getIndex());

        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setFont(fontHeader);
        styleHeader.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);

        DataFormat format = workbook.createDataFormat();
        CellStyle styleMoneda = workbook.createCellStyle();
        styleMoneda.setDataFormat(format.getFormat("$#,##0.00"));

        CellStyle stylePorcentaje = workbook.createCellStyle();
        stylePorcentaje.setDataFormat(format.getFormat("0.0%"));

        // Título del Reporte
        Row rowTit = sheet.createRow(0);
        Cell cellTit = rowTit.createCell(0);
        cellTit.setCellValue("CONSTRUCTORA PANAMEÑO GARCIA - REPORTE DE GESTIÓN FINANCIERA");
        cellTit.setCellStyle(styleTitulo);

        Row rowSub = sheet.createRow(1);
        rowSub.createCell(0).setCellValue("Período evaluado: " + inicio + " al " + fin);

        // Encabezados de Tabla
        String[] columnas = {
                "ID Proyecto", "Nombre de Obra", "Cliente", "Estado", 
                "Presupuesto Cotizado", "Gastos en Facturas", "Mano de Obra", 
                "Costo Total", "Balance (Diferencia)", "% Ejecutado"
        };

        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < columnas.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(columnas[i]);
            c.setCellStyle(styleHeader);
        }

        int rIndex = 4;
        for (ReporteProyectoDTO p : datos) {
            Row r = sheet.createRow(rIndex++);
            r.createCell(0).setCellValue(p.getProyectoId());
            r.createCell(1).setCellValue(p.getProyectoNombre());
            r.createCell(2).setCellValue(p.getClienteNombre());
            r.createCell(3).setCellValue(p.getEstadoProyecto());

            Cell cCot = r.createCell(4);
            cCot.setCellValue(p.getPresupuestoCotizado());
            cCot.setCellStyle(styleMoneda);

            Cell cFact = r.createCell(5);
            cFact.setCellValue(p.getGastoFacturas());
            cFact.setCellStyle(styleMoneda);

            Cell cNom = r.createCell(6);
            cNom.setCellValue(p.getGastoNomina());
            cNom.setCellStyle(styleMoneda);

            Cell cTot = r.createCell(7);
            cTot.setCellValue(p.getGastoTotal());
            cTot.setCellStyle(styleMoneda);

            Cell cBal = r.createCell(8);
            cBal.setCellValue(p.getSaldoDiferencia());
            cBal.setCellStyle(styleMoneda);

            Cell cPorc = r.createCell(9);
            cPorc.setCellValue(p.getPorcentajeEjecutado() / 100.0);
            cPorc.setCellStyle(stylePorcentaje);
        }

        // Fila de Totales
        Row totalRow = sheet.createRow(rIndex + 1);
        Cell lblTotal = totalRow.createCell(3);
        lblTotal.setCellValue("TOTALES CONSOLIDADOS:");
        Font fTot = workbook.createFont();
        fTot.setBold(true);
        CellStyle stTot = workbook.createCellStyle();
        stTot.setFont(fTot);
        lblTotal.setCellStyle(stTot);

        Cell totCot = totalRow.createCell(4);
        totCot.setCellValue(kpis.getTotalCotizado());
        totCot.setCellStyle(styleMoneda);

        Cell totFact = totalRow.createCell(5);
        totFact.setCellValue(kpis.getTotalFacturasGastos());
        totFact.setCellStyle(styleMoneda);

        Cell totNom = totalRow.createCell(6);
        totNom.setCellValue(kpis.getTotalNominaPagada());
        totNom.setCellStyle(styleMoneda);

        Cell totGas = totalRow.createCell(7);
        totGas.setCellValue(kpis.getTotalGastoAcumulado());
        totGas.setCellStyle(styleMoneda);

        Cell totBal = totalRow.createCell(8);
        totBal.setCellValue(kpis.getBalanceGlobal());
        totBal.setCellStyle(styleMoneda);

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
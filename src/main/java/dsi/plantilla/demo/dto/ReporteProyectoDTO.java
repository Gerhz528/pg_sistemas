package dsi.plantilla.demo.dto;

import lombok.Data;

@Data
public class ReporteProyectoDTO {
    private Long proyectoId;
    private String proyectoNombre;
    private String clienteNombre;
    private String estadoProyecto;
    private Double presupuestoCotizado;
    private Double gastoFacturas;
    private Double gastoNomina;
    private Double gastoTotal;
    private Double saldoDiferencia;
    private Double porcentajeEjecutado;
    private boolean alertaSobrecosto;

    public ReporteProyectoDTO() {
    }

    public ReporteProyectoDTO(Long proyectoId, String proyectoNombre, String clienteNombre, 
                              String estadoProyecto, Double presupuestoCotizado, Double gastoFacturas, 
                              Double gastoNomina, Double gastoTotal, Double saldoDiferencia, 
                              Double porcentajeEjecutado, boolean alertaSobrecosto) {
        this.proyectoId = proyectoId;
        this.proyectoNombre = proyectoNombre;
        this.clienteNombre = clienteNombre;
        this.estadoProyecto = estadoProyecto;
        this.presupuestoCotizado = presupuestoCotizado;
        this.gastoFacturas = gastoFacturas;
        this.gastoNomina = gastoNomina;
        this.gastoTotal = gastoTotal;
        this.saldoDiferencia = saldoDiferencia;
        this.porcentajeEjecutado = porcentajeEjecutado;
        this.alertaSobrecosto = alertaSobrecosto;
    }
}
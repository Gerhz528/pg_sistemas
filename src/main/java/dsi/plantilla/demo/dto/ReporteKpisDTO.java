package dsi.plantilla.demo.dto;

import lombok.Data;

@Data
public class ReporteKpisDTO {
    private Double totalCotizado;
    private Double totalFacturasGastos;
    private Double totalNominaPagada;
    private Double totalGastoAcumulado;
    private Double balanceGlobal;
    private Double totalHorasExtras;
    private String proyectoMasCostoso;
    private Double montoProyectoMasCostoso;

    public ReporteKpisDTO() {
        this.totalCotizado = 0.0;
        this.totalFacturasGastos = 0.0;
        this.totalNominaPagada = 0.0;
        this.totalGastoAcumulado = 0.0;
        this.balanceGlobal = 0.0;
        this.totalHorasExtras = 0.0;
        this.proyectoMasCostoso = "N/A";
        this.montoProyectoMasCostoso = 0.0;
    }
}
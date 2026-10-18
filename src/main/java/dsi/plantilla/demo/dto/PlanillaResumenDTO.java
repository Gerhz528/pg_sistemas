package dsi.plantilla.demo.dto;

import dsi.plantilla.demo.models.Trabajador;
import lombok.Data;

@Data
public class PlanillaResumenDTO {
    private Trabajador trabajador;
    private Double horasBase;
    private Double horasExtrasDiurnas;
    private Double horasExtrasNocturnas;
    
    private Double salarioBase;
    private Double montoExtras;
    private Double bonos;
    private Double deducciones;
    private Double salarioNeto;

    public PlanillaResumenDTO() {
    }

    public PlanillaResumenDTO(Trabajador trabajador, Double horasBase, Double horasExtrasDiurnas, 
                             Double horasExtrasNocturnas, Double salarioBase, Double montoExtras, 
                             Double bonos, Double deducciones, Double salarioNeto) {
        this.trabajador = trabajador;
        this.horasBase = horasBase;
        this.horasExtrasDiurnas = horasExtrasDiurnas;
        this.horasExtrasNocturnas = horasExtrasNocturnas;
        this.salarioBase = salarioBase;
        this.montoExtras = montoExtras;
        this.bonos = bonos;
        this.deducciones = deducciones;
        this.salarioNeto = salarioNeto;
    }
}
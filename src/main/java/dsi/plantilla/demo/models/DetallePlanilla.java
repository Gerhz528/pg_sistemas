package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalle_planillas")
@Data
public class DetallePlanilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "planilla_id")
    private Planilla planilla;

    @ManyToOne
    @JoinColumn(name = "trabajador_id")
    private Trabajador trabajador;

    private Double horasBase;
    private Double horasExtrasDiurnas;
    private Double horasExtrasNocturnas;

    private Double salarioBase;
    private Double montoExtras;
    private Double bonos;
    private Double deducciones; 
    private Double salarioNeto;
}
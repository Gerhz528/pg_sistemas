package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalle_cotizaciones")
@Data
public class DetalleCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    private String descripcion;
    private Double cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
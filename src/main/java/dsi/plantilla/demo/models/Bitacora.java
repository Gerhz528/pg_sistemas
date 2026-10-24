package dsi.plantilla.demo.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_auditoria")
@Data
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String usuario;

    private String rol;

    @Column(nullable = false, length = 30)
    private String accion; // CREACION, MODIFICACION, ELIMINACION, CIERRE_PERIODO, REVISION

    @Column(nullable = false, length = 40)
    private String modulo; // PLANILLAS, COTIZACIONES, INVENTARIO, PROYECTOS, ASISTENCIAS, FACTURAS

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 50)
    private String direccionIp;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    public Bitacora() {
    }

    public Bitacora(String usuario, String rol, String accion, String modulo, 
                    String descripcion, String direccionIp, LocalDateTime fechaHora) {
        this.usuario = usuario;
        this.rol = rol;
        this.accion = accion;
        this.modulo = modulo;
        this.descripcion = descripcion;
        this.direccionIp = direccionIp;
        this.fechaHora = fechaHora;
    }
}
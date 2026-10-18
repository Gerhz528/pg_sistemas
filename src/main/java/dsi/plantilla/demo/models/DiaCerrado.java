package dsi.plantilla.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "dias_cerrados")
@Data
public class DiaCerrado {
    
    @Id
    private LocalDate fecha;

    public DiaCerrado() {
    }

    public DiaCerrado(LocalDate fecha) {
        this.fecha = fecha;
    }
}
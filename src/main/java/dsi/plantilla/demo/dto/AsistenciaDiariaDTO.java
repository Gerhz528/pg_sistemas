package dsi.plantilla.demo.dto;

import dsi.plantilla.demo.models.Asistencia;
import lombok.Data;

import java.util.List;

@Data
public class AsistenciaDiariaDTO {
    private List<Asistencia> registros;

    public AsistenciaDiariaDTO() {
    }

    public AsistenciaDiariaDTO(List<Asistencia> registros) {
        this.registros = registros;
    }
}
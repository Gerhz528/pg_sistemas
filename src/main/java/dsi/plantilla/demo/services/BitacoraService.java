package dsi.plantilla.demo.services;

import dsi.plantilla.demo.models.Bitacora;
import dsi.plantilla.demo.repositories.BitacoraRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class BitacoraService {

    @Autowired
    private BitacoraRepository bitacoraRepository;

    @Transactional
    public void registrar(String modulo, String accion, String descripcion, HttpServletRequest request) {
        String username = "ANONIMO";
        String rol = "INVITADO";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
            rol = auth.getAuthorities().toString().replace("[", "").replace("]", "");
        }

        String ip = obtenerIpCliente(request);

        Bitacora log = new Bitacora(username, rol, accion, modulo, descripcion, ip, LocalDateTime.now());
        bitacoraRepository.save(log);
    }

    @Transactional
    public void registrarConUsuarioEIp(String usuario, String rol, String modulo, String accion, String descripcion, String ip) {
        Bitacora log = new Bitacora(usuario, rol, accion, modulo, descripcion, ip, LocalDateTime.now());
        bitacoraRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<Bitacora> consultarHistorial(String usuario, String modulo, LocalDate fInicio, LocalDate fFin) {
        LocalDateTime desde = fInicio != null ? fInicio.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime hasta = fFin != null ? fFin.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        String userFiltro = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : null;
        String modFiltro = (modulo != null && !modulo.trim().isEmpty()) ? modulo.trim() : null;

        return bitacoraRepository.filtrarLogs(userFiltro, modFiltro, desde, hasta);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
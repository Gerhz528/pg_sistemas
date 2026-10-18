package dsi.plantilla.demo.config;

import dsi.plantilla.demo.services.BitacoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class LoginAuditListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    @Autowired
    private BitacoraService bitacoraService;

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            String rol = auth.getAuthorities().toString().replace("[", "").replace("]", "");

            String ip = "127.0.0.1";
            if (auth.getDetails() instanceof WebAuthenticationDetails) {
                ip = ((WebAuthenticationDetails) auth.getDetails()).getRemoteAddress();
            }

            bitacoraService.registrarConUsuarioEIp(
                    username,
                    rol,
                    "SEGURIDAD",
                    "INICIO_SESION",
                    "Inicio de sesión exitoso en la plataforma PG SISTEMA.",
                    ip
            );
        }
    }
}
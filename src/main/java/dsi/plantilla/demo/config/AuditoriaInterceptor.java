package dsi.plantilla.demo.config;

import dsi.plantilla.demo.services.BitacoraService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    @Autowired
    private BitacoraService bitacoraService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String method = request.getMethod();

        // Auditamos peticiones POST exitosas (status 200 a 303) sin excepciones
        if ("POST".equalsIgnoreCase(method) && response.getStatus() >= 200 && response.getStatus() <= 303 && ex == null) {
            String uri = request.getRequestURI();

            String modulo = null;
            String accion = "MODIFICACION";
            String detalle = "Ejecución de acción en: " + uri;

            // --- ASISTENCIAS Y CONTROL DE PERSONAL (SUPERVISOR / JEFE) ---
            if (uri.contains("/asistencias/guardar-masivo")) {
                modulo = "ASISTENCIAS";
                accion = "REGISTRO_MASIVO";
                detalle = "Registro masivo de jornada diaria de personal de obra.";
            } else if (uri.contains("/asistencias/guardar")) {
                modulo = "ASISTENCIAS";
                accion = "REGISTRO_INDIVIDUAL";
                detalle = "Registro individual de asistencia de trabajador.";
            } else if (uri.contains("/asistencias/horas-extra/guardar")) {
                modulo = "ASISTENCIAS";
                accion = "HORAS_EXTRA";
                detalle = "Asignación de horas extra (diurnas/nocturnas) a asistencia.";
            } else if (uri.contains("/asistencias/importar")) {
                modulo = "ASISTENCIAS";
                accion = "IMPORTACION_EXCEL";
                detalle = "Carga e importación masiva de asistencias desde archivo Excel.";
            } else if (uri.contains("/asistencias/cerrar-dia")) {
                modulo = "ASISTENCIAS";
                accion = "CIERRE_DIA";
                detalle = "Bloqueo y finalización formal de jornada de trabajo del día.";
            } else if (uri.contains("/asistencias/revisar")) {
                modulo = "ASISTENCIAS";
                accion = "REVISION";
                detalle = "Validación (Aprobación o Rechazo) de asistencia por parte del Jefe.";
            }

            // --- PLANILLAS Y NÓMINA (CONTADORA) ---
            else if (uri.contains("/planillas/guardar")) {
                modulo = "PLANILLAS";
                accion = "CIERRE_PERIODO";
                detalle = "Cierre definitivo y almacenamiento de planilla de sueldos.";
            }

            // --- GASTOS Y FACTURAS (SUPERVISOR) ---
            else if (uri.contains("/facturas/guardar")) {
                modulo = "FACTURAS";
                accion = "GASTO_CAMPO";
                detalle = "Subida de fotografía y registro de comprobante de compra o gasto.";
            }

            // --- INVENTARIO Y BODEGA (SUPERVISOR / JEFE) ---
            else if (uri.contains("/inventario/movimiento")) {
                modulo = "INVENTARIO";
                accion = "MOVIMIENTO";
                detalle = "Movimiento en bodega de herramientas (Asignación o Devolución).";
            } else if (uri.contains("/inventario/guardar")) {
                modulo = "INVENTARIO";
                accion = "CATALOGO";
                detalle = "Registro o actualización de herramienta en el catálogo.";
            }

            // --- PROYECTOS Y GANTT (JEFE) ---
            else if (uri.contains("/proyectos/guardar")) {
                modulo = "PROYECTOS";
                accion = "PROYECTO";
                detalle = "Creación o edición de datos generales del proyecto.";
            } else if (uri.contains("/proyectos/tareas/guardar")) {
                modulo = "PROYECTOS";
                accion = "CRONOGRAMA";
                detalle = "Modificación de actividad o progreso en el Diagrama de Gantt.";
            }

            // --- EXPEDIENTES Y PERSONAL (SUPERVISOR) ---
            else if (uri.contains("/trabajadores/guardar")) {
                modulo = "TRABAJADORES";
                accion = "EXPEDIENTE";
                detalle = "Registro o modificación de datos y documentos del trabajador.";
            } else if (uri.contains("/trabajadores/bonos/guardar")) {
                modulo = "TRABAJADORES";
                accion = "ASIGNACION_BONO";
                detalle = "Registro de bono o incentivo salarial a empleado.";
            } else if (uri.contains("/trabajadores/puestos/guardar")) {
                modulo = "TRABAJADORES";
                accion = "PUESTOS";
                detalle = "Configuración de puesto laboral y salario por hora.";
            }

            // --- COTIZACIONES (JEFE) ---
            else if (uri.contains("/cotizaciones/guardar")) {
                modulo = "COTIZACIONES";
                accion = "REGISTRO";
                detalle = "Creación o edición de cotización comercial para cliente.";
            }

            // --- CLIENTES (JEFE) ---
            else if (uri.contains("/clientes/guardar")) {
                modulo = "CLIENTES";
                accion = "REGISTRO";
                detalle = "Alta o actualización de cliente en el directorio.";
            }

            // --- USUARIOS Y ROLES (ADMINISTRADOR) ---
            else if (uri.contains("/usuarios/guardar")) {
                modulo = "USUARIOS";
                accion = "GESTION_CUENTA";
                detalle = "Creación o actualización de cuenta de usuario y asignación de roles.";
            }

            if (modulo != null) {
                bitacoraService.registrar(modulo, accion, detalle, request);
            }
        }
    }
}
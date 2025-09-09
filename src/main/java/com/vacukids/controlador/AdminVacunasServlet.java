package com.vacukids.controlador;

import com.vacukids.dao.CentroDAO;
import com.vacukids.dao.LoteDAO;
import com.vacukids.dao.StockDAO;
import com.vacukids.dao.VacunaDAO;
import com.vacukids.modelo.LoteVacuna;
import com.vacukids.modelo.Vacuna;
import static com.vacukids.utils.Jsons.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Sirve y procesa TODO el módulo en la URL /Pages/vacunas.jsp (los formularios
 * deben apuntar a esa misma ruta).
 *
 * Nota: Para evitar bucle, esta clase hace forward a /Pages/vacunas_view.jsp
 * (mismo directorio "Pages"). Renombra tu JSP actual a vacunas_view.jsp.
 */
@WebServlet("/Pages/vacunas.jsp")
public class AdminVacunasServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final VacunaDAO vacunaDAO = new VacunaDAO();
    private final LoteDAO loteDAO = new LoteDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final CentroDAO centroDAO = new CentroDAO();

    /* ===========================
       Helpers de parámetros
       =========================== */
    private static String str(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null) ? null : v.trim();
    }

    private static String required(HttpServletRequest req, String name) throws ServletException {
        String v = str(req, name);
        if (v == null || v.isBlank()) {
            throw new ServletException("Parámetro requerido: " + name);
        }
        return v;
    }

    private static Integer intOrNull(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int requiredInt(HttpServletRequest req, String name) throws ServletException {
        Integer v = intOrNull(req, name);
        if (v == null) {
            throw new ServletException("Parámetro numérico inválido: " + name);
        }
        return v;
    }

    private static LocalDate dateOrNull(HttpServletRequest req, String name) {
        String v = str(req, name);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (Exception e) {
            return null;
        }
    }

    /* ===========================
       GET: carga de listados/combos
       =========================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Filtros desde la vista
        String action = str(req, "action");          // listVacunas | listLotes | listStock | null
        String q = str(req, "q");               // búsqueda para vacunas (nombre/fabricante)
        Integer idVac = intOrNull(req, "idVacuna");  // filtro para lotes/stock
        String vigencia = str(req, "vigencia");        // "vigente" | "vencido" | null
        Integer idCentro = intOrNull(req, "idCentro");  // filtro para stock por centro

        try {
            // Combos/base
            List<Vacuna> vacunasAll = vacunaDAO.listar(null, 10_000, 0);
            req.setAttribute("vacunasAll", vacunasAll);
            req.setAttribute("centros", centroDAO.listarOpciones());
            req.setAttribute("lotesAll", loteDAO.listarTodos());

            // Vacunas (tabla izquierda)
            if (action == null || "listVacunas".equals(action)) {
                req.setAttribute("vacunas", vacunaDAO.listar(q, 1_000, 0));
            } else {
                req.setAttribute("vacunas", vacunaDAO.listar(null, 1_000, 0));
            }

            // Lotes (tabla central)
            if (action == null || "listLotes".equals(action)) {
                req.setAttribute("lotes", loteDAO.listar(idVac, vigencia));
            } else {
                req.setAttribute("lotes", loteDAO.listar(null, null));
            }

            // Stock (tabla derecha)
            if (action == null || "listStock".equals(action)) {
                req.setAttribute("stock", stockDAO.listar(idCentro, idVac));
            } else {
                req.setAttribute("stock", stockDAO.listar(null, null));
            }

        } catch (Exception e) {
            req.setAttribute("error", "Error al cargar página: " + e.getMessage());
        }

        // Vista real: /Pages/vacunas_view.jsp (mismo directorio Pages)
        req.getRequestDispatcher("/Pages/vacunas_view.jsp").forward(req, resp);
    }

    /* ===========================
       POST: acciones de CRUD
       =========================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = str(req, "action");

        try {
            /* ========= Vacunas ========= */
            if ("createVacuna".equals(action) || "create".equals(action)) {
                Vacuna v = new Vacuna();
                v.setNombre(required(req, "nombre"));
                v.setDescripcion(str(req, "descripcion"));
                v.setFabricante(str(req, "fabricante"));
                vacunaDAO.crear(v);
                req.setAttribute("ok", "Vacuna creada correctamente.");

            } else if ("editVacuna".equals(action) || "update".equals(action)) {
                Integer idVac = intOrNull(req, "id");
                if (idVac == null) {
                    idVac = intOrNull(req, "idVacuna");
                }
                if (idVac == null) {
                    throw new ServletException("Falta id de vacuna.");
                }

                Vacuna v = new Vacuna();
                v.setIdVacuna(idVac);
                v.setNombre(required(req, "nombre"));
                v.setDescripcion(str(req, "descripcion"));
                v.setFabricante(str(req, "fabricante"));
                vacunaDAO.actualizar(v);
                req.setAttribute("ok", "Vacuna actualizada correctamente.");

            } else if ("deleteVacuna".equals(action) || "delete".equals(action)) {
                Integer idVac = intOrNull(req, "id");
                if (idVac == null) {
                    idVac = intOrNull(req, "idVacuna");
                }
                if (idVac == null) {
                    throw new ServletException("Falta id de vacuna.");
                }

                vacunaDAO.eliminar(idVac);
                req.setAttribute("ok", "Vacuna eliminada.");

                /* ========= Lotes ========= */
            } else if ("createLote".equals(action)) {
                LoteVacuna l = new LoteVacuna();
                l.setIdVacuna(requiredInt(req, "idVacuna"));
                l.setLoteCodigo(required(req, "loteCodigo"));
                l.setFechaFabricacion(dateOrNull(req, "fechaFabricacion"));
                l.setFechaVencimiento(dateOrNull(req, "fechaVencimiento"));
                l.setProveedor(str(req, "proveedor"));
                int newId = loteDAO.crear(l);
                req.setAttribute("ok", "Lote creado (id " + newId + ").");

            } else if ("editLote".equals(action)) {
                LoteVacuna l = new LoteVacuna();
                l.setIdLote(requiredInt(req, "id"));
                l.setIdVacuna(requiredInt(req, "idVacuna"));
                l.setLoteCodigo(required(req, "loteCodigo"));
                l.setFechaFabricacion(dateOrNull(req, "fechaFabricacion"));
                l.setFechaVencimiento(dateOrNull(req, "fechaVencimiento"));
                l.setProveedor(str(req, "proveedor"));

                if (loteDAO.editar(l)) {
                    req.setAttribute("ok", "Lote actualizado.");
                } else {
                    req.setAttribute("error", "No se actualizó el lote (id no encontrado).");
                }

            } else if ("deleteLote".equals(action)) {
                int idLote = requiredInt(req, "id");
                if (loteDAO.eliminar(idLote)) {
                    req.setAttribute("ok", "Lote eliminado.");
                } else {
                    req.setAttribute("error", "No se eliminó el lote (id no encontrado).");
                }

                /* ========= Stock ========= */
            } else if ("updateStock".equals(action)) {
                int idCentro = requiredInt(req, "idCentro");
                int idLote = requiredInt(req, "idLote");
                int cantidad = requiredInt(req, "cantidad");
                if (cantidad < 0) {
                    cantidad = 0; // normalizar
                }
                stockDAO.upsert(idCentro, idLote, cantidad);
                req.setAttribute("ok", "Stock actualizado.");

            } else {
                req.setAttribute("error", "Acción no soportada: " + action);
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            req.setAttribute("error", "Conflicto de datos: " + dup.getMessage());
        } catch (SQLException ex) {
            req.setAttribute("error", "Error SQL: " + (ex.getMessage() == null ? "desconocido" : ex.getMessage()));
        } catch (Exception ex) {
            req.setAttribute("error", "Error: " + ex.getMessage());
        }

        // Tras la operación, recargar la misma URL con listados y mensajes
        doGet(req, resp);
    }
}

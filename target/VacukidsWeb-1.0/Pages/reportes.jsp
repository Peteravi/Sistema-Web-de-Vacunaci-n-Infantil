<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>VacuKids · Reportes</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <!-- Solo Bootstrap 5 + Icons (sin estilos del proyecto) -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
         <link rel="stylesheet" href="<%=ctx%>/assets/css/reporte.css">  
    </head>
    <body>

        <header class="border-bottom">
            <div class="container d-flex align-items-center justify-content-between py-3">
                <h1 class="h3 m-0 d-flex align-items-center gap-2">
                    <i class="bi bi-graph-up text-primary" aria-hidden="true"></i>
                    <span>Reportes y Listados</span>
                </h1>
                <nav>
                    <a class="btn btn-outline-secondary" href="<%=ctx%>/Pages/admin.jsp">
                        <i class="bi bi-chevron-left me-1"></i> Volver al menú
                    </a>
                </nav>
            </div>
        </header>

        <main class="container py-4">

            <!-- Notificaciones -->
            <c:if test="${param.ok == '1'}">
                <div role="status" class="alert alert-success d-flex align-items-center gap-2">
                    <i class="bi bi-check-circle-fill"></i> <div>Operación realizada correctamente.</div>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div role="alert" class="alert alert-danger d-flex align-items-center gap-2">
                    <i class="bi bi-exclamation-triangle-fill"></i> <div>Error: ${error}</div>
                </div>
            </c:if>

            <!-- ================================================== -->
            <!-- Estadísticas de vacunación (agrupadas por fecha)   -->
            <!-- ================================================== -->
            <section aria-label="Estadísticas" class="mb-5">
                <div class="d-flex align-items-center justify-content-between mb-2">
                    <h2 class="h4 m-0 d-flex align-items-center gap-2">
                        <i class="bi bi-bar-chart-line text-primary"></i>
                        <span>Estadísticas</span>
                    </h2>
                    <a class="btn btn-outline-success"
                       href="<%=ctx%>/admin/reportes?action=statsExport&format=csv&${pageContext.request.queryString}">
                        <i class="bi bi-filetype-csv me-1"></i> Exportar CSV
                    </a>
                </div>

                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-body">
                        <form class="row g-3 align-items-end" method="get" action="<%=ctx%>/admin/reportes">
                            <input type="hidden" name="action" value="stats">

                            <div class="col-12 col-md-4">
                                <label for="st_centro" class="form-label">Centro</label>
                                <select id="st_centro" name="idCentro" class="form-select">
                                    <option value="">Todos</option>
                                    <c:forEach var="c" items="${centros}">
                                        <option value="${c.idCentro}" ${param.idCentro==c.idCentro?'selected':''}>${c.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-12 col-md-4">
                                <label for="st_vacuna" class="form-label">Vacuna</label>
                                <select id="st_vacuna" name="idVacuna" class="form-select">
                                    <option value="">Todas</option>
                                    <c:forEach var="v" items="${vacunas}">
                                        <option value="${v.idVacuna}" ${param.idVacuna==v.idVacuna?'selected':''}>${v.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-6 col-md-2">
                                <label for="st_desde" class="form-label">Desde</label>
                                <input id="st_desde" type="date" name="desde" value="${param.desde}" class="form-control">
                            </div>
                            <div class="col-6 col-md-2">
                                <label for="st_hasta" class="form-label">Hasta</label>
                                <input id="st_hasta" type="date" name="hasta" value="${param.hasta}" class="form-control">
                            </div>

                            <div class="col-12">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-search me-1"></i> Consultar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Centro</th>
                                        <th>Vacuna</th>
                                        <th>Dosis aplicadas</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="s" items="${stats}">
                                        <tr>
                                            <td>${s.fecha}</td>
                                            <td>${s.centro}</td>
                                            <td>${s.vacuna}</td>
                                            <td><span class="badge text-bg-primary">${s.totalDosis}</span></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty stats}">
                                        <tr><td colspan="4" class="text-center text-muted py-4">Sin datos para los filtros seleccionados.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

            <!-- =============================================== -->
            <!-- Listado de Pacientes vacunados (detalle)        -->
            <!-- =============================================== -->
            <section aria-label="Pacientes vacunados" class="mb-5">
                <div class="d-flex align-items-center justify-content-between mb-2">
                    <h2 class="h4 m-0 d-flex align-items-center gap-2">
                        <i class="bi bi-people text-primary"></i>
                        <span>Pacientes vacunados</span>
                    </h2>
                    <a class="btn btn-outline-success"
                       href="<%=ctx%>/admin/reportes?action=listPacientesExport&format=csv&${pageContext.request.queryString}">
                        <i class="bi bi-filetype-csv me-1"></i> Exportar CSV
                    </a>
                </div>

                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-body">
                        <form class="row g-3 align-items-end" method="get" action="<%=ctx%>/admin/reportes">
                            <input type="hidden" name="action" value="listPacientes">

                            <div class="col-12 col-md-3">
                                <label for="lp_centro" class="form-label">Centro</label>
                                <select id="lp_centro" name="idCentro" class="form-select">
                                    <option value="">Todos</option>
                                    <c:forEach var="c" items="${centros}">
                                        <option value="${c.idCentro}" ${param.idCentro==c.idCentro?'selected':''}>${c.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-12 col-md-3">
                                <label for="lp_vacuna" class="form-label">Vacuna</label>
                                <select id="lp_vacuna" name="idVacuna" class="form-select">
                                    <option value="">Todas</option>
                                    <c:forEach var="v" items="${vacunas}">
                                        <option value="${v.idVacuna}" ${param.idVacuna==v.idVacuna?'selected':''}>${v.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-6 col-md-2">
                                <label for="lp_desde" class="form-label">Desde</label>
                                <input id="lp_desde" type="date" name="desde" value="${param.desde}" class="form-control">
                            </div>
                            <div class="col-6 col-md-2">
                                <label for="lp_hasta" class="form-label">Hasta</label>
                                <input id="lp_hasta" type="date" name="hasta" value="${param.hasta}" class="form-control">
                            </div>

                            <div class="col-12 col-md-2">
                                <label for="lp_q" class="form-label">Buscar</label>
                                <input id="lp_q" name="q" value="${param.q}" class="form-control" placeholder="Cédula o nombre">
                            </div>

                            <div class="col-12">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-search me-1"></i> Buscar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card shadow-sm border-0">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Centro</th>
                                        <th>Paciente</th>
                                        <th>Cédula</th>
                                        <th>Vacuna</th>
                                        <th>Dosis #</th>
                                        <th>Efectos secundarios</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="r" items="${pacientesVacunados}">
                                        <tr>
                                            <td>${r.fechaAplicacion}</td>
                                            <td>${r.centro}</td>
                                            <td>${r.paciente}</td>
                                            <td>${r.cedulaPaciente}</td>
                                            <td>${r.vacuna}</td>
                                            <td><span class="badge text-bg-info">${r.dosisNumero}</span></td>
                                            <td class="text-truncate" style="max-width: 360px;" title="${r.efectosSecundarios}">
                                                ${r.efectosSecundarios}
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty pacientesVacunados}">
                                        <tr><td colspan="7" class="text-center text-muted py-4">No hay registros con los filtros aplicados.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

        </main>

        <!-- Bootstrap Bundle -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" defer></script>
    </body>
</html>

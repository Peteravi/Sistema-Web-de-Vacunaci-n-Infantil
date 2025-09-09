<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>VacuKids · Vacunas y Lotes</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <!-- Bootstrap 5 + Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
          <link rel="stylesheet" href="<%=ctx%>/assets/css/vacunas.css"> 

        <!-- Proyecto -->
        <link rel="icon" href="<%=ctx%>/img/logo.png">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/admin.css">
    </head>
    <body>

        <header class="border-bottom">
            <div class="container d-flex align-items-center justify-content-between py-3">
                <h1 class="h3 m-0 d-flex align-items-center gap-2">
                    <i class="bi bi-capsule-pill text-primary" aria-hidden="true"></i>
                    <span>Vacunas · Lotes · Stock</span>
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

            <!-- ================================= -->
            <!-- Sección: Vacunas                   -->
            <!-- ================================= -->
            <section aria-label="Vacunas" class="mb-4">
                <div class="d-flex align-items-center justify-content-between mb-2">
                    <h2 class="h4 m-0 d-flex align-items-center gap-2">
                        <i class="bi bi-shield-plus text-primary" aria-hidden="true"></i>
                        <span>Vacunas</span>
                    </h2>
                    <button type="button" data-modal-open="#modalNuevaVacuna" class="btn btn-success">
                        <i class="bi bi-plus-circle me-1"></i> Nueva vacuna
                    </button>
                </div>

                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-body">
                        <form class="row g-3 align-items-end" method="get" action="<%=ctx%>/Pages/vacunas.jsp">
                            <input type="hidden" name="action" value="listVacunas">
                            <div class="col-12 col-md-6">
                                <label for="fv_q" class="form-label">Buscar (nombre o fabricante)</label>
                                <input id="fv_q" name="q" value="${param.q}" class="form-control" placeholder="Ej. SRP, DTP, Pfizer...">
                            </div>
                            <div class="col-12 col-md-6">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-funnel me-1"></i> Filtrar
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
                                        <th>ID</th>
                                        <th>Nombre</th>
                                        <th>Fabricante</th>
                                        <th>Descripción</th>
                                        <th>Creado</th>
                                        <th class="text-nowrap">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="v" items="${vacunas}">
                                        <tr
                                            data-id="${v.idVacuna}"
                                            data-nombre="${v.nombre}"
                                            data-fabricante="${v.fabricante}"
                                            data-descripcion="${v.descripcion}">
                                            <td>${v.idVacuna}</td>
                                            <td>${v.nombre}</td>
                                            <td>${v.fabricante}</td>
                                            <td class="text-truncate" style="max-width: 360px;" title="${v.descripcion}">${v.descripcion}</td>
                                            <td>${v.creadoEn}</td>
                                            <td class="text-nowrap">
                                                <button type="button" class="btn btn-sm btn-outline-primary me-1"
                                                        data-modal-open="#modalEditarVacuna" data-fill="editarVacuna">
                                                    <i class="bi bi-pencil-square me-1"></i> Editar
                                                </button>
                                                <button type="button" class="btn btn-sm btn-outline-danger"
                                                        data-modal-open="#modalEliminarVacuna" data-fill="eliminarVacuna">
                                                    <i class="bi bi-trash3 me-1"></i> Eliminar
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty vacunas}">
                                        <tr><td colspan="6" class="text-center text-muted py-4">Sin resultados.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Modal: Nueva vacuna -->
            <dialog id="modalNuevaVacuna" aria-labelledby="nv_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="createVacuna">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0" id="nv_title"><i class="bi bi-plus-circle me-2"></i>Nueva vacuna</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <div class="mb-3">
                            <label for="nv_nombre" class="form-label">Nombre</label>
                            <input id="nv_nombre" name="nombre" required class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="nv_fabricante" class="form-label">Fabricante</label>
                            <input id="nv_fabricante" name="fabricante" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="nv_dosis" class="form-label">Dosis requeridas (opcional)</label>
                            <input id="nv_dosis" name="dosisRequeridas" type="number" min="1" step="1" class="form-control">
                        </div>
                        <div class="mb-0">
                            <label for="nv_desc" class="form-label">Descripción</label>
                            <input id="nv_desc" name="descripcion" maxlength="255" class="form-control" placeholder="Esquema, notas, etc.">
                        </div>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-save2 me-1"></i> Guardar
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- Modal: Editar vacuna -->
            <dialog id="modalEditarVacuna" aria-labelledby="ev_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="editVacuna">
                    <input type="hidden" name="id" id="ev_id">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0" id="ev_title"><i class="bi bi-pencil-square me-2"></i>Editar vacuna</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <div class="mb-3">
                            <label for="ev_nombre" class="form-label">Nombre</label>
                            <input id="ev_nombre" name="nombre" required class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="ev_fabricante" class="form-label">Fabricante</label>
                            <input id="ev_fabricante" name="fabricante" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="ev_dosis" class="form-label">Dosis requeridas (opcional)</label>
                            <input id="ev_dosis" name="dosisRequeridas" type="number" min="1" step="1" class="form-control">
                        </div>
                        <div class="mb-0">
                            <label for="ev_desc" class="form-label">Descripción</label>
                            <input id="ev_desc" name="descripcion" maxlength="255" class="form-control">
                        </div>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i> Guardar cambios
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- Modal: Eliminar vacuna -->
            <dialog id="modalEliminarVacuna" aria-labelledby="delv_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="deleteVacuna">
                    <input type="hidden" name="id" id="delv_id">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0 text-danger" id="delv_title"><i class="bi bi-trash3-fill me-2"></i>Eliminar vacuna</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <p class="mb-0">¿Seguro que deseas eliminar esta vacuna?</p>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-trash3 me-1"></i> Eliminar
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- ================================= -->
            <!-- Sección: Lotes                    -->
            <!-- ================================= -->
            <section aria-label="Lotes de vacunas" class="mb-4 mt-5">
                <div class="d-flex align-items-center justify-content-between mb-2">
                    <h2 class="h4 m-0 d-flex align-items-center gap-2">
                        <i class="bi bi-box-seam text-primary" aria-hidden="true"></i>
                        <span>Lotes de Vacunas</span>
                    </h2>
                    <button type="button" data-modal-open="#modalNuevoLote" class="btn btn-success">
                        <i class="bi bi-plus-circle me-1"></i> Nuevo lote
                    </button>
                </div>

                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-body">
                        <form class="row g-3 align-items-end" method="get" action="<%=ctx%>/Pages/vacunas.jsp">
                            <input type="hidden" name="action" value="listLotes">
                            <div class="col-12 col-md-4">
                                <label for="fl_vacuna" class="form-label">Vacuna</label>
                                <select id="fl_vacuna" name="idVacuna" class="form-select">
                                    <option value="">Todas</option>
                                    <c:forEach var="v" items="${vacunasAll}">
                                        <option value="${v.idVacuna}" ${param.idVacuna==v.idVacuna?'selected':''}>${v.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <label for="fl_estado" class="form-label">Vigencia</label>
                                <select id="fl_estado" name="vigencia" class="form-select">
                                    <option value="">Todas</option>
                                    <option value="vigente" ${param.vigencia=='vigente'?'selected':''}>Vigente</option>
                                    <option value="vencido" ${param.vigencia=='vencido'?'selected':''}>Vencido</option>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-funnel me-1"></i> Filtrar
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
                                        <th>ID</th>
                                        <th>Vacuna</th>
                                        <th>Lote</th>
                                        <th>Fabricación</th>
                                        <th>Vencimiento</th>
                                        <th>Proveedor</th>
                                        <th class="text-nowrap">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="l" items="${lotes}">
                                        <tr
                                            data-id="${l.idLote}"
                                            data-vacunaid="${l.idVacuna}"
                                            data-lote="${l.loteCodigo}"
                                            data-ff="${l.fechaFabricacion}"
                                            data-fv="${l.fechaVencimiento}"
                                            data-prov="${l.proveedor}">
                                            <td>${l.idLote}</td>
                                            <td>${l.vacuna}</td>
                                            <td>${l.loteCodigo}</td>
                                            <td>${l.fechaFabricacion}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${l.vigente}"><span class="badge text-bg-success" title="Aún vigente">${l.fechaVencimiento}</span></c:when>
                                                    <c:otherwise><span class="badge text-bg-danger" title="Vencido">${l.fechaVencimiento}</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${l.proveedor}</td>
                                            <td class="text-nowrap">
                                                <button type="button" class="btn btn-sm btn-outline-primary me-1"
                                                        data-modal-open="#modalEditarLote" data-fill="editarLote">
                                                    <i class="bi bi-pencil-square me-1"></i> Editar
                                                </button>
                                                <button type="button" class="btn btn-sm btn-outline-danger"
                                                        data-modal-open="#modalEliminarLote" data-fill="eliminarLote">
                                                    <i class="bi bi-trash3 me-1"></i> Eliminar
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty lotes}">
                                        <tr><td colspan="7" class="text-center text-muted py-4">Sin resultados.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Modal: Nuevo lote -->
            <dialog id="modalNuevoLote" aria-labelledby="nl_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="createLote">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0" id="nl_title"><i class="bi bi-plus-circle me-2"></i>Nuevo lote</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <div class="mb-3">
                            <label for="nl_vacuna" class="form-label">Vacuna</label>
                            <select id="nl_vacuna" name="idVacuna" required class="form-select">
                                <c:forEach var="v" items="${vacunasAll}">
                                    <option value="${v.idVacuna}">${v.nombre}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="nl_lote" class="form-label">Código de lote</label>
                            <input id="nl_lote" name="loteCodigo" required maxlength="60" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="nl_ff" class="form-label">Fecha de fabricación</label>
                            <input id="nl_ff" type="date" name="fechaFabricacion" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="nl_fv" class="form-label">Fecha de vencimiento</label>
                            <input id="nl_fv" type="date" name="fechaVencimiento" class="form-control">
                        </div>
                        <div class="mb-0">
                            <label for="nl_prov" class="form-label">Proveedor</label>
                            <input id="nl_prov" name="proveedor" maxlength="120" class="form-control">
                        </div>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-save2 me-1"></i> Guardar
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- Modal: Editar lote -->
            <dialog id="modalEditarLote" aria-labelledby="el_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="editLote">
                    <input type="hidden" name="id" id="el_id">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0" id="el_title"><i class="bi bi-pencil-square me-2"></i>Editar lote</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <div class="mb-3">
                            <label for="el_vacuna" class="form-label">Vacuna</label>
                            <select id="el_vacuna" name="idVacuna" required class="form-select">
                                <c:forEach var="v" items="${vacunasAll}">
                                    <option value="${v.idVacuna}">${v.nombre}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="el_lote" class="form-label">Código de lote</label>
                            <input id="el_lote" name="loteCodigo" required maxlength="60" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="el_ff" class="form-label">Fecha de fabricación</label>
                            <input id="el_ff" type="date" name="fechaFabricacion" class="form-control">
                        </div>
                        <div class="mb-3">
                            <label for="el_fv" class="form-label">Fecha de vencimiento</label>
                            <input id="el_fv" type="date" name="fechaVencimiento" class="form-control">
                        </div>
                        <div class="mb-0">
                            <label for="el_prov" class="form-label">Proveedor</label>
                            <input id="el_prov" name="proveedor" maxlength="120" class="form-control">
                        </div>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i> Guardar cambios
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- Modal: Eliminar lote -->
            <dialog id="modalEliminarLote" aria-labelledby="dell_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="deleteLote">
                    <input type="hidden" name="id" id="dell_id">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0 text-danger" id="dell_title"><i class="bi bi-trash3-fill me-2"></i>Eliminar lote</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <p class="mb-0">¿Seguro que deseas eliminar este lote?</p>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-trash3 me-1"></i> Eliminar
                        </button>
                    </footer>
                </form>
            </dialog>

            <!-- ================================= -->
            <!-- Sección: Stock por centro         -->
            <!-- ================================= -->
            <section aria-label="Stock por centro" class="mb-4 mt-5">
                <div class="d-flex align-items-center justify-content-between mb-2">
                    <h2 class="h4 m-0 d-flex align-items-center gap-2">
                        <i class="bi bi-graph-up-arrow text-primary"></i>
                        <span>Stock por centro</span>
                    </h2>
                    <button type="button" data-modal-open="#modalActualizarStock" class="btn btn-warning">
                        <i class="bi bi-arrow-repeat me-1"></i> Actualizar stock
                    </button>
                </div>

                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-body">
                        <form class="row g-3 align-items-end" method="get" action="<%=ctx%>/Pages/vacunas.jsp">
                            <input type="hidden" name="action" value="listStock">
                            <div class="col-12 col-md-4">
                                <label for="fs_centro" class="form-label">Centro</label>
                                <select id="fs_centro" name="idCentro" class="form-select">
                                    <option value="">Todos</option>
                                    <c:forEach var="c" items="${centros}">
                                        <option value="${c.idCentro}" ${param.idCentro==c.idCentro?'selected':''}>${c.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <label for="fs_vacuna" class="form-label">Vacuna</label>
                                <select id="fs_vacuna" name="idVacuna" class="form-select">
                                    <option value="">Todas</option>
                                    <c:forEach var="v" items="${vacunasAll}">
                                        <option value="${v.idVacuna}" ${param.idVacuna==v.idVacuna?'selected':''}>${v.nombre}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-12 col-md-4">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-funnel me-1"></i> Filtrar
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
                                        <th>Centro</th>
                                        <th>Vacuna</th>
                                        <th>Lote</th>
                                        <th>Disponible</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="s" items="${stock}">
                                        <tr data-centro="${s.centro}" data-loteid="${s.idLote}">
                                            <td>${s.centro}</td>
                                            <td>${s.vacuna}</td>
                                            <td>${s.loteCodigo}</td>
                                            <td>
                                                <span class="badge text-bg-${s.cantidadDisponible > 0 ? 'success' : 'secondary'}">
                                                    ${s.cantidadDisponible}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty stock}">
                                        <tr><td colspan="4" class="text-center text-muted py-4">Sin resultados.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Modal: Actualizar stock -->
            <dialog id="modalActualizarStock" aria-labelledby="as_title" aria-modal="true" class="rounded-3">
                <form method="post" action="<%=ctx%>/Pages/vacunas.jsp" class="p-0">
                    <input type="hidden" name="action" value="updateStock">
                    <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                        <h3 class="h5 m-0" id="as_title"><i class="bi bi-arrow-repeat me-2"></i>Actualizar stock</h3>
                        <button type="button" data-modal-close class="btn btn-light btn-sm">
                            <i class="bi bi-x-lg"></i><span class="visually-hidden">Cerrar</span>
                        </button>
                    </header>
                    <div class="px-3 py-3">
                        <div class="mb-3">
                            <label for="as_centro" class="form-label">Centro</label>
                            <select id="as_centro" name="idCentro" required class="form-select">
                                <c:forEach var="c" items="${centros}">
                                    <option value="${c.idCentro}">${c.nombre}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="as_lote" class="form-label">Lote</label>
                            <select id="as_lote" name="idLote" required class="form-select">
                                <c:forEach var="l" items="${lotesAll}">
                                    <option value="${l.idLote}">${l.vacuna} - ${l.loteCodigo}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="mb-0">
                            <label for="as_cant" class="form-label">Cantidad</label>
                            <input id="as_cant" name="cantidad" type="number" min="0" step="1" required class="form-control">
                        </div>
                    </div>
                    <footer class="d-flex justify-content-end gap-4 px-3 pb-3 border-top">
                        <button type="button" data-modal-close class="btn btn-outline-secondary">
                            <i class="bi bi-x-circle me-1"></i> Cancelar
                        </button>
                        <button type="submit" class="btn btn-warning">
                            <i class="bi bi-save2 me-1"></i> Actualizar
                        </button>
                    </footer>
                </form>
            </dialog>

        </main>

        <!-- Bootstrap Bundle -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" defer></script>

        <!-- Página JS -->
        <script>
            (function () {
                'use strict';

                // Helpers para <dialog>
                function openDialog(dlg) {
                    dlg.showModal ? dlg.showModal() : dlg.setAttribute('open', '');
                }
                function closeDialog(dlg) {
                    dlg.close ? dlg.close() : dlg.removeAttribute('open');
                }

                // Abrir/llenar modales (delegación)
                document.addEventListener('click', function (ev) {
                    const btn = ev.target.closest('[data-modal-open]');
                    if (!btn)
                        return;

                    const sel = btn.getAttribute('data-modal-open');
                    const fill = btn.getAttribute('data-fill');
                    const dlg = document.querySelector(sel);
                    if (!dlg)
                        return;

                    const tr = btn.closest('tr');

                    // Vacunas
                    if (sel === '#modalEditarVacuna' && fill === 'editarVacuna' && tr) {
                        document.getElementById('ev_id').value = tr.getAttribute('data-id') || '';
                        document.getElementById('ev_nombre').value = tr.getAttribute('data-nombre') || '';
                        document.getElementById('ev_fabricante').value = tr.getAttribute('data-fabricante') || '';
                        document.getElementById('ev_desc').value = tr.getAttribute('data-descripcion') || '';
                        document.getElementById('ev_dosis').value = ''; // Cargar si backend lo expone
                    }
                    if (sel === '#modalEliminarVacuna' && fill === 'eliminarVacuna' && tr) {
                        document.getElementById('delv_id').value = tr.getAttribute('data-id') || '';
                    }

                    // Lotes
                    if (sel === '#modalEditarLote' && fill === 'editarLote' && tr) {
                        document.getElementById('el_id').value = tr.getAttribute('data-id') || '';
                        document.getElementById('el_vacuna').value = tr.getAttribute('data-vacunaid') || '';
                        document.getElementById('el_lote').value = tr.getAttribute('data-lote') || '';
                        document.getElementById('el_ff').value = (tr.getAttribute('data-ff') || '').split('T')[0] || '';
                        document.getElementById('el_fv').value = (tr.getAttribute('data-fv') || '').split('T')[0] || '';
                        document.getElementById('el_prov').value = tr.getAttribute('data-prov') || '';
                    }
                    if (sel === '#modalEliminarLote' && fill === 'eliminarLote' && tr) {
                        document.getElementById('dell_id').value = tr.getAttribute('data-id') || '';
                    }

                    openDialog(dlg);
                });

                // Cerrar modales (delegación)
                document.addEventListener('click', function (ev) {
                    const btn = ev.target.closest('[data-modal-close]');
                    if (!btn)
                        return;
                    const dlg = btn.closest('dialog');
                    if (dlg)
                        closeDialog(dlg);
                });

            })();
        </script>

    </body>
</html>

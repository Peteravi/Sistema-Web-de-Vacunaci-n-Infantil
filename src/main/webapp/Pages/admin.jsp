<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8" />
        <title>VacuKids · Menú principal</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <!-- Bootstrap 5 + Bootstrap Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/admin.css">    
        <!-- Favicon e hoja de estilos del proyecto -->
        <link rel="icon" href="<%=ctx%>/img/logo.png">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/admin.css">

        <!-- Preferencia de color (opcional: modo oscuro del SO) -->
        <meta name="color-scheme" content="light dark">
    </head>
    <body>

        <header class="py-3 border-bottom">
            <nav aria-label="Menú principal" class="container">
                <ul class="row g-3 list-unstyled mb-0">
                    <li class="col-12 col-sm-6 col-lg-3">
                        <button type="button" data-modal-open="#modalUsuarios" title="Gestión de usuarios"
                                class="w-100 btn btn-light border d-grid gap-2 py-4 shadow-sm dashboard-card">
                            <i class="bi bi-people-fill fs-1" aria-hidden="true"></i>
                            <span class="fw-semibold">Gestión de usuarios</span>
                        </button>
                    </li>

                    <li class="col-12 col-sm-6 col-lg-3">
                        <a href="<%=ctx%>/Pages/citas.jsp" class="w-100 btn btn-light border d-grid gap-2 py-4 shadow-sm text-decoration-none dashboard-card">
                            <i class="bi bi-calendar2-check fs-1" aria-hidden="true"></i>
                            <span class="fw-semibold">Citas</span>
                        </a>
                    </li>

                    <li class="col-12 col-sm-6 col-lg-3">
                        <a href="<%=ctx%>/Pages/vacunas.jsp" class="w-100 btn btn-light border d-grid gap-2 py-4 shadow-sm text-decoration-none dashboard-card">
                            <i class="bi bi-capsule-pill fs-1" aria-hidden="true"></i>
                            <span class="fw-semibold">Vacunas</span>
                        </a>
                    </li>

                    <li class="col-12 col-sm-6 col-lg-3">
                        <a href="<%=ctx%>/Pages/reportes.jsp" class="w-100 btn btn-light border d-grid gap-2 py-4 shadow-sm text-decoration-none dashboard-card">
                            <i class="bi bi-justify fs-1" aria-hidden="true"></i>
                            <span class="fw-semibold">Reportes</span>
                        </a>
                    </li>

                    <li class="col-12 col-sm-6 col-lg-3">
                        <a href="<%=ctx%>/Pages/esquemas_view.jsp" class="w-100 btn btn-light border d-grid gap-2 py-4 shadow-sm text-decoration-none dashboard-card">
                            <i class="bi bi-diagram-3 fs-1" aria-hidden="true"></i>
                            <span class="fw-semibold">Esquemas de Vacunación</span>
                        </a>
                    </li>
                </ul>

                <div class="nav-right mt-3 d-flex justify-content-end">
                    <form id="logoutForm" method="post" action="<%=ctx%>/Pages/login.jsp" class="m-0">
                        <button type="submit" class="btn btn-outline-secondary btn-logout">
                            <i class="bi bi-box-arrow-right me-2" aria-hidden="true"></i><span>Cerrar sesión</span>
                        </button>
                    </form>
                </div>
            </nav>
        </header>

        <!-- ============================= -->
        <!-- MODAL: GESTIÓN DE USUARIOS   -->
        <!-- ============================= -->
        <dialog id="modalUsuarios" aria-labelledby="usuarios-title" aria-modal="true">
            <article class="p-0">
                <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                    <h2 class="h4 m-0" id="usuarios-title">
                        <i class="bi bi-people-gear me-2" aria-hidden="true"></i>Gestión de usuarios
                    </h2>
                    <button type="button" title="Cerrar" data-modal-close class="btn btn-light btn-sm">
                        <i class="bi bi-x-lg" aria-hidden="true"></i><span class="visually-hidden">Cerrar</span>
                    </button>
                </header>

                <!-- Notificaciones opcionales (JSP) -->
                <div class="px-3 pt-3">
                    <c:if test="${param.ok == '1'}">
                        <div role="status" class="alert alert-success d-flex align-items-center gap-2 py-2">
                            <i class="bi bi-check-circle-fill" aria-hidden="true"></i> Operación realizada correctamente.
                        </div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div role="alert" class="alert alert-danger d-flex align-items-center gap-2 py-2">
                            <i class="bi bi-exclamation-triangle-fill" aria-hidden="true"></i> Error: ${error}
                        </div>
                    </c:if>
                    <c:if test="${not empty param.msg}">
                        <div role="status" class="alert alert-info d-flex align-items-center gap-2 py-2">
                            <i class="bi bi-info-circle-fill" aria-hidden="true"></i> ${param.msg}
                        </div>
                    </c:if>
                </div>

                <section aria-label="acciones" class="acciones px-3">
                    <button type="button" data-modal-open="#modalNuevo" class="btn btn-primary">
                        <i class="bi bi-person-plus-fill me-2" aria-hidden="true"></i> Nuevo usuario
                    </button>
                </section>

                <section aria-label="listado" class="px-3 pb-3">
                    <div class="table-responsive mt-3">
                        <table class="table table-hover table-striped align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col">ID</th>
                                    <th scope="col">Usuario (email)</th>
                                    <th scope="col">Rol</th>
                                    <th scope="col">Actor</th>
                                    <th scope="col">Estado</th>
                                    <th scope="col">Flags</th>
                                    <th scope="col">Creado</th>
                                    <th scope="col">Acciones</th>
                                </tr>
                            </thead>
                            <tbody id="tbodyUsuarios">
                                <!-- Fallback render desde servidor -->
                                <c:forEach var="u" items="${data}">
                                    <tr
                                        data-id="${u.idUsuario}"
                                        data-usuario="${u.usuario}"
                                        data-idrol="${u.idRol}"
                                        data-actor="${u.actor}"
                                        data-activo="${u.activo}"
                                        data-must="${u.mustChangePassword}">
                                        <td>${u.idUsuario}</td>
                                        <td>${u.usuario}</td>
                                        <td>${u.rol}</td>
                                        <td>${u.actor}</td>
                                        <td>
                                            <span class="badge text-bg-${u.activo ? 'success' : 'secondary'}">
                                                <i class="bi ${u.activo ? 'bi-check-circle' : 'bi-pause-circle'} me-1" aria-hidden="true"></i>
                                                ${u.activo ? "Activo" : "Inactivo"}
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${u.mustChangePassword}">
                                                <span class="badge text-bg-warning">
                                                    <i class="bi bi-key me-1" aria-hidden="true"></i>Debe cambiar clave
                                                </span>
                                            </c:if>
                                        </td>
                                        <td>${u.creadoEn}</td>
                                        <td class="actions">
                                            <button type="button" data-modal-open="#modalEditar" class="btn btn-sm btn-outline-primary">
                                                <i class="bi bi-pencil-square me-1" aria-hidden="true"></i> Editar
                                            </button>
                                            <c:if test="${u.activo}">
                                                <button type="button" data-modal-open="#modalConfirmar" data-fill="desactivar" class="btn btn-sm btn-outline-warning">
                                                    <i class="bi bi-slash-circle me-1" aria-hidden="true"></i> Desactivar
                                                </button>
                                            </c:if>
                                            <c:if test="${!u.activo}">
                                                <button type="button" data-modal-open="#modalConfirmar" data-fill="activar" class="btn btn-sm btn-outline-success">
                                                    <i class="bi bi-check2-circle me-1" aria-hidden="true"></i> Activar
                                                </button>
                                            </c:if>
                                            <button type="button" data-modal-open="#modalEliminar" data-fill="eliminar" class="btn btn-sm btn-outline-danger">
                                                <i class="bi bi-trash3 me-1" aria-hidden="true"></i> Eliminar
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty data}">
                                    <tr><td colspan="8" class="text-center text-muted">Sin resultados.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </section>
            </article>
        </dialog>

        <!-- ===================== -->
        <!-- MODAL: NUEVO USUARIO -->
        <!-- ===================== -->
        <dialog id="modalNuevo" aria-labelledby="nuevo-title" aria-modal="true">
            <form id="formNuevo" method="post" action="<%=ctx%>/admin/usuarios" class="p-0" novalidate>
                <input type="hidden" name="action" value="create">
                <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                    <h3 class="h5 m-0" id="nuevo-title"><i class="bi bi-person-plus-fill me-2" aria-hidden="true"></i>Nuevo usuario</h3>
                    <button type="button" data-modal-close class="btn btn-light btn-sm" title="Cerrar">
                        <i class="bi bi-x-lg" aria-hidden="true"></i><span class="visually-hidden">Cerrar</span>
                    </button>
                </header>

                <div class="px-3 py-3">
                    <div class="mb-3">
                        <label for="tipoUsuario" class="form-label"><i class="bi bi-person-vcard me-2" aria-hidden="true"></i>Tipo de cuenta</label>
                        <select id="tipoUsuario" name="tipo" required class="form-select">
                            <option value="tutor">Tutor / Representante</option>
                            <option value="personal">Personal (Médico/Enfermero/Recepción)</option>
                        </select>
                    </div>

                    <!-- Datos TUTOR -->
                    <fieldset data-section="tutor" class="border rounded p-3">
                        <legend class="float-none w-auto px-2 small text-muted"><i class="bi bi-person me-1" aria-hidden="true"></i>Datos del Tutor</legend>

                        <label for="t_cedula" class="form-label mt-2">Cédula</label>
                        <input id="t_cedula" name="cedula" maxlength="10" pattern="\d{10}" inputmode="numeric" class="form-control">

                        <label for="t_nombres" class="form-label mt-2">Nombres</label>
                        <input id="t_nombres" name="nombres" class="form-control">

                        <label for="t_apellidos" class="form-label mt-2">Apellidos</label>
                        <input id="t_apellidos" name="apellidos" class="form-control">

                        <label for="t_correo" class="form-label mt-2">Correo</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-envelope-at" aria-hidden="true"></i></span>
                            <input id="t_correo" name="correo" type="email" class="form-control" autocomplete="email">
                        </div>

                        <label for="t_direccion" class="form-label mt-2">Dirección</label>
                        <input id="t_direccion" name="direccion" class="form-control" autocomplete="address-line1">

                        <label for="t_telefono" class="form-label mt-2">Teléfono</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-telephone" aria-hidden="true"></i></span>
                            <input id="t_telefono" name="telefono" class="form-control" autocomplete="tel">
                        </div>

                        <p class="text-muted small mt-2">
                            <i class="bi bi-info-circle me-1" aria-hidden="true"></i>
                            La clave inicial será la cédula. Se forzará cambio de clave al primer inicio.
                        </p>
                    </fieldset>

                    <!-- Datos PERSONAL -->
                    <fieldset data-section="personal" hidden class="border rounded p-3 mt-3">
                        <legend class="float-none w-auto px-2 small text-muted"><i class="bi bi-person-gear me-1" aria-hidden="true"></i>Datos del Personal</legend>

                        <label for="p_cedula" class="form-label mt-2">Cédula</label>
                        <input id="p_cedula" name="cedula" maxlength="10" pattern="\d{10}" inputmode="numeric" class="form-control">

                        <label for="p_nombres" class="form-label mt-2">Nombres</label>
                        <input id="p_nombres" name="nombres" class="form-control">

                        <label for="p_apellidos" class="form-label mt-2">Apellidos</label>
                        <input id="p_apellidos" name="apellidos" class="form-control">

                        <label for="p_correo" class="form-label mt-2">Correo</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-envelope-at" aria-hidden="true"></i></span>
                            <input id="p_correo" name="correo" type="email" class="form-control" autocomplete="email">
                        </div>

                        <label for="p_telefono" class="form-label mt-2">Teléfono</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-telephone" aria-hidden="true"></i></span>
                            <input id="p_telefono" name="telefono" class="form-control" autocomplete="tel">
                        </div>

                        <label for="p_cargo" class="form-label mt-2">Cargo</label>
                        <div class="input-group">
                            <span class="input-group-text"><i class="bi bi-briefcase" aria-hidden="true"></i></span>
                            <input id="p_cargo" name="cargo" placeholder="Médico/Enfermero/Recepción" class="form-control">
                        </div>
                    </fieldset>
                </div>

                <footer class="d-flex justify-content-end gap-2 px-3 pb-3">
                    <button type="button" data-modal-close class="btn btn-outline-secondary">
                        <i class="bi bi-x-circle me-1" aria-hidden="true"></i> Cancelar
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-floppy2-fill me-1" aria-hidden="true"></i> Crear usuario
                    </button>
                </footer>
            </form>
        </dialog>

        <!-- ===================== -->
        <!-- MODAL: EDITAR USUARIO -->
        <!-- ===================== -->
        <dialog id="modalEditar" aria-labelledby="editar-title" aria-modal="true">
            <form id="formEditar" method="post" action="<%=ctx%>/admin/usuarios" class="p-0" novalidate>
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="id" id="ed_id">

                <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                    <h3 class="h5 m-0" id="editar-title"><i class="bi bi-pencil-square me-2" aria-hidden="true"></i>Editar usuario</h3>
                    <button type="button" data-modal-close class="btn btn-light btn-sm" title="Cerrar">
                        <i class="bi bi-x-lg" aria-hidden="true"></i><span class="visually-hidden">Cerrar</span>
                    </button>
                </header>

                <div class="px-3 py-3">
                    <label for="ed_usuario" class="form-label">Usuario (email)</label>
                    <div class="input-group mb-2">
                        <span class="input-group-text"><i class="bi bi-envelope-at" aria-hidden="true"></i></span>
                        <input id="ed_usuario" name="usuario" type="email" required class="form-control" autocomplete="email">
                    </div>

                    <label for="ed_idRol" class="form-label">Rol</label>
                    <div class="input-group mb-2">
                        <span class="input-group-text"><i class="bi bi-person-badge" aria-hidden="true"></i></span>
                        <select id="ed_idRol" name="idRol" required class="form-select">
                            <option value="1">Administrador</option>
                            <option value="2">Médico</option>
                            <option value="3">Enfermero</option>
                            <option value="4">Recepción</option>
                            <option value="5">Representante</option>
                        </select>
                    </div>

                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="activo" id="ed_activo">
                        <label class="form-check-label" for="ed_activo"><i class="bi bi-toggle-on me-1" aria-hidden="true"></i>Activo</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="mustChange" id="ed_must">
                        <label class="form-check-label" for="ed_must"><i class="bi bi-key me-1" aria-hidden="true"></i>Debe cambiar contraseña</label>
                    </div>
                </div>

                <footer class="d-flex justify-content-end gap-2 px-3 pb-3">
                    <button type="button" data-modal-close class="btn btn-outline-secondary">
                        <i class="bi bi-x-circle me-1" aria-hidden="true"></i> Cancelar
                    </button>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-floppy2-fill me-1" aria-hidden="true"></i> Guardar cambios
                    </button>
                </footer>
            </form>
        </dialog>

        <!-- ================= -->
        <!-- MODAL: ELIMINAR   -->
        <!-- ================= -->
        <dialog id="modalEliminar" aria-labelledby="eliminar-title" aria-modal="true">
            <form id="formEliminar" method="post" action="<%=ctx%>/admin/usuarios" class="p-0">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" id="del_id">

                <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                    <h3 class="h5 m-0 text-danger" id="eliminar-title"><i class="bi bi-trash3-fill me-2" aria-hidden="true"></i>Eliminar usuario</h3>
                    <button type="button" data-modal-close class="btn btn-light btn-sm" title="Cerrar">
                        <i class="bi bi-x-lg" aria-hidden="true"></i><span class="visually-hidden">Cerrar</span>
                    </button>
                </header>

                <div class="px-3 py-3">
                    <p class="mb-0"><strong class="text-danger"><i class="bi bi-exclamation-triangle-fill me-1" aria-hidden="true"></i>Advertencia:</strong>
                        en sistemas de salud se recomienda <em>desactivar</em> en lugar de eliminar.</p>
                </div>

                <footer class="d-flex justify-content-end gap-2 px-3 pb-3">
                    <button type="button" data-modal-close class="btn btn-outline-secondary">
                        <i class="bi bi-x-circle me-1" aria-hidden="true"></i> Cancelar
                    </button>
                    <button type="submit" class="btn btn-danger">
                        <i class="bi bi-trash3 me-1" aria-hidden="true"></i> Eliminar
                    </button>
                </footer>
            </form>
        </dialog>

        <!-- ================= -->
        <!-- MODAL: CONFIRMAR  -->
        <!-- ================= -->
        <dialog id="modalConfirmar" aria-labelledby="confirmar-title" aria-modal="true">
            <form class="p-0">
                <input type="hidden" id="cf_id" name="id">
                <input type="hidden" id="cf_action" name="action">

                <header class="d-flex align-items-center justify-content-between px-3 pt-3 pb-2 border-bottom">
                    <h3 class="h5 m-0" id="confirmar-title"><i class="bi bi-question-circle-fill me-2" aria-hidden="true"></i>Confirmar</h3>
                    <button type="button" data-modal-close class="btn btn-light btn-sm" title="Cerrar">
                        <i class="bi bi-x-lg" aria-hidden="true"></i><span class="visually-hidden">Cerrar</span>
                    </button>
                </header>

                <div class="px-3 py-3">
                    <p id="cf_texto" class="mb-0">¿Estás seguro?</p>
                </div>

                <footer class="d-flex justify-content-end gap-2 px-3 pb-3">
                    <button type="button" data-modal-close class="btn btn-outline-secondary">
                        <i class="bi bi-x-circle me-1" aria-hidden="true"></i> Cancelar
                    </button>
                    <button id="cf_submit" type="submit" class="btn btn-primary">
                        <i class="bi bi-check2-circle me-1" aria-hidden="true"></i> Sí, confirmar
                    </button>
                </footer>
            </form>
        </dialog>

        <!-- Bootstrap JS (para offcanvas, etc.) -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" defer></script>

        <!-- App JS -->
        <script>
            (function () {
                'use strict';
                const CTX = '<%= request.getContextPath()%>';

                // ====== Soporte básico <dialog> en navegadores sin método showModal() ======
                function openDialog(dlg) {
                    dlg.showModal ? dlg.showModal() : dlg.setAttribute('open', '');
                }
                function closeDialog(dlg) {
                    dlg.close ? dlg.close() : dlg.removeAttribute('open');
                }

                // ====== Delegación global: abrir/cerrar modales ======
                document.addEventListener('click', function (ev) {
                    const btnOpen = ev.target.closest('[data-modal-open]');
                    if (btnOpen) {
                        const sel = btnOpen.getAttribute('data-modal-open');
                        const dlg = document.querySelector(sel);
                        if (!dlg)
                            return;

                        // Si el botón está dentro de una fila con data-*
                        const tr = btnOpen.closest('tr');
                        const fill = btnOpen.getAttribute('data-fill');

                        if (dlg.id === 'modalEditar' && tr) {
                            // Rellenar modal de edición
                            var id = tr.getAttribute('data-id');
                            var usuario = tr.getAttribute('data-usuario');
                            var idRol = tr.getAttribute('data-idrol');
                            var activo = tr.getAttribute('data-activo') === 'true';
                            var must = tr.getAttribute('data-must') === 'true';
                            document.getElementById('ed_id').value = id || '';
                            document.getElementById('ed_usuario').value = usuario || '';
                            document.getElementById('ed_idRol').value = idRol || '5';
                            document.getElementById('ed_activo').checked = !!activo;
                            document.getElementById('ed_must').checked = !!must;
                        }

                        if (dlg.id === 'modalEliminar' && tr) {
                            document.getElementById('del_id').value = tr.getAttribute('data-id') || '';
                        }

                        if (dlg.id === 'modalConfirmar') {
                            var texto = '¿Estás seguro?';
                            if (fill === 'activar')
                                texto = '¿Activar este usuario?';
                            if (fill === 'desactivar')
                                texto = '¿Desactivar este usuario?';
                            if (fill === 'eliminar')
                                texto = '¿Eliminar este usuario?';
                            document.getElementById('cf_texto').textContent = texto;
                            document.getElementById('cf_action').value = fill || '';
                            if (tr)
                                document.getElementById('cf_id').value = tr.getAttribute('data-id') || '';
                        }

                        openDialog(dlg);
                    }

                    const btnClose = ev.target.closest('[data-modal-close]');
                    if (btnClose) {
                        const dlg = btnClose.closest('dialog');
                        if (dlg)
                            closeDialog(dlg);
                    }
                });

                // ====== Alternar secciones tutor/personal en "Nuevo usuario" ======
                var selTipo = document.getElementById('tipoUsuario');
                function toggleSecciones() {
                    var v = (selTipo ? selTipo.value : 'tutor');
                    document.querySelectorAll('fieldset[data-section]').forEach(function (fs) {
                        fs.hidden = (fs.getAttribute('data-section') !== v);
                    });
                }
                if (selTipo) {
                    selTipo.addEventListener('change', toggleSecciones);
                    toggleSecciones();
                }

                // Abrir automáticamente el modal de gestión si llega ?open=usuarios
                if (new URLSearchParams(location.search).get('open') === 'usuarios') {
                    var dlg = document.getElementById('modalUsuarios');
                    if (dlg)
                        openDialog(dlg);
                }

                // ====== API ======
                const API = {
                    async getUsuarios(params) {
                        const qs = new URLSearchParams(params || {}).toString();
                        const r = await fetch(CTX + '/api/admin/usuarios?' + qs, {credentials: 'include'});
                        if (!r.ok)
                            throw new Error('GET ' + r.status);
                        return r.json();
                    },
                    async post(action, body) {
                        const r = await fetch(CTX + '/api/admin/usuarios?action=' + encodeURIComponent(action), {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json'},
                            credentials: 'include',
                            body: JSON.stringify(body || {})
                        });
                        // Muchos servlets devuelven JSON con ok/msg; caemos en JSON igual si status 4xx con cuerpo JSON
                        let data;
                        try {
                            data = await r.json();
                        } catch (e) {
                            data = {ok: false, msg: 'Respuesta no JSON'};
                        }
                        if (!r.ok || data.ok === false) {
                            const msg = (data && (data.error || data.msg)) || ('Error HTTP ' + r.status);
                            throw new Error(msg);
                        }
                        return data;
                    },
                    async getCatalogos() {
                        const r = await fetch(CTX + '/api/admin/catalogos', {credentials: 'include'});
                        if (!r.ok)
                            throw new Error('GET ' + r.status);
                        return r.json();
                    }
                };

                // ====== Render ======
                function renderUsuarios(rows) {
                    const tb = document.getElementById('tbodyUsuarios');
                    if (!tb)
                        return;
                    tb.innerHTML = '';

                    if (!Array.isArray(rows) || rows.length === 0) {
                        var trEmpty = document.createElement('tr');
                        var tdEmpty = document.createElement('td');
                        tdEmpty.colSpan = 8;
                        tdEmpty.textContent = 'Sin resultados.';
                        trEmpty.appendChild(tdEmpty);
                        tb.appendChild(trEmpty);
                        return;
                    }

                    rows.forEach(function (u) {
                        const tr = document.createElement('tr');

                        // Normalizar campos
                        const id = (u.id_usuario != null ? u.id_usuario : u.idUsuario);
                        const usuario = (u.usuario != null ? u.usuario : '');
                        const rol = (u.rol != null ? u.rol : '');
                        const activo = (u.activo === undefined || u.activo === null) ? true : !!u.activo;
                        const mustChange = (u.must_change_password != null ? u.must_change_password : u.mustChangePassword);
                        const creado = (u.creado_en != null ? u.creado_en : u.creadoEn);
                        const actor = (u.personal ? 'Personal' : (u.tutor ? 'Tutor' : (u.actor != null ? u.actor : '')));
                        const idRol = (u.id_rol != null ? u.id_rol : u.idRol);

                        tr.setAttribute('data-id', id != null ? String(id) : '');
                        tr.setAttribute('data-usuario', usuario);
                        tr.setAttribute('data-idrol', idRol != null ? String(idRol) : '');
                        tr.setAttribute('data-actor', actor);
                        tr.setAttribute('data-activo', String(!!activo));
                        tr.setAttribute('data-must', String(!!mustChange));

                        function td(val) {
                            const c = document.createElement('td');
                            c.textContent = (val == null ? '' : String(val));
                            return c;
                        }

                        tr.appendChild(td(id));
                        tr.appendChild(td(usuario));
                        tr.appendChild(td(rol));
                        tr.appendChild(td(actor));
                        // Estado visual comprimido + texto accesible
                        const tdEstado = document.createElement('td');
                        tdEstado.innerHTML = activo
                                ? '<span class="badge text-bg-success"><i class="bi bi-check-circle me-1" aria-hidden="true"></i>Activo</span>'
                                : '<span class="badge text-bg-secondary"><i class="bi bi-pause-circle me-1" aria-hidden="true"></i>Inactivo</span>';
                        tr.appendChild(tdEstado);

                        const tdFlags = document.createElement('td');
                        tdFlags.innerHTML = mustChange
                                ? '<span class="badge text-bg-warning"><i class="bi bi-key me-1" aria-hidden="true"></i>Debe cambiar clave</span>'
                                : '';
                        tr.appendChild(tdFlags);

                        tr.appendChild(td(creado));

                        // Acciones
                        const tdAcc = document.createElement('td');
                        tdAcc.className = 'd-flex flex-wrap gap-2';

                        const btnEdit = document.createElement('button');
                        btnEdit.type = 'button';
                        btnEdit.className = 'btn btn-sm btn-outline-primary';
                        btnEdit.setAttribute('data-modal-open', '#modalEditar');
                        btnEdit.innerHTML = '<i class="bi bi-pencil-square me-1" aria-hidden="true"></i>Editar';
                        tdAcc.appendChild(btnEdit);

                        const btnToggle = document.createElement('button');
                        btnToggle.type = 'button';
                        btnToggle.className = 'btn btn-sm ' + (activo ? 'btn-outline-warning' : 'btn-outline-success');
                        btnToggle.setAttribute('data-modal-open', '#modalConfirmar');
                        btnToggle.setAttribute('data-fill', activo ? 'desactivar' : 'activar');
                        btnToggle.innerHTML = activo
                                ? '<i class="bi bi-slash-circle me-1" aria-hidden="true"></i>Desactivar'
                                : '<i class="bi bi-check2-circle me-1" aria-hidden="true"></i>Activar';
                        tdAcc.appendChild(btnToggle);

                        const btnDel = document.createElement('button');
                        btnDel.type = 'button';
                        btnDel.className = 'btn btn-sm btn-outline-danger';
                        btnDel.setAttribute('data-modal-open', '#modalEliminar');
                        btnDel.setAttribute('data-fill', 'eliminar');
                        btnDel.innerHTML = '<i class="bi bi-trash3 me-1" aria-hidden="true"></i>Eliminar';
                        tdAcc.appendChild(btnDel);

                        tr.appendChild(tdAcc);
                        tb.appendChild(tr);
                    });
                }

                async function cargarUsuarios(page, q) {
                    try {
                        const res = await API.getUsuarios({action: 'listarPaged', page: page || 1, q: q || ''});
                        const data = Array.isArray(res) ? res : (res.data || res.rows || []);
                        renderUsuarios(data);
                    } catch (e) {
                        console.error(e);
                        const tb = document.getElementById('tbodyUsuarios');
                        if (tb)
                            tb.innerHTML = '<tr><td colspan="8" class="text-danger text-center">No se pudieron cargar los usuarios.</td></tr>';
                    }
                }

                // ====== Formularios ======
                const fNuevo = document.getElementById('formNuevo');
                if (fNuevo) {
                    fNuevo.addEventListener('submit', async function (ev) {
                        ev.preventDefault();
                        const fd = new FormData(fNuevo);
                        const tipo = (fd.get('tipo') || '').toLowerCase();

                        const base = {
                            usuario: (fd.get('correo') || fd.get('usuario') || '').toString().trim(),
                            id_rol: parseInt(fd.get('idRol') || (tipo === 'tutor' ? 5 : 2), 10)
                        };
                        let payload = Object.assign({}, base);

                        if (tipo === 'tutor') {
                            payload.id_tutor = null;
                            payload.tutor = {
                                cedula: fd.get('cedula'),
                                nombres: fd.get('nombres'),
                                apellidos: fd.get('apellidos'),
                                correo: fd.get('correo'),
                                direccion: fd.get('direccion'),
                                telefono: fd.get('telefono')
                            };
                        } else {
                            payload.id_personal = null;
                            payload.personal = {
                                cedula: fd.get('cedula'),
                                nombres: fd.get('nombres'),
                                apellidos: fd.get('apellidos'),
                                correo: fd.get('correo'),
                                telefono: fd.get('telefono'),
                                cargo: fd.get('cargo') || ''
                            };
                        }

                        try {
                            const r = await API.post('crear', payload);
                            // si llega ok:true → recargar
                            await cargarUsuarios(1, '');
                            closeDialog(document.getElementById('modalNuevo'));
                            fNuevo.reset();
                            toggleSecciones();
                        } catch (err) {
                            alert(err.message || 'Error al crear usuario');
                        }
                    });
                }

                const fEditar = document.getElementById('formEditar');
                if (fEditar) {
                    fEditar.addEventListener('submit', async function (ev) {
                        ev.preventDefault();
                        const fd = new FormData(fEditar);
                        const payload = {
                            id_usuario: parseInt(fd.get('id') || document.getElementById('ed_id').value, 10),
                            usuario: (fd.get('usuario') || document.getElementById('ed_usuario').value),
                            id_rol: parseInt(fd.get('idRol') || document.getElementById('ed_idRol').value, 10),
                            activo: !!(fd.get('activo') || document.getElementById('ed_activo').checked),
                            must_change_password: !!(fd.get('mustChange') || document.getElementById('ed_must').checked)
                        };
                        try {
                            await API.post('actualizarBasico', payload);
                            await cargarUsuarios();
                            closeDialog(document.getElementById('modalEditar'));
                        } catch (err) {
                            alert(err.message || 'Error al actualizar');
                        }
                    });
                }

                const fEliminar = document.getElementById('formEliminar');
                if (fEliminar) {
                    fEliminar.addEventListener('submit', async function (ev) {
                        ev.preventDefault();
                        const id = parseInt(document.getElementById('del_id').value, 10);
                        try {
                            await API.post('eliminar', {id_usuario: id});
                            await cargarUsuarios();
                            closeDialog(document.getElementById('modalEliminar'));
                        } catch (err) {
                            alert(err.message || 'Error al eliminar');
                        }
                    });
                }

                // Confirmar activar/desactivar
                const modalConf = document.getElementById('modalConfirmar');
                if (modalConf) {
                    const cfForm = modalConf.querySelector('form');
                    if (cfForm) {
                        cfForm.addEventListener('submit', async function (ev) {
                            ev.preventDefault();
                            const id = parseInt(document.getElementById('cf_id').value, 10);
                            const action = document.getElementById('cf_action').value; // 'activar' | 'desactivar'
                            try {
                                await API.post('setActivo', {
                                    id_usuario: id,
                                    activo: action === 'activar'
                                });
                                await cargarUsuarios();
                                closeDialog(modalConf);
                            } catch (e) {
                                alert(e.message || 'Error al cambiar estado');
                            }
                        });
                    }
                }


                // Catálogos (roles, etc.)
                API.getCatalogos().then(function (cat) {
                    const selRol = document.getElementById('ed_idRol');
                    if (selRol && Array.isArray(cat.roles)) {
                        selRol.innerHTML = '';
                        cat.roles.forEach(function (r) {
                            const opt = document.createElement('option');
                            opt.value = (r.idRol != null ? r.idRol : r.id_rol);
                            opt.textContent = r.nombre;
                            selRol.appendChild(opt);
                        });
                    }
                }).catch(function (e) {
                    console.warn('Catálogos no disponibles:', e);
                });

                // Carga inicial
                cargarUsuarios(1, '');
            })();
        </script>
    </body>
</html>

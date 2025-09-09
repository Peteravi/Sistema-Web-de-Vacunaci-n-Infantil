<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8" />
        <title>VacuKids · Panel del Representante</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <!-- Bootstrap 5 + Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet"/>
        <link rel="stylesheet" href="<%=ctx%>/assets/css/representante.css"> 
    </head>
    <body>
        <div class="container py-3">

            <!-- Encabezado -->
            <div class="page-header">
                <div>
                    <h1 class="h4 mb-1">Panel del Representante</h1>
                    <div class="muted">Registra a tu hijo/a y gestiona sus citas.</div>
                </div>
                <div class="d-flex gap-2">
                    <a class="btn btn-outline-secondary btn-sm" href="<%=ctx%>/logout">
                        <i class="bi bi-box-arrow-right"></i> Cerrar sesión
                    </a>
                </div>
            </div>

            <!-- Acciones principales -->
            <div class="mb-3 d-flex flex-wrap gap-2">
                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalNuevoHijo">
                    <i class="bi bi-person-plus"></i> Registrar hijo/a
                </button>
                <button class="btn btn-outline-secondary" id="btnRecargarTodo">
                    <i class="bi bi-arrow-clockwise"></i> Actualizar datos
                </button>
            </div>

            <!-- Card: Lista de hijos/as -->
            <div class="card-vk p-3 mb-3">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <h2 class="h6 m-0">Hijos/as del representante</h2>
                        <div class="muted">Se muestran los hijos/as asociados a tu cuenta.</div>
                    </div>
                    <div>
                        <button id="btnRecargarHijos" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-arrow-repeat"></i> Recargar
                        </button>
                    </div>
                </div>
                <div class="mt-3 table-responsive">
                    <table class="table table-sm align-middle">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Cédula</th>
                                <th>Nombres</th>
                                <th>Apellidos</th>
                                <th>Fecha nac.</th>
                                <th>Sexo</th>
                                <th>Teléfono</th>
                                <th>Dirección</th>
                            </tr>
                        </thead>
                        <tbody id="tbodyHijos"><!-- JS --></tbody>
                    </table>
                </div>
            </div>

            <!-- Card: Agendar nueva cita -->
            <div class="card-vk p-3 mb-3">
                <h2 class="h6 m-0 mb-3">Agendar nueva cita</h2>
                <form id="formCita" class="row g-3">
                    <div class="col-12 col-md-4">
                        <label class="form-label">Hijo/a</label>
                        <select id="pacienteSel" class="form-select">
                            <option value="">-- Selecciona --</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label">Centro de salud</label>
                        <select id="centroSel" class="form-select">
                            <option value="">-- Selecciona --</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label">Fecha y hora</label>
                        <input id="fechaHora" type="datetime-local" class="form-control" />
                    </div>
                    <div class="col-12">
                        <label class="form-label">Observaciones</label>
                        <input id="observ" type="text" class="form-control" placeholder="(opcional)"/>
                    </div>
                    <div class="col-12">
                        <button type="submit" class="btn btn.success btn-success">
                            <i class="bi bi-calendar-plus"></i> Crear cita
                        </button>
                    </div>
                </form>
                <div class="text-muted small mt-2">

                </div>
            </div> 

            <a class="btn btn-primary"
               href="https://www.google.com/maps/place/Cl%C3%ADnica+El+Bat%C3%A1n/@-0.1654382,-78.478215,17z/data=!3m1!4b1!4m6!3m5!1s0x91d59a9fe57a4d2b:0x80c3d692438c1d1a!8m2!3d-0.1654436!4d-78.4756401!16s%2Fg%2F11gmslcgry?entry=ttu&g_ep=EgoyMDI1MDkwMy4wIKXMDSoASAFQAw%3D%3D"
               target="_blank" rel="noopener">
                Ver ubicación del centro
            </a>

            <!-- Card: Historial de citas -->
            <div class="card-vk p-3 mb-5">
                <div class="d-flex align-items-center justify-content-between">
                    <div>
                        <h2 class="h6 m-0">Historial de citas</h2>
                        <div class="muted">Citas de todos tus hijos/as</div>
                    </div>
                    <div>
                        <button id="btnRecargarCitas" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-arrow-repeat"></i> Recargar
                        </button>
                    </div>
                </div>
                <div class="mt-3 table-responsive">
                    <table class="table table-sm align-middle">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Fecha/Hora</th>
                                <th>Paciente</th>
                                <th>Centro</th>
                                <th>Estado</th>
                                <th>Obs.</th>
                                <th class="text-end">Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="tbodyCitas"><!-- JS --></tbody>
                    </table>
                </div>
            </div>

        </div>

        <!-- Modal: Registrar hijo/a (SIN validaciones en cliente) -->
        <div class="modal fade" id="modalNuevoHijo" tabindex="-1" aria-labelledby="nh_title" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content">

                    <div class="modal-header">
                        <h5 class="modal-title" id="nh_title"><i class="bi bi-person-plus"></i> Registrar hijo/a</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>

                    <div class="modal-body">
                        <form id="formNuevoHijo" autocomplete="off">
                            <div class="row g-3">
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Cédula</label>
                                    <input type="text" class="form-control" id="nh_cedula">
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Nombres</label>
                                    <input type="text" class="form-control" id="nh_nombres">
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Apellidos</label>
                                    <input type="text" class="form-control" id="nh_apellidos">
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Fecha de nacimiento</label>
                                    <input type="date" class="form-control" id="nh_fecha">
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Sexo</label>
                                    <select class="form-select" id="nh_sexo">
                                        <option value="">-- Selecciona --</option>
                                        <option value="M">Masculino</option>
                                        <option value="F">Femenino</option>
                                    </select>
                                </div>
                                <div class="col-12 col-md-4">
                                    <label class="form-label">Teléfono</label>
                                    <input type="text" class="form-control" id="nh_telefono">
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Dirección</label>
                                    <input type="text" class="form-control" id="nh_direccion">
                                </div>
                            </div>
                        </form>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cerrar</button>
                        <button type="button" id="btnGuardarHijo" class="btn btn-primary">
                            <i class="bi bi-save"></i> Guardar
                        </button>
                    </div>

                </div>
            </div>
        </div>

        <!-- Bootstrap Bundle -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
            /* =========================
             Config
             ========================= */
            const ctx = '<%=request.getContextPath()%>';
            const apiBase = ctx + '/api/representante';
            const EP = {
                pacientes: apiBase + '/pacientes',
                centros: apiBase + '/centros',
                citas: apiBase + '/citas'
            };

            /* =========================
             Helpers de red (robustos)
             ========================= */
            async function getJSON(url) {
                const r = await fetch(url, {headers: {'Accept': 'application/json'}, credentials: 'same-origin'});
                let data = null;
                try {
                    data = await r.json();
                } catch {
                }
                if (!r.ok)
                    throw new Error((data && (data.error || data.message)) || ('HTTP ' + r.status));
                return data;
            }
            async function postJSON(url, body) {
                const r = await fetch(url, {
                    method: 'POST',
                    headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
                    credentials: 'same-origin',
                    body: JSON.stringify(body || {})
                });
                let data = null;
                try {
                    data = await r.json();
                } catch {
                }
                if (!r.ok)
                    throw new Error((data && (data.error || data.message)) || ('HTTP ' + r.status));
                return data;
            }
            async function putJSON(url, body) {
                const r = await fetch(url, {
                    method: 'PUT',
                    headers: {'Accept': 'application/json', 'Content-Type': 'application/json'},
                    credentials: 'same-origin',
                    body: JSON.stringify(body || {})
                });
                let data = null;
                try {
                    data = await r.json();
                } catch {
                }
                if (!r.ok)
                    throw new Error((data && (data.error || data.message)) || ('HTTP ' + r.status));
                return data;
            }
            async function delJSON(url) {
                const r = await fetch(url, {method: 'DELETE', headers: {'Accept': 'application/json'}, credentials: 'same-origin'});
                let data = null;
                try {
                    data = await r.json();
                } catch {
                }
                if (!r.ok)
                    throw new Error((data && (data.error || data.message)) || ('HTTP ' + r.status));
                return data;
            }

            /* =========================
             Helpers de render
             ========================= */
            function badgeClass(estado) {
                const s = (estado || '').toLowerCase();
                if (s === 'confirmada')
                    return 'success';
                if (s === 'asistio')
                    return 'primary';
                if (s === 'cancelada')
                    return 'secondary';
                return 'warning'; // pendiente u otros
            }
            function safe(s) {
                return (s == null ? '' : String(s));
            }

// Normalizador para campos variables del backend
            function normCita(x) {
                const idCita = x.idCita ?? x.id_cita ?? x.idcita;
                const fechaHora = x.fechaHora ?? x.fecha_hora ?? x.fecha;
                const estado = x.estado;
                const observaciones = x.observaciones ?? x.obs;

                // nombres que puede enviar el backend
                const paciente = x.pacienteNombre ?? x.paciente ?? ((x.paciente_apellidos || x.pac_apellidos || '') + ' ' + (x.paciente_nombres || x.pac_nombres || '')).trim();
                const centro = x.centroNombre   ?? x.centro   ?? x.centro_nombre;

                return {idCita, fechaHora, estado, observaciones, paciente, centro};
            }

            function parseListJSON(j) {
                const items = Array.isArray(j?.items) ? j.items
                        : Array.isArray(j?.data?.items) ? j.data.items : [];
                const total = (typeof j?.total === 'number') ? j.total
                        : (typeof j?.data?.total === 'number') ? j.data.total : items.length;
                const size = (typeof j?.size === 'number') ? j.size
                        : (typeof j?.data?.size === 'number') ? j.data.size : items.length || 10;
                const page = (typeof j?.page === 'number') ? j.page
                        : (typeof j?.data?.page === 'number') ? j.data.page : 1;

                const ok = (j && (j.ok === true || j.success === true || j.status === 'ok')) || Array.isArray(items);
                return {ok, items, total, size, page};
            }

            /* =========================
             Cargar: Hijos/Pacientes
             ========================= */
            async function cargarHijos() {
                const tbody = document.getElementById('tbodyHijos');
                const selPac = document.getElementById('pacienteSel');

                if (tbody)
                    tbody.innerHTML = '<tr><td colspan="8">Cargando...</td></tr>';

                try {
                    const resp = await getJSON(EP.pacientes);
                    const items = (resp && resp.items) ? resp.items : (resp?.data?.items || []);

                    // Tabla
                    if (tbody) {
                        if (!items.length) {
                            tbody.innerHTML = '<tr><td colspan="8" class="text-muted">Sin registros para este tutor.</td></tr>';
                        } else {
                            tbody.innerHTML = items.map((x, i) => (
                                        '<tr>'
                                        + '<td>' + (i + 1) + '</td>'
                                        + '<td>' + safe(x.cedula) + '</td>'
                                        + '<td>' + safe(x.nombres) + '</td>'
                                        + '<td>' + safe(x.apellidos) + '</td>'
                                        + '<td>' + safe(x.fechaNacimiento) + '</td>'
                                        + '<td>' + safe(x.sexo) + '</td>'
                                        + '<td>' + safe(x.telefono) + '</td>'
                                        + '<td>' + safe(x.direccion) + '</td>'
                                        + '</tr>'
                                        )).join('');
                        }
                    }

                    // Combo pacientes
                    if (selPac) {
                        const current = selPac.value;
                        selPac.innerHTML = '<option value="">-- Selecciona --</option>'
                                + items.map(x => (
                                            '<option value="' + safe(x.idPaciente) + '">'
                                            + safe(x.nombres) + ' ' + safe(x.apellidos) + ' (' + safe(x.cedula) + ')</option>'
                                            )).join('');
                        if (current)
                            selPac.value = current;
                    }
                } catch (err) {
                    if (tbody)
                        tbody.innerHTML = '<tr><td colspan="8" class="text-danger">Error: ' + safe(err.message) + '</td></tr>';
                }
            }

            /* =========================
             Cargar: Centros
             ========================= */
            async function cargarCentros() {
                const sel = document.getElementById('centroSel');
                if (sel)
                    sel.innerHTML = '<option value="">Cargando...</option>';
                try {
                    const resp = await getJSON(EP.centros);
                    const items = (resp && resp.items) ? resp.items : (resp?.data?.items || []);
                    if (sel) {
                        const current = sel.value;
                        sel.innerHTML = '<option value="">-- Selecciona --</option>'
                                + items.map(x => '<option value="' + safe(x.idCentro) + '">' + safe(x.nombre) + '</option>').join('');
                        if (current)
                            sel.value = current;
                    }
                } catch (err) {
                    if (sel)
                        sel.innerHTML = '<option value="">(Error al cargar centros)</option>';
                }
            }

            /* =========================
             Cargar: Citas
             ========================= */
            async function cargarCitas() {
                const tbody = document.getElementById('tbodyCitas');
                if (!tbody)
                    return;
                tbody.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';

                try {
                    const resp = await getJSON(EP.citas);
                    const parsed = parseListJSON(resp);
                    const items = parsed.items;

                    if (!items.length) {
                        tbody.innerHTML = '<tr><td colspan="7" class="text-muted">No hay citas.</td></tr>';
                        return;
                    }

                    tbody.innerHTML = items.map((x, i) => {
                        const n = normCita(x);
                        const fh = safe(n.fechaHora).replace('T', ' ').replace('Z', '');
                        const est = safe(n.estado);
                        return (
                                '<tr>'
                                + '<td>' + (i + 1) + '</td>'
                                + '<td>' + fh + '</td>'
                                + '<td>' + safe(n.paciente) + '</td>'
                                + '<td>' + safe(n.centro) + '</td>'
                                + '<td><span class="badge rounded-pill text-bg-' + badgeClass(est) + '">' + est + '</span></td>'
                                + '<td>' + safe(n.observaciones) + '</td>'
                                + '<td class="text-end"><div class="btn-group btn-group-sm">'
                                + '<button class="btn btn-outline-primary" title="Confirmar" onclick="marcarEstado(' + safe(n.idCita) + ',\'confirmada\')"><i class="bi bi-check2-circle"></i></button>'
                                + '<button class="btn btn-outline-success" title="Asistió"   onclick="marcarEstado(' + safe(n.idCita) + ',\'asistio\')"><i class="bi bi-clipboard-check"></i></button>'
                                + '<button class="btn btn-outline-secondary" title="Cancelar" onclick="cancelarCita(' + safe(n.idCita) + ')"><i class="bi bi-x-circle"></i></button>'
                                + '</div></td>'
                                + '</tr>'
                                );
                    }).join('');
                } catch (err) {
                    tbody.innerHTML = '<tr><td colspan="7" class="text-danger">Error: ' + safe(err.message) + '</td></tr>';
                }
            }

            /* =========================
             Crear hijo/a
             ========================= */
            async function guardarHijo() {
                const payload = {
                    cedula: document.getElementById('nh_cedula').value,
                    nombres: document.getElementById('nh_nombres').value,
                    apellidos: document.getElementById('nh_apellidos').value,
                    fechaNacimiento: document.getElementById('nh_fecha').value, // yyyy-MM-dd
                    sexo: document.getElementById('nh_sexo').value,
                    telefono: document.getElementById('nh_telefono').value,
                    direccion: document.getElementById('nh_direccion').value
                };
                try {
                    const resp = await postJSON(EP.pacientes, payload);
                    const idGen = resp.id_paciente || resp?.data?.id_paciente || '';
                    // cerrar modal
                    const modalEl = document.getElementById('modalNuevoHijo');
                    if (modalEl)
                        (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).hide();
                    // limpiar y recargar
                    const form = document.getElementById('formNuevoHijo');
                    if (form)
                        form.reset();
                    await cargarHijos();
                    //alert('Hijo/a registrado. ID: ' + idGen);
                } catch (err) {
                    alert('No se pudo registrar: ' + err.message);
                }
            }

            /* =========================
             Crear/Actualizar/Cancelar cita
             ========================= */
            async function crearCita(e) {
                e.preventDefault();
                const form = document.getElementById('formCita');
                const btn = form ? form.querySelector('[type="submit"]') : null;

                // lee valores del form
                const idPaciente = parseInt(document.getElementById('pacienteSel').value || '0', 10);
                const idCentro = parseInt(document.getElementById('centroSel').value || '0', 10);
                let   fechaHora = (document.getElementById('fechaHora').value || '').trim(); // "YYYY-MM-DDTHH:mm"
                const observ = document.getElementById('observ').value;

                // asegura segundos si vienen sin ":ss"
                if (fechaHora && fechaHora.length === 16)
                    fechaHora += ':00';

                // ⚠️ Enviar en snake_case (lo que espera tu CitasApiServlet)
                const payload = {
                    id_paciente: idPaciente,
                    id_centro: idCentro,
                    fecha_hora: fechaHora, // el servlet acepta con 'T' o con espacio
                    observaciones: observ
                };

                try {
                    if (btn) {
                        btn.disabled = true;
                        btn.dataset._oldText = btn.innerHTML;
                        btn.innerHTML = 'Guardando...';
                    }
                    const resp = await postJSON(EP.citas, payload);
                    const idGen = resp.id_cita ?? resp.idCita ?? resp?.data?.id_cita ?? resp?.data?.idCita ?? '';
                    if (form)
                        form.reset();
                    await cargarCitas();
                    //alert('Cita creada. ID: ' + idGen);
                } catch (err) {
                    alert('Crear cita no disponible en backend: ' + (err?.message || 'Error'));
                } finally {
                    if (btn) {
                        btn.disabled = false;
                        btn.innerHTML = btn.dataset._oldText || 'Guardar';
                    }
                }
            }

// Cambiar estado (POST fallback: evita PATCH/PUT 501)
            async function marcarEstado(idCita, estado) {
                try {
                    const resp = await postJSON(EP.citas, {action: 'estado', id_cita: idCita, estado});
                    const ok = (resp && (resp.ok === true || resp.success === true || resp.status === 'ok' || resp.patched === true));
                    if (!ok)
                        throw new Error(resp?.error || 'Error');
                    await cargarCitas();
                } catch (err) {
                    alert('Actualizar estado no disponible en backend: ' + err.message);
                }
            }

// Eliminar (DELETE con query param id=..., no /citas/{id})
            async function cancelarCita(idCita) {
                try {
                    const resp = await delJSON(EP.citas + '?id=' + encodeURIComponent(idCita));
                    const ok = (resp && (resp.ok === true || resp.success === true || resp.status === 'ok' || resp.deleted === true));
                    if (!ok)
                        throw new Error(resp?.error || 'Error');
                    await cargarCitas();
                } catch (err) {
                    alert('Cancelar cita no disponible en backend: ' + err.message);
                }
            }

            /* =========================
             Eventos
             ========================= */
            document.addEventListener('DOMContentLoaded', function () {
                // Cargas iniciales
                cargarHijos();
                cargarCentros();
                cargarCitas();

                // Botones / formularios
                const btnGuardarHijo = document.getElementById('btnGuardarHijo');
                if (btnGuardarHijo)
                    btnGuardarHijo.addEventListener('click', guardarHijo);

                const formCita = document.getElementById('formCita');
                if (formCita)
                    formCita.addEventListener('submit', crearCita);

                const btnRecargarHijos = document.getElementById('btnRecargarHijos');
                if (btnRecargarHijos)
                    btnRecargarHijos.addEventListener('click', cargarHijos);

                const btnRecargarCitas = document.getElementById('btnRecargarCitas');
                if (btnRecargarCitas)
                    btnRecargarCitas.addEventListener('click', cargarCitas);

                const btnRecargarTodo = document.getElementById('btnRecargarTodo');
                if (btnRecargarTodo)
                    btnRecargarTodo.addEventListener('click', function () {
                        cargarHijos();
                        cargarCentros();
                        cargarCitas();
                    });
            });
        </script>




    </body>
</html>

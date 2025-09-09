<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
    Integer idRol = (Integer) session.getAttribute("idRol"); // 1=admin, 5=tutor
    if (idRol == null)
        idRol = -1;
%>
<!doctype html>
<html lang="es">
    <head>
        <meta charset="utf-8">
        <title>Citas</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Bootstrap 5 -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Bootstrap Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/citas.css">  
    </head>
    <body class="bg-light">
        <div class="container py-4">
            <div class="d-flex align-items-center mb-3">
                <h1 class="h4 mb-0">Gestión de Citas</h1>
                <span class="ms-3 badge bg-secondary">Rol: <%= idRol == 1 ? "Admin" : "Tutor"%></span>
                <div class="ms-auto">
                    <a href="<%= (idRol == 1 ? (ctx + "/Pages/admin.jsp") : (ctx + "/Pages/representante.jsp"))%>" 
                       class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left-circle"></i> Volver al panel
                    </a>
                </div>
            </div>


            <!-- Filtros -->
            <form id="filtros" class="row g-2 mb-3">
                <div class="col-sm-3">
                    <input type="text" name="q" class="form-control" placeholder="Buscar (nombre, centro, obs)">
                </div>
                <div class="col-sm-2">
                    <select name="estado" class="form-select">
                        <option value="">-- Estado --</option>
                        <option value="pendiente">Pendiente</option>
                        <option value="confirmada">Confirmada</option>
                        <option value="asistio">Asistió</option>
                        <option value="cancelada">Cancelada</option>
                    </select>
                </div>
                <div class="col-sm-2">
                    <input type="text" name="cedula" class="form-control" placeholder="Cédula">
                </div>
                <div class="col-sm-2">
                    <input type="datetime-local" name="desde" class="form-control" placeholder="Desde">
                </div>
                <div class="col-sm-2">
                    <input type="datetime-local" name="hasta" class="form-control" placeholder="Hasta">
                </div>
                <div class="col-sm-1 d-grid">
                    <button class="btn btn-primary" type="submit"><i class="bi bi-search"></i></button>
                </div>
            </form>

            <div class="mb-3">
                <button class="btn btn-success" type="button" onclick="openNew()">
                    <i class="bi bi-plus-lg"></i> Nueva cita
                </button>
            </div>

            <!-- Tabla -->
            <div class="table-responsive">
                <table class="table table-striped align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Fecha/Hora</th>
                            <th>Paciente</th>
                            <th>Cédula</th>
                            <th>Centro</th>
                            <th>Estado</th>
                            <th>Observaciones</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                    </thead>
                    <tbody id="tbody"></tbody>
                </table>
            </div>

            <!-- Paginación -->
            <nav>
                <ul id="pager" class="pagination"></ul>
            </nav>
        </div>

        <!-- Modal -->
        <div class="modal fade" id="modalCita" tabindex="-1">
            <div class="modal-dialog">
                <form id="frmCita" class="modal-content">
                    <div class="modal-header">
                        <h5 id="modalTitle" class="modal-title">Nueva cita</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <input type="hidden" id="id_cita">
                        <div class="mb-2">
                            <label class="form-label">ID Paciente</label>
                            <input type="number" id="id_paciente" class="form-control" placeholder="ID del paciente" required>
                        </div>
                        <div class="mb-2">
                            <label class="form-label">ID Centro</label>
                            <input type="number" id="id_centro" class="form-control" placeholder="ID del centro" required>
                        </div>
                        <div class="mb-2">
                            <label class="form-label">Fecha/Hora</label>
                            <input type="datetime-local" id="fecha_hora_local" class="form-control" required>
                        </div>
                        <div class="mb-2">
                            <label class="form-label">Estado</label>
                            <select id="estado" class="form-select">
                                <option value="pendiente">Pendiente</option>
                                <option value="confirmada">Confirmada</option>
                                <option value="asistio">Asistió</option>
                                <option value="cancelada">Cancelada</option>
                            </select>
                        </div>
                        <div class="mb-2">
                            <label class="form-label">Observaciones</label>
                            <textarea id="observaciones" class="form-control" rows="2" placeholder="Opcional"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" data-bs-dismiss="modal" type="button">Cancelar</button>
                        <button class="btn btn-primary" type="submit">Guardar</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <script>
                    /* ========= Contexto ========= */
                    const ctx = '<%=ctx%>';
                    const ID_ROL = <%= idRol%>; // 1=admin, 5=tutor
                    const BASE = ctx + (ID_ROL === 1 ? '/api/admin/citas' : '/api/representante/citas');

                    // Refs
                    const modal = new bootstrap.Modal(document.getElementById('modalCita'));
                    const modalTitle = document.getElementById('modalTitle');
                    const frmCita = document.getElementById('frmCita');
                    const filtros = document.getElementById('filtros');
                    const tbody = document.getElementById('tbody');
                    const pager = document.getElementById('pager');

                    /* ========= Utils ========= */
                    function readFecha(v) {
                        if (!v)
                            return '';
                        if (typeof v === 'string')
                            return v.replace('T', ' ').slice(0, 16);
                        try {
                            const d = v.date || v, t = v.time || v;
                            const y = Number(d.year || v.year), m = Number(d.month || v.month || 1), d2 = Number(d.day || v.day || 1);
                            const H = Number(t.hour || v.hour || 0), M = Number(t.minute || v.minute || 0);
                            if (!y)
                                return '';
                            const pad = n => String(n).padStart(2, '0');
                            return y + '-' + pad(m) + '-' + pad(d2) + ' ' + pad(H) + ':' + pad(M);
                        } catch {
                            return '';
                        }
                    }
                    const toApiDateFromLocal = s => (s || '').replace('T', ' ').slice(0, 16);
                    const toLocalInputFromApi = s => !s ? '' : (s.replace('T', ' ').slice(0, 10) + 'T' + s.replace('T', ' ').slice(11, 16));
                    const badgeClass = e => e === 'confirmada' ? 'primary' : e === 'asistio' ? 'success' : e === 'cancelada' ? 'danger' : 'secondary';
                    const escapeHtml = s => !s ? '' : s.replace(/[&<>"']/g, m => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[m]));

                    function norm(x) {
                        const idCita = x.idCita ?? x.id_cita ?? x.idcita;
                        const idPaciente = x.idPaciente ?? x.id_paciente;
                        const idCentro = x.idCentro ?? x.id_centro;
                        const fechaHora = x.fechaHora ?? x.fecha_hora ?? x.fecha;
                        const estado = x.estado;
                        const observaciones = x.observaciones ?? x.obs;

                        const pacienteNombre = x.pacienteNombre
                                ?? x.paciente
                                ?? ((x.pac_apellidos || x.paciente_apellidos || '') + ' ' + (x.pac_nombres || x.paciente_nombres || '')).trim();

                        const pacienteCedula = x.pacienteCedula ?? x.cedula ?? x.paciente_cedula ?? x.pac_cedula;
                        const centroNombre = x.centroNombre ?? x.centro ?? x.centro_nombre;

                        return {idCita, idPaciente, idCentro, fechaHora, estado, observaciones, pacienteNombre, pacienteCedula, centroNombre};
                    }

                    function rowHtml(raw) {
                        const x = norm(raw);
                        const fh = readFecha(x.fechaHora);
                        const estadoBadge = '<span class="badge rounded-pill text-bg-' + badgeClass(x.estado) + '">' + (x.estado || '') + '</span>';
                        return ''
                                + '<tr>'
                                + '<td>' + (x.idCita || '') + '</td>'
                                + '<td>' + fh + '</td>'
                                + '<td>' + escapeHtml(x.pacienteNombre || '') + '</td>'
                                + '<td>' + escapeHtml(x.pacienteCedula || '') + '</td>'
                                + '<td>' + escapeHtml(x.centroNombre || '') + '</td>'
                                + '<td>' + estadoBadge + '</td>'
                                + '<td>' + escapeHtml(x.observaciones || '') + '</td>'
                                + '<td class="text-end"><div class="btn-group">'
                                + '<button class="btn btn-sm btn-outline-secondary" title="Editar"   onclick="openEditById(' + (x.idCita || 0) + ')"><i class="bi bi-pencil"></i></button>'
                                + '<button class="btn btn-sm btn-outline-primary"   title="Confirmar" onclick="marcar(' + (x.idCita || 0) + ',\'confirmada\')"><i class="bi bi-check2-circle"></i></button>'
                                + '<button class="btn btn-sm btn-outline-success"   title="Asistió"   onclick="marcar(' + (x.idCita || 0) + ',\'asistio\')"><i class="bi bi-person-check"></i></button>'
                                + '<button class="btn btn-sm btn-outline-warning"   title="Cancelar"  onclick="marcar(' + (x.idCita || 0) + ',\'cancelada\')"><i class="bi bi-x-circle"></i></button>'
                                + '<button class="btn btn-sm btn-outline-danger"    title="Eliminar"  onclick="delCita(' + (x.idCita || 0) + ')"><i class="bi bi-trash"></i></button>'
                                + '</div></td>'
                                + '</tr>';
                    }

                    function parseListJSON(j) {
                        const ok =
                                (j && (j.ok === true || j.success === true || j.status === 'ok')) ||
                                (j && Array.isArray(j.items)) ||
                                (j && j.data && Array.isArray(j.data.items));

                        const items = Array.isArray(j?.items) ? j.items
                                : Array.isArray(j?.data?.items) ? j.data.items : [];
                        const total = (typeof j?.total === 'number') ? j.total
                                : (typeof j?.data?.total === 'number') ? j.data.total : items.length;
                        const size = (typeof j?.size === 'number') ? j.size
                                : (typeof j?.data?.size === 'number') ? j.data.size : 10;
                        const page = (typeof j?.page === 'number') ? j.page
                                : (typeof j?.data?.page === 'number') ? j.data.page : 1;

                        return {ok, items, total, size, page};
                    }

                    async function list(page = 1) {
                        const fd = new FormData(filtros);
                        const params = new URLSearchParams(fd);
                        params.set('page', page);
                        params.set('size', 10);

                        let r;
                        try {
                            r = await fetch(BASE + '?' + params.toString(), {
                                credentials: 'same-origin',
                                headers: {'Accept': 'application/json'}
                            });
                        } catch {
                            alert('No se pudo contactar al servidor.');
                            return;
                        }
                        if (!r.ok) {
                            const msg = r.status === 401 ? 'Sesión inválida o expirada.'
                                    : r.status === 403 ? 'No autorizado para ver este recurso.'
                                    : 'Error del servidor (' + r.status + ').';
                            alert(msg);
                            return;
                        }

                        let j;
                        try {
                            j = await r.json();
                        } catch {
                            alert('Respuesta inválida del servidor');
                            return;
                        }
                        const parsed = parseListJSON(j);
                        if (!parsed.ok) {
                            alert((j && (j.error || j.message)) || 'Error al listar');
                            return;
                        }

                        tbody.innerHTML = parsed.items.map(rowHtml).join('');
                        const pages = Math.max(1, Math.ceil(parsed.total / parsed.size));
                        pager.innerHTML = '';
                        for (let i = 1; i <= pages; i++) {
                            const li = document.createElement('li');
                            li.className = 'page-item ' + (i === page ? 'active' : '');
                            li.innerHTML = '<a class="page-link" href="#">' + i + '</a>';
                            li.querySelector('a').addEventListener('click', ev => {
                                ev.preventDefault();
                                list(i);
                            });
                            pager.appendChild(li);
                    }
                    }

                    function openNew() {
                        modalTitle.textContent = 'Nueva cita';
                        frmCita.reset();
                        document.getElementById('id_cita').value = '';
                        document.getElementById('estado').value = 'pendiente';
                        modal.show();
                    }

                    async function openEditById(id) {
                        try {
                            const r = await fetch(BASE + '?id=' + id, {credentials: 'same-origin', headers: {'Accept': 'application/json'}});
                            if (!r.ok) {
                                alert(r.status === 403 ? 'No autorizado.' : 'No se pudo obtener la cita');
                                return;
                            }
                            const j = await r.json();
                            const data = j?.data ?? j;
                            if ((j && (j.ok === false || j.success === false)) && !data) {
                                alert(j.error || j.message || 'No se pudo obtener la cita');
                                return;
                            }
                            openEdit(data);
                        } catch {
                            alert('Error obteniendo la cita');
                        }
                    }

                    function openEdit(raw) {
                        const x = norm(raw);
                        modalTitle.textContent = 'Editar cita';
                        document.getElementById('id_cita').value = x.idCita || '';
                        document.getElementById('id_paciente').value = x.idPaciente || '';
                        document.getElementById('id_centro').value = x.idCentro || '';
                        document.getElementById('fecha_hora_local').value = toLocalInputFromApi(readFecha(x.fechaHora));
                        document.getElementById('estado').value = x.estado || 'pendiente';
                        document.getElementById('observaciones').value = x.observaciones || '';
                        modal.show();
                    }

                    // Fallback universal: acción estado via POST (evita PATCH 501)
                    async function marcar(id, estado) {
                        const r = await fetch(BASE, {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
                            credentials: 'same-origin',
                            body: JSON.stringify({action: 'estado', id_cita: id, estado})
                        });
                        let j;
                        try {
                            j = await r.json();
                        } catch {
                            alert('Respuesta inválida del servidor');
                            return;
                        }
                        const ok = (j && (j.ok === true || j.success === true || j.status === 'ok' || j.patched === true));
                        if (!r.ok || !ok) {
                            alert(j && (j.error || j.message) ? (j.error || j.message) : 'No se pudo actualizar estado');
                            return;
                        }
                        list();
                    }

                    frmCita.addEventListener('submit', async (e) => {
                        e.preventDefault();
                        const id = document.getElementById('id_cita').value;
                        const body = {
                            id_cita: id ? Number(id) : undefined,
                            id_paciente: Number(document.getElementById('id_paciente').value),
                            id_centro: Number(document.getElementById('id_centro').value),
                            fecha_hora: toApiDateFromLocal(document.getElementById('fecha_hora_local').value),
                            estado: document.getElementById('estado').value,
                            observaciones: document.getElementById('observaciones').value || null
                        };
                        const r = await fetch(BASE, {
                            method: id ? 'PUT' : 'POST',
                            headers: {'Content-Type': 'application/json', 'Accept': 'application/json'},
                            credentials: 'same-origin',
                            body: JSON.stringify(body)
                        });
                        let j;
                        try {
                            j = await r.json();
                        } catch {
                            alert('Respuesta inválida del servidor');
                            return;
                        }
                        const ok = (j && (j.ok === true || j.success === true || j.status === 'ok' || j.id_cita || j.updated === true));
                        if (!r.ok || !ok) {
                            alert(j && (j.error || j.message) ? (j.error || j.message) : 'No se pudo guardar');
                            return;
                        }
                        modal.hide();
                        list();
                    });

                    async function delCita(id) {
                        if (!confirm('¿Eliminar esta cita?'))
                            return;
                        const r = await fetch(BASE + '?id=' + id, {method: 'DELETE', credentials: 'same-origin', headers: {'Accept': 'application/json'}});
                        let j;
                        try {
                            j = await r.json();
                        } catch {
                            alert('Respuesta inválida del servidor');
                            return;
                        }
                        const ok = (j && (j.ok === true || j.success === true || j.status === 'ok' || j.deleted === true));
                        if (!r.ok || !ok) {
                            alert(j && (j.error || j.message) ? (j.error || j.message) : 'No se pudo eliminar');
                            return;
                        }
                        list();
                    }

                    filtros.addEventListener('submit', e => {
                        e.preventDefault();
                        list(1);
                    });

                    // Inicial
                    list();
        </script>
    </body>
</html>

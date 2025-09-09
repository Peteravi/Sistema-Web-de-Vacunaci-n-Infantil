<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>

<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>VacuKids · Esquemas de Vacunación</title>
        <meta name="viewport" content="width=device-width, initial-scale=1" />

        <!-- Bootstrap 5 -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
         <link rel="stylesheet" href="<%=ctx%>/assets/css/esquemas_view.css">
    </head>
    <body>

        <div class="container py-4">

            <!-- Header -->
            <div class="page-header mb-3">
                <a class="btn btn-outline-secondary btn-sm" href="<%=ctx%>/Pages/admin.jsp">← Volver al panel</a>
                <h1>Esquemas de Vacunación</h1>
            </div>

            <!-- Toolbar de lista -->
            <div class="card mb-4">
                <div class="card-body">
                    <div class="row g-2 align-items-end">
                        <div class="col-sm-4">
                            <label class="form-label">Buscar</label>
                            <input id="q" type="text" class="form-control" placeholder="Ej. MSP 2025, Pentavalente"/>
                        </div>
                        <div class="col-sm-2">
                            <label class="form-label">Límite</label>
                            <select id="limit" class="form-select">
                                <option>10</option>
                                <option selected>20</option>
                                <option>50</option>
                            </select>
                        </div>
                        <div class="col-sm-6 text-sm-end">
                            <button id="btnBuscar" class="btn btn-primary">Buscar</button>
                            <button id="btnNuevo" class="btn btn-success ms-2" data-bs-toggle="modal" data-bs-target="#modalEsquema">Nuevo esquema</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tabla -->
            <div class="card mb-4">
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle" id="tblEsquemas">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nombre</th>
                                    <th>Vigencia</th>
                                    <th>Versión</th>
                                    <th>Activo</th>
                                    <th class="text-end">Acciones</th>
                                </tr>
                            </thead>
                            <tbody><!-- rows --></tbody>
                        </table>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <div><span id="lblResumen">0 resultados</span></div>
                        <div class="btn-group">
                            <button id="prev" class="btn btn-outline-secondary btn-sm">« Anterior</button>
                            <button id="next" class="btn btn-outline-secondary btn-sm">Siguiente »</button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Panel de Detalle -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <div>
                        <strong>Detalle del Esquema</strong>
                        <div class="text-muted small" id="lblEsquemaSel">Selecciona un esquema para editar su detalle.</div>
                    </div>
                    <div>
                        <button id="btnAgregarFila" class="btn btn-outline-primary btn-sm" disabled>Agregar fila</button>
                        <button id="btnGuardarDetalle" class="btn btn-primary btn-sm" disabled>Guardar detalle (reemplazar)</button>
                    </div>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-sm align-middle" id="tblDetalle">
                            <thead>
                                <tr>
                                    <th class="text-nowrap">Vacuna (ID)</th>
                                    <th>Dosis #</th>
                                    <th title="Edad mínima recomendada (meses)">Edad Min (m)</th>
                                    <th title="Edad máxima recomendada (meses)">Edad Max (m)</th>
                                    <th title="Intervalo mínimo desde la dosis anterior (días)">Int. Min (d)</th>
                                    <th title="Intervalo máximo desde la dosis anterior (días)">Int. Max (d)</th>
                                    <th>Req. dosis previa</th>
                                    <th>Observaciones</th>
                                    <th class="text-end">—</th>
                                </tr>
                            </thead>
                            <tbody><!-- filas detalle --></tbody>
                        </table>
                    </div>
                    <div class="form-hint">* Solo envía campos numéricos cuando apliquen; deja vacío si no corresponde.</div>
                </div>
            </div>

        </div>

        <!-- Modal Crear/Editar Esquema -->
        <div class="modal fade" id="modalEsquema" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <form class="modal-content" id="formEsquema">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modalTitle">Nuevo esquema</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                    </div>
                    <div class="modal-body">
                        <input type="hidden" id="id_esquema" name="id_esquema"/>
                        <div class="mb-3">
                            <label class="form-label">Nombre</label>
                            <input class="form-control" id="nombre" name="nombre" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Descripción</label>
                            <textarea class="form-control" id="descripcion" name="descripcion" rows="2"></textarea>
                        </div>
                        <div class="row g-2">
                            <div class="col-md-6">
                                <label class="form-label">Vigente desde</label>
                                <input type="date" class="form-control" id="vigente_desde" name="vigente_desde" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Vigente hasta</label>
                                <input type="date" class="form-control" id="vigente_hasta" name="vigente_hasta" />
                            </div>
                        </div>
                        <div class="row g-2 mt-1">
                            <div class="col-md-6">
                                <label class="form-label">Versión</label>
                                <input type="number" class="form-control" id="version" name="version" min="1" value="1" />
                            </div>
                            <div class="col-md-6 d-flex align-items-end">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="activo" name="activo">
                                    <label class="form-check-label" for="activo">Activo</label>
                                </div>
                            </div>
                        </div>
                        <div class="form-hint mt-2">Puedes activar luego desde la lista (solo 1 activo a la vez).</div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" data-bs-dismiss="modal" type="button">Cancelar</button>
                        <button class="btn btn-primary" type="submit" id="btnSaveEsquema">Guardar</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Toasts -->
        <div class="position-fixed bottom-0 end-0 p-3" style="z-index: 1080">
            <div id="toast" class="toast align-items-center text-bg-dark border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body" id="toastBody">...</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        </div>

        <!-- Bootstrap JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            (() => {
                const ctx = "<%=ctx%>";
                const api = ctx + "/admin/esquemas";
                const tblBody = document.querySelector("#tblEsquemas tbody");
                const lblResumen = document.getElementById("lblResumen");
                const q = document.getElementById("q");
                const limitSel = document.getElementById("limit");
                const btnBuscar = document.getElementById("btnBuscar");
                const btnNuevo = document.getElementById("btnNuevo");
                const prevBtn = document.getElementById("prev");
                const nextBtn = document.getElementById("next");

                // detalle
                const lblEsqSel = document.getElementById("lblEsquemaSel");
                const btnAgregarFila = document.getElementById("btnAgregarFila");
                const btnGuardarDetalle = document.getElementById("btnGuardarDetalle");
                const tblDetBody = document.querySelector("#tblDetalle tbody");

                // modal
                const modalEl = document.getElementById("modalEsquema");
                const modal = new bootstrap.Modal(modalEl);
                const formEsquema = document.getElementById("formEsquema");
                const modalTitle = document.getElementById("modalTitle");

                // toast
                const toastEl = document.getElementById("toast");
                const toastBody = document.getElementById("toastBody");
                const toast = new bootstrap.Toast(toastEl);

                let offset = 0;
                let currentId = null; // esquema seleccionado para detalle

                function showToast(msg, ok = true) {
                    toastEl.classList.remove("text-bg-danger", "text-bg-dark", "text-bg-success");
                    toastEl.classList.add(ok ? "text-bg-success" : "text-bg-danger");
                    toastBody.textContent = msg;
                    toast.show();
                }

                async function getJSON(url) {
                    const r = await fetch(url, {headers: {"Accept": "application/json"}});
                    return r.json();
                }
                async function postForm(url, data) {
                    const r = await fetch(url, {
                        method: "POST",
                        headers: {"Accept": "application/json", "Content-Type": "application/x-www-form-urlencoded"},
                        body: new URLSearchParams(data)
                    });
                    return r.json();
                }

                // --------- Listado principal ----------
                async function loadList() {
                    const params = new URLSearchParams({
                        action: "list",
                        q: q.value.trim(),
                        limit: limitSel.value,
                        offset: offset
                    });
                    const res = await getJSON(api + "?" + params.toString());
                    if (!res.ok) {
                        showToast(res.msg || "Error al listar", false);
                        return;
                    }
                    const arr = res.data || [];
                    renderRows(arr);
                    lblResumen.textContent = `${arr.length} resultados (página ${Math.floor(offset/limitSel.value)+1})`;
                    prevBtn.disabled = offset <= 0;
                    nextBtn.disabled = arr.length < Number(limitSel.value);
                }

                function renderRows(arr) {
                    tblBody.innerHTML = "";
                    arr.forEach(e => {
                        const tr = document.createElement("tr");
                        const activoBadge = e.activo
                                ? '<span class="badge badge-active">Sí</span>'
                                : '<span class="badge badge-inactive">No</span>';

                        tr.innerHTML =
                                '<td>' + (e.id_esquema ?? '') + '</td>' +
                                '<td class="pointer text-primary select-esquema" data-id="' + (e.id_esquema ?? '') + '" title="Editar detalle">' + escapeHtml(e.nombre) + '</td>' +
                                '<td>' + escapeHtml(e.vigente_desde) + ' — ' + escapeHtml(e.vigente_hasta) + '</td>' +
                                '<td>' + e.version + '</td>' +
                                '<td>' + activoBadge + '</td>' +
                                '<td class="text-end">' +
                                '<div class="btn-group">' +
                                '<button class="btn btn-outline-secondary btn-sm btn-edit" data-id="' + (e.id_esquema ?? '') + '">Editar</button>' +
                                '<button class="btn btn-outline-success btn-sm btn-activate" data-id="' + (e.id_esquema ?? '') + '">Activar</button>' +
                                '<button class="btn btn-outline-danger btn-sm btn-del" data-id="' + (e.id_esquema ?? '') + '">Eliminar</button>' +
                                '</div>' +
                                '</td>';
                        tblBody.appendChild(tr);
                    });
                }


                // --------- Selección de esquema para detalle ----------
                async function selectEsquema(id) {
                    currentId = id;
                    lblEsqSel.textContent = `Esquema seleccionado: #${id}`;
                    btnAgregarFila.disabled = false;
                    btnGuardarDetalle.disabled = false;
                    await loadDetalle();
                }

                // --------- CRUD Esquema ----------
                function openModalCreate() {
                    modalTitle.textContent = "Nuevo esquema";
                    formEsquema.reset();
                    formEsquema.id_esquema.value = "";
                    formEsquema.version.value = 1;
                    formEsquema.activo.checked = false;
                    modal.show();
                }

                async function openModalEdit(id) {
                    const res = await getJSON(api + "?action=get&id=" + id);
                    if (!res.ok) {
                        showToast(res.msg || "No encontrado", false);
                        return;
                    }
                    const e = res.data;
                    modalTitle.textContent = "Editar esquema";
                    formEsquema.id_esquema.value = e.id_esquema;
                    formEsquema.nombre.value = e.nombre || "";
                    formEsquema.descripcion.value = e.descripcion || "";
                    formEsquema.vigente_desde.value = e.vigente_desde || "";
                    formEsquema.vigente_hasta.value = e.vigente_hasta || "";
                    formEsquema.version.value = e.version || 1;
                    formEsquema.activo.checked = !!e.activo;
                    modal.show();
                }

                formEsquema.addEventListener("submit", async (ev) => {
                    ev.preventDefault();
                    const isEdit = !!formEsquema.id_esquema.value;
                    const data = {
                        action: isEdit ? "update" : "create",
                        id_esquema: formEsquema.id_esquema.value,
                        nombre: formEsquema.nombre.value.trim(),
                        descripcion: formEsquema.descripcion.value.trim(),
                        vigente_desde: formEsquema.vigente_desde.value,
                        vigente_hasta: formEsquema.vigente_hasta.value,
                        version: formEsquema.version.value || 1,
                        activo: formEsquema.activo.checked ? "1" : ""
                    };
                    const res = await postForm(api, data);
                    if (!res.ok) {
                        showToast(res.msg || "Error al guardar", false);
                        return;
                    }
                    showToast(isEdit ? "Esquema actualizado" : "Esquema creado");
                    modal.hide();
                    await loadList();
                });

                async function activateEsquema(id) {
                    const res = await postForm(api, {action: "activate", id_esquema: id});
                    if (!res.ok) {
                        showToast(res.msg || "No se pudo activar", false);
                        return;
                    }
                    showToast("Esquema activado");
                    await loadList();
                }

                async function deleteEsquema(id) {
                    if (!confirm("¿Eliminar el esquema #" + id + "? Esta acción es irreversible."))
                        return;
                    const res = await postForm(api, {action: "delete", id_esquema: id});
                    if (!res.ok) {
                        showToast(res.msg || "No se pudo eliminar", false);
                        return;
                    }
                    showToast("Esquema eliminado");
                    if (currentId == id) {
                        currentId = null;
                        lblEsqSel.textContent = "Selecciona un esquema para editar su detalle.";
                        btnAgregarFila.disabled = true;
                        btnGuardarDetalle.disabled = true;
                        tblDetBody.innerHTML = "";
                    }
                    await loadList();
                }

                // --------- Detalle ----------
                function nuevaFilaDetalle(row = {}) {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="id_vacuna" value="${safe(row.id_vacuna)}"></td>
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="dosis" value="${safe(row.nro_dosis)}"></td>
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="emin" value="${safe(row.edad_min_meses)}"></td>
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="emax" value="${safe(row.edad_max_meses)}"></td>
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="imin" value="${safe(row.intervalo_min_dias)}"></td>
              <td><input type="number" class="form-control form-control-sm small-input" placeholder="imax" value="${safe(row.intervalo_max_dias)}"></td>
              <td class="text-center"><input type="checkbox" class="form-check-input" ${row.requisito_dosis_previa ? "checked": ""}></td>
              <td><input type="text" class="form-control form-control-sm" placeholder="Observaciones" value="${escapeAttr(row.observaciones || "")}"></td>
              <td class="text-end"><button class="btn btn-sm btn-outline-danger btn-remove">✕</button></td>
            `;
                    tblDetBody.appendChild(tr);
                }

                async function loadDetalle() {
                    tblDetBody.innerHTML = "";
                    if (!currentId)
                        return;
                    const res = await getJSON(api + "?action=listDetalle&id_esquema=" + currentId);
                    if (!res.ok) {
                        showToast(res.msg || "Error al cargar detalle", false);
                        return;
                    }
                    (res.data || []).forEach(nuevaFilaDetalle);
                }

                function recogerDetalleComoFormData() {
                    const fd = new URLSearchParams();
                    fd.append("action", "replaceDetalle");
                    fd.append("id_esquema", currentId);

                    const rows = Array.from(tblDetBody.querySelectorAll("tr"));
                    if (rows.length === 0)
                        return fd;

                    rows.forEach(tr => {
                        const inputs = tr.querySelectorAll("input");
                        const idVac = valOrNull(inputs[0].value);
                        const dosis = valOrNull(inputs[1].value);
                        const emin = valOrNull(inputs[2].value);
                        const emax = valOrNull(inputs[3].value);
                        const imin = valOrNull(inputs[4].value);
                        const imax = valOrNull(inputs[5].value);
                        const req = inputs[6].checked ? "1" : "";
                        const obs = inputs[7].value || "";

                        if (!idVac || !dosis)
                            return; // se ignoran filas inválidas

                        fd.append("id_vacuna[]", idVac);
                        fd.append("nro_dosis[]", dosis);
                        if (emin !== null)
                            fd.append("edad_min_meses[]", emin);
                        else
                            fd.append("edad_min_meses[]", "");
                        if (emax !== null)
                            fd.append("edad_max_meses[]", emax);
                        else
                            fd.append("edad_max_meses[]", "");
                        if (imin !== null)
                            fd.append("intervalo_min_dias[]", imin);
                        else
                            fd.append("intervalo_min_dias[]", "");
                        if (imax !== null)
                            fd.append("intervalo_max_dias[]", imax);
                        else
                            fd.append("intervalo_max_dias[]", "");
                        fd.append("requisito_dosis_previa[]", req);
                        fd.append("observaciones[]", obs);
                    });
                    return fd;
                }

                async function saveDetalle() {
                    if (!currentId) {
                        showToast("Selecciona un esquema", false);
                        return;
                    }
                    const fd = recogerDetalleComoFormData();
                    const r = await fetch(api, {method: "POST", headers: {"Accept": "application/json"}, body: fd});
                    const res = await r.json();
                    if (!res.ok) {
                        showToast(res.msg || "Error al guardar detalle", false);
                        return;
                    }
                    showToast("Detalle guardado");
                    await loadDetalle();
                }

                // --------- Utils ----------
                function valOrNull(v) {
                    const s = String(v || "").trim();
                    if (s === "")
                        return null;
                    const n = Number(s);
                    return Number.isNaN(n) ? null : n;
                }
                function safe(v) {
                    return (v === undefined || v === null) ? "" : v;
                }
                function escapeHtml(s) {
                    return String(s ?? "").replace(/[&<>"']/g, m => ({
                            "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#039;"
                        }[m]));
                }
                function escapeAttr(s) {
                    return escapeHtml(s).replace(/"/g, "&quot;");
                }

                // --------- Eventos ----------
                btnBuscar.addEventListener("click", () => {
                    offset = 0;
                    loadList();
                });
                limitSel.addEventListener("change", () => {
                    offset = 0;
                    loadList();
                });
                prevBtn.addEventListener("click", () => {
                    offset = Math.max(0, offset - Number(limitSel.value));
                    loadList();
                });
                nextBtn.addEventListener("click", () => {
                    offset = offset + Number(limitSel.value);
                    loadList();
                });

                btnNuevo.addEventListener("click", openModalCreate);

                tblBody.addEventListener("click", (ev) => {
                    const t = ev.target;
                    if (t.classList.contains("btn-edit")) {
                        openModalEdit(t.dataset.id);
                    } else if (t.classList.contains("btn-activate")) {
                        activateEsquema(t.dataset.id);
                    } else if (t.classList.contains("btn-del")) {
                        deleteEsquema(t.dataset.id);
                    } else if (t.classList.contains("select-esquema")) {
                        selectEsquema(t.dataset.id);
                    }
                });

                btnAgregarFila.addEventListener("click", () => nuevaFilaDetalle({}));
                tblDetBody.addEventListener("click", (ev) => {
                    if (ev.target.classList.contains("btn-remove")) {
                        ev.target.closest("tr").remove();
                    }
                });
                btnGuardarDetalle.addEventListener("click", saveDetalle);

                // init
                loadList();
            })();
        </script>

    </body>
</html>

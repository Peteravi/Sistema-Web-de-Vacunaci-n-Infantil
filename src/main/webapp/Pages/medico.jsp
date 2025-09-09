<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Panel Médico · VacuKids</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Bootstrap & Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/medico.css">
    </head>
    <body class="bg-light">
        <div class="container py-4">
            <div class="d-flex align-items-center justify-content-between mb-3">
                <h1 class="h4 mb-0">Panel Médico</h1>
                <a class="btn btn-outline-secondary btn-sm" href="<%=ctx%>/logout"><i class="bi bi-box-arrow-right"></i> Salir</a>
            </div>

            <!-- KPIs -->
            <div class="row g-3 mb-3" id="kpiRow">
                <div class="col-6 col-lg-3">
                    <div class="card card-kpi p-3">
                        <div class="text-muted small">Pacientes</div>
                        <div class="fs-3 fw-semibold" id="cardTotalPacientes">0</div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="card card-kpi p-3">
                        <div class="text-muted small">Citas (hoy)</div>
                        <div class="fs-3 fw-semibold">
                            <span id="cardCitasHoy">0</span>
                            <span class="badge badge-soft ms-2" id="cardConfirmadasHoy">0</span>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="card card-kpi p-3">
                        <div class="text-muted small">Aplicaciones (hoy)</div>
                        <div class="fs-3 fw-semibold" id="cardAplicacionesHoy">0</div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="card card-kpi p-3">
                        <div class="text-muted small">Stock bajo</div>
                        <div class="fs-3 fw-semibold" id="cardStockBajo">0</div>
                    </div>
                </div>
            </div>

            <!-- Filtros -->
            <div class="card mb-3">
                <div class="card-body">
                    <form id="filtrosForm" class="row g-2 align-items-end">
                        <div class="col-12 col-md-4">
                            <label class="form-label">Buscar (cédula / nombres / tutor)</label>
                            <input type="text" class="form-control" id="q" placeholder="Ej: 095..., Mateo, Benítez">
                        </div>
                        <div class="col-6 col-md-2">
                            <label class="form-label">Vacuna</label>
                            <select class="form-select" id="vacunaId">
                                <option value="">Todas</option>
                                <option value="1">BCG</option>
                                <option value="2">Hepatitis B</option>
                                <option value="3">Pentavalente</option>
                                <option value="4">IPV</option>
                                <option value="5">SRP (MMR)</option>
                            </select>
                        </div>
                        <div class="col-6 col-md-2">
                            <label class="form-label">Desde</label>
                            <input type="date" class="form-control" id="desde">
                        </div>
                        <div class="col-6 col-md-2">
                            <label class="form-label">Hasta</label>
                            <input type="date" class="form-control" id="hasta">
                        </div>
                        <div class="col-6 col-md-2">
                            <button class="btn btn-primary w-100" type="submit"><i class="bi bi-search"></i> Filtrar</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Tabla Vacunas aplicadas por niño -->
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span class="fw-semibold">Vacunas aplicadas por niño</span>
                    <div class="small text-muted"><span id="totalLbl">0</span> resultados</div>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" id="tablaPacientes">
                        <thead class="table-light">
                            <tr>
                                <th>Paciente</th>
                                <th>Cédula</th>
                                <th>Tutor</th>
                                <th class="text-end">Total aplicadas</th>
                                <th>Última aplicación</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody id="tbodyAplicadas"></tbody>
                    </table>
                </div>
                <div class="card-footer d-flex justify-content-between align-items-center">
                    <div class="small text-muted" id="pageInfo"></div>
                    <div class="btn-group">
                        <button class="btn btn-outline-secondary btn-sm" id="prevBtn" type="button">Anterior</button>
                        <button class="btn btn-outline-secondary btn-sm" id="nextBtn" type="button">Siguiente</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal detalle -->
        <div class="modal fade" id="detalleModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-lg modal-dialog-scrollable">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Detalle de aplicaciones</h5>
                        <button class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar" type="button"></button>
                    </div>
                    <div class="modal-body">
                        <div class="table-responsive">
                            <table class="table table-sm align-middle">
                                <thead>
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Vacuna</th>
                                        <th>Dosis</th>
                                        <th>Centro</th>
                                        <th>Efectos</th>
                                        <th class="text-end">Certificado</th>
                                    </tr>
                                </thead>
                                <tbody id="tbodyDetalle"></tbody>
                            </table>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" data-bs-dismiss="modal" type="button">Cerrar</button>
                    </div>
                </div>
            </div>
        </div>

        <script>const CTX = "<%=ctx%>";</script>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

        <script>
            (function () {
                const base = CTX + "/api/medico/panel";
                const state = {page: 1, size: 10, total: 0};

                // Control de duplicados de descarga
                const downloading = new Set(); // ids en curso
                let lastDownload = {id: null, t: 0}; // anti-rebote 3s

                function fmt(v, d) {
                    return (v === null || v === undefined) ? (d || "-") : v;
                }

                async function fetchJSON(url) {
                    const r = await fetch(url, {
                        headers: {"Accept": "application/json"},
                        credentials: "same-origin",
                        cache: "no-store"
                    });
                    const txt = await r.text();
                    let data = null;
                    try {
                        data = JSON.parse(txt);
                    } catch (_) {
                    }
                    if (!r.ok)
                        throw new Error((data && data.message) || ("HTTP " + r.status));
                    if (data && data.ok === false)
                        throw new Error(data.message || "Error");
                    return (data && data.data) ? data.data : data;
                }

                async function loadOverview() {
                    try {
                        const info = await fetchJSON(base + "/overview");
                        document.getElementById("cardTotalPacientes").textContent = fmt(info.totalPacientes, 0);
                        document.getElementById("cardCitasHoy").textContent = fmt(info.citasHoy, 0);
                        document.getElementById("cardConfirmadasHoy").textContent = fmt(info.confirmadasHoy, 0);
                        document.getElementById("cardAplicacionesHoy").textContent = fmt(info.aplicacionesHoy, 0);
                        document.getElementById("cardStockBajo").textContent = fmt(info.stockBajo, 0);
                    } catch (e) {
                        console.error(e);
                    }
                }

                function buildQuery(page, size) {
                    const p = [];
                    p.push("page=" + encodeURIComponent(page));
                    p.push("size=" + encodeURIComponent(size));
                    const q = document.getElementById("q").value.trim();
                    const vacuna = document.getElementById("vacunaId").value;
                    const desde = document.getElementById("desde").value;
                    const hasta = document.getElementById("hasta").value;
                    if (q)
                        p.push("q=" + encodeURIComponent(q));
                    if (vacuna)
                        p.push("vacunaId=" + encodeURIComponent(vacuna));
                    if (desde)
                        p.push("desde=" + encodeURIComponent(desde));
                    if (hasta)
                        p.push("hasta=" + encodeURIComponent(hasta));
                    return p.join("&");
                }

                async function loadLista(page, size) {
                    try {
                        const paged = await fetchJSON(base + "/aplicaciones?" + buildQuery(page, size));
                        const items = (paged && paged.items) ? paged.items : [];
                        const total = (paged && typeof paged.total === "number") ? paged.total : 0;
                        state.page = page;
                        state.size = size;
                        state.total = total;

                        const tbody = document.getElementById("tbodyAplicadas");
                        tbody.innerHTML = "";
                        for (let it of items) {
                            const tr = document.createElement("tr");
                            const ultima = it.ultima_aplicacion ? it.ultima_aplicacion.replace("T", " ") : "-";
                            tr.innerHTML =
                                    "<td>" + fmt(it.paciente, "-") + "</td>" +
                                    "<td>" + fmt(it.cedula, "-") + "</td>" +
                                    "<td>" + fmt(it.tutor, "-") + "</td>" +
                                    "<td class='text-end'>" + fmt(it.total_aplicadas, 0) + "</td>" +
                                    "<td>" + ultima + "</td>" +
                                    "<td><button type='button' class='btn btn-sm btn-outline-primary' data-id='" + fmt(it.id_paciente, "") + "' data-action='detalle'>Ver detalle</button></td>";
                            tbody.appendChild(tr);
                        }

                        const desdeIdx = (total === 0) ? 0 : ((page - 1) * size + 1);
                        const hastaIdx = Math.min(page * size, total);
                        document.getElementById("totalLbl").textContent = total;
                        document.getElementById("pageInfo").textContent =
                                (total === 0 ? "Sin resultados" : ("Mostrando " + desdeIdx + "–" + hastaIdx + " de " + total));
                        document.getElementById("prevBtn").disabled = (page <= 1);
                        document.getElementById("nextBtn").disabled = (page * size >= total);
                    } catch (e) {
                        console.error(e);
                        alert("No se pudo cargar la lista: " + e.message);
                    }
                }

                async function loadDetalle(idPaciente) {
                    try {
                        const detalle = await fetchJSON(base + "/aplicaciones/detalle?idPaciente=" + encodeURIComponent(idPaciente));
                        const tbody = document.getElementById("tbodyDetalle");
                        tbody.innerHTML = "";
                        for (let d of detalle) {
                            const f = d.fecha_aplicacion ? d.fecha_aplicacion.replace("T", " ") : "-";
                            const appId = d.id_aplicacion || d.idAplicacion; // tolera ambos
                            const tr = document.createElement("tr");
                            tr.innerHTML =
                                    "<td>" + fmt(f, "-") + "</td>" +
                                    "<td>" + fmt(d.vacuna, "-") + "</td>" +
                                    "<td>" + fmt(d.dosis_numero, 0) + "</td>" +
                                    "<td>" + fmt(d.centro, "-") + "</td>" +
                                    "<td>" + fmt(d.efectos, "-") + "</td>" +
                                    "<td class='text-end'>" +
                                    (appId
                                            ? "<button type='button' class='btn btn-sm btn-success' data-app='" + appId + "' data-action='cert'><i class='bi bi-patch-check'></i></button>"
                                            : "<button type='button' class='btn btn-sm btn-secondary' disabled title='Sin id_aplicacion'><i class='bi bi-exclamation-circle'></i></button>") +
                                    "</td>";
                            tbody.appendChild(tr);
                        }
                        bootstrap.Modal.getOrCreateInstance(document.getElementById("detalleModal")).show();
                    } catch (e) {
                        console.error(e);
                        alert("No se pudo cargar el detalle: " + e.message);
                    }
                }

                // ===== Descarga robusta y sin duplicados =====
                async function descargarCertificado(idAplicacion, btnEl) {
                    // anti-rebote: si es el mismo id en los últimos 3s, ignorar
                    const now = Date.now();
                    if (lastDownload.id === idAplicacion && (now - lastDownload.t) < 3000)
                        return;

                    if (downloading.has(idAplicacion))
                        return; // ya en curso
                    downloading.add(idAplicacion);
                    lastDownload = {id: idAplicacion, t: now};

                    // deshabilitar UI mientras descarga
                    if (btnEl) {
                        btnEl.disabled = true;
                        btnEl.setAttribute("aria-busy", "true");
                    }

                    try {
                        const url = CTX + "/api/certificados/vacunacion?id_aplicacion="
                                + encodeURIComponent(idAplicacion) + "&t=" + now;

                        const resp = await fetch(url, {
                            method: "GET",
                            credentials: "same-origin",
                            cache: "no-store",
                            headers: {"Accept": "application/pdf"}
                        });

                        if (!resp.ok)
                            throw new Error("HTTP " + resp.status + " " + (resp.statusText || ""));

                        const ct = (resp.headers.get("Content-Type") || "").toLowerCase();
                        if (!ct.includes("application/pdf")) {
                            // Posible HTML de login o error
                            const text = await resp.text();
                            alert("No se pudo descargar el certificado. ¿Sesión expirada o sin permiso?\n" +
                                    (text ? text.slice(0, 200) : "Respuesta no-PDF"));
                            return;
                        }

                        const blob = await resp.blob();

                        // Nombre desde Content-Disposition
                        const cd = resp.headers.get("Content-Disposition") || "";
                        let filename = "certificado_" + idAplicacion + ".pdf";
                        // Soporta filename*=UTF-8'' y filename="..."
                        const m = cd.match(/filename\*=UTF-8''([^;]+)|filename="?([^"]+)"?/i);
                        if (m)
                            filename = decodeURIComponent((m[1] || m[2] || "").trim()) || filename;

                        // Forzar descarga (sin redescargas en bucle)
                        const href = URL.createObjectURL(blob);
                        const a = document.createElement("a");
                        a.href = href;
                        a.download = filename;
                        a.rel = "noopener";
                        document.body.appendChild(a);
                        a.click();
                        a.remove();
                        // liberar URL una vez completado el ciclo del event loop
                        setTimeout(() => URL.revokeObjectURL(href), 0);
                    } catch (e) {
                        console.error(e);
                        alert("Error al descargar el certificado: " + e.message);
                    } finally {
                        downloading.delete(idAplicacion);
                        if (btnEl) {
                            btnEl.disabled = false;
                            btnEl.removeAttribute("aria-busy");
                        }
                    }
                }

                // Eventos
                document.getElementById("filtrosForm").addEventListener("submit", e => {
                    e.preventDefault();
                    loadLista(1, state.size);
                });
                document.getElementById("prevBtn").addEventListener("click", () => {
                    if (state.page > 1)
                        loadLista(state.page - 1, state.size);
                });
                document.getElementById("nextBtn").addEventListener("click", () => {
                    if (state.page * state.size < state.total)
                        loadLista(state.page + 1, state.size);
                });

                document.addEventListener("click", function (ev) {
                    const btnDetalle = ev.target.closest("[data-action='detalle']");
                    if (btnDetalle) {
                        const id = btnDetalle.getAttribute("data-id");
                        if (id)
                            loadDetalle(id);
                        return;
                    }
                    const btnCert = ev.target.closest("[data-action='cert']");
                    if (btnCert) {
                        const appId = btnCert.getAttribute("data-app");
                        if (appId)
                            descargarCertificado(appId, btnCert);
                        return;
                    }
                });

                // init
                loadOverview();
                loadLista(1, 10);
            })();
        </script>

    </body>
</html>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String ctx = request.getContextPath();
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>VacuKids · Iniciar sesión</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Solo Bootstrap (tu CSS lo enlazas después si quieres) -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=ctx%>/assets/css/login.css">
    </head>
    <body>

        <div class="container py-5">
            <div class="row justify-content-center">
                <div class="col-12 col-md-8 col-lg-5">

                    <div class="text-center mb-3">
                        <img src="<%=ctx%>/img/L1.png" alt="VacuKids" style="width:250px;height:250px;object-fit:contain;">
                        <p class="text-muted mb-0">Inicia sesión para continuar</p>
                    </div>

                    <!-- Mensajes del backend -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger">${error}</div>
                    </c:if>
                    <c:if test="${not empty ok}">
                        <div class="alert alert-success">${ok}</div>
                    </c:if>
                    <c:if test="${param.msg ne null}">
                        <div class="alert alert-info"><%= msg%></div>
                    </c:if>
                    <c:if test="${param.changed == '1'}">
                        <div class="alert alert-success">¡Contraseña actualizada! Ya puedes iniciar sesión.</div>
                    </c:if>
                    <c:if test="${not empty trace}">
                        <details class="mt-3">
                            <summary>Detalle técnico (debug)</summary>
                            <pre class="small bg-light border p-2 mt-2" style="max-height: 240px; overflow:auto;">
                                <c:forEach var="t" items="${trace}">${t}&#10;</c:forEach>
                                </pre>
                            </details>
                    </c:if>


                    <!-- Card de Login -->
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <form method="post" action="<%=ctx%>/login" novalidate>
                                <div class="mb-3">
                                    <label class="form-label">Usuario</label>
                                    <input type="email" name="usuario" class="form-control" placeholder="tucorreo@ejemplo.com" required>
                                    <div class="form-text">Para tutores, el usuario es su <strong>correo</strong>.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Contraseña</label>
                                    <div class="input-group">
                                        <input type="password" id="password" name="password" class="form-control" placeholder="Tu contraseña" required>
                                        <button class="btn btn-outline-secondary" type="button" onclick="togglePass('password', this)">👁</button>
                                    </div>
                                    <div class="form-text">
                                        Primer ingreso de tutor: usa tu <strong>cédula (10 dígitos)</strong>, luego cámbiala.
                                    </div>
                                </div>

                                <button class="btn btn-primary w-100" type="submit">Ingresar</button>

                                <div class="d-flex gap-2 justify-content-center mt-3">
                                    <a href="#" data-bs-toggle="modal" data-bs-target="#modalChange">Cambiar contraseña</a>
                                    <span class="text-muted">·</span>
                                    <a href="#" data-bs-toggle="modal" data-bs-target="#modalReset">¿Olvidaste tu contraseña?</a>
                                    <span class="text-muted">·</span>
                                </div>
                            </form>
                        </div>
                    </div>

                    <p class="text-center text-muted mt-3 mb-0">© VacuKids</p>
                </div>
            </div>
        </div>

        <!-- =============== Modal: Cambiar Contraseña (correo + actual + nueva) =============== -->
        <div class="modal fade" id="modalChange" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <form method="post" action="<%=ctx%>/cambiar-password">
                        <div class="modal-header">
                            <h5 class="modal-title">Cambiar contraseña</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <p class="mb-2">Ingresa tu correo y tu contraseña actual para actualizarla.</p>
                            <div class="mb-3">
                                <label class="form-label">Usuario (correo)</label>
                                <input type="email" class="form-control" name="usuario" placeholder="tucorreo@ejemplo.com" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Contraseña actual</label>
                                <input type="password" class="form-control" name="actual" required>
                            </div>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Nueva contraseña</label>
                                    <input type="password" class="form-control" name="nueva" minlength="8" required>
                                    <div class="form-text">Mínimo 8 caracteres.</div>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Repetir nueva</label>
                                    <input type="password" class="form-control" name="repetir" minlength="8" required>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">Cancelar</button>
                            <button class="btn btn-primary" type="submit">Actualizar</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- =============== Modal: Olvidé mi contraseña (envío de enlace) =============== -->
        <div class="modal fade" id="modalReset" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <form method="post" action="<%=ctx%>/enviar-reset">
                        <div class="modal-header">
                            <h5 class="modal-title">Restablecer contraseña</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body">
                            <p>Te enviaremos un enlace de restablecimiento a tu correo.</p>
                            <div class="mb-3">
                                <label class="form-label">Correo</label>
                                <input type="email" class="form-control" name="usuario" required>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">Cancelar</button>
                            <button class="btn btn-primary" type="submit">Enviar enlace</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- JS -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script>
                                            function togglePass(id, btn) {
                                                const el = document.getElementById(id);
                                                if (!el)
                                                    return;
                                                el.type = (el.type === 'password') ? 'text' : 'password';
                                                btn.textContent = (el.type === 'password') ? '👁' : '🙈';
                                            }
                                            document.querySelector("form[action$='/login']").addEventListener("submit", function (e) {
                                                e.preventDefault(); // Evita que el formulario se envíe al backend

                                                const usuario = this.usuario.value.trim();
                                                const pass = this.password.value.trim();

                                                // Validación específica para médico
                                                if (usuario === "medico2@vacukids.ec" && pass === "medico123") {
                                                    // Redirigir a la página del médico
                                                    window.location.href = "<%=ctx%>/Pages/medico.jsp";
                                                } else {
                                                    // En caso contrario, enviar al backend normal
                                                    this.submit();
                                                }
                                            });
        </script>
    </body>
</html>

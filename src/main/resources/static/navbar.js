const RUTA_PERMISO = {
    '/usuarios':         p => p.permisoUsuarios,
    '/permisos':         p => p.permisoRoles,
    '/productos':        p => p.permisoProductos,
    '/categorias':       p => p.permisoCategorias,
    '/sucursales':       p => p.permisoSucursales,
    '/stocks':           p => p.permisoStocks,
    '/almacen':          p => p.permisoStocks,
    '/traslados':        p => p.permisoTraslados,
    '/historial':        p => p.permisoHistorial,
    '/compras':          p => p.permisoComprasIngresar,
    '/registro_compras': p => p.permisoComprasRegistro,
    '/venta':            p => p.permisoVentasRealizar,
    '/registro_ventas':  p => p.permisoVentasRegistro,
    '/registro_cotizaciones':  p => p.permisoVentasRegistro,
    '/pedidos':          p => p.permisoVentasRegistro,
    '/clientes':         p => p.permisoClientes,
    '/reporte_ventas':   p => p.permisoVentasRegistro,
    '/clientes-web':     p => p.permisoClientes,
    '/gestion-web':      p => p.permisoWeb,
    '/gestion-cajas':    p => p.permisoCajasadmin,
    '/configuracion-almacen': p => p.permisoCajasadmin,
};
let _permisosGlobal = null;
function _tienePermiso(permisos, ruta) {
    const fn = RUTA_PERMISO[ruta];
    return fn ? fn(permisos) : true;
}
function _mostrarAlertaPermisos() {
    Swal.fire({
        icon: 'error',
        title: '¡Acceso Restringido!',
        text: 'No tienes los permisos necesarios para ingresar a este módulo.',
        confirmButtonColor: '#e31b23',
        backdrop: 'rgba(3,105,161,0.4)'
    });
}
function _ocultarEnlace(href) {
    const el = document.querySelector(`a[href="${href}"]`);
    if (el) el.closest('li').style.display = 'none';
}
function _ocultarDropdownSiVacio(selector) {
    const el = document.querySelector(selector);
    if (el) el.closest('.nav-item.dropdown').style.display = 'none';
}
function _aplicarPermisos(p) {
    if (!p.permisoCajasadmin)  _ocultarEnlace('/gestion-cajas');
    if (!p.permisoCajasadmin) {
        _ocultarEnlace('/gestion-cajas');
        _ocultarEnlace('/configuracion-almacen');   // <-- agregar esto
    }
    if (!p.permisoUsuarios)   _ocultarEnlace('/usuarios');
    if (!p.permisoRoles)      _ocultarEnlace('/permisos');
    if (!p.permisoProductos)  _ocultarEnlace('/productos');
    if (!p.permisoCategorias) _ocultarEnlace('/categorias');
    if (!p.permisoSucursales) _ocultarEnlace('/sucursales');
    if (!p.permisoClientes) {
        _ocultarEnlace('/clientes');
        _ocultarEnlace('/clientes-web');
    }
    if (!p.permisoWeb) _ocultarEnlace('/gestion-web');
    if (!p.permisoUsuarios && !p.permisoRoles && !p.permisoProductos
        && !p.permisoCategorias && !p.permisoSucursales
        && !p.permisoCajasadmin
        && !p.permisoClientes && !p.permisoWeb) {
        _ocultarDropdownSiVacio('a[href="/usuarios"]');
    }
    if (!p.permisoStocks) {
        _ocultarEnlace('/stocks');
        _ocultarEnlace('/almacen');
    }
    if (!p.permisoTraslados) _ocultarEnlace('/traslados');
    if (!p.permisoHistorial) _ocultarEnlace('/historial');
    if (!p.permisoComprasIngresar) _ocultarEnlace('/compras');
    if (!p.permisoComprasRegistro) _ocultarEnlace('/registro_compras');
    if (!p.permisoComprasIngresar && !p.permisoComprasRegistro) {
        _ocultarDropdownSiVacio('a[href="/compras"]');
    }
    if (!p.permisoVentasRealizar) _ocultarEnlace('/venta');
    if (!p.permisoVentasRegistro) _ocultarEnlace('/registro_ventas');
    if (!p.permisoVentasRegistro) _ocultarEnlace('/registro_cotizaciones');
    if (!p.permisoVentasRegistro) _ocultarEnlace('/reporte_ventas');
    if (!p.permisoVentasRealizar && !p.permisoVentasRegistro) {
        _ocultarDropdownSiVacio('a[href="/venta"]');
    }
}
function _marcarActivo() {
    const path = window.location.pathname.replace(/\/$/, '') || '/';
    document.querySelectorAll('.dropdown-menu a.dropdown-item').forEach(a => {
        try {
            const linkPath = a.getAttribute('href');
            if (!linkPath || linkPath === '#') return;
            if (linkPath === path) {
                a.classList.add('active');
                const dropdown = a.closest('.nav-item.dropdown');
                if (dropdown) {
                    const toggle = dropdown.querySelector('.nav-link.dropdown-toggle');
                    if (toggle && !dropdown.querySelector('#textoNombreNav')) {
                        toggle.classList.add('active');
                    }
                }
            }
        } catch (_) {}
    });
    document.querySelectorAll('.navbar-nav > li > a.nav-link:not(.dropdown-toggle)').forEach(a => {
        try {
            const linkPath = a.getAttribute('href');
            if (linkPath && linkPath === path) a.classList.add('active');
        } catch (_) {}
    });
}
async function _cargarNavbar() {
    const container = document.getElementById('navbar-container');
    if (!container) return;
    try {
        const res = await fetch('/navbar.html');
        container.innerHTML = await res.text();
        container.querySelectorAll('[data-bs-toggle="dropdown"]').forEach(el => {
            new bootstrap.Dropdown(el);
        });
    } catch (e) {
        console.error('Error cargando navbar:', e);
    }
}
async function initNavbar(callback) {
    await _cargarNavbar();
    try {
        const res = await fetch('/api/auth/me');
        if (!res.ok) { window.location.href = '/login'; return; }
        const data = await res.json();
        const p = data.permisos || {};
        _permisosGlobal = p;
        const path = window.location.pathname.replace(/\/$/, '');
        if (RUTA_PERMISO[path] && !_tienePermiso(p, path)) {
            window.location.href = '/inicio?error=permisos';
            return;
        }
        const params = new URLSearchParams(window.location.search);
        if (params.get('error') === 'permisos') {
            _mostrarAlertaPermisos();
            window.history.replaceState(null, '', window.location.pathname);
        }
        const elNombre = document.getElementById('textoNombreNav');
        if (elNombre) elNombre.textContent = data.nombre.split(' ')[0];
        if (data.foto) {
            const icon = document.getElementById('iconPerfilNav');
            const img  = document.getElementById('imgPerfilNav');
            if (icon) icon.style.display = 'none';
            if (img)  { img.src = '/uploads/' + data.foto; img.style.display = 'block'; }
        }
        _aplicarPermisos(p);
        _marcarActivo();
        document.addEventListener('click', function(e) {
            const a = e.target.closest('a');
            if (!a || !a.href || !_permisosGlobal) return;
            try {
                const linkPath = new URL(a.href).pathname.replace(/\/$/, '');
                if (RUTA_PERMISO[linkPath] && !_tienePermiso(_permisosGlobal, linkPath)) {
                    e.preventDefault();
                    _mostrarAlertaPermisos();
                }
            } catch (_) {}
        });
        if (typeof callback === 'function') callback(data);
    } catch (e) {
        window.location.href = '/login';
    }
}
async function cerrarSesion() {
    try { await fetch('/api/auth/logout', { method: 'POST' }); } catch (_) {}
    window.location.href = '/login';
}
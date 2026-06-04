let carrito = JSON.parse(localStorage.getItem('tato_carrito')) || [];
let inventarioGlobal = [];

document.addEventListener("DOMContentLoaded", () => {
    cargarConfiguracionWeb();
    verificarSesionCliente();
    cargarCatalogo();
    actualizarInterfazCarrito();

    document.getElementById('btnProcederPago').addEventListener('click', () => {
        const nombreCliente = localStorage.getItem('tato_cliente_nombre');
        if (!nombreCliente) {
            sessionStorage.setItem('tato_login_redirect', '/checkout');
            window.location.href = '/login-cliente';
            return;
        }
        window.location.href = '/checkout';
    });
});

function verificarSesionCliente() {
    const nombreCliente = localStorage.getItem('tato_cliente_nombre');
    const authContainer = document.getElementById('authClienteContainer');

    if (nombreCliente && authContainer) {
        const primerNombre = nombreCliente.split(' ')[0];

        authContainer.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-tato-red btn-sm fw-bold px-3 dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-person-circle me-1"></i> Hola, ${primerNombre}
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2">
                    <li><a class="dropdown-item fw-bold text-dark" href="/mis-pedidos"><i class="bi bi-bag-check me-2"></i>Mis Pedidos</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" onclick="cerrarSesionCliente(); return false;">
                        <i class="bi bi-box-arrow-right me-2"></i>Cerrar Sesión
                    </a></li>
                 </ul>
            </div>
        `;
    } else if (authContainer) {
        authContainer.innerHTML = '<a href="/login-cliente" class="btn btn-tato-red btn-sm fw-bold px-3">Ingresar</a>';
    }
}

async function cerrarSesionCliente() {
    try {
        await fetch('/api/clientes-web/logout', { method: 'POST' });
    } catch (e) {
        console.error(e);
    }
    localStorage.removeItem('tato_cliente_nombre');
    window.location.href = '/tienda';
}

function cargarConfiguracionWeb() {
    const config = JSON.parse(localStorage.getItem('tato_web_config')) || {
        titulo: 'Los Mejores Repuestos para tu Moto',
        subtitulo: 'Calidad, garantía y velocidad en Motorepuestos Tato',
        colorPrimario: '#e31b23', colorSecundario: '#212529',
        whatsapp: '', facebook: '', instagram: ''
    };

    document.documentElement.style.setProperty('--tato-primary', config.colorPrimario);
    document.documentElement.style.setProperty('--tato-secondary', config.colorSecundario);

    document.getElementById('frontTitulo').innerText = config.titulo;
    document.getElementById('frontSubtitulo').innerText = config.subtitulo;

    if(config.facebook) {
        let btn = document.getElementById('btnFrontFb');
        btn.href = config.facebook; btn.classList.remove('d-none');
    }
    if(config.instagram) {
        let btn = document.getElementById('btnFrontIg');
        btn.href = config.instagram; btn.classList.remove('d-none');
    }
    if(config.whatsapp) {
        let btn = document.getElementById('btnFrontWa');
        btn.href = `https://wa.me/${config.whatsapp}`; btn.classList.remove('d-none');
    }
}

async function cargarCatalogo() {
    try {
        const response = await fetch('/api/productos/activos');
        if (!response.ok) throw new Error("Error en el servidor");
        inventarioGlobal = await response.json();
        poblarFiltroCategorias();
        aplicarFiltros();
    } catch (error) {
        document.getElementById('contenedorProductos').innerHTML = '<div class="alert alert-danger w-100 text-center">Error al cargar el catálogo.</div>';
    }
}

function poblarFiltroCategorias() {
    const selectCat = document.getElementById('filtroCategoria');
    const categoriasUnicas = new Set();
    inventarioGlobal.forEach(item => {
        if (item.producto?.categoria?.nombre) categoriasUnicas.add(item.producto.categoria.nombre);
    });
    categoriasUnicas.forEach(cat => { selectCat.innerHTML += `<option value="${cat}">${cat}</option>`; });
}

function aplicarFiltros() {
    const textoBuscar = document.getElementById('filtroNombre').value.toLowerCase();
    const categoriaSel = document.getElementById('filtroCategoria').value;
    const ordenSel = document.getElementById('filtroOrden').value;

    let filtrados = inventarioGlobal.filter(item => {
        if (!item || !item.producto || !item.activo) return false;
        const prod = item.producto;
        const coincideNombre = (prod.nombre || "").toLowerCase().includes(textoBuscar);
        const catNombre = prod.categoria ? prod.categoria.nombre : "General";
        const coincideCat = (categoriaSel === "TODAS" || catNombre === categoriaSel);
        return coincideNombre && coincideCat;
    });

    const getPrecioLimpio = (item) => {
        if (item.precioVenta) return parseFloat(item.precioVenta.toString().replace(',', '.')) || 0;
        return 0;
    };

    filtrados.sort((a, b) => {
        let precioA = getPrecioLimpio(a);
        let precioB = getPrecioLimpio(b);
        if (ordenSel === "MENOR_PRECIO") return precioA - precioB;
        if (ordenSel === "MAYOR_PRECIO") return precioB - precioA;
        return 0;
    });

    const esFiltroPorDefecto = (textoBuscar === "" && categoriaSel === "TODAS" && ordenSel === "RELEVANTE");
    if (esFiltroPorDefecto) {
        filtrados = filtrados.slice(0, 16);
    }

    renderizarProductos(filtrados);
}

function renderizarProductos(lista) {
    const contenedor = document.getElementById('contenedorProductos');
    contenedor.innerHTML = '';
    if (lista.length === 0) {
        contenedor.innerHTML = '<div class="col-12 text-center text-muted py-5"><i class="bi bi-search fs-1 mb-3 d-block"></i>No hay repuestos.</div>';
        return;
    }

    lista.forEach(item => {
        const prod = item.producto;
        let precioNumeric = parseFloat(item.precioVenta?.toString().replace(',', '.') || 0) || 0;
        const imagenRuta = prod.imagen ? `/uploads/${prod.imagen}` : 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300';

        const hayStock = item.stock > 0;
        const btnClase = hayStock ? 'btn-outline-dark' : 'btn-danger disabled';
        const btnIcono = hayStock ? '<i class="bi bi-cart-plus me-1"></i> Al Carrito' : '<i class="bi bi-x-circle me-1"></i> Agotado';
        const btnAccion = hayStock ? `onclick="agregarAlCarrito(${prod.id}, '${prod.nombre.replace(/'/g, "\\'")}', ${precioNumeric}, ${item.stock})"` : '';

        contenedor.innerHTML += `
            <div class="col-12 col-sm-6 col-md-4 col-lg-3">
                <div class="card h-100 bg-white card-producto ${!hayStock ? 'opacity-75' : ''}">
                    <div class="p-2 d-flex align-items-center justify-content-center position-relative" style="height: 200px; background-color: #f8f9fa;">
                        ${!hayStock ? '<div class="position-absolute top-50 start-50 translate-middle bg-danger text-white px-3 py-1 fw-bold rounded shadow" style="z-index: 2; transform: translate(-50%, -50%) rotate(-10deg) !important;">AGOTADO</div>' : ''}
                        <img src="${imagenRuta}" onerror="this.src='https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300'" class="img-fluid" alt="${prod.nombre}" style="max-height: 100%; object-fit: contain; ${!hayStock ? 'filter: grayscale(100%);' : ''}">
                    </div>
                    <div class="card-body d-flex flex-column border-top">
                        <div class="d-flex justify-content-between">
                            <span class="badge bg-dark mb-2">${prod.categoria?.nombre || 'General'}</span>
                            <span class="badge ${hayStock ? 'bg-success' : 'bg-danger'} mb-2">Stock: ${item.stock}</span>
                        </div>
                        <h6 class="card-title fw-bold text-dark">${prod.nombre || 'Sin nombre'}</h6>
                        <p class="card-text text-tato-red fw-bold fs-4 mb-3 mt-auto">S/ ${precioNumeric.toFixed(2)}</p>
                        <button class="btn w-100 fw-bold ${btnClase}" ${btnAccion}>${btnIcono}</button>
                    </div>
                </div>
            </div>`;
    });
}

function agregarAlCarrito(id, nombre, precio, stockMaximo) {
    const itemExistente = carrito.find(i => i.id === id);
    if (itemExistente) {
        if (itemExistente.cantidad >= stockMaximo) {
            Swal.fire({ icon: 'warning', title: 'Stock Límite', text: `Solo hay ${stockMaximo} unidades disponibles.`});
            return;
        }
        itemExistente.cantidad++;
    } else {
        carrito.push({ id, nombre, precio, cantidad: 1 });
    }

    guardarCarrito();
    actualizarInterfazCarrito();

    Swal.fire({
        toast: true,
        position: 'bottom-end',
        icon: 'success',
        title: 'Agregado al carrito',
        showConfirmButton: false,
        timer: 1500,
        timerProgressBar: true
    });
}

function actualizarInterfazCarrito() {
    const cont = document.getElementById('itemsCarrito');
    const badge = document.getElementById('contadorCarrito');
    const totalSpan = document.getElementById('totalCarrito');
    const btnPago = document.getElementById('btnProcederPago');
    cont.innerHTML = ''; let total = 0; let cant = 0;

    if (carrito.length === 0) {
        cont.innerHTML = '<div class="text-center text-muted mt-5"><i class="bi bi-cart-x fs-1 d-block mb-3"></i>Tu carrito está vacío.</div>';
        badge.innerText = '0'; totalSpan.innerText = 'S/ 0.00'; btnPago.disabled = true; return;
    }
    btnPago.disabled = false;
    carrito.forEach((item, index) => {
        const subt = item.precio * item.cantidad;
        total += subt; cant += item.cantidad;
        cont.innerHTML += `
            <div class="d-flex justify-content-between align-items-center mb-2 p-2 border rounded bg-white shadow-sm">
                <div>
                    <h6 class="mb-0 text-dark fw-bold" style="font-size: 0.85rem;">${item.nombre}</h6>
                    <small class="text-muted">${item.cantidad} x S/ ${item.precio.toFixed(2)}</small>
                </div>
                <div class="d-flex align-items-center">
                    <span class="fw-bold text-tato-red me-2">S/ ${subt.toFixed(2)}</span>
                    <button class="btn btn-sm btn-light text-danger border-0" onclick="eliminarDelCarrito(${index})"><i class="bi bi-trash"></i></button>
                </div>
            </div>`;
    });
    badge.innerText = cant; totalSpan.innerText = `S/ ${total.toFixed(2)}`;
}

function eliminarDelCarrito(index) { carrito.splice(index, 1); guardarCarrito(); actualizarInterfazCarrito(); }
function guardarCarrito() { localStorage.setItem('tato_carrito', JSON.stringify(carrito)); }
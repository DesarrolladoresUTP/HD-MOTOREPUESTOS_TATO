let carritoCheckout = [];
let totalGeneral = 0;

document.addEventListener("DOMContentLoaded", () => {
    cargarCarrito();
});

function cargarCarrito() {
    carritoCheckout = JSON.parse(localStorage.getItem('tato_carrito')) || [];
    const contenedor = document.getElementById('listaProductosCheckout');
    const totalSpan = document.getElementById('totalCheckout');
    const btn = document.getElementById('btnFinalizarPedido');

    if (carritoCheckout.length === 0) {
        contenedor.innerHTML = '<div class="alert alert-warning text-center fw-bold"><i class="bi bi-exclamation-triangle me-2"></i>Tu carrito está vacío.</div>';
        btn.disabled = true;
        totalSpan.innerText = 'S/ 0.00';
        return;
    }

    btn.disabled = false;
    contenedor.innerHTML = '';
    totalGeneral = 0;

    carritoCheckout.forEach(item => {
        const subtotal = item.precio * item.cantidad;
        totalGeneral += subtotal;
        contenedor.innerHTML += `
            <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                <div>
                    <h6 class="mb-0 fw-bold" style="color: var(--tato-secondary); font-size:0.95rem;">${item.nombre}</h6>
                    <small class="text-muted">Cant: ${item.cantidad} | P.U: S/ ${item.precio.toFixed(2)}</small>
                </div>
                <span class="fw-bold text-dark">S/ ${subtotal.toFixed(2)}</span>
            </div>
        `;
    });

    totalSpan.innerText = `S/ ${totalGeneral.toFixed(2)}`;
}

function ajustarFormularioDoc() {
    const tipo = document.getElementById('tipoDocumento').value;
    const docInput = document.getElementById('numDocumento');

    if (tipo === 'DNI') {
        document.getElementById('lblDocumento').innerText = 'Número de DNI';
        document.getElementById('lblNombreCompleto').innerText = 'Nombres y Apellidos';
        docInput.maxLength = 8;
    } else {
        document.getElementById('lblDocumento').innerText = 'Número de RUC';
        document.getElementById('lblNombreCompleto').innerText = 'Razón Social';
        docInput.maxLength = 11;
    }
    docInput.value = '';
    document.getElementById('nombreResultado').value = '';
}

function toggleDireccion() {
    const isDelivery = document.getElementById('entregaDelivery').checked;
    const container = document.getElementById('containerDireccion');
    if (isDelivery) {
        container.style.display = 'block';
    } else {
        container.style.display = 'none';
        document.getElementById('direccionCliente').value = '';
    }
}

async function consultarDocumentoPeruanos() {
    const tipo = document.getElementById('tipoDocumento').value;
    const numero = document.getElementById('numDocumento').value.trim();
    const inputNombre = document.getElementById('nombreResultado');
    const btn = document.getElementById('btnValidarDoc');

    if (!numero || (tipo === 'DNI' && numero.length !== 8) || (tipo === 'RUC' && numero.length !== 11)) {
        Swal.fire({ icon: 'warning', title: 'Documento Inválido', text: `Ingresa un ${tipo} válido.` });
        return;
    }

    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
    btn.disabled = true;
    inputNombre.value = 'Buscando...';

    try {
        const res = await fetch(`/api/clientes/buscar-api?tipo=${tipo}&numero=${numero}`);
        if (res.ok) {
            const data = await res.json();
            inputNombre.value = data.nombre;
            inputNombre.classList.remove('bg-light');
            inputNombre.classList.add('bg-success-subtle');
        } else {
            throw new Error("No encontrado");
        }
    } catch (error) {
        inputNombre.value = '';
        inputNombre.classList.add('bg-light');
        inputNombre.classList.remove('bg-success-subtle');
        Swal.fire({ icon: 'error', title: 'Oops...', text: 'No se encontraron datos. Puedes ingresarlo manualmente si lo deseas.' });
        inputNombre.readOnly = false;
    } finally {
        btn.innerHTML = '<i class="bi bi-search"></i>';
        btn.disabled = false;
    }
}

async function procesarPedidoFinal() {
    const tipoDoc = document.getElementById('tipoDocumento').value;
    const numDoc = document.getElementById('numDocumento').value.trim();
    const nombre = document.getElementById('nombreResultado').value.trim();
    const telefono = document.getElementById('telefonoCliente').value.trim();
    const metodoEntrega = document.querySelector('input[name="metodoEntrega"]:checked').value;
    const direccion = document.getElementById('direccionCliente').value.trim();

    if (!numDoc || !nombre || !telefono) {
        Swal.fire({ icon: 'warning', title: 'Campos Incompletos', text: 'El documento, nombre y teléfono son obligatorios.' });
        return;
    }
    if (metodoEntrega === 'DELIVERY' && !direccion) {
        Swal.fire({ icon: 'warning', title: 'Dirección Requerida', text: 'Has seleccionado Delivery, necesitamos tu dirección de entrega.' });
        return;
    }

    const btn = document.getElementById('btnFinalizarPedido');
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando Orden...';
    btn.disabled = true;

    const payload = {
        tipoDocumento: tipoDoc,
        numeroDocumento: numDoc,
        nombreCompleto: nombre,
        telefono: telefono,
        metodoEntrega: metodoEntrega,
        direccionEntrega: direccion,
        carrito: carritoCheckout.map(item => ({
            id: item.id,
            nombre: item.nombre,
            precio: parseFloat(item.precio),
            cantidad: parseInt(item.cantidad)
        }))
    };

    try {
        const response = await fetch('/api/pedidos-web/procesar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const data = await response.json();

            localStorage.removeItem('tato_carrito');

            Swal.fire({
                icon: 'success',
                title: '¡Pedido Confirmado!',
                html: `Tu número de orden es: <b>#000${data.id}</b><br>Nos comunicaremos contigo pronto.`,
                confirmButtonColor: '#e31b23',
                allowOutsideClick: false
            }).then(() => {
                window.location.href = '/tienda.html';
            });

        } else {
            const err = await response.text();
            Swal.fire('Error al procesar', err, 'error');
            btn.innerHTML = '<i class="bi bi-shield-lock fs-5 me-2"></i> Confirmar Pedido Seguro';
            btn.disabled = false;
        }
    } catch (error) {
        Swal.fire('Error de Conexión', 'No pudimos comunicarnos con el servidor.', 'error');
        btn.innerHTML = '<i class="bi bi-shield-lock fs-5 me-2"></i> Confirmar Pedido Seguro';
        btn.disabled = false;
    }
}
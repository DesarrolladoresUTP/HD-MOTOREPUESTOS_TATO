document.addEventListener("DOMContentLoaded", async () => {
    await initNavbar();

    cargarDatosDashboard();
});

async function cargarDatosDashboard() {
    try {
        const response = await fetch('/api/dashboard/resumen');
        if (!response.ok) throw new Error("No se pudo cargar la información.");

        const data = await response.json();

        document.getElementById('kpiIngresos').innerText = `S/ ${data.ingresosWeb.toFixed(2)}`;
        document.getElementById('kpiPedidos').innerText = data.pedidosPendientes;
        document.getElementById('kpiBajoStock').innerText = data.bajoStockCount;
        document.getElementById('kpiProductos').innerText = data.totalProductos;

        llenarTablaStock(data.tablaBajoStock);

        renderizarGraficoTopVendidos(data.graficoTopVendidos);
        renderizarGraficoPedidos(data.graficoPedidos);

    } catch (error) {
        console.error(error);
        Swal.fire({
            toast: true, position: 'top-end', icon: 'error',
            title: 'Error al cargar métricas', showConfirmButton: false, timer: 3000
        });
    }
}

function llenarTablaStock(listaBajoStock) {
    const tbody = document.getElementById('tablaAlertasBody');
    tbody.innerHTML = '';

    if (!listaBajoStock || listaBajoStock.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-3">No hay alertas de stock. ¡Todo está en orden!</td></tr>';
        return;
    }

    listaBajoStock.forEach(item => {
        tbody.innerHTML += `
            <tr>
                <td class="fw-bold text-dark">${item.nombre}</td>
                <td class="text-center"><span class="badge bg-danger fs-6">${item.stock}</span></td>
                <td class="text-center text-muted">${item.minimo}</td>
                <td class="text-center"><i class="bi bi-arrow-down-circle-fill text-danger fs-5"></i></td>
            </tr>
        `;
    });
}

function renderizarGraficoTopVendidos(dataTop) {
    const canvas = document.getElementById('chartTopVendidos');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const labels = Object.keys(dataTop || {});
    const valores = Object.values(dataTop || {});

    if(labels.length === 0) { labels.push('Aún no hay ventas'); valores.push(1); }

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: valores,
                backgroundColor: ['#e31b23', '#f97316', '#f59e0b', '#0ea5e9', '#64748b'],
                borderWidth: 0, hoverOffset: 4
            }]
        },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, padding: 15, font: {size: 10} } } }
        }
    });
}

function renderizarGraficoPedidos(dataPedidos) {
    const canvas = document.getElementById('chartPedidos');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const estados = ['PENDIENTE', 'EN_PREPARACION', 'LISTO_RECOJO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];
    const valores = estados.map(estado => (dataPedidos && dataPedidos[estado]) ? dataPedidos[estado] : 0);

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Pendiente', 'En Preparación', 'Listo', 'Enviado', 'Entregado', 'Cancelado'],
            datasets: [{
                label: 'Cantidad de Órdenes',
                data: valores,
                backgroundColor: [
                    '#f59e0b',
                    '#0ea5e9',
                    '#3b82f6',
                    '#64748b',
                    '#10b981',
                    '#ef4444'
                ],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 } },
                x: { grid: { display: false } }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}
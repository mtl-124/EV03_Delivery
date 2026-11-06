const API = {
    clientes: '/api/clientes',
    repartidores: '/api/repartidores',
    entregas: '/api/entregas'
};

const ETIQUETAS_ESTADO = {
    clientes: 'Clientes',
    repartidores: 'Repartidores',
    entregas: 'Entregas'
};

let datosCreados = false;

function interpretar(texto) {
    if (!texto) {
        return null;
    }
    try {
        return JSON.parse(texto);
    } catch (error) {
        return texto;
    }
}

function formatear(datos) {
    if (datos === null || datos === undefined) {
        return '(sin cuerpo)';
    }
    if (typeof datos === 'string') {
        return datos;
    }
    return JSON.stringify(datos, null, 2);
}

function registrar(metodo, ruta, estado, cuerpo) {
    const contenedor = document.getElementById('registro');
    const item = document.createElement('div');
    item.className = 'registro-item';
    const hora = new Date().toLocaleTimeString('es-PE', { hour12: false });
    const encabezado = document.createElement('div');
    encabezado.textContent = '[' + hora + '] ' + metodo + ' ' + ruta + ' -> ' + estado;
    item.appendChild(encabezado);
    const respuesta = document.createElement('div');
    respuesta.textContent = 'Respuesta: ' + (cuerpo || '(sin cuerpo)');
    item.appendChild(respuesta);
    contenedor.appendChild(item);
    while (contenedor.children.length > 40) {
        contenedor.removeChild(contenedor.firstChild);
    }
    contenedor.scrollTop = contenedor.scrollHeight;
}

async function peticion(metodo, ruta, cuerpo) {
    const configuracion = { method: metodo, headers: {} };
    if (cuerpo !== undefined) {
        configuracion.headers['Content-Type'] = 'application/json';
        configuracion.body = JSON.stringify(cuerpo);
    }
    let respuesta;
    let texto = '';
    try {
        respuesta = await fetch(ruta, configuracion);
        texto = await respuesta.text();
    } catch (error) {
        const datos = { message: 'No se pudo conectar con el servidor' };
        registrar(metodo, ruta, 0, JSON.stringify(datos));
        return { estado: 0, ok: false, datos };
    }
    const datos = interpretar(texto);
    registrar(metodo, ruta, respuesta.status, texto);
    return { estado: respuesta.status, ok: respuesta.ok, datos };
}

async function solicitar(metodo, ruta, cuerpo) {
    const resultado = await peticion(metodo, ruta, cuerpo);
    if (!resultado.ok) {
        const mensaje = resultado.datos && typeof resultado.datos === 'object' && resultado.datos.message
            ? resultado.datos.message
            : 'Error HTTP ' + resultado.estado;
        const error = new Error(mensaje);
        error.estado = resultado.estado;
        error.datos = resultado.datos;
        throw error;
    }
    return resultado.datos;
}

function textoError(error) {
    if (error.datos && typeof error.datos === 'object' && error.datos.message) {
        if (Array.isArray(error.datos.errors) && error.datos.errors.length > 0) {
            const detalles = error.datos.errors
                .map(detalle => detalle.campo + ': ' + detalle.mensaje)
                .join('; ');
            return error.datos.message + ' (' + detalles + ')';
        }
        return error.datos.message;
    }
    return error.message;
}

function mostrarMensaje(id, texto, esError) {
    const elemento = document.getElementById(id);
    elemento.textContent = texto;
    elemento.classList.toggle('error', Boolean(esError));
}

function marcarEstado(clave, disponible) {
    const elemento = document.getElementById('estado-' + clave);
    elemento.textContent = ETIQUETAS_ESTADO[clave] + ': ' + (disponible ? 'disponible' : 'no disponible');
    elemento.classList.remove('pendiente');
    elemento.classList.toggle('disponible', disponible);
    elemento.classList.toggle('no-disponible', !disponible);
}

function celda(texto) {
    const elemento = document.createElement('td');
    elemento.textContent = texto;
    return elemento;
}

function botonAccion(texto, clase, accion) {
    const elemento = document.createElement('button');
    elemento.type = 'button';
    elemento.textContent = texto;
    if (clase) {
        elemento.className = clase;
    }
    elemento.addEventListener('click', accion);
    return elemento;
}

function llenarSelect(id, elementos, etiqueta) {
    const select = document.getElementById(id);
    const anterior = select.value;
    select.replaceChildren();
    const inicial = document.createElement('option');
    inicial.value = '';
    inicial.textContent = elementos.length > 0 ? 'Seleccione' : 'Sin registros';
    select.appendChild(inicial);
    elementos.forEach(elemento => {
        const opcion = document.createElement('option');
        opcion.value = elemento.id;
        opcion.textContent = etiqueta(elemento);
        select.appendChild(opcion);
    });
    if (elementos.some(elemento => String(elemento.id) === anterior)) {
        select.value = anterior;
    }
}

function configurarTabs() {
    const botones = document.querySelectorAll('.tab');
    botones.forEach(boton => {
        boton.addEventListener('click', () => {
            botones.forEach(otro => otro.classList.toggle('activo', otro === boton));
            document.querySelectorAll('main > .panel').forEach(panel => {
                panel.classList.toggle('activo', panel.id === 'tab-' + boton.dataset.tab);
            });
            if (boton.dataset.tab === 'clientes') {
                cargarClientes();
            }
            if (boton.dataset.tab === 'repartidores') {
                cargarRepartidores();
            }
            if (boton.dataset.tab === 'entregas') {
                cargarEntregas();
            }
        });
    });
}

async function cargarClientes() {
    try {
        const clientes = await solicitar('GET', API.clientes);
        renderClientes(clientes);
        llenarSelect('entrega-cliente', clientes, cliente => cliente.id + ' - ' + cliente.nombre + ' ' + cliente.apellido);
        marcarEstado('clientes', true);
    } catch (error) {
        marcarEstado('clientes', false);
        mostrarMensaje('mensaje-clientes', 'No se pudo cargar clientes: ' + textoError(error), true);
    }
}

function renderClientes(clientes) {
    const cuerpo = document.getElementById('tabla-clientes');
    cuerpo.replaceChildren();
    clientes.forEach(cliente => {
        const fila = document.createElement('tr');
        fila.appendChild(celda(cliente.id));
        fila.appendChild(celda(cliente.nombre));
        fila.appendChild(celda(cliente.apellido));
        fila.appendChild(celda(cliente.email));
        fila.appendChild(celda(cliente.telefono));
        fila.appendChild(celda(cliente.direccion));
        const acciones = document.createElement('td');
        acciones.appendChild(botonAccion('Editar', null, () => editarCliente(cliente)));
        acciones.appendChild(botonAccion('Eliminar', 'peligro', () => eliminarCliente(cliente)));
        fila.appendChild(acciones);
        cuerpo.appendChild(fila);
    });
}

function limpiarCliente() {
    document.getElementById('form-cliente').reset();
    document.getElementById('cliente-id').value = '';
    mostrarMensaje('mensaje-clientes', '');
}

function editarCliente(cliente) {
    document.getElementById('cliente-id').value = cliente.id;
    document.getElementById('cliente-nombre').value = cliente.nombre;
    document.getElementById('cliente-apellido').value = cliente.apellido;
    document.getElementById('cliente-email').value = cliente.email;
    document.getElementById('cliente-telefono').value = cliente.telefono;
    document.getElementById('cliente-direccion').value = cliente.direccion;
    mostrarMensaje('mensaje-clientes', 'Editando cliente ' + cliente.id);
}

async function guardarCliente(evento) {
    evento.preventDefault();
    const id = document.getElementById('cliente-id').value;
    const datos = {
        nombre: document.getElementById('cliente-nombre').value.trim(),
        apellido: document.getElementById('cliente-apellido').value.trim(),
        email: document.getElementById('cliente-email').value.trim(),
        telefono: document.getElementById('cliente-telefono').value.trim(),
        direccion: document.getElementById('cliente-direccion').value.trim()
    };
    try {
        let mensaje;
        if (id) {
            const actualizado = await solicitar('PUT', API.clientes + '/' + id, datos);
            mensaje = 'Cliente ' + actualizado.id + ' actualizado';
        } else {
            const creado = await solicitar('POST', API.clientes, datos);
            mensaje = 'Cliente creado con id ' + creado.id;
        }
        limpiarCliente();
        mostrarMensaje('mensaje-clientes', mensaje);
        await cargarClientes();
    } catch (error) {
        mostrarMensaje('mensaje-clientes', textoError(error), true);
    }
}

async function eliminarCliente(cliente) {
    if (!window.confirm('Eliminar el cliente ' + cliente.id + '?')) {
        return;
    }
    try {
        await solicitar('DELETE', API.clientes + '/' + cliente.id);
        limpiarCliente();
        mostrarMensaje('mensaje-clientes', 'Cliente ' + cliente.id + ' eliminado');
        await cargarClientes();
    } catch (error) {
        mostrarMensaje('mensaje-clientes', textoError(error), true);
    }
}

function configurarClientes() {
    document.getElementById('form-cliente').addEventListener('submit', guardarCliente);
    document.getElementById('cliente-limpiar').addEventListener('click', limpiarCliente);
}

async function cargarRepartidores() {
    try {
        const repartidores = await solicitar('GET', API.repartidores);
        renderRepartidores(repartidores);
        llenarSelect('entrega-repartidor', repartidores, repartidor => repartidor.id + ' - ' + repartidor.nombre + ' ' + repartidor.apellido);
        marcarEstado('repartidores', true);
    } catch (error) {
        marcarEstado('repartidores', false);
        mostrarMensaje('mensaje-repartidores', 'No se pudo cargar repartidores: ' + textoError(error), true);
    }
}

function renderRepartidores(repartidores) {
    const cuerpo = document.getElementById('tabla-repartidores');
    cuerpo.replaceChildren();
    repartidores.forEach(repartidor => {
        const fila = document.createElement('tr');
        fila.appendChild(celda(repartidor.id));
        fila.appendChild(celda(repartidor.nombre));
        fila.appendChild(celda(repartidor.apellido));
        fila.appendChild(celda(repartidor.telefono));
        fila.appendChild(celda(repartidor.vehiculo));
        fila.appendChild(celda(repartidor.activo ? 'Sí' : 'No'));
        const acciones = document.createElement('td');
        acciones.appendChild(botonAccion('Editar', null, () => editarRepartidor(repartidor)));
        acciones.appendChild(botonAccion(repartidor.activo ? 'Desactivar' : 'Activar', 'secundario', () => alternarRepartidor(repartidor)));
        acciones.appendChild(botonAccion('Eliminar', 'peligro', () => eliminarRepartidor(repartidor)));
        fila.appendChild(acciones);
        cuerpo.appendChild(fila);
    });
}

function limpiarRepartidor() {
    document.getElementById('form-repartidor').reset();
    document.getElementById('repartidor-id').value = '';
    mostrarMensaje('mensaje-repartidores', '');
}

function editarRepartidor(repartidor) {
    document.getElementById('repartidor-id').value = repartidor.id;
    document.getElementById('repartidor-nombre').value = repartidor.nombre;
    document.getElementById('repartidor-apellido').value = repartidor.apellido;
    document.getElementById('repartidor-telefono').value = repartidor.telefono;
    document.getElementById('repartidor-vehiculo').value = repartidor.vehiculo;
    document.getElementById('repartidor-activo').value = repartidor.activo ? 'true' : 'false';
    mostrarMensaje('mensaje-repartidores', 'Editando repartidor ' + repartidor.id);
}

async function guardarRepartidor(evento) {
    evento.preventDefault();
    const id = document.getElementById('repartidor-id').value;
    const datos = {
        nombre: document.getElementById('repartidor-nombre').value.trim(),
        apellido: document.getElementById('repartidor-apellido').value.trim(),
        telefono: document.getElementById('repartidor-telefono').value.trim(),
        vehiculo: document.getElementById('repartidor-vehiculo').value,
        activo: document.getElementById('repartidor-activo').value === 'true'
    };
    try {
        let mensaje;
        if (id) {
            const actualizado = await solicitar('PUT', API.repartidores + '/' + id, datos);
            mensaje = 'Repartidor ' + actualizado.id + ' actualizado';
        } else {
            const creado = await solicitar('POST', API.repartidores, datos);
            mensaje = 'Repartidor creado con id ' + creado.id;
        }
        limpiarRepartidor();
        mostrarMensaje('mensaje-repartidores', mensaje);
        await cargarRepartidores();
    } catch (error) {
        mostrarMensaje('mensaje-repartidores', textoError(error), true);
    }
}

async function alternarRepartidor(repartidor) {
    try {
        const actualizado = await solicitar('PATCH', API.repartidores + '/' + repartidor.id + '/activo', { activo: !repartidor.activo });
        mostrarMensaje('mensaje-repartidores', 'Repartidor ' + actualizado.id + (actualizado.activo ? ' activado' : ' desactivado'));
        await cargarRepartidores();
    } catch (error) {
        mostrarMensaje('mensaje-repartidores', textoError(error), true);
    }
}

async function eliminarRepartidor(repartidor) {
    if (!window.confirm('Eliminar el repartidor ' + repartidor.id + '?')) {
        return;
    }
    try {
        await solicitar('DELETE', API.repartidores + '/' + repartidor.id);
        limpiarRepartidor();
        mostrarMensaje('mensaje-repartidores', 'Repartidor ' + repartidor.id + ' eliminado');
        await cargarRepartidores();
    } catch (error) {
        mostrarMensaje('mensaje-repartidores', textoError(error), true);
    }
}

function configurarRepartidores() {
    document.getElementById('form-repartidor').addEventListener('submit', guardarRepartidor);
    document.getElementById('repartidor-limpiar').addEventListener('click', limpiarRepartidor);
}

async function cargarEntregas() {
    try {
        const entregas = await solicitar('GET', API.entregas);
        renderEntregas(entregas);
        marcarEstado('entregas', true);
    } catch (error) {
        marcarEstado('entregas', false);
        mostrarMensaje('mensaje-entregas', 'No se pudo cargar entregas: ' + textoError(error), true);
    }
}

function formatearFecha(fecha) {
    if (!fecha) {
        return '';
    }
    const valor = new Date(fecha);
    if (Number.isNaN(valor.getTime())) {
        return fecha;
    }
    return valor.toLocaleString('es-PE', { hour12: false });
}

function renderEntregas(entregas) {
    const cuerpo = document.getElementById('tabla-entregas');
    cuerpo.replaceChildren();
    entregas.forEach(entrega => {
        const fila = document.createElement('tr');
        fila.appendChild(celda(entrega.id));
        fila.appendChild(celda(entrega.idCliente));
        fila.appendChild(celda(entrega.idRepartidor));
        fila.appendChild(celda(entrega.direccion));
        fila.appendChild(celda(formatearFecha(entrega.fecha)));
        fila.appendChild(celda(Number(entrega.costo).toFixed(2)));
        fila.appendChild(celda(entrega.estado));
        const acciones = document.createElement('td');
        if (entrega.estado === 'PENDIENTE') {
            acciones.appendChild(botonAccion('Avanzar a EN_CAMINO', null, () => avanzarEntrega(entrega)));
        } else if (entrega.estado === 'EN_CAMINO') {
            acciones.appendChild(botonAccion('Avanzar a ENTREGADA', null, () => avanzarEntrega(entrega)));
        } else {
            const finalizada = botonAccion('Entregada', null, () => {});
            finalizada.disabled = true;
            acciones.appendChild(finalizada);
        }
        fila.appendChild(acciones);
        cuerpo.appendChild(fila);
    });
}

async function guardarEntrega(evento) {
    evento.preventDefault();
    const idCliente = document.getElementById('entrega-cliente').value;
    const idRepartidor = document.getElementById('entrega-repartidor').value;
    const datos = {
        idCliente: Number(idCliente),
        idRepartidor: Number(idRepartidor),
        direccion: document.getElementById('entrega-direccion').value.trim(),
        costo: Number(document.getElementById('entrega-costo').value)
    };
    try {
        const creada = await solicitar('POST', API.entregas, datos);
        mostrarMensaje('mensaje-entregas', 'Entrega registrada con id ' + creada.id + ' en estado ' + creada.estado);
        document.getElementById('form-entrega').reset();
        await cargarEntregas();
    } catch (error) {
        mostrarMensaje('mensaje-entregas', textoError(error), true);
    }
}

async function avanzarEntrega(entrega) {
    const siguiente = entrega.estado === 'PENDIENTE' ? 'EN_CAMINO' : 'ENTREGADA';
    try {
        const actualizada = await solicitar('PATCH', API.entregas + '/' + entrega.id + '/estado', { estado: siguiente });
        mostrarMensaje('mensaje-entregas', 'Entrega ' + actualizada.id + ' actualizada a ' + actualizada.estado);
        await cargarEntregas();
    } catch (error) {
        mostrarMensaje('mensaje-entregas', textoError(error), true);
    }
}

async function buscarEntrega() {
    const id = document.getElementById('busqueda-entrega').value;
    const salida = document.getElementById('resultado-busqueda');
    if (!id) {
        salida.textContent = 'Ingrese un ID para buscar';
        return;
    }
    try {
        const entrega = await solicitar('GET', API.entregas + '/' + id);
        salida.textContent = 'Estado HTTP: 200\n' + formatear(entrega);
    } catch (error) {
        salida.textContent = 'Estado HTTP: ' + error.estado + '\n' + formatear(error.datos === undefined ? { message: error.message } : error.datos);
    }
}

function configurarEntregas() {
    document.getElementById('form-entrega').addEventListener('submit', guardarEntrega);
    document.getElementById('buscar-entrega').addEventListener('click', buscarEntrega);
}

function clienteEjemplo() {
    const sufijo = Date.now();
    return {
        nombre: 'Cliente',
        apellido: 'Prueba ' + sufijo,
        email: 'prueba' + sufijo + '@test.com',
        telefono: '900000000',
        direccion: 'Av. Prueba 1'
    };
}

function repartidorEjemplo() {
    const sufijo = Date.now();
    return {
        nombre: 'Repartidor',
        apellido: 'Prueba ' + sufijo,
        telefono: '900000001',
        vehiculo: 'MOTO',
        activo: true
    };
}

async function datosBase() {
    let clientes = await solicitar('GET', API.clientes);
    if (clientes.length === 0) {
        const creado = await solicitar('POST', API.clientes, clienteEjemplo());
        datosCreados = true;
        clientes = [creado];
    }
    let repartidores = await solicitar('GET', API.repartidores);
    if (repartidores.length === 0) {
        const creado = await solicitar('POST', API.repartidores, repartidorEjemplo());
        datosCreados = true;
        repartidores = [creado];
    }
    return {
        idCliente: clientes[0].id,
        idRepartidor: repartidores[0].id
    };
}

async function casoCostoNegativo() {
    const base = await datosBase();
    return peticion('POST', API.entregas, {
        idCliente: base.idCliente,
        idRepartidor: base.idRepartidor,
        direccion: 'Av. Error 1',
        costo: -1
    });
}

async function casoDireccionVacia() {
    const base = await datosBase();
    return peticion('POST', API.entregas, {
        idCliente: base.idCliente,
        idRepartidor: base.idRepartidor,
        direccion: '',
        costo: 10
    });
}

async function casoEntregaInexistente() {
    return peticion('GET', API.entregas + '/999999');
}

async function casoClienteInexistente() {
    const base = await datosBase();
    return peticion('POST', API.entregas, {
        idCliente: 999999,
        idRepartidor: base.idRepartidor,
        direccion: 'Av. Error 2',
        costo: 10
    });
}

async function casoRepartidorInexistente() {
    const base = await datosBase();
    return peticion('POST', API.entregas, {
        idCliente: base.idCliente,
        idRepartidor: 999999,
        direccion: 'Av. Error 3',
        costo: 10
    });
}

async function casoTransicionInvalida() {
    let entregas = await solicitar('GET', API.entregas);
    if (entregas.length === 0) {
        const base = await datosBase();
        const creada = await solicitar('POST', API.entregas, {
            idCliente: base.idCliente,
            idRepartidor: base.idRepartidor,
            direccion: 'Av. Error 4',
            costo: 10
        });
        datosCreados = true;
        entregas = [creada];
    }
    const entrega = entregas[0];
    const siguiente = entrega.estado === 'PENDIENTE' ? 'ENTREGADA' : 'PENDIENTE';
    return peticion('PATCH', API.entregas + '/' + entrega.id + '/estado', { estado: siguiente });
}

async function casoEstadoInvalido() {
    const entregas = await solicitar('GET', API.entregas);
    const id = entregas.length > 0 ? entregas[0].id : 1;
    return peticion('PATCH', API.entregas + '/' + id + '/estado', { estado: 'VOLANDO' });
}

const CASOS_ERROR = {
    costo_negativo: { titulo: 'Costo negativo', esperado: 400, accion: casoCostoNegativo },
    direccion_vacia: { titulo: 'Dirección vacía', esperado: 400, accion: casoDireccionVacia },
    entrega_inexistente: { titulo: 'Entrega inexistente', esperado: 404, accion: casoEntregaInexistente },
    cliente_inexistente: { titulo: 'Cliente inexistente', esperado: 404, accion: casoClienteInexistente },
    repartidor_inexistente: { titulo: 'Repartidor inexistente', esperado: 404, accion: casoRepartidorInexistente },
    transicion_invalida: { titulo: 'Transición de estado inválida', esperado: 409, accion: casoTransicionInvalida },
    estado_invalido: { titulo: 'JSON de estado inválido', esperado: 400, accion: casoEstadoInvalido }
};

async function ejecutarCaso(caso) {
    const salida = document.getElementById('caso-salida');
    salida.textContent = 'Ejecutando: ' + caso.titulo + '...';
    datosCreados = false;
    try {
        const resultado = await caso.accion();
        salida.textContent = caso.titulo + '\nEstado esperado: ' + caso.esperado + '\nEstado obtenido: ' + resultado.estado + '\n' + formatear(resultado.datos);
    } catch (error) {
        salida.textContent = caso.titulo + '\nError inesperado: ' + error.message;
    }
    if (datosCreados) {
        datosCreados = false;
        await refrescarDatos();
    }
}

function configurarErrores() {
    document.querySelectorAll('[data-caso]').forEach(boton => {
        boton.addEventListener('click', () => {
            const caso = CASOS_ERROR[boton.dataset.caso];
            if (caso) {
                ejecutarCaso(caso);
            }
        });
    });
}

async function refrescarDatos() {
    await Promise.allSettled([cargarClientes(), cargarRepartidores(), cargarEntregas()]);
}

document.addEventListener('DOMContentLoaded', () => {
    configurarTabs();
    configurarClientes();
    configurarRepartidores();
    configurarEntregas();
    configurarErrores();
    refrescarDatos();
});

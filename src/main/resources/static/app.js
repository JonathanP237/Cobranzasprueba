const state = {
  token: localStorage.getItem("cobranzas.jwt"),
  authCobrador: JSON.parse(localStorage.getItem("cobranzas.authCobrador") || "null"),
  clients: [],
  credits: [],
  quotas: [],
  selectedCredit: null
};

const money = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0
});
const dateFormat = new Intl.DateTimeFormat("es-CO", { dateStyle: "medium" });
const labels = {
  AL_DIA: "Al día", PAGADO: "Pagado", REFINANCIADO: "Refinanciado",
  PENDIENTE: "Pendiente", PAGADA: "Pagada", LLAMADA: "Llamada",
  MENSAJE: "Mensaje", VISITA: "Visita", PAGO_CONFIRMADO: "Pago confirmado",
  SIN_RESPUESTA: "Sin respuesta", PROMESA_PAGO: "Promesa de pago",
  ACUERDO_PAGO: "Acuerdo de pago", REFINANCIACION: "Refinanciación",
  CONTACTO_EXITOSO: "Contacto exitoso", RECORDATORIO: "Recordatorio"
};
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];

async function api(url, options = {}) {
  const headers = { "Content-Type": "application/json", ...options.headers };
  if (state.token) {
    headers["Authorization"] = `Bearer ${state.token}`;
  }
  const response = await fetch(url, { ...options, headers });
  
  if (response.status === 401) {
  logout();
  throw new Error(
    "Tu sesión no es válida o ha expirado. Inicia sesión nuevamente."
  );
}

if (response.status === 403) {
  throw new Error(
    "No tienes permisos para realizar esta operación."
  );
}
  
  const data = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.error || data?.message || `Error ${response.status}`);
  return data;
}

function escapeHtml(value = "") {
  return String(value).replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

function pretty(value) {
  return labels[value] || value || "—";
}

function dateValue(value) {
  if (!value) return "—";
  return dateFormat.format(new Date(`${value.slice(0, 10)}T12:00:00`));
}

function notify(message, type = "success") {
  const notice = $("#notice");
  notice.textContent = message;
  notice.className = `notice ${type}`;
  notice.hidden = false;
  window.clearTimeout(notify.timeout);
  notify.timeout = window.setTimeout(() => { notice.hidden = true; }, 5000);
}

function updateSessionUI() {
  if (state.authCobrador) {
    $("#auth-collector-name").textContent = state.authCobrador.nombreCobrador;
    $("#user-session-badge").textContent = `Conectado: @${state.authCobrador.username}`;
    $("#login-dialog").close();
  } else {
    $("#auth-collector-name").textContent = "No autenticado";
    $("#user-session-badge").textContent = "Sesión requerida";
    $("#login-dialog").showModal();
  }
}

function logout() {
  localStorage.removeItem("cobranzas.jwt");
  localStorage.removeItem("cobranzas.authCobrador");
  state.token = null;
  state.authCobrador = null;
  state.credits = [];
  state.clients = [];
  state.quotas = [];
  updateSessionUI();
}

function fillClientOptions() {
  $("#credit-client").innerHTML = state.clients.length
    ? state.clients.map(client => `<option value="${client.id}">${escapeHtml(client.nombre)}</option>`).join("")
    : '<option value="">Primero registra un cliente</option>';
}

function renderTable(container, headers, rows, emptyMessage) {
  if (!rows.length) {
    container.innerHTML = `<div class="empty-state"><span>▤</span><strong>${emptyMessage}</strong><p>Sin información registrada en tu cartera.</p></div>`;
    return;
  }
  container.innerHTML = `<table><thead><tr>${headers.map(item => `<th>${item}</th>`).join("")}</tr></thead><tbody>${rows.join("")}</tbody></table>`;
}

function creditRow(credit, withAction = true) {
  return `<tr>
    <td><span class="id-cell">#${credit.id}</span></td>
    <td><strong>${escapeHtml(credit.nombreCliente)}</strong><small>${escapeHtml(credit.nombreCobrador)}</small></td>
    <td>${money.format(credit.monto)}</td>
    <td><span class="rate-pill">${Number(credit.tasaInteres).toFixed(0)}%</span></td>
    <td>${dateValue(credit.fechaLimite)}</td>
    <td><span class="status ${credit.estado.toLowerCase()}">${pretty(credit.estado)}</span></td>
    ${withAction ? `<td><button class="row-action" data-credit="${credit.id}">Ver detalle →</button></td>` : ""}
  </tr>`;
}

async function refresh() {
  if (!state.token || !state.authCobrador) {
    updateSessionUI();
    return;
  }

  try {
    const cobradorId = state.authCobrador.cobradorId;
    const [clients, credits] = await Promise.all([
      api("/api/clientes"),
      api(`/api/cobradores/${cobradorId}/creditos`)
    ]);

    state.credits = credits;
    
    // Filtrar clientes para mostrar únicamente los que tienen créditos con este cobrador
    state.clients = clients;
    state.allClients = clients;

    fillClientOptions();

    $("#credit-client").disabled = clients.length === 0;
    renderClients();
    renderCredits();
    await renderOverview();
  } catch (error) {
    notify(error.message, "error");
  }
}

function renderClients() {
  renderTable($("#client-list"), ["CLIENTE", "TELÉFONO", "MIS CRÉDITOS", ""],
    state.clients.map(client => {
      const count = state.credits.filter(credit => credit.clienteId === client.id).length;
      return `<tr><td><strong>${escapeHtml(client.nombre)}</strong></td><td>${escapeHtml(client.telefono || "—")}</td><td>${count}</td><td><button class="row-action" data-client-credit="${client.id}">Ver créditos →</button></td></tr>`;
    }), "No tienes clientes con créditos activos");
}

function renderCredits() {
  const headers = ["N.º", "CLIENTE / COBRADOR", "CAPITAL", "TASA", "VENCIMIENTO", "ESTADO", ""];
  renderTable($("#credit-list"), headers, state.credits.map(credit => creditRow(credit)), "No tienes créditos asignados");
}

async function renderOverview() {
  const quotaGroups = await Promise.all(state.credits.map(credit => api(`/api/cuotas/credito/${credit.id}`)));
  state.quotas = quotaGroups.flat();
  const pending = state.quotas.filter(item => item.estado === "PENDIENTE");
  const portfolio = pending.reduce((sum, item) =>
    sum + Math.max(0, Number(item.montoCuota) - Number(item.montoPagado || 0)), 0);
    
  $("#stat-credits").textContent = state.credits.length;
  $("#stat-clients").textContent = state.clients.length;
  $("#stat-pending").textContent = pending.length;
  $("#stat-balance").textContent = money.format(portfolio);
  
  const recent = [...state.credits].sort((a, b) => b.id - a.id).slice(0, 5);
  renderTable($("#recent-credits"), ["N.º", "CLIENTE / COBRADOR", "CAPITAL", "TASA", "VENCIMIENTO", "ESTADO", ""],
    recent.map(credit => creditRow(credit)), "No hay créditos para mostrar");
}

function showPage(name) {
  $$(".page-section").forEach(section => { section.hidden = section.id !== `page-${name}`; });
  $$(".nav-link").forEach(link => link.classList.toggle("active", link.dataset.page === name));
  $("#page-title").textContent = { inicio: "Resumen", creditos: "Mis Créditos", clientes: "Mis Clientes" }[name] || "Resumen";
}

function getRate(startValue, endValue) {
  if (!startValue || !endValue) return null;
  const start = new Date(`${startValue}T12:00:00`);
  const end = new Date(`${endValue}T12:00:00`);
  if (end <= start) return null;
  const days = Math.round((end - start) / 86400000);
  if (days === 7) return 5;
  if (days === 15) return 10;
  if (days >= 28) return 20;
  return null;
}

function updateInterestPreview() {
  const start = $('[name="fechaInicio"]', $("#credit-form")).value;
  const end = $('[name="fechaLimite"]', $("#credit-form")).value;
  const amount = Number($('[name="monto"]', $("#credit-form")).value || 0);
  const preview = $("#interest-preview");
  const rate = getRate(start, end);
  if (!rate || !amount) {
    preview.className = "preview-box";
    preview.textContent = "Regla de duración: 7 días = 5%, 15 días = 10%, un mes o más (≥ 28 días) = 20%.";
    return;
  }
  const interest = Math.round((amount * rate) / 100);
  const total = amount + interest;
  preview.className = "preview-box active";
  preview.innerHTML = `<strong>Tasa aplicada por duración: ${rate}%</strong><br>Capital: ${money.format(amount)} • Interés: ${money.format(interest)}<br>Total a pagar en cuotas: <strong>${money.format(total)}</strong>`;
}

async function openDetail(creditId) {
  const credit = state.credits.find(item => item.id === Number(creditId));
  if (!credit) return;
  state.selectedCredit = credit;
  const [quotas, history] = await Promise.all([
    api(`/api/cuotas/credito/${credit.id}`),
    api(`/api/creditos/${credit.id}/gestiones`)
  ]);
  $("#detail-title").textContent = `Crédito #${credit.id} — ${credit.nombreCliente}`;
  const total = quotas.reduce((acc, q) => acc + Number(q.montoCuota), 0);
  const paid = quotas.reduce((acc, q) => acc + Number(q.montoPagado || 0), 0);
  const pending = Math.max(0, total - paid);
  const interest = Math.round((credit.monto * credit.tasaInteres) / 100);
  const interestCovered = paid >= interest;

  $("#detail-summary").innerHTML = `
    <div><span>Cliente</span><strong>${escapeHtml(credit.nombreCliente)}</strong></div>
    <div><span>Cobrador</span><strong>${escapeHtml(credit.nombreCobrador)}</strong></div>
    <div><span>Capital prestado</span><strong>${money.format(credit.monto)}</strong></div>
    <div><span>Tasa / Interés</span><strong>${Number(credit.tasaInteres).toFixed(0)}% (${money.format(interest)})</strong></div>
    <div><span>Total financiado</span><strong>${money.format(total)}</strong></div>
    <div><span>Total recaudado</span><strong>${money.format(paid)}</strong></div>
    <div><span>Saldo pendiente</span><strong>${money.format(pending)}</strong></div>
    <div><span>Estado</span><strong class="status ${credit.estado.toLowerCase()}">${pretty(credit.estado)}</strong></div>
    <div><span>Interés cubierto</span><strong>${interestCovered ? "✅ Sí (Apto refinanciar)" : "❌ No (" + money.format(paid) + "/" + money.format(interest) + ")"}</strong></div>
  `;

  renderTable($("#detail-installments"), ["CUOTA", "VALOR", "ABONADO", "SALDO", "VENCE", "ESTADO"],
    quotas.map(quota => {
      const balance = Math.max(0, Number(quota.montoCuota) - Number(quota.montoPagado || 0));
      return `<tr><td>#${quota.numeroCuota}</td><td>${money.format(quota.montoCuota)}</td><td>${money.format(quota.montoPagado || 0)}</td><td>${money.format(balance)}</td><td>${dateValue(quota.fechaVencimiento)}</td><td><span class="status ${quota.estado.toLowerCase()}">${pretty(quota.estado)}</span></td></tr>`;
    }), "Sin cuotas generadas");

  $("#detail-history").innerHTML = history.length
    ? history.map(item => `<div class="history-item"><div class="history-top"><span class="badge ${item.tipoGestion.toLowerCase()}">${pretty(item.tipoGestion)}</span><strong>${pretty(item.resultado)}</strong><small>${item.fechaGestion ? item.fechaGestion.replace("T", " ").slice(0, 16) : "—"}</small></div><p>${escapeHtml(item.observaciones || "Sin observaciones")}</p></div>`).join("")
    : '<div class="empty-state"><span>ℹ</span><strong>Sin gestiones registradas</strong><p>Registra la primera llamada o visita.</p></div>';

  $("#detail-dialog").showModal();
}

function openManagement() {
  if (!state.selectedCredit) return;
  api(`/api/cobradores/${state.authCobrador.cobradorId}/creditos/${state.selectedCredit.id}/cuotas-pendientes`)
    .then(quotas => {
      if (!quotas.length) throw new Error("No hay cuotas pendientes para gestionar en este crédito.");
      $("#management-installment").innerHTML = quotas.map(q =>
        `<option value="${q.id}">Cuota #${q.numeroCuota} — ${money.format(q.montoCuota)} (Vence ${dateValue(q.fechaVencimiento)})</option>`).join("");
      $("#management-dialog").showModal();
    }).catch(error => notify(error.message, "error"));
}

function openPayment() {
  if (!state.selectedCredit) return;
  api(`/api/cobradores/${state.authCobrador.cobradorId}/creditos/${state.selectedCredit.id}/cuotas-pendientes`)
    .then(quotas => {
      if (!quotas.length) throw new Error("No hay cuotas pendientes para pagar en este crédito.");
      $("#payment-installment").innerHTML = quotas.map(q => {
        const balance = Math.max(0, Number(q.montoCuota) - Number(q.montoPagado || 0));
        return `<option value="${q.id}" data-max="${balance}">Cuota #${q.numeroCuota} — Saldo: ${money.format(balance)}</option>`;
      }).join("");
      $('[name="monto"]', $("#payment-form")).value = $("#payment-installment").selectedOptions[0]?.dataset.max || "";
      $("#payment-dialog").showModal();
    }).catch(error => notify(error.message, "error"));
}

document.addEventListener("DOMContentLoaded", () => {
  // Manejo de autenticación
  $("#login-form").addEventListener("submit", async event => {
    event.preventDefault();
    const username = $("#login-username").value;
    const password = $("#login-password").value;
    try {
      const authData = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      state.token = authData.token;
      state.authCobrador = authData;
      localStorage.setItem("cobranzas.jwt", authData.token);
      localStorage.setItem("cobranzas.authCobrador", JSON.stringify(authData));
      updateSessionUI();
      notify(`Bienvenido, ${authData.nombreCobrador}`);
      await refresh();
    } catch (error) {
      notify(error.message, "error");
    }
  });

  $("#logout-button").addEventListener("click", () => {
    logout();
    notify("Has cerrado sesión.");
  });

  document.body.addEventListener("click", event => {
    const pageButton = event.target.closest("[data-page]");
    if (pageButton) {
      event.preventDefault();
      showPage(pageButton.dataset.page);
      return;
    }
    const openButton = event.target.closest("[data-open]");
    if (openButton) {
      $(`#${openButton.dataset.open}`).showModal();
      return;
    }
    const closeButton = event.target.closest("[data-close]");
    if (closeButton) {
      closeButton.closest("dialog").close();
      return;
    }
    const creditButton = event.target.closest("[data-credit]");
    if (creditButton) {
      openDetail(creditButton.dataset.credit);
      return;
    }
    const clientCreditButton = event.target.closest("[data-client-credit]");
    if (clientCreditButton) {
      showPage("creditos");
      const clientCredits = state.credits.filter(c => c.clienteId === Number(clientCreditButton.dataset.clientCredit));
      renderTable($("#credit-list"), ["N.º", "CLIENTE / COBRADOR", "CAPITAL", "TASA", "VENCIMIENTO", "ESTADO", ""],
        clientCredits.map(credit => creditRow(credit)), "El cliente no tiene créditos con este cobrador");
      return;
    }
  });

  $('[name="fechaInicio"]', $("#credit-form")).addEventListener("change", updateInterestPreview);
  $('[name="fechaLimite"]', $("#credit-form")).addEventListener("change", updateInterestPreview);
  $('[name="monto"]', $("#credit-form")).addEventListener("input", updateInterestPreview);

  $("#manage-action").addEventListener("click", openManagement);
  $("#pay-action").addEventListener("click", openPayment);

  $("#client-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
      await api("/api/clientes", {
        method: "POST",
        body: JSON.stringify({
          nombreCliente: formData.get("nombreCliente"),
          telefonoCliente: formData.get("telefonoCliente") || null
        })
      });
      event.target.reset();
      $("#client-dialog").close();
      notify("Cliente registrado correctamente");
      await refresh();
    } catch (error) {
      notify(error.message, "error");
    }
  });

  $("#credit-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
      await api("/api/creditos", {
        method: "POST",
        body: JSON.stringify({
          clienteId: Number(formData.get("clienteId")),
          monto: Number(formData.get("monto")),
          fechaInicio: formData.get("fechaInicio"),
          fechaLimite: formData.get("fechaLimite"),
          frecuenciaPago: formData.get("frecuenciaPago")
        })
      });
      event.target.reset();
      updateInterestPreview();
      $("#credit-dialog").close();
      notify("Crédito creado correctamente");
      await refresh();
    } catch (error) {
      notify(error.message, "error");
    }
  });

  $("#management-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
      await api(`/api/cobradores/${state.authCobrador.cobradorId}/gestiones`, {
        method: "POST",
        body: JSON.stringify({
          cuotaId: Number(formData.get("cuotaId")),
          tipoGestion: formData.get("tipoGestion"),
          resultado: formData.get("resultado"),
          observaciones: formData.get("observaciones")
        })
      });
      event.target.reset();
      $("#management-dialog").close();
      notify("Gestión de cobro guardada");
      if (state.selectedCredit) await openDetail(state.selectedCredit.id);
      await refresh();
    } catch (error) {
      notify(error.message, "error");
    }
  });

  $("#payment-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formData = new FormData(event.target);
    try {
      await api(`/api/cobradores/${state.authCobrador.cobradorId}/pagos`, {
        method: "POST",
        body: JSON.stringify({
          cuotaId: Number(formData.get("cuotaId")),
          monto: Number(formData.get("monto")),
          tipoGestion: "VISITA",
          observaciones: "Pago recibido en campo por cobrador."
        })
      });
      event.target.reset();
      $("#payment-dialog").close();
      notify("Pago registrado con éxito");
      if (state.selectedCredit) await openDetail(state.selectedCredit.id);
      await refresh();
    } catch (error) {
      notify(error.message, "error");
    }
  });

  updateSessionUI();
  if (state.token && state.authCobrador) {
    refresh();
  }
});

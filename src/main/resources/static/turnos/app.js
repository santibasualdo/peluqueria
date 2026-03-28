const API_BASE_URL = "/api/v1";

const queryParams = new URLSearchParams(window.location.search);

const state = {
  peluqueriaId: Number(queryParams.get("peluqueriaId") || 1),
  selectedDate: normalizeDateFromQuery(),
  turnos: [],
  peluqueros: [],
  servicios: [],
  loading: false,
  saving: false,
  modalMode: "create",
  editingTurnoId: null,
};

const elements = {
  pageTitle: document.getElementById("page-title"),
  pageHint: document.getElementById("page-hint"),
  dateList: document.getElementById("date-list"),
  turnosCount: document.getElementById("turnos-count"),
  appointmentsList: document.getElementById("appointments-list"),
  loadingState: document.getElementById("loading-state"),
  emptyState: document.getElementById("empty-state"),
  errorState: document.getElementById("error-state"),
  errorMessage: document.getElementById("error-message"),
  retryButton: document.getElementById("retry-button"),
  prevDay: document.getElementById("prev-day"),
  nextDay: document.getElementById("next-day"),
  newTurnoButton: document.getElementById("new-turno-button"),
  newPeluqueroButton: document.getElementById("new-peluquero-button"),
  modalShell: document.getElementById("turno-modal-shell"),
  modalBackdrop: document.getElementById("modal-backdrop"),
  closeModalButton: document.getElementById("close-modal-button"),
  cancelFormButton: document.getElementById("cancel-form-button"),
  modalTitle: document.getElementById("turno-modal-title"),
  modalSubtitle: document.getElementById("turno-modal-subtitle"),
  form: document.getElementById("turno-form"),
  saveTurnoButton: document.getElementById("save-turno-button"),
  formError: document.getElementById("form-error"),
  clienteNombre: document.getElementById("cliente-nombre"),
  clienteTelefono: document.getElementById("cliente-telefono"),
  clienteObservaciones: document.getElementById("cliente-observaciones"),
  turnoObservaciones: document.getElementById("turno-observaciones"),
  peluqueroId: document.getElementById("peluquero-id"),
  servicioId: document.getElementById("servicio-id"),
  turnoFecha: document.getElementById("turno-fecha"),
  turnoHora: document.getElementById("turno-hora"),
  peluqueroModalShell: document.getElementById("peluquero-modal-shell"),
  peluqueroModalBackdrop: document.getElementById("peluquero-modal-backdrop"),
  closePeluqueroModalButton: document.getElementById("close-peluquero-modal-button"),
  cancelPeluqueroFormButton: document.getElementById("cancel-peluquero-form-button"),
  peluqueroForm: document.getElementById("peluquero-form"),
  savePeluqueroButton: document.getElementById("save-peluquero-button"),
  peluqueroFormError: document.getElementById("peluquero-form-error"),
  peluqueroNombre: document.getElementById("peluquero-nombre"),
  peluqueroTelefono: document.getElementById("peluquero-telefono"),
  peluqueroPassword: document.getElementById("peluquero-password"),
};

document.addEventListener("DOMContentLoaded", async () => {
  bindEvents();
  renderHeader();
  renderDateSelector();
  await bootstrap();
});

function bindEvents() {
  elements.prevDay.addEventListener("click", () => moveSelectedDate(-1));
  elements.nextDay.addEventListener("click", () => moveSelectedDate(1));
  elements.retryButton.addEventListener("click", () => loadTurnos());
  elements.newTurnoButton.addEventListener("click", () => openCreateModal());
  elements.newPeluqueroButton.addEventListener("click", openPeluqueroModal);
  elements.closeModalButton.addEventListener("click", closeModal);
  elements.cancelFormButton.addEventListener("click", closeModal);
  elements.modalBackdrop.addEventListener("click", closeModal);
  elements.form.addEventListener("submit", handleTurnoSubmit);
  elements.turnoHora.addEventListener("input", handleHoraInput);
  elements.closePeluqueroModalButton.addEventListener("click", closePeluqueroModal);
  elements.cancelPeluqueroFormButton.addEventListener("click", closePeluqueroModal);
  elements.peluqueroModalBackdrop.addEventListener("click", closePeluqueroModal);
  elements.peluqueroForm.addEventListener("submit", handlePeluqueroSubmit);
}

async function bootstrap() {
  try {
    await loadCatalogos();
    await loadTurnos();
  } catch (error) {
    showError(error.message || "No pudimos inicializar la agenda.");
  }
}

async function loadCatalogos() {
  const [peluquerosResponse, serviciosResponse] = await Promise.all([
    fetch(`${API_BASE_URL}/peluqueros?peluqueriaId=${state.peluqueriaId}`),
    fetch(`${API_BASE_URL}/servicios?peluqueriaId=${state.peluqueriaId}`),
  ]);

  if (!peluquerosResponse.ok || !serviciosResponse.ok) {
    throw new Error("No pudimos cargar los catalogos de la peluqueria.");
  }

  state.peluqueros = await peluquerosResponse.json();
  state.servicios = await serviciosResponse.json();

  populateSelect(
    elements.peluqueroId,
    state.peluqueros,
    (peluquero) => peluquero.id,
    (peluquero) => peluquero.nombre
  );

  populateSelect(
    elements.servicioId,
    state.servicios,
    (servicio) => servicio.id,
    (servicio) => `${servicio.nombre} · ${servicio.duracionMinutos} min`
  );
}

async function loadTurnos() {
  setLoading(true);
  hideError();

  try {
    const response = await fetch(
      `${API_BASE_URL}/turnos?peluqueriaId=${state.peluqueriaId}&fecha=${formatDateForApi(state.selectedDate)}`
    );

    if (!response.ok) {
      throw new Error("No pudimos obtener los turnos del dia.");
    }

    state.turnos = await response.json();
    renderHeader();
    renderTurnos();
  } catch (error) {
    showError(error.message || "Ocurrio un error cargando la agenda.");
  } finally {
    setLoading(false);
  }
}

async function handleTurnoSubmit(event) {
  event.preventDefault();
  setFormSaving(true);
  hideFormError();

  try {
    const payload = buildTurnoPayload();
    const selectedDateValue = elements.turnoFecha.value;
    const url = state.modalMode === "edit"
      ? `${API_BASE_URL}/turnos/${state.editingTurnoId}`
      : `${API_BASE_URL}/turnos`;
    const method = state.modalMode === "edit" ? "PUT" : "POST";

    const response = await fetch(url, {
      method,
      headers: buildJsonHeaders(),
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(await extractErrorMessage(response));
    }

    state.selectedDate = parseDate(selectedDateValue);
    closeModal();
    syncQueryDate();
    renderHeader();
    renderDateSelector();
    await loadTurnos();
  } catch (error) {
    showFormError(error.message || "No pudimos guardar el turno.");
  } finally {
    setFormSaving(false);
  }
}

async function handlePeluqueroSubmit(event) {
  event.preventDefault();
  hidePeluqueroFormError();
  setPeluqueroSaving(true);

  try {
    const response = await fetch(`${API_BASE_URL}/peluqueros`, {
      method: "POST",
      headers: buildJsonHeaders(),
      body: JSON.stringify({
        nombre: elements.peluqueroNombre.value.trim(),
        telefono: elements.peluqueroTelefono.value.trim(),
        password: elements.peluqueroPassword.value,
        peluqueriaId: state.peluqueriaId,
      }),
    });

    if (!response.ok) {
      throw new Error(await extractErrorMessage(response));
    }

    await loadCatalogos();
    closePeluqueroModal();
    window.alert("Peluquero creado. El usuario de ingreso es su telefono en numeros.");
  } catch (error) {
    showPeluqueroFormError(error.message || "No pudimos crear el peluquero.");
  } finally {
    setPeluqueroSaving(false);
  }
}

async function cancelTurno(turnoId) {
  const confirmed = window.confirm("¿Querés cancelar este turno?");
  if (!confirmed) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/turnos/${turnoId}/cancelar`, {
      method: "PATCH",
      headers: buildJsonHeaders(),
      body: JSON.stringify({
        observaciones: "Cancelado desde la agenda web",
      }),
    });

    if (!response.ok) {
      throw new Error(await extractErrorMessage(response));
    }

    await loadTurnos();
  } catch (error) {
    window.alert(error.message || "No pudimos cancelar el turno.");
  }
}

async function reactivarTurno(turnoId) {
  const confirmed = window.confirm("¿Querés reactivar este turno?");
  if (!confirmed) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/turnos/${turnoId}/reactivar`, {
      method: "PATCH",
      headers: buildJsonHeaders(),
    });

    if (!response.ok) {
      throw new Error(await extractErrorMessage(response));
    }

    await loadTurnos();
  } catch (error) {
    window.alert(error.message || "No pudimos reactivar el turno.");
  }
}

function openCreateModal() {
  state.modalMode = "create";
  state.editingTurnoId = null;
  resetForm();

  elements.modalTitle.textContent = "Nuevo turno";
  elements.modalSubtitle.textContent = "Completá los datos para reservar un turno manualmente.";
  elements.saveTurnoButton.textContent = "Guardar turno";
  elements.clienteTelefono.placeholder = "";
  elements.turnoFecha.value = formatDateForApi(state.selectedDate);
  elements.turnoHora.value = suggestTime();

  if (state.peluqueros[0]) {
    elements.peluqueroId.value = String(state.peluqueros[0].id);
  }

  if (state.servicios[0]) {
    elements.servicioId.value = String(state.servicios[0].id);
  }

  showModal();
}

function openEditModal(turnoId) {
  const turno = state.turnos.find((item) => item.id === turnoId);
  if (!turno) {
    window.alert("No encontramos el turno seleccionado.");
    return;
  }

  state.modalMode = "edit";
  state.editingTurnoId = turno.id;
  resetForm();

  elements.modalTitle.textContent = "Editar turno";
  elements.modalSubtitle.textContent = `Modificá la reserva de ${turno.clienteNombre}. Si no querés cambiar el telefono, dejalo vacio.`;
  elements.saveTurnoButton.textContent = "Guardar cambios";
  elements.clienteNombre.value = turno.clienteNombre ?? "";
  elements.clienteTelefono.value = "";
  elements.clienteTelefono.placeholder = "Solo cargalo si querés reemplazar el telefono";
  elements.clienteObservaciones.value = "";
  elements.turnoObservaciones.value = turno.observaciones ?? "";
  elements.peluqueroId.value = String(turno.peluqueroId);
  elements.servicioId.value = String(turno.servicioId);
  elements.turnoFecha.value = formatDateForApi(new Date(turno.fechaHoraInicio));
  elements.turnoHora.value = formatTimeForInput(turno.fechaHoraInicio);

  showModal();
}

function openPeluqueroModal() {
  elements.peluqueroForm.reset();
  hidePeluqueroFormError();
  setPeluqueroSaving(false);
  elements.peluqueroModalShell.classList.remove("hidden");
  elements.peluqueroModalShell.setAttribute("aria-hidden", "false");
}

function renderHeader() {
  const titleFormatter = new Intl.DateTimeFormat("es-AR", {
    weekday: "long",
    day: "numeric",
    month: "short",
  });

  elements.pageTitle.textContent = capitalize(titleFormatter.format(state.selectedDate));
  elements.pageHint.textContent = `Peluqueria ${state.peluqueriaId}`;
}

function renderDateSelector() {
  const days = Array.from({ length: 5 }, (_, index) => addDays(state.selectedDate, index - 2));

  elements.dateList.innerHTML = days
    .map((date) => {
      const isActive = isSameDay(date, state.selectedDate);
      return `
        <button class="date-pill ${isActive ? "date-pill--active" : "date-pill--muted"}" type="button" data-date="${formatDateForApi(date)}">
          <span class="date-pill__day">${formatWeekdayShort(date)}</span>
          <span class="date-pill__number">${date.getDate()}</span>
        </button>
      `;
    })
    .join("");

  elements.dateList.querySelectorAll("[data-date]").forEach((button) => {
    button.addEventListener("click", async () => {
      state.selectedDate = parseDate(button.dataset.date);
      syncQueryDate();
      renderHeader();
      renderDateSelector();
      await loadTurnos();
    });
  });

  centerActiveDatePill();
}

function renderTurnos() {
  const turnos = [...state.turnos].sort(
    (left, right) => new Date(left.fechaHoraInicio).getTime() - new Date(right.fechaHoraInicio).getTime()
  );

  elements.turnosCount.textContent = `${turnos.length} Total`;
  elements.appointmentsList.innerHTML = "";

  if (turnos.length === 0) {
    elements.emptyState.classList.remove("hidden");
    return;
  }

  elements.emptyState.classList.add("hidden");
  const fragment = document.createDocumentFragment();

  turnos.forEach((turno) => {
    const editable = ["RESERVADO", "EN_CURSO"].includes(turno.estado);
    const card = document.createElement("article");
    card.className = `appointment-card ${turno.estado === "CANCELADO" ? "appointment-card--cancelled" : ""}`;

    card.innerHTML = `
      <div class="appointment-card__time">
        <span class="appointment-card__hour">${formatTime(turno.fechaHoraInicio)}</span>
        <span class="appointment-card__duration">${turno.servicioDuracionMinutos} min</span>
      </div>

      <div class="appointment-card__content">
        <div class="appointment-card__header">
          <h3>${escapeHtml(turno.clienteNombre)}</h3>
          ${buildStatusChip(turno.estado)}
        </div>

        <div class="appointment-card__meta">
          <span class="badge">${escapeHtml(turno.servicioNombre)}</span>
          <span class="meta-item">
            <span class="material-symbols-outlined meta-item__icon">content_cut</span>
            ${escapeHtml(turno.peluqueroNombre)}
          </span>
          <span class="meta-item">
            <span class="material-symbols-outlined meta-item__icon">call</span>
            ${escapeHtml(turno.clienteTelefonoMascarado ?? "Telefono protegido")}
          </span>
        </div>

        ${turno.observaciones ? `<p class="appointment-card__note">${escapeHtml(turno.observaciones)}</p>` : ""}
      </div>

      <div class="appointment-card__actions">
        <button class="btn btn--secondary js-edit-turno" type="button" data-turno-id="${turno.id}" ${editable ? "" : "disabled"}>
          <span class="material-symbols-outlined btn__icon">edit</span>
          Editar
        </button>
        ${turno.estado === "CANCELADO"
          ? `
            <button class="btn btn--primary js-reactivate-turno" type="button" data-turno-id="${turno.id}">
              <span class="material-symbols-outlined btn__icon">restart_alt</span>
              Reactivar
            </button>
          `
          : `
            <button class="btn btn--danger js-cancel-turno" type="button" data-turno-id="${turno.id}">
              <span class="material-symbols-outlined btn__icon">cancel</span>
              Cancelar
            </button>
          `}
      </div>
    `;

    const cancelButton = card.querySelector(".js-cancel-turno");
    const reactivateButton = card.querySelector(".js-reactivate-turno");

    if (cancelButton) {
      cancelButton.addEventListener("click", () => cancelTurno(turno.id));
    }

    if (reactivateButton) {
      reactivateButton.addEventListener("click", () => reactivarTurno(turno.id));
    }

    card.querySelector(".js-edit-turno").addEventListener("click", () => openEditModal(turno.id));

    fragment.appendChild(card);
  });

  elements.appointmentsList.appendChild(fragment);
}

function buildTurnoPayload() {
  const fechaHoraInicio = `${elements.turnoFecha.value}T${normalizeTime(elements.turnoHora.value)}`;
  const cliente = {
    nombre: elements.clienteNombre.value.trim(),
    telefono: elements.clienteTelefono.value.trim(),
    observaciones: normalizeNullableString(elements.clienteObservaciones.value),
  };

  if (state.modalMode === "edit") {
    return {
      peluqueroId: Number(elements.peluqueroId.value),
      servicioId: Number(elements.servicioId.value),
      cliente,
      fechaHoraInicio,
      observaciones: normalizeNullableString(elements.turnoObservaciones.value),
    };
  }

  return {
    peluqueriaId: state.peluqueriaId,
    peluqueroId: Number(elements.peluqueroId.value),
    servicioId: Number(elements.servicioId.value),
    cliente,
    fechaHoraInicio,
    observaciones: normalizeNullableString(elements.turnoObservaciones.value),
  };
}

function buildStatusChip(status) {
  if (!status || status === "RESERVADO") {
    return "";
  }

  const statusClassByCode = {
    CANCELADO: "status-chip--cancelado",
    COMPLETADO: "status-chip--completado",
    AUSENTE: "status-chip--ausente",
    EN_CURSO: "status-chip--en-curso",
  };

  return `<span class="status-chip ${statusClassByCode[status] || ""}">${formatStatus(status)}</span>`;
}

function moveSelectedDate(days) {
  state.selectedDate = addDays(state.selectedDate, days);
  syncQueryDate();
  renderHeader();
  renderDateSelector();
  loadTurnos();
}

function populateSelect(selectElement, collection, valueSelector, labelSelector) {
  selectElement.innerHTML = collection
    .map((item) => `<option value="${valueSelector(item)}">${escapeHtml(labelSelector(item))}</option>`)
    .join("");
}

function setLoading(isLoading) {
  state.loading = isLoading;
  elements.loadingState.classList.toggle("hidden", !isLoading);
  if (isLoading) {
    elements.appointmentsList.innerHTML = "";
    elements.emptyState.classList.add("hidden");
  }
}

function setFormSaving(isSaving) {
  state.saving = isSaving;
  elements.saveTurnoButton.disabled = isSaving;
  elements.saveTurnoButton.textContent = isSaving
    ? "Guardando..."
    : state.modalMode === "edit"
      ? "Guardar cambios"
      : "Guardar turno";
}

function setPeluqueroSaving(isSaving) {
  elements.savePeluqueroButton.disabled = isSaving;
  elements.savePeluqueroButton.textContent = isSaving ? "Guardando..." : "Guardar peluquero";
}

function showError(message) {
  elements.errorMessage.textContent = message;
  elements.errorState.classList.remove("hidden");
  elements.emptyState.classList.add("hidden");
}

function hideError() {
  elements.errorState.classList.add("hidden");
}

function showFormError(message) {
  elements.formError.textContent = message;
  elements.formError.classList.remove("hidden");
}

function showPeluqueroFormError(message) {
  elements.peluqueroFormError.textContent = message;
  elements.peluqueroFormError.classList.remove("hidden");
}

function hideFormError() {
  elements.formError.classList.add("hidden");
  elements.formError.textContent = "";
}

function hidePeluqueroFormError() {
  elements.peluqueroFormError.classList.add("hidden");
  elements.peluqueroFormError.textContent = "";
}

function showModal() {
  elements.modalShell.classList.remove("hidden");
  elements.modalShell.setAttribute("aria-hidden", "false");
}

function closeModal() {
  elements.modalShell.classList.add("hidden");
  elements.modalShell.setAttribute("aria-hidden", "true");
  resetForm();
}

function closePeluqueroModal() {
  elements.peluqueroModalShell.classList.add("hidden");
  elements.peluqueroModalShell.setAttribute("aria-hidden", "true");
  elements.peluqueroForm.reset();
  hidePeluqueroFormError();
}

function resetForm() {
  elements.form.reset();
  hideFormError();
}

function normalizeDateFromQuery() {
  const value = queryParams.get("fecha");
  return value ? parseDate(value) : stripTime(new Date());
}

function syncQueryDate() {
  const url = new URL(window.location.href);
  url.searchParams.set("fecha", formatDateForApi(state.selectedDate));
  url.searchParams.set("peluqueriaId", String(state.peluqueriaId));
  window.history.replaceState({}, "", url);
}

function parseDate(value) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function addDays(date, amount) {
  const result = new Date(date);
  result.setDate(result.getDate() + amount);
  return stripTime(result);
}

function stripTime(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function formatDateForApi(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatWeekdayShort(date) {
  return new Intl.DateTimeFormat("es-AR", { weekday: "short" })
    .format(date)
    .replace(".", "")
    .slice(0, 3);
}

function formatTime(value) {
  return new Intl.DateTimeFormat("es-AR", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(new Date(value));
}

function formatTimeForInput(value) {
  const date = new Date(value);
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
}

function suggestTime() {
  const base = new Date(state.selectedDate);
  const now = new Date();

  if (isSameDay(base, now)) {
    base.setHours(now.getHours(), now.getMinutes() + 30, 0, 0);
  } else {
    base.setHours(10, 0, 0, 0);
  }

  const minutes = base.getMinutes();
  const remainder = minutes % 15;
  if (remainder !== 0) {
    base.setMinutes(minutes + (15 - remainder));
  }

  return `${String(base.getHours()).padStart(2, "0")}:${String(base.getMinutes()).padStart(2, "0")}`;
}

function normalizeTime(value) {
  const normalized = value.trim();
  if (!/^\d{2}:\d{2}$/.test(normalized)) {
    throw new Error("hora: ingresá la hora con formato 24 hs, por ejemplo 14:30.");
  }

  const [hour, minutes] = normalized.split(":").map(Number);
  if (hour < 0 || hour > 23 || minutes < 0 || minutes > 59) {
    throw new Error("hora: ingresá una hora valida entre 00:00 y 23:59.");
  }

  return `${normalized}:00`;
}

function normalizeNullableString(value) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function buildJsonHeaders() {
  const headers = {
    "Content-Type": "application/json",
  };

  const csrfToken = getCookie("XSRF-TOKEN");
  if (csrfToken) {
    headers["X-XSRF-TOKEN"] = decodeURIComponent(csrfToken);
  }

  return headers;
}

function getCookie(name) {
  const cookies = document.cookie ? document.cookie.split("; ") : [];
  const prefix = `${name}=`;

  for (const cookie of cookies) {
    if (cookie.startsWith(prefix)) {
      return cookie.slice(prefix.length);
    }
  }

  return null;
}

function formatStatus(status) {
  return status
    .toLowerCase()
    .split("_")
    .map((part) => capitalize(part))
    .join(" ");
}

function isSameDay(left, right) {
  return formatDateForApi(left) === formatDateForApi(right);
}

function capitalize(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

async function extractErrorMessage(response) {
  try {
    const data = await response.json();
    if (data.details?.length) {
      return `${data.message} ${data.details.join(" ")}`.trim();
    }
    return data.message || "Ocurrio un error en el servidor.";
  } catch (_error) {
    return "Ocurrio un error en el servidor.";
  }
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function handleHoraInput(event) {
  const digits = event.target.value.replace(/\D/g, "").slice(0, 4);

  if (digits.length <= 2) {
    event.target.value = digits;
    return;
  }

  event.target.value = `${digits.slice(0, 2)}:${digits.slice(2)}`;
}

function centerActiveDatePill() {
  const activePill = elements.dateList.querySelector(".date-pill--active");
  if (!activePill) {
    return;
  }

  elements.dateList.scrollLeft = 0;

  requestAnimationFrame(() => {
    activePill.scrollIntoView({
      inline: "center",
      block: "nearest",
      behavior: "auto",
    });
  });
}

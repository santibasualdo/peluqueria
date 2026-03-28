const passwordForm = document.getElementById("password-form");
const passwordFeedback = document.getElementById("password-feedback");
const newPasswordInput = document.getElementById("new-password");
const confirmPasswordInput = document.getElementById("confirm-password");
const passwordSubmitButton = document.getElementById("password-submit");

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

function showFeedback(message) {
    passwordFeedback.textContent = message;
    passwordFeedback.classList.remove("hidden");
}

function hideFeedback() {
    passwordFeedback.textContent = "";
    passwordFeedback.classList.add("hidden");
}

async function extractErrorMessage(response) {
    try {
        const data = await response.json();
        if (data.details?.length) {
            return `${data.message} ${data.details.join(" ")}`.trim();
        }
        return data.message || "No pudimos actualizar la contrasena.";
    } catch (_error) {
        return "No pudimos actualizar la contrasena.";
    }
}

passwordForm?.addEventListener("submit", async (event) => {
    event.preventDefault();
    hideFeedback();

    const nuevaPassword = newPasswordInput.value.trim();
    const confirmarPassword = confirmPasswordInput.value.trim();

    if (nuevaPassword.length < 6) {
        showFeedback("La contrasena debe tener al menos 6 caracteres.");
        return;
    }

    if (nuevaPassword !== confirmarPassword) {
        showFeedback("La confirmacion de la contrasena no coincide.");
        return;
    }

    passwordSubmitButton.disabled = true;
    passwordSubmitButton.textContent = "Guardando...";

    try {
        const response = await fetch("/api/v1/peluqueros/me/password", {
            method: "POST",
            headers: buildJsonHeaders(),
            body: JSON.stringify({
                nuevaPassword,
                confirmarPassword,
            }),
        });

        if (!response.ok) {
            throw new Error(await extractErrorMessage(response));
        }

        window.location.href = "/turnos";
    } catch (error) {
        showFeedback(error.message || "No pudimos actualizar la contrasena.");
    } finally {
        passwordSubmitButton.disabled = false;
        passwordSubmitButton.textContent = "Guardar y entrar";
    }
});

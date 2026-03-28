const params = new URLSearchParams(window.location.search);
const feedback = document.getElementById("login-feedback");
const loginForm = document.getElementById("login-form");
const togglePasswordButton = document.getElementById("toggle-password");
const passwordInput = document.getElementById("password");
const submittedFlag = "turnero-login-submitted";

function getCookie(name) {
    const prefix = `${name}=`;
    return document.cookie
        .split(";")
        .map((item) => item.trim())
        .find((item) => item.startsWith(prefix))
        ?.slice(prefix.length);
}

function ensureCsrfField(form) {
    if (!form) {
        return;
    }

    const csrfToken = getCookie("XSRF-TOKEN");
    if (!csrfToken) {
        return;
    }

    let csrfInput = form.querySelector('input[name="_csrf"]');
    if (!csrfInput) {
        csrfInput = document.createElement("input");
        csrfInput.type = "hidden";
        csrfInput.name = "_csrf";
        form.appendChild(csrfInput);
    }

    csrfInput.value = decodeURIComponent(csrfToken);
}

ensureCsrfField(loginForm);

if (loginForm) {
    loginForm.addEventListener("submit", () => {
        ensureCsrfField(loginForm);
        sessionStorage.setItem(submittedFlag, "1");
    });
}

if (feedback) {
    const wasSubmittedHere = sessionStorage.getItem(submittedFlag) === "1";

    if (params.has("error") && wasSubmittedHere) {
        feedback.hidden = false;
        feedback.classList.add("is-error");
        feedback.textContent = "Usuario o contrasena incorrectos. Vuelve a intentarlo.";
        sessionStorage.removeItem(submittedFlag);
    } else if (params.has("logout")) {
        feedback.hidden = false;
        feedback.classList.add("is-success");
        feedback.textContent = "Sesion cerrada correctamente.";
        sessionStorage.removeItem(submittedFlag);
    } else {
        sessionStorage.removeItem(submittedFlag);
    }

    if (params.has("error") && !wasSubmittedHere) {
        window.history.replaceState({}, document.title, "/login");
    }
}

if (togglePasswordButton && passwordInput) {
    togglePasswordButton.addEventListener("click", () => {
        const showing = passwordInput.type === "text";
        passwordInput.type = showing ? "password" : "text";
        togglePasswordButton.textContent = showing ? "Ver" : "Ocultar";
        togglePasswordButton.setAttribute("aria-pressed", String(!showing));
        togglePasswordButton.setAttribute("aria-label", showing ? "Mostrar contrasena" : "Ocultar contrasena");
    });
}

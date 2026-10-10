(() => {
  "use strict";

  const API = "/risk-api";
  const $ = (selector, root = document) => root.querySelector(selector);
  const authView = $("#authView");
  const dashboardView = $("#dashboardView");
  const screenLoader = $("#screenLoader");
  const authForm = $("#authForm");
  const paymentForm = $("#paymentForm");
  const authState = { mode: "login", busy: false };
  let currentUser = null;

  async function request(path, options = {}) {
    const config = {
      credentials: "same-origin",
      ...options,
      headers: { ...(options.headers || {}) }
    };
    if (options.body && typeof options.body !== "string") {
      config.headers["Content-Type"] = "application/json";
      config.body = JSON.stringify(options.body);
    }
    const response = await fetch(`${API}${path}`, config);
    if (response.status === 204) return null;
    const contentType = response.headers.get("content-type") || "";
    const body = contentType.includes("application/json")
      ? await response.json().catch(() => null)
      : await response.text().catch(() => "");
    if (!response.ok) {
      const error = new Error(
        (body && typeof body === "object" && (body.message || body.error || body.detail)) ||
        (typeof body === "string" && body.trim()) ||
        (response.status === 401 ? "Your session has ended. Please sign in again." : "Something went wrong. Please try again.")
      );
      error.status = response.status;
      throw error;
    }
    return body;
  }

  function hideLoader() {
    screenLoader.classList.add("is-gone");
    window.setTimeout(() => { screenLoader.hidden = true; }, 220);
  }

  function showAuth(message) {
    currentUser = null;
    dashboardView.hidden = true;
    authView.hidden = false;
    hideLoader();
    if (message) showAlert($("#authError"), message);
  }

  function showDashboard(user) {
    currentUser = user;
    authView.hidden = true;
    dashboardView.hidden = false;
    $("#userName").textContent = user.name || user.email || "Account";
    const source = (user.name || user.email || "CL").trim().split(/\s+/);
    $("#userInitials").textContent = source.length > 1
      ? `${source[0][0]}${source[1][0]}`.toUpperCase()
      : source[0].slice(0, 2).toUpperCase();
    hideLoader();
    loadWorkspace();
  }

  function showAlert(element, message) {
    element.textContent = message;
    element.hidden = false;
  }

  function clearAlert(element) {
    element.textContent = "";
    element.hidden = true;
  }

  function setBusy(button, busy) {
    button.disabled = busy;
    button.classList.toggle("is-loading", busy);
  }

  function clearErrors(form) {
    form.querySelectorAll(".field").forEach(field => field.classList.remove("has-error"));
    form.querySelectorAll(".field-error").forEach(error => { error.textContent = ""; });
  }

  function setFieldError(form, name, message) {
    const input = form.elements.namedItem(name);
    const field = input && input.closest(".field");
    if (!field) return;
    field.classList.add("has-error");
    const error = field.querySelector(`[data-error="${name}"]`);
    if (error) error.textContent = message;
  }

  function setAuthMode(mode) {
    authState.mode = mode;
    const registering = mode === "register";
    $("#loginTab").classList.toggle("active", !registering);
    $("#registerTab").classList.toggle("active", registering);
    $("#loginTab").setAttribute("aria-selected", String(!registering));
    $("#registerTab").setAttribute("aria-selected", String(registering));
    $("#nameField").hidden = !registering;
    $("#authName").required = registering;
    $("#authPassword").autocomplete = registering ? "new-password" : "current-password";
    $("#authTitle").textContent = registering ? "Create your account" : "Welcome back";
    $("#authSubtitle").textContent = registering
      ? "Set up a workspace for clear payment reviews."
      : "Sign in to review your payment activity.";
    $(".button-label", $("#authSubmit")).textContent = registering ? "Create account" : "Sign in";
    $("#authError").hidden = true;
    clearErrors(authForm);
  }

  function validateAuth() {
    clearErrors(authForm);
    clearAlert($("#authError"));
    const email = $("#authEmail").value.trim();
    const password = $("#authPassword").value;
    let valid = true;
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setFieldError(authForm, "email", "Enter a valid email address.");
      valid = false;
    }
    if (password.length < 8) {
      setFieldError(authForm, "password", "Use at least 8 characters.");
      valid = false;
    }
    if (authState.mode === "register") {
      const name = $("#authName").value.trim();
      if (name.length < 2) {
        setFieldError(authForm, "name", "Enter your name (at least 2 characters).");
        valid = false;
      }
    }
    return valid;
  }

  authForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (authState.busy || !validateAuth()) return;
    authState.busy = true;
    setBusy($("#authSubmit"), true);
    try {
      const registering = authState.mode === "register";
      const payload = registering
        ? { name: $("#authName").value.trim(), email: $("#authEmail").value.trim(), password: $("#authPassword").value }
        : { email: $("#authEmail").value.trim(), password: $("#authPassword").value };
      const user = await request(`/auth/${registering ? "register" : "login"}`, {
        method: "POST",
        body: payload
      });
      currentUser = user && user.id ? user : await request("/auth/me");
      showDashboard(currentUser);
      authForm.reset();
    } catch (error) {
      showAlert($("#authError"), error.message || "We couldn't sign you in. Please try again.");
    } finally {
      authState.busy = false;
      setBusy($("#authSubmit"), false);
    }
  });
  $("#loginTab").addEventListener("click", () => setAuthMode("login"));
  $("#registerTab").addEventListener("click", () => setAuthMode("register"));

  $("#logoutButton").addEventListener("click", async event => {
    const button = event.currentTarget;
    button.disabled = true;
    try {
      await request("/auth/logout", { method: "POST" });
    } catch (error) {
      if (error.status !== 401) {
        showAlert($("#dashboardAlert"), error.message || "We couldn't sign you out. Please try again.");
        button.disabled = false;
        return;
      }
    }
    showAuth();
    setAuthMode("login");
  });

  async function loadWorkspace() {
    clearAlert($("#dashboardAlert"));
    await Promise.all([loadDashboard(), loadTransactions()]);
  }

  async function loadDashboard() {
    try {
      const dashboard = await request("/dashboard");
      $("#totalTransactions").textContent = numberValue(dashboard.totalTransactions);
      $("#approvedCount").textContent = numberValue(dashboard.approvedCount);
      $("#flaggedCount").textContent = numberValue(dashboard.flaggedCount);
      $("#blockedCount").textContent = numberValue(dashboard.blockedCount);
      const average = Number(dashboard.averageRiskScore);
      $("#averageRiskScore").textContent = Number.isFinite(average) ? Math.round(average) : "0";
    } catch (error) {
      if (error.status === 401) return showAuth("Please sign in to continue.");
      showAlert($("#dashboardAlert"), `Your overview couldn't be loaded. ${error.message}`);
    }
  }

  function numberValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? new Intl.NumberFormat().format(number) : "0";
  }

  async function loadTransactions() {
    const host = $("#historyContent");
    host.innerHTML = '<div class="history-loading" aria-label="Loading payment history"><div class="skeleton-line w-65"></div><div class="skeleton-line w-95"></div><div class="skeleton-line w-80"></div></div>';
    try {
      const transactions = await request("/transactions");
      renderTransactions(Array.isArray(transactions) ? transactions : []);
    } catch (error) {
      if (error.status === 401) return showAuth("Please sign in to continue.");
      host.innerHTML = "";
      const panel = document.createElement("div");
      panel.className = "history-error";
      const title = document.createElement("strong");
      title.textContent = "History is having a moment";
      const message = document.createElement("p");
      message.textContent = error.message || "We couldn't load your saved payments.";
      const retry = document.createElement("button");
      retry.className = "retry-button";
      retry.type = "button";
      retry.textContent = "Try again";
      retry.addEventListener("click", loadTransactions);
      panel.append(title, message, retry);
      host.append(panel);
      $("#historyCount").textContent = "—";
    }
  }

  function renderTransactions(transactions) {
    const host = $("#historyContent");
    host.replaceChildren();
    $("#historyCount").textContent = transactions.length > 99 ? "99+" : String(transactions.length);
    if (!transactions.length) {
      const empty = document.createElement("div");
      empty.className = "empty-state";
      empty.innerHTML = '<span class="empty-art" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none"><rect x="3.5" y="5" width="17" height="14" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M4 9.5h16" stroke="currentColor" stroke-width="1.5"/><path d="M7 14h4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg></span><strong>Your history starts here</strong><p>Once you review a payment, its decision and risk score will show up here.</p>';
      host.append(empty);
      return;
    }
    const list = document.createElement("div");
    list.className = "history-list";
    transactions.forEach(transaction => {
      const row = document.createElement("article");
      row.className = "history-row";
      const main = document.createElement("div");
      main.className = "history-main";
      main.innerHTML = '<span class="history-icon" aria-hidden="true"><svg viewBox="0 0 20 20" fill="none"><rect x="2.5" y="4" width="15" height="12" rx="2" stroke="currentColor" stroke-width="1.4"/><path d="M3 8h14" stroke="currentColor" stroke-width="1.4"/><path d="M6 12h3" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg></span>';
      const copy = document.createElement("div");
      copy.className = "history-copy";
      const amount = document.createElement("div");
      amount.className = "history-amount";
      const parsedAmount = Number(transaction.amount);
      amount.textContent = Number.isFinite(parsedAmount)
        ? new Intl.NumberFormat(undefined, { style: "currency", currency: transaction.currency || "USD" }).format(parsedAmount)
        : String(transaction.amount ?? "Payment");
      const meta = document.createElement("div");
      meta.className = "history-meta";
      meta.textContent = [transaction.location, transaction.device, formatDate(transaction.transactionTime || transaction.createdAt)].filter(Boolean).join(" · ");
      copy.append(amount, meta);
      main.append(copy);
      const result = document.createElement("div");
      result.className = "history-result";
      const status = document.createElement("span");
      const normalizedStatus = String(transaction.status || "").toLowerCase();
      status.className = `status-badge ${normalizedStatus}`;
      status.textContent = transaction.status || "Reviewed";
      const score = document.createElement("span");
      score.className = "history-score";
      const scoreValue = Number(transaction.riskScore);
      score.textContent = Number.isFinite(scoreValue) ? `Risk ${Math.round(scoreValue)}/100` : "Risk score —";
      result.append(status, score);
      row.append(main, result);
      list.append(row);
    });
    host.append(list);
  }

  function formatDate(value) {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" }).format(date);
  }

  function validatePayment() {
    clearErrors(paymentForm);
    clearAlert($("#paymentError"));
    const amount = Number($("#amount").value);
    let valid = true;
    if (!Number.isFinite(amount) || amount <= 0) {
      setFieldError(paymentForm, "amount", "Enter an amount greater than zero.");
      valid = false;
    }
    if (!$("#location").value.trim()) {
      setFieldError(paymentForm, "location", "Enter the payment location.");
      valid = false;
    }
    if (!$("#device").value) {
      setFieldError(paymentForm, "device", "Choose the device used.");
      valid = false;
    }
    if (!$("#paymentTime").value) {
      setFieldError(paymentForm, "time", "Choose when the payment happened.");
      valid = false;
    }
    if (!$("#frequency").value) {
      setFieldError(paymentForm, "frequency", "Choose a recent payment frequency.");
      valid = false;
    }
    return valid;
  }

  paymentForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (!validatePayment()) return;
    const button = $("#paymentSubmit");
    setBusy(button, true);
    $("#decisionResult").hidden = true;
    const payload = {
      amount: Number($("#amount").value),
      location: $("#location").value.trim(),
      device: $("#device").value,
      time: $("#paymentTime").value,
      frequency: Number($("#frequency").value)
    };
    try {
      const transaction = await request("/transactions", { method: "POST", body: payload });
      renderDecision(transaction);
      paymentForm.reset();
      clearErrors(paymentForm);
      await loadWorkspace();
    } catch (error) {
      if (error.status === 401) {
        showAuth("Your session has ended. Please sign in again.");
      } else {
        showAlert($("#paymentError"), error.message || "We couldn't review this payment. Check your details and try again.");
      }
    } finally {
      setBusy(button, false);
    }
  });

  function renderDecision(transaction) {
    const panel = $("#decisionResult");
    const status = String(transaction.status || "Reviewed");
    const statusClass = status.toLowerCase();
    panel.className = `decision-result status-${statusClass}`;
    panel.replaceChildren();
    const top = document.createElement("div");
    top.className = "result-top";
    const copy = document.createElement("div");
    const title = document.createElement("div");
    title.className = "result-title";
    title.textContent = `Payment ${status.toLowerCase()}`;
    const subtitle = document.createElement("div");
    subtitle.className = "result-subtitle";
    subtitle.textContent = status === "Approved"
      ? "This payment meets the current low-risk rules."
      : status === "Flagged"
        ? "Some details stand out. A closer look is recommended."
        : "This payment matched high-risk rules.";
    copy.append(title, subtitle);
    const score = document.createElement("div");
    score.className = "result-score";
    const numericScore = Number(transaction.riskScore);
    score.append(document.createTextNode(Number.isFinite(numericScore) ? String(Math.round(numericScore)) : "—"));
    const scoreLabel = document.createElement("small");
    scoreLabel.textContent = "RISK / 100";
    score.append(scoreLabel);
    top.append(copy, score);
    panel.append(top);
    const reasons = Array.isArray(transaction.reasons) ? transaction.reasons : [];
    if (reasons.length) {
      const list = document.createElement("ul");
      list.className = "result-reasons";
      reasons.forEach(reason => {
        const item = document.createElement("li");
        const mark = document.createElement("span");
        mark.className = "reason-check";
        mark.setAttribute("aria-hidden", "true");
        mark.textContent = "✓";
        const text = document.createElement("span");
        text.textContent = typeof reason === "string" ? reason : (reason.message || reason.reason || "Payment rule evaluated.");
        item.append(mark, text);
        list.append(item);
      });
      panel.append(list);
    } else {
      const note = document.createElement("div");
      note.className = "result-foot";
      note.textContent = "No additional rule reasons were returned for this review.";
      panel.append(note);
    }
    const saved = document.createElement("div");
    saved.className = "result-foot";
    saved.textContent = "Saved to your payment history.";
    panel.append(saved);
    panel.hidden = false;
  }

  async function initialize() {
    setAuthMode("login");
    try {
      const user = await request("/auth/me");
      if (user && user.id) showDashboard(user);
      else showAuth();
    } catch (error) {
      if (error.status === 401) {
        showAuth();
      } else {
        showAuth("We couldn't connect to your workspace. Please refresh and try again.");
      }
    }
  }

  initialize();
})();

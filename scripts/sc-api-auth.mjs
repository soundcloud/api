#!/usr/bin/env node
/**
 * SoundCloud OAuth 2.1 (PKCE) + local callback, or remote pairing code flow, then POST /me/apps.
 * Node.js stdlib only (no npm dependencies).
 * @see https://developers.soundcloud.com/docs/api/guide#authentication
 */
import crypto from "node:crypto";
import { spawn } from "node:child_process";
import http from "node:http";
import { hostname } from "node:os";
import { basename } from "node:path";
import { parseArgs } from "node:util";
import process from "node:process";

const SCRIPT_NAME = basename(process.argv[1] ?? "sc-api-auth.mjs");

const SOUNDCLOUD_AUTHORIZE = "https://secure.soundcloud.com/authorize";
const SOUNDCLOUD_TOKEN = "https://secure.soundcloud.com/oauth/token";
const PAIRING_ACTIVATE_BASE = "https://secure.soundcloud.com/activate";
const APP_REGISTRATION_API = "https://api-reg.soundcloud.com";
const ME_APPS_URL = new URL("/me/apps", APP_REGISTRATION_API).toString();
const PAIRING_SIGN_IN_SCOPE = "";
const PAIRING_DEVICE_LABEL = "SoundCloud API CLI";

function pairingDeviceName() {
  const host = hostname();
  if (/\.local$/i.test(host)) return host;

  return PAIRING_DEVICE_LABEL;
}

const BUNDLED_REDIRECT_URI = "http://127.0.0.1:8765/callback";
const CALLBACK_HOST = "127.0.0.1";
const CALLBACK_PORT = 8765;
const CALLBACK_PATH = "/callback";

const CREATE_APP_ERROR_MESSAGES = {
  user_already_has_application: "You already have a registered application.",
  application_creation_not_available:
    "Application registration is not available for your account. You may need an Artist Pro subscription.",
  application_name_not_allowed: "This application name is not allowed.",
  application_website_not_allowed: "This website URL is not allowed.",
};

const UPDATE_REDIRECT_ERROR_MESSAGES = {
  application_redirect_not_allowed: "This redirect URI is not allowed.",
};

/** Public SoundCloud sign-in app for this CLI (OAuth + PKCE token exchange). Scoped to this tool only. */
const BUNDLED_CLIENT_ID = "nXIZT4VQQYkgHs75vpIYbnINQciCkV5Y";

const API_ERROR_DETAIL_MAX_LENGTH = 500;
const MAX_PAIRING_GATEWAY_TIMEOUTS = 5;

function truncateErrorDetail(text) {
  if (text.length <= API_ERROR_DETAIL_MAX_LENGTH) return text;
  return `${text.slice(0, API_ERROR_DETAIL_MAX_LENGTH)}…`;
}

function apiErrorDetail(text) {
  if (!text) return "";
  try {
    const data = JSON.parse(text);
    if (typeof data?.error === "string") return JSON.stringify({ error: data.error });
    const first = data?.errors?.[0];
    if (first) return JSON.stringify(first);
    return truncateErrorDetail(text);
  } catch {
    return truncateErrorDetail(text);
  }
}

function parseCliArgs(config) {
  try {
    return parseArgs(config);
  } catch (e) {
    console.error(e.message);
    process.exit(1);
  }
}

function parseStructuredApiError(text) {
  try {
    const first = JSON.parse(text).errors?.[0];
    if (!first) return null;
    return { code: first.code, error_message: first.error_message };
  } catch {
    return null;
  }
}

function createAppUserMessage({ code, error_message: apiMessage }) {
  if (apiMessage) return apiMessage;
  if (code && CREATE_APP_ERROR_MESSAGES[code]) return CREATE_APP_ERROR_MESSAGES[code];
  return "You are not allowed to create an application.";
}

function updateRedirectUserMessage({ code, error_message: apiMessage }) {
  if (apiMessage) return apiMessage;
  if (code && UPDATE_REDIRECT_ERROR_MESSAGES[code]) return UPDATE_REDIRECT_ERROR_MESSAGES[code];
  return "Could not update redirect URI.";
}

function validateAppRedirectUri(redirectUri) {
  const trimmed = redirectUri?.trim();
  if (!trimmed) {
    throw new Error("Missing required argument: --redirect-uri");
  }
  let url;
  try {
    url = new URL(trimmed);
  } catch {
    throw new Error("Invalid redirect URI: must be a valid absolute URL.");
  }
  if (url.protocol !== "http:" && url.protocol !== "https:") {
    throw new Error("Invalid redirect URI: only http and https URLs are supported.");
  }
  return trimmed;
}

function base64url(buf) {
  return buf
    .toString("base64")
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+/g, "");
}

function newPkcePair() {
  const codeVerifier = base64url(crypto.randomBytes(32));
  const codeChallenge = base64url(crypto.createHash("sha256").update(codeVerifier).digest());
  return { codeVerifier, codeChallenge };
}

function callbackPathVariants(callbackPath) {
  const p = callbackPath;
  if (p === "/") return [p];
  const withSlash = p.endsWith("/") ? p : `${p}/`;
  const noSlash = p.replace(/\/+$/, "") || "/";
  if (p === withSlash) return [noSlash, p];
  return [noSlash, withSlash];
}

function parseRequestQuery(req) {
  const u = new URL(req.url ?? "/", `http://${req.headers.host ?? "127.0.0.1"}`);
  return Object.fromEntries(u.searchParams.entries());
}

function parseRequestPathname(req) {
  const u = new URL(req.url ?? "/", `http://${req.headers.host ?? "127.0.0.1"}`);
  return u.pathname || "/";
}

async function readRequestBody(req) {
  const chunks = [];
  for await (const chunk of req) {
    chunks.push(chunk);
  }
  return Buffer.concat(chunks).toString("utf8");
}

function parseUrlEncodedBody(body) {
  if (!body) return {};
  return Object.fromEntries(new URLSearchParams(body).entries());
}

function readOAuthCallbackParams({ query, body }) {
  return {
    code: query.code ?? body.code,
    state: query.state ?? body.state,
    error: query.error ?? body.error,
    error_description: query.error_description ?? body.error_description,
  };
}

function sendHtml(res, status, body) {
  if (res.writableEnded) return;
  res.writeHead(status, { "content-type": "text/html; charset=utf-8" });
  res.end(body);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function openBrowser(url) {
  let command;
  let args;
  if (process.platform === "darwin") {
    command = "open";
    args = [url];
  } else if (process.platform === "win32") {
    command = "cmd";
    args = ["/c", "start", "", url];
  } else {
    command = "xdg-open";
    args = [url];
  }
  const child = spawn(command, args, { detached: true, stdio: "ignore" });
  child.once("error", () => {
    console.error("Could not open a browser automatically. Open the sign-in link printed above.");
  });
  child.unref();
}

async function tokenExchange({ clientId, code, codeVerifier, redirectUri }) {
  const body = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: clientId,
    redirect_uri: redirectUri,
    code_verifier: codeVerifier,
    code,
  });
  const res = await fetch(SOUNDCLOUD_TOKEN, {
    method: "POST",
    headers: {
      accept: "application/json; charset=utf-8",
      "content-type": "application/x-www-form-urlencoded",
    },
    body: body.toString(),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Token exchange failed: ${res.status} ${apiErrorDetail(text)}`);
  }
  return JSON.parse(text);
}

/**
 * App-registration API uses `Authorization: OAuth <access_token>`.
 */
async function soundcloudApiRequest({ accessToken, endpoint, method = "GET", body }) {
  const headers = {
    accept: "application/json; charset=utf-8",
    authorization: `OAuth ${accessToken}`,
  };
  if (body !== undefined) {
    headers["content-type"] = "application/json";
  }
  const res = await fetch(endpoint, {
    method,
    headers,
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });
  return { res, text: await res.text() };
}

async function listUserApps({ accessToken }) {
  const { res, text } = await soundcloudApiRequest({ accessToken, endpoint: ME_APPS_URL });
  if (!res.ok) {
    throw new Error(`List apps failed: ${res.status} ${apiErrorDetail(text)}`);
  }
  return JSON.parse(text);
}

function findUserApp(page, { notFoundMessage } = {}) {
  const apps = appsFromPage(page);
  if (apps.length === 0) {
    throw new Error(notFoundMessage ?? "No application credentials were found for your account.");
  }
  return apps[0];
}

function selectAppForUpdate(page, { clientId } = {}) {
  const apps = appsFromPage(page);
  if (apps.length === 0) {
    throw new Error("No registered application found. Run without update-redirect to register one.");
  }

  const normalizedId = clientId?.trim();
  if (normalizedId) {
    const app = apps.find((item) => String(item.client_id) === normalizedId);
    if (!app) {
      const ids = apps.map((a) => a.client_id).filter(Boolean);
      throw new Error(
        `No application with client_id ${JSON.stringify(normalizedId)}. Registered client_id values: ${ids.join(", ")}`
      );
    }
    return app;
  }

  if (apps.length > 1) {
    const ids = apps.map((a) => a.client_id).filter(Boolean);
    throw new Error(
      `Multiple applications found. Pass --client-id to choose one. Registered client_id values: ${ids.join(", ")}\n` +
        `Tip: run \`${SCRIPT_NAME} list-apps\` to inspect apps.`
    );
  }

  return apps[0];
}

/** PUT /me/apps/{urn} expects the credentials URN (e.g. soundcloud:credentials:456), not the application URN. */
function credentialsUrn(app) {
  if (typeof app?.credentials === "string") return app.credentials;
  return null;
}

function credentialsFromApp(app) {
  return {
    client_id: app.client_id,
    client_secret: app.client_secret,
    name: app.name,
    description: app.description,
    website: app.url,
    redirect_uri: app.redirect_uri,
  };
}

function credentialsFromAppsPage(page) {
  return credentialsFromApp(findUserApp(page));
}

function appsFromPage(page) {
  return (page?.collection ?? []).filter((item) => item?.client_id);
}

function listAppPublicFields(app) {
  const fields = {};
  for (const [key, value] of [
    ["client_id", app.client_id],
    ["name", app.name],
    ["description", app.description],
    ["website", app.url],
    ["redirect_uri", app.redirect_uri],
  ]) {
    if (value !== undefined && value !== null && value !== "") {
      fields[key] = value;
    }
  }
  return fields;
}

function formatListAppsOutput(apps) {
  return `${JSON.stringify(apps, null, 2)}\n`;
}

function printUpdateRedirectRequestDebug({ endpoint, body, urn, app, responseStatus, responseText }) {
  console.error("");
  console.error("Update redirect request (for debugging):");
  console.error(`  PUT ${endpoint}`);
  console.error(`  Body: ${JSON.stringify(body)}`);
  if (app) {
    console.error(`  Selected app client_id: ${app.client_id ?? "(none)"}`);
    console.error(`  Selected credentials URN: ${urn ?? "(none)"}`);
  }
  if (responseStatus !== undefined) {
    console.error(`  Response status: ${responseStatus}`);
  }
  if (responseText !== undefined && responseText !== "") {
    console.error(`  Response body: ${responseText}`);
  }
  console.error("");
  console.error("Equivalent curl (replace YOUR_ACCESS_TOKEN):");
  const escapedUrl = endpoint.replace(/'/g, "'\\''");
  const bodyJson = JSON.stringify(body);
  console.error(`  curl -sS -X PUT '${escapedUrl}' \\`);
  console.error(`    -H 'Authorization: OAuth YOUR_ACCESS_TOKEN' \\`);
  console.error(`    -H 'Content-Type: application/json' \\`);
  console.error(`    -d '${bodyJson}'`);
  console.error("");
}

function credentialsWithRedirectUri(app, redirectUri) {
  return { ...credentialsFromApp(app), redirect_uri: redirectUri };
}

function publicCredentialFields(credentials) {
  const fields = {};
  for (const key of ["client_id", "client_secret", "name", "description", "website", "redirect_uri"]) {
    if (credentials[key] !== undefined && credentials[key] !== null) {
      fields[key] = credentials[key];
    }
  }
  return fields;
}

function formatCredentialOutput(credentials) {
  const publicCredentials = publicCredentialFields(credentials);
  const lines = [`client_id=${publicCredentials.client_id}`];
  if (publicCredentials.client_secret) {
    lines.push(`client_secret=${publicCredentials.client_secret}`);
  }
  lines.push("", JSON.stringify(publicCredentials, null, 2), "");
  return lines.join("\n");
}

function pairingActivateUrl(code) {
  return `${PAIRING_ACTIVATE_BASE}/${encodeURIComponent(code)}`;
}

function appRegUrl(path) {
  return new URL(path, APP_REGISTRATION_API);
}

function appRegUrlWithParams(path, searchParams = {}) {
  const url = appRegUrl(path);
  for (const [key, value] of Object.entries(searchParams)) {
    url.searchParams.set(key, value);
  }
  return url;
}


function accessTokenFromPairingSignIn(data) {
  return data?.session?.access_token ?? data?.access_token ?? null;
}

async function createPairingCode({ clientId, device }) {
  const res = await fetch(appRegUrlWithParams("/pairing/codes", { client_id: clientId }), {
    method: "POST",
    headers: {
      accept: "application/json; charset=utf-8",
      "content-type": "application/json",
    },
    body: JSON.stringify({ device }),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Create pairing code failed: ${res.status} ${apiErrorDetail(text)}`);
  }
  const data = JSON.parse(text);
  if (!data.code || !data.poll_token) {
    throw new Error("Pairing code response missing code or poll_token.");
  }
  return data;
}

async function checkPairingCodeStatus({ clientId, code, pollToken }) {
  const res = await fetch(
    appRegUrlWithParams(`/pairing/codes/${encodeURIComponent(code)}`, {
      client_id: clientId,
      poll_token: pollToken,
    }),
    {
      method: "GET",
      headers: {
        accept: "application/json; charset=utf-8",
      },
    }
  );
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  return { res, text, data };
}

async function signInWithPairingCode({ clientId, code, pollToken }) {
  const res = await fetch(appRegUrlWithParams("/pairing/sign-in", { client_id: clientId }), {
    method: "POST",
    headers: {
      accept: "application/json; charset=utf-8",
      "content-type": "application/json",
    },
    body: JSON.stringify({
      client_id: clientId,
      pairing_code: code,
      poll_token: pollToken,
      scope: PAIRING_SIGN_IN_SCOPE,
    }),
  });
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }
  return { res, text, data };
}

async function waitForPairingActivation({ clientId, code, pollToken, pollIntervalSeconds, timeoutMs }) {
  const deadline = Date.now() + timeoutMs;
  const parsedInterval = Number.parseInt(String(pollIntervalSeconds ?? 3), 10);
  let intervalMs = Math.max(1000, (Number.isFinite(parsedInterval) ? parsedInterval : 3) * 1000);
  let gatewayTimeouts = 0;
  while (Date.now() < deadline) {
    const status = await checkPairingCodeStatus({ clientId, code, pollToken });
    if (status.res.status === 504) {
      gatewayTimeouts += 1;
      if (gatewayTimeouts >= MAX_PAIRING_GATEWAY_TIMEOUTS) {
        throw new Error("Pairing status repeatedly timed out. Try again later.");
      }
      await sleep(intervalMs);
      continue;
    }
    if (status.res.status === 404) {
      await sleep(intervalMs);
      continue;
    }
    if (!status.res.ok) {
      throw new Error(`Pairing status failed: ${status.res.status} ${apiErrorDetail(status.text)}`);
    }
    if (status.data?.status === "expired") {
      throw new Error("Pairing code expired. Run the CLI again to get a new code.");
    }
    if (status.data?.status !== "activated") {
      await sleep(intervalMs);
      continue;
    }
    const { res, text, data } = await signInWithPairingCode({ clientId, code, pollToken });
    if (res.status === 404) {
      throw new Error("Pairing code is no longer valid. Run the CLI again to get a new code.");
    }
    if (!res.ok) {
      throw new Error(`Pairing sign-in failed: ${res.status} ${apiErrorDetail(text)}`);
    }
    const accessToken = accessTokenFromPairingSignIn(data);
    if (!accessToken) {
      throw new Error("No access_token in pairing sign-in response.");
    }
    return { access_token: accessToken };
  }
  throw new Error("Timed out waiting for pairing code activation (10 minutes).");
}

async function authenticateRemote({ clientId }) {
  const device = {
    id: crypto.randomUUID(),
    type: "cli",
    name: pairingDeviceName(),
  };
  const pairing = await createPairingCode({ clientId, device });
  const activateUrl = pairingActivateUrl(pairing.code);
  console.log(`Sign in at:\n${activateUrl}\n`);
  console.log("Waiting for authorization...\n");
  try {
    openBrowser(activateUrl);
  } catch {
    /* user can open the URL manually */
  }
  return waitForPairingActivation({
    clientId,
    code: pairing.code,
    pollToken: pairing.poll_token,
    pollIntervalSeconds: pairing.poll_interval_seconds,
    timeoutMs: 10 * 60 * 1000,
  });
}

async function runRemotePairingFlow({ clientId, appName, appDescription, appWebsite }) {
  const tokens = await authenticateRemote({ clientId });
  return createOrFetchUserAppCredentials({
    accessToken: tokens.access_token,
    name: appName,
    description: appDescription,
    website: appWebsite,
  });
}

async function updateAppRedirectUri({ accessToken, urn, redirectUri, app }) {
  const body = { redirect_uri: redirectUri };
  const endpoint = appRegUrl(`/me/apps/${urn}`).toString();
  const { res, text } = await soundcloudApiRequest({
    accessToken,
    endpoint,
    method: "PUT",
    body,
  });
  if (!res.ok) {
    printUpdateRedirectRequestDebug({
      endpoint,
      body,
      urn,
      app,
      responseStatus: res.status,
      responseText: text,
    });
    const apiError = parseStructuredApiError(text);
    const userMessage = apiError ? updateRedirectUserMessage(apiError) : null;
    throw new Error(userMessage || `Update redirect failed: ${res.status} ${apiErrorDetail(text)}`);
  }
  const data = JSON.parse(text);
  return data.redirect_uri;
}

async function runListAppsFlow({ clientId, remote }) {
  const tokens = remote ? await authenticateRemote({ clientId }) : await authenticateLocal({ clientId });
  const page = await listUserApps({ accessToken: tokens.access_token });
  return appsFromPage(page).map(listAppPublicFields);
}

async function runUpdateRedirectFlow({ clientId, redirectUri, remote, targetClientId }) {
  const tokens = remote ? await authenticateRemote({ clientId }) : await authenticateLocal({ clientId });
  const page = await listUserApps({ accessToken: tokens.access_token });
  const app = selectAppForUpdate(page, { clientId: targetClientId });
  const urn = credentialsUrn(app);
  if (!urn) {
    throw new Error(
      "Credentials URN missing from API response (expected app.credentials as a string, e.g. soundcloud:credentials:…)."
    );
  }

  if (app.redirect_uri === redirectUri) {
    return { credentials: credentialsFromApp(app), updated: false };
  }

  const updatedRedirectUri = await updateAppRedirectUri({
    accessToken: tokens.access_token,
    urn,
    redirectUri,
    app,
  });
  return {
    credentials: credentialsWithRedirectUri(app, updatedRedirectUri ?? redirectUri),
    updated: true,
    notice: "Updated redirect URI.",
  };
}

async function createOrFetchUserAppCredentials({ accessToken, name, description, website }) {
  const { res, text } = await soundcloudApiRequest({
    accessToken,
    endpoint: ME_APPS_URL,
    method: "POST",
    body: { name, description, website },
  });

  if (res.status === 201) {
    return { credentials: JSON.parse(text), existing: false };
  }

  if (res.status === 403) {
    const apiError = parseStructuredApiError(text);
    if (apiError?.code === "user_already_has_application") {
      const page = await listUserApps({ accessToken });
      return {
        credentials: credentialsFromAppsPage(page),
        existing: true,
        notice: createAppUserMessage(apiError),
      };
    }
    throw new Error(createAppUserMessage(apiError ?? {}));
  }

  throw new Error(`Create app failed: ${res.status} ${apiErrorDetail(text)}`);
}

function mainDoc() {
  return `<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>SoundCloud</title>
<style>body{font-family:system-ui,sans-serif;max-width:36em;margin:3em auto;padding:0 1em;}</style>
</head><body><h1>Done</h1><p>You can close this tab and return to the terminal.</p></body></html>`;
}

function errorDoc(msg) {
  return `<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>Error</title>
<style>body{font-family:system-ui,sans-serif;max-width:36em;margin:3em auto;padding:0 1em;}</style>
</head><body><h1>Authorization error</h1><p>${escapeHtml(msg)}</p></body></html>`;
}

function escapeHtml(s) {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function printRegisterHelp() {
  console.log(`Usage: ${SCRIPT_NAME} [options]
       ${SCRIPT_NAME} list-apps [options]
       ${SCRIPT_NAME} update-redirect [options]

Commands:
  (default)         Sign in and register or fetch your API application
  list-apps         Sign in and list your API applications (no client_secret)
  update-redirect   Sign in and update your application's OAuth redirect URI

Register options:
  --name            Required. App name for POST /me/apps
  --description     Required. App description for POST /me/apps
  --website         Required. App website URL for POST /me/apps
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help        Print usage and exit

List apps options:
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help        Print usage and exit

Update redirect options:
  --redirect-uri    Required. OAuth redirect URI for your API application
  --client-id       Required when you have multiple apps; use list-apps to find it
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help        Print usage and exit

  Sign-in always uses a fixed local callback (${BUNDLED_REDIRECT_URI}) or, with
  --remote, ${PAIRING_ACTIVATE_BASE}/<code>. That is separate from your app's
  redirect URI, which you set with update-redirect.

  With npm, pass a double dash before flags: npm start -- --name "…" …
`);
}

function printUpdateRedirectHelp() {
  console.log(`Usage: ${SCRIPT_NAME} update-redirect [options]

  Signs you in to SoundCloud, then calls PUT /me/apps/<credentials-urn> to update your
  application's OAuth redirect URI. Only redirect_uri is changed; client_id and
  client_secret are re-printed from your existing app.

Options:
  --redirect-uri    Required. OAuth redirect URI for your API application
  --client-id       Required when you have multiple apps; use list-apps to find it
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help        Print usage and exit

  Sign-in uses a fixed local callback (${BUNDLED_REDIRECT_URI}) or, with
  --remote, ${PAIRING_ACTIVATE_BASE}/<code>.

  On failure, prints the PUT URL, body, and a curl template to stderr.

  With npm: npm start -- update-redirect --redirect-uri "https://example.com/callback"
  Multiple apps: npm start -- update-redirect --client-id "…" --redirect-uri "https://example.com/callback"
`);
}

function printListAppsHelp() {
  console.log(`Usage: ${SCRIPT_NAME} list-apps [options]

  Signs you in to SoundCloud, then calls GET /me/apps and prints your API
  applications as JSON. client_secret is never included.

Options:
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help        Print usage and exit

  Sign-in uses a fixed local callback (${BUNDLED_REDIRECT_URI}) or, with
  --remote, ${PAIRING_ACTIVATE_BASE}/<code>.

  With npm: npm start -- list-apps
`);
}

function authenticateLocal({ clientId }) {
  const { codeVerifier, codeChallenge } = newPkcePair();
  const state = base64url(crypto.randomBytes(24));
  const callbackPaths = new Set(callbackPathVariants(CALLBACK_PATH));
  let server;
  let callbackHandled = false;
  let idle;

  return new Promise((resolve, reject) => {
    const finish = (err, result) => {
      if (callbackHandled) return;
      callbackHandled = true;
      clearTimeout(idle);
      server?.close();
      if (err) reject(err);
      else resolve(result);
    };

    const handleOAuthCallback = async (req, res) => {
      try {
        const query = parseRequestQuery(req);
        const rawBody = req.method === "POST" ? await readRequestBody(req) : "";
        const body = parseUrlEncodedBody(rawBody);
        const { code, state: st, error, error_description: ed } = readOAuthCallbackParams({ query, body });
        if (error) {
          sendHtml(res, 400, errorDoc(String(ed || error)));
          finish(new Error(String(ed || error)));
          return;
        }
        if (typeof code !== "string" || !code) {
          sendHtml(res, 400, errorDoc("Missing code in callback (query or POST body)."));
          finish(new Error("Missing authorization code."));
          return;
        }
        if (st !== state) {
          sendHtml(res, 400, errorDoc("Invalid state parameter (CSRF)."));
          finish(new Error("State mismatch."));
          return;
        }
        const tokens = await tokenExchange({
          clientId,
          code,
          codeVerifier,
          redirectUri: BUNDLED_REDIRECT_URI,
        });
        if (!tokens.access_token) {
          finish(new Error("No access_token in token response"));
          return;
        }
        sendHtml(res, 200, mainDoc());
        finish(null, tokens);
      } catch (e) {
        sendHtml(res, 500, errorDoc(e.message));
        finish(e);
      }
    };

    server = http.createServer((req, res) => {
      const pathname = parseRequestPathname(req);
      if (!callbackPaths.has(pathname)) {
        sendHtml(res, 404, errorDoc("Not found."));
        return;
      }
      if (req.method !== "GET" && req.method !== "POST") {
        sendHtml(res, 405, errorDoc("Method not allowed."));
        return;
      }
      void handleOAuthCallback(req, res);
    });

    server.listen(CALLBACK_PORT, CALLBACK_HOST, () => {
      const params = new URLSearchParams({
        client_id: clientId,
        redirect_uri: BUNDLED_REDIRECT_URI,
        response_type: "code",
        code_challenge: codeChallenge,
        code_challenge_method: "S256",
        state,
      });
      const authUrl = `${SOUNDCLOUD_AUTHORIZE}?${params.toString()}`;
      console.log("Starting browser login. If it does not open, visit this URL:\n" + authUrl);
      idle = setTimeout(
        () => finish(new Error("Timed out waiting for SoundCloud callback (10 minutes).")),
        10 * 60 * 1000
      );
      try {
        openBrowser(authUrl);
      } catch {
        /* user can paste URL */
      }
    });

    server.on("error", (e) => {
      finish(new Error(`Could not start local server: ${e.message}`));
    });
  });
}

async function runLocalAuthFlow({ clientId, appName, appDescription, appWebsite }) {
  const tokens = await authenticateLocal({ clientId });
  return createOrFetchUserAppCredentials({
    accessToken: tokens.access_token,
    name: appName,
    description: appDescription,
    website: appWebsite,
  });
}

function printCredentialResult(result) {
  if (result.notice) {
    console.error(result.notice);
  }
  process.stdout.write(formatCredentialOutput(result.credentials));
}

const CLI_SUBCOMMANDS = new Set(["list-apps", "update-redirect"]);

function runCli() {
  const rawArgs = process.argv.slice(2);
  const subcommand = CLI_SUBCOMMANDS.has(rawArgs[0]) ? rawArgs[0] : null;
  const argsToParse = subcommand ? rawArgs.slice(1) : rawArgs;

  if (!subcommand && rawArgs[0] && !rawArgs[0].startsWith("-")) {
    console.error(`Unknown command: ${rawArgs[0]}`);
    console.error(`Run ${SCRIPT_NAME} --help for usage.`);
    process.exit(1);
  }

  if (subcommand === "list-apps") {
    const {
      values: { remote: remoteArg, help: helpArg },
      positionals,
    } = parseCliArgs({
      args: argsToParse,
      options: {
        remote: { type: "boolean" },
        help: { type: "boolean", short: "h" },
      },
      strict: true,
      allowPositionals: true,
    });

    if (positionals.length > 0) {
      console.error(`Unexpected extra argument(s): ${positionals.map((p) => JSON.stringify(p)).join(" ")}`);
      process.exit(1);
    }

    if (helpArg) {
      printListAppsHelp();
      process.exit(0);
    }

    runListAppsFlow({ clientId: BUNDLED_CLIENT_ID, remote: remoteArg })
      .then(
        (apps) => {
          process.stdout.write(formatListAppsOutput(apps));
        },
        (e) => {
          console.error("Error:", e?.message || e);
          process.exit(1);
        }
      )
      .then(() => process.exit(0));
    return;
  }

  if (subcommand === "update-redirect") {
    const {
      values: {
        "redirect-uri": redirectArg,
        "client-id": targetClientIdArg,
        remote: remoteArg,
        help: helpArg,
      },
      positionals,
    } = parseCliArgs({
      args: argsToParse,
      options: {
        "redirect-uri": { type: "string" },
        "client-id": { type: "string" },
        remote: { type: "boolean" },
        help: { type: "boolean", short: "h" },
      },
      strict: true,
      allowPositionals: true,
    });

    if (positionals.length > 0) {
      console.error(`Unexpected extra argument(s): ${positionals.map((p) => JSON.stringify(p)).join(" ")}`);
      process.exit(1);
    }

    if (helpArg) {
      printUpdateRedirectHelp();
      process.exit(0);
    }

    let redirectUri;
    try {
      redirectUri = validateAppRedirectUri(redirectArg);
    } catch (e) {
      console.error(e.message);
      console.error(`Example: node ${SCRIPT_NAME} update-redirect --redirect-uri "https://example.com/callback"`);
      process.exit(1);
    }

    runUpdateRedirectFlow({
      clientId: BUNDLED_CLIENT_ID,
      redirectUri,
      remote: remoteArg,
      targetClientId: targetClientIdArg,
    })
      .then(printCredentialResult, (e) => {
        console.error("Error:", e?.message || e);
        process.exit(1);
      })
      .then(() => process.exit(0));
    return;
  }

  const {
    values: { name: nameArg, description: descArg, website: siteArg, remote: remoteArg, help: helpArg },
    positionals,
  } = parseCliArgs({
    args: argsToParse,
    options: {
      name: { type: "string" },
      description: { type: "string" },
      website: { type: "string" },
      remote: { type: "boolean" },
      help: { type: "boolean", short: "h" },
    },
    strict: true,
    allowPositionals: true,
  });

  if (positionals.length > 0) {
    console.error(`Unexpected extra argument(s): ${positionals.map((p) => JSON.stringify(p)).join(" ")}`);
    console.error(
      "If you used npm, put a double dash before the script options so npm does not swallow them, e.g.:\n" +
        '  npm start -- --name "My App" --description "…" --website "https://example.com"'
    );
    process.exit(1);
  }

  if (helpArg) {
    printRegisterHelp();
    process.exit(0);
  }

  const appName = nameArg;
  const appDescription = descArg;
  const appWebsite = siteArg;
  if (!appName || !appDescription || !appWebsite) {
    console.error("Missing required arguments: --name, --description, --website");
    console.error(
      `Example: node ${SCRIPT_NAME} --name "My First API App" --description "Internal integration for uploads" --website "https://example.com"`
    );
    process.exit(1);
  }

  if (remoteArg) {
    runRemotePairingFlow({ clientId: BUNDLED_CLIENT_ID, appName, appDescription, appWebsite })
      .then(printCredentialResult, (e) => {
        console.error("Error:", e?.message || e);
        process.exit(1);
      })
      .then(() => process.exit(0));
  } else {
    runLocalAuthFlow({ clientId: BUNDLED_CLIENT_ID, appName, appDescription, appWebsite })
      .then(printCredentialResult, (e) => {
        console.error("Error:", e?.message || e);
        process.exit(1);
      })
      .then(() => process.exit(0));
  }
}

runCli();

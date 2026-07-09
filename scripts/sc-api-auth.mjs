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
import { parseArgs } from "node:util";
import process from "node:process";

const SOUNDCLOUD_AUTHORIZE = "https://secure.soundcloud.com/authorize";
const SOUNDCLOUD_TOKEN = "https://secure.soundcloud.com/oauth/token";
const PAIRING_ACTIVATE_BASE = "https://secure.soundcloud.com/activate";
const APP_REGISTRATION_API = "https://api-reg.soundcloud.com";
const ME_APPS_URL = new URL("/me/apps", APP_REGISTRATION_API).toString();
const PAIRING_CODES_URL = new URL("/pairing/codes", APP_REGISTRATION_API).toString();
const PAIRING_SIGN_IN_SCOPE = "";
const PAIRING_DEVICE_LABEL = "SoundCloud API CLI";

function pairingDeviceName() {
  const override = process.env.SC_API_AUTH_DEVICE_NAME?.trim();
  if (override) return override;

  const host = hostname();
  // Personal machines (e.g. MacBook) usually have a *.local hostname.
  if (/\.local$/i.test(host)) return host;

  // Cloud shells, CI runners, and containers often expose opaque hostnames.
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

/** Public SoundCloud sign-in app for this CLI (OAuth + PKCE token exchange). Scoped to this tool only. */
const BUNDLED_CLIENT_ID = "nXIZT4VQQYkgHs75vpIYbnINQciCkV5Y";

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
    throw new Error(`Token exchange (POST ${SOUNDCLOUD_TOKEN}) failed: ${res.status} ${text}`);
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
    throw new Error(`List apps (GET ${ME_APPS_URL}) failed: ${res.status} ${text}`);
  }
  return JSON.parse(text);
}

function credentialsFromAppsPage(page) {
  const app = page?.collection?.find((item) => item?.client_id);
  if (!app) {
    throw new Error("No application credentials were found for your account.");
  }
  return {
    client_id: app.client_id,
    client_secret: app.client_secret,
    name: app.name,
    description: app.description,
    website: app.url,
    redirect_uri: app.redirect_uri,
  };
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

const PAIRING_SIGN_IN_URL = new URL("/pairing/sign-in", APP_REGISTRATION_API).toString();

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
    throw new Error(`Create pairing code (POST ${PAIRING_CODES_URL}) failed: ${res.status} ${text}`);
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
  while (Date.now() < deadline) {
    const status = await checkPairingCodeStatus({ clientId, code, pollToken });
    if (status.res.status === 404) {
      await sleep(intervalMs);
      continue;
    }
    if (!status.res.ok) {
      throw new Error(`Pairing status (GET ${PAIRING_CODES_URL}/…) failed: ${status.res.status} ${status.text}`);
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
      throw new Error(`Pairing sign-in (POST ${PAIRING_SIGN_IN_URL}) failed: ${res.status} ${text}`);
    }
    const accessToken = accessTokenFromPairingSignIn(data);
    if (!accessToken) {
      throw new Error("No access_token in pairing sign-in response.");
    }
    return { access_token: accessToken };
  }
  throw new Error("Timed out waiting for pairing code activation (10 minutes).");
}

async function runRemotePairingFlow({ clientId, appName, appDescription, appWebsite }) {
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
  const tokens = await waitForPairingActivation({
    clientId,
    code: pairing.code,
    pollToken: pairing.poll_token,
    pollIntervalSeconds: pairing.poll_interval_seconds,
    timeoutMs: 10 * 60 * 1000,
  });
  return createOrFetchUserAppCredentials({
    accessToken: tokens.access_token,
    name: appName,
    description: appDescription,
    website: appWebsite,
  });
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

  throw new Error(`Create app (POST ${ME_APPS_URL}) failed: ${res.status} ${text}`);
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

const {
  values: { name: nameArg, description: descArg, website: siteArg, remote: remoteArg, help: helpArg },
  positionals,
} = parseArgs({
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
  console.log(`Usage: sc-api-auth [options]

  Signs you in to SoundCloud, then calls POST /me/apps and prints the client_id
  and client_secret.

  By default, opens the SoundCloud login page and receives the redirect on a
  local server. Use --remote for the pairing code flow (for agents and other
  environments without a local callback server).

Options:
  --name            Required. App name for POST /me/apps
  --description     Required. App description for POST /me/apps
  --website         Required. App website URL for POST /me/apps
  --remote          Use pairing code sign-in instead of a local callback server
  -h, --help

  Local OAuth sign-in uses a fixed callback (${BUNDLED_REDIRECT_URI}) registered
  for this tool. Remote sign-in opens ${PAIRING_ACTIVATE_BASE}/<code>.

  With npm, pass a double dash before these flags: npm start -- --name "…" …
`);
  process.exit(0);
}

const clientId = BUNDLED_CLIENT_ID;

const appName = nameArg;
const appDescription = descArg;
const appWebsite = siteArg;
if (!appName || !appDescription || !appWebsite) {
  console.error("Missing required arguments: --name, --description, --website");
  console.error('Example: node src/cli.mjs --name "My First API App" --description "Internal integration for uploads" --website "https://example.com"');
  process.exit(1);
}

function printCredentialResult(result) {
  if (result.existing && result.notice) {
    console.error(result.notice);
  }
  process.stdout.write(formatCredentialOutput(result.credentials));
}

if (remoteArg) {
  runRemotePairingFlow({ clientId, appName, appDescription, appWebsite })
    .then(printCredentialResult, (e) => {
      console.error("Error:", e?.message || e);
      process.exit(1);
    })
    .then(() => process.exit(0));
} else {
  runLocalAuthFlow({ clientId, appName, appDescription, appWebsite })
    .then(printCredentialResult, (e) => {
      console.error("Error:", e?.message || e);
      process.exit(1);
    })
    .then(() => process.exit(0));
}

function runLocalAuthFlow({ clientId, appName, appDescription, appWebsite }) {
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
        const result = await createOrFetchUserAppCredentials({
          accessToken: tokens.access_token,
          name: appName,
          description: appDescription,
          website: appWebsite,
        });
        sendHtml(res, 200, mainDoc());
        finish(null, result);
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

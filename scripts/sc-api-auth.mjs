#!/usr/bin/env node
/**
 * SoundCloud OAuth 2.1 (PKCE) + local callback, then POST /me/apps on the app-registration API.
 * Node.js stdlib only (no npm dependencies).
 * @see https://developers.soundcloud.com/docs/api/guide#authentication
 */
import crypto from "node:crypto";
import { spawn } from "node:child_process";
import http from "node:http";
import { parseArgs } from "node:util";
import process from "node:process";

const SOUNDCLOUD_AUTHORIZE = "https://secure.soundcloud.com/authorize";
const SOUNDCLOUD_TOKEN = "https://secure.soundcloud.com/oauth/token";
const APP_REGISTRATION_API = "https://api-reg.soundcloud.com";
const ME_APPS_URL = new URL("/me/apps", APP_REGISTRATION_API).toString();
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
  values: { name: nameArg, description: descArg, website: siteArg, help: helpArg },
  positionals,
} = parseArgs({
  options: {
    name: { type: "string" },
    description: { type: "string" },
    website: { type: "string" },
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

  Opens the SoundCloud login page, receives the redirect on a local server,
  then calls POST /me/apps and prints the client_id and client_secret.

Options:
  --name            Required. App name for POST /me/apps
  --description     Required. App description for POST /me/apps
  --website         Required. App website URL for POST /me/apps
  -h, --help

  OAuth sign-in uses a fixed local callback (${BUNDLED_REDIRECT_URI}) registered for this tool.

  With npm, pass a double dash before these flags: npm start -- --name "…" …
`);
  process.exit(0);
}

const clientId = BUNDLED_CLIENT_ID;

const { codeVerifier, codeChallenge } = newPkcePair();
const state = base64url(crypto.randomBytes(24));

const appName = nameArg;
const appDescription = descArg;
const appWebsite = siteArg;
if (!appName || !appDescription || !appWebsite) {
  console.error("Missing required arguments: --name, --description, --website");
  console.error('Example: node src/cli.mjs --name "My First API App" --description "Internal integration for uploads" --website "https://example.com"');
  process.exit(1);
}

const callbackPaths = new Set(callbackPathVariants(CALLBACK_PATH));
let server;
let callbackHandled = false;

const p = new Promise((resolve, reject) => {
  const finish = (err, result) => {
    if (callbackHandled) return;
    callbackHandled = true;
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
    const idle = setTimeout(
      () => {
        console.error("Timed out waiting for SoundCloud callback (10 minutes).");
        server?.close();
        process.exit(1);
      },
      10 * 60 * 1000
    );
    p.then(
      (result) => {
        clearTimeout(idle);
        if (result?.credentials) {
          if (result.existing && result.notice) {
            console.error(result.notice);
          }
          process.stdout.write(formatCredentialOutput(result.credentials));
        }
        server?.close();
        process.exit(0);
      },
      (e) => {
        clearTimeout(idle);
        console.error("Error:", e?.message || e);
        server?.close();
        process.exit(1);
      }
    );
    try {
      openBrowser(authUrl);
    } catch {
      /* user can paste URL */
    }
  });

  server.on("error", (e) => {
    console.error("Could not start local server:", e.message);
    process.exit(1);
  });
});

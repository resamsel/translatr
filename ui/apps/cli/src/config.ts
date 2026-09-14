import { existsSync, readFileSync, writeFileSync } from "node:fs";
import yaml from "js-yaml";
import { parse as parseDotenv } from "dotenv";

export const CONFIG_FILE = ".translatr.yml";
const DOTENV_FILE = ".env";

export interface FileSpec {
  file_type: string;
  target: string;
}

export interface TranslatrConfig {
  endpoint: string;
  access_token: string;
  project_id: string;
  default_locale: string;
  pull: FileSpec;
  push: FileSpec;
  /**
   * When true, load `.env` from the current directory (if present) before
   * `${VAR}`/`${?VAR}` substitution, filling in any variables not already
   * set in the process environment. Defaults to false/absent (no `.env`
   * file is read).
   */
  load_dotenv?: boolean;
  [key: string]: unknown;
}

function loadDotenvIfEnabled(raw: { load_dotenv?: unknown }): void {
  if (raw.load_dotenv !== true) return;
  if (!existsSync(DOTENV_FILE)) return;

  const parsed = parseDotenv(readFileSync(DOTENV_FILE, "utf8"));
  for (const [key, value] of Object.entries(parsed)) {
    if (process.env[key] === undefined) process.env[key] = value;
  }
}

// Mirrors the Python CLI's ${VAR} / ${?VAR} substitution: ${VAR} requires the
// env var to be set, ${?VAR} silently resolves to "" when it's unset.
const ENV_VAR_PATTERN = /^(.*?)\$\{(\?)?([^}]*)\}(.*)$/s;

function replaceEnvVars(value: string): string {
  const match = ENV_VAR_PATTERN.exec(value);
  if (!match) return value;
  const [, prefix, optional, envVar, suffix] = match;
  if (process.env[envVar] !== undefined) {
    return prefix + process.env[envVar] + replaceEnvVars(suffix);
  }
  if (!optional) {
    throw new Error(`Environment variable ${envVar} is not set`);
  }
  return prefix + replaceEnvVars(suffix);
}

function substitute(node: unknown): unknown {
  if (typeof node === "string") return replaceEnvVars(node);
  if (Array.isArray(node)) return node.map(substitute);
  if (node && typeof node === "object") {
    const out: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(node)) out[k] = substitute(v);
    return out;
  }
  return node;
}

export class ConfigError extends Error {}

export function readConfig(): TranslatrConfig {
  if (!existsSync(CONFIG_FILE)) {
    throw new ConfigError(
      `Could not find ${CONFIG_FILE}: initialise with \`translatr init\``,
    );
  }
  const raw = yaml.load(readFileSync(CONFIG_FILE, "utf8")) as
    | { translatr?: { load_dotenv?: unknown; [key: string]: unknown } }
    | undefined;
  if (!raw || raw.translatr === undefined) {
    throw new ConfigError(`Error in ${CONFIG_FILE}: could not find key "translatr"`);
  }
  loadDotenvIfEnabled(raw.translatr);
  return substitute(raw.translatr) as TranslatrConfig;
}

export function assertExists(config: Record<string, unknown>, ...keys: string[]): void {
  for (const key of keys) {
    let d: any = config;
    const path = ["translatr"];
    for (const k of key.split(".")) {
      path.push(k);
      if (d == null || !(k in d)) {
        throw new ConfigError(`Error in ${CONFIG_FILE}: could not find key "${path.join(".")}"`);
      }
      d = d[k];
      if (d === null || d === undefined) {
        throw new ConfigError(`Error in ${CONFIG_FILE}: key "${path.join(".")}" is empty`);
      }
    }
  }
}

export interface GlobalOptions {
  endpoint?: string;
  accessToken?: string;
  defaultLocale?: string;
  projectId?: string;
}

export function readConfigMerge(overrides: GlobalOptions): TranslatrConfig {
  const config = readConfig();
  if (overrides.endpoint) config.endpoint = overrides.endpoint;
  if (overrides.accessToken) config.access_token = overrides.accessToken;
  if (overrides.defaultLocale) config.default_locale = overrides.defaultLocale;
  if (overrides.projectId) config.project_id = overrides.projectId;

  if (config.endpoint) config.endpoint = config.endpoint.replace(/\/+$/, "");
  if (!config.default_locale) config.default_locale = "default";

  return config;
}

export interface InitOptions {
  endpoint: string;
  accessToken: string;
  projectId: string;
  pullFileType: string;
  pullTarget: string;
  pushFileType: string;
  pushTarget: string;
}

export function writeInitConfig(opts: InitOptions): void {
  const content = `translatr:
  endpoint: ${opts.endpoint}
  access_token: ${opts.accessToken}
  project_id: ${opts.projectId}
  default_locale: default
  push:
    file_type: ${opts.pushFileType}
    target: ${opts.pushTarget}
  pull:
    file_type: ${opts.pullFileType}
    target: ${opts.pullTarget}
`;
  writeFileSync(CONFIG_FILE, content);
}

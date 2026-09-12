import { mkdirSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";
import type { TranslatrConfig } from "./config.js";
import { assertExists } from "./config.js";

export interface Project {
  id: string;
  name: string;
  ownerName: string;
  [key: string]: unknown;
}

export interface Locale {
  id: string;
  name: string;
  [key: string]: unknown;
}

export interface Key {
  id: string;
  name: string;
  [key: string]: unknown;
}

export interface User {
  id: string;
  name: string;
  username: string;
  [key: string]: unknown;
}

export class ApiError extends Error {}

interface ApiErrorBody {
  error?: {
    message?: string;
    violations?: { message: string; field: string }[];
  };
}

async function handleHttpError(response: Response): Promise<never> {
  const text = await response.text();
  try {
    const json = JSON.parse(text) as ApiErrorBody;
    if (response.status === 400 && json.error?.violations) {
      const violations = json.error.violations
        .map((v) => `${v.message} (${v.field})`)
        .join(", ");
      throw new ApiError(`${json.error.message}: ${violations}`);
    }
    throw new ApiError(json.error?.message ?? text);
  } catch (e) {
    if (e instanceof ApiError) throw e;
    throw new ApiError(`An undefined error occurred while talking to the API:\n\n${text}`);
  }
}

export class Api {
  constructor(private config: TranslatrConfig) {}

  private buildUrl(path: string, params: Record<string, string | undefined> = {}): string {
    assertExists(this.config as unknown as Record<string, unknown>, "endpoint", "access_token");
    const url = new URL(`${this.config.endpoint}/api/${path}`);
    url.searchParams.set("access_token", this.config.access_token);
    for (const [k, v] of Object.entries(params)) {
      if (v !== undefined) url.searchParams.set(k, v);
    }
    return url.toString();
  }

  private async request(
    method: string,
    path: string,
    opts: { params?: Record<string, string | undefined>; body?: BodyInit; json?: unknown } = {},
  ): Promise<Response> {
    const url = this.buildUrl(path, opts.params);
    let body = opts.body;
    const headers: Record<string, string> = {};
    if (opts.json !== undefined) {
      body = JSON.stringify(opts.json);
      headers["Content-Type"] = "application/json";
    }

    let response: Response;
    try {
      response = await fetch(url, { method, body, headers });
    } catch (e) {
      throw new ApiError(
        `Connection to ${this.config.endpoint} could not be established, please check your .translatr.yml config (translatr.endpoint)`,
      );
    }
    if (!response.ok) await handleHttpError(response);
    return response;
  }

  async projects(search?: string): Promise<Project[]> {
    const res = await this.request("GET", "projects", { params: { search } });
    return ((await res.json()) as { list: Project[] }).list;
  }

  async projectCreate(name: string): Promise<Project> {
    const res = await this.request("POST", "project", { json: { name } });
    return (await res.json()) as Project;
  }

  async projectDelete(projectId: string): Promise<Project> {
    const res = await this.request("DELETE", `project/${projectId}`);
    return (await res.json()) as Project;
  }

  async locales(search?: string): Promise<Locale[]> {
    assertExists(this.config as unknown as Record<string, unknown>, "project_id");
    const res = await this.request("GET", `locales/${this.config.project_id}`, {
      params: { search },
    });
    return ((await res.json()) as { list: Locale[] }).list;
  }

  async localeCreate(name: string): Promise<Locale> {
    const res = await this.request("POST", "locale", {
      json: { projectId: this.config.project_id, name },
    });
    return (await res.json()) as Locale;
  }

  async localeDelete(localeId: string): Promise<Locale> {
    const res = await this.request("DELETE", `locale/${localeId}`);
    return (await res.json()) as Locale;
  }

  async localeImport(localeId: string, fileType: string, fileContent: Buffer, fileName: string): Promise<void> {
    const form = new FormData();
    form.set("fileType", fileType);
    form.set("messages", new Blob([new Uint8Array(fileContent)]), fileName);
    await this.request("POST", `locale/${localeId}/import`, { body: form });
  }

  async localeExportToFile(localeId: string, fileType: string, target: string): Promise<void> {
    const res = await this.request("GET", `locale/${localeId}/export/${fileType}`);
    const buffer = Buffer.from(await res.arrayBuffer());
    mkdirSync(dirname(target), { recursive: true });
    writeFileSync(target, buffer);
  }

  async keys(search?: string): Promise<Key[]> {
    assertExists(this.config as unknown as Record<string, unknown>, "project_id");
    const res = await this.request("GET", `keys/${this.config.project_id}`, {
      params: { search },
    });
    return ((await res.json()) as { list: Key[] }).list;
  }

  async keyCreate(name: string): Promise<Key> {
    const res = await this.request("POST", "key", {
      json: { projectId: this.config.project_id, name },
    });
    return (await res.json()) as Key;
  }

  async keyDelete(keyId: string): Promise<Key> {
    const res = await this.request("DELETE", `key/${keyId}`);
    return (await res.json()) as Key;
  }

  async users(search?: string): Promise<User[]> {
    const res = await this.request("GET", "users", { params: { search } });
    return ((await res.json()) as { list: User[] }).list;
  }
}

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function isUuid(value: string): boolean {
  return UUID_PATTERN.test(value);
}

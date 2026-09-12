import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";

export async function withTempCwd<T>(fn: (dir: string) => Promise<T> | T): Promise<T> {
  const dir = mkdtempSync(join(tmpdir(), "translatr-cli-test-"));
  const originalCwd = process.cwd();
  process.chdir(dir);
  try {
    return await fn(dir);
  } finally {
    process.chdir(originalCwd);
    rmSync(dir, { recursive: true, force: true });
  }
}

export interface FetchCall {
  url: string;
  method: string;
  body?: unknown;
}

export function mockFetch(
  handler: (call: FetchCall) => { status?: number; json?: unknown; text?: string },
): { fetchFn: typeof fetch; calls: FetchCall[] } {
  const calls: FetchCall[] = [];
  const fetchFn = (async (input: string | URL, init?: RequestInit) => {
    const url = input.toString();
    const method = init?.method ?? "GET";
    calls.push({ url, method, body: init?.body });
    const result = handler({ url, method, body: init?.body });
    const status = result.status ?? 200;
    return {
      ok: status >= 200 && status < 300,
      status,
      json: async () => result.json,
      text: async () => result.text ?? JSON.stringify(result.json ?? {}),
      arrayBuffer: async () => new TextEncoder().encode(result.text ?? "").buffer,
    } as unknown as Response;
  }) as typeof fetch;
  return { fetchFn, calls };
}

import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import type { Page, Route } from '@playwright/test';

/**
 * Network mocking for the e2e suite.
 *
 * A former `cy.intercept('/api/x*', { fixture: 'y' })` becomes
 * `mockApi(page, '/api/x*', 'y')`. Patterns use `*` as a wildcard (translated to
 * a non-anchored RegExp), matching the old `minimatch`-style globs including
 * trailing query strings.
 *
 * Playwright runs the most recently registered matching handler first, so a
 * per-test `mockApi` shadows a default registered in the base fixture, and
 * `remockApi` swaps a handler mid-test ("last registration wins").
 */

const FIXTURES_DIR = join(__dirname, '..', 'fixtures');
const fixtureCache = new Map<string, unknown>();

export interface MockOptions {
  /** Restrict to a single HTTP method (e.g. 'POST'). Any method by default. */
  method?: string;
  /** Response status. Defaults to 200. */
  status?: number;
}

type Payload = string | Record<string, unknown> | unknown[];

export function loadFixture(name: string): unknown {
  const cached = fixtureCache.get(name);
  if (cached !== undefined) {
    return cached;
  }
  const file = join(FIXTURES_DIR, `${name}.json`);
  const parsed = JSON.parse(readFileSync(file, 'utf8'));
  fixtureCache.set(name, parsed);
  return parsed;
}

function patternToRegExp(pattern: string): RegExp {
  const escaped = pattern.replace(/[.+?^${}()|[\]\\]/g, '\\$&').replace(/\*/g, '.*');
  return new RegExp(escaped);
}

/** Per-page registry of handler refs so `remockApi` can `unroute` precisely. */
const handlers = new WeakMap<Page, Map<string, (route: Route) => Promise<void>>>();

function registryFor(page: Page): Map<string, (route: Route) => Promise<void>> {
  let map = handlers.get(page);
  if (!map) {
    map = new Map();
    handlers.set(page, map);
  }
  return map;
}

function keyFor(regex: RegExp, method?: string): string {
  return `${regex.source}::${method?.toUpperCase() ?? 'ANY'}`;
}

/**
 * Resolve the response body: a fixture name (string) or an inline JSON value.
 * A string that looks like a path segment is treated as a fixture name; pass an
 * object/array to reply with an inline body (the `cy.intercept(url, { body })`
 * case).
 */
function resolvePayload(fixtureOrBody: Payload): unknown {
  return typeof fixtureOrBody === 'string' ? loadFixture(fixtureOrBody) : fixtureOrBody;
}

export async function mockApi(
  page: Page,
  pattern: string,
  fixtureOrBody: Payload,
  opts: MockOptions = {},
): Promise<void> {
  const regex = patternToRegExp(pattern);
  const payload = resolvePayload(fixtureOrBody);

  const handler = async (route: Route): Promise<void> => {
    if (opts.method && route.request().method() !== opts.method.toUpperCase()) {
      return route.fallback();
    }
    await route.fulfill({
      status: opts.status ?? 200,
      contentType: 'application/json',
      body: JSON.stringify(payload),
    });
  };

  registryFor(page).set(keyFor(regex, opts.method), handler);
  await page.route(regex, handler);
}

/** Unregister a previous `mockApi` for the same pattern/method, then register anew. */
export async function remockApi(
  page: Page,
  pattern: string,
  fixtureOrBody: Payload,
  opts: MockOptions = {},
): Promise<void> {
  const regex = patternToRegExp(pattern);
  const prev = registryFor(page).get(keyFor(regex, opts.method));
  if (prev) {
    await page.unroute(regex, prev);
    registryFor(page).delete(keyFor(regex, opts.method));
  }
  await mockApi(page, pattern, fixtureOrBody, opts);
}

/**
 * Register a route whose handler is driven by the request itself — the
 * `cy.intercept(url, req => req.reply(...))` case (stateful mocks).
 */
export async function mockApiWith(
  page: Page,
  pattern: string,
  handler: (route: Route) => unknown | Promise<unknown>,
  opts: Pick<MockOptions, 'method'> = {},
): Promise<void> {
  const regex = patternToRegExp(pattern);
  const wrapped = async (route: Route): Promise<void> => {
    if (opts.method && route.request().method() !== opts.method.toUpperCase()) {
      return route.fallback();
    }
    await handler(route);
  };
  registryFor(page).set(keyFor(regex, opts.method), wrapped);
  await page.route(regex, wrapped);
}

/** Reply to `**\/api/me*` with 401 — the signed-out state. */
export async function mockUnauthenticated(page: Page): Promise<void> {
  await remockApi(page, '/api/me*', {}, { status: 401 });
}

/** Convenience: assert the JSON body of the next request matching pattern/method. */
export function waitForApi(
  page: Page,
  pattern: string,
  method?: string,
): Promise<import('@playwright/test').Request> {
  const regex = patternToRegExp(pattern);
  return page.waitForRequest(
    (req) => regex.test(req.url()) && (!method || req.method() === method.toUpperCase()),
  );
}

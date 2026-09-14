## Context

All CLI-to-server calls go through `Api.request` in [cli/src/api.ts](cli/src/api.ts), which calls `handleHttpError(response)` for any non-2xx response. `handleHttpError` parses the body as JSON and, only if it matches `{ error: { message, violations? } }`, builds a message from that structure; any other shape (including valid-but-different JSON like `{"status":404,"message":"..."}`) or a JSON-parse failure falls through to `An undefined error occurred while talking to the API:\n\n${text}` today. See [proposal.md](proposal.md) for why that fallback is a problem.

## Goals / Non-Goals

**Goals:**
- Fallback path names method, URL, and status instead of dumping raw text.
- 404s in the fallback path get an actionable hint (check `endpoint`/`project_id`).

**Non-Goals:**
- Changing the structured-error path (400 + violations, or `error.message` present) - that already produces a good message.
- Retrying requests, validating config before the request is sent, or adding new CLI flags.
- Changing the network-failure branch (`fetch` throwing) - already has a clear message.

## Decisions

- **Where**: implement entirely inside `handleHttpError` in `cli/src/api.ts`. It already has `response` (method/url available via `response.url`) - no need to thread extra context through `request()`.
  - Alternative considered: catch and rewrap in `request()` instead. Rejected - `handleHttpError` already owns all error-formatting, keeping it in one place avoids splitting the logic.
- **Shape detection**: keep the existing `try { JSON.parse ...}` structure, but distinguish "parsed JSON, has `error.message`" from "parsed JSON, no `error.message`" from "did not parse as JSON" - only the first case keeps today's structured message; the other two both go through the new CLI-authored fallback.
- **404 hint**: keyed off `response.status === 404` in the fallback branch only. A 404 that *does* match the structured shape (server explicitly returns `error.message` on a 404) is left alone, since the server already gave a specific reason.
- **Message format**: `${method} ${url} failed with ${status}` as the base, with the 404 hint appended as a second line. No new error type - keep using `ApiError` so callers (`eprint(e.message)`) need no changes.

## Risks / Trade-offs

- [Losing server-provided detail when the body *is* useful but doesn't match the exact shape] → Low risk: today that detail is already dumped raw and unlabeled; the new message is strictly more informative (adds method/URL/status) even without repeating the raw body. Acceptable given the proposal's scope.
- [404 hint is misleading for a 404 that is unrelated to `endpoint`/`project_id`, e.g. a genuinely deleted resource] → Hint is phrased as a suggestion ("check ..."), not a diagnosis; low cost if wrong.

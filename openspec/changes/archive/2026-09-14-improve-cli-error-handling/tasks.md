## 1. Error message rework

- [x] 1.1 In `handleHttpError` (`cli/src/api.ts`), branch on parsed-JSON-with-`error.message` vs. everything else, and build the "everything else" branch's message as `${method} ${url} failed with ${status}` (method/url available from the `Response`/`fetch` call), instead of `An undefined error occurred while talking to the API:\n\n${text}` - verify by unit test asserting the message for a non-JSON 500 body and for JSON without an `error` key
- [x] 1.2 Append a `.translatr.yml` `endpoint`/`project_id` hint line to that message only when `response.status === 404` - verify by unit test asserting the hint text is present for a 404 with an unparseable body and absent for a non-404 in the same branch
- [x] 1.3 Confirm the structured-error branch (`error.message` present, with or without `violations`) is unchanged - verify existing behavior via unit test for a 400 with `violations` and a non-400 with `error.message` only

## 2. Tests

- [x] 2.1 Create `cli/src/api.test.ts` covering: structured 400 with violations, structured error without violations, generic 404 body `{"status":404,"message":"..."}` (the exact case from the bug report), non-JSON error body, and a non-404 unparseable error (no hint appended) - verify with `npm test` (or the CLI's configured test runner) passing
- [x] 2.2 Manually reproduce the original repro (`translatr push` against a URL that 404s) and confirm the printed error names the method, URL, status, and the config hint - verify by running the command and inspecting stderr output

# E2E Coverage Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add stubbed Cypress specs closing the biggest e2e coverage gaps in `translatr` and `translatr-admin` (project members, access tokens, auth/guards, users/projects/activity pages, project overview, add-flow + editor persistence, navbar), and replace the `translatr-admin-e2e` placeholder with a real suite.

**Architecture:** Five phases, each its own commit on `feature/quarkus-migration`. Every spec follows the existing `translatr-e2e` convention verbatim: `describe` per screen; `beforeEach` calls `cy.clearCookies()` then registers per-spec `cy.intercept('/api/...', { fixture })` stubs; a page object per screen under `src/support/**` extending `Page`, exposing `navigateTo()` + `getX()` accessors; `// given / when / then` body comments. Fixtures under `src/fixtures/<owner>/<project>/...`. No shared support layer (`supportFile: false` stays). Exact CSS selectors for new `getX()` accessors are found by TDD against the running app; this plan gives the selectors already known from the component templates.

**Tech Stack:** Cypress 15.21.1 (`@nx/cypress@23`, `cypress.config.js` / `defineConfig`, `specPattern: ./src/integration/**/*.spec.ts`, `supportFile: false`), Angular 22 apps, Nx 23. Run: `npx nx e2e translatr-e2e` / `npx nx e2e translatr-admin-e2e` (each ~7 min, real Electron), `npx nx lint translatr-e2e` / `translatr-admin-e2e`.

**Spec:** `docs/superpowers/specs/2026-08-29-e2e-coverage-expansion-design.md`

## Global Constraints

- All work under `ui/apps/translatr-e2e/` (Phases 1–4) and `ui/apps/translatr-admin-e2e/` (Phase 5). No changes to `ui/apps/translatr/`, `ui/apps/translatr-admin/`, `ui/libs/`, or any non-e2e file.
- **No change to `ui/apps/translatr-e2e/cypress.config.js`.**
- `translatr-admin-e2e` is ALREADY on `cypress.config.js` (the Cypress migration did this): `cypress.json` + `src/plugins/index.js` are gone, `project.json` `cypressConfig` already points at `cypress.config.js`. Phase 5 does NOT redo that — only the placeholder-spec removal + new content.
- Spec convention, copied verbatim: `describe('<Screen>', () => { let page: <Po>; beforeEach(() => { page = new <Po>(...); cy.clearCookies(); cy.intercept('/api/me?fetch=features', { fixture: 'me' }); /* + screen intercepts */ }); it('should ...', () => { /* given */ /* when */ page.navigateTo(); /* then */ ... }); });`
- Page objects extend `Page` from `src/support/page.po.ts`, which already provides `getPageName()` → `cy.get('.page')`, `getTitle()` → `cy.get('title')`, `getFloatingActionButton()` → `cy.get('.floating-action-btn')`, `getDialog()` → `cy.get('mat-dialog-container')`.
- Verified API paths (from `libs/translatr-sdk`, `AbstractService`): list = `GET <listPath>` with criteria as query params; get = `GET <entityPath>/<id>`; create = `POST <entityPath>`; update = `PUT <entityPath>` (base path, NOT `/id`); delete = `DELETE <entityPath>/<id>`.
  - Members: list `/api/project/{id}/members`, entity `/api/member`
  - Access tokens: list `/api/accesstokens`, entity `/api/accesstoken`
  - Feature flags: list `/api/featureflags`, entity `/api/featureflag`
  - Users: list `/api/user`; single by-username `/api/{username}`; user activity `/api/user/{id}/activity`; user settings `/api/user/{id}/settings`
  - Projects: list + entity `/api/project`; single by name `/api/{username}/{projectName}`
  - Activities: `/api/activities`, `/api/activities/aggregated`; project activity `/api/project/{id}/activity`
  - Statistics: `/api/statistics`; me: `/api/me?fetch=features`
  - Auth clients: `/api/authclients`
- Trivial one-off stubs (404/403, POST echo of the request) may use `cy.intercept('METHOD', '/api/x', { statusCode, body })` inline; anything reused across `it`s/specs gets a fixture file.
- Existing per-role fixtures reused: `johndoe/p1.json` (no `myRole` = full access via Admin `me`), `johndoe/p1-developer.json`, `johndoe/p1-translator.json`, `johndoe/p1-manager.json` (`"myRole"` field). Per-user `me` fixtures: `janesmith.json` (role `User`), `sophiaoreilly.json`, `ronnylee.json`, `anneearth.json` (role `Admin`).
- Each phase: run `npx nx lint <e2e-project>` then `npx nx e2e <e2e-project>`, iterate to green, then one commit `test(e2e): <phase>`.
- Do not touch `src/main/**` (backend, unrelated) or the two unrelated uncommitted working-tree files (`ui/nx.json`, `src/main/resources/application-dev.properties`) — leave them, never stage them.

---

## The verification loop (referenced by every task)

```bash
cd /Users/renepanzar/Development/translatr/ui
npx nx lint translatr-e2e          # (Phase 5: translatr-admin-e2e)
npx nx e2e translatr-e2e           # (Phase 5: translatr-admin-e2e) — real Electron, ~7 min
```

- `nx e2e` spins the app dev server + runs all specs headless. A new spec is "green" when it passes here.
- If a `getX()` selector finds nothing: open the component template named in the task, pick a stable structural selector (prefer an existing class the component already uses — `.page`, `.floating-action-btn`, `confirm-button`, `mat-dialog-container`, `entity-table`, `app-nav-list`, `mat-list-item`), update the PO, re-run.
- Flake: re-run the single failing spec once (`npx nx e2e translatr-e2e --spec 'apps/translatr-e2e/src/integration/<path>.spec.ts'`) before treating a failure as real.
- A phase's commit lands only when `nx lint` + `nx e2e` for that project are green (pre-existing failures noted: `translatr-admin-e2e`'s current placeholder spec fails — Phase 5 deletes it).

---

## File Structure

### Phase 1 — `ui/apps/translatr-e2e/`
- Modify `src/support/project/project-members-page.po.ts` — add member-list + dialog accessors.
- Create `src/fixtures/johndoe/p1/members-added.json`, `member-created.json`, `member-updated.json`, `members-two-owners.json`.
- Modify `src/integration/project/members/project-members.spec.ts` — replace the single page-name `it` with real assertions.
- Create `src/integration/project/members/project-members-add.spec.ts`, `project-members-edit.spec.ts`, `project-members-delete.spec.ts`, `project-members-transfer-ownership.spec.ts`, `project-members-add-visibility.spec.ts`, `project-members-modify-visibility.spec.ts`.

### Phase 2 — `ui/apps/translatr-e2e/`
- Create `src/support/user/user-access-tokens-page.po.ts`, `src/support/user/user-access-token-page.po.ts`, `src/support/auth/login-page.po.ts`, `src/support/auth/registration-page.po.ts`.
- Create fixtures `src/fixtures/johndoe/access-tokens.json`, `access-tokens-empty.json`, `access-token.json`, `access-token-created.json`; `src/fixtures/johndoe-register.json`.
- Create `src/integration/user/access-tokens/user-access-tokens.spec.ts`, `user-access-token-create.spec.ts`, `user-access-token-edit.spec.ts`, `user-access-tokens-visibility.spec.ts`.
- Create `src/integration/auth/login.spec.ts`, `registration.spec.ts`, `forbidden.spec.ts`, `not-found.spec.ts`.

### Phase 3 — `ui/apps/translatr-e2e/`
- Create `src/support/users-page.po.ts`, `src/support/projects-page.po.ts`, `src/support/user/user-activity-page.po.ts`, `src/support/project/project-activity-page.po.ts`.
- Create fixtures `src/fixtures/users.json`, `users-page2.json`, `users-empty.json`, `users-search.json`, `projects.json`, `projects-search.json`, `project-created.json`, `johndoe/activities-page2.json`.
- Create `src/integration/users/users.spec.ts`, `src/integration/projects/projects.spec.ts`, `src/integration/user/activity/user-activity.spec.ts`, `src/integration/project/activity/project-activity.spec.ts`.
- Modify `src/integration/project/project.spec.ts` — add overview-tab assertions.

### Phase 4 — `ui/apps/translatr-e2e/`
- Create `src/support/nav/navbar.po.ts`.
- Create fixtures `src/fixtures/johndoe/p1/locale-created.json`, `locales-added.json`, `search-results.json`, `me-language-switcher.json`.
- Modify `src/integration/project/locales/project-locales-add.spec.ts`, `src/integration/project/keys/project-keys-add.spec.ts` — add persistence `it`s.
- Create `src/integration/project/keys/project-key-editor-save.spec.ts`, `src/integration/project/locales/project-locale-editor-save.spec.ts`, `src/integration/nav/navbar.spec.ts`.

### Phase 5 — `ui/apps/translatr-admin-e2e/`
- Delete `src/integration/app.spec.ts`, `src/support/app.po.ts`, `src/support/commands.ts`, `src/support/index.ts`, `src/fixtures/example.json`.
- Create `src/support/page.po.ts`, `src/support/dashboard-page.po.ts`, `src/support/feature-flags-page.po.ts`, `src/support/access-tokens-page.po.ts`, `src/support/users-page.po.ts`, `src/support/projects-page.po.ts`.
- Create fixtures `src/fixtures/me.json`, `me-non-admin.json`, `users.json`, `user.json`, `user-updated.json`, `projects.json`, `feature-flags.json`, `feature-flag-created.json`, `feature-flag-updated.json`, `access-tokens.json`, `access-token-updated.json`, `statistics.json`, `activities.json`.
- Create `src/integration/dashboard.spec.ts`, `feature-flags.spec.ts`, `access-tokens.spec.ts`, `users.spec.ts`, `projects.spec.ts`, `auth.spec.ts`.

---

## Task 1: Phase 1 — Project Members

**Files:**
- Modify: `ui/apps/translatr-e2e/src/support/project/project-members-page.po.ts`
- Create: `ui/apps/translatr-e2e/src/fixtures/johndoe/p1/{members-added,member-created,member-updated,members-two-owners}.json`
- Modify: `ui/apps/translatr-e2e/src/integration/project/members/project-members.spec.ts`
- Create: `ui/apps/translatr-e2e/src/integration/project/members/project-members-{add,edit,delete,transfer-ownership,add-visibility,modify-visibility}.spec.ts`

**Interfaces:**
- Consumes: existing `johndoe/p1/members.json` (4 members: John Doe/Owner id `...4531`, Jane Smith/Developer `...4532`, Sophia O'Reilly/Translator `...4533`, Ronny Lee/Manager `...4534`); existing `johndoe/p1.json`, `johndoe/p1-{developer,translator,manager}.json`; `me.json`, `janesmith.json`, `sophiaoreilly.json`, `ronnylee.json`, `anneearth.json`.
- Produces: `ProjectMembersPage` with `getMemberRows()`, `getAddDialog()`, `getEditButton(name)`, `getDeleteButton(name)`, `getTransferButton(name)`, `getDialogSaveButton()`, `getDialogCancelButton()`.

- [ ] **Step 1: Extend the page object**

Replace `ui/apps/translatr-e2e/src/support/project/project-members-page.po.ts` with:

```ts
import { Page } from '../page.po';

export class ProjectMembersPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) {
    super();
  }

  navigateTo(): ProjectMembersPage {
    cy.visit(`/${this.username}/${this.projectName}/members`);
    return this;
  }

  getMemberList(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-member-list app-nav-list');
  }

  getMemberRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-member-list mat-list-item');
  }

  getMemberRow(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRows().contains('mat-list-item', userName);
  }

  getEditButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('button.edit');
  }

  getDeleteButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('confirm-button.delete');
  }

  getTransferButton(userName: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getMemberRow(userName).find('button.edit mat-icon').contains('swap_horiz').parents('button');
  }

  getEditDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-member-edit-dialog, mat-dialog-container');
  }

  getOwnerEditDialog(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-project-owner-edit-dialog, mat-dialog-container');
  }

  getDialogSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getDialog().find('button[transloco="button.save"]');
  }

  getDialogCancelButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getDialog().find('button[transloco="button.cancel"]');
  }
}
```

(Selectors from `apps/translatr/src/app/modules/pages/project-page/project-members/member-list/member-list.component.html` — `app-nav-list`, `mat-list-item`, `button.edit`, `confirm-button.delete`, `<mat-icon>swap_horiz`; dialog from `project-member-edit-dialog.component.html` — `button[transloco="button.save"]` / `button[transloco="button.cancel"]`. If `contains('mat-list-item', name)` is brittle, use `.member .name` or the row's `matListItemTitle` text — verify against the running DOM.)

- [ ] **Step 2: Create the fixtures**

`ui/apps/translatr-e2e/src/fixtures/johndoe/p1/member-created.json` — a single `Member` (Sophia's brother, a new user), shape copied from a row of `members.json`:

```json
{
  "id": 1007,
  "whenCreated": "2020-01-02T10:00:00.000Z",
  "whenUpdated": "2020-01-02T10:00:00.000Z",
  "role": "Developer",
  "projectId": "bc5f3fa7-3d9f-4aee-9457-0f6c4d5f5486",
  "projectName": "p1",
  "projectOwnerUsername": "johndoe",
  "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4540",
  "userName": "Mika Novak",
  "userUsername": "mikanovak",
  "userEmailHash": "6d041874043a1820c908f747e8a608bc"
}
```

`member-updated.json` — Jane Smith's row with `role` changed to `Manager`:

```json
{
  "id": 1004,
  "whenCreated": "2019-10-25T10:51:41.007Z",
  "whenUpdated": "2020-01-02T11:00:00.000Z",
  "role": "Manager",
  "projectId": "bc5f3fa7-3d9f-4aee-9457-0f6c4d5f5486",
  "projectName": "p1",
  "projectOwnerUsername": "johndoe",
  "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4532",
  "userName": "Jane Smith",
  "userUsername": "janesmith",
  "userEmailHash": "6d041874043a1820c908f747e8a608bc"
}
```

`members-added.json` — the existing `members.json` `list` plus the `member-created` row appended; `total` 4 → 5. (Copy `johndoe/p1/members.json` verbatim, append the `member-created.json` object to `list`, bump `total`.)

`members-two-owners.json` — the existing `members.json` with Ronny Lee's `role` changed from `Manager` to `Owner` (so two `Owner` rows), `total` stays 4.

- [ ] **Step 3: Rewrite `project-members.spec.ts` with real assertions**

Replace `ui/apps/translatr-e2e/src/integration/project/members/project-members.spec.ts`:

```ts
import { ProjectMembersPage } from '../../../support/project/project-members-page.po';

describe('Project Members', () => {
  let page: ProjectMembersPage;

  beforeEach(() => {
    page = new ProjectMembersPage('johndoe', 'p1');

    cy.clearCookies();

    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p1/keys' });
    cy.intercept('/api/project/*/messages*', { fixture: 'johndoe/p1/messages' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' });
    cy.intercept('/api/project/*/activities*', { fixture: 'johndoe/p1/activities' });
    cy.intercept('/api/activities/aggregated*', { fixture: 'johndoe/p1/activities-aggregated' });
  });

  it('should have page name johndoe/p1', () => {
    // when
    page.navigateTo();

    // then
    page.getPageName().should('have.text', 'johndoe/p1');
  });

  it('should render all four members with name and role', () => {
    // when
    page.navigateTo();

    // then
    page.getMemberRows().should('have.length', 4);
    page.getMemberRow('John Doe').should('contain.text', 'Owner');
    page.getMemberRow('Jane Smith').should('contain.text', 'Developer');
    page.getMemberRow("Sophia O'Reilly").should('contain.text', 'Translator');
    page.getMemberRow('Ronny Lee').should('contain.text', 'Manager');
  });
});
```

- [ ] **Step 4: Create `project-members-add.spec.ts`**

`beforeEach` = the same intercept block as Step 3. Then:

```ts
  it('should show the add member FAB', () => {
    // when
    page.navigateTo();
    // then
    page.getFloatingActionButton().should('be.visible');
  });

  it('should open the add member dialog on FAB click', () => {
    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    // then
    page.getDialog().should('have.length', 1);
    page.getDialog().find('[mat-dialog-title]').should('have.attr', 'transloco', 'member.create.title');
  });

  it('should hide the dialog on cancel', () => {
    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialogCancelButton().click();
    // then
    page.getDialog().should('have.length', 0);
  });

  it('should add the member and show the new row on save', () => {
    // given
    cy.intercept('POST', '/api/member', { fixture: 'johndoe/p1/member-created' });
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-added' });
    cy.intercept('/api/johndoe/p1*', { fixture: 'johndoe/p1' });

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    // fill the member-edit-form: type a username into its autocomplete, pick a role.
    // Selectors: verify against app-project-member-edit-form — likely `input[formcontrolname="user"]`
    // + `mat-option`, and `mat-select[formcontrolname="role"]` + `mat-option`.
    page.getDialog().find('input').first().type('mika');
    cy.get('mat-option').contains('Mika Novak').click();
    page.getDialogSaveButton().click();

    // then
    page.getDialog().should('have.length', 0);
    page.getMemberRow('Mika Novak').should('exist');
  });
```

(The form-fill selectors — user autocomplete + role select — are the one part to TDD: read `apps/translatr/src/app/modules/shared/project-member-edit-form/project-member-edit-form.component.html`, wire the PO `fillMemberForm(username, role)` helper, iterate.)

- [ ] **Step 5: Create `project-members-edit.spec.ts`**

`beforeEach` = Step 3 intercepts. `it`s:

| `it` | given (extra intercepts) | when | then |
|---|---|---|---|
| shows edit button on a non-owner row | — | `navigateTo()` | `getEditButton('Jane Smith').should('be.visible')` |
| opens edit dialog pre-filled | — | `navigateTo()`; `getEditButton('Jane Smith').click()` | `getDialog().should('have.length', 1)`; dialog title attr `transloco="member.edit.title"` |
| updates the role on save | `cy.intercept('PUT', '/api/member', { fixture: 'johndoe/p1/member-updated' })`; `cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members' })` re-stub with Jane as Manager (make a `members-jane-manager.json` or inline `body`) | `navigateTo()`; `getEditButton('Jane Smith').click()`; change role select to Manager; `getDialogSaveButton().click()` | `getDialog().should('have.length', 0)`; `getMemberRow('Jane Smith').should('contain.text', 'Manager')` |
| hides dialog on cancel | — | `navigateTo()`; `getEditButton('Jane Smith').click()`; `getDialogCancelButton().click()` | `getDialog().should('have.length', 0)` |

- [ ] **Step 6: Create `project-members-delete.spec.ts`**

`beforeEach` = Step 3 intercepts. `it`s:

```ts
  it('should show the delete button on a non-owner row', () => {
    page.navigateTo();
    page.getDeleteButton('Ronny Lee').should('exist');
  });

  it('should not show a delete button on the sole Owner row', () => {
    page.navigateTo();
    page.getMemberRow('John Doe').find('confirm-button.delete').should('not.exist');
  });

  it('should remove the row after confirming delete', () => {
    // given
    cy.intercept('DELETE', '/api/member/*', { statusCode: 200, body: {} });
    cy.intercept('/api/project/*/members*', (req) => {
      req.reply({ fixture: 'johndoe/p1/members' }); // first call
    });
    // simpler: re-stub with a members list minus Ronny after the delete — make johndoe/p1/members-minus-ronny.json
    cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-minus-ronny' });

    // when
    page.navigateTo();
    page.getDeleteButton('Ronny Lee').click();               // confirm-button: first click arms
    page.getDeleteButton('Ronny Lee').find('button').last().click(); // second click confirms — verify confirm-button DOM
    page.getFloatingActionButton().should('exist');          // wait for re-render

    // then
    page.getMemberRow('Ronny Lee').should('not.exist');
    page.getMemberRows().should('have.length', 3);
  });
```

Add fixture `johndoe/p1/members-minus-ronny.json` = `members.json` with Ronny's row removed, `total` 3. The `confirm-button` two-step interaction: check `libs/translatr-components/src/lib/modules/button/confirm-button/confirm-button.component.html` for how it arms — mirror the existing `project-keys-delete.spec.ts` which already drives a `confirm-button`.

- [ ] **Step 7: Create `project-members-transfer-ownership.spec.ts`**

`beforeEach` = Step 3 intercepts, but `cy.intercept('/api/project/*/members*', { fixture: 'johndoe/p1/members-two-owners' })` and `cy.intercept('/api/me?fetch=features', { fixture: 'anneearth' })` (Admin → `canTransferOwnership`). `it`s:

```ts
  it('should show the transfer-ownership button on an owner row when two owners exist', () => {
    page.navigateTo();
    page.getMemberRow('John Doe').find('button.edit mat-icon').should('contain.text', 'swap_horiz');
  });

  it('should open the owner edit dialog and navigate back to members on complete', () => {
    // given
    cy.intercept('PUT', '/api/project', { fixture: 'johndoe/p1' });
    // when
    page.navigateTo();
    page.getMemberRow('John Doe').find('button.edit').click();
    // then
    cy.get('app-project-owner-edit-dialog, mat-dialog-container').should('have.length', 1);
    // complete: pick the new owner + save; assert URL
    // page.getDialog().find('...').click(); page.getDialogSaveButton().click();
    // cy.url().should('contain', '/johndoe/p1/members');
  });
```

- [ ] **Step 8: Create `project-members-add-visibility.spec.ts`** — mirror `project/keys/project-keys-add-visibility.spec.ts` exactly, `ProjectMembersPage`, asserting `getFloatingActionButton()`:

| `it` | `me` fixture | `/api/johndoe/p1*` fixture | assertion |
|---|---|---|---|
| Developer: no add FAB | `janesmith` | `johndoe/p1-developer` | `.should('not.exist')` |
| Translator: no add FAB | `sophiaoreilly` | `johndoe/p1-translator` | `.should('not.exist')` |
| Manager: add FAB visible | `ronnylee` | `johndoe/p1-manager` | `.should('exist')` |
| Owner/Admin: add FAB visible | `anneearth` | `johndoe/p1` | `.should('exist')` |

`beforeEach` intercept block: the Step 3 block minus the `/api/me` and `/api/johndoe/p1*` lines (those are per-`it`), exactly like the keys visibility spec.

- [ ] **Step 9: Create `project-members-modify-visibility.spec.ts`** — same role matrix, asserting the per-row edit + delete controls:

| `it` | `me` / project fixture | assertion |
|---|---|---|
| Developer: no edit/delete controls | `janesmith` / `p1-developer` | `getMemberRow('Ronny Lee').find('button.edit').should('not.exist')` and `.find('confirm-button.delete').should('not.exist')` |
| Translator: no edit/delete | `sophiaoreilly` / `p1-translator` | same |
| Manager: edit + delete visible | `ronnylee` / `p1-manager` | `getMemberRow('Jane Smith').find('button.edit').should('exist')` |
| Owner/Admin: edit + delete visible | `anneearth` / `p1` | same |

- [ ] **Step 10: Verify + commit**

```bash
cd /Users/renepanzar/Development/translatr/ui
npx nx lint translatr-e2e
npx nx e2e translatr-e2e
```

Iterate selectors/fixtures until every `project/members/*.spec.ts` passes and no previously-green spec regressed. Then:

```bash
git add apps/translatr-e2e
git commit -m "test(e2e): project members — add/edit/delete/transfer + role visibility

7 specs covering member add (POST /api/member), role edit (PUT), remove
(DELETE), ownership transfer, and add/modify visibility across
Developer/Translator/Manager/Owner/Admin. Mirrors the keys visibility pattern.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 2: Phase 2 — Access tokens & auth / entry pages

**Files:**
- Create: `ui/apps/translatr-e2e/src/support/user/{user-access-tokens-page,user-access-token-page}.po.ts`, `src/support/auth/{login-page,registration-page}.po.ts`
- Create: `ui/apps/translatr-e2e/src/fixtures/johndoe/{access-tokens,access-tokens-empty,access-token,access-token-created}.json`, `src/fixtures/johndoe-register.json`, `src/fixtures/johndoe-not-unique.json` (if not present — check; `johndoe-not-unique.json` already exists)
- Create: `ui/apps/translatr-e2e/src/integration/user/access-tokens/{user-access-tokens,user-access-token-create,user-access-token-edit,user-access-tokens-visibility}.spec.ts`
- Create: `ui/apps/translatr-e2e/src/integration/auth/{login,registration,forbidden,not-found}.spec.ts`

**Interfaces:**
- Consumes: `me.json`, `me-null.json`, `janesmith.json`, `johndoe.json`, `johndoe/projects.json`, `johndoe/activities.json`, `statistics.json`, `activities-aggregated.json`.
- Produces: `UserAccessTokensPage`, `UserAccessTokenPage`, `LoginPage`, `RegistrationPage`.

- [ ] **Step 1: Page objects**

`src/support/user/user-access-tokens-page.po.ts`:

```ts
import { Page } from '../page.po';

export class UserAccessTokensPage extends Page {
  constructor(private readonly username: string) {
    super();
  }
  navigateTo(): UserAccessTokensPage {
    cy.visit(`/${this.username}/access-tokens`);
    return this;
  }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('app-user-access-tokens mat-list-item, app-user-access-tokens .access-token');
  }
  getEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('dev-empty-view, app-empty-view, .empty-view');
  }
}
```

`src/support/user/user-access-token-page.po.ts`:

```ts
import { Page } from '../page.po';

export class UserAccessTokenPage extends Page {
  constructor(private readonly username: string, private readonly id: string = 'create') {
    super();
  }
  navigateTo(): UserAccessTokenPage {
    cy.visit(`/${this.username}/access-tokens/${this.id}`);
    return this;
  }
  getNameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.name input, input[formcontrolname="name"]');
  }
  getSaveButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('button.save, button[transloco="button.save"]');
  }
  getSecret(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.access-token-key, code, input[readonly]');
  }
}
```

`src/support/auth/login-page.po.ts`:

```ts
import { Page } from '../page.po';

export class LoginPage extends Page {
  navigateTo(): LoginPage {
    cy.visit('/login');
    return this;
  }
  getProviderLinks(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.options a.client');
  }
}
```

`src/support/auth/registration-page.po.ts`:

```ts
import { Page } from '../page.po';

export class RegistrationPage extends Page {
  navigateTo(): RegistrationPage {
    cy.visit('/register');
    return this;
  }
  getUsernameField(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.username input, input[formcontrolname="username"]');
  }
  getUsernameError(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('mat-form-field.username mat-error, mat-error');
  }
  getSubmitButton(): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('button.save, button[type="submit"], button[transloco="button.save"]');
  }
}
```

Selectors to TDD against: `apps/translatr/src/app/modules/pages/user-page/user-access-tokens/*.html`, `user-access-token/*.html`, `apps/translatr/src/app/modules/pages/registration-page/*.html`. Login page selectors are confirmed from `libs/translatr-components/src/lib/modules/pages/login-page/login-page.component.html`: `h1.page` text "Login Page", `.options a.client`, `a.client-keycloak`.

- [ ] **Step 2: Access-token fixtures**

`johndoe/access-tokens.json` — `PagedList<AccessToken>`:

```json
{
  "list": [
    {
      "id": "a1000000-0000-0000-0000-000000000001",
      "whenCreated": "2020-01-01T10:00:00.000Z",
      "whenUpdated": "2020-01-01T10:00:00.000Z",
      "name": "CI token",
      "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4531",
      "userName": "John Doe",
      "userUsername": "johndoe",
      "scope": "project:read,project:write"
    },
    {
      "id": "a1000000-0000-0000-0000-000000000002",
      "whenCreated": "2020-02-01T10:00:00.000Z",
      "whenUpdated": "2020-02-01T10:00:00.000Z",
      "name": "Local dev",
      "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4531",
      "userName": "John Doe",
      "userUsername": "johndoe",
      "scope": "project:read"
    }
  ],
  "offset": 0,
  "limit": 20,
  "total": 2,
  "hasPrev": false,
  "hasNext": false
}
```

`johndoe/access-tokens-empty.json` — same shape, `"list": []`, `"total": 0`.

`johndoe/access-token.json` — the first row above, as a bare object (for `GET /api/accesstoken/{id}`).

`johndoe/access-token-created.json` — a new token with a `key` (one-time secret):

```json
{
  "id": "a1000000-0000-0000-0000-000000000009",
  "whenCreated": "2020-03-01T10:00:00.000Z",
  "whenUpdated": "2020-03-01T10:00:00.000Z",
  "name": "New token",
  "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4531",
  "userName": "John Doe",
  "userUsername": "johndoe",
  "scope": "project:read",
  "key": "tk_live_ONE_TIME_SECRET_abcdef123456"
}
```

- [ ] **Step 3: `user-access-tokens.spec.ts`**

```ts
import { UserAccessTokensPage } from '../../../support/user/user-access-tokens-page.po';

describe('User Access Tokens', () => {
  let page: UserAccessTokensPage;

  beforeEach(() => {
    page = new UserAccessTokensPage('johndoe');
    cy.clearCookies();
    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe', { fixture: 'johndoe' });
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens' });
  });

  it('should list the access tokens', () => {
    page.navigateTo();
    page.getRows().should('have.length', 2);
    page.getRows().should('contain.text', 'CI token');
  });

  it('should show an empty view when there are none', () => {
    // given
    cy.intercept('/api/accesstokens*', { fixture: 'johndoe/access-tokens-empty' });
    // when
    page.navigateTo();
    // then
    page.getEmptyView().should('be.visible');
  });

  it('should navigate to the create page from the FAB', () => {
    page.navigateTo();
    page.getFloatingActionButton().click();
    cy.url().should('contain', '/johndoe/access-tokens/create');
  });
});
```

- [ ] **Step 4: `user-access-token-create.spec.ts`**

`beforeEach` intercepts: `me`, `/api/johndoe`, `/api/accesstokens*` → `access-tokens`. `it`s:

```ts
  it('should create a token and reveal the secret once', () => {
    // given
    cy.intercept('POST', '/api/accesstoken', { fixture: 'johndoe/access-token-created' });
    const page = new UserAccessTokenPage('johndoe', 'create').navigateTo();

    // when
    page.getNameField().type('New token');
    page.getSaveButton().click();

    // then
    page.getSecret().should('contain.text', 'tk_live_ONE_TIME_SECRET_abcdef123456');
  });

  it('should return to the list afterwards', () => {
    cy.intercept('POST', '/api/accesstoken', { fixture: 'johndoe/access-token-created' });
    const page = new UserAccessTokenPage('johndoe', 'create').navigateTo();
    page.getNameField().type('New token');
    page.getSaveButton().click();
    // then — a back/list link or auto-redirect
    cy.url().should('match', /\/johndoe\/access-tokens(\/create)?$/);
  });
```

(If create auto-redirects to the list rather than showing the secret inline, assert the list state instead — TDD.)

- [ ] **Step 5: `user-access-token-edit.spec.ts`**

`beforeEach` intercepts: `me`, `/api/johndoe`, `/api/accesstokens*` → `access-tokens`, `/api/accesstoken/*` → `johndoe/access-token`. `it`s:

| `it` | given | when | then |
|---|---|---|---|
| fields populated from the token | — | `new UserAccessTokenPage('johndoe', 'a1000000-0000-0000-0000-000000000001').navigateTo()` | `getNameField().should('have.value', 'CI token')` |
| persists on save | `cy.intercept('PUT', '/api/accesstoken', { statusCode: 200, body: {} })` | navigate; `getNameField().type('{selectall}CI token renamed')`; `getSaveButton().click()` | `cy.url().should('contain', '/johndoe/access-tokens')` (list) |
| deletes and returns to list | `cy.intercept('DELETE', '/api/accesstoken/*', { statusCode: 200, body: {} })`; re-stub `/api/accesstokens*` with a one-row list `johndoe/access-tokens-minus-one.json` | navigate; click the delete control (find in the edit page or list row); confirm | row gone / URL is the list |

Add `johndoe/access-tokens-minus-one.json` (first row only, `total` 1) if the delete `it` needs it.

- [ ] **Step 6: `user-access-tokens-visibility.spec.ts`**

```ts
  beforeEach: cy.intercept('/api/me?fetch=features', { fixture: 'janesmith' });
             cy.intercept('/api/johndoe', { fixture: 'johndoe' });

  it('should block another user from johndoe access-tokens (MyselfGuard)', () => {
    const page = new UserAccessTokensPage('johndoe').navigateTo();
    // then — assert whatever the guard does: URL is NOT /johndoe/access-tokens, OR /forbidden
    cy.url().should('not.contain', '/johndoe/access-tokens');
  });
```

(`MyselfGuard` behaviour — redirect vs `/forbidden` — is decided by observing the run.)

- [ ] **Step 7: `auth/login.spec.ts`**

```ts
import { LoginPage } from '../../support/auth/login-page.po';

describe('Login', () => {
  let page: LoginPage;
  beforeEach(() => {
    page = new LoginPage();
    cy.clearCookies();
    cy.intercept('/api/authclients', { body: [{ key: 'keycloak', url: '/login/keycloak' }] });
  });

  it('should render the login page with a provider link', () => {
    page.navigateTo();
    page.getPageName().should('have.text', 'Login Page');
    page.getProviderLinks().should('have.length.greaterThan', 0);
    cy.get('a.client-keycloak').should('exist');
  });

  it('should redirect an unauthenticated visit to /dashboard to /login', () => {
    // given
    cy.intercept('/api/me*', { fixture: 'me-null' });
    cy.intercept('/api/authclients', { body: [{ key: 'keycloak', url: '/login/keycloak' }] });
    // when
    cy.visit('/dashboard');
    // then
    cy.url().should('contain', '/login');
  });
});
```

- [ ] **Step 8: `auth/registration.spec.ts`**

`beforeEach`: `cy.intercept('/api/me*', { fixture: 'me' })` (or a fixture with no `username` so registration is required — check `registration-page` guard; `apps/translatr/src/app/modules/pages/registration-page/*`). `it`s:

| `it` | given | when | then |
|---|---|---|---|
| shows the username field | — | `page.navigateTo()` | `getUsernameField().should('be.visible')` |
| disables submit on invalid pattern | — | `navigateTo()`; `getUsernameField().type('bad name!').blur()` | `getSubmitButton().should('be.disabled')`; `getUsernameError().should('be.visible')` |
| shows a field error when username not unique | `cy.intercept('PUT', '/api/user', { statusCode: 400, fixture: 'johndoe-not-unique' })` | `navigateTo()`; `getUsernameField().type('johndoe')`; `getSubmitButton().click()` | `getUsernameError().should('be.visible')` |
| redirects on success | `cy.intercept('PUT', '/api/user', { fixture: 'johndoe' })` | `navigateTo()`; `getUsernameField().type('freshuser')`; `getSubmitButton().click()` | `cy.url().should('match', /\/(dashboard)?$/)` |

- [ ] **Step 9: `auth/forbidden.spec.ts`**

```ts
  beforeEach: cy.clearCookies(); cy.intercept('/api/me?fetch=features', { fixture: 'me' });

  it('should land on /forbidden for a private project as a non-member', () => {
    // given
    cy.intercept('/api/johndoe/p1*', { statusCode: 403, body: { error: { type: 'PermissionException', message: 'forbidden' } } });
    // when
    cy.visit('/johndoe/p1');
    // then
    cy.url().should('contain', '/forbidden');
  });
```

- [ ] **Step 10: `auth/not-found.spec.ts`**

```ts
  it('should land on /not-found for a missing project', () => {
    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/johndoe/nope*', { statusCode: 404, body: { error: { type: 'NotFoundException', entity: 'Project' } } });
    cy.visit('/johndoe/nope');
    cy.url().should('contain', '/not-found');
  });

  it('should land on /not-found for an unknown top-level route', () => {
    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.visit('/this-route-does-not-exist', { failOnStatusCode: false });
    cy.url().should('contain', '/not-found');
  });
```

- [ ] **Step 11: Verify + commit**

```bash
cd /Users/renepanzar/Development/translatr/ui
npx nx lint translatr-e2e
npx nx e2e translatr-e2e
```

Iterate. Then:

```bash
git add apps/translatr-e2e
git commit -m "test(e2e): access tokens + auth/guard/entry pages

user access-tokens list/create/edit/delete (/api/accesstoken(s)), MyselfGuard
block; login provider render + unauth redirect, registration validation,
403 -> /forbidden, 404 -> /not-found.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 3: Phase 3 — Users, Projects, Activity, Project overview

**Files:**
- Create: `src/support/users-page.po.ts`, `src/support/projects-page.po.ts`, `src/support/user/user-activity-page.po.ts`, `src/support/project/project-activity-page.po.ts`
- Create fixtures: `src/fixtures/{users,users-page2,users-empty,users-search,projects,projects-search,project-created}.json`, `src/fixtures/johndoe/activities-page2.json`
- Create: `src/integration/users/users.spec.ts`, `src/integration/projects/projects.spec.ts`, `src/integration/user/activity/user-activity.spec.ts`, `src/integration/project/activity/project-activity.spec.ts`
- Modify: `src/integration/project/project.spec.ts`

**Interfaces:**
- Consumes: `me.json`, `johndoe.json`, `johndoe/projects.json`, `johndoe/p1.json`, `johndoe/p1/*` fixtures, `johndoe/activities.json`, `statistics.json`, `activities-aggregated.json`.
- Produces: `UsersPage`, `ProjectsPage`, `UserActivityPage`, `ProjectActivityPage`.

- [ ] **Step 1: Page objects**

```ts
// src/support/users-page.po.ts
import { Page } from './page.po';
export class UsersPage extends Page {
  navigateTo(): UsersPage { cy.visit('/users'); return this; }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-user-list mat-list-item, app-user-list a.user'); }
  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-filter-field input, input[type="search"]'); }
  getEmptyView(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('.empty-view, dev-empty-view, app-empty-view'); }
  getLoadMoreButton(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('button.more, .load-more button, button').contains('more'); }
}
```

```ts
// src/support/projects-page.po.ts
import { Page } from './page.po';
export class ProjectsPage extends Page {
  navigateTo(): ProjectsPage { cy.visit('/projects'); return this; }
  getCards(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-project-card, .project-card'); }
  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-filter-field input, input[type="search"]'); }
}
```

```ts
// src/support/user/user-activity-page.po.ts
import { Page } from '../page.po';
export class UserActivityPage extends Page {
  constructor(private readonly username: string) { super(); }
  navigateTo(): UserActivityPage { cy.visit(`/${this.username}/activity`); return this; }
  getActivityRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-activity-list mat-list-item, app-activity-list .activity'); }
}
```

```ts
// src/support/project/project-activity-page.po.ts
import { Page } from '../page.po';
export class ProjectActivityPage extends Page {
  constructor(private readonly username: string, private readonly projectName: string) { super(); }
  navigateTo(): ProjectActivityPage { cy.visit(`/${this.username}/${this.projectName}/activity`); return this; }
  getActivityRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-activity-list mat-list-item, app-activity-list .activity'); }
  getActivityGraph(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('dev-activity-graph, app-activity-graph'); }
}
```

Selectors to TDD: `apps/translatr/src/app/modules/pages/users-page/*`, `projects-page/*`, `shared/user-list/*`, `shared/activity-list/*`, `shared/project-card-list/*`, `modules/filter-field/*`.

- [ ] **Step 2: Fixtures**

`users.json` — `PagedList<User>` with ~3 users (John Doe, Jane Smith, Sophia O'Reilly — copy shapes from `johndoe.json`/`janesmith.json`), `offset` 0, `limit` 20, `total` 3, `hasNext` false.

`users-page2.json` — a different 3 users, `offset` 20, `hasPrev` true, `hasNext` false. (For "load more" — verify how the user-list paginates: `?offset=` or a cursor. Read `apps/translatr/src/app/utils/paged-list-utils.ts` and `user-list.component`.)

`users-empty.json` — `list: []`, `total: 0`.

`users-search.json` — one user (Jane Smith), `total` 1 (for the `?search=jane` assertion).

`projects.json` — `PagedList<Project>`, 3 projects, copy a row from `johndoe/projects.json`'s `list`.

`projects-search.json` — one project, `total` 1.

`project-created.json` — a bare `Project` returned by `POST /api/project`, with `name` `"newproj"`, `ownerUsername` `"johndoe"` (shape from a `johndoe/projects.json` `list` row, minus the `members` array or with a single Owner member).

`johndoe/activities-page2.json` — a second page of the `johndoe/activities.json` shape (different `id`s, `offset` bumped, `hasPrev` true).

- [ ] **Step 3: `users/users.spec.ts`**

```ts
import { UsersPage } from '../../support/users-page.po';

describe('Users', () => {
  let page: UsersPage;
  beforeEach(() => {
    page = new UsersPage();
    cy.clearCookies();
    cy.intercept('/api/me?fetch=features', { fixture: 'me' });
    cy.intercept('/api/user*', { fixture: 'users' });
  });

  it('should list users', () => {
    page.navigateTo();
    page.getRows().should('have.length', 3);
  });

  it('should filter on search input', () => {
    // given
    cy.intercept('/api/user*search=*', { fixture: 'users-search' }).as('search');
    // when
    page.navigateTo();
    page.getSearchField().type('jane');
    // then
    cy.wait('@search').its('request.url').should('contain', 'search=jane');
    page.getRows().should('have.length', 1);
  });

  it('should show an empty view when there are no users', () => {
    cy.intercept('/api/user*', { fixture: 'users-empty' });
    page.navigateTo();
    page.getEmptyView().should('be.visible');
  });

  it('should navigate to a user page on row click', () => {
    page.navigateTo();
    page.getRows().first().click();
    cy.url().should('match', /\/[a-z]+$/);
  });
});
```

- [ ] **Step 4: `projects/projects.spec.ts`**

`beforeEach`: `me`, `cy.intercept('/api/project*', { fixture: 'projects' })`. `it`s:

| `it` | given | when | then |
|---|---|---|---|
| lists projects | — | `navigateTo()` | `getCards().should('have.length', 3)` |
| filters on search | `cy.intercept('/api/project*search=*', { fixture: 'projects-search' }).as('s')` | `navigateTo()`; `getSearchField().type('p1')` | `cy.wait('@s')`; `getCards().should('have.length', 1)` |
| creates a project from the FAB and redirects | `cy.intercept('POST', '/api/project', { fixture: 'project-created' })`; `cy.intercept('/api/johndoe/newproj*', { fixture: 'project-created' })` | `navigateTo()`; `getFloatingActionButton().click()`; fill dialog name `newproj`; `getDialog().find('button[transloco="button.save"], button.save').click()` | `cy.url().should('contain', '/johndoe/newproj')` |

- [ ] **Step 5: `user/activity/user-activity.spec.ts`**

```ts
  beforeEach: cy.intercept('/api/me?fetch=features', { fixture: 'me' });
             cy.intercept('/api/johndoe', { fixture: 'johndoe' });
             cy.intercept('/api/user/*/activity*', { fixture: 'johndoe/activities' });
             cy.intercept('/api/activities*', { fixture: 'johndoe/activities' });

  it('should render the activity list', () => {
    new UserActivityPage('johndoe').navigateTo();
    // then — assert > 0 rows (johndoe/activities.json list length)
    cy.get('app-activity-list').should('exist');
  });
```

(Second `it` for an activity link navigating — TDD the link selector against `shared/activity-list/activity-*-link` components.)

- [ ] **Step 6: `project/activity/project-activity.spec.ts`**

`beforeEach`: the standard `johndoe/p1` project intercept block (copy from `project/project.spec.ts`) plus `cy.intercept('/api/project/*/activity*', { fixture: 'johndoe/p1/activities' })`. `it`s:

```ts
  it('should render the project activity list and graph', () => {
    new ProjectActivityPage('johndoe', 'p1').navigateTo();
    cy.get('app-activity-list').should('exist');
    cy.get('dev-activity-graph, app-activity-graph').should('exist');
  });
```

- [ ] **Step 7: Extend `project/project.spec.ts`**

Keep the existing `beforeEach` + the page-name `it`. Add:

```ts
  it('should show locale, key and message counts on the overview', () => {
    page.navigateTo();
    // then — assert the info metrics render. Selectors from
    // apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.html
    cy.get('app-project-info, dev-metric').should('exist');
  });

  it('should render the infographic only when its feature flag is present', () => {
    // given — me WITH the ProjectInfographic feature
    cy.intercept('/api/me?fetch=features', { body: { ...meWithInfographicFeature } });
    // when
    page.navigateTo();
    // then
    cy.get('app-project-infographic').should('exist');
  });
```

(For the feature-flag `it`, add fixture `me-infographic.json` = `me.json` plus the feature list Angular's `feature-flag` directive checks — read `libs/translatr-components/src/lib/modules/feature-flag/feature-flag.directive.ts` for the `me.features` / `/api/me?fetch=features` shape. If the shape is complex, do a negative assertion instead: default `me` → `app-project-infographic` `should('not.exist')`.)

- [ ] **Step 8: Verify + commit**

```bash
npx nx lint translatr-e2e && npx nx e2e translatr-e2e
git add apps/translatr-e2e
git commit -m "test(e2e): users / projects list pages, activity pages, project overview

/users + /projects list/search/paginate/empty/create; user + project activity
lists; project overview metrics + infographic feature-flag gate.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 4: Phase 4 — Add-flow persistence, Editor persistence, Navbar

**Files:**
- Create: `src/support/nav/navbar.po.ts`
- Create fixtures: `src/fixtures/johndoe/p1/{locale-created,locales-added,search-results}.json`, `src/fixtures/me-language-switcher.json`
- Modify: `src/integration/project/locales/project-locales-add.spec.ts`, `src/integration/project/keys/project-keys-add.spec.ts`
- Create: `src/integration/project/keys/project-key-editor-save.spec.ts`, `src/integration/project/locales/project-locale-editor-save.spec.ts`, `src/integration/nav/navbar.spec.ts`

**Interfaces:**
- Consumes: `johndoe/p1/*` fixtures, `johndoe/p3/{key-created,keys-added}.json` (exist), `me.json`, `me-save-behavior-saveandnext.json` (exists).
- Produces: `NavbarPage`.

- [ ] **Step 1: Extend `project-locales-add.spec.ts` with a persistence `it`**

Keep the existing 3 `it`s (button visible, dialog show, dialog hide). Add:

```ts
  it('should add the locale and show the new row on save', () => {
    // given
    cy.intercept('POST', '/api/locale', { fixture: 'johndoe/p1/locale-created' });
    cy.intercept('/api/project/*/locales*', { fixture: 'johndoe/p1/locales-added' });

    // when
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialog().find('mat-form-field.name input').type('de');
    page.getDialog().find('button[transloco="button.save"], button.save').click();

    // then
    page.getDialog().should('have.length', 0);
    cy.get('app-locale-list').should('contain.text', 'de');
  });
```

Add fixtures:
- `johndoe/p1/locale-created.json` — a bare `Locale` `{ id, whenCreated, whenUpdated, name: "de", displayName: "German", projectId: "bc5f3fa7-3d9f-4aee-9457-0f6c4d5f5486", projectName: "p1", projectOwnerUsername: "johndoe", pathName: "de" }` (copy field set from a row of `johndoe/p1/locales.json`).
- `johndoe/p1/locales-added.json` — `johndoe/p1/locales.json` with the `de` locale appended to `list`, `total` +1.

- [ ] **Step 2: Extend `project-keys-add.spec.ts` with a persistence `it`**

Same shape, using the existing `johndoe/p3` fixtures (that spec may already target `p3` — check; if it targets `p1`, add `johndoe/p1/key-created.json` + `keys-added.json` mirroring the `p3` ones):

```ts
  it('should add the key and show the new row on save', () => {
    cy.intercept('POST', '/api/key', { fixture: 'johndoe/p3/key-created' });
    cy.intercept('/api/project/*/keys*', { fixture: 'johndoe/p3/keys-added' });
    page.navigateTo();
    page.getFloatingActionButton().click();
    page.getDialog().find('mat-form-field.name input').type('home.title');
    page.getDialog().find('button[transloco="button.save"], button.save').click();
    page.getDialog().should('have.length', 0);
    cy.get('app-key-list').should('contain.text', 'home.title');
  });
```

- [ ] **Step 3: `project/keys/project-key-editor-save.spec.ts`**

`beforeEach`: copy the `beforeEach` from the existing `project/keys/project-key-editor.spec.ts` (it already stubs `/api/johndoe/p1/keys/k1`, locales, messages). `it`s:

```ts
  it('should persist a translation on Save', () => {
    // given
    cy.intercept('PUT', '/api/message', (req) => req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } }));
    cy.intercept('/api/me?fetch=features', { fixture: 'me' }); // save-behavior: save

    // when — from the key editor: activate a locale in the sidebar, type, Save.
    // Mirror the sidebar-activation steps in project-key-editor.spec.ts.
    // ...activate locale... editor.type('Hallo'); cy.get('button.save, button[transloco="editor.save"]').click();

    // then — editor keeps the value / progress updates
    // cy.get('...editor value...').should('contain.text', 'Hallo');
  });

  it('should advance to the next locale on "Save and next"', () => {
    cy.intercept('/api/me?fetch=features', { fixture: 'me-save-behavior-saveandnext' });
    cy.intercept('PUT', '/api/message', (req) => req.reply({ statusCode: 200, body: { ...req.body, id: 'm-new' } }));
    // when — Save; then the sidebar selection moves to the next locale
    // then — assert the active sidebar item changed
  });
```

(This one leans hardest on TDD — reuse the sidebar-activation + editor-typing steps already proven in `project-key-editor.spec.ts` and `project-locale-editor.spec.ts`; add the `PUT /api/message` stub + the persist/advance assertions.)

- [ ] **Step 4: `project/locales/project-locale-editor-save.spec.ts`** — symmetric to Step 3, from the locale editor iterating keys, `beforeEach` copied from `project/locales/project-locale-editor.spec.ts`.

- [ ] **Step 5: `nav/navbar.po.ts` + `nav/navbar.spec.ts`**

```ts
// src/support/nav/navbar.po.ts
import { Page } from '../page.po';
export class NavbarPage extends Page {
  navigateTo(): NavbarPage { cy.visit('/dashboard'); return this; }
  getSearchInput(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-search-bar input, .search-bar input'); }
  getSearchResults(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-search-bar .result, .search-results a'); }
  getLanguageSwitcher(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-auth-bar-language-switcher, auth-bar-language-switcher'); }
  getUserMenuTrigger(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('app-auth-bar-item button, .auth-bar-item button'); }
}
```

`navbar.spec.ts` `beforeEach`: `me`, `statistics`, `activities-aggregated`, and the dashboard intercepts (copy from `dashboard/dashboard.spec.ts`). `it`s:

| `it` | given | when | then |
|---|---|---|---|
| search shows results and navigates | `cy.intercept('/api/search*', { fixture: 'johndoe/p1/search-results' }).as('search')` (confirm the real search endpoint — grep `search` in `libs/translatr-sdk` + `search-bar.component.ts`) | `navigateTo()`; `getSearchInput().type('p1')` | `cy.wait('@search')`; `getSearchResults().should('have.length.greaterThan', 0)`; click first → `cy.url()` changed |
| language switcher present / toggles | — | `navigateTo()` | `getLanguageSwitcher().should('exist')` (deepen to an actual language change if the switcher is not feature-flag-hidden for default `me`) |
| user menu exposes profile/settings/logout | — | `navigateTo()`; `getUserMenuTrigger().click()` | `cy.get('.mat-mdc-menu-panel a, [mat-menu-item]').should('contain.text', 'settings')` etc. |

Add `johndoe/p1/search-results.json` shaped to the search endpoint's response (projects + keys + locales + users arrays, or a flat list — match what `search-bar.component` consumes).

- [ ] **Step 6: Verify + commit**

```bash
npx nx lint translatr-e2e && npx nx e2e translatr-e2e
git add apps/translatr-e2e
git commit -m "test(e2e): add-flow persistence, editor message save, navbar

locale/key add now assert the new row (POST /api/locale, /api/key); key + locale
editor Save persists via PUT /api/message and 'save and next' advances; navbar
global search results + navigation, language switcher, user menu.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Task 5: Phase 5 — `translatr-admin-e2e` full rebuild

**Files:**
- Delete: `ui/apps/translatr-admin-e2e/src/integration/app.spec.ts`, `src/support/app.po.ts`, `src/support/commands.ts`, `src/support/index.ts`, `src/fixtures/example.json`
- Create: `ui/apps/translatr-admin-e2e/src/support/{page,dashboard-page,feature-flags-page,access-tokens-page,users-page,projects-page}.po.ts`
- Create fixtures: `ui/apps/translatr-admin-e2e/src/fixtures/{me,me-non-admin,users,user,user-updated,projects,feature-flags,feature-flag-created,feature-flag-updated,access-tokens,access-token-updated,statistics,activities}.json`
- Create: `ui/apps/translatr-admin-e2e/src/integration/{dashboard,feature-flags,access-tokens,users,projects,auth}.spec.ts`

**Interfaces:**
- Consumes: nothing (fresh). The `cypress.config.js` + `project.json` are already migrated — do not touch them.
- Produces: a real `translatr-admin-e2e` suite.

- [ ] **Step 1: Remove the placeholder scaffolding**

```bash
cd /Users/renepanzar/Development/translatr/ui
git rm apps/translatr-admin-e2e/src/integration/app.spec.ts \
       apps/translatr-admin-e2e/src/support/app.po.ts \
       apps/translatr-admin-e2e/src/support/commands.ts \
       apps/translatr-admin-e2e/src/support/index.ts \
       apps/translatr-admin-e2e/src/fixtures/example.json
```

(`supportFile: false` in `cypress.config.js` — `commands.ts`/`index.ts` were never loaded. Confirm `apps/translatr-admin-e2e/tsconfig.e2e.json` / `tsconfig.json` don't reference the deleted files; if they do, drop the reference.)

- [ ] **Step 2: `Page` base + page objects**

```ts
// src/support/page.po.ts
export class Page {
  getPageName(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('h1.page'); }
  getFloatingActionButton(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('.floating-action-btn'); }
  getDialog(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('mat-dialog-container'); }
}
```

```ts
// src/support/dashboard-page.po.ts
import { Page } from './page.po';
export class DashboardPage extends Page {
  navigateTo(): DashboardPage { cy.visit('/'); return this; }
  getMetric(cls: string): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get(`dev-metric.${cls}`); }
}
```

```ts
// src/support/feature-flags-page.po.ts
import { Page } from './page.po';
export class FeatureFlagsPage extends Page {
  navigateTo(): FeatureFlagsPage { cy.visit('/featureflags'); return this; }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('.feature-row'); }
  getRow(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return cy.get('.feature-row').contains('.feature-row', featureKey);
  }
  getToggle(featureKey: string): Cypress.Chainable<JQuery<HTMLElement>> {
    return this.getRow(featureKey).find('.feature-actions button');
  }
}
```

```ts
// src/support/access-tokens-page.po.ts
import { Page } from './page.po';
export class AccessTokensPage extends Page {
  navigateTo(): AccessTokensPage { cy.visit('/accesstokens'); return this; }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table tbody tr, .access-token'); }
}
```

```ts
// src/support/users-page.po.ts
import { Page } from './page.po';
export class UsersPage extends Page {
  navigateTo(): UsersPage { cy.visit('/users'); return this; }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table tbody tr'); }
  getRow(name: string): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table tbody tr').contains('tr', name); }
  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table input[type="search"], app-filter-field input'); }
  getEditButton(name: string): Cypress.Chainable<JQuery<HTMLElement>> { return this.getRow(name).find('button').first(); }
}
```

```ts
// src/support/projects-page.po.ts
import { Page } from './page.po';
export class ProjectsPage extends Page {
  navigateTo(): ProjectsPage { cy.visit('/projects'); return this; }
  getRows(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table tbody tr'); }
  getSearchField(): Cypress.Chainable<JQuery<HTMLElement>> { return cy.get('entity-table input[type="search"], app-filter-field input'); }
}
```

Selectors confirmed from templates: dashboard-info `h1.page` "Dashboard" + `dev-metric.user.count` / `.project.count` / `.access-token.count` / `.activity.count`; dashboard-users `h1.page` "Users" + `entity-table` + `td .name a.link` + edit `button mat-icon-button` (`<mat-icon>edit`) + `confirm-button` + `.floating-action-btn`; feature-flags `h1.page` "Features" + `.feature-row` + `.feature-actions button` with `.enabled`/`.disabled` + `<mat-icon>toggle_on`/`toggle_off`; user-edit-dialog `dev-user-edit-form` + `button[transloco="button.save"]` / `button[transloco="button.cancel"]`. Admin routes: `/`, `/users`, `/users/:id`, `/projects`, `/accesstokens`, `/featureflags`, `/forbidden`.

- [ ] **Step 3: Fixtures**

`me.json` — admin user:

```json
{
  "id": "5e15a05d-c583-45a0-84fa-1e770b2a4531",
  "whenCreated": "2019-09-05T15:57:47.691Z",
  "whenUpdated": "2019-09-05T15:57:47.691Z",
  "name": "John Doe",
  "username": "johndoe",
  "email": "john.doe@gmail.com",
  "emailHash": "6d041874043a1820c908f747e8a608bc",
  "role": "Admin",
  "memberships": []
}
```

`me-non-admin.json` — same but `"role": "User"`.

`statistics.json` — `{ "userCount": 25, "projectCount": 15, "activityCount": 100 }`.

`users.json` — `PagedList<User>` with 3 users (`total` 3). `user.json` — one bare `User` (for `/users/:id`). `user-updated.json` — that user with `"role": "Admin"`.

`projects.json` — `PagedList<Project>` with 3 (`total` 3).

`feature-flags.json` — `PagedList<UserFeatureFlag>`. Read `Feature` enum + `UserFeatureFlag` model (`libs/translatr-model`): fields likely `{ id, whenCreated, whenUpdated, userId, feature, enabled }`. Include ONE existing flag row (e.g. `feature: "LanguageSwitcher", enabled: true`), leaving the other `Feature` values with no row:

```json
{
  "list": [
    {
      "id": "f1000000-0000-0000-0000-000000000001",
      "whenCreated": "2020-01-01T10:00:00.000Z",
      "whenUpdated": "2020-01-01T10:00:00.000Z",
      "userId": "5e15a05d-c583-45a0-84fa-1e770b2a4531",
      "feature": "LanguageSwitcher",
      "enabled": true
    }
  ],
  "offset": 0, "limit": 20, "total": 1, "hasPrev": false, "hasNext": false
}
```

`feature-flag-created.json` — a new row `{ ...same shape, feature: "ProjectInfographic", enabled: true }`. `feature-flag-updated.json` — the `LanguageSwitcher` row with `enabled: false`.

`access-tokens.json` — `PagedList<AccessToken>` 2 rows. `access-token-updated.json` — first row renamed.

`activities.json` — `PagedList<Activity>` (for the dashboard info panel's `activities$`).

Confirm exact model field names by reading `libs/translatr-model/src/lib/translatr-model.*.ts` before finalizing.

- [ ] **Step 4: `dashboard.spec.ts`**

```ts
import { DashboardPage } from '../support/dashboard-page.po';

describe('Admin Dashboard', () => {
  let page: DashboardPage;
  beforeEach(() => {
    page = new DashboardPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/statistics', { fixture: 'statistics' });
    cy.intercept('/api/user*', { fixture: 'users' });
    cy.intercept('/api/project*', { fixture: 'projects' });
    cy.intercept('/api/accesstokens*', { fixture: 'access-tokens' });
    cy.intercept('/api/activities*', { fixture: 'activities' });
  });

  it('should render the dashboard with the page title', () => {
    page.navigateTo();
    page.getPageName().should('have.text', 'Dashboard');
  });

  it('should render the metric cards', () => {
    page.navigateTo();
    page.getMetric('user').should('exist');
    page.getMetric('project').should('exist');
    page.getMetric('access-token').should('exist');
    page.getMetric('activity').should('exist');
  });
});
```

- [ ] **Step 5: `feature-flags.spec.ts`**

```ts
import { FeatureFlagsPage } from '../support/feature-flags-page.po';

describe('Admin Feature Flags', () => {
  let page: FeatureFlagsPage;
  beforeEach(() => {
    page = new FeatureFlagsPage();
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me' });
    cy.intercept('/api/featureflags*', { fixture: 'feature-flags' });
  });

  it('should render the Features page with one row per feature', () => {
    page.navigateTo();
    page.getPageName().should('have.text', 'Features');
    page.getRows().should('have.length.greaterThan', 0);
  });

  it('should CREATE a flag when toggling a feature that has no row yet', () => {
    // given
    cy.intercept('POST', '/api/featureflag', { fixture: 'feature-flag-created' }).as('create');
    // when
    page.navigateTo();
    page.getToggle('ProjectInfographic').click();
    // then
    cy.wait('@create').its('request.body').should('include', { feature: 'ProjectInfographic', enabled: true });
    page.getToggle('ProjectInfographic').find('mat-icon').should('have.text', 'toggle_on');
    page.getToggle('ProjectInfographic').should('have.class', 'enabled');
  });

  it('should UPDATE a flag when toggling a feature that already has a row', () => {
    // given
    cy.intercept('PUT', '/api/featureflag', { fixture: 'feature-flag-updated' }).as('update');
    // when
    page.navigateTo();
    page.getToggle('LanguageSwitcher').click();
    // then
    cy.wait('@update').its('request.body').should('include', { enabled: false });
    page.getToggle('LanguageSwitcher').find('mat-icon').should('have.text', 'toggle_off');
  });
});
```

(`onToggle` in `dashboard-feature-flags.component.ts`: existing `row.flag` → `updateFeatureFlag` (PUT); no flag → `createFeatureFlag` (POST). The `.feature-row .sub-title` shows the raw `Feature` key — `getRow(featureKey)` matches on it.)

- [ ] **Step 6: `access-tokens.spec.ts`**

`beforeEach`: `me`, `cy.intercept('/api/accesstokens*', { fixture: 'access-tokens' })`. `it`s:

```ts
  it('should list access tokens', () => {
    new AccessTokensPage().navigateTo();
    cy.get('entity-table, .access-token').should('exist');
  });

  it('should persist an edit', () => {
    cy.intercept('PUT', '/api/accesstoken', { fixture: 'access-token-updated' }).as('update');
    const page = new AccessTokensPage().navigateTo();
    // open the row's edit affordance (dialog or inline), change the name, save
    // page.getRows().first().find('button').first().click();
    // cy.get('mat-dialog-container input').type('{selectall}renamed');
    // cy.get('button[transloco="button.save"]').click();
    cy.wait('@update');
  });
```

(Read `dashboard-access-tokens.component.html` for the edit affordance.)

- [ ] **Step 7: `users.spec.ts`**

`beforeEach`: `me`, `cy.intercept('/api/user*', { fixture: 'users' })`, `cy.intercept('/api/user/*', { fixture: 'user' })`. `it`s:

| `it` | given | when | then |
|---|---|---|---|
| lists users | — | `navigateTo()` | `getRows().should('have.length', 3)` |
| filters on search | `cy.intercept('/api/user*search=*', { fixture: 'users' }).as('s')` (or a filtered fixture) | `navigateTo()`; `getSearchField().type('jane')` | `cy.wait('@s').its('request.url').should('contain', 'search=jane')` |
| opens a user detail | — | `navigateTo()`; `getRows().first().find('a.link').click()` | `cy.url().should('match', /\/users\/[0-9a-f-]+$/)` |
| edits a user's role | `cy.intercept('PUT', '/api/user*', { fixture: 'user-updated' }).as('update')` | `navigateTo()`; `getEditButton('John Doe').click()`; change `role` in `dev-user-edit-form`; `cy.get('button[transloco="button.save"]').click()` | `cy.wait('@update')` |

- [ ] **Step 8: `projects.spec.ts`**

`beforeEach`: `me`, `cy.intercept('/api/project*', { fixture: 'projects' })`. `it`s: lists (`getRows().should('have.length', 3)`), filters on search (`.as('s')`, assert `search=` in URL).

- [ ] **Step 9: `auth.spec.ts`**

```ts
  it('should redirect a non-admin to /forbidden', () => {
    cy.clearCookies();
    cy.intercept('/api/me*', { fixture: 'me-non-admin' });
    cy.visit('/users');
    cy.url().should('contain', '/forbidden');
  });
```

(`translatr-admin`'s `AuthGuard` — read `apps/translatr-admin/src/app/guards/auth.guard.ts` — asserts whatever it actually does for a non-admin: `/forbidden`, or a redirect away.)

- [ ] **Step 10: Verify + commit**

```bash
cd /Users/renepanzar/Development/translatr/ui
npx nx lint translatr-admin-e2e
npx nx e2e translatr-admin-e2e
```

`nx e2e translatr-admin-e2e` must now be **fully green** (the placeholder that used to fail is deleted). Iterate. Then:

```bash
git add apps/translatr-admin-e2e
git commit -m "test(e2e): rebuild translatr-admin-e2e with a real suite

Replace the Nx placeholder spec. New Page base + 6 page objects + fixtures;
specs for dashboard metrics, feature-flag toggle (POST vs PUT /api/featureflag),
access-token edit, users list/search/detail/role-edit, projects list/search,
and non-admin -> /forbidden.

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>"
```

---

## Self-Review

**Spec coverage:**
- Spec Phase 1 (Project Members, 7 specs) → Task 1 Steps 3–9. ✎
- Spec Phase 2 access tokens (4 specs) + auth/entry (4 specs) → Task 2 Steps 3–10. ✎ (spec's `/api/access_token(s)` corrected to verified `/api/accesstoken(s)` in Global Constraints + Task 2.)
- Spec Phase 3 `/users`, `/projects`, activity ×2, project overview → Task 3 Steps 3–7. ✎
- Spec Phase 4 add-flow ×2, editor save ×2, navbar → Task 4 Steps 1–5. ✎
- Spec Phase 5 config migration → already done by the Cypress migration; Task 5 Step 1 does only the placeholder removal (noted in Global Constraints). Page objects, fixtures, 6 specs → Task 5 Steps 2–9. ✎ (spec's `/api/user_feature_flag` corrected to verified `/api/featureflag` in Task 5 Step 5.)
- Spec "Verification" (per-phase `nx lint` + `nx e2e` + one commit) → the verification loop section + every task's final Step. ✎
- Spec "Out of scope" (no live-backend suite, no `cypress.config.js` change, no component/unit changes, no unrelated refactors) → Global Constraints. ✎
- Spec "Risks" (selector drift, admin API paths, guard redirect behaviour) → the verification loop's selector-fix guidance; API paths pinned in Global Constraints; guard behaviour "assert what the app does" noted in Task 2 Step 6 / Task 5 Step 9. ✎

**Placeholder scan:** Fixture JSON is concrete. Spec skeletons are real code. The deliberately-deferred items are (a) exact CSS selectors for a handful of form-fill interactions (member role select, editor sidebar activation, search endpoint) — the spec itself mandates TDD-against-the-app for these and each task names the component file to read and an existing spec to mirror; (b) the `me?fetch=features` feature-flag payload shape (Task 3 Step 7) — task says read the directive and gives a negative-assertion fallback. No "TODO"/"handle edge cases"/"similar to Task N" (patterns are repeated, not referenced).

**Type consistency:** Page-object method names are consistent within and across tasks (`getRows`, `getSearchField`, `getFloatingActionButton` from `Page`, `getDialog`, `navigateTo`). Intercept URL patterns match the Global Constraints table everywhere they appear. Fixture filenames referenced in specs match the Create lists. Commit-message prefix `test(e2e):` consistent across all five phases.

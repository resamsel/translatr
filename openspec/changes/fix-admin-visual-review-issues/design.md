## Context

See proposal.md - Why. Two independent Angular Material fixes in the Nx monorepo under `ui/`: the admin Users table (`ui/apps/translatr-admin/.../users/`) and `mat-form-field` appearance, some of which lives in the shared `ui/libs/translatr-components` lib consumed by both `translatr-admin` and `translatr` apps.

## Goals / Non-Goals

**Goals:**
- Fix the two specific visual defects from issue #302 without a broader design-system pass.
- Keep the fix minimal: touch only the columns/attributes identified, not unrelated table or form styling.

**Non-Goals:**
- Redesigning the Users table (e.g. adding sorting/filtering) beyond removing the two columns.
- Adding a global `MAT_FORM_FIELD_DEFAULT_OPTIONS` provider to enforce "outline" app-wide — even with every known `fill` field now migrated explicitly, a global default would silently affect any future field too, which this change does not attempt.

## Decisions

- **Remove columns instead of hiding/collapsing them responsively**: The proposal removes `email` and `when_created` outright rather than making the table responsive (e.g. hiding columns below a breakpoint), because the underlying user data remains accessible via other means and a responsive table adds complexity disproportionate to a two-column overflow. Confirmed acceptable: this data is not the only way to reach it (see Open Questions if that assumption is wrong).
- **Fix `entity-table`'s search field appearance directly**: Since `entity-table` is shared with the non-admin `translatr` app, changing its default appearance to `outline` affects both apps' entity tables, not just admin's. This is accepted as in-scope because it's the same shared component the admin Users table itself uses, and per-instance overriding would require adding a new input to `entity-table` just to special-case admin — more complex than aligning the one shared field.
- **Extended to the remaining `appearance="fill"` fields in the `translatr` app**: after the admin-only fix, the user asked to also normalize `list-header`, `key-editor-page`, `locale-editor-page`, and the shared navbar `search-bar` (all previously `fill`) to `outline`, so every form field in both apps is now consistent - not just admin's.

## Risks / Trade-offs

- [Removing `entity-table`'s `fill` → `outline` also changes the look of the non-admin `translatr` app's list search fields] → Acceptable and arguably a net consistency win (13 fields already use `outline` there); call out in the PR description so reviewers are aware it's not admin-only.
- [Removing email/join-date columns could regress a workflow that relies on scanning them in bulk] → Mitigated by leaving the data available on the per-user detail/edit view; flagged here for review before implementation.

## 1. Root README.md

- [x] 1.1 In the intro paragraph (near the top, describing importable file formats), add the missing `json` file type alongside Play Framework messages, Java properties, and Gettext, and correct the Gettext example path from `i18n/locale/main.po` to `locale/{locale.name}/LC_MESSAGES/message.po` (matching `FileType.Gettext`'s actual target in `ui/apps/translatr/src/app/modules/pages/project-page/project-info/project-info.component.ts`). Verify by reading the updated paragraph against `file-type.ts`'s 4 file types and each one's real target pattern.
- [x] 1.2 In the "Pulling" section, replace the `translatr.pull.target key` reference (removed by `cli-flat-config`/`cli-multi-target-config`) with a description of the current `targets` map (each entry configures where a locale's file is written). Verify by confirming no remaining reference to `pull.target`, `push.target`, or a `translatr.` prefix.

## 2. `ui/apps/cli/README.md`

- [x] 2.1 Remove the claim that the CLI has "the same `.translatr.yml` config format ... as the old Python script" (the Python CLI was removed in `sunset-python-cli`, and the format changed twice since). Verify by confirming no `src/python/translatr.py` reference remains.
- [x] 2.2 Add a short `.translatr.yml` example to the Usage section (flat top-level keys: `endpoint`, `access_token`, `project_id`, `default_locale`, `targets` map with at least one entry) so a reader can see the current shape without cross-referencing the app's project page. Verify the example matches `ui/apps/cli/src/config.ts`'s `TranslatrConfig`/`writeInitConfig` shape (flat, no `translatr:` wrapper).

## 3. Cross-check

- [x] 3.1 Grep both README files for any remaining `translatr:` wrapper mention, `pull.target`/`push.target`, or `translatr.py` reference, and confirm none remain outside of intentional historical/changelog context.

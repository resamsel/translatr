# Changelog

## [v4.0.0](https://github.com/resamsel/translatr/tree/4.0.0) (2026-09-15)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.3.3...v4.0.0)

**Implemented enhancements:**

- Migrate backend from Play Framework to Quarkus [\#225](https://github.com/resamsel/translatr/pull/225) ([resamsel](https://github.com/resamsel))
- Translatr CLI: Allow for multiple targets within one .translatr.yml config [\#336](https://github.com/resamsel/translatr/issues/336)
- Adopt contract-first OpenAPI: single openapi.yaml as source of truth, generate DTOs/interfaces [\#256](https://github.com/resamsel/translatr/issues/256)
- Show admin sidebar by default on large screens [\#243](https://github.com/resamsel/translatr/issues/243)
- Admin: manage feature flags for any user \(user filter on the Feature Flags page\) [\#240](https://github.com/resamsel/translatr/issues/240)
- Add OpenTelemetry + SigNoz observability to verify load-test performance [\#239](https://github.com/resamsel/translatr/issues/239)
- Allow feature flags to be enabled globally [\#227](https://github.com/resamsel/translatr/issues/227)
- Migrate E2E tests to Playwright [\#226](https://github.com/resamsel/translatr/issues/226)
- Re-introduce Swagger API Documentation [\#214](https://github.com/resamsel/translatr/issues/214)

**Fixed bugs:**

- Translatr CLI: the API paths are wrong [\#331](https://github.com/resamsel/translatr/issues/331)
- CLI: unhelpful raw error printed on API 404 \(e.g. `translatr push`\) [\#329](https://github.com/resamsel/translatr/issues/329)
- Login page: identity provider labels hard to read and not vertically centered [\#310](https://github.com/resamsel/translatr/issues/310)
- Flaky test: ActivityResourceAggregatedCriteriaTest.findAggregatedActivity\_projectIdAndUserId\_areNotSwapped [\#304](https://github.com/resamsel/translatr/issues/304)
- Visual review problems [\#302](https://github.com/resamsel/translatr/issues/302)
- generator: updateRandomProject crashes on a project with no description [\#297](https://github.com/resamsel/translatr/issues/297)
- UserService.create\(\) has no generated endpoint — user registration is broken [\#296](https://github.com/resamsel/translatr/issues/296)
- Admin UI must only be reachable by admins [\#289](https://github.com/resamsel/translatr/issues/289)
- Restore logout under multi-tenant OIDC [\#258](https://github.com/resamsel/translatr/issues/258)
- Only Keycloak can be used as auth provider [\#255](https://github.com/resamsel/translatr/issues/255)
- Fix failing deployment on Heroku [\#247](https://github.com/resamsel/translatr/issues/247)
- After re-login always gets redirected to main page [\#245](https://github.com/resamsel/translatr/issues/245)
- Tab texts are too dark on the darkened background [\#233](https://github.com/resamsel/translatr/issues/233)
- UI: further Material 22 \(MDC\) migration regressions [\#230](https://github.com/resamsel/translatr/issues/230)
- Saving with shortkey doesn't work any longer [\#229](https://github.com/resamsel/translatr/issues/229)
- Fix UI issues after Angular upgrades [\#228](https://github.com/resamsel/translatr/issues/228)

**Closed issues:**

- CLI: support loading .env into .translatr.yml substitution [\#333](https://github.com/resamsel/translatr/issues/333)
- Admin UI: access tokens table never shows the User column [\#317](https://github.com/resamsel/translatr/issues/317)
- Use constructor injection in Quarkus components [\#286](https://github.com/resamsel/translatr/issues/286)
- Route AbstractService's consumers through generated TypeScript API clients [\#282](https://github.com/resamsel/translatr/issues/282)
- Upgrade Gradle to latest version [\#250](https://github.com/resamsel/translatr/issues/250)
- Replace TestBed.get with TestBed.inject in tests [\#167](https://github.com/resamsel/translatr/issues/167)
- Rewrite activity component [\#123](https://github.com/resamsel/translatr/issues/123)
- Make timeouts and limits configurable [\#35](https://github.com/resamsel/translatr/issues/35)

**Merged pull requests:**

- lets-release: drop Github token requirement for non-prerelease releases [\#341](https://github.com/resamsel/translatr/pull/341) ([resamsel](https://github.com/resamsel))
- Bump version to v4.0.0-5 [\#340](https://github.com/resamsel/translatr/pull/340) ([resamsel](https://github.com/resamsel))
- docs: fix stale .translatr.yml wording and CLI screenshot [\#339](https://github.com/resamsel/translatr/pull/339) ([resamsel](https://github.com/resamsel))
- cli: drop .translatr.yml's top-level translatr: wrapper [\#338](https://github.com/resamsel/translatr/pull/338) ([resamsel](https://github.com/resamsel))
- cli: allow multiple targets in .translatr.yml [\#337](https://github.com/resamsel/translatr/pull/337) ([resamsel](https://github.com/resamsel))
- fix\(cli\): correct locales/keys API paths [\#335](https://github.com/resamsel/translatr/pull/335) ([resamsel](https://github.com/resamsel))
- Archive completed CLI changes and sync translatr-cli main spec [\#334](https://github.com/resamsel/translatr/pull/334) ([resamsel](https://github.com/resamsel))
- Support loading .env into .translatr.yml substitution [\#332](https://github.com/resamsel/translatr/pull/332) ([resamsel](https://github.com/resamsel))
- Move cli/ to ui/apps/cli/ and share generated DTO types [\#330](https://github.com/resamsel/translatr/pull/330) ([resamsel](https://github.com/resamsel))
- fix\(deps\): resolve Dependabot vulnerability alerts [\#328](https://github.com/resamsel/translatr/pull/328) ([resamsel](https://github.com/resamsel))
- chore\(release\): bump version to v4.0.0-4 [\#327](https://github.com/resamsel/translatr/pull/327) ([resamsel](https://github.com/resamsel))
- fix\(ci\): replace unmaintained release actions, bump checkout/setup-node [\#326](https://github.com/resamsel/translatr/pull/326) ([resamsel](https://github.com/resamsel))
- Bump version to v4.0.0-3 [\#325](https://github.com/resamsel/translatr/pull/325) ([resamsel](https://github.com/resamsel))
- fix\(release\): commit version bumps on a work branch, not main [\#324](https://github.com/resamsel/translatr/pull/324) ([resamsel](https://github.com/resamsel))
- chore\(openspec\): archive release-loadgenerator-image change [\#323](https://github.com/resamsel/translatr/pull/323) ([resamsel](https://github.com/resamsel))
- fix\(release\): keep docker-compose-loadtest.yml image tags in sync [\#322](https://github.com/resamsel/translatr/pull/322) ([resamsel](https://github.com/resamsel))
- fix\(loadgenerator\): fix Dockerfile build \(lockfile, npmrc, scripts\) [\#321](https://github.com/resamsel/translatr/pull/321) ([resamsel](https://github.com/resamsel))
- ci\(release\): publish loadgenerator image alongside translatr [\#320](https://github.com/resamsel/translatr/pull/320) ([resamsel](https://github.com/resamsel))
- fix\(ui\): show token owner's username in admin access-tokens table [\#318](https://github.com/resamsel/translatr/pull/318) ([resamsel](https://github.com/resamsel))
- docs\(openspec\): close out sunset-python-cli's last task [\#316](https://github.com/resamsel/translatr/pull/316) ([resamsel](https://github.com/resamsel))
- ci\(docker\): build the release image on every push/PR [\#315](https://github.com/resamsel/translatr/pull/315) ([resamsel](https://github.com/resamsel))
- fix\(docker\): don't spawn a nested container for the native image build [\#314](https://github.com/resamsel/translatr/pull/314) ([resamsel](https://github.com/resamsel))
- fix\(docker\): skip tests during image build [\#313](https://github.com/resamsel/translatr/pull/313) ([resamsel](https://github.com/resamsel))
- Sunset the Python CLI, adopt the Bun/TypeScript CLI [\#312](https://github.com/resamsel/translatr/pull/312) ([resamsel](https://github.com/resamsel))
- fix\(ui,admin\): login page contrast/centering, admin health check, finish \#302 [\#311](https://github.com/resamsel/translatr/pull/311) ([resamsel](https://github.com/resamsel))
- Fix admin visual review issues: Users table overflow and inconsistent input appearance [\#309](https://github.com/resamsel/translatr/pull/309) ([resamsel](https://github.com/resamsel))
- Show admin health status; fix broken user registration \(\#296\) [\#308](https://github.com/resamsel/translatr/pull/308) ([resamsel](https://github.com/resamsel))
- feat\(admin\): show overall health status with collapsible provider details [\#306](https://github.com/resamsel/translatr/pull/306) ([resamsel](https://github.com/resamsel))
- fix\(test\): poll every assertion in the async activity-aggregation test [\#305](https://github.com/resamsel/translatr/pull/305) ([resamsel](https://github.com/resamsel))
- refactor\(admin\): give every admin page its own dev-admin-page shell [\#303](https://github.com/resamsel/translatr/pull/303) ([resamsel](https://github.com/resamsel))
- fix\(generator\): guard suffix-toggle mutations against null fields [\#300](https://github.com/resamsel/translatr/pull/300) ([resamsel](https://github.com/resamsel))
- Route AbstractService's consumers through generated TypeScript API clients [\#298](https://github.com/resamsel/translatr/pull/298) ([resamsel](https://github.com/resamsel))
- test\(e2e\): migrate Cypress suites to Playwright [\#293](https://github.com/resamsel/translatr/pull/293) ([resamsel](https://github.com/resamsel))
- feat\(admin\): activity graph on Info, tidy Health page [\#292](https://github.com/resamsel/translatr/pull/292) ([resamsel](https://github.com/resamsel))
- fix\(admin\): guard every admin UI page against non-admins [\#291](https://github.com/resamsel/translatr/pull/291) ([resamsel](https://github.com/resamsel))
- feat\(admin\): let admins manage feature flags for any user [\#290](https://github.com/resamsel/translatr/pull/290) ([resamsel](https://github.com/resamsel))
- refactor\(di\): use constructor injection in Quarkus components [\#288](https://github.com/resamsel/translatr/pull/288) ([resamsel](https://github.com/resamsel))
- feat\(observability\): OpenTelemetry + SigNoz for load-test verification \(\#239\) [\#287](https://github.com/resamsel/translatr/pull/287) ([resamsel](https://github.com/resamsel))
- build\(openapi\): make frontend codegen an Nx target that observes the contract \(\#256\) [\#285](https://github.com/resamsel/translatr/pull/285) ([resamsel](https://github.com/resamsel))
- test\(openapi\): fill in the missing toCriteria mapping tests for FeatureFlag/User \(\#256\) [\#280](https://github.com/resamsel/translatr/pull/280) ([resamsel](https://github.com/resamsel))
- Migrate UserResource to contract-first OpenAPI \(\#256\) [\#279](https://github.com/resamsel/translatr/pull/279) ([resamsel](https://github.com/resamsel))
- Migrate FeatureFlagResource to contract-first OpenAPI \(\#256\) [\#277](https://github.com/resamsel/translatr/pull/277) ([resamsel](https://github.com/resamsel))
- Migrate GlobalFeatureFlagResource to contract-first OpenAPI \(\#256\) [\#276](https://github.com/resamsel/translatr/pull/276) ([resamsel](https://github.com/resamsel))
- Migrate ActivityResource to contract-first OpenAPI \(\#256\) [\#275](https://github.com/resamsel/translatr/pull/275) ([resamsel](https://github.com/resamsel))
- Migrate NotificationResource to contract-first OpenAPI \(\#256\) [\#274](https://github.com/resamsel/translatr/pull/274) ([resamsel](https://github.com/resamsel))
- Migrate AuthClientsResource to contract-first OpenAPI \(\#256\) [\#273](https://github.com/resamsel/translatr/pull/273) ([resamsel](https://github.com/resamsel))
- Migrate StatisticsResource to contract-first OpenAPI \(\#256\) [\#272](https://github.com/resamsel/translatr/pull/272) ([resamsel](https://github.com/resamsel))
- Migrate HealthResource to contract-first OpenAPI \(\#256\) [\#271](https://github.com/resamsel/translatr/pull/271) ([resamsel](https://github.com/resamsel))
- Migrate MemberResource to contract-first OpenAPI \(\#256\) [\#270](https://github.com/resamsel/translatr/pull/270) ([resamsel](https://github.com/resamsel))
- Migrate KeyResource to contract-first OpenAPI \(\#256\) [\#269](https://github.com/resamsel/translatr/pull/269) ([resamsel](https://github.com/resamsel))
- Migrate LocaleResource to contract-first OpenAPI \(\#256\) [\#268](https://github.com/resamsel/translatr/pull/268) ([resamsel](https://github.com/resamsel))
- fix\(message\): restore localeDisplayName on MessageDto read paths [\#267](https://github.com/resamsel/translatr/pull/267) ([resamsel](https://github.com/resamsel))
- Migrate MessageResource to contract-first OpenAPI \(\#256\) [\#266](https://github.com/resamsel/translatr/pull/266) ([resamsel](https://github.com/resamsel))
- feat\(openapi\): migrate ProjectResource to the generated contract \(\#256\) [\#265](https://github.com/resamsel/translatr/pull/265) ([resamsel](https://github.com/resamsel))
- feat\(openapi\): migrate AccessTokenResource to the generated contract \(\#256\) [\#264](https://github.com/resamsel/translatr/pull/264) ([resamsel](https://github.com/resamsel))
- feat\(openapi\): contract-first OpenAPI toolchain + pilot resource migration \(\#256\) [\#263](https://github.com/resamsel/translatr/pull/263) ([resamsel](https://github.com/resamsel))
- chore\(build\): upgrade Gradle wrapper to 9.7.1 [\#262](https://github.com/resamsel/translatr/pull/262) ([resamsel](https://github.com/resamsel))
- Admin Health page + logout Javadoc fix [\#261](https://github.com/resamsel/translatr/pull/261) ([resamsel](https://github.com/resamsel))
- feat\(admin\): Health page for OIDC provider diagnostics + dashboard page rename [\#260](https://github.com/resamsel/translatr/pull/260) ([resamsel](https://github.com/resamsel))
- feat\(auth\): multi-provider OIDC SSO \(\#255\) [\#259](https://github.com/resamsel/translatr/pull/259) ([resamsel](https://github.com/resamsel))
- fix\(ui\): generate build-info.ts before build:ui:prod [\#254](https://github.com/resamsel/translatr/pull/254) ([resamsel](https://github.com/resamsel))
- fix\(quinoa\): keep devDependencies for the npm ci build path [\#253](https://github.com/resamsel/translatr/pull/253) ([resamsel](https://github.com/resamsel))
- fix\(quinoa\): install devDependencies for the SPA build on Heroku [\#252](https://github.com/resamsel/translatr/pull/252) ([resamsel](https://github.com/resamsel))
- fix\(quinoa\): produce a deployable SPA bundle on the Heroku build [\#251](https://github.com/resamsel/translatr/pull/251) ([resamsel](https://github.com/resamsel))
- fix: unblock Heroku deployment \(lockfile sync + Play→Quarkus buildpacks\) [\#249](https://github.com/resamsel/translatr/pull/249) ([resamsel](https://github.com/resamsel))
- fix\(auth\): return to the current route after re-login \(\#245\) [\#248](https://github.com/resamsel/translatr/pull/248) ([resamsel](https://github.com/resamsel))
- feat\(admin\): dock the sidebar by default on large screens \(\#243\) [\#246](https://github.com/resamsel/translatr/pull/246) ([resamsel](https://github.com/resamsel))
- feat\(admin\): move feature-flag tabs into the page header [\#244](https://github.com/resamsel/translatr/pull/244) ([resamsel](https://github.com/resamsel))
- Activate sending analytics to NX by default [\#242](https://github.com/resamsel/translatr/pull/242) ([resamsel](https://github.com/resamsel))
- chore\(admin\): remove orphaned per-user featureFlags NgRx slice \(\#227\) [\#241](https://github.com/resamsel/translatr/pull/241) ([resamsel](https://github.com/resamsel))
- feat: allow feature flags to be enabled globally \(\#227\) [\#238](https://github.com/resamsel/translatr/pull/238) ([resamsel](https://github.com/resamsel))
- fix\(ui\): whiten header tab labels on the darkened strip \(\#233\) [\#237](https://github.com/resamsel/translatr/pull/237) ([resamsel](https://github.com/resamsel))
- fix\(editor\): restore Cmd/Ctrl+Enter save shortcut \(\#229\) [\#236](https://github.com/resamsel/translatr/pull/236) ([resamsel](https://github.com/resamsel))
- chore\(lets-release\): bump simple-git to ^3.36.0 \(fixes critical RCE alert\) [\#235](https://github.com/resamsel/translatr/pull/235) ([resamsel](https://github.com/resamsel))
- chore: fix Dependabot alerts \(safe dependency updates\) [\#234](https://github.com/resamsel/translatr/pull/234) ([resamsel](https://github.com/resamsel))
- fix\(ui\): resolve UI regressions after the Angular/Material 22 upgrade \(\#228\) [\#232](https://github.com/resamsel/translatr/pull/232) ([resamsel](https://github.com/resamsel))
- fix\(ui\): Material 22 \(MDC\) migration regressions \(\#230\) [\#231](https://github.com/resamsel/translatr/pull/231) ([resamsel](https://github.com/resamsel))

## [v3.3.3](https://github.com/resamsel/translatr/tree/v3.3.3) (2022-04-16)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.3.2...v3.3.3)

## [v3.3.2](https://github.com/resamsel/translatr/tree/v3.3.2) (2022-04-16)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.3.1...v3.3.2)

## [v3.3.1](https://github.com/resamsel/translatr/tree/v3.3.1) (2022-04-15)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.3.0...v3.3.1)

## [v3.3.0](https://github.com/resamsel/translatr/tree/v3.3.0) (2022-04-15)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.2.0...v3.3.0)

## [v3.2.0](https://github.com/resamsel/translatr/tree/v3.2.0) (2021-05-31)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.2.0-0...v3.2.0)

**Implemented enhancements:**

- Add route for creating an access token [\#155](https://github.com/resamsel/translatr/issues/155)

**Fixed bugs:**

- Dashboard page scrolls horizontally on mobile [\#163](https://github.com/resamsel/translatr/issues/163)

**Closed issues:**

- Migrate to ESLint from Codelyzer and TSLint [\#224](https://github.com/resamsel/translatr/issues/224)
- Upgrade to Angular 11 [\#223](https://github.com/resamsel/translatr/issues/223)
- Upgrade to Angular 10 [\#222](https://github.com/resamsel/translatr/issues/222)
- Re-write activity graph template style [\#220](https://github.com/resamsel/translatr/issues/220)

## [v3.2.0-0](https://github.com/resamsel/translatr/tree/v3.2.0-0) (2020-12-05)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.6...v3.2.0-0)

## [v3.1.6](https://github.com/resamsel/translatr/tree/v3.1.6) (2020-11-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.6-3...v3.1.6)

**Fixed bugs:**

- Session timeout is much too short [\#221](https://github.com/resamsel/translatr/issues/221)

## [v3.1.6-3](https://github.com/resamsel/translatr/tree/v3.1.6-3) (2020-11-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.6-2...v3.1.6-3)

## [v3.1.6-2](https://github.com/resamsel/translatr/tree/v3.1.6-2) (2020-11-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.6-1...v3.1.6-2)

## [v3.1.6-1](https://github.com/resamsel/translatr/tree/v3.1.6-1) (2020-11-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.5...v3.1.6-1)

## [v3.1.5](https://github.com/resamsel/translatr/tree/v3.1.5) (2020-11-13)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.4...v3.1.5)

**Fixed bugs:**

- Pushing messages does not work [\#219](https://github.com/resamsel/translatr/issues/219)

## [v3.1.4](https://github.com/resamsel/translatr/tree/v3.1.4) (2020-11-10)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.2...v3.1.4)

**Merged pull requests:**

- Add docker push image [\#210](https://github.com/resamsel/translatr/pull/210) ([resamsel](https://github.com/resamsel))
- Feature/run it on travis [\#209](https://github.com/resamsel/translatr/pull/209) ([resamsel](https://github.com/resamsel))
- Add node.js Github workflow [\#208](https://github.com/resamsel/translatr/pull/208) ([resamsel](https://github.com/resamsel))
- Add Scala Github Actions integration [\#207](https://github.com/resamsel/translatr/pull/207) ([resamsel](https://github.com/resamsel))

## [v3.1.2](https://github.com/resamsel/translatr/tree/v3.1.2) (2020-11-09)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.1...v3.1.2)

## [v3.1.1](https://github.com/resamsel/translatr/tree/v3.1.1) (2020-11-09)

[Full Changelog](https://github.com/resamsel/translatr/compare/ls...v3.1.1)

## [ls](https://github.com/resamsel/translatr/tree/ls) (2020-11-09)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.1.0...ls)

## [v3.1.0](https://github.com/resamsel/translatr/tree/v3.1.0) (2020-11-09)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.0.3...v3.1.0)

**Implemented enhancements:**

- Introduce access token from ENV for default user [\#215](https://github.com/resamsel/translatr/issues/215)
- Rewrite load generator with personas [\#213](https://github.com/resamsel/translatr/issues/213)
- Introduce diff in activity so changes can be displayed [\#193](https://github.com/resamsel/translatr/issues/193)
- Introduce keyboard shortcuts [\#144](https://github.com/resamsel/translatr/issues/144)
- Migrate to latest Play version [\#92](https://github.com/resamsel/translatr/issues/92)

**Fixed bugs:**

- Removing user leads to validation exception [\#212](https://github.com/resamsel/translatr/issues/212)

**Closed issues:**

- Clean unnecessary takeUntil\(\) usages [\#156](https://github.com/resamsel/translatr/issues/156)
- Move actor code to separate component [\#152](https://github.com/resamsel/translatr/issues/152)

## [v3.0.3](https://github.com/resamsel/translatr/tree/v3.0.3) (2020-07-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.0.2...v3.0.3)

## [v3.0.2](https://github.com/resamsel/translatr/tree/v3.0.2) (2020-06-29)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.0.1...v3.0.2)

**Fixed bugs:**

- Wrong indentation of CLI integration example file [\#192](https://github.com/resamsel/translatr/issues/192)
- Header image on main page looks odd on large screen [\#191](https://github.com/resamsel/translatr/issues/191)

## [v3.0.1](https://github.com/resamsel/translatr/tree/v3.0.1) (2020-06-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v3.0.0...v3.0.1)

**Fixed bugs:**

- Redirect URI does not respect FORCE\_SSL configuration [\#186](https://github.com/resamsel/translatr/issues/186)
- Default feature values are not returned [\#185](https://github.com/resamsel/translatr/issues/185)

## [v3.0.0](https://github.com/resamsel/translatr/tree/v3.0.0) (2020-06-27)

[Full Changelog](https://github.com/resamsel/translatr/compare/v2.2.0...v3.0.0)

**Implemented enhancements:**

- Improve empty states [\#165](https://github.com/resamsel/translatr/issues/165)
- Language Editor: Add key creation teaser [\#164](https://github.com/resamsel/translatr/issues/164)
- Rewrite main page [\#147](https://github.com/resamsel/translatr/issues/147)
- Introduce feature flags [\#145](https://github.com/resamsel/translatr/issues/145)
- Improve header images [\#142](https://github.com/resamsel/translatr/issues/142)
- Locales: Make downloadable [\#140](https://github.com/resamsel/translatr/issues/140)
- Entry Point: Make /ui the default UI [\#138](https://github.com/resamsel/translatr/issues/138)
- Key Editor: Add language creation teaser [\#137](https://github.com/resamsel/translatr/issues/137)
- Dialogs: Unify action buttons [\#135](https://github.com/resamsel/translatr/issues/135)
- Editor: Copy value of existing translation [\#129](https://github.com/resamsel/translatr/issues/129)
- User Page: Make information editable [\#128](https://github.com/resamsel/translatr/issues/128)
- I18n: Make UI translatable [\#127](https://github.com/resamsel/translatr/issues/127)
- Introduce load indicators [\#126](https://github.com/resamsel/translatr/issues/126)
- Project Card: Restrict length of user name [\#124](https://github.com/resamsel/translatr/issues/124)
- Locales/Keys: Add translation progress bar [\#122](https://github.com/resamsel/translatr/issues/122)
- Project Members: Implement removing [\#121](https://github.com/resamsel/translatr/issues/121)
- Key Editor: Add filter for missing translations [\#120](https://github.com/resamsel/translatr/issues/120)
- Language Editor: Add filter for missing translations [\#119](https://github.com/resamsel/translatr/issues/119)
- Project/Members: Enable actions on list items [\#118](https://github.com/resamsel/translatr/issues/118)
- Project/Keys: Enable actions on list items [\#117](https://github.com/resamsel/translatr/issues/117)
- Project/Locales: Enable actions on list items [\#116](https://github.com/resamsel/translatr/issues/116)
- Project: Create settings page [\#115](https://github.com/resamsel/translatr/issues/115)
- Mark certain users as admins [\#88](https://github.com/resamsel/translatr/issues/88)
- Toggle jumping to next translation after saving/cmd+Enter [\#80](https://github.com/resamsel/translatr/issues/80)
- Add wizard for creating a .translatr.yml config file [\#33](https://github.com/resamsel/translatr/issues/33)

**Fixed bugs:**

- Fix load testing [\#111](https://github.com/resamsel/translatr/issues/111)
- UI language not being sent to backend [\#174](https://github.com/resamsel/translatr/issues/174)
- Editor empty after saving a new message [\#173](https://github.com/resamsel/translatr/issues/173)
- Editing locale only works once [\#169](https://github.com/resamsel/translatr/issues/169)
- Cannot modify my access tokens [\#166](https://github.com/resamsel/translatr/issues/166)
- Initial load fails with URL error [\#159](https://github.com/resamsel/translatr/issues/159)
- Transfer ownership does not display any dialog [\#153](https://github.com/resamsel/translatr/issues/153)
- Activity list forces horizontal scrolling [\#148](https://github.com/resamsel/translatr/issues/148)
- Fix layout of user info page [\#146](https://github.com/resamsel/translatr/issues/146)
- User Page: Looks odd on mobile [\#141](https://github.com/resamsel/translatr/issues/141)
- Access Tokens: Not shown when clicked [\#139](https://github.com/resamsel/translatr/issues/139)
- Project Keys: Cannot use a%2Fb as key name [\#136](https://github.com/resamsel/translatr/issues/136)
- User Projects: Load more is shown even if there are no more projects [\#134](https://github.com/resamsel/translatr/issues/134)
- Email address should only be visible to authorized users [\#132](https://github.com/resamsel/translatr/issues/132)
- User Page: "Start new project" should only be shown on own user page  [\#130](https://github.com/resamsel/translatr/issues/130)
- Error Handler: Forbidden should display an error message for logged-in users [\#114](https://github.com/resamsel/translatr/issues/114)
- Project Info: Latest translations don't show [\#113](https://github.com/resamsel/translatr/issues/113)
- Project: Languages/Keys counts don't match 50 items [\#112](https://github.com/resamsel/translatr/issues/112)
- Messages: timestamps are empty [\#110](https://github.com/resamsel/translatr/issues/110)
- Key Editor: Locale list is empty [\#109](https://github.com/resamsel/translatr/issues/109)
- Key Editor: Switch button between editors is not visible [\#108](https://github.com/resamsel/translatr/issues/108)
- Editor: Fix height issue [\#107](https://github.com/resamsel/translatr/issues/107)
- Access Tokens: Duplicate request sent to server [\#106](https://github.com/resamsel/translatr/issues/106)
- Dashboard: No activity doesn't stop loading [\#105](https://github.com/resamsel/translatr/issues/105)
- Language Editor: Translations should show messages from all languages [\#104](https://github.com/resamsel/translatr/issues/104)
- Dashboard: Doesn't show projects I'm participating in [\#103](https://github.com/resamsel/translatr/issues/103)
- Project Members: Adding as admin does not add [\#102](https://github.com/resamsel/translatr/issues/102)
- Project Members: Error message is shown when adding member twice [\#101](https://github.com/resamsel/translatr/issues/101)
- Project card: Looks odd on mobile [\#100](https://github.com/resamsel/translatr/issues/100)
- User Page: Bad UX for new user [\#99](https://github.com/resamsel/translatr/issues/99)
- User Activity: Shouldn't show activity from private projects [\#98](https://github.com/resamsel/translatr/issues/98)
- Error in CLI: unexpected keyword argument 'pathName' [\#96](https://github.com/resamsel/translatr/issues/96)

**Documentation updates:**

- Update Documentation [\#131](https://github.com/resamsel/translatr/issues/131)

**Closed issues:**

- Release version 3.0.0 [\#172](https://github.com/resamsel/translatr/issues/172)
- Fix e2e testing [\#161](https://github.com/resamsel/translatr/issues/161)
- Fix Java unit tests [\#143](https://github.com/resamsel/translatr/issues/143)
- Introduce UI Unit Tests [\#125](https://github.com/resamsel/translatr/issues/125)
- Error with CLI: YAMLLoadWarning: calling yaml.load\(\) without Loader=... is deprecated [\#97](https://github.com/resamsel/translatr/issues/97)

**Merged pull requests:**

- Release version 3.0.0 [\#179](https://github.com/resamsel/translatr/pull/179) ([resamsel](https://github.com/resamsel))

## [v2.2.0](https://github.com/resamsel/translatr/tree/v2.2.0) (2019-02-27)

[Full Changelog](https://github.com/resamsel/translatr/compare/v2.1.2...v2.2.0)

**Implemented enhancements:**

- Allow downloading messages by owner, project, and locale name [\#90](https://github.com/resamsel/translatr/issues/90)
- New routing based on GitHub routes [\#87](https://github.com/resamsel/translatr/issues/87)

## [v2.1.2](https://github.com/resamsel/translatr/tree/v2.1.2) (2017-06-15)

[Full Changelog](https://github.com/resamsel/translatr/compare/v2.1.1...v2.1.2)

## [v2.1.1](https://github.com/resamsel/translatr/tree/v2.1.1) (2017-06-14)

[Full Changelog](https://github.com/resamsel/translatr/compare/v2.1.0...v2.1.1)

**Implemented enhancements:**

- Show list of contributors [\#86](https://github.com/resamsel/translatr/issues/86)
- Endpoint in example .translatr.yml file points to localhost [\#85](https://github.com/resamsel/translatr/issues/85)

**Fixed bugs:**

- NPE when creating and deleting keys [\#84](https://github.com/resamsel/translatr/issues/84)
- Re-calculating the word count adds activity to all messages [\#83](https://github.com/resamsel/translatr/issues/83)
- Translatr install script fails with 404 [\#82](https://github.com/resamsel/translatr/issues/82)

## [v2.1.0](https://github.com/resamsel/translatr/tree/v2.1.0) (2017-06-06)

[Full Changelog](https://github.com/resamsel/translatr/compare/v2.0.0...v2.1.0)

**Implemented enhancements:**

- Decrease amount of queries [\#52](https://github.com/resamsel/translatr/issues/52)
- Word count per locale [\#76](https://github.com/resamsel/translatr/issues/76)
- Migrate editor to use a JS framework [\#71](https://github.com/resamsel/translatr/issues/71)
- Add user listing in Translatr CLI [\#69](https://github.com/resamsel/translatr/issues/69)
- Allow key creation in CLI [\#65](https://github.com/resamsel/translatr/issues/65)
- Show type of membership in project lists [\#64](https://github.com/resamsel/translatr/issues/64)
- Make preview more visible in editor [\#62](https://github.com/resamsel/translatr/issues/62)
- When editing keys of a language add links to switch to other language [\#57](https://github.com/resamsel/translatr/issues/57)
- Add validation to API [\#54](https://github.com/resamsel/translatr/issues/54)
- Add Swagger specification of the Translatr API [\#53](https://github.com/resamsel/translatr/issues/53)
- Remove duplicate API methods in web UI [\#48](https://github.com/resamsel/translatr/issues/48)
- Allow using the API without access\_token [\#46](https://github.com/resamsel/translatr/issues/46)
- Add Keycloak as auth provider [\#45](https://github.com/resamsel/translatr/issues/45)
- Allow environment vars in .translatr.yml [\#37](https://github.com/resamsel/translatr/issues/37)

**Fixed bugs:**

- OAuth: long email address leads to database error [\#77](https://github.com/resamsel/translatr/issues/77)
- Editor: Key with empty name is not editable [\#73](https://github.com/resamsel/translatr/issues/73)
- Editor discards changes on item list scroll to load [\#72](https://github.com/resamsel/translatr/issues/72)
- Removing keys and locales results in losing search term [\#68](https://github.com/resamsel/translatr/issues/68)
- Owner can remove themselves from members [\#63](https://github.com/resamsel/translatr/issues/63)
- Notifications overflow container [\#60](https://github.com/resamsel/translatr/issues/60)
- Activity CSV throws an exception [\#59](https://github.com/resamsel/translatr/issues/59)
- Creating locales does not work with CLI [\#58](https://github.com/resamsel/translatr/issues/58)
- Fix alignment of input labels in Safari [\#51](https://github.com/resamsel/translatr/issues/51)
- Merging users fails with duplicate key exception [\#47](https://github.com/resamsel/translatr/issues/47)
- Any logged-in user can view any project [\#44](https://github.com/resamsel/translatr/issues/44)
- Any logged-in user can view any access token [\#43](https://github.com/resamsel/translatr/issues/43)
- Any logged-in user can delete a project [\#42](https://github.com/resamsel/translatr/issues/42)
- Anyone can modify locales of a project [\#41](https://github.com/resamsel/translatr/issues/41)
- Anyone can add users to a project [\#40](https://github.com/resamsel/translatr/issues/40)
- Fix navigation for small screens [\#39](https://github.com/resamsel/translatr/issues/39)
- Fix header on small screens [\#38](https://github.com/resamsel/translatr/issues/38)

**Closed issues:**

- Script tags might destroy the page in preview [\#66](https://github.com/resamsel/translatr/issues/66)
- Add unit tests [\#29](https://github.com/resamsel/translatr/issues/29)

## [v2.0.0](https://github.com/resamsel/translatr/tree/v2.0.0) (2016-10-30)

[Full Changelog](https://github.com/resamsel/translatr/compare/v1.1.0...v2.0.0)

**Implemented enhancements:**

- Add notifications [\#36](https://github.com/resamsel/translatr/issues/36)
- Require API key for CLI [\#32](https://github.com/resamsel/translatr/issues/32)
- Login with Github [\#8](https://github.com/resamsel/translatr/issues/8)
- Add user roles [\#7](https://github.com/resamsel/translatr/issues/7)
- Introduce users [\#5](https://github.com/resamsel/translatr/issues/5)
- Make tables sortable [\#4](https://github.com/resamsel/translatr/issues/4)

## [v1.1.0](https://github.com/resamsel/translatr/tree/v1.1.0) (2016-10-07)

[Full Changelog](https://github.com/resamsel/translatr/compare/v1.0.2...v1.1.0)

**Implemented enhancements:**

- Enable exception handling in translatr.py [\#27](https://github.com/resamsel/translatr/issues/27)
- Add support for PO files [\#6](https://github.com/resamsel/translatr/issues/6)

**Fixed bugs:**

- Add install instructions for translatr.py [\#19](https://github.com/resamsel/translatr/issues/19)

## [v1.0.2](https://github.com/resamsel/translatr/tree/v1.0.2) (2016-09-28)

[Full Changelog](https://github.com/resamsel/translatr/compare/v1.0.1...v1.0.2)

**Implemented enhancements:**

- Combine CSS and JS files [\#28](https://github.com/resamsel/translatr/issues/28)

**Fixed bugs:**

- Changing a key is not intuitive [\#26](https://github.com/resamsel/translatr/issues/26)
- "Add a new key" button not working [\#25](https://github.com/resamsel/translatr/issues/25)
- Exception when creating a locale with a long name [\#24](https://github.com/resamsel/translatr/issues/24)

## [v1.0.1](https://github.com/resamsel/translatr/tree/v1.0.1) (2016-09-26)

[Full Changelog](https://github.com/resamsel/translatr/compare/v1.0.0...v1.0.1)

**Implemented enhancements:**

- Make search more predictable [\#22](https://github.com/resamsel/translatr/issues/22)
- Make dashboard button a FAB on frontpage [\#21](https://github.com/resamsel/translatr/issues/21)

**Fixed bugs:**

- Returning to front page is not possible in small viewport [\#23](https://github.com/resamsel/translatr/issues/23)
- Show raw content of message in editor sidebar [\#20](https://github.com/resamsel/translatr/issues/20)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*

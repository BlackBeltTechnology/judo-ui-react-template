# judo-ui-react-template — module agent doctrine

## Module purpose

`judo-ui-react-template` is the estate's *frontend factory*: it turns a JUDO UI
model instance (`judo-meta-ui` — `Application`, its `PageDefinition`s,
`PageContainer`s, `Table`/`Link`/`Widget` elements, `Action`s, actor/role and
menu structure, plus the data classes reachable from them) into a complete,
buildable React 19 + TypeScript + Vite single-page application, one application
per *actor* declared in the model. It does not render anything itself and it
contains no runtime React library — it ships a Handlebars template set
(~236 template entries under `judo-ui-react/src/main/resources/actor/`,
registered in `judo-ui-react/src/main/resources/ui-react.yaml`) plus the
`@TemplateHelper`-annotated Java classes those templates call through SpEL, and
hands both to the shared `judo-generator-commons` engine.

What comes out of the generator, per actor: routed pages and dialogs, containers
and widget fragments bound to model attributes, MUI DataGrid tables with
per-column filtering and pagination, action buttons wired to the model's
operations and relation navigations, form validation and masks, Keycloak-backed
auth wiring, i18n JSON bundles plus the translation-sync tooling, MUI theming,
Pandino service registrations that give the generated app named interface keys
for customization, and the project scaffolding (`package.json`, Vite and
TypeScript config, Biome config, `.nvmrc`) that makes the output installable and
buildable without hand editing.

The generated UI does not talk HTTP itself — the REST/service/Axios layer it
imports is produced in an earlier phase by `judo-ui-typescript-rest-template`,
whose `judo-ui-typescript-rest-commons` helpers this module depends on directly.
Consumers (project templates such as `judo-jsl-fullstack-karaf-project-template`)
run both phases in order and pass template parameters — `muiLicensePlan`,
`tablePageLimit`, `defaultLanguage` — from their own POMs.

**Repository:** BlackBeltTechnology/judo-ui-react-template ·
**Artifact:** `hu.blackbelt.judo.generator:judo-ui-react-template`
(packaging `pom`, version `${revision}` = `1.0.0-SNAPSHOT`) ·
**License:** EPL-2.0 · **Java:** 21 (21.0.7-zulu via sdkman) ·
**Build:** Maven 3.9.4, mvnd-capable parallel builds.

## Reactor map

The root `pom.xml` declares four modules; build order is the declared order, and
`judo-ui-react` is the artifact every consumer outside this repo actually uses.

<modules>
  <module>docs</module>
  <module>judo-diff-checker-maven-plugin</module>
  <module>judo-ui-react</module>
  <module>judo-ui-react-itest</module>
</modules>

| Module | Artifact / packaging | What it contributes |
|---|---|---|
| `docs` | `judo-ui-react-template-docs`, `jar` | AsciiDoc source describing the generated application's architecture, packaged so the documentation ships alongside the generator rather than living only in the repo. |
| `judo-diff-checker-maven-plugin` | `judo-diff-checker-maven-plugin`, `maven-plugin` | Snapshot regression gate. Compares freshly generated output against committed snapshots with java-diff-utils and fails the build on any unexpected diff — the mechanism that makes template edits reviewable line by line instead of trusting that "it still compiles". Used by the itest modules here and reusable by other generator repos. |
| `judo-ui-react` | `judo-ui-react`, `bundle` (Felix OSGi) | The generator proper: the Handlebars template tree, the `ui-react.yaml` registry that binds each template to an output path via SpEL path/factory expressions, and the `@TemplateHelper` classes under `hu.blackbelt.judo.ui.generator.react` that the templates invoke — page routing and navigation (`UiPageHelper`), action/operation logic (`UiActionsHelper`), widgets (`UiWidgetHelper`), container layout (`UiPageContainerHelper`), tables (`UiTableHelper`), menus (`UIMenuHelper`), i18n keys (`UiI18NHelper`), Pandino DI registration (`UiPandinoHelper`), TypeScript import bookkeeping (`UiImportHelper`), naming/path derivation (`UiGeneralHelper`), npm/pnpm packaging (`UiNPMHelper`), auth (`UiSecurityHelper`), stored-variable state (`ReactStoredVariableHelper`), plus the input-mask package under `.../react/mask`. |
| `judo-ui-react-itest` | `judo-ui-react-itest`, `pom` | Aggregator for the executable proof that the templates work. Each child owns a `.model` file and generates a full React app from it, then formats with Biome, runs the diff checker against committed snapshots, and builds with Vite. Children: `ActionGroupTest` (action groups, community MUI), `ActionGroupTestPro` (action groups, pro MUI license), `CRUDActionsTest` (create/read/update/delete flows), `OperationParametersTest` (operation input parameters), `RelationTest` (relation navigation, inline-edit and tag-container transfers), `SimpleOrderManagement` (an end-to-end order-management scenario). Each child in turn has one sub-module per actor — e.g. `ActionGroupTest/action_group_test__god` — because one actor means one generated application. |

## Build commands

```bash
# Full build (compile generator + run all integration tests)
mvn clean install

# Run unit tests only
mvn clean test

# Build a single integration test module
mvn clean install -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am

# Parallel build using mvnd (requires sdkman)
./full-build-parallel.sh

# Skip Node.js setup if already installed
mvn clean install -DskipPrepareNodeJS

# Set up Java/Maven/mvnd versions
sdk env
```

### Maven profiles

| Profile | Purpose |
|---|---|
| `sign-artifacts` | Sign artifacts using `sign-maven-plugin` for release. |
| `release-dummy` | Deploy to local filesystem (`/tmp/`) for testing. |
| `release-judong` | Deploy to the JUDO NG Nexus repository. |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH. |
| `generate-github-asciidoc-diagrams` | Generate PNG diagrams from AsciiDoc PlantUML blocks. |
| `update-source-code-license` | Update EPL-2.0 license headers in source files. |

## Technology stack

**Generator side**

- **Eclipse EMF** (`ecore-xmi` 2.2.3) — loads and traverses the XMI UI model
- **`judo-meta-ui`** 1.1.0-SNAPSHOT — the UI metamodel this generator reads
- **`judo-generator-commons`** — template engine, `@TemplateHelper` discovery, output writing
- **`judo-ui-typescript-rest-commons`** — shared helpers with the REST-layer generator (phase 1)
- **Handlebars** — template engine producing TypeScript/React sources
- **Spring Expression Language (SpEL)** 5.0.0 — path and factory expressions in `ui-react.yaml`
- **Lombok** 1.18.34 · **Apache Felix `maven-bundle-plugin`** — OSGi bundle packaging

**Generated application side**

- React 19 + TypeScript + Vite 7
- MUI (Material UI) 7.x + DataGrid Pro 8.x
- Pandino — runtime extensibility via OSGi-style dependency injection
- Biome — formatting and linting (not ESLint/Prettier)
- i18n via JSON files (`public/i18n/application_*.json`, `system_*.json`)

**Build & quality**

- Maven 3.9.4 with `flatten-maven-plugin` for CI-friendly `${revision}` versions
- JaCoCo 0.8.12 · SonarQube via `sonar-maven-plugin` · JUnit 5
- `frontend-maven-plugin` 1.12.1 auto-installs Node.js 22.14.0 / pnpm 9.15.9

## Architecture pointers

- `judo-ui-react/src/main/resources/ui-react.yaml` — the template registry. Every generated file exists because a row here maps a `.hbs` template to an output path; `copy: true` rows are emitted verbatim. Adding an output file starts here, not in the templates.
- `judo-ui-react/src/main/resources/actor/` — the template tree; its directory structure mirrors the generated React app one-to-one. `*.fragment.hbs` files are partials pulled in via `{{> fragment.hbs}}`.
- `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/` — the helper classes; templates call them from `ui-react.yaml` in SpEL form, e.g. `#getPagesForRouting(#application)`.
- Generated code exposes Pandino interface keys (e.g. `ROUTE_GOD_GALAXIES_TABLE_INTERFACE_KEY`) as the supported runtime customization seam.
- The pipeline runs in two phases: phase 1 generates the TypeScript REST layer (types, services, Axios) via `judo-ui-typescript-rest-template`; phase 2 generates the React UI from this repo.
- Build environment is pinned by `.sdkmanrc` (Java 21.0.7-zulu, Maven 3.9.4, mvnd 1.0-m6-m40); test logging is configured by the repo-root `logback-test.xml`; `full-build-parallel.sh` drives the mvnd parallel build.
- [README.md](README.md) — project overview and usage example.
- [CONTRIBUTING.md](CONTRIBUTING.md) — development setup and submission guidelines.
- [.github/CIFLOW.md](.github/CIFLOW.md) — branch strategy and CI/CD workflow.
- [judo-diff-checker-maven-plugin/README.md](judo-diff-checker-maven-plugin/README.md) — snapshot diff checker usage.
- [docs/pages/](docs/pages/) — detailed generated-application documentation.

## Development environment

**Required:** Java 21 JDK (`sdk env` reads `.sdkmanrc`) · Maven 3.9.4+ ·
Node.js 22.14.0 / pnpm 9.15.9 (auto-installed during the build, or bypass with
`-DskipPrepareNodeJS`). **Optional:** mvnd for parallel builds. The `.vscode`
directory is git-ignored — IDE settings stay local to each developer.

## Git workflow

- **Main branch:** `develop`
- **Versioning:** CI-friendly `${revision}` (`1.0.0-SNAPSHOT` in development)
- **Branch naming:** `feature/JNG-xxx_description`, `bugfix/JNG-xxx_description`, `release/x.y.z`
- **Rule:** every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions workflows for build, release, merge-pr handling, changelog generation

## Scope guard — invariants an edit must not break

1. **A template change is a snapshot change.** After editing templates, refresh the affected snapshots by copying from `target/frontend-react/` to the itest module's `src/test/resources/snapshots/frontend-react/`, and read the resulting diff — that diff *is* the review.
2. **Never speculate about code you have not opened.** If a file is referenced, read it before answering; ground every claim about templates or helpers in the file.
3. **Keep changes minimal and DRY.** Prefer the smallest edit that works; when the same pattern appears in several templates or helpers, extract it into a fragment or a shared helper instead of copying it.
4. **Implement test-first.** Write or update the test/snapshot expectation that defines the behaviour, watch it fail, then make it pass.
5. **Check in before a major change.** Explain the plan and get it verified before large or structural edits, and give a high-level summary of what changed after each step.

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.

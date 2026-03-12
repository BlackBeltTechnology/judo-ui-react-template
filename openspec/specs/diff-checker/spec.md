# Diff Checker Specification

## Purpose

The `judo-diff-checker-maven-plugin` compares freshly generated files against committed snapshot files and fails the Maven build when unexpected differences are detected, providing snapshot-based regression testing for the code generator.

## Architecture

The plugin consists of a single Mojo class:

- **`DiffCheckerMojo`** (`hu.blackbelt.judo.diffchecker.DiffCheckerMojo`): A Maven Mojo bound to the `VERIFY` phase by default (typically overridden to `generate-sources` in practice). It iterates over a configured list of source file paths, resolves each against a `sourceDirectory` (generated output) and `snapshotDirectory` (committed snapshots), and uses `java-diff-utils` (`DiffUtils`, `UnifiedDiffUtils`) to compute and display unified diffs.

Configuration parameters:
- `sources` (List<String>): Relative file paths to compare
- `sourceDirectory` (String, required): Path to generated files
- `snapshotDirectory` (String, required): Path to snapshot files
- `createSnapshotIfNotExists` (Boolean, default: `true`): Auto-create missing snapshots
- `forceSnapshotOverwrite` (Boolean, default: `false`): Overwrite snapshots before comparing
- `snapshotPostfix` (String, default: `".snapshot"`): Suffix appended to snapshot file names

## Requirements

### Requirement: Plugin SHALL fail when generated files differ from snapshots

The `checkDiffs` goal SHALL throw a `MojoExecutionException` when any configured source file differs from its corresponding snapshot file.

#### Scenario: Matching files
- **GIVEN** a source file `src/pages/Foo/index.tsx` in `sourceDirectory`
- **AND** an identical snapshot file `src/pages/Foo/index.tsx.snapshot` in `snapshotDirectory`
- **WHEN** the `checkDiffs` goal executes
- **THEN** the build continues without error

#### Scenario: Differing files
- **GIVEN** a source file that differs from its snapshot
- **WHEN** the `checkDiffs` goal executes
- **THEN** a unified diff (with 3 lines of context) is printed to stdout
- **AND** a `MojoExecutionException` is thrown with message "Snapshot changes detected in ..."

### Requirement: Plugin SHALL fail when source files are missing

#### Scenario: Missing source file
- **GIVEN** a configured source path that does not exist in `sourceDirectory`
- **WHEN** the `checkDiffs` goal executes
- **THEN** a `MojoExecutionException` is thrown with message "Source file does not exist: ..."

### Requirement: Plugin SHALL auto-create snapshot files when configured

#### Scenario: Missing snapshot with createSnapshotIfNotExists=true
- **GIVEN** a source file exists but its snapshot file does not
- **AND** `createSnapshotIfNotExists` is `true` (default)
- **WHEN** the `checkDiffs` goal executes
- **THEN** the snapshot file is created by copying the source file (including parent directories)
- **AND** the build continues without error

#### Scenario: Missing snapshot with createSnapshotIfNotExists=false
- **GIVEN** a source file exists but its snapshot file does not
- **AND** `createSnapshotIfNotExists` is `false`
- **WHEN** the `checkDiffs` goal executes
- **THEN** a `MojoExecutionException` is thrown with message "Snapshot file does not exist: ..."

### Requirement: Plugin SHALL support forced snapshot overwrite

#### Scenario: Force overwrite enabled
- **GIVEN** `forceSnapshotOverwrite` is `true`
- **AND** both source and snapshot files exist
- **WHEN** the `checkDiffs` goal executes
- **THEN** the snapshot file is overwritten with the source file content before comparison
- **AND** the comparison always passes (since files are now identical)

### Requirement: Plugin SHALL require at least one source to be configured

#### Scenario: No sources configured
- **GIVEN** the `sources` list is null or empty
- **WHEN** the `checkDiffs` goal executes
- **THEN** a `MojoExecutionException` is thrown with message "No sources configured for snapshot checking."

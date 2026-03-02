# Development Version and Branch Handling

## Branches

The versioning policy follows [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

| Branch Pattern | Purpose |
|----------------|---------|
| `develop` | Main development branch — contains latest sources of the active version |
| `feature/JNG-NUMBER_short_summary` | Feature branches based on `develop` |
| `(release/)X_Y_betaN` | Release branches (the `release/` prefix is reserved for CI) |
| `bugfix/JNG-NUMBER_short_summary` | Bugfixes based on release branches — must be applied to newer versions too |
| `support/JNG-NUMBER_short_summary` | Support branches based on release branches |
| `master` | Latest released sources of the active version |

```mermaid
gitGraph
    commit id: "init"
    branch develop
    checkout develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1"
    commit id: "feat-2"
    checkout develop
    merge feature/JNG-1 id: "merge-feat-1"
    branch feature/JNG-2
    commit id: "feat-3"
    checkout develop
    merge feature/JNG-2 id: "merge-feat-2"
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-1"
    checkout release/1.0-beta1
    merge bugfix/JNG-4 id: "merge-fix"
    checkout develop
    merge release/1.0-beta1 id: "merge-release"
    checkout master
    merge release/1.0-beta1 id: "v1.0"
```

## Version Numbers

Version numbers use semantic versioning with these rules:

| Event | Version Change |
|-------|---------------|
| Start a `feature/` branch | No change |
| Start a `release/` branch from `develop` | 2nd number on `develop` is incremented |
| `bugfix/` branch on a release | No change (applied before merging to master) |
| `support/` branch | 3rd number incremented — for minor changes to a previous release |
| `hotfix/` branch | 4th number incremented — applied to both release and master |

## GitHub Actions Workflows

The CI/CD pipeline consists of several interconnected GitHub Actions workflows that automate building, testing, versioning, and releasing.

### build.yml — Main Build Pipeline

```mermaid
flowchart TD
    TRIGGER["Push on develop<br/>OR PR on develop/master/increment/release"]
    TRIGGER --> CHECK{Branch type?}
    CHECK -->|"master, release/*"| VERSION_RELEASE["Version from pom.xml<br/>(without -SNAPSHOT)"]
    CHECK -->|"develop, increment/*"| VERSION_DEV["Version: major.minor.qualifier<br/>.date_commitId_branchName"]
    VERSION_RELEASE --> BUILD["Build & deploy to Nexus"]
    VERSION_DEV --> BUILD
    BUILD --> TAG["Create git tag v&lt;version&gt;"]
    TAG --> IS_INCREMENT{increment/*, release/*?}
    IS_INCREMENT -->|Yes| MERGE_TAG["Create tag merge-pr/&lt;version&gt;"]
    MERGE_TAG --> TRIGGER_MERGE["Trigger merge-pr-tagged.yml"]
    IS_INCREMENT -->|No| IS_DEVELOP{develop?}
    IS_DEVELOP -->|Yes| CHANGELOG["Build changelog"]
    CHANGELOG --> GH_RELEASE["Create GitHub prerelease"]
```

### merge-pr-tagged.yml — Auto-merge Handler

```mermaid
flowchart TD
    TRIGGER["Push on merge-pr/* tag"]
    TRIGGER --> GET_VER["Get version from tag"]
    GET_VER --> CHECK{Version format?}
    CHECK -->|"major.minor.qualifier"| MERGE_MASTER["Merge PR to master"]
    MERGE_MASTER --> TRIGGER_RELEASE["Trigger create-release-on-master.yml"]
    CHECK -->|"Other format"| SQUASH_DEV["Squash PR to develop"]
    SQUASH_DEV --> TRIGGER_BUILD["Trigger build.yml"]
    MERGE_MASTER --> CLEANUP["Delete merge-pr/* tag"]
    SQUASH_DEV --> CLEANUP
```

### release.yml — Manual Release

```mermaid
flowchart TD
    TRIGGER["Manual trigger<br/>with version ('auto' or major.minor.qualifier)"]
    TRIGGER --> CHECK{Given version?}
    CHECK -->|"'auto'"| AUTO["Release version from pom.xml<br/>(without -SNAPSHOT)"]
    CHECK -->|"Specific"| SPECIFIC["Use given version"]
    AUTO --> NEXT["Next version = qualifier + 1"]
    SPECIFIC --> NEXT
    NEXT --> PR_MASTER["Create PR on master<br/>with release version"]
    NEXT --> PR_DEVELOP["Create PR on develop<br/>with next version"]
    PR_MASTER --> BUILD_1["Trigger build.yml"]
    PR_DEVELOP --> BUILD_2["Trigger build.yml"]
```

### create-release-on-master.yml

Triggered by a push on `master` — builds a changelog and creates a GitHub release (marked as latest).

## Development Rules

> **Important:** There is no commit without a ticket number. Every pull request and commit must include a `JNG-xxx` JIRA reference.

Issue tracking uses [JIRA](https://blackbelt.atlassian.net/jira/dashboards).

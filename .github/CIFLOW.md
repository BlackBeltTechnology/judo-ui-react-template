# Development version and branch handling

## Table of Contents
- [Branches](#branches)
- [Version numbers](#version-numbers)
- [GitHub action flows](#github-action-flows)
- [How to develop](#how-to-develop)

## Branches

Versioning policy of JUDO NG modules are based on [GitFlow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

Branches:

* **develop**: development branch contains latest development sources of the last active version
* **feature/JNG-NUMBER_short_summary**: feature branches are based on **develop** and contains sources of new features that will be included in last active version
* **(release/)1_0_beta1**: release branches of 1.0-beta1 (release/ prefix is still reserved for CI)
* **bugfix/JNG-NUMBER_short_summary**, **support/JNG-NUMBER_short_summary**: bugfix and support branches are based on release branches and must be applied to release and development branches of newer versions too
* **master**: contains latest released sources of the last active version

```mermaid
%%{init: {'theme': 'base', 'gitGraph': {'rotateCommitLabel': false}}}%%
gitGraph
    commit id: "init"
    branch develop order: 1
    commit id: "d1"
    
    branch feature/JNG-2 order: 2
    commit id: "f2-1"
    commit id: "f2-2"
    checkout develop
    merge feature/JNG-2 id: "merge-f2"
    
    branch feature/JNG-1 order: 3
    commit id: "f1-1"
    commit id: "f1-2"
    checkout develop
    merge feature/JNG-1 id: "merge-f1"
    
    branch feature/JNG-3 order: 4
    commit id: "f3-1"
    checkout develop
    merge feature/JNG-3 id: "merge-f3"
    
    branch release/1.0-beta1 order: 5
    commit id: "r1-1"
    
    branch bugfix/JNG-4 order: 6
    commit id: "bf-1"
    checkout release/1.0-beta1
    merge bugfix/JNG-4 id: "merge-bf"
    
    checkout develop
    merge release/1.0-beta1 id: "merge-r1"
    
    branch release/1.0-beta2 order: 7
    commit id: "r2-1"
    
    branch support/JNG-5 order: 8
    commit id: "sp-1"
    checkout release/1.0-beta2
    merge support/JNG-5 id: "merge-sp"
    
    checkout master
    merge release/1.0-beta2 id: "release-1.0"
    
    branch hotfix/JNG-6 order: 9
    commit id: "hf-1"
    checkout master
    merge hotfix/JNG-6 id: "merge-hf"
    
    checkout develop
    merge master id: "sync-hf"
    
    branch release/1.1-beta1 order: 10
    commit id: "r3-1"
    checkout master
    merge release/1.1-beta1 id: "release-1.1"
```

**Branch Legend:**

| Branch Type | Color      | Purpose                        |
|-------------|------------|--------------------------------|
| master      | Green      | Latest released sources        |
| develop     | Blue       | Latest development sources     |
| feature/*   | Gold       | New features                   |
| release/*   | Cyan       | Release preparation            |
| bugfix/*    | Red        | Bug fixes on release branches  |
| support/*   | Aquamarine | Support for previous releases  |
| hotfix/*    | Dark Red   | Urgent fixes for production    |

## Version numbers

Version numbers are increased using semantic versioning:

* do not change version numbers on starting feature/ branches
* 2nd number in version of **develop** branch is increased when a release branch started
* do not change version numbers on bugfix/ branches - that are applied on release branches during testing before releasing it (merging to master)
* 3rd number in version of support/ branches is increased when started - it is used to support a previous release including new (minor) changes; support/ branches are merged back to release branch when update is released (without merging changes to master)
* 4th number in version of hotfix/ branches is increased when started (that are applied on both release and master branches)

## GitHub action flows

### build.yml

```mermaid
flowchart TD
    A[/"<b>when</b><br>push on <b>develop</b> branch<br>or<br>pull request on <b>develop</b>, <b>master</b>,<br><b>increment/*</b>, <b>release/*</b> branch"/]
    
    A --> B{Commit or Pull request's<br>base branch?}
    
    B -->|master, release/*| C["set <b>version</b><br>from project pom.xml<br>(version without '-SNAPSHOT')"]
    B -->|develop, increment/*| D["set version<br><b>major.minor.qualifier.date_commitId_branchName</b><br>from project pom.xml<br>(version without '-SNAPSHOT')"]
    
    C --> E[build and deploy to nexus]
    D --> E
    
    E --> F["create git tag <b>v&lt;version&gt;</b>"]
    
    F --> G{Pull request or commit<br>base branch?}
    
    G -->|increment/*, release/*| H["create tag <b>merge-pr/&lt;PR number&gt;</b>"]
    H --> I[/"<b>trigger merge-pr-tagged.yml</b>"/]
    
    G -->|develop| J[build change log]
    J --> K["create <b>github release</b><br>(prerelease) with change log"]
    
    G -->|other| L[End]
    I --> L
    K --> L

    style A fill:#ffffcc
    style I fill:#90EE90
```

### merge-pr-tagged.yml

```mermaid
flowchart TD
    A[/"<b>when</b><br>push on <b>merge-pr/*</b> tag"/]
    
    A --> B["get PR number from tag name"]
    
    B --> C["get version from base ref"]
    
    C --> D{"check version format"}
    
    D -->|"v(major.minor.qualifier)"| E["merge pull request to <b>master</b>"]
    E --> F[/"<b>trigger create-release-on-master.yml</b><br>(implicit via push to master)"/]
    
    D -->|other| G["squash pull request to <b>develop</b>"]
    G --> H[/"<b>trigger build.yml</b><br>(implicit via push to develop)"/]
    
    F --> I["delete tag <b>merge-pr/&lt;PR number&gt;</b>"]
    H --> I
    
    I --> J[End]

    style A fill:#ffffcc
    style F fill:#90EE90
    style H fill:#90EE90
```

### create-release-on-master.yml

```mermaid
flowchart TD
    A[/"<b>when</b><br>push on <b>master</b> branch"/]
    
    A --> B["get <b>version</b> from project pom.xml<br>(version without '-SNAPSHOT')"]
    
    B --> C[build change log]
    
    C --> D["create <b>github release</b><br>(non-prerelease) with change log"]
    
    D --> E[End]

    style A fill:#ffffcc
```

### release.yml

```mermaid
flowchart TD
    A[/"<b>when</b><br>manually triggered with <b>next_version</b> input<br>which is <b>'Auto'</b> (default) or any other version"/]
    
    A --> B{"next_version is"}
    
    B -->|'Auto'| C["set <b>release version</b><br>from project pom.xml<br>(version without '-SNAPSHOT')"]
    B -->|other| D["set <b>release version</b><br>to given <b>next_version</b>"]
    
    C --> E["set <b>next version</b> to<br><b>release version</b>'s incremental + 1"]
    D --> E
    
    E --> F["create PR on <b>master</b><br>via <b>release/v&lt;version&gt;</b> branch"]
    F --> G[/"<b>trigger build.yml</b><br>(on PR creation)"/]
    
    E --> H["create PR on <b>develop</b><br>via <b>increment/v&lt;version&gt;</b> branch"]
    H --> I[/"<b>trigger build.yml</b><br>(on PR creation)"/]
    
    G --> J[End]
    I --> J

    style A fill:#ffffcc
    style G fill:#90EE90
    style I fill:#90EE90
```

## How to develop

For issue tracking we are using [JIRA](https://blackbelt.atlassian.net/jira/dashboards). Golden rule:

> **IMPORTANT: There is no commit without ticket number**

So for pull request or commit `JNG-xxx` have to be presented in the commit.

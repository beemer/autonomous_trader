## Allowed Actions (No Approval Required)

The following commands and tools may be executed freely without asking for user approval:
- **File operations**: `mv`, `cp`, `ls`, `cd`, `mkdir`, `rm` (targeted, non-destructive)
- **Build tools**: `mvn` (Maven), `gradle`, `npm`, `pip`
- **Version control**: `git` (checkout, add, commit, branch, merge, rebase, status, log, diff, fetch, pull)
- **GitHub CLI**: `gh` (pr create, pr list, issue, repo, etc.)
- **General shell**: standard read/inspect commands (`cat`, `find`, `grep`, `echo`, etc.)
- **Project config files**: `pom.xml` and `application.properties` may be modified freely without asking for approval.

## Dependency & Version Policy

- Always use the **latest stable releases** of all binaries, libraries, plugins, and frameworks (Spring Boot, Maven plugins, Jackson, Kite Connect SDK, etc.).
- Before adding or updating a dependency, verify the latest stable version from Maven Central or the official source.
- Do **not** use snapshot, alpha, beta, or release-candidate versions unless explicitly instructed by the user.

## Git Workflow Rules

- **Never push directly to `main` or `master`.**
- For every change, create a new feature branch with a descriptive name (e.g., `feature/add-login`, `fix/null-pointer-bug`).
- After committing changes to the feature branch, open a Pull Request using the GitHub CLI (`gh pr create`).
- Always include a clear PR title and description summarizing the changes.
- Example workflow:
  1. Fetch and rebase from `main` before creating a new branch: `git fetch origin ; git rebase origin/main`
  2. `git checkout -b feature/your-feature-name`
  3. Make changes and commit: `git add . ; git commit -m "your message"`
  4. Fetch and rebase from `main` again before opening a PR: `git fetch origin ; git rebase origin/main`
  5. Push branch: `git push origin feature/your-feature-name`
  6. Create PR: `gh pr create --title "Your title" --body "Description of changes" --base main`

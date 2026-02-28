## Allowed Actions (No Approval Required)

The following commands and tools may be executed freely without asking for user approval:
- **File operations**: `mv`, `cp`, `ls`, `cd`, `mkdir`, `rm` (targeted, non-destructive)
- **Build tools**: `mvn` (Maven), `gradle`, `npm`, `pip`
- **Version control**: `git` (checkout, add, commit, branch, merge, rebase, status, log, diff, fetch, pull)
- **GitHub CLI**: `gh` (pr create, pr list, issue, repo, etc.)
- **General shell**: standard read/inspect commands (`cat`, `find`, `grep`, `echo`, etc.)

## Git Workflow Rules

- **Never push directly to `main` or `master`.**
- For every change, create a new feature branch with a descriptive name (e.g., `feature/add-login`, `fix/null-pointer-bug`).
- After committing changes to the feature branch, open a Pull Request using the GitHub CLI (`gh pr create`).
- Always include a clear PR title and description summarizing the changes.
- Example workflow:
  1. `git checkout -b feature/your-feature-name`
  2. Make changes and commit: `git add . ; git commit -m "your message"`
  3. Push branch: `git push origin feature/your-feature-name`
  4. Create PR: `gh pr create --title "Your title" --body "Description of changes" --base main`

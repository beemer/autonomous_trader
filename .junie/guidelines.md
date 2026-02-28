## Allowed Actions (No Approval Required)

The following commands and tools may be executed freely without asking for user approval:
- **File operations**: `mv`, `cp`, `ls`, `cd`, `mkdir`, `rm` (targeted, non-destructive)
- **Build tools**: `mvn` (Maven), `gradle`, `npm`, `pip`
- **Version control**: `git` (checkout, add, commit, branch, merge, rebase, status, log, diff, fetch, pull)
- **GitHub CLI**: `gh` (pr create, pr list, issue, repo, etc.)
- **General shell**: standard read/inspect commands (`cat`, `find`, `grep`, `echo`, etc.)
- **Windows PowerShell commands** (all sensible read/write/inspect operations, no approval needed):
  - File & directory management: `New-Item`, `Remove-Item`, `Copy-Item`, `Move-Item`, `Rename-Item`, `Get-Item`, `Get-ChildItem`, `Set-Location`, `Get-Location`, `Test-Path`, `Resolve-Path`
  - Content operations: `Get-Content`, `Set-Content`, `Add-Content`, `Clear-Content`, `Out-File`, `Select-String`
  - Object & pipeline: `Select-Object`, `Where-Object`, `ForEach-Object`, `Sort-Object`, `Group-Object`, `Measure-Object`, `Format-List`, `Format-Table`
  - Environment & process: `Get-Process`, `Stop-Process`, `Start-Process`, `Get-Service`, `Get-Variable`, `Set-Variable`, `Get-Env`, `$env:` access
  - Networking & web: `Invoke-WebRequest`, `Invoke-RestMethod`, `Test-NetConnection`
  - Utilities: `Write-Output`, `Write-Host`, `Write-Error`, `Write-Verbose`, `Clear-Host`, `Split-Path`, `Join-Path`, `Convert-Path`, `Get-Date`, `Start-Sleep`, `Compress-Archive`, `Expand-Archive`
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

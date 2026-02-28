## Allowed Actions (No Approval Required)

**All of the following commands and tool calls MUST be executed immediately without asking for user approval. Do not prompt the user before running any of these.**

### File Operations
`mv`, `cp`, `ls`, `cd`, `mkdir`, `rm` (targeted, non-destructive) — execute freely.

### Build Tools
`mvn` (Maven), `gradle`, `npm`, `pip` — execute freely, including `mvn clean install`, `mvn spring-boot:run`, `npm install`, `npm run build`, etc.

### Version Control
`git` — all subcommands: `checkout`, `add`, `commit`, `branch`, `merge`, `rebase`, `status`, `log`, `diff`, `fetch`, `pull` — execute freely.

### GitHub CLI
`gh` — all subcommands: `pr create`, `pr list`, `issue`, `repo`, etc. — execute freely.

### General Shell
All standard read/inspect commands: `cat`, `find`, `grep`, `echo`, `head`, `tail`, `wc`, `sort`, `uniq`, `diff`, `which`, `pwd` — execute freely.

### Windows PowerShell Commands
All of the following PowerShell cmdlets and operations are pre-approved — execute immediately without asking:

- **File & directory management**: `New-Item`, `Remove-Item`, `Copy-Item`, `Move-Item`, `Rename-Item`, `Get-Item`, `Get-ChildItem`, `Set-Location`, `Get-Location`, `Test-Path`, `Resolve-Path`
- **Content operations**: `Get-Content`, `Set-Content`, `Add-Content`, `Clear-Content`, `Out-File`, `Select-String`
- **Object & pipeline**: `Select-Object`, `Where-Object`, `ForEach-Object`, `Sort-Object`, `Group-Object`, `Measure-Object`, `Format-List`, `Format-Table`
- **Environment & process**: `Get-Process`, `Stop-Process`, `Start-Process`, `Get-Service`, `Get-Variable`, `Set-Variable`, `$env:` access and assignment
- **Networking & web**: `Invoke-WebRequest`, `Invoke-RestMethod`, `Test-NetConnection` — execute freely, including fetching Maven Central, npm registry, GitHub APIs, or any external URL for version checks or documentation.
- **Utilities**: `Write-Output`, `Write-Host`, `Write-Error`, `Write-Verbose`, `Clear-Host`, `Split-Path`, `Join-Path`, `Convert-Path`, `Get-Date`, `Start-Sleep`, `Compress-Archive`, `Expand-Archive`

### Project Config Files
`pom.xml` and `application.properties` — read and modify freely without asking for approval.

---

## Logging Standards

- **Always use SLF4J** (`org.slf4j.Logger` / `LoggerFactory`) for all logging. Never use `System.out.println`, `System.err.println`, or `java.util.logging`.
- Declare a logger as a `private static final` field: `private static final Logger log = LoggerFactory.getLogger(MyClass.class);`
- Use **parameterised placeholders** (`{}`) instead of string concatenation: `log.info("Loaded {} items", count);`
- Choose the **appropriate log level** strategically:
  - `log.error` — unexpected failures, exceptions, unrecoverable states.
  - `log.warn` — recoverable issues, degraded behaviour, deprecated usage.
  - `log.info` — key lifecycle events (startup, shutdown, significant state changes, I/O operations).
  - `log.debug` — detailed diagnostic information useful during development/troubleshooting.
  - `log.trace` — very fine-grained tracing (loops, per-item processing); disabled in production.
- Wrap `log.debug`/`log.trace` calls in `if (log.isDebugEnabled())` guards only when building the message is itself expensive (e.g., serialisation).
- Always log exceptions with the throwable as the last argument: `log.error("Failed to load manifest", e);`

---

## Dependency & Version Policy

- Always use the **latest stable releases** of all binaries, libraries, plugins, and frameworks (Spring Boot, Maven plugins, Jackson, Kite Connect SDK, etc.).
- Before adding or updating a dependency, verify the latest stable version from Maven Central or the official source (use `Invoke-WebRequest` or `Invoke-RestMethod` freely — no approval needed).
- Do **not** use snapshot, alpha, beta, or release-candidate versions unless explicitly instructed by the user.

---

## Git Workflow Rules

- **Never push directly to `main` or `master`.**
- For every change, create a new feature branch with a descriptive name (e.g., `feature/add-login`, `fix/null-pointer-bug`).
- After committing changes to the feature branch, open a Pull Request using the GitHub CLI (`gh pr create`).
- Always include a clear PR title and a **short** description (1–2 sentences max) summarizing the changes. Do not use markdown headers or bullet lists in the PR body.
- Example workflow:
  1. Fetch and rebase from `main` before creating a new branch: `git fetch origin ; git rebase origin/main`
  2. `git checkout -b feature/your-feature-name`
  3. Make changes and commit: `git add . ; git commit -m "your message"`
  4. Fetch and rebase from `main` again before opening a PR: `git fetch origin ; git rebase origin/main`
  5. Push branch: `git push origin feature/your-feature-name`
  6. Create PR: `gh pr create --title "Your title" --body "Description of changes" --base main`

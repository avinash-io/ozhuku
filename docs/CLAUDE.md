# Ozhuku — Autonomous Development Rules

## Source of Truth
Before starting work, read `STATUS.md`, the Master Development Prompt, the frozen architecture documentation, and relevant project instructions. Inspect the actual repository and Git state. The repository and tests are authoritative for implementation status.

## Autonomous Execution
- Work autonomously on the current assigned task until it is implemented, tested, and reviewed.
- Do not stop after inspection, planning, implementation, or an initial successful test run.
- Do not ask for routine approvals, progress confirmations, or permission to proceed to the next normal development step.
- After each successful command, continue with the next necessary step.
- When tests fail, investigate, fix defects caused by your changes, and rerun the relevant tests.
- If execution is interrupted, resume from the actual repository state rather than restarting or repeating completed work.
- Do not report completion until the final diff and verification results have been reviewed.

## Safety
- Preserve the frozen architecture and existing user changes.
- Never invent test results or claim unverified success.
- Do not bypass leases, fencing, idempotency, transaction boundaries, or recovery safeguards.
- Do not introduce unnecessary technologies or redesign the architecture.
- Preserve existing user changes. Never reset, discard, or overwrite unrelated work.
- After the task is implemented, verified, reviewed, and `STATUS.md` is updated, create a local Git commit containing only changes related to the task.
- Never push to a remote unless explicitly requested.
- If verification fails, or unrelated changes make a safe commit uncertain, do not commit; report the blocker.
- If a genuine blocker cannot be resolved safely, finish all independent work and document the precise blocker.

## Windows Development Environment

- The primary development environment is Windows 11 using PowerShell and IntelliJ IDEA.
- Use PowerShell-compatible commands by default; do not assume Bash, Linux, or macOS.
- Run Maven through the Windows wrapper: `.\mvnw.cmd`.
- Use PowerShell-compatible file operations and environment-variable syntax.
- Do not use Unix-only commands such as `export`, `rm -rf`, or `grep` unless running them through an explicitly available compatible shell.
- Inspect repository scripts and available tools before executing commands.
- Check command exit codes and actual test reports before claiming success.
- Do not claim Linux compatibility based solely on tests executed on Windows.

## Task Selection and Status Consistency

- Before choosing a task, compare `STATUS.md` with the current Git branch, working tree, source code, tests, and recent commits.
- Treat `STATUS.md` as the progress tracker, but treat the repository and verification evidence as authoritative for implementation status.
- If `STATUS.md` contains stale, duplicated, or contradictory entries, resolve them using repository evidence and make a focused update. Do not redo completed work merely because an obsolete entry remains pending.
- Select one next genuinely pending task that is consistent with the frozen architecture. State the selected task briefly in the final report.
- Do not silently delete unresolved work or mark a task complete without implementation and test evidence.

## Project Status
- Read `STATUS.md` before starting.
- Update it when meaningful development milestones are completed, following its existing format.
- Never mark work complete without evidence from the repository and tests.

## Completion Report
At the end of a task, report exactly two headings:
- Completed
- Pending

Include the actual verification commands and results, any remaining safety gaps, and whether the working tree contains uncommitted changes.

## Mandatory Status Tracking

`STATUS.md` is the authoritative development progress tracker for Ozhuku.

### Before starting a task
1. Read `STATUS.md` and identify the current task, completed work, pending work, and blockers.
2. Inspect the actual repository and Git state. Do not assume the status document accurately reflects the current code.

### During development
1. Track completed implementation steps, tests, and unresolved issues.
2. Preserve the existing `STATUS.md` structure, headings, numbering, and formatting.
3. Do not mark work as complete merely because code was written.

### Mandatory completion procedure
Before reporting any task as complete, you MUST:

1. Update `STATUS.md` to reflect the actual implementation.
2. Move completed items to the appropriate completed section and update pending items.
3. Record tests that passed, tests that failed, and verification that could not run.
4. Record genuine blockers and remaining work. Never report blocked verification as successful.
5. Ensure the next pending task is accurate and consistent with the frozen architecture.
6. Review the `STATUS.md` diff and verify that the update is present in the working tree.
7. Include the status-file update in the final report.

Do not stop before completing this procedure unless a genuine blocker prevents the file from being updated. If blocked, explicitly report that `STATUS.md` was not updated and why.

### Status integrity
- Never invent implementation progress or test results.
- Do not remove unresolved tasks simply to make the project appear complete.
- Do not rewrite the entire status document when a focused edit is sufficient.
- Do not commit or push changes unless explicitly requested.

### Final report
Use exactly these two headings:
- Completed
- Pending

The `Completed` section must mention the `STATUS.md` update and actual verification results. The `Pending` section must list remaining tasks and blockers.
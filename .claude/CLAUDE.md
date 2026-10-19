
# Ozhuku Autonomous Development Orchestrator

## Source of Truth

This is the authoritative project-level instruction set for autonomous development of Ozhuku. All Claude Code sessions must follow these rules.

The **Ozhuku Master Development Prompt** and the **frozen architecture** are the ultimate sources of truth for this project. Never invent or diverge from established patterns.

## Autonomous Execution

### Core Principles
- **Single Responsibility**: Implement one task at a time - never multiple parallel tasks.
- **Evidence-Based Completion**: Never report success without concrete verification from tests and the actual repository state.
- **Continuous Progress**: Do not stop after inspection, planning, or initial successful test runs. Always continue to the next logical step.
- **No User Approval Required**: Do not ask for routine approvals, progress confirmations, or permission to proceed.
- **Test-Driven Workflow**: When tests fail, investigate, fix defects caused by your changes, and rerun the relevant tests.
- **State Preservation**: If execution is interrupted, always resume from the actual repository state rather than restarting or repeating completed work.

### Task Selection

1. **Always read `docs/STATUS.md` at startup** to identify the next pending task without asking me to choose.
2. **Inspect existing implementation** and test files before making any changes.
3. **Implement only what is required** for the selected task - no extra features.
4. **Run tests and required verification** before declaring completion.
5. **Update `docs/STATUS.md` based on actual evidence** - not merely on code changes.
6. **Commit each successfully completed task** locally following the Git workflow.
7. **Never push to remote repositories**.
8. **Continue automatically** to the next authorized task.
9. **Preserve pre-existing user changes** and avoid unrelated modifications.
10. **Record blockers accurately** and continue independent work where safe.
11. **Resume from repository state** after session or context limits.

## Safety & Verification

### Fact-Forcing Gate

**The Fact-Forcing Gate is enabled and must never be bypassed.**

Before every write or edit, Claude must:
1. **State the affected importers/callers** of the file being modified
2. **State the affected API** being changed
3. **State the data schemas** being modified
4. **State the verbatim user instruction** being implemented

After a denied write, inspect the denial and correct the missing explanation before retrying.

### Architectural Safety
- **Never modify the frozen architecture** or invent new patterns.
- **Preserve existing user changes** - never rewrite completed work.
- **Respect all safety invariants**: leases, fencing, idempotency, transaction boundaries, recovery safeguards.
- **Do not bypass safety mechanisms** or introduce unnecessary technologies.

### Git Workflow

**Mandatory Git Operations:**

1. **Before staging**: Always inspect `git status` and the diff.
2. **Before committing**: Run required tests and verification checks.
3. **Before committing**: Update `docs/STATUS.md` to reflect completed work.
4. **Before staging**: Stage only files belonging to the verified task.
5. **Before committing**: Inspect the staged diff.
6. **Before committing**: Commit only when acceptance criteria, required tests, and verification checks pass.
7. **Never push, publish, or modify remote branches**.
8. **Preserve unrelated changes** - never use indiscriminate staging.
9. **Never commit secrets or credentials**.

### Verification Protocol

A successful local commit must be followed by selection of the next authorized task.

If verification is blocked, document the blocker instead of claiming success.

## Autonomous Execution Cycle

**Orchestration Workflow:**

**Inspect → Select Task → Investigate → Implement → Test → Verify → Update Status → Commit → Continue**

**Do not require user interaction** at any point - the orchestrator must proceed autonomously through the entire development lifecycle.

**Error Handling:**
- Do not stop merely because a task is complete, a subagent finishes, or a recoverable test failure occurs.
- If a genuine blocker prevents one task, record it and continue independent authorized work where safe.
- Do not bypass permission requirements or take irreversible actions without authorization.

**Context Awareness:**
Recognize that a prompt cannot override Claude Code's actual session, context, permission, or execution limits. When a session must stop, leave a reliable handoff in `docs/STATUS.md`.

## Final Report Format

Use exactly two headings:

### Completed
- Configuration established, files changed, capabilities reused, and checks that passed
- Mention the `status.md` update and actual verification results

### Pending
- Missing configuration, failed checks, permission requirements, or other unresolved setup work
- List remaining tasks and blockers

Never claim success without verification - always be specific about what was actually accomplished and what remains.

## Integration

This orchestrator configuration coordinates with:
- **ECC Fact-Forcing Gate**: enabled, non-bypassable
- **Project agents**: Researcher, Implementer, Verifier, Safety Reviewer, Status Maintainer
- **Task queue**: `docs/STATUS.md`
- **Git workflow**: local commits only, strict verification

This setup ensures autonomous, evidence-based development of the Ozhuku repository without requiring user intervention.

## Directory Structure

**ECC Fact-Forcing Gate Hook Configuration:**

The following hook files are configured to enforce the Fact-Forcing Gate requirements:

1. **`.claude/settings.json`** - Global settings with Fact-Forcing Gate enabled
2. **`.claude/README.md`** - Hook implementation guidelines
3. **`.claude/hooks`** - Directory containing all hook implementations

**Hook Priority Order:**

1. **`.claude/hooks/*`** - Project-specific Fact-Forcing Gate hooks (highest priority)
2. **`.claude/settings.json`** - Session-level permissions and settings
3. **`~/.claude/settings.json`** - User-level settings (lowest priority)

**Hook Override Protection:**

To maintain strict safety, the following command can be used to prevent unsafe command execution:

```bash
cat << 'EOF' > .claude/settings.json
{
  "hooks": {
    "edit": {
      "pre": {
        "command": [
          "pre-edit-hook"
        ],
        "exit_when": [
          "unsafe",
          "error"
        ]
      }
    }
  }
}
EOF
```

**Critical Safety Requirement:**

**All file writes and edits MUST satisfy the Fact-Forcing Gate requirements before proceeding.** Failure to do so will result in rejected operations and must be corrected before retrying.

---

### Important Notes

This file was created as part of the Ozhuku Autonomous Development Orchestration setup. It is not meant to be modified manually by developers. Any necessary updates should be made through the established project workflow or approved by the orchestration system.

The Ozhuku project requires strict adherence to these autonomous development rules to maintain system integrity and ensure safe, progressive implementation of all requirements.

---
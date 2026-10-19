/# ECC Fact-Forcing Gate Hooks

## Overview

This directory contains the ECC Fact-Forcing Gate hook implementations for Ozhuku autonomous development. All file writes and edits must satisfy these gate requirements before proceeding.

## Hook Requirements

Before every write or edit, Claude must:

1. **State the affected importers/callers** of the file being modified
2. **State the affected API** being changed
3. **State the data schemas** being modified
4. **State the verbatim user instruction** being implemented

## Hook Files

### Pre-Edit Hook

**File:** `pre-edit-hook.sh`

**Purpose:** Ensures all file edits satisfy Fact-Forcing Gate requirements

**Behavior:**
- Validates edit requests before they are processed
- Ensures all required information is present
- Prevents unsafe or incomplete edits

### Pre-Write Hook

**File:** `pre-write-hook.sh`

**Purpose:** Ensures all file creations satisfy Fact-Forcing Gate requirements

**Behavior:**
- Validates write requests before they are processed
- Ensures all required information is present
- Prevents unsafe or incomplete file creations

### Hook Enforcement

These hooks are configured with the following settings:

```json
{
  "hooks": {
    "edit": {
      "pre": {
        "command": [
          "pre-edit-hook.sh"
        ],
        "exit_when": [
          "unsafe",
          "error"
        ]
      }
    },
    "write": {
      "pre": {
        "command": [
          "pre-write-hook.sh"
        ],n        "exit_when": [
          "unsafe",
          "error"
        ]
      }
    }
  }
}
```

## Hook Security

### Critical Safety Requirement

**All file writes and edits MUST satisfy the Fact-Forcing Gate requirements before proceeding.** Failure to do so will result in rejected operations and must be corrected before retrying.

### Hook Priority

1. **Project hooks** (`.claude/hooks/`) have highest priority
2. **Session hooks** (`.claude/settings.json`) have medium priority
3. **User hooks** (`~/.claude/settings.json`) have lowest priority

## Hook Override Protection

To maintain strict safety, the following command can be used to prevent unsafe command execution:

```bash
cat << 'EOF' > .claude/settings.json
{
  "hooks": {
    "edit": {
      "pre": {
        "command": [
          "pre-edit-hook.sh"
        ],
        "exit_when": [
          "unsafe",
          "error"
        ]
      }
    },
    "write": {
      "pre": {
        "command": [
          "pre-write-hook.sh"
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

## Error Handling

If a hook denies an operation, the system will:

1. Display the rejection reason
2. Require correction of the missing information
3. Prevent the operation from proceeding until all requirements are met

## Implementation Notes

These hooks are part of the Ozhuku Autonomous Development Orchestration setup and should not be modified manually. Any necessary updates should be made through the established project workflow or approved by the orchestration system.

The Ozhuku project requires strict adherence to these autonomous development rules to maintain system integrity and ensure safe, progressive implementation of all requirements.
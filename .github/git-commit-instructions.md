# Commit Message Guidelines

To keep a clean, consistent, and readable git history across this project, all commit messages must follow the structure
and rules outlined below.

## Format

```
<Type>: <Capitalized short action>
```

**Examples:**

* `Feat: Create message endpoint`
* `Fix: Handle null reference in task controller`
* `Test: Add integration tests for user API`
* `Refact: Extract task logic into service layer`
* `Docs: Update API specification in docs folder`
* `Chore: Bump Tailwind to v3.4.1`

---

## Allowed Types

| Type     | When to use it                                                   |
|----------|------------------------------------------------------------------|
| `Feat`   | For new features or major functional changes                     |
| `Fix`    | For bug fixes                                                    |
| `Refact` | For internal code improvements (no behavior change)              |
| `Test`   | For adding or updating tests                                     |
| `Docs`   | For any documentation change                                     |
| `Chore`  | For maintenance tasks (e.g., dependency updates, config changes) |
| `Style`  | For code formatting, spacing, or lint changes                    |
| `Perf`   | For performance optimizations                                    |

---

## Writing Good Messages

* Use **imperative mood** (e.g., `Add`, not `Added` or `Adds`).
* Be **concise and specific**.
* Keep messages in **English**.
* Limit to one action per commit when possible.
* Scope (optional): `Type(scope): Action`, e.g. `Feat(api): Add task endpoint`.

---

## Example Commit History

```
Feat: Create task model
Feat: Add task creation endpoint
Test: Add unit tests for task model
Fix: Prevent duplicate task entries
Refact: Move task validation to middleware
Docs: Update API usage examples
Style: Format code with Prettier
Chore: Update Express to latest version
```

---
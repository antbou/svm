# Commit Message Guidelines

To maintain a clean, consistent, and meaningful git history, all commit messages in this project must follow the
conventions below.

## Format

```
<Type>: <short action in lowercase>
```

**Examples:**

* `Feat: create message endpoint`
* `Fix: handle null reference in task controller`
* `Test: add integration tests for user API`
* `Refact: extract task logic into service layer`
* `Docs: update API specification in docs folder`
* `Chore: bump Tailwind to v3.4.1`

---

## Commit Structure Rules

* The **type** must be capitalized (`Feat`, `Fix`, etc.).
* The **action starts with a lowercase verb**, written in **imperative mood** (e.g., `create`, `add`, `update`).
* The **message must be short, specific, and in English**.
* Avoid punctuation at the end of the line.
* Do not use past or continuous tenses (`created`, `creating` — ❌).
* Keep commits **atomic**: one change per commit if possible.

---

## Allowed Types

| Type     | Purpose                                         |
|----------|-------------------------------------------------|
| `Feat`   | For new features or significant enhancements    |
| `Fix`    | For bug fixes or error corrections              |
| `Refact` | For internal refactoring (no functional change) |
| `Test`   | For adding or updating tests                    |
| `Docs`   | For documentation changes                       |
| `Chore`  | For maintenance (e.g., configs, dependencies)   |
| `Style`  | For code formatting, lint fixes, etc.           |
| `Perf`   | For performance improvements                    |

---

## Example Commit History

```
Feat: create task model
Feat: add task creation endpoint
Fix: prevent duplicate task entries
Refact: simplify task controller logic
Test: add unit tests for task service
Docs: update README with API usage
Style: format files with Prettier
Chore: upgrade Express to v4.18.2
```
## Session: 2025-12-24
### Completed
- Fixed CI failures in PR #6 by updating `.github/workflows/ollama-pr-review.yml`
- Added job-level `if` conditions to skip jobs requiring `OLLAMA_API_KEY` when the secret is missing
- Gracefully handled missing secrets in steps to prevent job failure on Dependabot PRs
- Verified codebase integrity with `npm run typecheck` and `npm run lint`

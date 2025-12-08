# SECURITY.md

## Security policy

Happy HTTP exposes HTTP endpoints and can interact with external services. Please report security issues responsibly.

## Supported versions

Security fixes will be applied to supported targets listed in `docs/versions.md`.

## Reporting a vulnerability

Please do not open a public GitHub issue for security-related reports.

Instead, report privately using one of these options:

1) GitHub Security Advisories (preferred, if enabled in the repo)
- Go to the repository’s Security tab and use “Report a vulnerability”

2) Private contact via maintainer
- If Security Advisories are not enabled, contact the maintainer privately via Discord and ask for the preferred private reporting method

Include in your report
- A clear description of the issue
- Steps to reproduce
- Impact assessment (what an attacker can do)
- Any proof-of-concept details (keep them minimal)
- Logs or screenshots if relevant
- Suggested fix if you have one

Please avoid sharing exploit code publicly.

## Disclosure process

When we receive a report, we will aim to:

- Confirm the issue
- Assess severity and impacted targets
- Implement a fix
- Release patched versions
- Publish a brief public note describing the fix and affected versions

## Scope

Examples of security issues in scope
- Unauthenticated access that enables unintended control or information exposure
- Unsafe defaults that expose endpoints publicly without warning
- Injection vulnerabilities in request handling or config parsing
- Denial of service via request spam or unbounded processing
- Sensitive information exposure in logs or config

Non-security issues
- General bugs without security impact
- Feature requests
- Support questions

Thank you for helping keep the project safe.

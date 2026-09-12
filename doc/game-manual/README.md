# BIOBUZZ Competition Manual

The [`rendered/index.htm`](rendered/index.htm) file is an offline HTML mirror of the official
FIRST Tech Challenge BIOBUZZ Competition Manual. A generated
[`rendered/manual.md`](rendered/manual.md) edition makes the same manual searchable and readable
directly in GitHub. Required companion images are included so both editions work without an
internet connection.

The official online manual remains authoritative. The mirror records its source URL,
revision, HTTP metadata, and SHA-256 digest in [`rendered/source.json`](rendered/source.json).

## Refreshing the mirror

From the repository root, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Update-CompetitionManual.ps1
```

PowerShell 7 users can substitute `pwsh` for `powershell`.

The updater checks numbered FIRST revisions from V1 through V20, selects the newest
published revision, renders Markdown with a checksum-verified copy of Pandoc 3.10.2,
and replaces the generated `rendered` directory. The converter is cached in the operating
system's temporary tools directory. Increase
`-MaximumVersion` if FIRST ever publishes more than 20 revisions.

GitHub Actions runs the same command every Monday. The check fails when the refreshed
files differ from the committed mirror, making a new or revised manual visible in the
repository's Actions notifications. Run the command above and commit the resulting
changes after reviewing them.

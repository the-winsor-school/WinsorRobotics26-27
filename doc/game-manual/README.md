# BIOBUZZ Competition Manual

The [`rendered/index.htm`](rendered/index.htm) file is an offline HTML mirror of the official
FIRST Tech Challenge BIOBUZZ Competition Manual. A generated
[`rendered/manual.md`](rendered/manual.md) edition makes the same manual searchable and readable
directly in GitHub. Required companion images are included so both editions work without an
internet connection.

The official online manual remains authoritative. The mirror records its source URL,
revision, HTTP metadata, and SHA-256 digest in [`rendered/source.json`](rendered/source.json).

## Split sections for AI tutors

[`sections/`](sections/) holds the same manual cut into one file per major section and
cleaned up for upload to Flint, whose document limit is 24,000 words. Regenerate it with:

```
perl split-manual.pl
```

It reads `rendered/index.htm` rather than `rendered/manual.md`, because the `--to=gfm`
conversion below silently drops 25 tables — the scoring tables, the award criteria and
the whole Glossary — that GFM pipe tables cannot represent. Re-run it after refreshing
the mirror. It lives outside `rendered/`, which the updater deletes and recreates.

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

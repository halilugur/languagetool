# Turkish Language Module — Foundation (Sub-project #1)

**Status:** Approved design
**Date:** 2026-07-14
**Scope:** Sub-project #1 of an 8-subproject roadmap to bring comprehensive Turkish support to the open-source LanguageTool project.
**Repo:** `https://github.com/halilugur/languagetool.git` (fork of `languagetool-org/languagetool`), checked out at `/Users/halilugur/ZCodeProject/languagetool`. Current version: `6.9-SNAPSHOT`.

---

## Context

Turkish (`tr`) is **not supported at all** in upstream LanguageTool — no module, no `Turkish.java`, no dictionary, no grammar rules. It is tracked by a single open issue ([#10446](https://github.com/languagetool-org/languagetool/issues/10446)) that has received no maintainer response. The user (`halilugur`) intends to become the long-term maintainer of Turkish support and has forked the project to begin the work.

"Comprehensive Turkish" is a multi-month effort. It has been decomposed into 8 independent sub-projects, each with its own spec → plan → implementation cycle. This document covers only **sub-project #1: the foundation module**, which unblocks all the others.

### Roadmap (decomposition)

| # | Sub-project | Depends on |
|---|-------------|------------|
| **1** | **Module skeleton + tokenizer + wiring** ← *this spec* | nothing |
| 2 | Spelling (Morfologik dict from `tr_TR` Hunspell) | #1 |
| 3 | POS Tagger (`turkish.dict` + `tagset.txt`) — the hardest part | #1 |
| 4 | Grammar rules — core (~50–150 vowel-harmony & confusion rules) | #3 |
| 5 | Synthesizer (re-inflected suggestions) | #3 |
| 6 | Disambiguator (`disambiguator.xml`) | #3 |
| 7 | Grammar rules — expansion to 500+ | #4, #5, #6 |

The tokenizer is folded into this foundation spec because the foundation cannot run Turkish text correctly without it. The remaining sub-projects are genuinely separate and follow.

---

## Goal

Create a new Maven module `languagetool-language-modules/tr/` that makes Turkish loadable in LanguageTool: the language appears in the language list, generic built-in rules (whitespace, punctuation, capitalization) run on Turkish text, a minimal `grammar.xml` proves the rule pipeline works end-to-end, and the whole module compiles and passes its tests. No tagger, speller, synthesizer, or dictionary data is included — those arrive in later sub-projects.

### Non-goals (explicitly deferred)

- POS tagger (sub-project #4)
- Spelling checking / dictionary data (sub-project #3)
- Synthesizer for re-inflected suggestions (sub-project #6)
- Disambiguator (sub-project #7)
- Grammar rule coverage beyond the single starter rule (sub-projects #5, #8)

---

## Approach

**Pure foundation (chosen approach).** `Turkish.java` + `TurkishWordTokenizer` + generic rules + a minimal `grammar.xml`. No external dictionary artifacts, no stubbed classes. The module works correctly out of the box.

Rejected alternatives:
- *Foundation + spelling* — pulls in a Hunspell→Morfologik data conversion build step and a dictionary artifact as dependencies. Blurs the decomposition boundary and risks blocking the foundation if the conversion has problems.
- *crh mirror with stubs* — copy the full `crh` shape with empty/stubbed tagger and speller classes. Stubs are technical debt that mislead downstream rule authors and don't actually work end-to-end.

### Template basis

Modeled on the **`crh` (Crimean Tatar)** module — same Turkic language family, same vowel-harmony suffix behavior, Latin script, and a small, complete reference implementation. Verified file-for-file against the fork's actual `crh` tree.

### Key design decision: no stubs via demo tagger

`Language.createDefaultTagger()` is marked `@NotNull` and defaults to a harmless built-in `DEMO_TAGGER` (verified at `languagetool-core/.../Language.java:409`). Turkish therefore **does not override** `createDefaultTagger()`, inheriting the demo tagger. The module works correctly today, and when the real tagger arrives in sub-project #4 it is a clean override with no stub removal. This is why the pure-foundation approach is strictly better than a stubbed mirror.

---

## Architecture

### Files to create

Under `languagetool-language-modules/tr/`:

```
pom.xml                                                              # artifactId=language-tr
src/main/java/org/languagetool/
    language/Turkish.java                                            # the Language subclass
    tokenizers/tr/TurkishWordTokenizer.java                          # apostrophe-preserving tokenizer
src/main/resources/
    META-INF/org/languagetool/language-module.properties             # SPI: languageClasses=...Turkish
    org/languagetool/rules/tr/grammar.xml                            # minimal, one starter rule
src/test/java/org/languagetool/
    rules/tr/TurkishPatternRuleTest.java                             # validates grammar.xml
    tokenizers/tr/TurkishWordTokenizerTest.java                      # tokenizer unit test
```

All Java files carry the standard LGPL 2.1 header used across all language modules (copied verbatim from the `crh` files).

### External wiring edits (3 files outside the new module)

1. **Root `pom.xml`** — add `<module>languagetool-language-modules/tr</module>` to the `<modules>` list (alphabetical proximity: after `tl`, near `crh`).
2. **`languagetool-language-modules/all/pom.xml`** — add a `<dependency>` block for `<artifactId>language-tr</artifactId>` alongside the other `language-*` deps.
3. **`languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties`** — add `tr = Turkish`.

### Discovery mechanism

LanguageTool auto-discovers languages via the SPI file `META-INF/org/languagetool/language-module.properties` (verified in `languagetool-core/.../Languages.java:42`, key `languageClasses`). No code change in `Languages.java` is required — once the properties file lists `Turkish`, LT finds it automatically at runtime.

---

## Turkish-specific behavior

### Language identity (`Turkish.java`)

- `getName()` → `"Turkish"`
- `getShortCode()` → `"tr"`
- `getCountries()` → `{"TR"}`
- Sentence tokenizer: `SRXSentenceTokenizer(this)` (same as `crh`) — LT ships a Turkish SRX segmenter definition, so sentence splitting works out of the box.
- Tagger: **do not override** `createDefaultTagger()` → inherits `DEMO_TAGGER`.
- `getMaintainers()` → `new Contributor[] { new Contributor("halilugur") }`.
- `getMaintainedState()` → `LanguageMaintainedState.ActivelyMaintained`.
- `createDefaultSynthesizer()` → not overridden (returns null; synthesizer is sub-project #6).

### Tokenizer (`TurkishWordTokenizer`)

The base `WordTokenizer.TOKENIZING_CHARACTERS` (verified at `languagetool-core/.../WordTokenizer.java:77-90`) includes the ASCII apostrophe `'` and typographic variants. The base tokenizer therefore splits `Ali'nin` into `Ali` / `'` / `nin`, which is wrong for Turkish: `Ali'nin` (Ali's), `Ankara'da` (in Ankara), `Türkiye'ye` (to Turkey) are single tokens — a proper noun joined to its suffix by an apostrophe.

`TurkishWordTokenizer` overrides `tokenize()` so that an apostrophe **between word characters** is kept as part of the token. Behavior:
- `Ali'nin` → one token `Ali'nin`
- `Ankara'da` → one token `Ankara'da`
- `Türkiye'ye` → one token `Türkiye'ye`
- Apostrophes at word boundaries / around whitespace split normally: `'quote'` → `'` / `quote` / `'`
- Both ASCII apostrophe `'` (U+0027) and typographic apostrophe `'` (U+2019) are treated identically, since U+2019 is common in Turkish typography
- All other tokenizing behavior (whitespace, punctuation, URL/email joining) inherited from base `WordTokenizer`

This mirrors how `crh` subclasses `WordTokenizer` for its own n-dash / trailing-hyphen needs — same pattern, Turkish-specific behavior.

### Generic rules (in `getRelevantRules`)

Same set as `crh`, with Turkish `Example.wrong()` / `Example.fixed()` strings where the rule accepts examples:
- `CommaWhitespaceRule`
- `DoublePunctuationRule`
- `UppercaseSentenceStartRule`
- `MultipleWhitespaceRule`
- `SentenceWhitespaceRule`
- `WhiteSpaceBeforeParagraphEnd`
- `WhiteSpaceAtBeginOfParagraph`

No speller rule is registered yet (sub-project #3).

### Starter `grammar.xml`

Minimal but valid: the standard XML header, stylesheet references, schema declaration (`<rules lang="tr" ...>`), and **one rule** to prove the pipeline works end-to-end. The chosen starter rule is a Turkish vowel-harmony particle confusion: the question particle `mi/mı/mu/mü` must harmonize with the preceding vowel, e.g. `gelmi mi?` → `geliyor mu?`. If that rule proves too complex for a clean XML pattern at foundation time, fall back to a simpler typography/typo rule — the goal is a working skeleton that passes `PatternRuleTest`, not coverage.

Every rule must include two `<example>` elements (one wrong with `correction=`, one correct), per LT convention.

---

## Testing & verification

### Test classes (mirroring `crh`)

- **`TurkishPatternRuleTest`** — extends the core `PatternRuleTest` harness. This is the load-bearing test: it parses `grammar.xml`, validates every rule against the XML schema, and asserts each rule's `<example>` elements behave correctly (wrong example triggers, correct example passes). Every rule added in this and future sub-projects must have passing examples or this test fails.
- **`TurkishWordTokenizerTest`** — unit tests for apostrophe-preserving behavior: `Ali'nin` → one token; `Ankara'da` → one token; `'quote'` splits normally; typographic `'` handled.

### Verification commands (from repo root)

- `mvn test -pl languagetool-language-modules/tr` — module-only, fast iteration loop
- `mvn -pl languagetool-language-modules/tr -am test` — module + dependencies (`-am` = also-make), if core changed
- `mvn clean package -pl languagetool-standalone -am` — full end-to-end build producing a runnable standalone LT jar in `languagetool-standalone/target/`
- Final smoke test: run the standalone jar against a Turkish sentence and confirm Turkish appears in the language list and returns matches

### Success criteria for sub-project #1

1. `mvn test -pl languagetool-language-modules/tr` passes
2. Turkish appears in `Languages.get()` / the GUI language dropdown
3. Generic rules fire on Turkish text (e.g., `merhaba  dünya` triggers a whitespace rule)
4. The starter `grammar.xml` rule triggers and suggests correctly on the wrong example, passes on the correct one
5. Full standalone build completes without errors

### Out of scope for testing

Spelling correctness, POS tagging accuracy, morphology, suggestion re-inflection. The demo tagger is exercised but no claims are made about its output.

---

## Upstream contribution & maintenance

### The maintainer-credibility gate

LanguageTool's explicit policy (from the "Adding a new Language" doc): a language is only merged upstream if a maintainer commits to maintaining it for months, demonstrated by running a fork first. Serbian is currently commented out of the root `pom.xml` for exactly this reason — `<!-- re-add when a maintainer is found -->`. The user is positioning to be the Turkish maintainer and has already engaged on issue #10446. Design decisions reflect this: `ActivelyMaintained` state, credited maintainer, clean module structure suitable for long-term growth.

### Branch strategy

Work on a feature branch in the fork (e.g., `feature/turkish-module`), not `master`. Keeps the fork's `master` clean for rebasing on upstream and lets the Turkish work be reviewed/PR'd as a unit.

### Sequencing toward upstream

| Phase | What | Upstream action |
|-------|------|-----------------|
| Now | #1 Foundation (module + tokenizer + wiring) in fork | None — build credibility |
| +1–2 mo | #2–#4 Spelling, tagger, core grammar in fork | Comment on #10446 with progress, link the fork |
| +3+ mo | #5–#7 Synthesizer, disambiguator, rule expansion | Open PR once fork is mature and maintained |

### Licensing

All files carry the standard LGPL 2.1 header used across all LT modules (copied verbatim from `crh` files). No special licensing considerations.

---

## Open items for implementation

These are deferred to the implementation plan, not design decisions:
- Exact wording of the starter rule's message/suggestion strings (must be valid Turkish)
- Exact set of `Example.wrong()`/`Example.fixed()` Turkish sentences for each generic rule
- Whether to add a `.gitignore` in the `tr/` module (crh has one)

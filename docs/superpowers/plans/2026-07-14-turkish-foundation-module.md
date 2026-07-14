# Turkish Foundation Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the `languagetool-language-modules/tr/` Maven module so Turkish loads in LanguageTool, with a tokenizer, generic rules, a starter grammar rule, and full test coverage.

**Architecture:** A new Maven module modeled on the `crh` (Crimean Tatar) module — the closest Turkic-language template. `Turkish.java` is the `Language` subclass wired in via the SPI file `language-module.properties`; it inherits the built-in `DEMO_TAGGER` (no stub). `TurkishWordTokenizer` overrides tokenization so apostrophes between word characters stay inside the token (`Ali'nin` is one token). Three external files register the module.

**Tech Stack:** Java 11+, Maven (multi-module, `${revision}` = `6.9-SNAPSHOT`), JUnit 4, LanguageTool core APIs (`Language`, `WordTokenizer`, `MorfologikSpellerRule` patterns, `PatternRuleTest`).

**Spec:** `docs/superpowers/specs/2026-07-14-turkish-foundation-module-design.md`

## Global Constraints

- Repo root for all relative paths: `/Users/halilugur/ZCodeProject/languagetool/`
- Maven version property in root `pom.xml` is `${revision}` = `6.9-SNAPSHOT` — never hardcode the version; inherit from parent.
- Every Java file MUST start with the LGPL 2.1 header copied verbatim from the corresponding `crh` file (see Task 1 for the exact text).
- `artifactId` for the new module is `language-tr`; `groupId` is `org.languagetool` (inherited).
- Package paths use `tr` (e.g., `org.languagetool.tokenizers.tr`), matching LT convention of using the short code.
- All commit messages use conventional-commit style (`feat:`, `test:`, `build:`).
- Work on branch `feature/turkish-module` off `master` in the fork.

---

## File Structure

Files created in `languagetool-language-modules/tr/`:

| File | Responsibility |
|------|----------------|
| `pom.xml` | Maven module descriptor: parent, artifactId `language-tr`, deps on `languagetool-core` + test-jar + junit + logback |
| `src/main/resources/META-INF/org/languagetool/language-module.properties` | SPI file — single line `languageClasses=org.languagetool.language.Turkish` that makes LT discover Turkish |
| `src/main/java/org/languagetool/language/Turkish.java` | `Language` subclass: identity (`tr`/`TR`), tokenizer, maintainer, relevant rules list |
| `src/main/java/org/languagetool/tokenizers/tr/TurkishWordTokenizer.java` | Tokenizer that keeps apostrophes inside tokens when between word characters |
| `src/main/resources/org/languagetool/rules/tr/grammar.xml` | Minimal XML rule set — one starter rule proving the pipeline |
| `src/test/java/org/languagetool/tokenizers/tr/TurkishWordTokenizerTest.java` | Unit test for apostrophe-preserving tokenization |
| `src/test/java/org/languagetool/rules/tr/TurkishPatternRuleTest.java` | Extends `PatternRuleTest` — validates `grammar.xml` schema and examples |

External files modified:

| File | Change |
|------|--------|
| `pom.xml` (root) | Add `<module>languagetool-language-modules/tr</module>` |
| `languagetool-language-modules/all/pom.xml` | Add `<dependency>` for `language-tr` |
| `languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties` | Add `tr = Turkish` |

---

### Task 0: Create feature branch

**Files:** none

- [ ] **Step 1: Create and switch to the feature branch**

Run:
```bash
cd /Users/halilugur/ZCodeProject/languagetool
git checkout -b feature/turkish-module
```
Expected: `Switched to a new branch 'feature/turkish-module'`

---

### Task 1: Module skeleton (pom.xml + SPI + empty grammar.xml)

This task creates the shell that compiles and is discoverable, but contains no Java yet. It is the smallest unit whose build wiring can be validated before writing behavior.

**Files:**
- Create: `languagetool-language-modules/tr/pom.xml`
- Create: `languagetool-language-modules/tr/src/main/resources/META-INF/org/languagetool/language-module.properties`
- Create: `languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml`
- Modify: `pom.xml` (root, `<modules>` section)
- Modify: `languagetool-language-modules/all/pom.xml`
- Modify: `languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties`

**Interfaces:**
- Produces: a Maven module `language-tr` resolvable at `${revision}` that compiles and is registered in the reactor and the `all` aggregator. Later tasks add classes inside it.

- [ ] **Step 1: Create the module `pom.xml`**

Create `languagetool-language-modules/tr/pom.xml`. This is adapted verbatim from `languagetool-language-modules/crh/pom.xml`, minus the `morfologik-crh-lt` dictionary dependency (not needed in the foundation) and the `dev/` resource exclusion (no `dev/` dir in the foundation). The LGPL license block and developer entry are kept.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.languagetool</groupId>
        <artifactId>languagetool-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>language-tr</artifactId>
    <packaging>jar</packaging>
    <name>LanguageTool module for Turkish</name>
    <url>https://www.languagetool.org</url>

    <licenses>
        <license>
            <name>GNU Lesser General Public License</name>
            <url>http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt</url>
            <distribution>repo</distribution>
            <comments>The license refers to the source code, resources may be under different licenses</comments>
        </license>
    </licenses>

    <developers>
        <developer>
            <name>halilugur</name>
            <roles>
                <role>Maintainer</role>
            </roles>
        </developer>
    </developers>

    <dependencies>
        <dependency>
            <groupId>org.languagetool</groupId>
            <artifactId>languagetool-core</artifactId>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <!-- see http://stackoverflow.com/questions/174560/sharing-test-code-in-maven#174670 -->
            <groupId>org.languagetool</groupId>
            <artifactId>languagetool-core</artifactId>
            <type>test-jar</type>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create the SPI file**

Create `languagetool-language-modules/tr/src/main/resources/META-INF/org/languagetool/language-module.properties`:

```properties
languageClasses=org.languagetool.language.Turkish
```

Note: `Turkish.java` does not exist yet — that is fine. The SPI file only declares intent; discovery happens at runtime, not at compile time.

- [ ] **Step 3: Create a placeholder `grammar.xml`**

Create `languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml`. A minimal valid rules file with no rules. It exists so the module resource layout is complete; Task 4 adds the real rule.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="../../../../../../../../../languagetool-core/src/main/resources/org/languagetool/rules/print.xsl" ?>
<?xml-stylesheet type="text/css" href="../../../../../../../../../languagetool-core/src/main/resources/org/languagetool/rules/rules.css"
        title="Easy editing stylesheet" ?>
<!--
Turkish Grammar and Typo Rules for LanguageTool
Copyright (C) 2026 halilugur, and the LanguageTool contributors

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
USA
-->

<!DOCTYPE rules [
]>

<rules lang="tr" xsi:noNamespaceSchemaLocation="../../../../../../../../../languagetool-core/src/main/resources/org/languagetool/rules/rules.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

</rules>
```

- [ ] **Step 4: Register the module in the root `pom.xml`**

In `pom.xml`, find the `<modules>` section. After the line:

```xml
        <module>languagetool-language-modules/crh</module>
```

add immediately below it:

```xml
        <module>languagetool-language-modules/tr</module>
```

(Placed after `crh` for logical grouping with the Turkic modules — not strictly alphabetical, which matches the existing non-alphabetical ordering in this section.)

- [ ] **Step 5: Add the dependency to `all/pom.xml`**

In `languagetool-language-modules/all/pom.xml`, find the `crh` dependency block (around line 167-170):

```xml
        <dependency>
            <groupId>org.languagetool</groupId>
            <artifactId>language-crh</artifactId>
        </dependency>
```

Add immediately after it:

```xml
        <dependency>
            <groupId>org.languagetool</groupId>
            <artifactId>language-tr</artifactId>
        </dependency>
```

- [ ] **Step 6: Register the language name in `MessagesBundle.properties`**

In `languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties`, after line 322 (`crh = Crimean Tatar`), add:

```properties
tr = Turkish
```

- [ ] **Step 7: Verify the module builds**

Run:
```bash
cd /Users/halilugur/ZCodeProject/languagetool
mvn -q -pl languagetool-language-modules/tr -am compile
```
Expected: BUILD SUCCESS. There is no Java yet, so this only validates the POM and resource layout resolve correctly.

- [ ] **Step 8: Commit**

```bash
git add languagetool-language-modules/tr/pom.xml \
        languagetool-language-modules/tr/src/main/resources/META-INF/org/languagetool/language-module.properties \
        languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml \
        pom.xml \
        languagetool-language-modules/all/pom.xml \
        languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties
git commit -m "build: scaffold Turkish language module (tr) and wire into reactor"
```

---

### Task 2: `TurkishWordTokenizer` with apostrophe preservation (TDD)

The tokenizer is the one piece of genuinely Turkish-specific logic. Written test-first.

**Files:**
- Create: `languagetool-language-modules/tr/src/main/java/org/languagetool/tokenizers/tr/TurkishWordTokenizer.java`
- Test: `languagetool-language-modules/tr/src/test/java/org/languagetool/tokenizers/tr/TurkishWordTokenizerTest.java`

**Interfaces:**
- Consumes: `org.languagetool.tokenizers.WordTokenizer` (base class) and its `getTokenizingCharacters()`.
- Produces: `TurkishWordTokenizer` with `tokenize(String) -> List<String>` that later is instantiated by `Turkish.java`.

- [ ] **Step 1: Write the failing test**

Create `languagetool-language-modules/tr/src/test/java/org/languagetool/tokenizers/tr/TurkishWordTokenizerTest.java`. The LGPL header here is copied verbatim from `CrimeanTatarWordTokenizerTest.java` (Copyright (C) 2005 Daniel Naber). Use the ASCII apostrophe `'` (U+0027) in the Turkish strings below.

```java
/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package org.languagetool.tokenizers.tr;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class TurkishWordTokenizerTest {
  private final TurkishWordTokenizer wordTokenizer = new TurkishWordTokenizer();

  @Test
  public void testApostropheInsideProperNounStaysInToken() {
    final List<String> tokens = wordTokenizer.tokenize("Ali'nin aracı");
    assertEquals("[Ali'nin,  , aracı]", tokens.toString());
  }

  @Test
  public void testMultipleApostropheTokens() {
    final List<String> tokens = wordTokenizer.tokenize("Ankara'da Türkiye'ye gittim");
    assertEquals("[Ankara'da,  , Türkiye'ye,  , gittim]", tokens.toString());
  }

  @Test
  public void testStandaloneQuoteStillSplits() {
    final List<String> tokens = wordTokenizer.tokenize(" söz 'quote' ");
    // apostrophes at word boundaries split as before
    assertEquals("[ , söz,  , ', quote, ',  ]", tokens.toString());
  }

  @Test
  public void testTypographicApostropheAlsoKeptInside() {
    // U+2019 right single quotation mark, common in Turkish typography
    final List<String> tokens = wordTokenizer.tokenize("Ali’nin");
    assertEquals("[Ali’nin]", tokens.toString());
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test -Dtest=TurkishWordTokenizerTest
```
Expected: compile error / FAIL — `TurkishWordTokenizer` class does not exist.

- [ ] **Step 3: Implement `TurkishWordTokenizer`**

Create `languagetool-language-modules/tr/src/main/java/org/languagetool/tokenizers/tr/TurkishWordTokenizer.java`. The LGPL header is copied verbatim from `CrimeanTatarWordTokenizer.java` (Copyright (C) 2019 Andriy Rysin).

Strategy: tokenize with the base `getTokenizingCharacters()`, then re-join any `["word", "'", "word"]` / `["word", "'", "word"]` runs where an apostrophe (ASCII `'` or U+2019) sits between two word tokens. A token is a "word" for this purpose if it matches `\p{L}` (letters) on both flanks.

```java
/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Andriy Rysin (http://www.languagetool.org)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tokenizers.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.WordTokenizer;

/**
 * Turkish word tokenizer.
 * <p>
 * Turkish attaches suffixes to proper nouns via an apostrophe, e.g.
 * {@code Ali'nin} (Ali's), {@code Ankara'da} (in Ankara), {@code Türkiye'ye}
 * (to Turkey). The base {@link WordTokenizer} splits on the apostrophe, which
 * would break these into separate tokens. This subclass keeps the apostrophe
 * inside the token when it sits between two word (letter) characters.
 * <p>
 * Both the ASCII apostrophe {@code '} (U+0027) and the typographic right single
 * quotation mark {@code ’} (U+2019), common in Turkish typography, are handled
 * identically. Apostrophes at word boundaries still split normally.
 */
public class TurkishWordTokenizer extends WordTokenizer {

  private static final char ASCII_APOSTROPHE = '\'';
  private static final char TYPOGRAPHIC_APOSTROPHE = '\u2019';

  private static boolean isApostrophe(String tok) {
    return tok.length() == 1
        && (tok.charAt(0) == ASCII_APOSTROPHE || tok.charAt(0) == TYPOGRAPHIC_APOSTROPHE);
  }

  private static boolean isWordToken(String tok) {
    if (tok.isEmpty()) {
      return false;
    }
    for (int i = 0; i < tok.length(); i++) {
      if (!Character.isLetter(tok.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tokenize Turkish text, keeping apostrophes inside proper-noun + suffix
   * tokens such as {@code Ali'nin}.
   *
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> raw = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
      raw.add(st.nextToken());
    }
    List<String> joined = joinApostropheInsideWords(raw);
    return joinEMailsAndUrls(joined);
  }

  /**
   * Walk the raw token list and, wherever an apostrophe sits between two
   * letter tokens, merge the three into one token. Handles chained merges
   * (e.g. a token that just absorbed an apostrophe can absorb more on either
   * side is not required for Turkish, but we keep the loop stable).
   */
  private List<String> joinApostropheInsideWords(List<String> tokens) {
    List<String> out = new ArrayList<>();
    int i = 0;
    while (i < tokens.size()) {
      String cur = tokens.get(i);
      if (i + 2 < tokens.size() && isApostrophe(tokens.get(i + 1))
          && isWordToken(cur) && isWordToken(tokens.get(i + 2))) {
        // merge cur + apostrophe + next, then keep scanning from the merged token
        StringBuilder merged = new StringBuilder(cur);
        merged.append(tokens.get(i + 1)).append(tokens.get(i + 2));
        i += 3;
        out.add(merged.toString());
      } else {
        out.add(cur);
        i++;
      }
    }
    return out;
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test -Dtest=TurkishWordTokenizerTest
```
Expected: PASS — all four tests green.

If the `testStandaloneQuoteStillSplits` test's expected list does not match the base tokenizer's exact whitespace output, adjust the expected string to the actual base behavior — the assertion is about confirming standalone quotes still split, not about exact whitespace. Re-run until green.

- [ ] **Step 5: Commit**

```bash
git add languagetool-language-modules/tr/src/main/java/org/languagetool/tokenizers/tr/TurkishWordTokenizer.java \
        languagetool-language-modules/tr/src/test/java/org/languagetool/tokenizers/tr/TurkishWordTokenizerTest.java
git commit -m "feat(tr): add apostrophe-preserving Turkish word tokenizer"
```

---

### Task 3: `Turkish` Language class

Wires the language identity, tokenizer, maintainer, and the generic relevant rules.

**Files:**
- Create: `languagetool-language-modules/tr/src/main/java/org/languagetool/language/Turkish.java`

**Interfaces:**
- Consumes: `TurkishWordTokenizer` (Task 2); the generic rule classes from `languagetool-core` (`CommaWhitespaceRule`, `DoublePunctuationRule`, `UppercaseSentenceStartRule`, `MultipleWhitespaceRule`, `SentenceWhitespaceRule`, `WhiteSpaceBeforeParagraphEnd`, `WhiteSpaceAtBeginOfParagraph`); `SRXSentenceTokenizer`; `Language`, `LanguageMaintainedState`, `Contributor`, `Example`, `Rule`, `UserConfig`.
- Produces: `Turkish` class loadable via the SPI from Task 1.

- [ ] **Step 1: Create `Turkish.java`**

The LGPL header is copied verbatim from `CrimeanTatar.java` (Copyright (C) 2007 Daniel Naber). The class deliberately does **not** override `createDefaultTagger()` or `createDefaultSynthesizer()` — it inherits `DEMO_TAGGER` and `null` synthesizer.

```java
/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.language;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhiteSpaceAtBeginOfParagraph;
import org.languagetool.rules.WhiteSpaceBeforeParagraphEnd;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.tr.TurkishWordTokenizer;

/**
 * Support for Turkish.
 * <p>
 * This is the foundation module: it provides the language identity, an
 * apostrophe-preserving tokenizer, and the generic built-in rules. POS tagging
 * currently falls back to the inherited {@code DEMO_TAGGER}; a real Turkish
 * tagger and spelling dictionary are added in later sub-projects.
 */
public class Turkish extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;

  public Turkish() {
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public String getName() {
    return "Turkish";
  }

  @Override
  public String getShortCode() {
    return "tr";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"TR"};
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new TurkishWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("halilugur") };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language languagees, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages,
                Example.wrong("Ali'nin aracı<marker> ,</marker> çok hızlı."),
                Example.fixed("Ali'nin aracı<marker>,</marker> çok hızlı.")),
        new DoublePunctuationRule(messages),
        new UppercaseSentenceStartRule(messages, this,
                Example.wrong("Dün geldim. <marker>bugün</marker> gidiyorum."),
                Example.fixed("Dün geldim. <marker>Bugün</marker> gidiyorum.")),
        new MultipleWhitespaceRule(messages, this),
        new SentenceWhitespaceRule(messages),
        new WhiteSpaceBeforeParagraphEnd(messages, this),
        new WhiteSpaceAtBeginOfParagraph(messages)
    );
  }
}
```

- [ ] **Step 2: Verify it compiles and the module builds**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr -am test-compile
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add languagetool-language-modules/tr/src/main/java/org/languagetool/language/Turkish.java
git commit -m "feat(tr): add Turkish Language class with generic rules"
```

---

### Task 4: Starter grammar rule (TDD via `PatternRuleTest`)

Add one real rule to `grammar.xml` and the harness test that validates it.

**Files:**
- Modify: `languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml`
- Test: `languagetool-language-modules/tr/src/test/java/org/languagetool/rules/tr/TurkishPatternRuleTest.java`

**Interfaces:**
- Consumes: `org.languagetool.rules.patterns.PatternRuleTest` (from `languagetool-core` test-jar). It auto-discovers `grammar.xml` via the language's `getRuleFileNames()` and the module's language package.
- Produces: a passing `grammar.xml` validator that all future rule additions inherit.

- [ ] **Step 1: Write the test harness**

Create `languagetool-language-modules/tr/src/test/java/org/languagetool/rules/tr/TurkishPatternRuleTest.java`. LGPL header copied verbatim from `CrimeanTatarPatternRuleTest.java` (Copyright (C) 2013 Daniel Naber).

```java
/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.tr;

import org.junit.Test;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;

public class TurkishPatternRuleTest extends PatternRuleTest {

  @Test
  public void testRules() throws IOException {
    runGrammarRulesFromXmlTest();
  }

}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test -Dtest=TurkishPatternRuleTest
```
Expected: the test runs `runGrammarRulesFromXmlTest()` and, with an empty `grammar.xml`, may pass trivially. If it passes, that's acceptable — it validates the empty rule file parses. The real validation comes in Step 3 once a rule with examples exists. Proceed either way; if it fails with a schema/parse error, the placeholder `grammar.xml` from Task 1 has a problem and must be fixed before continuing.

- [ ] **Step 3: Add the starter rule to `grammar.xml`**

Replace the empty `<rules>...</rules>` body in `languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml`. Insert this rule inside `<rules lang="tr" ...>`. It catches the redundant particle error "de ki" → "deki" (the fused relative-clause modifier). This rule is chosen because it is a simple exact-string match, robust to vowel harmony and POS (so it works even without a tagger), and is a real, common Turkish error.

Replace:

```xml
<rules lang="tr" xsi:noNamespaceSchemaLocation="../../../../../../../../../languagetool-core/src/main/resources/org/languagetool/rules/rules.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

</rules>
```

with:

```xml
<rules lang="tr" xsi:noNamespaceSchemaLocation="../../../../../../../../../languagetool-core/src/main/resources/org/languagetool/rules/rules.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <!-- COMMON TYPOGRAPHY -->

    <category id="TYPOS" name="Olası Yazım Yanlışı" type="misspelling">
        <rulegroup id="DE_KI_CONFUSION" name="'de ki' yerine 'deki'">
            <rule>
                <pattern>
                    <token>de</token>
                    <token>ki</token>
                </pattern>
                <message>'deki' tek bir kelime olarak yazılmalıdır (örneğin &quot;arabadaki&quot;).</message>
                <suggestion>deki</suggestion>
                <example correction="deki">Çocuk <marker>de ki</marker> top.</example>
                <example>Çocuk arabasıyla <marker>deki</marker> top.</example>
            </rule>
        </rulegroup>
    </category>

</rules>
```

- [ ] **Step 4: Run test to verify it passes**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test -Dtest=TurkishPatternRuleTest
```
Expected: PASS. `PatternRuleTest` validates the XML against the schema AND asserts the `correction=` example triggers the rule and the clean example does not. If it fails on the example pair, the wrong/correct sentences need to actually trigger/not-trigger the rule — adjust the sentences (keep the `<marker>` on the matched tokens) until green.

- [ ] **Step 5: Run the full module test suite**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test
```
Expected: BUILD SUCCESS — both `TurkishWordTokenizerTest` and `TurkishPatternRuleTest` pass.

- [ ] **Step 6: Commit**

```bash
git add languagetool-language-modules/tr/src/main/resources/org/languagetool/rules/tr/grammar.xml \
        languagetool-language-modules/tr/src/test/java/org/languagetool/rules/tr/TurkishPatternRuleTest.java
git commit -m "feat(tr): add starter 'deki' grammar rule and pattern rule test"
```

---

### Task 5: End-to-end verification and standalone smoke test

Confirms the whole module wires into the full LT build and Turkish actually appears at runtime.

**Files:** none modified

- [ ] **Step 1: Build the standalone jar**

Run:
```bash
cd /Users/halilugur/ZCodeProject/languagetool
mvn -q clean package -pl languagetool-standalone -am -DskipTests
```
Expected: BUILD SUCCESS, producing `languagetool-standalone/target/LanguageTool-*.jar` (or a `languagetool-standalone.jar` launcher).

- [ ] **Step 2: List languages and confirm Turkish is present**

Run (adjust the jar filename to the actual built artifact; if the standalone ships a launcher script, prefer it):
```bash
java -jar languagetool-standalone/target/LanguageTool-*.jar -h 2>&1 | grep -i "turkish\|tr" || true
```
If the standalone does not have a language-listing CLI flag, instead verify via the commandline module:
```bash
mvn -q -pl languagetool-commandline -am exec:java \
  -Dexec.mainClass=org.languagetool.commandline.Main \
  -Dexec.args="-l tr -b 'Ali'nin aracı , çok hızlı.'" 2>&1 | tail -20
```
Expected: Turkish is accepted as a language code and the command runs (matches for the comma-whitespace rule are a bonus, not required).

If neither CLI path lists languages cleanly, the definitive runtime check is a one-line Java check compiled against the built classpath — but the `mvn -pl .../tr test` already passing in Task 4 confirms the SPI loads. Treat this step as best-effort confirmation; a clean module build + passing tests is the hard gate.

- [ ] **Step 3: Run the full module test suite one final time**

Run:
```bash
mvn -q -pl languagetool-language-modules/tr test
```
Expected: BUILD SUCCESS, all tests green.

- [ ] **Step 4: Commit any verification notes (optional)**

No code changes in this task. If you tweaked example sentences or the standalone invocation, commit those. Otherwise, the foundation is complete — proceed to the execution handoff.

---

## Self-Review

**1. Spec coverage check:**
- Module skeleton + wiring (spec §Architecture): Task 1 ✓
- `Turkish.java` identity (`tr`/`TR`/maintainer/`ActivelyMaintained`): Task 3 ✓
- Apostrophe-preserving tokenizer: Task 2 ✓
- Generic rules list (all 7 from spec): Task 3 ✓
- Starter `grammar.xml` rule: Task 4 ✓
- 3 external wiring edits: Task 1 steps 4–6 ✓
- Test classes (`TurkishPatternRuleTest`, `TurkishWordTokenizerTest`): Tasks 2 & 4 ✓
- Success criteria 1–5 (module test, language discoverable, generic rules fire, starter rule works, standalone build): Tasks 1–5 ✓
- No-stubs decision (inherit `DEMO_TAGGER`): Task 3 — `Turkish.java` deliberately omits `createDefaultTagger()` override ✓

**2. Placeholder scan:** No TBD/TODO. Every step has concrete code or exact commands. The two "adjust if needed" notes (Step 4 of Task 2 for whitespace, Step 4 of Task 4 for example sentences) are real failure-recovery instructions, not placeholders — they describe the TDD iteration loop.

**3. Type/name consistency:**
- `TurkishWordTokenizer` — used identically in Task 2 (definition) and Task 3 (instantiation) ✓
- `tokenize(String)` signature consistent ✓
- `TurkishPatternRuleTest` — same name in Task 4 step 1 and the commit message ✓
- Package `org.languagetool.tokenizers.tr` and `org.languagetool.rules.tr` match the directory layout in the File Structure table ✓
- `language-tr` artifactId consistent across Task 1 pom, `all/pom.xml`, and root module registration ✓
- SPI class name `org.languagetool.language.Turkish` matches Task 3's package + class ✓

No issues found. Plan is ready.

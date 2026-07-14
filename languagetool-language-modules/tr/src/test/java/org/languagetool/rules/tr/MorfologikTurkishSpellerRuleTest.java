/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Turkish;

/**
 * Tests for {@link MorfologikTurkishSpellerRule}.
 * <p>
 * The dictionary is built from the tr_TR Hunspell dictionary via {@code unmunch},
 * which does not exhaustively expand every Turkish affix combination (Turkish is
 * highly agglutinative). Test words are therefore chosen from forms verified to
 * be present in the expanded word list, and coverage gaps on heavily-suffixed
 * forms are an expected, known limitation of this sub-project.
 */
public class MorfologikTurkishSpellerRuleTest {
  private JLanguageTool langTool;
  private MorfologikTurkishSpellerRule rule;

  @Before
  public void init() throws IOException {
    rule = new MorfologikTurkishSpellerRule(TestTools.getMessages("tr"), new Turkish(), null, Collections.emptyList());
    langTool = new JLanguageTool(new Turkish());
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    // valid Turkish words (verified present in the dict) -> no match
    assertEquals(Arrays.asList(), Arrays.asList(rule.match(langTool.getAnalyzedSentence("merhaba dünya"))));
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("araba")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("gitmek")).length);

    // obvious misspellings -> 1 match each
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("merrrrhaba")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("zzzxqqq")).length);
  }

}

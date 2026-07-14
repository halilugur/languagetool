/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 halilugur (http://www.languagetool.org)
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
package org.languagetool.tagging.tr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.TestTools;
import org.languagetool.language.Turkish;
import org.languagetool.tokenizers.tr.TurkishWordTokenizer;

public class TurkishTaggerTest {

  private TurkishTagger tagger;
  private TurkishWordTokenizer tokenizer;

  @Before
  public void setUp() {
    tagger = new TurkishTagger();
    tokenizer = new TurkishWordTokenizer();
  }

  @Test
  public void testDictionary() throws IOException {
    // Validates the binary dictionary loads and every entry is well-formed.
    TestTools.testDictionary(tagger, new Turkish());
  }

  @Test
  public void testTaggerKnownWords() throws IOException {
    // Common words -> at least the expected primary POS reading present.
    List<AnalyzedToken> evdeReadings = tagger.tag(tokenizer.tokenize("evde")).get(0).getReadings();
    assertTrue("evde should be tagged NOUN, got: " + evdeReadings,
        evdeReadings.stream().anyMatch(t -> "NOUN".equals(t.getPOSTag())));

    List<AnalyzedToken> veReadings = tagger.tag(tokenizer.tokenize("ve")).get(0).getReadings();
    assertTrue("ve should be tagged CCONJ, got: " + veReadings,
        veReadings.stream().anyMatch(t -> "CCONJ".equals(t.getPOSTag())));
  }

  @Test
  public void testTaggerUnknownWordIsNullTag() throws IOException {
    // Unknown word -> BaseTagger returns a single reading with null POS (graceful fallback).
    List<AnalyzedToken> readings = tagger.tag(tokenizer.tokenize("zzzxqqq")).get(0).getReadings();
    assertEquals(1, readings.size());
    // null POS tag for unknown words
    assertEquals(null, readings.get(0).getPOSTag());
  }

}

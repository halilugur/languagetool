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
    final List<String> tokens = wordTokenizer.tokenize("Ali\u2019nin");
    assertEquals("[Ali\u2019nin]", tokens.toString());
  }
}

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
package org.languagetool.synthesis.tr;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.languagetool.AnalyzedToken;

/**
 * Smoke test for {@link TurkishSynthesizer}: verifies the synth dict loads and
 * can produce at least one word form for a known lemma+tag.
 */
public class TurkishSynthesizerTest {

  @Test
  public void testSynthesizerLoadsAndProduces() throws IOException {
    TurkishSynthesizer synth = TurkishSynthesizer.INSTANCE;
    // "ev" (house) is a NOUN in the UD-BOUN dictionary; synthesis of lemma+tag
    // should yield at least one surface form (typically "ev" itself).
    String[] forms = synth.synthesize(new AnalyzedToken("ev", "NOUN", "ev"), "NOUN");
    assertTrue("Synthesis of ev+NOUN should return >=1 form, got: " + Arrays.toString(forms),
        forms.length >= 1);
  }

}

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

import org.languagetool.synthesis.BaseSynthesizer;

/**
 * Turkish word-form synthesizer, used to re-inflect suggestions in grammar rules.
 * <p>
 * Backed by a Morfologik dictionary built from Zemberek-generated inflected forms
 * (Apache-2.0) merged with the UD Turkish-BOUN treebank (CC BY-SA 4.0).
 */
public class TurkishSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/tr/turkish_synth.dict";
  private static final String TAGS_FILE_NAME = "/tr/turkish_synth.dict_tags.txt";

  public static final TurkishSynthesizer INSTANCE = new TurkishSynthesizer();

  private TurkishSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME, "tr");
  }

}

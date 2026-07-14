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

import java.util.Locale;

import org.languagetool.tagging.BaseTagger;

/**
 * Turkish part-of-speech tagger.
 * <p>
 * Backed by a Morfologik dictionary built from Zemberek-generated inflected forms
 * ({@code https://github.com/ahmetaa/zemberek-nlp}, Apache-2.0) merged with the
 * UD Turkish-BOUN treebank
 * ({@code https://github.com/UniversalDependencies/UD_Turkish-BOUN}, CC BY-SA 4.0),
 * yielding ~4M wordforms with broad coverage of Turkish's agglutinative morphology.
 * The tagset is Universal Dependencies UPOS (see {@code tagset.txt}).
 * <p>
 * Unknown words fall back to a null tag via {@link BaseTagger}'s graceful handling.
 */
public class TurkishTagger extends BaseTagger {

  public TurkishTagger() {
    super("/tr/turkish.dict", new Locale("tr", "TR"), false);
  }

}

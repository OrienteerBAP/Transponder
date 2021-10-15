package org.orienteer.transponder.polyglot;

import org.orienteer.transponder.IDriver;
import org.orienteer.transponder.IPolyglot;

public class DefaultPolyglot implements IPolyglot {

	@Override
	public Translation translate(String queryId, String srcLanguage, String srcDialect, String srcQuery, IDriver to) {
		return new Translation(srcLanguage, srcQuery);
	}

}

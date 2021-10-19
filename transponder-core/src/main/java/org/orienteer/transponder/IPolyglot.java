package org.orienteer.transponder;

import lombok.AllArgsConstructor;
import lombok.Value;

public interface IPolyglot {
	
	@Value
	@AllArgsConstructor
	public static class Translation {
		private String language;
		private String query;
	}
	
	public Translation translate(Class<?> ownerClass,
								 String queryId,
								 String srcLanguage,
								 String srcQuery,
								 String srcDialect,
								 String toDialect);

}

package org.orienteer.transponder;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Interface for polyglot implementations.
 * Polyglot helps to translate queries/commands to specific dialect 
 */
public interface IPolyglot {
	
	/**
	 * Container for a translation
	 */
	@Value
	@AllArgsConstructor
	public static class Translation {
		private String language;
		private String query;
	}
	
	/**
	 * Translate query/command to a specific dialect
	 * @param ownerClass class which mentions this query to be translated
	 * @param queryId id of a query: can be obtained from an {@link Query}/{@link Command}/{@link Lookup} 
	 * or generated as <b>className.methodName</b>
	 * @param srcLanguage language which was specified on an annotation
	 * @param srcQuery query which was specified on an annotation
	 * @param srcDialect dialect which was specified on an annotation
	 * @param toDialect dialect to translate to
	 * @return translation
	 */
	public Translation translate(Class<?> ownerClass,
								 String queryId,
								 String srcLanguage,
								 String srcQuery,
								 String srcDialect,
								 String toDialect);

}

package org.orienteer.transponder.polyglot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.orienteer.transponder.IPolyglot;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Default implementation of {@link IPolyglot}
 */
public class DefaultPolyglot implements IPolyglot {
	
	/**
	 * Name of a resource to be scanned
	 */
	public static final String POLYGLOT_RESOURCES = "META-INF/transponder/polyglot.properties";
	/**
	 * Suffix of property keys to be used for language overriding
	 */
	public static final String LANGUAGE_SUFFIX  = ".language";
	/**
	 * Just empty string
	 */
	public static final String EMPTY = "";
	
	private Table<String, String, Translation> dictionary = HashBasedTable.create();
	
	private Set<Integer> loadedFromCL = Collections.synchronizedSet(new HashSet<>());

	@Override
	public Translation translate(Class<?> ownerClass, String queryId, String srcLanguage, String srcQuery, String srcDialect, String toDialect) {
		loadForClassSafe(ownerClass);
		Translation translation = dictionary.get(queryId, toDialect);
		if(translation==null) {
			translation = dictionary.get(queryId, EMPTY);
			if(translation==null) {
				if(toDialect.equals(srcDialect) || Strings.isNullOrEmpty(srcDialect)) 
					translation = new Translation(srcLanguage, srcQuery);
			}
		}
		return checkLanguage(translation, srcLanguage);
	}
	
	private Translation checkLanguage(Translation translation, String defaultLanguage) {
		if(translation==null) return null;
		else if(!Strings.isNullOrEmpty(translation.getLanguage()) 
				|| Strings.isNullOrEmpty(defaultLanguage)) return translation;
		else return new Translation(defaultLanguage, translation.getQuery());
	}
	
	private void loadForClassSafe(Class<?> ownerClass) {
		try {
			loadForClass(ownerClass);
		} catch (IOException e) {
			throw new IllegalArgumentException("Can't load polyglot resources", e);
		}
	}
	
	/**
	 * Load resources for specified class
	 * @param ownerClass class to be used for resource loading
	 * @throws IOException if resources can't be loaded
	 */
	public void loadForClass(Class<?> ownerClass) throws IOException {
		tryToLoadForClassLoader(DefaultPolyglot.class.getClassLoader());
		tryToLoadForClassLoader(ownerClass.getClassLoader());
	}
	
	/**
	 * Try to load resources for specified class loader
	 * @param cl class loader to load from
	 * @throws IOException if resources can't be loaded
	 */
	public void tryToLoadForClassLoader(ClassLoader cl) throws IOException {
		if(loadedFromCL.contains(cl.hashCode())) return;
		try {
			Enumeration<URL> resources = cl.getResources(POLYGLOT_RESOURCES);
			while(resources.hasMoreElements()) {
				loadFromURL(resources.nextElement());
			}
		} finally {
			loadedFromCL.add(cl.hashCode());
		}
	}
	
	/**
	 * Load resources from specified URL
	 * @param url url to load from
	 * @throws IOException if resources can't be loaded
	 */
	public void loadFromURL(URL url) throws IOException {
		try(InputStream is = url.openStream()) {
			Properties content = new Properties();
			content.load(is);
			
			content.forEach((k, v) -> {
				String key = (String)k;
				String value = (String)v;
				if(!key.endsWith(LANGUAGE_SUFFIX)) {
					int indx = key.indexOf('.');
					String dialect;
					String queryId;
					if(indx>0) {
						dialect = key.substring(0, indx);
						queryId = key.substring(indx+1);
					} else {
						dialect = EMPTY;
						queryId = key;
					}
					String lang = content.getProperty(key+LANGUAGE_SUFFIX, EMPTY);
					dictionary.put(queryId, dialect, new Translation(lang, value));
				}
			});
		}
	}

}

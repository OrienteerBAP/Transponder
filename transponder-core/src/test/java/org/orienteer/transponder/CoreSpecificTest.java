package org.orienteer.transponder;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.IPolyglot.Translation;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

import net.bytebuddy.dynamic.DynamicType.Builder;

public class CoreSpecificTest 
{
	@Test
	public void testPolyglotLoading() {
		Transponder transponder = new Transponder(new TestDriver());
		IPolyglot polyglot = transponder.getPolyglot();
		Translation translation = polyglot.translate(CoreSpecificTest.class, "loaded1", "", "", "test", "test");
		assertEquals("text", translation.getLanguage());
		assertEquals("This was loaded", translation.getQuery());
		translation = polyglot.translate(CoreSpecificTest.class, "loaded2", "text", "Original Query", "test", "test");
		assertEquals("text", translation.getLanguage());
		assertEquals("Keep source lang", translation.getQuery());
	}
	
}

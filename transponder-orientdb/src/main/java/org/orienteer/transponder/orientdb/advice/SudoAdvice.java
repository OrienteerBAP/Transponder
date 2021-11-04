package org.orienteer.transponder.orientdb.advice;

import java.lang.reflect.Method;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBInternal;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.*;

/**
 * {@link Advice} to execute inner method under previledged rights
 *
 */
public class SudoAdvice {
	
	private SudoAdvice() {
	}

	/**
	 * Preserve current db and create db with super rights
	 * @return preserved current db
	 */
	@Advice.OnMethodEnter
	public static ODatabaseDocumentInternal before() {
		ODatabaseDocumentInternal db = ODatabaseRecordThreadLocal.instance().get();
		ODatabaseRecordThreadLocal.instance().remove();
		OrientDBInternal internal = ((ODatabaseInternal<?>)db).getSharedContext().getOrientDB();
		ODatabaseDocumentInternal newDb =  internal.openNoAuthorization(db.getName());
		newDb.activateOnCurrentThread();
		return db;
	}
	
	/**
	 * Close DB with privileged rights and make active previous db
	 * @param prevDb db which was previosly preserved
	 */
	@Advice.OnMethodExit
	public static void after(@Advice.Enter ODatabaseDocumentInternal prevDb) {
		ODatabaseSession currentDb = ODatabaseRecordThreadLocal.instance().get();
		currentDb.close();
		if(prevDb!=null) ODatabaseRecordThreadLocal.instance().set(prevDb);
	}

}

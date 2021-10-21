package org.orienteer.transponder.orientdb;

import java.io.Serializable;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

/**
 * Interface which mirror methods of {@link ODocumentWrapper} 
 */
public interface IODocumentWrapper extends Serializable {
	//CHECKSTYLE IGNORE MissingJavadocMethod FOR NEXT 35 LINES
	public void fromStream(final ODocument iDocument);
	public ODocument toStream();
	public default <R extends IODocumentWrapper> R load(final String iFetchPlan, final boolean iIgnoreCache) {
		return (R) ODriver.asWrapper(this).load(iFetchPlan, iIgnoreCache);
	}
	public default <R extends IODocumentWrapper> R reload() {
		return (R) ODriver.asWrapper(this).reload();
	}
	public default <R extends IODocumentWrapper> R reload(final String iFetchPlan) {
		return (R) ODriver.asWrapper(this).reload(iFetchPlan);
	}
	public default <R extends IODocumentWrapper> R reload(final String iFetchPlan, final boolean iIgnoreCache) {
		return (R) ODriver.asWrapper(this).reload(iFetchPlan, iIgnoreCache);
	}
	public default <R extends IODocumentWrapper> R save() {
		return (R) ODriver.asWrapper(this).save();
	}
	public default <R extends IODocumentWrapper> R save(final String iClusterName) {
		return (R) ODriver.asWrapper(this).reload(iClusterName);
	}
	public ODocument getDocument();
}
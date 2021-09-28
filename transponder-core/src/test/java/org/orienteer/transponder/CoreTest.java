package org.orienteer.transponder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.dao.ClassTestDAO;
import org.orienteer.transponder.dao.ITestDAO;

public class CoreTest 
{
	public static final Random RANDOM = new Random();
	
	@Test
	public void testInterfaceDAO() {
		ITestDAO dao = new Transponder(new TestDriver()).dao(ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoDAO(checkInt));
	}
	
	@Test
	public void testClassDAO() {
		ClassTestDAO dao = new Transponder(new TestDriver()).dao(ClassTestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
	}
	
	@Test
	public void testJointDAO() {
		ClassTestDAO dao = new Transponder(new TestDriver()).dao(ClassTestDAO.class, ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
		assertEquals(checkInt, ((ITestDAO)dao).echoDAO(checkInt));
	}
}

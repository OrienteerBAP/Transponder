package org.orienteer.transponder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

class DescribeContext {
	
	class ContextItem {
		private Class<?> typeClass;
		private String type;
		private Map<String, Runnable> postponedTillExit = new HashMap<String, Runnable>();
		private Multimap<String, Runnable> postponedTillDefined = ArrayListMultimap.create();
		
		ContextItem(Class<?> daoClass, String oClass) {
			this.typeClass = daoClass;
			this.type = oClass;
		}
	}
	
	private final Transponder transponder;
	
	private Map<Class<?>, String> describedClasses = new HashMap<Class<?>, String>();
	
	private Stack<Class<?>> processingStackIndex = new Stack<Class<?>>();
	private Stack<ContextItem> processingStack = new Stack<ContextItem>();
	
	private Multimap<String, Runnable> globalPostponedTillDefined = ArrayListMultimap.create();
	
	public DescribeContext(Transponder transponder) {
		this.transponder = transponder;
	}
	

	public void entering(Class<?> clazz, String type) {
		if(processingStackIndex.contains(clazz)) throw new IllegalStateException("Class "+clazz.getName()+" is already in stack. Stop infinite loop.");
		processingStackIndex.push(clazz);
		processingStack.push(new ContextItem(clazz, type));
	}
	
	public void exiting(Class<?> clazz, String type) {
		Class<?> exiting = processingStackIndex.pop();
		if(!clazz.equals(exiting)) throw new IllegalStateException("Exiting from wrong execution: expected "+clazz.getName()+" but in a stack "+exiting.getName());
		ContextItem last = processingStack.pop();
		if(!type.equals(last.type))  throw new IllegalStateException("Exiting from wrong execution: expected "+type+" but in a stack "+last.type);
		for (Runnable postponed : last.postponedTillExit.values()) {
			postponed.run();
		}
		describedClasses.put(clazz, type);
		
		Multimap<String, Runnable> mergeTo = processingStack.empty()?globalPostponedTillDefined:processingStack.lastElement().postponedTillDefined;
		Iterator<Map.Entry<String, Collection<Runnable>>> it = last.postponedTillDefined.asMap().entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, Collection<Runnable>> entry = it.next();
			if(wasDescribed(entry.getKey())) {
				for (Runnable runnable : entry.getValue()) {
					runnable.run();
				}
				it.remove();
			} else {
				Collection<Runnable> mergeToValue = mergeTo.get(entry.getKey());
				if(mergeToValue!=null) mergeToValue.addAll(entry.getValue());
				else mergeTo.putAll(entry.getKey(), entry.getValue());
			}
		}
		
	}
	
	public boolean inStack(Class<?> clazz) {
		return processingStackIndex.contains(clazz);
	}
	
	public boolean wasDescribed(Class<?> clazz) {
		return describedClasses.containsKey(clazz);
	}
	
	public boolean wasDescribed(String oClass) {
		return describedClasses.containsValue(oClass);
	}
	
	public String getType(Class<?> clazz) {
		return describedClasses.get(clazz);
	}
	
	public String getTypeFromStack(Class<?> clazz) {
		int indx = processingStackIndex.indexOf(clazz);
		return indx>=0?processingStack.get(indx).type:null;
	}
	
	public String resolveType(Class<?> clazz, Supplier<String> supplier) {
		String ret = getType(clazz);
		if(Strings.isNullOrEmpty(ret)) ret = getTypeFromStack(clazz);
		return !Strings.isNullOrEmpty(ret) ? ret : supplier.get();
	}
	
	public String resolveOrDescribeTypeClass(Class<?> clazz) {
		if(clazz==null) return null;
		String ret = getType(clazz);
		if(Strings.isNullOrEmpty(ret)) ret = getTypeFromStack(clazz);
		return !Strings.isNullOrEmpty(ret) ? ret : transponder.describe(clazz, this);
	}
	
	public boolean isPropertyCreationScheduled(String propertyName) {
		return processingStack.lastElement().postponedTillExit.containsKey(propertyName);
	}
	
	public void postponeTillExit(String propertyName, Runnable supplier) {
		processingStack.lastElement().postponedTillExit.put(propertyName, supplier);
	}
	
	public void postponeTillDefined(String linkedClass, Runnable supplier) {
		processingStack.lastElement().postponedTillDefined.put(linkedClass, supplier);
	}
	
	public void close(boolean restrictDependencies) {
		if(processingStackIndex.size()>0) throw new IllegalStateException("Can't close context because stack is not null");
		Collection<Runnable> remaining = globalPostponedTillDefined.values();
		if(restrictDependencies && remaining.size()>0) throw new IllegalStateException("There are unsitisfied dependencies");
		remaining.forEach(Runnable::run);
	}
	
	public String getCurrentType() {
		return processingStack.peek().type;
	}

}

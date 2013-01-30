package soot.jimple.infoflow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
/**
 * A list of methods is passed which contains signatures of methods which taint their base objects if they are called with a tainted parameter
 * When a base object is tainted, all return values are tainted, too.
 * 
 * @author Christian
 *
 */
public class EasyTaintWrapper implements ITaintPropagationWrapper {
	private HashMap<String, List<String>> classList;

	public EasyTaintWrapper(HashMap<String, List<String>> l){
		classList = l;
	}
	
	public EasyTaintWrapper(File f) throws IOException{
		BufferedReader reader = null;
		try{
			FileReader freader = new FileReader(f);
			reader = new BufferedReader(freader);
			String line = reader.readLine();
			SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
			List<String> methodList = new LinkedList<String>();
			while(line != null){
				if (!line.isEmpty() && !line.startsWith("%"))
					methodList.add(line);
				line = reader.readLine();
			}
			classList = parser.parseClassNames(methodList, true);
		}
		finally {
			if (reader != null)
				reader.close();
		}
	}
	
	//TODO: only classes from jdk etc
	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		if(classList.containsKey(c.getName())){
			return true;
		}
		if(!c.isInterface()){
			List<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOf(c);
			for(SootClass sclass : superclasses){
				if(classList.containsKey(sclass.getName()))
					return true;
			}
		}
		for(String interfaceString : classList.keySet()){
			if(c.implementsInterface(interfaceString))
				return true;
		}
		
		return false;
	}

	@Override
	public List<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		List<Value> taints = new ArrayList<Value>();
		
		//if param is tainted && classList contains classname && if list. contains signature of method -> add propagation
		if(taintedparam >= 0){
			SootMethod method = stmt.getInvokeExpr().getMethod();
			List<String> methodList = getMethodsForClass(method.getDeclaringClass());
		
			if(methodList.contains(method.getSubSignature())){
				// If we call a method on an instance, this instance is assumed to be tainted
				if(stmt.getInvokeExprBox().getValue() instanceof InstanceInvokeExpr) {
					taints.add(((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase());
					
					// If make sure to also taint the left side of an assignment
					// if the object just got tainted 
					if(stmt instanceof JAssignStmt)
						taints.add(((JAssignStmt)stmt).getLeftOp());
				}
				
				/* handled further down anyway (SA)
				else if(stmt.getInvokeExprBox().getValue() instanceof StaticInvokeExpr)
					if(stmt instanceof JAssignStmt){
						return ((JAssignStmt)stmt).getLeftOp();
					}
				*/
			}
		}
		
		// If the base object is tainted, all calls to its methods always return
		// tainted values
		if (taintedBase != null)
			if(stmt instanceof JAssignStmt)
				taints.add(((JAssignStmt)stmt).getLeftOp());
		
		return taints;
	}
	
	public List<String> getMethodsForClass(SootClass c){
		List<String> methodList = new LinkedList<String>();
		if(classList.containsKey(c.getName())){
			methodList.addAll(classList.get(c.getName()));
		}
		if(!c.isInterface()){
			List<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOf(c);
			for(SootClass sclass : superclasses){
				if(classList.containsKey(sclass.getName()))
					methodList.addAll(classList.get(sclass.getName()));
			}
		}

		for(String interfaceString : classList.keySet()){
			if(c.implementsInterface(interfaceString))
				methodList.addAll(classList.get(interfaceString));
		}
		return methodList;
	}

}
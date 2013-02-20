package soot.jimple.infoflow.data;

import java.util.HashMap;
import java.util.Stack;

import soot.EquivalentValue;
import soot.Local;

public class Abstraction {
	private final AccessPath accessPath;
	private final EquivalentValue source;
	//only used for backward-search to find matching call:
	private Stack<HashMap<Integer, Local>> originalCallArgs;
	

	public Abstraction(EquivalentValue taint, EquivalentValue src){
		source = src;
		accessPath = new AccessPath(taint);
	}
	
	protected Abstraction(EquivalentValue taint, EquivalentValue src, boolean fieldtainted){
		source = src;
		accessPath = new AccessPath(taint, fieldtainted);	
	}
	
	//TODO: make private and change AwP
	protected Abstraction(AccessPath p, EquivalentValue src){
		source = src;
		accessPath = p;
	}
	
	
	public Abstraction deriveNewAbstraction(AccessPath p){
		Abstraction a = new Abstraction(p, source);
		a.originalCallArgs = originalCallArgs;
		return a;
	}
	
	public Abstraction deriveNewAbstraction(EquivalentValue taint, boolean fieldtainted){
		Abstraction a = new Abstraction(new AccessPath(taint, fieldtainted), source);
		a.originalCallArgs = originalCallArgs;
		return a;
	}
	
	public EquivalentValue getSource() {
		return source;
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Abstraction other = (Abstraction) obj;
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.equals(other.accessPath))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		if(accessPath != null && source != null){
			return accessPath.toString() + " /source: "+ source.toString();
		}
		if(accessPath != null){
			return accessPath.toString();
		}
		return "Abstraction (null)";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
	
	public HashMap<Integer,Local> getcurrentArgs(){
		if(!originalCallArgs.isEmpty()){
			return originalCallArgs.peek();
		}
		return null;
	}
	
	public void popCurrentCallArgs(){
		//this is possible since we start at an entryPoint and might go back in control flow
		if(!originalCallArgs.isEmpty()){
			originalCallArgs.pop();
		}
	}
	
	public void addCurrentCallArgs(HashMap<Integer, Local> callArgs){
		if(callArgs.containsKey(null)){
			System.out.println("alarm!");
		}
		
		if(originalCallArgs == null)
			originalCallArgs = new Stack<HashMap<Integer, Local>>();
		originalCallArgs.push(callArgs);
	}
	
}

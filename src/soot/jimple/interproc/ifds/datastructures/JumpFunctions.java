package soot.jimple.interproc.ifds.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.jimple.interproc.ifds.EdgeFunction;
import soot.jimple.interproc.ifds.edgefunc.AllTop;
import soot.jimple.interproc.ifds.solver.PathEdge;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

/**
 * The IDE algorithm uses a list of jump functions. Instead of a list, we use a set of three
 * maps that are kept in sync. This allows for efficient indexing: the algorithm accesses
 * elements from the list through three different indices.
 */
public class JumpFunctions<N,D,L> {
	
	//mapping from target node and value to a list of all source values and associated functions
	//where the list is implemented as a mapping from the source value to the function
	//we exclude empty default functions 
	protected Table<N,D,Map<D,EdgeFunction<L>>> nonEmptyReverseLookup = HashBasedTable.create();
	
	//mapping from source value and target node to a list of all target values and associated functions
	//where the list is implemented as a mapping from the source value to the function
	//we exclude empty default functions 
	protected Table<D,N,Map<D,EdgeFunction<L>>> nonEmptyForwardLookup = HashBasedTable.create();

	//a mapping from target node to a list of triples consisting of source value,
	//target value and associated function; the triple is implemented by a table
	//we exclude empty default functions 
	protected Map<N,Table<D,D,EdgeFunction<L>>> nonEmptyLookupByTargetNode = new HashMap<N,Table<D,D,EdgeFunction<L>>>();

	private final AllTop<L> allTop;
	
	public JumpFunctions(AllTop<L> allTop) {
		this.allTop = allTop;
	}

	/**
	 * Records a jump function. The source statement is implicit.
	 * @see PathEdge
	 */
	public void addFunction(D sourceVal, N target, D targetVal, EdgeFunction<L> function) {
		assert sourceVal!=null;
		assert target!=null;
		assert targetVal!=null;
		assert function!=null;
		
		//we do not store the default function (all-top)
		if(function.equalTo(allTop)) return;
		
		Map<D,EdgeFunction<L>> sourceValToFunc = nonEmptyReverseLookup.get(target, targetVal);
		if(sourceValToFunc==null) {
			sourceValToFunc = new HashMap<D,EdgeFunction<L>>();
			nonEmptyReverseLookup.put(target,targetVal,sourceValToFunc);
		}
		sourceValToFunc.put(sourceVal, function);
		
		Map<D, EdgeFunction<L>> targetValToFunc = nonEmptyForwardLookup.get(sourceVal, target);
		if(targetValToFunc==null) {
			targetValToFunc = new HashMap<D,EdgeFunction<L>>();
			nonEmptyForwardLookup.put(sourceVal,target,targetValToFunc);
		}
		targetValToFunc.put(targetVal, function);
		
		Table<D,D,EdgeFunction<L>> table = nonEmptyLookupByTargetNode.get(target);
		if(table==null) {
			table = HashBasedTable.create();
			nonEmptyLookupByTargetNode.put(target,table);
		}
		table.put(sourceVal, targetVal, function);
	}
	
	/**
     * Returns, for a given target statement and value all associated
     * source values, and for each the associated edge function.
     * The return value is a mapping from source value to function.
	 */
	public Map<D,EdgeFunction<L>> reverseLookup(N target, D targetVal) {
		assert target!=null;
		assert targetVal!=null;
		Map<D,EdgeFunction<L>> res = nonEmptyReverseLookup.get(target,targetVal);
		if(res==null) return Collections.emptyMap();
		return res;
	}
	
	/**
	 * Returns, for a given source value and target statement all
	 * associated target values, and for each the associated edge function. 
     * The return value is a mapping from target value to function.
	 */
	public Map<D,EdgeFunction<L>> forwardLookup(D sourceVal, N target) {
		assert sourceVal!=null;
		assert target!=null;
		Map<D, EdgeFunction<L>> res = nonEmptyForwardLookup.get(sourceVal, target);
		if(res==null) return Collections.emptyMap();
		return res;
	}
	
	/**
	 * Returns for a given target statement all jump function records with this target.
	 * The return value is a set of records of the form (sourceVal,targetVal,edgeFunction).
	 */
	public Set<Cell<D,D,EdgeFunction<L>>> lookupByTarget(N target) {
		assert target!=null;
		Table<D, D, EdgeFunction<L>> table = nonEmptyLookupByTargetNode.get(target);
		if(table==null) return Collections.emptySet();
		Set<Cell<D, D, EdgeFunction<L>>> res = table.cellSet();
		if(res==null) return Collections.emptySet();
		return res;
	}
	
}

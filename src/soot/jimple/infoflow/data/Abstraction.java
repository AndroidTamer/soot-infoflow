/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.data;


import heros.solver.PathTrackingIFDSSolver.LinkedNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.NullType;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.heros.InfoflowCFG.UnitContainer;
import soot.jimple.internal.JimpleLocal;

import com.google.common.collect.Sets;
/**
 * the abstraction class contains all information that is necessary to track the taint.
 *
 */
public class Abstraction implements Cloneable, LinkedNode<Abstraction> {

	private static Abstraction zeroValue = null;
    private static final Logger logger = LoggerFactory.getLogger(Abstraction.class);
    
	/**
	 * Class representing a source value together with the statement that created it
	 * 
	 * @author Steven Arzt
	 */
	public class SourceContext implements Cloneable {
		private final Value value;
		private final Stmt stmt;
		
		public SourceContext(Value value, Stmt stmt) {
			this.value = value;
			this.stmt = stmt;
		}
		
		public Value getValue() {
			return this.value;
		}
		
		public Stmt getStmt() {
			return this.stmt;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !(obj instanceof SourceContext))
				return false;
			SourceContext other = (SourceContext) obj;
			if (stmt == null) {
				if (other.stmt != null)
					return false;
			} else if (!stmt.equals(other.stmt))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		
		@Override
		public SourceContext clone() {
			SourceContext sc = new SourceContext(value, stmt);
			assert sc.equals(this);
			return sc;
		}

		@Override
		public String toString() {
			return value.toString();
		}
	}
	
	public class SourceContextAndPath extends SourceContext implements Cloneable {
		private final List<Stmt> path = new LinkedList<Stmt>();
		
		public SourceContextAndPath(Value value, Stmt stmt) {
			super(value, stmt);
			path.add(stmt);
		}
		
		public List<Stmt> getPath() {
			return Collections.unmodifiableList(this.path);
		}
		
		public SourceContextAndPath extendPath(Stmt s) {
			SourceContextAndPath scap = clone();
			scap.path.add(s);
			return scap;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (other == null || !(other instanceof SourceContextAndPath))
				return false;
			if (!super.equals(other))
				return false;
			//SourceContextAndPath scap = (SourceContextAndPath) other;
			return true; //this.path.equals(scap.path);
		}
		
		@Override
		public int hashCode() {
			return 31 * super.hashCode(); // + 7 * path.hashCode();
		}
		
		@Override
		public SourceContextAndPath clone() {
			SourceContextAndPath scap = new SourceContextAndPath(getValue(), getStmt());
			scap.path.clear();
			scap.path.addAll(this.path);
			assert scap.equals(this);
			return scap;
		}
		
		@Override
		public String toString() {
			return super.toString() + "\n\ton Path: " + path;
		}	
	}
	
	private class Neighbor{
		private Abstraction abstraction;
		
		public Neighbor(Abstraction abstraction) {
			this.abstraction = abstraction;
		}
		
		@Override
		public int hashCode() {
			return abstraction.hashCode()
					+ 31 * (abstraction.predecessor == null ? 0 : abstraction.predecessor.hashCode())
					+ 31 * (abstraction.currentStmt == null ? 0 : abstraction.currentStmt.hashCode());
		}
		
		@Override
		public boolean equals(Object other) {
			if (!abstraction.equals(other))
				return false;
			Abstraction abs = (Abstraction) other;
			return abs.predecessor == abstraction.predecessor
					&& abs.currentStmt == abstraction.currentStmt;
		}
	}

	/**
	 * the access path contains the currently tainted variable or field
	 */
	private final AccessPath accessPath;
	
	private Abstraction predecessor = null;
	private Set<Neighbor> neighbors = null;
	private Stmt currentStmt = null;
	
	private SourceContext sourceContext = null;

	// only used in path generation
	private Set<Abstraction> successors = null;
	private Set<SourceContextAndPath> pathCache = null;
	private Set<Abstraction> roots = null;
	
	/**
	 * Unit/Stmt which activates the taint when the abstraction passes it
	 */
	private Unit activationUnit = null;
	/**
	 * active abstraction is tainted value,
	 * inactive abstraction is an alias to a tainted value that
	 * might be activated in the future
	 */
	private boolean isActive = true;
	/**
	 * taint is thrown by an exception (is set to false when it reaches the catch-Stmt)
	 */
	private boolean exceptionThrown = false;
	private int hashCode = 0;

	/**
	 * The postdominators we need to pass in order to leave the current conditional
	 * branch. Do not use the synchronized Stack class here to avoid deadlocks.
	 */
	private final List<UnitContainer> postdominators = new ArrayList<UnitContainer>();
	
	private final boolean flowSensitiveAliasing;
	
	public Abstraction(Value taint, Value currentVal, Stmt currentStmt,
			boolean exceptionThrown, boolean isActive, Unit activationUnit,
			boolean flowSensitiveAliasing){
		this.sourceContext = new SourceContext(currentVal, currentStmt);
		this.accessPath = new AccessPath(taint);
		
		if (flowSensitiveAliasing)
			this.activationUnit = activationUnit;
		else
			this.activationUnit = null;
		
		this.exceptionThrown = exceptionThrown;
		
		if (flowSensitiveAliasing)
			this.isActive = isActive;
		else
			this.isActive = true;
		
		this.flowSensitiveAliasing = flowSensitiveAliasing;
	}

	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path. -> only used by AbstractionWithPath
	 * @param p The access path for the new abstraction
	 * @param original The original abstraction to copy
	 */
	protected Abstraction(AccessPath p, Abstraction original){
		if (original == null) {
			sourceContext = null;
			exceptionThrown = false;
			activationUnit = null;
			flowSensitiveAliasing = true;
		}
		else {
			sourceContext = original.sourceContext;
			exceptionThrown = original.exceptionThrown;
			activationUnit = original.activationUnit;
			
			isActive = original.isActive;
			
			postdominators.addAll(original.postdominators);
			assert this.postdominators.equals(original.postdominators);
			
			flowSensitiveAliasing = original.flowSensitiveAliasing;
			assert flowSensitiveAliasing || this.activationUnit == null;
			assert this.isActive || flowSensitiveAliasing;
		}
		accessPath = p;
	}
	
	public final Abstraction deriveInactiveAbstraction(){
		if (!flowSensitiveAliasing)
			return this;
		
		// If this abstraction is already inactive, we keep it
		if (!this.isActive)
			return this;

		Abstraction a = deriveNewAbstractionMutable(accessPath, null);
		a.isActive = false;
		a.postdominators.clear();
		return a;
	}

	public Abstraction deriveNewAbstraction(AccessPath p, Stmt currentStmt){
		if (this.accessPath.equals(p) && this.currentStmt == currentStmt)
			return this;
		return deriveNewAbstractionMutable(p, currentStmt);
	}
	
	private Abstraction deriveNewAbstractionMutable(AccessPath p, Stmt currentStmt){
		if (this.accessPath.equals(p) && this.currentStmt == currentStmt)
			return clone();
		
		Abstraction abs = new Abstraction(p, this);
		abs.predecessor = this;
		abs.currentStmt = currentStmt;
		
		if (!abs.getAccessPath().isEmpty())
			abs.postdominators.clear();
		
		abs.sourceContext = null;
		return abs;
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
		return this.deriveNewAbstraction(taint, false, activationUnit);
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit){
		assert !this.getAccessPath().isEmpty();

		Abstraction a;
		SootField[] orgFields = accessPath.getFields();
		SootField[] fields = new SootField[cutFirstField ? orgFields.length - 1 : orgFields.length];
		for (int i = cutFirstField ? 1 : 0; i < orgFields.length; i++)
			fields[cutFirstField ? i - 1 : i] = orgFields[i];
		AccessPath newAP = new AccessPath(taint, fields);
		
		a = deriveNewAbstractionMutable(newAP, (Stmt) newActUnit);
		if (flowSensitiveAliasing && isActive) {
			assert newActUnit != null;
			if (!this.getAccessPath().isEmpty())
				a.activationUnit = newActUnit;
		}
		return a;
	}

	/**
	 * Derives a new abstraction that models the current local being thrown as
	 * an exception
	 * @param throwStmt The statement at which the exception was thrown
	 * @return The newly derived abstraction
	 */
	public final Abstraction deriveNewAbstractionOnThrow(Stmt throwStmt){
		assert !this.exceptionThrown;
		Abstraction abs = cloneWithPredecessor(throwStmt);
		
		abs.sourceContext = null;
		abs.exceptionThrown = true;
		return abs;
	}
	
	/**
	 * Derives a new abstraction that models the current local being caught as
	 * an exception
	 * @param taint The value in which the tainted exception is stored
	 * @return The newly derived abstraction
	 */
	public final Abstraction deriveNewAbstractionOnCatch(Value taint, Unit newActivationUnit){
		assert this.exceptionThrown;
		Abstraction abs = deriveNewAbstractionMutable(new AccessPath(taint), (Stmt) newActivationUnit);
		abs.exceptionThrown = false;
		
		if(flowSensitiveAliasing && isActive) {
			assert newActivationUnit != null;
			abs.activationUnit = newActivationUnit;
		}
		return abs;
	}
		
	/**
	 * Gets the path of statements from the source to the current statement
	 * with which this abstraction is associated. If this path is ambiguous,
	 * a single path is selected randomly.
	 * @return The path from the source to the current statement
	 */
	public Set<SourceContextAndPath> getPaths() {
		Runtime.getRuntime().gc();
		return getPaths(true);
	}
	
	/**
	 * Gets the path of statements from the source to the current statement
	 * with which this abstraction is associated. If this path is ambiguous,
	 * a single path is selected randomly.
	 * @return The path from the source to the current statement
	 */
	public Set<SourceContextAndPath> getSources() {
		Runtime.getRuntime().gc();
		return getPaths(false);
	}
	
	private Abstraction sinkAbs = null;
	
	/**
	 * Gets the path of statements from the source to the current statement
	 * with which this abstraction is associated. If this path is ambiguous,
	 * a single path is selected randomly.
	 * @return The path from the source to the current statement
	 */
	private Set<SourceContextAndPath> getPaths(boolean reconstructPaths) {
		if (pathCache != null)
			return Collections.unmodifiableSet(pathCache);
		
		// Phase 1: Collect the graph roots
		logger.info("Running path collection phase 1...");

		Set<Abstraction> roots = Sets.newIdentityHashSet();
		List<Abstraction> workList = new LinkedList<Abstraction>();
		Set<Abstraction> iterativeWorklist = Sets.newIdentityHashSet();
		
		// Add this abstraction to the work list
		workList.add(this);
		this.successors = Sets.newIdentityHashSet();

		// Add all neighbors of this abstraction to the work list
		if (this.neighbors != null)
			for (Neighbor nb : this.neighbors) {
				workList.add(nb.abstraction);
				nb.abstraction.successors = this.successors;
			}

		while (!workList.isEmpty()) {
			Abstraction curAbs = workList.remove(0);
			curAbs.roots = roots;
			
			// Since we walk up from the bottom, a predecessor must already
			// have a successor list 
			assert !reconstructPaths || curAbs.successors != null;
			// We must either have a source or a predecessor
			assert !(curAbs.sourceContext != null && curAbs.predecessor != null);
			assert !(curAbs.sourceContext == null && curAbs.predecessor == null);
			
			if (curAbs.sourceContext != null) {
				if (roots.add(curAbs)) {
					// Construct the path root
					SourceContextAndPath sourceAndPath = new SourceContextAndPath
							(curAbs.sourceContext.value, curAbs.sourceContext.stmt);
					if (curAbs.pathCache == null)
						curAbs.pathCache = Collections.singleton(sourceAndPath);
					else
						assert curAbs.pathCache.contains(sourceAndPath);
				}
				
				// Sources may not have neighbors
				assert curAbs.neighbors == null;
			}
			else if (curAbs.predecessor != null) {
				// Set the current abstraction as a successor of the current
				// predecessor
				if (curAbs.predecessor.successors == null)
					curAbs.predecessor.successors = Sets.newIdentityHashSet();
				if (curAbs.predecessor.successors.add(curAbs) && curAbs.predecessor.sinkAbs != this) {
					// Schedule the predecessor
					curAbs.predecessor.sinkAbs = this;
					workList.add(curAbs.predecessor);
					if (curAbs.predecessor.pathCache != null) {
						iterativeWorklist.add(curAbs.predecessor);
						roots.addAll(curAbs.predecessor.roots);
					}
				}

				// Schedule the predecessor's neighbors
				if (curAbs.predecessor.neighbors != null)
					for (Neighbor nb : curAbs.predecessor.neighbors) {
						boolean addIt = false;
						if (nb.abstraction.successors == null) {
							nb.abstraction.successors = curAbs.predecessor.successors;
							addIt = true;
						}
						else
							addIt = nb.abstraction.successors.add(curAbs);
							
						if (addIt) {
							workList.add(nb.abstraction);
							if (nb.abstraction.pathCache != null) {
								iterativeWorklist.add(nb.abstraction);
								roots.addAll(nb.abstraction.roots);
							}
						}
					}
			}
			else
				throw new RuntimeException("Invalid abstraction detected");
		}
		
		logger.info("Phase 1 completed.");
		
		// If we have not found any roots, we are in trouble
		assert !roots.isEmpty();
		
		// If we don't need the paths, just return the roots
		if (!reconstructPaths) {
			Set<SourceContextAndPath> res = new HashSet<SourceContextAndPath>();
			for (Abstraction abs : roots)
				res.add(new SourceContextAndPath(abs.sourceContext.value, abs.sourceContext.stmt));
			return res;
		}
		
		// Make sure that nothing wonky is going on
		for (Abstraction abs : iterativeWorklist)
			assert abs.pathCache != null;
		
		// Phase 2: Construct the paths
		logger.info("Running path collection phase 2 from {} roots...", roots.size());
		
		// If we perform an incremental build, we must add the nodes that have
		// received new children
		if (!iterativeWorklist.isEmpty()) {
			workList.addAll(iterativeWorklist);
			logger.info("Running in incremental mode with {} nodes", iterativeWorklist.size());
		}
		else {
			workList.addAll(roots);
			logger.info("Running in main mode");
		}
		
		if (workList.isEmpty())
			logger.warn("Path reconstruction work list is empty");
		
		while (!workList.isEmpty()) {
			Abstraction curAbs = workList.remove(0);
			assert curAbs.pathCache != null;
			
			// Merge the path cache with all neighbors
			if (curAbs.neighbors != null)
				for (Neighbor neighbor : curAbs.neighbors) {
//					assert neighbor.abstraction.successors == curAbs.successors;
					if (neighbor.abstraction.pathCache != null)
						curAbs.pathCache.addAll(neighbor.abstraction.pathCache);
					neighbor.abstraction.pathCache = curAbs.pathCache;
				}

			// Shortcut: If we have found an equivalent abstraction, we
			// copy its path cache
			if (this.pathCache == null
					&& curAbs.equals(this)
					&& curAbs.currentStmt == this.currentStmt
					&& curAbs.predecessor == this.predecessor)
				this.pathCache = curAbs.pathCache;

			// Propagate the path down
			if (curAbs.successors != null)
				for (Abstraction successor : curAbs.successors) {
					if (successor.pathCache == null)
						successor.pathCache = new HashSet<SourceContextAndPath>();
					
//					System.out.println(System.identityHashCode(curAbs) + "->" + System.identityHashCode(successor));
					
					for (SourceContextAndPath curScap : curAbs.pathCache) {
						SourceContextAndPath extendedPath = successor.currentStmt == null
								? curScap : curScap.extendPath(successor.currentStmt);
						if (successor.pathCache.add(extendedPath))
							workList.add(successor);
					}
				}
		}
		
//		System.out.println("---------");
		logger.info("Path construction done");
		
		assert pathCache != null;
		return Collections.unmodifiableSet(pathCache);
	}
	
	public boolean isAbstractionActive(){
		return isActive;
	}
	
	@Override
	public String toString(){
		return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString()) + ">>";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
	
	public Unit getActivationUnit(){
		return activationUnit;
	}
	
	public Abstraction getActiveCopy(){
		assert !this.isActive;
		
		Abstraction a = cloneWithPredecessor(null);
		a.sourceContext = null;
		a.isActive = true;
		// do not kill the original activation point since we might return into
		// a caller, find a new alias there and then need to know where both
		// aliases originally became active.
//		a.activationUnit = null;
		return a;
	}
	
	/**
	 * Gets whether this value has been thrown as an exception
	 * @return True if this value has been thrown as an exception, otherwise
	 * false
	 */
	public boolean getExceptionThrown() {
		return this.exceptionThrown;
	}
	
	public final Abstraction deriveConditionalAbstractionEnter(UnitContainer postdom,
			Stmt conditionalUnit) {
		if (postdominators.contains(postdom))
			return this;
		
		Abstraction abs = deriveNewAbstractionMutable
				(AccessPath.getEmptyAccessPath(), conditionalUnit);
		// TODO
//		abs.activationUnit = null;
		abs.postdominators.add(0, postdom);
		abs.isActive = true;
		return abs;
	}
	
	public final Abstraction deriveConditionalAbstractionCall(Unit conditionalCallSite) {
		assert conditionalCallSite != null;
		
		Abstraction abs = deriveNewAbstractionMutable
				(AccessPath.getEmptyAccessPath(), (Stmt) conditionalCallSite);
		abs.isActive = true;
		abs.activationUnit = conditionalCallSite;
		
		// Postdominators are only kept intraprocedurally in order to not
		// mess up the summary functions with caller-side information
		abs.postdominators.clear();

		return abs;
	}
	
	public final Abstraction dropTopPostdominator() {
		if (postdominators.isEmpty())
			return this;
		
		Abstraction abs = clone();
		abs.sourceContext = null;
		abs.postdominators.remove(0);
		return abs;
	}
	
	public UnitContainer getTopPostdominator() {
		if (this.postdominators.isEmpty())
			return null;
		return this.postdominators.get(0);
	}
	
	public boolean isTopPostdominator(Unit u) {
		UnitContainer uc = getTopPostdominator();
		if (uc == null)
			return false;
		return uc.getUnit() == u;
	}

	public boolean isTopPostdominator(SootMethod sm) {
		UnitContainer uc = getTopPostdominator();
		if (uc == null)
			return false;
		return uc.getMethod() == sm;
	}
	
	@Override
	public Abstraction clone() {
		return cloneWithPredecessor(null);
	}
	
	private Abstraction cloneWithPredecessor(Stmt predStmt) {
		Abstraction abs = new Abstraction(accessPath, this);
		abs.predecessor = this;
		abs.currentStmt = predStmt;
		abs.neighbors = null;
		
		assert abs.equals(this);
		return abs;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		if (obj == null || !(obj instanceof Abstraction))
			return false;
		Abstraction other = (Abstraction) obj;
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.equals(other.accessPath))
			return false;
		
		return localEquals(other);
	}
	
	/**
	 * Checks whether this object locally equals the given object, i.e. the both
	 * are equal modulo the access path
	 * @param other The object to compare this object with
	 * @return True if this object is locally equal to the given one, otherwise
	 * false
	 */
	private boolean localEquals(Abstraction other) {
		// deliberately ignore prevAbs
		if (sourceContext == null) {
			if (other.sourceContext != null)
				return false;
		} else if (!sourceContext.equals(other.sourceContext))
			return false;
		if (activationUnit == null) {
			if (other.activationUnit != null)
				return false;
		} else if (!activationUnit.equals(other.activationUnit))
			return false;
		if (this.exceptionThrown != other.exceptionThrown)
			return false;
		if(this.isActive != other.isActive)
			return false;
		if(!this.postdominators.equals(other.postdominators))
			return false;
		if(this.flowSensitiveAliasing != other.flowSensitiveAliasing)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		if (this.hashCode != 0)
			return hashCode;

		final int prime = 31;
		synchronized (this) { 
			this.hashCode = 1;
	
			// deliberately ignore prevAbs
			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
			this.hashCode = prime * this.hashCode + ((activationUnit == null) ? 0 : activationUnit.hashCode());
			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + postdominators.hashCode();
			this.hashCode = prime * this.hashCode + (flowSensitiveAliasing ? 1231 : 1237);
			return hashCode;
		}
	}
	
	/**
	 * Checks whether this abstraction entails the given abstraction, i.e. this
	 * taint also taints everything that is tainted by the given taint.
	 * @param other The other taint abstraction
	 * @return True if this object at least taints everything that is also tainted
	 * by the given object
	 */
	public boolean entails(Abstraction other) {
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.entails(other.accessPath))
			return false;
		return localEquals(other);
	}

	/**
	 * Gets the context of the taint, i.e. the statement and value of the source
	 * @return The statement and value of the source
	 */
	public SourceContext getSourceContext() {
		return sourceContext;
	}
	
	@Override
	public void addNeighbor(Abstraction originalAbstraction) {
		assert this != zeroValue;
		assert originalAbstraction.equals(this);
//		assert originalAbstraction.neighbors == null;
		
		// We should not register ourselves as a neighbor
		if (originalAbstraction == this)
			return;
		if (this.predecessor == originalAbstraction.predecessor
				&& this.currentStmt == originalAbstraction.currentStmt)
			return;
		
		synchronized (this) {
			if (neighbors == null) {
				neighbors = Sets.newHashSet();
				neighbors.add(new Neighbor(this));
			}
			
			this.neighbors.add(new Neighbor(originalAbstraction));
		}
		synchronized (originalAbstraction) {
			originalAbstraction.neighbors = this.neighbors;
		}
	}
		
	public static Abstraction getZeroAbstraction(boolean flowSensitiveAliasing) {
		if (zeroValue == null)
			zeroValue = new Abstraction(new JimpleLocal("zero", NullType.v()), null,
					null, false, true, null, flowSensitiveAliasing);
		return zeroValue;
	}
	
}

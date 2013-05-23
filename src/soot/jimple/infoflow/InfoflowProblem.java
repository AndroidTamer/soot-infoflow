package soot.jimple.infoflow;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.PathEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;
import soot.jimple.infoflow.heros.InfoflowSolver;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.SourceSinkManager;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.icfg.JimpleBasedBiDiICFG;

public class InfoflowProblem extends AbstractInfoflowProblem {

	InfoflowSolver bSolver;
	private final static boolean DEBUG = false;
	final SourceSinkManager sourceSinkManager;
	Abstraction zeroValue = null;
	
	/**
	 * Computes the taints produced by a taint wrapper object
	 * @param iStmt The call statement the taint wrapper shall check for well-
	 * known methods that introduce black-box taint propagation 
	 * @param callArgs The actual parameters with which the method in invoked
	 * @param source The taint source
	 * @return The taints computed by the wrapper
	 */
	private Set<Abstraction> computeWrapperTaints
			(final Stmt iStmt,
			final List<Value> callArgs,
			Abstraction source) {
		Set<Abstraction> res = new HashSet<Abstraction>();
		if(taintWrapper == null || !taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass()))
			return Collections.emptySet();
		
		int taintedPos = -1;
		for(int i=0; i< callArgs.size(); i++){
			if(source.getAccessPath().isLocal() && callArgs.get(i).equals(source.getAccessPath().getPlainValue())){
				taintedPos = i;
				break;
			}
		}
		Value taintedBase = null;
		if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
			if(iiExpr.getBase().equals(source.getAccessPath().getPlainValue())){
				if(source.getAccessPath().isLocal()){
					taintedBase = iiExpr.getBase();
				}
				else if(source.getAccessPath().isInstanceFieldRef()){
					// The taint refers to the actual type of the field, not the formal type,
					// so we must check whether we have the tainted field at all
					SootClass callerClass = interproceduralCFG().getMethodOf(iStmt).getDeclaringClass();
					if (callerClass.getFields().contains(source.getAccessPath().getFirstField()))
						taintedBase = new JInstanceFieldRef(iiExpr.getBase(),
								callerClass.getFieldByName(source.getAccessPath().getFirstField().getName()).makeRef());
				}
			}
		}
			
		List<Value> vals = taintWrapper.getTaintsForMethod(iStmt, taintedPos, taintedBase);
		if(vals != null) {
			for (Value val : vals) {
				Abstraction newAbs;
				if (pathTracking == PathTrackingMethod.ForwardTracking) {
					newAbs = ((AbstractionWithPath) source).deriveNewAbstraction(val).addPathElement(iStmt);
					res.add(newAbs);
				}
				else {
					newAbs = source.deriveNewAbstraction(val);
					res.add(newAbs);
				}

				// If the taint wrapper taints the base object, this must be propagated
				// backwards as there might be aliases for the base object
				if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
					if (iiExpr.getBase().equals(newAbs.getAccessPath().getPlainValue())
							|| newAbs.getAccessPath().isStaticFieldRef()){
						Abstraction bwAbs = source.deriveNewAbstraction(val,iStmt);
						for (Unit predUnit : interproceduralCFG().getPredsOf(iStmt))
							bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(bwAbs, predUnit, bwAbs));
					}
				}
			}
		}

		// For the moment, we don't implement static taints on wrappers. Pass it on
		// not to break anything
		if(source.getAccessPath().isStaticFieldRef())
			res.add(source);

		return res;
	}

	/**
	 * Checks whether a taint wrapper is exclusive for a specific invocation statement
	 * @param iStmt The call statement the taint wrapper shall check for well-
	 * known methods that introduce black-box taint propagation 
	 * @param callArgs The actual parameters with which the method in invoked
	 * @param source The taint source
	 * @return True if the wrapper is exclusive, otherwise false
	 */
	private boolean isWrapperExclusive
			(final Stmt iStmt,
			final List<Value> callArgs,
			Abstraction source) {
		if(taintWrapper == null || !taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass()))
			return false;
		
		int taintedPos = -1;
		for(int i=0; i< callArgs.size(); i++){
			if(source.getAccessPath().isLocal() && callArgs.get(i).equals(source.getAccessPath().getPlainValue())){
				taintedPos = i;
				break;
			}
		}
		Value taintedBase = null;
		if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
			if(iiExpr.getBase().equals(source.getAccessPath().getPlainValue())){
				if(source.getAccessPath().isLocal()){
					taintedBase = iiExpr.getBase();
				}else if(source.getAccessPath().isInstanceFieldRef()){
					// The taint refers to the actual type of the field, not the formal type,
					// so we must check whether we have the tainted field at all
					SootClass callerClass = interproceduralCFG().getMethodOf(iStmt).getDeclaringClass();
					if (callerClass.getFields().contains(source.getAccessPath().getFirstField()))
						taintedBase = new JInstanceFieldRef(iiExpr.getBase(),
								callerClass.getFieldByName(source.getAccessPath().getFirstField().getName()).makeRef());
				}
			}
			if(source.getAccessPath().isStaticFieldRef()){
				//TODO
			}
		}
			
		return taintWrapper.isExclusive(iStmt, taintedPos, taintedBase);
	}
	
	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			/**
			 * Creates a new taint abstraction for the given value
			 * @param src The source statement from which the taint originated
			 * @param targetValue The target value that shall now be tainted
			 * @param source The incoming taint abstraction from the source
			 * @param taintSet The taint set to which to add all newly produced
			 * taints
			 */
			private void addTaintViaStmt
					(final Unit src,
					final Value targetValue,
					Abstraction source,
					Set<Abstraction> taintSet,
					boolean cutFirstField) {
				taintSet.add(source);
				if (pathTracking == PathTrackingMethod.ForwardTracking)
					taintSet.add(((AbstractionWithPath) source.deriveNewAbstraction(targetValue, cutFirstField))
							.addPathElement(src));
				else
					taintSet.add(source.deriveNewAbstraction(targetValue, cutFirstField));

				if (triggerReverseFlow(targetValue, source)) {
					// call backwards-check:
					Abstraction newAbs = source.deriveNewAbstraction(targetValue, cutFirstField, src);
					for (Unit predUnit : interproceduralCFG().getPredsOf(src))
						bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(newAbs, predUnit, newAbs));
				}
			}

			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
				// If we compute flows on parameters, we create the initial
				// flow fact here
				if (src instanceof IdentityStmt) {
					final IdentityStmt is = (IdentityStmt) src;
					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();
							
							Set<Abstraction> res = new HashSet<Abstraction>();
							boolean addOriginal = true;
							if (is.getRightOp() instanceof CaughtExceptionRef) {
								if (source.getExceptionThrown()) {
									res.add(source.deriveNewAbstractionOnCatch(is.getLeftOp()));
									addOriginal = false;
								}
							}

							if (addOriginal)
								res.add(source);
							
							if (sourceSinkManager.isSource(is, interproceduralCFG())) {
								if (pathTracking != PathTrackingMethod.NoTracking)
									res.add(new AbstractionWithPath(is.getLeftOp(),
										is.getRightOp(),
										is, false).addPathElement(is));
								else
									res.add(new Abstraction(is.getLeftOp(),
										is.getRightOp(), is, false));
							}
							
							return res;
						}
					};

				}

				// taint is propagated with assignStmt
				else if (src instanceof AssignStmt) {
					final AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();

					final Value leftValue = BaseSelector.selectBase(left, false);
					final Set<Value> rightVals = BaseSelector.selectBaseList(right, true);

					return new FlowFunction<Abstraction>() {

						 @Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();
							boolean addLeftValue = false;
							boolean cutFirstField = false;
							Set<Abstraction> res = new HashSet<Abstraction>();
														
							// shortcuts:
							// on NormalFlow taint cannot be created
							if (source.equals(zeroValue)) {
								return Collections.emptySet();
							}
							Abstraction newSource;
							//check inactive elements:
							if (!source.isAbstractionActive() && source.getActivationUnit().equals(src)){
								newSource = source.getActiveCopy();
							}else{
								newSource = source;
							}
							
							for (Value rightValue : rightVals) {
								// check if static variable is tainted (same name, same class)
								//y = X.f && X.f tainted --> y, X.f tainted
								if (newSource.getAccessPath().isStaticFieldRef()) {
									if (rightValue instanceof StaticFieldRef) {
										StaticFieldRef rightRef = (StaticFieldRef) rightValue;
										if (newSource.getAccessPath().getFirstField().equals(rightRef.getField())) {
											addLeftValue = true; //TODO: check all fields --> create Testcase for this!
											cutFirstField = true;
										}
									}
								} else {
									// if both are fields, we have to compare their fieldName via equals and their bases
									//y = x.f && x tainted --> y, x tainted
									//y = x.f && x.f tainted --> y, x tainted
									if (rightValue instanceof InstanceFieldRef) {
										InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
										Local rightBase = (Local) rightRef.getBase();
										Local sourceBase =  newSource.getAccessPath().getPlainLocal();
										if (rightBase.equals(sourceBase)) {
											if (newSource.getAccessPath().isInstanceFieldRef()) {
												if (rightRef.getField().equals(newSource.getAccessPath().getFirstField())) {
													addLeftValue = true;
													cutFirstField = true;
												}
											} else {
												addLeftValue = true;
											}
										}
									}

									// indirect taint propagation:
									// if rightvalue is local and source is instancefield of this local:
									// y = x && x.f tainted --> y.f, x.f tainted
									// y.g = x && x.f tainted --> y.g.f, x.f tainted
									if (rightValue instanceof Local && newSource.getAccessPath().isInstanceFieldRef()) {
										Local base = newSource.getAccessPath().getPlainLocal();
										if (rightValue.equals(base)) {
											if (leftValue instanceof Local) {
												if (pathTracking == PathTrackingMethod.ForwardTracking)
													res.add(((AbstractionWithPath) newSource.deriveNewAbstraction
															(newSource.getAccessPath().copyWithNewValue(leftValue))).addPathElement(src));
												else
													res.add(newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(leftValue)));												

											} else {
												// access path length = 1 - taint entire value if left is field reference
												if (pathTracking == PathTrackingMethod.ForwardTracking)
													res.add(((AbstractionWithPath) newSource.deriveNewAbstraction(leftValue))
															.addPathElement(src));
												else
													res.add(newSource.deriveNewAbstraction(leftValue));
											}
										}
									}
	
									if (rightValue instanceof ArrayRef) {
										//y = x[i] && x tainted -> x, y tainted
										Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
										if (rightBase.equals(newSource.getAccessPath().getPlainValue())) {
											addLeftValue = true;
										}
									}
	
									// generic case, is true for Locals, ArrayRefs that are equal etc..
									//y = x && x tainted --> y, x tainted
									if (rightValue.equals(newSource.getAccessPath().getPlainValue())) {
										addLeftValue = true;
									}
								}
							}
							// if one of them is true -> add leftValue
							if (addLeftValue) {
								if (sourceSinkManager.isSink(assignStmt, interproceduralCFG())) {
									if (pathTracking != PathTrackingMethod.NoTracking)
										results.addResult(leftValue, assignStmt,
												newSource.getSource(),
												newSource.getSourceContext(),
												((AbstractionWithPath) newSource).getPropagationPathAsString(interproceduralCFG()),
												assignStmt.toString());
									else
										results.addResult(leftValue, assignStmt,
												newSource.getSource(), newSource.getSourceContext());
								}
								addTaintViaStmt(src, leftValue, newSource, res, cutFirstField);
								return res;
							}
							//if leftvalue contains the tainted value -> it is overwritten - remove taint:
							//but not for arrayRefs:
							// x[i] = y --> taint is preserved since we do not distinguish between elements of collections 
							//because we do not use a MUST-Alias analysis, we cannot delete aliases of taints 
							if(((AssignStmt)src).getLeftOp() instanceof ArrayRef){
								return Collections.singleton(newSource);
							}
							if(newSource.getAccessPath().isInstanceFieldRef()){

								//x.f = y && x.f tainted --> no taint propagated
								if (leftValue instanceof InstanceFieldRef) {
									InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
									if (leftRef.getBase().equals(newSource.getAccessPath().getPlainValue())) {
										if (leftRef.getField().equals(newSource.getAccessPath().getFirstField())) {
											return Collections.emptySet();
										}
										
									}
									//x = y && x.f tainted -> no taint propagated
								}else if (leftValue instanceof Local){
									if (leftValue.equals(newSource.getAccessPath().getPlainValue())) {
										return Collections.emptySet();
									}
								}	
							}else if(newSource.getAccessPath().isStaticFieldRef()){
								//X.f = y && X.f tainted -> no taint propagated
								if(leftValue instanceof StaticFieldRef && ((StaticFieldRef)leftValue).getField().equals(newSource.getAccessPath().getFirstField())){
									//TODO: create Testcase for this (with several fields?)
									return Collections.emptySet();
								}
								
							}
							//when the fields of an object are tainted, but the base object is overwritten then the fields should not be tainted any more
							//x.. = y && x tainted -> no taint propagated
							if(newSource.getAccessPath().isLocal() && leftValue.equals(newSource.getAccessPath().getPlainValue())){
								return Collections.emptySet();
							}
							//nothing applies: z = y && x tainted -> taint is preserved
							return Collections.singleton(newSource);
						}
					};
				}
				// for unbalanced problems, return statements correspond to
				// normal flows, not return flows, because there is no return
				// site we could jump to
				else if (src instanceof ReturnStmt) {
					final ReturnStmt returnStmt = (ReturnStmt) src;
					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();

							if (returnStmt.getOp().equals(source.getAccessPath().getPlainValue()) && sourceSinkManager.isSink(returnStmt, interproceduralCFG())) {
								if (pathTracking != PathTrackingMethod.NoTracking)
									results.addResult(returnStmt.getOp(), returnStmt,
											source.getSource(),
											source.getSourceContext(),
											((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()),
											interproceduralCFG().getMethodOf(returnStmt) + ": " + returnStmt.toString());
								else
									results.addResult(returnStmt.getOp(), returnStmt,
											source.getSource(), source.getSourceContext());
							}

							return Collections.singleton(source);
						}
					};
				}
				else if (src instanceof ThrowStmt) {
					final ThrowStmt throwStmt = (ThrowStmt) src;
					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();
							
							if (source.getAccessPath().getPlainLocal().equals(throwStmt.getOp()))
								return Collections.singleton(source.deriveNewAbstractionOnThrow());
							return Collections.singleton(source);
						}
					};
				}
				return Identity.v();
			}

			@Override
			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest) {
				final Stmt stmt = (Stmt) src;
				final InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> paramLocals = new ArrayList<Value>();
				for (int i = 0; i < dest.getParameterCount(); i++) {
					paramLocals.add(dest.getActiveBody().getParameterLocal(i));
				}
				
				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (stopAfterFirstFlow && !results.isEmpty())
							return Collections.emptySet();
						if (source.equals(zeroValue)) {
							return Collections.singleton(source);
						}
						if(isWrapperExclusive(stmt, callArgs, source)) {
							//taint is propagated in CallToReturnFunction, so we do not need any taint here:
							return Collections.emptySet();
						}
					
						//if we do not have to look into sinks:
						if (!inspectSinks && sourceSinkManager.isSink(stmt, interproceduralCFG())) {
							return Collections.emptySet();
						}

						Set<Abstraction> res = new HashSet<Abstraction>();
						// check if whole object is tainted (happens with strings, for example:)
						if (!dest.isStatic() && ie instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							// this might be enough because every call must happen with a local variable which is tainted itself:
							if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
								Abstraction abs;
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									abs = ((AbstractionWithPath) source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
											(dest.getActiveBody().getThisLocal()))).addPathElement(stmt);
								else
									abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
											(dest.getActiveBody().getThisLocal()));
								//add new callArgs:
								assert abs != source;		// our source abstraction must be immutable
								abs.setAbstractionFromCallEdge(abs.clone());
								res.add(abs);
							}
						}

						//special treatment for clinit methods - no param mapping possible
						if(!dest.getName().equals("<clinit>")) {
							assert dest.getParameterCount() == callArgs.size();
							// check if param is tainted:
							for (int i = 0; i < callArgs.size(); i++) {
								if (callArgs.get(i).equals(source.getAccessPath().getPlainLocal())) {
									Abstraction abs;
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										abs = ((AbstractionWithPath) source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
												(paramLocals.get(i)))).addPathElement(stmt);
									else
										abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
												(paramLocals.get(i)));
									assert abs != source;		// our source abstraction must be immutable
									abs.setAbstractionFromCallEdge(abs.clone());
									res.add(abs);
								}
							}
						}

						// staticfieldRefs must be analyzed even if they are not part of the params:
						if (source.getAccessPath().isStaticFieldRef()) {
							Abstraction abs;
							abs = source.clone();
							assert (abs.equals(source) && abs.hashCode() == source.hashCode());
							assert abs != source;		// our source abstraction must be immutable
							abs.setAbstractionFromCallEdge(abs.clone());
							res.add(abs);
						}
						
						return res;
					}
				};
			}

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, final Unit retSite) {

				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (stopAfterFirstFlow && !results.isEmpty())
							return Collections.emptySet();
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}
						if(callee.getName().contains("add")){
							System.out.println("x");
						}
						Abstraction newSource;
						if(!source.isAbstractionActive() && source.getActivationUnit().equals(callSite)){
							newSource = source.getActiveCopy();
						}else{
							newSource = source;
						}
						
						//if abstraction is not active and activeStmt was in this method, it will not get activated = it can be removed:
						if(!newSource.isAbstractionActive() && interproceduralCFG().getMethodOf(newSource.getActivationUnit()).equals(callee)){
							return Collections.emptySet();
						}
						
						Set<Abstraction> res = new HashSet<Abstraction>();

						// if we have a returnStmt we have to look at the returned value:
						if (exitStmt instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							Value retLocal = returnStmt.getOp();

							if (callSite instanceof DefinitionStmt) {
								DefinitionStmt defnStmt = (DefinitionStmt) callSite;
								Value leftOp = defnStmt.getLeftOp();
								if (retLocal.equals(newSource.getAccessPath().getPlainLocal())) {
									Abstraction abs;
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										abs = ((AbstractionWithPath) newSource.deriveNewAbstraction(newSource.getAccessPath()
											.copyWithNewValue(leftOp))).addPathElement(exitStmt);
									else
										abs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(leftOp));
									assert abs != newSource;		// our source abstraction must be immutable
									res.add(abs);
									 //call backwards-solver:
									Abstraction bwAbs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(leftOp), callSite);
									for (Unit predUnit : interproceduralCFG().getPredsOf(callSite))
										bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(bwAbs, predUnit, bwAbs));
								}
							}
								// this is required for sublists, because they assign the list to the return variable and call a method that taints the list afterwards
							

							// Check whether this return is treated as a sink

							assert returnStmt.getOp() == null
									|| returnStmt.getOp() instanceof Local
									|| returnStmt.getOp() instanceof Constant;
							if (returnStmt.getOp() != null
									&& newSource.getAccessPath().isLocal()
									&& newSource.getAccessPath().getPlainValue().equals(returnStmt.getOp())
									&& sourceSinkManager.isSink(returnStmt, interproceduralCFG())) {

								if (pathTracking != PathTrackingMethod.NoTracking)
									results.addResult(returnStmt.getOp(), returnStmt,
											newSource.getSource(),
											newSource.getSourceContext(),
											((AbstractionWithPath) newSource).getPropagationPathAsString(interproceduralCFG()),
											interproceduralCFG().getMethodOf(returnStmt) + ": " + returnStmt.toString());
								else
									results.addResult(returnStmt.getOp(), returnStmt,
											newSource.getSource(), newSource.getSourceContext());
							}
						}

						// easy: static
						if (newSource.getAccessPath().isStaticFieldRef()) {
							Abstraction abs = newSource.clone();
							assert (abs.equals(newSource) && abs.hashCode() == newSource.hashCode());
							res.add(abs);
							//TODO: hier auch immer bw starten?! geht das nicht anders?
							// call backwards-check:
							Abstraction bwAbs = newSource.deriveNewAbstraction(callSite);
							for (Unit predUnit : interproceduralCFG().getPredsOf(callSite))
								bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(bwAbs, predUnit, bwAbs));
						}
						
						
						
						// checks: this/params/fields

						// check one of the call params are tainted (not if simple type)
						Value sourceBase = newSource.getAccessPath().getPlainLocal();
						Value originalCallArg = null;

						for (int i = 0; i < callee.getParameterCount(); i++) {
							if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
								if (callSite instanceof Stmt) {
									Stmt iStmt = (Stmt) callSite;
									originalCallArg = iStmt.getInvokeExpr().getArg(i);
									//either the param is a fieldref (not possible in jimple?) or an array Or one of its fields is tainted/all fields are tainted
									if (triggerReverseFlow(originalCallArg, newSource)) {
										Abstraction abs;
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											abs = ((AbstractionWithPath) newSource.deriveNewAbstraction(newSource.getAccessPath()
														.copyWithNewValue(originalCallArg))).addPathElement(exitStmt);
										else
											abs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(originalCallArg));
										res.add(abs);

										// call backwards-check:
										Abstraction bwAbs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(originalCallArg), callSite);
										for (Unit predUnit : interproceduralCFG().getPredsOf(callSite))
											bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(bwAbs, predUnit, bwAbs));
									}
								}
							}
						}


						Local thisL = null;
						if (!callee.isStatic()) {
							thisL = callee.getActiveBody().getThisLocal();
						}
						if (thisL != null) {
							if (thisL.equals(sourceBase)) {
								boolean param = false;
								// check if it is not one of the params (then we have already fixed it)
								for (int i = 0; i < callee.getParameterCount(); i++) {
									if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
										param = true;
									}
								}
								if (!param) {
									if (callSite instanceof Stmt) {
										Stmt stmt = (Stmt) callSite;
										if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
											InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
											Abstraction abs;
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												abs = ((AbstractionWithPath) newSource).deriveNewAbstraction
													(newSource.getAccessPath().copyWithNewValue(iIExpr.getBase())).addPathElement(stmt);
											else
												abs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(iIExpr.getBase()));
											res.add(abs);
											Abstraction bwAbs = newSource.deriveNewAbstraction(newSource.getAccessPath().copyWithNewValue(iIExpr.getBase()), callSite);
											for (Unit predUnit : interproceduralCFG().getPredsOf(callSite))
												bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(bwAbs, predUnit, bwAbs));
										}
									}
								}
							}	
						}

						return res; 
					} 

				};
			}

			@Override
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, final Unit returnSite) {
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();
							Abstraction newSource;
							//check inactive elements:
							if (!source.isAbstractionActive() && source.getActivationUnit().equals(call)){
								newSource = source.getActiveCopy();
							}else{
								newSource = source;
							}

							Set<Abstraction> res = new HashSet<Abstraction>();
							res.addAll(computeWrapperTaints(iStmt, callArgs, newSource));

							// We can only pass on a taint if it is neither a parameter nor the
							// base object of the current call
							boolean passOn = true;
							if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
								if (((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase().equals
										(newSource.getAccessPath().getPlainLocal()))
									passOn = false;
							if (passOn)
								for (int i = 0; i < callArgs.size(); i++)
									if (callArgs.get(i).equals(newSource.getAccessPath().getPlainLocal()) && isTransferableValue(callArgs.get(i))) {
										passOn = false;
										break;
									}
							//static variables are always propagated if they are not overwritten. So if we have at least one call/return edge pair,
							//we can be sure that the value does not get "lost" if we do not pass it on:
							if(newSource.getAccessPath().isStaticFieldRef()){
								if(interproceduralCFG().getCalleesOfCallAt(call).size()>0)
									passOn = false;
							}
							if (passOn)
								res.add(newSource);
							if (iStmt.getInvokeExpr().getMethod().isNative()) {
								if (callArgs.contains(newSource.getAccessPath().getPlainValue())) {
									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
									res.addAll(ncHandler.getTaintedValues(iStmt, newSource, callArgs));
								}
							}

							if (iStmt instanceof AssignStmt) {
								final AssignStmt stmt = (AssignStmt) iStmt;
								if (sourceSinkManager.isSource(stmt, interproceduralCFG())) {
									if (DEBUG)
										System.out.println("Found source: " + stmt.getInvokeExpr().getMethod());
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(stmt.getLeftOp(),
												stmt.getInvokeExpr(),
												stmt, false).addPathElement(call));
									else
										res.add(new Abstraction(stmt.getLeftOp(),
												stmt.getInvokeExpr(), stmt, false));
									res.remove(zeroValue);
								}
							}

							// if we have called a sink we have to store the path from the source - in case one of the params is tainted!
							if (sourceSinkManager.isSink(iStmt, interproceduralCFG())) {
								boolean taintedParam = false;
								for (int i = 0; i < callArgs.size(); i++) {
									if (callArgs.get(i).equals(newSource.getAccessPath().getPlainLocal())) {
										taintedParam = true;
										break;
									}
								}

								if (taintedParam) {
									if (pathTracking != PathTrackingMethod.NoTracking)
										results.addResult(iStmt.getInvokeExpr(), iStmt,
												newSource.getSource(),
												newSource.getSourceContext(),
												((AbstractionWithPath) newSource).getPropagationPathAsString(interproceduralCFG()),
												interproceduralCFG().getMethodOf(call) + ": " + call.toString());
									else
										results.addResult(iStmt.getInvokeExpr(), iStmt,
												newSource.getSource(), newSource.getSourceContext());
								}
								// if the base object which executes the method is tainted the sink is reached, too.
								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
									InstanceInvokeExpr vie = (InstanceInvokeExpr) iStmt.getInvokeExpr();
									if (vie.getBase().equals(newSource.getAccessPath().getPlainValue())) {
										if (pathTracking != PathTrackingMethod.NoTracking)
											results.addResult(iStmt.getInvokeExpr(), iStmt,
													newSource.getSource(),
													newSource.getSourceContext(),
													((AbstractionWithPath) newSource).getPropagationPathAsString(interproceduralCFG()),
													interproceduralCFG().getMethodOf(call) + ": " + call.toString());

										else
											results.addResult(iStmt.getInvokeExpr(), iStmt,
													newSource.getSource(), newSource.getSourceContext());
									}
								}
							}
							return res;
						}


					};
				}
				return Identity.v();
			}
		};
	}

	public InfoflowProblem(List<String> sourceList, List<String> sinkList) {
		super(new JimpleBasedBiDiICFG());
		this.sourceSinkManager = new DefaultSourceSinkManager(sourceList, sinkList);
	}

	public InfoflowProblem(SourceSinkManager sourceSinkManager) {
		super(new JimpleBasedBiDiICFG());
		this.sourceSinkManager = sourceSinkManager;
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinkList) {
		super(icfg);
		this.sourceSinkManager = new DefaultSourceSinkManager(sourceList, sinkList);
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, SourceSinkManager sourceSinkManager) {
		super(icfg);
		this.sourceSinkManager = sourceSinkManager;
	}

	public InfoflowProblem(SourceSinkManager mySourceSinkManager, Set<Unit> analysisSeeds) {
	    super(new JimpleBasedBiDiICFG());
	    this.sourceSinkManager = mySourceSinkManager;
	    this.initialSeeds.addAll(analysisSeeds);
    }

    @Override
	public Abstraction createZeroValue() {
		if (zeroValue == null) {
			zeroValue = this.pathTracking == PathTrackingMethod.NoTracking ?
				new Abstraction(new JimpleLocal("zero", NullType.v()), null, null, false) :
				new AbstractionWithPath(new JimpleLocal("zero", NullType.v()), null, null, false);
		}
		return zeroValue;
	}
	
	public void setBackwardSolver(InfoflowSolver backwardSolver){
		bSolver = backwardSolver;
	}

	@Override
	public boolean autoAddZero() {
		return false;
	}

}


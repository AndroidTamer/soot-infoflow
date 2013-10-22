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
package soot.jimple.infoflow.problems;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import heros.solver.PathEdge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.heros.BackwardsInfoflowCFG;
import soot.jimple.infoflow.heros.InfoflowSolver;
import soot.jimple.infoflow.heros.SolverCallToReturnFlowFunction;
import soot.jimple.infoflow.heros.SolverNormalFlowFunction;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.BaseSelector;

/**
 * class which contains the flow functions for the backwards solver. This is required for on-demand alias analysis.
 */
public class BackwardsInfoflowProblem extends AbstractInfoflowProblem {
	private InfoflowSolver fSolver;

	public void setTaintWrapper(ITaintPropagationWrapper wrapper) {
		taintWrapper = wrapper;
	}

	public BackwardsInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
	}

	public BackwardsInfoflowProblem() {
		super(new BackwardsInfoflowCFG());
	}

	public void setForwardSolver(InfoflowSolver forwardSolver) {
		fSolver = forwardSolver;
	}
	
	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			/**
			 * Computes the aliases for the given statement
			 * @param def The definition statement from which to extract
			 * the alias information
			 * @param d1 The abstraction at the method's start node
			 * @param source The source abstraction of the alias search
			 * from before the current statement
			 * @return The set of abstractions after the current statement
			 */
			private Set<Abstraction> computeAliases
					(final DefinitionStmt defStmt, Abstraction d1, Abstraction source) {
				final Set<Abstraction> res = new HashSet<Abstraction>();
				final Value leftValue = BaseSelector.selectBase(defStmt.getLeftOp(), true);
				
				// A backward analysis looks for aliases of existing taints and thus
				// cannot create new taints out of thin air
				if (source.equals(zeroValue))
					return Collections.emptySet();

				// Check whether the left side of the assignment matches our
				// current taint abstraction
				final boolean leftSideMatches = baseMatches(leftValue, source);
				if (!leftSideMatches)
					res.add(source);

				// Is the left side overwritten completely?
				if (leftSideMatches) {
					// If we have an assignment to the base local of the current taint,
					// all taint propagations must be below that point, so this is the
					// right point to turn around.
					if (triggerInaktiveTaintOrReverseFlow(leftValue, source))
						for (Unit u : interproceduralCFG().getPredsOf(defStmt))
							fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, source));
				}
				
				if (defStmt instanceof AssignStmt) {
					// Get the right side of the assignment
					final AssignStmt assignStmt = (AssignStmt) defStmt;
					final Value rightValue = BaseSelector.selectBase(assignStmt.getRightOp(), false);

					// Is the left side overwritten completely?
					if (leftSideMatches) {
						// Termination shortcut: If the right side is a value we do not track,
						// we can stop here.
						if (rightValue instanceof NewExpr
									|| rightValue instanceof NewArrayExpr
									|| rightValue instanceof Constant) {
							return Collections.emptySet();
						}
					}

					// If we assign a constant, there is no need to track the right side
					// any further or do any forward propagation since constants cannot
					// carry taint.
					if (rightValue instanceof Constant)
						return res;

					// We only process heap objects. Binary operations can only
					// be performed on primitive objects.
					if (rightValue instanceof BinopExpr)
						return res;

					// If the tainted value 'b' is assigned to variable 'a' and 'a'
					// is a heap object, we must also look for aliases of 'a' upwards
					// from the current statement.
					if (triggerInaktiveTaintOrReverseFlow(leftValue, source)) {
						if (rightValue instanceof InstanceFieldRef) {
							InstanceFieldRef ref = (InstanceFieldRef) rightValue;
							if (source.getAccessPath().isInstanceFieldRef()
									&& ref.getBase().equals(source.getAccessPath().getPlainValue())
									&& ref.getField().equals(source.getAccessPath().getFirstField())) {
								Abstraction abs = source.deriveNewAbstraction(leftValue, true, defStmt);
								res.add(abs);
							}
						}
						else if (enableStaticFields && rightValue instanceof StaticFieldRef) {
							StaticFieldRef ref = (StaticFieldRef) rightValue;
							if (source.getAccessPath().isStaticFieldRef()
									&& ref.getField().equals(source.getAccessPath().getFirstField())) {
								Abstraction abs = source.deriveNewAbstraction(leftValue, true, defStmt);
								res.add(abs);
							}
						}
						else if (rightValue.equals(source.getAccessPath().getPlainValue())) {
							Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue),
									defStmt);
							res.add(abs);
						}
					}

					// If we have the tainted value on the left side of the assignment,
					// we also have to look or aliases of the value on the right side of
					// the assignment.
					if (!(rightValue instanceof NewExpr || rightValue instanceof NewArrayExpr
							|| rightValue instanceof Constant
							|| rightValue instanceof BinopExpr
							|| rightValue instanceof InvokeExpr)) {
						boolean addRightValue = false;
						boolean cutFirstField = false;

						// if both are fields, we have to compare their fieldName via equals and their bases via PTS
						if (leftValue instanceof InstanceFieldRef) {
							InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
							if (leftRef.getBase().equals(source.getAccessPath().getPlainLocal())) {
								if (source.getAccessPath().isInstanceFieldRef()) {
									if (leftRef.getField().equals(source.getAccessPath().getFirstField())) {
										addRightValue = true;
										cutFirstField = true;
									}
								} else {
									addRightValue = true;
								}
							}
							// indirect taint propagation:
							// if leftValue is local and source is instancefield of this local:
						} else if (leftValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
							Local base = source.getAccessPath().getPlainLocal(); // ?
							if (leftValue.equals(base)) {
								res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rightValue),
										defStmt));
							}
						} else if (leftValue instanceof ArrayRef) {
							Local leftBase = (Local) ((ArrayRef) leftValue).getBase();
							if (leftBase.equals(source.getAccessPath().getPlainValue())) {
								addRightValue = true;
							}
							// generic case, is true for Locals, ArrayRefs that are equal etc..
						} else if (leftValue.equals(source.getAccessPath().getPlainValue())) {
							addRightValue = true;
						}

						// if one of them is true -> add rightValue
						if (addRightValue) {
							Abstraction newAbs = source.deriveNewAbstraction(rightValue, cutFirstField, defStmt);
							res.add(newAbs);
						}
					}
				}

				// Trigger the forward solver with every newly-created alias
				// on a static field. Since we don't have a fixed turn-around
				// point for static fields, we turn around on every use.
				if (enableStaticFields)
					for (Abstraction newAbs : res)
						if (newAbs.getAccessPath().isStaticFieldRef() && newAbs != source)
							for (Unit u : interproceduralCFG().getPredsOf(defStmt))
								fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, newAbs));

				return res;
			}

			/**
			 * Checks whether the given base value matches the base of the given
			 * taint abstraction
			 * @param baseValue The value to check
			 * @param source The taint abstraction to check
			 * @return True if the given value has the same base value as the given
			 * taint abstraction, otherwise false
			 */
			private boolean baseMatches(final Value baseValue, Abstraction source) {
				boolean leftSideMatches = baseValue instanceof Local
						&& baseValue.equals(source.getAccessPath().getPlainValue());
				if (!leftSideMatches && baseValue instanceof InstanceFieldRef) {
					InstanceFieldRef ifr = (InstanceFieldRef) baseValue;
					if (ifr.getBase().equals(source.getAccessPath().getPlainValue())
							&& ifr.getField().equals(source.getAccessPath().getFirstField()))
						leftSideMatches = true;
				}
				if (!leftSideMatches && baseValue instanceof StaticFieldRef) {
					StaticFieldRef sfr = (StaticFieldRef) baseValue;
					if (sfr.getField().equals(source.getAccessPath().getFirstField()))
						leftSideMatches = true;
				}
				return leftSideMatches;
			}

			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
				
				if (src instanceof DefinitionStmt) {
					final DefinitionStmt defStmt = (DefinitionStmt) src;

					return new SolverNormalFlowFunction() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
							if (source.equals(zeroValue))
								return Collections.emptySet();
							assert !source.isAbstractionActive();

							Set<Abstraction> res = computeAliases(defStmt, d1, source);
							
							// If the next statement assigns the base of the tainted value,
							// we look ahead with the alias search. This fixes the problem
							// that the first statement of a method never ends up in "src".
							if (dest instanceof DefinitionStmt) {
								DefinitionStmt defStmt = (DefinitionStmt) dest;
								for (Abstraction newAbs : res)
									if (baseMatches(defStmt.getLeftOp(), newAbs)
											&& triggerInaktiveTaintOrReverseFlow(defStmt.getLeftOp(), newAbs))
										for (Unit u : interproceduralCFG().getPredsOf(defStmt))
											fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, newAbs));
 							}

							return res;
						}

					};
				}
				return Identity.v();
			}

			@Override
			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest) {
				return new FlowFunction<Abstraction>() {

					public Set<Abstraction> computeTargets(Abstraction source) {
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}
						
						/* Backwards taint wrapping is highly complicated, so we ignore it for the moment
						if (taintWrapper != null && taintWrapper.supportsBackwardWrapping() && taintWrapper.supportsTaintWrappingForClass(dest.getDeclaringClass())) {
							// taint is propagated in CallToReturnFunction, so we do not need any taint here if it is exclusive:
							if(taintWrapper.isExclusive((Stmt)src, 0, null)){
								return Collections.emptySet();
							}
						}
						*/
						Set<Abstraction> res = new HashSet<Abstraction>();
						
						// if the returned value is tainted - taint values from return statements
						if (src instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) src;
							Value leftOp = defnStmt.getLeftOp();
							if (leftOp.equals(source.getAccessPath().getPlainValue())) {
								// look for returnStmts:
								for (Unit u : dest.getActiveBody().getUnits()) {
									if (u instanceof ReturnStmt) {
										ReturnStmt rStmt = (ReturnStmt) u;
										if (rStmt.getOp() instanceof Local
												|| rStmt.getOp() instanceof FieldRef) {
											Abstraction abs = source.deriveNewAbstraction
													(source.getAccessPath().copyWithNewValue(rStmt.getOp()), (Stmt) src);
											assert abs != source;		// our source abstraction must be immutable
											res.add(abs);
										}
									}
								}
							}
						}

						// easy: static
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
							res.add(source);

						// checks: this/fields

						Value sourceBase = source.getAccessPath().getPlainValue();
						Stmt iStmt = (Stmt) src;
						if (!dest.isStatic()) {
							Local thisL = dest.getActiveBody().getThisLocal();
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
							if (iIExpr.getBase().equals(sourceBase)) {
								// there is only one case in which this must be added, too: if the caller-Method has the same thisLocal - check this:

								boolean param = false;
								// check if it is not one of the params (then we have already fixed it)
								for (int i = 0; i < dest.getParameterCount(); i++) {
									if (iStmt.getInvokeExpr().getArg(i).equals(sourceBase)) {
										param = true;
										break;
									}
								}
								if (!param) {
									if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
										Abstraction abs = source.deriveNewAbstraction
												(source.getAccessPath().copyWithNewValue(thisL), (Stmt) src);
										assert abs != source;		// our source abstraction must be immutable
										res.add(abs);
									}
								}
							}
							// TODO: add testcase which requires params to be propagated

						}

						for (Abstraction abs : res)
							if (!abs.getAccessPath().isEmpty())
								fSolver.injectContext(solver, dest, abs, src, source);
					
						return res;
					}
				};
			}

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, Unit exitStmt, final Unit retSite) {
				return KillAll.v();
			}

			@Override
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, final Unit returnSite) {
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;

					return new SolverCallToReturnFlowFunction() {
						@Override
						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
							// only pass source if the source is not created by this methodcall
							if (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp().equals
									(source.getAccessPath().getPlainValue())){
								//terminates here, but we have to start a forward pass to consider all method calls:
								for (Unit u : interproceduralCFG().getPredsOf(iStmt))
									fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, source));
								return Collections.emptySet();
							}
							else {
								// If the return site matches on an abstraction we pass on, we may have to turn around
								// that the first statement of a method never ends up in "src".
								if (returnSite instanceof DefinitionStmt) {
									DefinitionStmt defStmt = (DefinitionStmt) returnSite;
									if (baseMatches(defStmt.getLeftOp(), source)
											&& triggerInaktiveTaintOrReverseFlow(defStmt.getLeftOp(), source))
										for (Unit u : interproceduralCFG().getPredsOf(defStmt))
											fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, source));
	 							}
								return Collections.singleton(source);
							}
						}
					};
				}
				return Identity.v();
			}
		};
	}

}

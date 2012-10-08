package soot.jimple.infoflow.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import soot.BooleanType;
import soot.G;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.dava.internal.javaRep.DIntConstant;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

public class EntryPointCreator {

	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * 
	 * @param classmethods
	 *            the methods to call
	 * @param createdClass
	 *            the class which contains the methods
	 * @return list of entryPoints
	 */
	public List<SootMethod> createDummyMain(Entry<String, List<SootMethod>> classEntry, SootClass createdClass) {

		// create new class:
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		JimpleBody body = Jimple.v().newBody();
		LocalGenerator generator = new LocalGenerator(body);
		Local tempLocal = generator.generateLocal(RefType.v(classEntry.getKey()));

		// call class for the required methods - use every possible constructor
		if (isConstructorGenerationPossible(createdClass)) {
			generateClassConstructor(createdClass, body);
		} else {
			// backup: simplified form:

			NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(classEntry.getKey()));
			AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
			SpecialInvokeExpr sinvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, Scene.v().makeMethodRef(createdClass, "<init>", new ArrayList<Type>(), VoidType.v(), false));
			body.getUnits().add(assignStmt);
			body.getUnits().add(Jimple.v().newInvokeStmt(sinvokeExpr));

			// TODO: also call constructor of call params:
			for (SootMethod method : classEntry.getValue()) {
				Local stringLocal = null;
				method.setDeclaringClass(createdClass);
				VirtualInvokeExpr vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, method.makeRef()); // TODO: aufrufparameter
				if (!(method.getReturnType() instanceof VoidType)) {
					stringLocal = generator.generateLocal(method.getReturnType());
					AssignStmt assignStmt2 = Jimple.v().newAssignStmt(stringLocal, vInvokeExpr);
					body.getUnits().add(assignStmt2);
				} else {
					body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
				}

			}

		}

		SootMethod mainMethod = new SootMethod("dummyMainMethod", new ArrayList<Type>(), VoidType.v());
		body.setMethod(mainMethod);
		mainMethod.setActiveBody(body);
		SootClass mainClass = new SootClass("dummyMainClass");
		mainClass.setApplicationClass();
		mainClass.addMethod(mainMethod);
		Scene.v().addClass(mainClass);
		entryPoints.add(mainMethod);
		return entryPoints;
	}

	private boolean isConstructorGenerationPossible(SootClass sClass) {
		if (sClass == null) {
			return false;
		}
		if(isSimpleType(sClass.toString())){
			return true;
		}
		
		// look for constructors:
		boolean oneOk = false; // we need at least one constructor that works
		List<SootMethod> methodList = (List<SootMethod>) sClass.getMethods();
		for (SootMethod currentMethod : methodList) {
			if (currentMethod.isPublic() && currentMethod.isConstructor()) {
				boolean canGenerateConstructor = true;
				List<Type> typeList = (List<Type>) currentMethod.getParameterTypes();
				for (Type type : typeList) {
					String typeName = type.toString().replaceAll("\\[\\]]", "");
					// 1. Type not available:
					if (!Scene.v().containsClass(typeName)) {
						canGenerateConstructor = false;
						break;
					} else {
						SootClass typeClass = Scene.v().getSootClass(typeName);
						// 2. Type not public:
						if (!typeClass.isPublic()) {
							canGenerateConstructor = false;
							break;
						}
						// we have to recursively check this type, too:
						if (!typeClass.isJavaLibraryClass() && !isConstructorGenerationPossible(typeClass)) { // TODO: is this okay for "primitive datatypes and others - maybe not because List<CustomType> has to be created, too?
							canGenerateConstructor = false;
							break;
						}
					}

					// -> no nicer way for this?
				}
				if (canGenerateConstructor) {
					oneOk = true;
				}

			}

		}
		return oneOk;
	}

	private Value generateClassConstructor(SootClass createdClass, JimpleBody body) {
		System.out.println("XXX - " + createdClass.toString());
		// if sootClass is simpleClass:
		if (EntryPointCreator.isSimpleType(createdClass.toString())) {
			LocalGenerator generator = new LocalGenerator(body);
			Local varLocal =  generator.generateLocal(createdClass.getType());
			
			AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(createdClass.toString()));
			body.getUnits().add(aStmt);
			return varLocal;
		} else {

			List<SootMethod> methodList = (List<SootMethod>) createdClass.getMethods();
			Local returnLocal = null;
			for (SootMethod currentMethod : methodList) {
				if (currentMethod.isPublic() && currentMethod.isConstructor()) {
					@SuppressWarnings("unchecked")
					List<Type> typeList = (List<Type>) currentMethod.getParameterTypes();
					List<Object> params = new LinkedList<Object>();
					for (Type type : typeList) {
						String typeName = type.toString().replaceAll("\\[\\]]", "");
						if (Scene.v().containsClass(typeName)) {
							SootClass typeClass = Scene.v().getSootClass(typeName);
							// 2. Type not public:
							if (typeClass.isPublic() && !typeClass.toString().equals(createdClass.toString())) { // avoid loops
								params.add(generateClassConstructor(typeClass, body));
							}
						} else {
							params.add(G.v().soot_jimple_NullConstant());	
						}
					}
					LocalGenerator generator = new LocalGenerator(body);
					Local tempLocal = generator.generateLocal(RefType.v(createdClass));
					
					VirtualInvokeExpr vInvokeExpr;
					if (params.isEmpty()) {
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef());
					} else {
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef(), params);
					}
					if (!(currentMethod.getReturnType() instanceof VoidType)) { //should always be true?
						returnLocal = generator.generateLocal(currentMethod.getReturnType());
						AssignStmt assignStmt2 = Jimple.v().newAssignStmt(returnLocal, vInvokeExpr);
						body.getUnits().add(assignStmt2);
					} else {
						body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
					}
				}
			}
			return returnLocal;
		}
	}

	private static boolean isSimpleType(String t) {
		if (t.equals("java.lang.String") || t.equals("void") || t.equals("char") || t.equals("byte") || t.equals("short") || t.equals("int") || t.equals("float") || t.equals("long") || t.equals("double") || t.equals("boolean")) {
			return true;
		} else {
			return false;
		}
	}

	private Value getSimpleDefaultValue(String t) {
		if (t.equals("boolean")) {
			return DIntConstant.v(0, BooleanType.v());
		} else if (t.equals("java.lang.String")) {
			return StringConstant.v("");
		} else if (t.equals("int")) {
			return IntConstant.v(0);
		} else if (t.equals("long")){
			return LongConstant.v(0);
		} else if (t.equals("double")){
			return DoubleConstant.v(0);
		} else if (t.equals("float")){
			return FloatConstant.v(0);
		}

		//also for arrays etc.
		return G.v().soot_jimple_NullConstant();

	}

}

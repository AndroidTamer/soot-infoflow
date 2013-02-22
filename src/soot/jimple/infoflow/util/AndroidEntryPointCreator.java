package soot.jimple.infoflow.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.IntType;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

/**
 * based on: http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
 * and http://developer.android.com/reference/android/app/Service.html
 * and http://developer.android.com/reference/android/content/BroadcastReceiver.html#ReceiverLifecycle
 * and http://developer.android.com/reference/android/content/BroadcastReceiver.html
 * @author Christian
 *
 */
public class AndroidEntryPointCreator extends BaseEntryPointCreator implements IEntryPointCreator{
	private JimpleBody body;
	private LocalGenerator generator;
	private int conditionCounter;
	private Value intCounter;
	
	/**
	 *  Soot requires a main method, so we create a dummy method which calls all entry functions. 
	 *  Androids components are detected and treated according to their lifecycles. This also
	 *  forces the classes in the list to be resolved.
	 *  
	 * @param methodSignatureList a list of method signatures in soot syntax ( <class: returntype methodname(arguments)> )
	 * @return the dummyMethod which was created
	 */
	public SootMethod createDummyMain(List<String> methodSignatureList){
		SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
		HashMap<String, List<String>> classes = parser.parseClassNames(methodSignatureList, false);
		
		for (String className : classes.keySet()) {
			SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
			c.setApplicationClass();
		}
		
		return createDummyMain(classes);
	}
	
	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * 
	 * @param classMap
	 *            the methods to call (signature as String)
	 * @param createdClass
	 *            the class which contains the methods
	 * @return list of entryPoints
	 */
	public SootMethod createDummyMain(Map<String, List<String>> classMap) {

		// create new class:
 		body = Jimple.v().newBody();
 		
 		SootMethod mainMethod = createEmptyMainMethod(body);
		
		generator = new LocalGenerator(body);
				
		// add entrypoint calls
		conditionCounter = 0;
		intCounter = generator.generateLocal(IntType.v());

		JAssignStmt assignStmt = new JAssignStmt(intCounter, IntConstant.v(conditionCounter));
		body.getUnits().add(assignStmt);
		
		
		for(Entry<String, List<String>> entry : classMap.entrySet()){
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			JNopStmt endClassStmt = new JNopStmt();

			boolean activity = false;
			boolean service = false;
			boolean broadcastReceiver = false;
			boolean contentProvider = false;
			boolean plain = false;
			
			//Scene.v().getSootClass("android.app.Activity");
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc : extendedClasses){
				if(sc.getName().equals(AndroidEntryPointConstants.ACTIVITYCLASS)){
					activity = true;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.SERVICECLASS)){
					service = true;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.BROADCASTRECEIVERCLASS)){
					broadcastReceiver = true;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.CONTENTPROVIDERCLASS)){
					contentProvider = true;
				}
				
			}
			if(!activity && !service && !broadcastReceiver && !contentProvider)
				plain = true;

			// Check if one of the methods is instance. This tells us whether
			// we need to create a constructor invocation or not. Furthermore,
			// we collect references to the corresponding SootMethod objects.
			boolean instanceNeeded = activity || service || broadcastReceiver || contentProvider;
			Map<String, SootMethod> plainMethods = new HashMap<String, SootMethod>();
			SootMethodRepresentationParser methodParser = new SootMethodRepresentationParser();
			if (!instanceNeeded || plain)
				for(String method : entry.getValue()){
					SootMethod sm = null;
					
					// Find the method. It may either be implemented directly in the
					// given class or it may be inherited from one of the superclasses.
					if(Scene.v().containsMethod(method))
						sm = Scene.v().getMethod(method);
					else {
						SootMethodAndClass methodAndClass = methodParser.parseSootMethodString(method);
						if (!Scene.v().containsClass(methodAndClass.getClassString())) {
							System.err.println("Class for entry point " + method + " not found, skipping...");
							continue;
						}
						for (SootClass sc : Scene.v().getActiveHierarchy().getSuperclassesOf
								(Scene.v().getSootClass(methodAndClass.getClassString()))) {
							if (sc.declaresMethodByName(methodAndClass.getSootMethod().getName())) {
								sm = sc.getMethodByName(methodAndClass.getSootMethod().getName());
								break;
							}
						}
						if (sm == null) {
							System.err.println("Method for entry point " + method + " not found in class, skipping...");
							continue;
						}
					}

					plainMethods.put(method, sm);
					if(!sm.isStatic())
						instanceNeeded = true;
				}
			
			// if we need to call a constructor, we insert the respective Jimple statement here
			if(instanceNeeded){
				String className = entry.getKey();
				SootClass createdClass = Scene.v().getSootClass(className);
				if (isConstructorGenerationPossible(createdClass)) {
					Local localVal = generateClassConstructor(createdClass, body);
					localVarsForClasses.put(className, localVal);
				}else{
					System.out.println("Constructor cannot be generated for "+ createdClass);
				}
			}
			Local classLocal = localVarsForClasses.get(entry.getKey());

			if(activity){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod
						(AndroidEntryPointConstants.ACTIVITY_ONCREATE, currentClass, entryPoints, classLocal);
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod
						(AndroidEntryPointConstants.ACTIVITY_ONSTART, currentClass, entryPoints, classLocal);
				//3. onResume:
				JNopStmt onResumeStmt = searchAndBuildMethod
						(AndroidEntryPointConstants.ACTIVITY_ONRESUME, currentClass, entryPoints, classLocal);
	
				
				//all other entryPoints of this class:
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getActivityLifecycleMethods().contains(currentMethod.getSubSignature())){
						JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
						conditionCounter++;
						JNopStmt thenStmt = new JNopStmt();
						JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
						body.getUnits().add(ifStmt);
						JNopStmt elseStmt = new JNopStmt();
						JGotoStmt elseGoto = new JGotoStmt(elseStmt);
						body.getUnits().add(elseGoto);
						
						body.getUnits().add(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);

						body.getUnits().add(elseStmt);
					}
				}
				body.getUnits().add(endWhileStmt);
				createIfStmt(startWhileStmt);
				
				//4. onPause:
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPAUSE, currentClass, entryPoints, classLocal);
				//goTo Stop, Resume or Create:
				JNopStmt pauseToStopStmt = new JNopStmt();
				createIfStmt(pauseToStopStmt);
				createIfStmt(onResumeStmt);
				createIfStmt(onCreateStmt);
				
				body.getUnits().add(pauseToStopStmt);
				//5. onStop:
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSTOP, currentClass, entryPoints, classLocal);
				//goTo onDestroy, onRestart or onCreate:
				conditionCounter++;
				JNopStmt stopToDestroyStmt = new JNopStmt();
				JNopStmt stopToRestartStmt = new JNopStmt();
				createIfStmt(stopToDestroyStmt);
				createIfStmt(stopToRestartStmt);
				createIfStmt(onCreateStmt);
				
				//6. onRestart:
				body.getUnits().add(stopToRestartStmt);
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTART, currentClass, entryPoints, classLocal);
				if(onStartStmt != null){
					JGotoStmt startGoto = new JGotoStmt(onStartStmt);
					body.getUnits().add(startGoto);
				}
				
				//7. onDestroy
				body.getUnits().add(stopToDestroyStmt);
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONDESTROY, currentClass, entryPoints, classLocal);

				createIfStmt(endClassStmt);
				
			}
			if(service){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONCREATE, currentClass, entryPoints, classLocal);
				
				//service has two different lifecycles:
				//lifecycle1:
				//2. onStart:
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART1, currentClass, entryPoints, classLocal);
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART2, currentClass, entryPoints, classLocal);
				//methods: 
				//all other entryPoints of this class:
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getServiceLifecycleMethods().contains(currentMethod.getSubSignature())){
						JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
						conditionCounter++;
						JNopStmt thenStmt = new JNopStmt();
						JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
						body.getUnits().add(ifStmt);
						JNopStmt elseStmt = new JNopStmt();
						JGotoStmt elseGoto = new JGotoStmt(elseStmt);
						body.getUnits().add(elseGoto);
						
						body.getUnits().add(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);

						body.getUnits().add(elseStmt);
					}
				}
				body.getUnits().add(endWhileStmt);
				createIfStmt(startWhileStmt);
				
				//lifecycle1 end
				
				//lifecycle2 start
				//onBind:
				JNopStmt onBindStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONBIND, currentClass, entryPoints, classLocal);
				
				JNopStmt beforemethodsStmt = new JNopStmt();
				body.getUnits().add(beforemethodsStmt);
				//methods
				JNopStmt startWhile2Stmt = new JNopStmt();
				JNopStmt endWhile2Stmt = new JNopStmt();
				body.getUnits().add(startWhile2Stmt);
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getServiceLifecycleMethods().contains(currentMethod.getSubSignature())){
						JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
						conditionCounter++;
						JNopStmt thenStmt = new JNopStmt();
						JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
						body.getUnits().add(ifStmt);
						JNopStmt elseStmt = new JNopStmt();
						JGotoStmt elseGoto = new JGotoStmt(elseStmt);
						body.getUnits().add(elseGoto);
						
						body.getUnits().add(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);

						body.getUnits().add(elseStmt);
					}
				}
				body.getUnits().add(endWhile2Stmt);
				createIfStmt(startWhile2Stmt);
				
				//onRebind:
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONREBIND, currentClass, entryPoints, classLocal);
				createIfStmt(beforemethodsStmt);
				//onUnbind:
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONUNBIND, currentClass, entryPoints, classLocal);
				createIfStmt(onBindStmt);
				//lifecycle2 end
				
				//onDestroy:
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONDESTROY, currentClass, entryPoints, classLocal);
				
				//either begin or end or next class:
				createIfStmt(onCreateStmt);
				createIfStmt(endClassStmt);
				
			}
			if(broadcastReceiver){
				List<String> entryPoints = entry.getValue();
				JNopStmt onReceiveStmt = searchAndBuildMethod(AndroidEntryPointConstants.BROADCAST_ONRECEIVE, currentClass, entryPoints, classLocal);
				//methods
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getBroadcastLifecycleMethods().contains(currentMethod.getSubSignature())){
						JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
						conditionCounter++;
						JNopStmt thenStmt = new JNopStmt();
						JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
						body.getUnits().add(ifStmt);
						JNopStmt elseStmt = new JNopStmt();
						JGotoStmt elseGoto = new JGotoStmt(elseStmt);
						body.getUnits().add(elseGoto);
						
						body.getUnits().add(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);

						body.getUnits().add(elseStmt);
					}
				}
				body.getUnits().add(endWhileStmt);
				createIfStmt(startWhileStmt);
				
				createIfStmt(onReceiveStmt);
				
			}
			if(contentProvider){
				List<String> entryPoints = entry.getValue();
				JNopStmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.CONTENTPROVIDER_ONCREATE, currentClass, entryPoints, classLocal);
				//TODO: which methods?
				// see: http://developer.android.com/reference/android/content/ContentProvider.html
				//methods
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getContentproviderLifecycleMethods().contains(currentMethod.getSubSignature())){
						JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
						conditionCounter++;
						JNopStmt thenStmt = new JNopStmt();
						JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
						body.getUnits().add(ifStmt);
						JNopStmt elseStmt = new JNopStmt();
						JGotoStmt elseGoto = new JGotoStmt(elseStmt);
						body.getUnits().add(elseGoto);
						
						body.getUnits().add(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);

						body.getUnits().add(elseStmt);
					}
				}
				body.getUnits().add(endWhileStmt);
				createIfStmt(startWhileStmt);
				
				createIfStmt(onCreateStmt);
				
			}
			if(plain){
				for(SootMethod currentMethod : plainMethods.values()){
					if (!currentMethod.isStatic() && classLocal == null) {
						System.out.println("Skipping method " + currentMethod + " because we have no instance");
						continue;
					}
					
					JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
					conditionCounter++;
					JNopStmt thenStmt = new JNopStmt();
					JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
					body.getUnits().add(ifStmt);
					JNopStmt elseStmt = new JNopStmt();
					JGotoStmt elseGoto = new JGotoStmt(elseStmt);
					body.getUnits().add(elseGoto);
						
					body.getUnits().add(thenStmt);
					buildMethodCall(currentMethod, body, classLocal, generator);
						
					createIfStmt(endClassStmt);
					body.getUnits().add(elseStmt);
				}
			}
			
			
			body.getUnits().add(endClassStmt);
		}
		
		
	
		
		return mainMethod;
	}
	
	private JNopStmt searchAndBuildMethod(String subsignature, SootClass currentClass, List<String> entryPoints, Local classLocal){
		if(currentClass.declaresMethod(subsignature)){
			entryPoints.remove("<"+ currentClass.toString()+ ": "+subsignature+">");
			return getMethodStmt(subsignature, currentClass, classLocal);
		}else{
			//look in history (at least the framework method itself will implement class:
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sclass : extendedClasses){
				if(sclass.declaresMethod(subsignature)){
					entryPoints.remove("<"+ currentClass.toString()+ ": "+subsignature+">");
					return getMethodStmt(subsignature, sclass, classLocal);
				}
			}		
		}
		//should not happen because customActivities extends the framework Activity
		return null;
	}
	
	private JNopStmt getMethodStmt(String signature, SootClass sclass, Local classLocal){
		SootMethod onMethod = sclass.getMethod(signature);
		assert onMethod.isStatic() || classLocal != null : "Class local was null for non-static method "
			+ signature + " in class " + sclass;
		
		JNopStmt onMethodStmt = new JNopStmt();
		body.getUnits().add(onMethodStmt);
			
		//write Method
		buildMethodCall(onMethod, body, classLocal, generator);
		return onMethodStmt;
	}
	
	private void createIfStmt(Unit target){
		if(target == null){
			return;
		}
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt ifStmt = new JIfStmt(cond, target);
		body.getUnits().add(ifStmt);
	}
	
	protected SootMethod findMethod(SootClass currentClass, String subsignature){
		if(currentClass.declaresMethod(subsignature)){
			return currentClass.getMethod(subsignature);
		}
		if(currentClass.hasSuperclass()){
			return findMethod(currentClass.getSuperclass(), subsignature);
		}
		return null;
	}
	

}

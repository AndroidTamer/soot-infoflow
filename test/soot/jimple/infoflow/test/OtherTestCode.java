package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithField;
import soot.jimple.infoflow.test.utilclasses.ClassWithField2;
import soot.jimple.infoflow.test.utilclasses.ClassWithFinal;
import soot.jimple.infoflow.test.utilclasses.ClassWithStatic;
import soot.jimple.infoflow.test.utilclasses.D1static;

public class OtherTestCode {

	public void testWithStaticInheritance(){
		D1static obj = new D1static(TelephonyManager.getDeviceId());
		obj.taintIt();
	}
	
	public void testWithFieldInheritance(){
		ClassWithField2 obj = new ClassWithField2(TelephonyManager.getDeviceId());
		obj.taintIt();
	}
	
	public void testWithField(){
		ClassWithField fclass = new ClassWithField();
		fclass.field = TelephonyManager.getDeviceId();
		
//		ClassWithField fclass2 = new ClassWithField();
//		fclass2.field = TelephonyManager.getDeviceId();
//		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(fclass.field);
		cm.publish(fclass.field);
	}
	
	public void staticTest(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithStatic static1 = new ClassWithStatic();
		static1.setTitle(tainted);
		ClassWithStatic static2 = new ClassWithStatic();
		String alsoTainted = static2.getTitle();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
		}
	
	public static void static2Test(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithStatic static1 = new ClassWithStatic();
		static1.setTitle(tainted);
		ClassWithStatic static2 = new ClassWithStatic();
		String alsoTainted = static2.getTitle();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
		}
	
	public void genericsfinalconstructorProblem(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithFinal<String> c0 = new ClassWithFinal<String>(tainted, false);
		String alsoTainted = c0.getString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
		
	}

	public void stringConcatTest(){
		String tainted = TelephonyManager.getDeviceId();
		String concat1 = tainted.concat("eins");
		String two = "zwei";
		String concat2 = two.concat(tainted);
		String concat3 = "test " + tainted;
		
		ConnectionManager cm = new ConnectionManager();
		//this way it does not work:
		cm.publish(concat1.concat(concat2).concat(concat3));
		//this way, it works:
//		cm.publish(concat1);
//		cm.publish(concat2);
//		cm.publish(concat3);
		
	}
	
	public void stringConcatTestSmall(){
		String tainted = TelephonyManager.getDeviceId();
		String two = "zwei";
		String one = two.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(one);
		
	}
	
	public void stringConcatTestSmall2(){
		String tainted = TelephonyManager.getDeviceId();
		//String two = "zwei";
		String one = tainted.concat("zwei").concat("eins");
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(one);
	
	}
	
	public void stringConcatTestSmall3(){
		String tainted = TelephonyManager.getDeviceId();
		String concat1 = tainted.concat("eins");
		String two = "zwei";
		String concat2 = two.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		//this way it does not work:
		cm.publish(concat1.concat(concat2).concat("foo"));
		//this way, it works:
//		cm.publish(concat1);
//		cm.publish(concat2);
//		cm.publish(concat3);
		
	}

	private String deviceId = "";
	
	public interface MyInterface {
		void doSomething();
	}
	
	public void innerClassTest() {
		this.deviceId = TelephonyManager.getDeviceId();
		runIt(new MyInterface() {
			
			@Override
			public void doSomething() {
				ConnectionManager cm = new ConnectionManager();
				cm.publish(deviceId);
			}
			
		});
	}
	
	private void runIt(MyInterface intf) {
		intf.doSomething();
	}
	
}

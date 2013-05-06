package soot.jimple.infoflow.test;

import java.util.LinkedList;
import java.util.List;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
/**
 * checks if taint is removed after overwriting the values (for both string and list<string>)
 * @author Christian
 *
 */
public class OverwriteTestCode {
	static String staticString;
	static List<String> staticList;
	
	public void varOverwrite(){
		String var;
		List<String> varList = new LinkedList<String>();
		String tainted = TelephonyManager.getDeviceId();
		var = tainted;
		varList.add(tainted);
		var = "123";
		varList = new LinkedList<String>();
		varList.add("123");
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(var);
		cm.publish(varList.get(0));
		
	}
	
	public void staticFieldOverwrite(){
		String tainted = TelephonyManager.getDeviceId();
		OverwriteTestCode.staticString = tainted;
		OverwriteTestCode.staticString = "123";
		OverwriteTestCode.staticList = new LinkedList<String>();
		OverwriteTestCode.staticList.add(tainted);
		OverwriteTestCode.staticList = new LinkedList<String>();
		OverwriteTestCode.staticList.add("123");
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(OverwriteTestCode.staticString);
		cm.publish(OverwriteTestCode.staticList.get(0));
	}
	
	public void fieldOverwrite(){
		String tainted = TelephonyManager.getDeviceId();
		Test1 t = new Test1();
		t.field = tainted;
		t.list.add(tainted);
		t.field = "123";
		t.list = new LinkedList<String>();
		t.list.add("123");
		ConnectionManager cm = new ConnectionManager();
		cm.publish(t.field);
		cm.publish(t.list.get(0));
	}
	
	public void returnOverwrite(){
		String tainted = TelephonyManager.getDeviceId();
		Test1 t = new Test1();
		t.field = tainted;
		t.field = t.testMethod();
		t.list.add(tainted);
		t.list = t.testMethodList();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(t.field);
		cm.publish(t.list.get(0));
	}
	
	public void returnOverwrite2(){
		String var;
		List<String> varList = new LinkedList<String>();
		String tainted = TelephonyManager.getDeviceId();
		Test1 t = new Test1();
		var = tainted;
		var = t.testMethod();
		varList.add(tainted);
		varList = t.testMethodList();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(var);
		cm.publish(varList.get(0));
	}
	
	public void returnOverwrite3(){
		String tainted = TelephonyManager.getDeviceId();
		Test1 t = new Test1();
		OverwriteTestCode.staticString = tainted;
		OverwriteTestCode.staticString = t.testMethod();
		OverwriteTestCode.staticList = new LinkedList<String>();
		OverwriteTestCode.staticList.add(tainted);
		OverwriteTestCode.staticList = t.testMethodList();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(OverwriteTestCode.staticString);
		cm.publish(OverwriteTestCode.staticList.get(0));
	}
	
	public void returnOverwrite4(){
		String tainted = TelephonyManager.getDeviceId();
		tainted = null;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(tainted);
	}

	public class Test1{
		String field;
		LinkedList<String> list = new LinkedList<String>();
		
		public String testMethod(){
			return "123";
		}
		
		public LinkedList<String> testMethodList(){
			LinkedList<String> result = new LinkedList<String>();
			result.add("123");
			return result;
		}
		
	}
	
	private String data = "";
	
	private void setData(String data) {
		this.data = data;
	}

	public void returnOverwrite5(){
		setData(TelephonyManager.getDeviceId());
		ConnectionManager cm = new ConnectionManager();
		cm.publish(data);
		setData(null);
		cm.publish(data);
	}

}

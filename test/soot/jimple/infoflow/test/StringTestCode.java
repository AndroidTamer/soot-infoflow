package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class StringTestCode {
	
	public void methodStringConcat1(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		
		String result = pre.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringConcat1b(){
		String var = "2";
		String tainted = TelephonyManager.getDeviceId();
		
		var = var.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(var);
	}
	
	public void methodStringConcat1c(String var){
		String tainted = TelephonyManager.getDeviceId();
		
		String result = var.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringConcat2(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		String post = tainted.concat(pre);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(post);
	}
	
	public void methodStringConcatPlus(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		
		String result = pre + tainted;
		String post =  tainted + pre;
		
		result.concat(post);
	}
	
	public void methodValueOf(){
		String tainted = TelephonyManager.getDeviceId();
		
		String result = String.valueOf(tainted);
		String result2 = tainted.toString();
		result.concat(result2);
	}
	
	public void methodStringBuilder1(){
		
		StringBuilder sb = new StringBuilder(TelephonyManager.getDeviceId());
		//sb.append("123");
		String test = sb.toString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
		
	}
public void methodStringBuilder2(){
		
		StringBuilder sb = new StringBuilder();
		sb.append(TelephonyManager.getDeviceId());
		String test = sb.toString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
		
	}

}

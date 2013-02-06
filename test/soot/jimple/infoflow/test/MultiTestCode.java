package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;
import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class MultiTestCode {
	
	public void multiSourceCode(){
		String tainted = TelephonyManager.getDeviceId();
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
				
		ConnectionManager cm = new ConnectionManager();
		cm.publish(tainted);
		doSomething(pwd);
	}
	
	private void doSomething(String msg) {
		ConnectionManager cm = new ConnectionManager();
		cm.publish(msg);
	}
	
	private String pwd;

	public void multiSourceCode2(){
		AccountManager am = new AccountManager();
		this.pwd = am.getPassword();
				
		String tainted = TelephonyManager.getDeviceId();

		ConnectionManager cm = new ConnectionManager();
		cm.publish(tainted);
		doSomething();
	}

	private void doSomething() {
		ConnectionManager cm = new ConnectionManager();
		cm.publish(this.pwd);
	}

	public void ifPathTestCode1(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
		
		String foo = "";
		String bar = "";
		if (pwd.length() > 0)
			foo = pwd;
		else
			bar = pwd;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
		cm.publish(bar);
	}

	public void ifPathTestCode2(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
		
		String foo = "";
		if (pwd.length() > 0)
			foo = pwd;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void ifPathTestCode3(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
		
		String foo = pwd;
		if (pwd.length() > 0)
			foo = "";
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void ifPathTestCode4(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
		String bar = am.getPassword();
		
		String foo = pwd;
		if (pwd.length() > 0)
			foo = bar;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void loopPathTestCode1(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();	
		sendPwd(pwd, 5);
	}
	
	private void sendPwd(String pwd, int cnt) {
		ConnectionManager cm = new ConnectionManager();
		cm.publish(pwd);
		if (cnt > 0)
			sendPwd(pwd, cnt - 1);
	}

	public void overwriteTestCode1(){
		AccountManager am = new AccountManager();
		String pwd = am.getPassword();
		System.out.println(pwd);
		
		pwd = new String("");
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(pwd);
	}

}

package soot.jimple.infoflow.test;

public class InFunctionCode {
	
	public String infSourceCode1(String secret) {
		return secret;
	}

	public String infSourceCode2(String foo) {
		String secret = "Hello World";
		return secret;
	}

	public String infSourceCode3(String foo) {
		String secret = copy(foo);
		return secret;
	}
	
	private String copy(String bar) {
		System.out.println("bar");
		return bar;
	}
	
}

package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class BasicTests extends JUnitTests {

	/**
	 * custom test to check whether there is an edge to string methods or not on different 
	 * configurations
	 */
	@Test
	public void basic0() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic0: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}
	
	@Test
	public void basic1() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}
	
	@Test
	public void basic2() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic3() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic4() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic4: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic5() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic5: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic6() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic6: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic7() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic7: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic8() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic8: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic9() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic9: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic10() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic10: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic11() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic11: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic12() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic12: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic13() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic13: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic14() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic14: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic15() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic15: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic16() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic16: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic17() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic17: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic18() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic18: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic19() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic19: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic20() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic20: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic21() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic21: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic22() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic22: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic23() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic23: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic24() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic24: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic25() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic25: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic26() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic26: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic27() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic27: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic28() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic28: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic29() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic29: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic30() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic30: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic31() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic31: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic32() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic32: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic33() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic33: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic34() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic34: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic35() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic35: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic36() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic36: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic37() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic37: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic38() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic38: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic39() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic39: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	}

	@Test
	public void basic40() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic40: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic41() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic41: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}

	@Test
	public void basic42() {
	Infoflow infoflow = initInfoflow();
	List<String> epoints = new ArrayList<String>();
	epoints.add("<securibench.micro.basic.Basic42: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
	infoflow.computeInfoflow(path, entryPointCreator, epoints, sources, sinks);
	checkInfoflow(infoflow);
	//TODO: this test fails at the moment!
	}


}

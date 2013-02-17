package soot.jimple.infoflow.test.securibench;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class ArrayTests extends JUnitTests {

	@Test
	public void arrays1() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays1: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");	
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}

	@Test
	public void arrays2() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays2: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays3() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays3: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays4() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays4: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays5() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays5: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays6() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays6: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays7() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays7: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays8() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays8: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays9() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays9: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
	
	@Test
	public void arrays10() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<securibench.micro.arrays.Arrays10: void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}
}

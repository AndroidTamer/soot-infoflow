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
package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

/**
 * Test class for implicit flows
 * @author Steven Arzt 
 */
public class ImplicitFlowTests extends JUnitTests {
	
	@Test
	public void simpleTest(){
		Infoflow infoflow = initInfoflow();
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void simpleTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}
	  
	@Test
	public void simpleNegativeTest(){
		Infoflow infoflow = initInfoflow();
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void simpleNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);	
	}

	@Test
	public void simpleOverwriteTest(){
		Infoflow infoflow = initInfoflow();
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void simpleOverwriteTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void switchTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void switchTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void convertTest(){
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);
		infoflow.setInspectSinks(false);
		
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void convertTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
	}

	@Test
	public void sinkTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void sinkTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void returnTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void returnTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void callTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void callTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void callTest2(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void callTest2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void negativeCallTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void negativeCallTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);	
	}

	@Test
	public void recursionTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void recursionTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void recursionTest2(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void recursionTest2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void exceptionTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void exceptionTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void exceptionTest2(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void exceptionTest2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void exceptionTest3(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void exceptionTest3()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void fieldTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void fieldTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test
	public void staticFieldTest(){
		Infoflow infoflow = initInfoflow();
		infoflow.setInspectSinks(false);
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.ImplicitFlowTestCode: void staticFieldTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

}

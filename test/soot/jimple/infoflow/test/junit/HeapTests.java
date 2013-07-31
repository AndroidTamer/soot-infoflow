package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.test.utilclasses.TestWrapper;
/**
 * tests aliasing of heap references
 */
public class HeapTests extends JUnitTests {
	
	@Test
	public void testForEarlyTermination(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForEarlyTermination()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	@Test
	public void testForLoop(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForLoop()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	
	@Test
	public void testForWrapper(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForWrapper()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
	}
	
	@Test
    public void simpleTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void simpleTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test
    public void argumentTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void argumentTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test
    public void negativeTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void negativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test
    public void doubleCallTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void doubleCallTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	//TODO: this test fails and describes the problem discussed earlier,
	//similar to the following one:
	// a = x;
	// sink(a.f);
	// x.f= taint();
	// 
	@Test
    public void heapTest0(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodTest0()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	  @Test
	    public void heapTest1(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodTest1()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	  
	  @Test
	    public void testExample1(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ForwardBackwardTest: void testMethod()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	  
	    @Test
	    public void testReturn(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodReturn()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	    
	    @Test
	    public void testTwoLevels(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void twoLevelTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }
	    
	    @Test
	    public void multiAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void multiAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test
	    public void overwriteAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void overwriteAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }
	    
	    @Test
	    public void arrayAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void arrayAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

}

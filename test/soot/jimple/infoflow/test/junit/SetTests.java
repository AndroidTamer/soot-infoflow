package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * test taint propagation in sets
 */
public class SetTests extends JUnitTests {
    
    @Test
    public void concreteHashSetTest(){
    	System.out.println("Running test case concreteHashSetTest...");
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadHashTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		System.out.println("Test case concreteHashSetTest done.");
    }
    
    @Test
    @Ignore		// implicit flow
    public void containsTest(){
    	System.out.println("Running test case containsTest...");
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void containsTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		System.out.println("Test case containsTest done.");
    }
    
    @Test
    public void concreteTreeSetPos0Test(){
    	System.out.println("Running test case concreteTreeSetPos0Test...");
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(2);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadTreePos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		System.out.println("Test case concreteTreeSetPos0Test done.");
    }
    
    @Test
    public void concreteTreeSetPos1Test(){
    	System.out.println("Running test case concreteTreeSetPos1Test...");
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(2);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadTreePos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		System.out.println("Test case concreteTreeSetPos1Test done.");
    }
    
    @Test
    public void concreteLinkedSetPos0Test(){
    	System.out.println("Running test case concreteLinkedSetPos0Test...");
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(2);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadLinkedPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		System.out.println("Test case concreteLinkedSetPos0Test done.");
    }
    
    @Test
    public void concreteLinkedSetPos1Test(){
    	System.out.println("Running test case concreteLinkedSetPos1Test...");
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(2);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadLinkedPos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		System.out.println("Test case concreteLinkedSetPos1Test done.");
    }
    
    @Test
    public void setTest(){
    	System.out.println("Running test case setTest...");
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void writeReadTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		System.out.println("Test case setTest done.");
    }
    
    @Test
    public void setIteratorTest(){
    	System.out.println("Running test case setIteratorTest...");
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void iteratorTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);		
		System.out.println("Test case setIteratorTest done.");
    }
    
    @Test
    public void concreteNegativeTest(){
    	System.out.println("Running test case concreteNegativeTest...");
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(2);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		System.out.println("Test case concreteNegativeTest done.");
    }
}

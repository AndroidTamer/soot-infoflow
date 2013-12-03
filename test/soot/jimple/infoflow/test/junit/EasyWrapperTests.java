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

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

/**
 * Test for the {@link EasyTaintWrapper} class
 * 
 * @author Steven Arzt
 */
public class EasyWrapperTests extends JUnitTests {
	
	private final EasyTaintWrapper easyWrapper;
	
	public EasyWrapperTests() throws IOException {
		easyWrapper = new EasyTaintWrapper(new File("EasyTaintWrapperSource.txt"));
	}

	@Test(timeout=300000)
    public void equalsTest(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAlwaysModelEqualsHashCode(true);
		
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void equalsTest()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test(timeout=300000)
    public void hashCodeTest(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAlwaysModelEqualsHashCode(true);
		
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void hashCodeTest()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }

	@Test(timeout=300000)
    public void equalsTest2(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAlwaysModelEqualsHashCode(true);

		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void equalsTest2()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

	@Test(timeout=300000)
    public void hashCodeTest2(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAlwaysModelEqualsHashCode(true);
		
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void hashCodeTest2()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

	@Test(timeout=300000)
    public void getConstantTest(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAggressiveMode(true);
		
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void constantTest1()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

	@Test(timeout=300000)
    public void getConstantTest2(){
		EasyTaintWrapper wrapper = easyWrapper.clone();
		wrapper.setAggressiveMode(false);
		
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyWrapperTestCode: void constantTest1()>");
    	infoflow.setTaintWrapper(wrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }

}

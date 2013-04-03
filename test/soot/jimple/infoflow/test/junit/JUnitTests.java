package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.InfoflowResults;
import soot.jimple.infoflow.config.SootConfigForTest;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

public abstract class JUnitTests {


    protected static String path;
    
    protected static List<String> sinks;
    protected static final String sink = "<soot.jimple.infoflow.test.android.ConnectionManager: void publish(java.lang.String)>";
    protected static final String sinkInt = "<soot.jimple.infoflow.test.android.ConnectionManager: void publish(int)>";

    protected static List<String> sources;
    protected static final String sourceDeviceId = "<soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>";
    protected static final String sourcePwd = "<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>";
    protected static final String sourceUserData = "<soot.jimple.infoflow.test.android.AccountManager: java.lang.String[] getUserData(java.lang.String)>";
   	
    protected static boolean taintWrapper = false;
   
    @BeforeClass
    public static void setUp() throws IOException
    {
    	File f = new File(".");
    	path = System.getProperty("java.home")+ File.separator + "lib"+File.separator + "rt.jar"+ System.getProperty("path.separator") +
    			f.getCanonicalPath() + File.separator + "bin";
    	
        sources = new ArrayList<String>();
        sources.add(sourcePwd);
        sources.add(sourceUserData);
        sources.add(sourceDeviceId);
        
        sinks = new ArrayList<String>();
        sinks.add(sink);
        sinks.add(sinkInt);
    }
    
    @Before
    public void resetSootAndStream() throws IOException{
    	 soot.G.reset();
    	 System.gc();
    	 
    }
    
    protected void checkInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				InfoflowResults map = infoflow.getResults();
				assertTrue(map.containsSinkMethod(sink) || map.containsSinkMethod(sinkInt));
				assertTrue(map.isPathBetweenMethods(sink, sourceDeviceId)
						|| map.isPathBetweenMethods(sinkInt, sourceDeviceId));
			}else{
				fail("result is not available");
			}
	  }
    
    protected void negativeCheckInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				InfoflowResults map = infoflow.getResults();
				assertFalse(map.containsSinkMethod(sink));
				assertFalse(map.containsSinkMethod(sinkInt));
			}else{
				fail("result is not available");
			}
	  }
    
    protected Infoflow initInfoflow(){
    	Infoflow result = new Infoflow();
    	SootConfigForTest testConfig = new SootConfigForTest();
    	result.setSootConfig(testConfig);
    	if(taintWrapper){
    		EasyTaintWrapper easyWrapper;
			try {
				easyWrapper = new EasyTaintWrapper(new File("EasyTaintWrapperSource.txt"));
				result.setTaintWrapper(easyWrapper);
			} catch (IOException e) {
				System.err.println("Could not initialized Taintwrapper:");
				e.printStackTrace();
			}
    		
    	}
    	return result;
    }
    
}
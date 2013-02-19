package soot.jimple.infoflow.test.securibench;

import java.util.LinkedList;
import java.util.List;

import soot.jimple.infoflow.config.IInfoflowSootConfig;
import soot.options.Options;

public class SootConfigSecuriBench implements IInfoflowSootConfig{

	@Override
	public void setSootOptions(Options options) {
		// explicitly include packages for shorter runtime:
		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.");
		includeList.add("java.util.");
		includeList.add("java.io.");
		includeList.add("sun.misc.");
		includeList.add("java.net.");
		includeList.add("org.apache.http.");
		includeList.add("de.test.");
		includeList.add("soot.");
		includeList.add("securibench.");
		includeList.add("javax.servlet.");
		options.set_include(includeList);
		options.set_output_format(Options.output_format_none);
	}

}

package soot.jimple.infoflow.util;

public class ArgBuilder {
	/**
	 * build the arguments
	 * at the moment this is build: -w -p cg.spark on -cp . -pp [className]
	 * @param input
	 * @return
	 */
	public String[] buildArgs(String path, String className){
		String[] result = {
			"-w",
			"-no-bodies-for-excluded",
//			"-allow-phantom-refs",
			"-include",
			"java.util",
			"-include",
			"java.lang",
			"-p",
			"cg.spark",
			"on",
//			"-p",
//			"cg.spark",
//			"dump-html",//"verbose:true",
			"-cp",
			path,//or ".\\bin",
			"-pp",
			className,
			"-p",
			"jb",
			"use-original-names:true",
		};
		
		return result;
	}
	
	/**TODO
	 * -android-jars F:\master\android-platforms\platforms
		-src-prec apk
		-cp F:\master\QueryContacts.apk -pp
		com.appsolut.example.queryContacts.MainActivity
	*/

}

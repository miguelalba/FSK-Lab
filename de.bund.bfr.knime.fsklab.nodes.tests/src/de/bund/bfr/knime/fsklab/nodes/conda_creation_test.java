package de.bund.bfr.knime.fsklab.nodes;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import org.junit.Test;

public class conda_creation_test {

	@Test
	public static void createConda() {
		//ProcessBuilder builder = new ProcessBuilder("/home/thschuel/miniconda3/bin/conda", "env", "create","-f","/home/thschuel/FSK_Development/py3_conda2.yml" );
		ProcessBuilder builder = new ProcessBuilder("/home/thschuel/miniconda3/bin/conda", "env", "create","-f","/home/thschuel/FSK_Development/r_conda.yml" );
	//new ProcessBuilder("conda env create -f /home/thschuel/FSK_Development/py3_conda2.yml");
		Process process;
		try {
			builder.redirectOutput(Redirect.INHERIT);
			builder.redirectError(Redirect.INHERIT);
			process = builder.start();
			
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

	}
	public static void main(String[] args) {
		createConda();
	}

}

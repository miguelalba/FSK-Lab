package de.bund.bfr.knime.fsklab.nodes;

import static org.junit.Assert.*;

import org.junit.Test;

public class PythonScriptHandlerTest {

	@Test
	public void test() {
		ScriptHandler handler = ScriptHandlerFactory.createHandler("py");
		assertTrue(handler.getStdOut().isEmpty());
	}

}

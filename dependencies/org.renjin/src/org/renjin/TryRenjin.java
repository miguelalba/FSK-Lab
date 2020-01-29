package org.renjin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.aether.ConsoleRepositoryListener;
import org.renjin.aether.ConsoleTransferListener;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

// TODO: Example class. To be removed.
public class TryRenjin {

	private static final String HOST = "";
	private static final Integer PORT = 0;

	public static void main(String[] args) throws Exception {

		Properties systemProperties = System.getProperties();
		systemProperties.setProperty("http.proxyHost", HOST);
		systemProperties.setProperty("http.proxyPort", PORT.toString());
		systemProperties.setProperty("https.proxyHost", HOST);
		systemProperties.setProperty("https.proxyPort", PORT.toString());

		final List<RemoteRepository> repositories = new ArrayList<>();
		repositories.add(addProxy(AetherFactory.mavenCentral()));
		repositories.add(addProxy(AetherFactory.renjinRepo()));

		AetherPackageLoader loader = new AetherPackageLoader(TryRenjin.class.getClassLoader(), repositories);
        loader.setRepositoryListener(new ConsoleRepositoryListener(System.out));
        loader.setTransferListener(new ConsoleTransferListener(System.out));

		Session session = new SessionBuilder().withDefaultPackages().setPackageLoader(loader).build();
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		RenjinScriptEngine engine = factory.getScriptEngine(session);

		// ... put your Java code here ..
		engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
		engine.eval("print(df)");
		engine.eval("print(lm(y ~ x, df))");

		engine.eval("library(mc2d)");
	}

	private static RemoteRepository addProxy(RemoteRepository repo) {
		RemoteRepository.Builder builder = new RemoteRepository.Builder(repo);
		builder.setProxy(new Proxy("https", HOST, PORT));
		return builder.build();
	}
}

package de.bund.bfr.knime.fsklab.preferences;

public class DefaultProxyProvider implements ProxyProvider {

	private final String m_host;
	private final int m_port;
	
	public DefaultProxyProvider(String host, int port) {
		m_host = host;
		m_port = port;
	}
	
	@Override
	public String getHost() {
		return m_host;
	}
	
	@Override
	public int getPort() {
		return m_port;
	}
}

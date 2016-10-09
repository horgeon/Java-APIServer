package fr.horgeon.apiserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTTPHandlers {
	private Map<String, HTTPHandler> list;

	public HTTPHandlers() {
		this.list = new HashMap<>();
	}

	public Set<Map.Entry<String, HTTPHandler>> entries() {
		return this.list.entrySet();
	}

	public void register( String path, HTTPHandler handler ) {
		this.list.put( path, handler );
	}
}
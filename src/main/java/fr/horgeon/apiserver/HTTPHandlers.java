package fr.horgeon.apiserver;

import com.sun.net.httpserver.HttpContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTTPHandlers {
	private Map<String, HTTPHandler> list;
	private Map<String, HttpContext> contexts;

	public HTTPHandlers() {
		this.list = new HashMap<>();
		this.contexts = new HashMap<>();
	}

	public Set<Map.Entry<String, HTTPHandler>> entries() {
		return this.list.entrySet();
	}

	public void register( String path, HTTPHandler handler ) {
		this.list.put( path, handler );
	}

	public void unregister( String path ) {
		this.list.remove( path );
	}

	public void registerContext( String path, HttpContext context ) {
		this.contexts.put( path, context );
	}

	public HttpContext getContext( String path ) {
		return this.contexts.get( path );
	}

	public void unregisterContext( String path ) {
		this.contexts.remove( path );
	}
}
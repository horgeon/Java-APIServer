package fr.horgeon.apiserver;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HTTPServer {
	private HttpServer server;
	private HTTPHandlers handlers;
	private ExecutorService threadPool;
	private boolean started = false;

	public Integer port;

	public HTTPServer( Integer port ) throws Exception {
		if( port == null )
			throw new NullPointerException( "No port specified!" );

		if( port < 1 || port > 65535 )
			throw new NullPointerException( "Invalid port specified!" );

		this.port = port;

		this.server = HttpServer.create( new InetSocketAddress( port ), 0 );

		this.handlers = new HTTPHandlers();
	}

	public void registerHandler( String path, HTTPHandler handler ) {
		this.handlers.register( path, handler );
		this.handlers.registerContext( path, this.server.createContext( path, handler ) );
	}

	public void unregisterHandler( String path ) {
		if( this.server != null )
			this.server.removeContext( this.handlers.getContext( path ) );
		this.handlers.unregisterContext( path );
		this.handlers.unregister( path );
	}

	public void setKeys( Map<String, String> keys ) {
		System.out.println( "WARNING: Public and private keys changed!" );

		for( Map.Entry<String, HTTPHandler> entry : handlers.entries() ) {
			entry.getValue().setKeys( keys );
		}
	}

	public void start() throws Exception {
		this.threadPool = Executors.newFixedThreadPool( 1 );
		this.server.setExecutor( this.threadPool );

		this.server.start();

		this.started = true;
	}

	public void stop() throws Exception {
		this.server.stop( 1 );

		this.threadPool.shutdown();
		this.threadPool.awaitTermination( 60, TimeUnit.SECONDS );

		this.started = false;
	}

	public void fullStop() throws Exception {
		for( Iterator<Map.Entry<String, HTTPHandler>> iterator = handlers.entries().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, HTTPHandler> entry = iterator.next();

			if( this.server != null )
				this.server.removeContext( this.handlers.getContext( entry.getKey() ) );
			this.handlers.unregisterContext( entry.getKey() );
		}

		this.handlers.clear();

		this.stop();
	}

	public void restart() throws Exception {
		this.stop();
		this.start();
	}

	public boolean isStarted() {
		return this.started;
	}
}
package fr.horgeon.apiserver;

import com.amazon.webservices.common.HMAC256;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class HTTPHandler implements HttpHandler {
	private Boolean auth;
	private String method;
	private String encoding;
	private Map<String, String> keys;

	public HTTPHandler( String method, String encoding, Boolean auth ) {
		this.method = method;
		this.auth = auth;
		this.encoding = encoding;
	}

	public void setKeys( Map<String, String> keys ) {
		this.keys = keys;
	}

	public void writeHTTP( HttpExchange t, Integer code, String body ) {
		System.out.println( String.format( "-> Response: %d %s", code, body ) );

		try {
			t.sendResponseHeaders( code, body.length() );
			OutputStream os = t.getResponseBody();
			os.write( body.getBytes() );
			os.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public void writeJSON( HttpExchange t, Integer code, Map<String, String> body ) {
		writeHTTP( t, code, JSONValue.toJSONString( body ) );
	}

	public void responseJSON( HttpExchange t, Integer code, Boolean success, String message ) {
		Map<String, String> body = new HashMap<>();

		body.put( "success", success.toString() );
		body.put( "message", message );
		writeJSON( t, code, body );
	}

	public void responseJSON( HttpExchange t, Boolean success, String message ) {
		responseJSON( t, ( success ? 200: 500 ), success, message );
	}

	public String signature( String privateKey, String method, String endpoint, String querystring ) throws SignatureException {
		if( endpoint == null )
			endpoint = "";
		if( querystring == null )
			querystring = "";

		return HMAC256.calculate( String.format( "%s\n%s\n%s", method, endpoint, querystring ), privateKey );
	}

	public Boolean handleAuth( HttpExchange t, String querystring ) {
		if( !t.getRequestHeaders().containsKey( "API-PublicKey" ) ) {
			responseJSON( t, 400, false, "Missing public key." );
			return false;
		}

		if( !t.getRequestHeaders().containsKey( "API-Signature" ) ) {
			responseJSON( t, 400, false, "Missing signature." );
			return false;
		}

		String publicKey = t.getRequestHeaders().getFirst( "API-PublicKey" );
		String signature = t.getRequestHeaders().getFirst( "API-Signature" );

		if( this.keys == null || !this.keys.containsKey( publicKey ) ) {
			responseJSON( t, 401, false, "Invalid public key." );
			return false;
		}

		String privateKey = this.keys.get( publicKey );
		String computed_signature;

		try {
			computed_signature = this.signature( privateKey, this.method, t.getRequestURI().getPath(), querystring );
		} catch( SignatureException e ) {
			e.printStackTrace();
			responseJSON( t, false, "Signature computation failed." );
			return false;
		}

		if( !computed_signature.equalsIgnoreCase( signature ) ) {
			responseJSON( t, 401, false, "Signatures does not match." );
			return false;
		}

		return true;
	}

	public void handle( HttpExchange t ) throws IOException {
		System.out.println( String.format( "- Request: %s %s", t.getRequestURI().getPath(), t.getRequestHeaders().values() ) );

		if( t.getRequestMethod().equalsIgnoreCase( this.method ) ) {
			if( this.auth ) {
				if( this.handleAuth( t, t.getRequestURI().getQuery() ) ) {
					List<NameValuePair> query = URLEncodedUtils.parse( t.getRequestURI(), encoding );
					handleRequest( t, query );
				}
			} else {
				List<NameValuePair> query = URLEncodedUtils.parse( t.getRequestURI(), encoding );
				handleRequest( t, query );
			}
		} else {
			writeHTTP( t, 404, "<h1>404 Not Found</h1>No context found for request" );
		}
	}

	public abstract void handleRequest( HttpExchange t, List<NameValuePair> query ) throws IOException;
}

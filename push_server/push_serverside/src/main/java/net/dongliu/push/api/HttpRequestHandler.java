package net.dongliu.push.api;

import java.io.IOException;
import java.net.Socket;

import net.dongliu.push.data.PushRequest;
import net.dongliu.push.data.PushResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 处理通过http api发送的push请求.
 * 
 * @author dongliu
 * 
 */
public class HttpRequestHandler implements Runnable {

	private Socket socket;
	private static ObjectMapper objectMapper = new ObjectMapper();
	private Logger logger = Logger.getLogger(this.getClass());
	private PushRequestHandler pushRequestHandler;

	public HttpRequestHandler(Socket socket) {
		this.socket = socket;
		this.pushRequestHandler = new PushRequestHandler();
	}

	@Override
	public void run() {
		HttpParams params = new BasicHttpParams();
		// use http core to manager http request.
		try (DefaultHttpServerConnection conn = new DefaultHttpServerConnection()) {
			conn.bind(socket, params);
			HttpRequest request = conn.receiveRequestHeader();
			PushResponse pushResponse;
			if (request instanceof HttpEntityEnclosingRequest) {
				conn.receiveRequestEntity((HttpEntityEnclosingRequest) request);
				HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
				if (entity != null) {
					String content = EntityUtils.toString(entity, "UTF-8");
					PushRequest pushRequest = objectMapper.readValue(content, PushRequest.class);

					switch (pushRequest.getType()) {
					case PushRequest.TYPE_QUERY:
						pushResponse = pushRequestHandler.onQuery(pushRequest);
						break;
					case PushRequest.TYPE_PUSH:
						pushResponse = pushRequestHandler.onPush(pushRequest);
						break;
					case PushRequest.TYPE_BROADCAST:
						pushResponse = pushRequestHandler.onBroadCast(pushRequest);
						break;
					default:
						pushResponse = new PushResponse();
						pushResponse.setCode(PushResponse.CODE_UNKNOW_TYPE);
						break;
					}
					EntityUtils.consume(entity);
				} else {
					pushResponse = new PushResponse();
					pushResponse.setCode(PushResponse.CODE_REQUEST_EMPTY);
				}
			} else {
				pushResponse = new PushResponse();
				pushResponse.setCode(PushResponse.CODE_REQUEST_EMPTY);
			}
			String result = objectMapper.writeValueAsString(pushResponse);
			HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
			response.setEntity(new StringEntity(result));
			conn.sendResponseHeader(response);
			conn.sendResponseEntity(response);
		} catch (IOException | HttpException e) {
			logger.warn("read http message error.", e);
		}
	}

}

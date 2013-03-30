package net.dongliu.push.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.dongliu.push.data.PushRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 测试发push消息api.
 *
 * @author dongliu
 *
 */
public class PushApiTest {

	/**
	 * @param args
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		PushRequest request = new PushRequest();
		request.setType(PushRequest.TYPE_PUSH);
		List<String> useridList = new ArrayList<String>();
		useridList.add("test");
		request.setUserids(useridList);
		request.setContent("这是一条push.");
		ObjectMapper mapper = new ObjectMapper();
		String s = mapper.writeValueAsString(request);

		HttpURLConnection con = (HttpURLConnection) new URL("http://127.0.0.1:9889").openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(s.getBytes("UTF-8"));
		os.flush();
		os.close();
		InputStream in = con.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		in.close();
		con.disconnect();
	}

}

package com.back.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class WeatherController {
    
    @PostMapping("/aaaaa")
    public HashMap<String, Object> weather(HttpServletRequest request, HttpServletResponse response){
    	HashMap<String, Object> rtn = new HashMap<String,Object>();
    	
    	//ex) conv = {lat: 35.907757, lng: 127.766922, x: 74, y: 91}
    	String x = request.getParameter("x");
    	String y = request.getParameter("y");
    	double lat = Double.parseDouble(request.getParameter("lat"));
    	double lng = Double.parseDouble(request.getParameter("lng"));
    	
    	System.out.println(x);
    	System.out.println(y);
    	System.out.println(lat);
    	System.out.println(lng);

    	String addr = coordToAddr(lat,lng);
    	
    	try {
			weatherApi(x,y);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    	rtn.put("addr", addr);
    	rtn.put("result", "ok");
    	
    	return rtn;
    	
    }
    
    
    // 카카오 api 호출 해서 주소 가져오는 매소드
    public static String coordToAddr(double lat , double lng) {
    	String url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x="+lng+"&y="+lat;
    	String addr = "";
    	try {
			addr = getJsonData(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return gerRegionAddress(addr);
    }
    
    
    // REST API로 통신하여 받은 JSON데이터 
    public static String getJsonData(String apiUrl) throws Exception{
    	
    	HttpURLConnection conn = null;
    	StringBuffer response = new StringBuffer();
    	
    	// 인증키
    	String auth ="KakaoAK " + "c52921122e1625fd1df2a2eba969a1d2";
    	// Url 설정
    	URL url = new URL(apiUrl);
    	
    	conn = (HttpURLConnection) url.openConnection();
 	
    	// Request 형식 설정
    	conn.setRequestMethod("GET");
    	conn.setRequestProperty("X-Requested-With", "crul");
    	conn.setRequestProperty("Authorization", auth);
   	
    	// Request에 JSON data 준비
    	conn.setDoOutput(true);
    	
    	//보내고 데이터 받기
    	int responseCode = conn.getResponseCode();
    	
    	if ( responseCode == 400) {
    		System.out.println("400:: 해당 명령을 실행할수없음");
    	}else if (responseCode == 401) {
    		System.out.println("401:: Authorization가 잘못됨");
    	}else if (responseCode == 500) {
    		System.out.println("500:: 서버 에러, 문의 필요");
    	}else {
    		Charset charset = Charset.forName("UTF-8");
    		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),charset));
    		String inputLine;
    		while((inputLine = br.readLine())!= null) {
    			response.append(inputLine);
    		}
    	}
    	
    	System.out.println(gerRegionAddress(response.toString()));
    	
    	return response.toString();
    }
    
    private static String gerRegionAddress (String jsonString) {
    	
    	String value = "";
    	
    	JSONObject jobj = (JSONObject) JSONValue.parse(jsonString);
    	JSONObject meta = (JSONObject) jobj.get("meta");
    	
    	long size = (long) meta.get("total_count");
    	
    	if(size>0) {
    		JSONArray jArray = (JSONArray) jobj.get("documents");
    		JSONObject subjobj = (JSONObject) jArray.get(0);
    		JSONObject roadAddress = (JSONObject) subjobj.get("road_address");
    		
    		if(roadAddress == null) {
    			JSONObject subsubjobj = (JSONObject) subjobj.get("address");
    			value = (String) subsubjobj.get("address_name");
    		}else {
    			value = (String) roadAddress.get("address_name");
    		}
    		
    		if( value.equals("") || value == null) {
    			subjobj = (JSONObject) jArray.get(1);
    			subjobj = (JSONObject) subjobj.get("address");
    			value = (String) subjobj.get("address_name");
    		}
    	}
    	return value;
    }
    
    private static String weatherApi(String x, String y) throws IOException {
    	String rtn = "";
    	
    	Date today = new Date();
    	SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyyMMdd");
    	
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=Ah9gIeZdtUkp3q9AMHmad%2BFS5KWFC2Y%2B%2BdjGRxmB7U1OGbpWJraVpM%2BxYIiB2yxxCt%2Bme8G8eR3i8rcrh89ZtQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(newDtFormat.format(today), "UTF-8")); /*‘21년 6월 28일 발표*/
        urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode("0500", "UTF-8")); /*06시 발표(정시단위) */
        urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(x, "UTF-8")); /*예보지점의 X 좌표값*/
        urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(y, "UTF-8")); /*예보지점의 Y 좌표값*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        // System.out.println(sb.toString());
        
        JSONObject tempData = (JSONObject) JSONValue.parse(sb.toString());
        
        System.out.println("asdlkansldanksld");
        
        System.out.println(tempData);
        
        
        System.out.println(tempData.get("response"));
        
        
        return rtn;
    }
}

package whu.websource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Helper extends TimerTask {
    public static int i = 0;

    private static String httpRequest(String requestUrl) {
        StringBuffer buffer = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConn = null;

        try {
            URL url = new URL(requestUrl);
            httpURLConn = (HttpURLConnection) url.openConnection();
            httpURLConn.setDoInput(true);
            httpURLConn.setRequestMethod("GET");

            inputStream = httpURLConn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            buffer = new StringBuffer();
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConn != null) {
                httpURLConn.disconnect();
            }
        }
        return buffer.toString();
    }

    private static String htmlFiter(String html) {
        StringBuffer buffer = new StringBuffer();
        String str1 = "";
        String str2 = "";
        buffer.append("Today:");
        Pattern p = Pattern.compile("(.*)(<li class=\'dn on\' data-dn=\'7dl\'>)(.*?)(</li>)(.*)");
        Matcher m = p.matcher(html);
        if (m.matches()) {
            str1 = m.group(3);
            p = Pattern.compile("(.*)(<h2>)(.*?)(</h2>)(.*)");
            m = p.matcher(str1);
            if (m.matches()) {
                str2 = m.group(3);
                buffer.append(str2);
                buffer.append("\nweather:");
            }
            //p = Pattern.compile()
        }
        return buffer.toString();
    }

    public void run() {
        String content = httpRequest("http://www.weather.com.cn/html/weather/101280101.shtml");
        System.out.print(content);
    }
}

public class DxyCrawler {

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask task = new Helper();

        timer.schedule(task, 2000, 5000);
    }
}

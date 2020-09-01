/**
 *
 */
package com.jack.dnscache.net;

import com.jack.dnscache.Tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * 轻量级的网络请求库
 * 备注: HTTPS不进行域名校验
 *
 * @version 1.0
 */
public class ApacheHttpClientNetworkRequests implements INetworkRequests {
    private static final int CONNECTION_TIMEOUT = 30 * 1000;

    /*
     * 得到图片字节流 数组大小
     */
    public static byte[] readStream(InputStream inStream) {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();

        } catch (Exception e) {
        }

        Tools.log("TAG", "outStream.toByteArray()=" + outStream.toByteArray());

        return outStream.toByteArray();
    }

    public static boolean upLoadFile(String url, File file) {
        boolean result = false;
        try {
            if (null == url || null == file || !file.exists() || file.length() < 1) {
                return false;
            }
            /**
             * 第一部分
             */
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            /**
             * 设置关键值
             */
            con.setRequestMethod(METHOD_POST); // 以Post方式提交表单，默认get方式
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false); // post方式不能使用缓存

            // 设置请求头信息
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");

            // 设置边界
            String BOUNDARY = "----------" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            // 请求正文信息

            // 第一部分：
            StringBuilder sb = new StringBuilder();
            sb.append("--"); // ////////必须多两道线
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
            sb.append("Content-Type:application/octet-stream\r\n\r\n");

            byte[] head = sb.toString().getBytes("utf-8");

            // 获得输出流
            OutputStream out = new DataOutputStream(con.getOutputStream());
            out.write(head);

            // 文件正文部分
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();

            // 结尾部分
            byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
            out.write(foot);
            out.flush();
            out.close();

            int statusCode = con.getResponseCode();
            if (statusCode == 200) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String requests(String url, String method) {
        return requests(url, "", method);
    }

    @Override
    public String requests(String url, String host, String method) {
        HashMap<String, String> map = null;
        if (host == null || host.equals("")) {
            map = null;
        } else {
            map = new HashMap<>();
            map.put("host", host);
        }

        return requests(url, map, method);
    }

    @Override
    public String requests(String url, HashMap<String, String> head, String method) {
        String result = null;
        BufferedReader reader = null;
        try {
            HttpURLConnection con = getHttpURLConnection(url, head, method);
            if (con.getResponseCode() == HttpsURLConnection.HTTP_OK)
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            if (reader != null)
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            result = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    @Override
    public byte[] requestsByteArr(String url, HashMap<String, String> head, String method) {
        byte[] result = null;
        try {
            HttpURLConnection con = getHttpURLConnection(url, head, method);
            if (con.getResponseCode() == 200)
                result = readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return result;
    }

    private HttpURLConnection getHttpURLConnection(String url, HashMap<String, String> head, String method) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        //https设置
//        trustAllHosts(con);
        /* 3. 设置请求参数等 */
        // 请求方式
        con.setRequestMethod(method);
        // 超时时间
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        // 设置是否输出(get请求用不到)
        if (!METHOD_GET.equalsIgnoreCase(method))
            con.setDoOutput(true);
        // 设置是否读入
        con.setDoInput(true);
        // 设置是否使用缓存
        con.setUseCaches(false);
        // 设置此 HttpURLConnection 实例是否应该自动执行 HTTP 重定向
        con.setInstanceFollowRedirects(true);

        //添加头部信息
        if (head != null) {
            for (Entry<String, String> entry : head.entrySet()) {
                Tools.log("TAG", "" + entry.getKey() + "  -  " + entry.getValue());
                con.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        // 连接
        con.connect();
        return con;
    }

    private void trustAllHosts(HttpsURLConnection con) {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }
        }};
        // 安装完全信任的信任管理器
        try {
            SSLContext sc = SSLContext.getInstance("SSL", "SunJSSE");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            con.setSSLSocketFactory(sc.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

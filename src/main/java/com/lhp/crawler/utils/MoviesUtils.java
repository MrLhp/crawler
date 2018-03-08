package com.lhp.crawler.utils;

import com.lhp.crawler.model.Movies;
import com.lhp.crawler.repository.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Date;

@Slf4j
@Component
public class MoviesUtils {
    @Autowired
    MoviesRepository moviesRepository;

    SystemConfig systemConfig = new SystemConfig();

    public String downloadImg(String src, String code, String title)  {
        try {
            URL url = new URL("http:"+src);
            InetSocketAddress addr = new InetSocketAddress("forward.xdaili.cn", 80);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection(proxy);

            int timestamp = (int) (new Date().getTime() / 1000);
            String headerKey = "Proxy-Authorization";
            String authHeader = authHeader(systemConfig.getProperty("proxy.orderno"), systemConfig.getProperty("proxy.secret"), timestamp);

            conn.setRequestProperty(headerKey,authHeader);
            //设置超时间为5秒
            conn.setConnectTimeout(5000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //conn.setRequestProperty("Cookie", Constans.CookieByOld);
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            //文件保存位置
            File saveDir = new File(SystemConfig.getProperty("img.dir"));
            if(!saveDir.exists()){
                saveDir.mkdir();
            }

            File file = new File(saveDir+File.separator+code+".jpg");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if(fos!=null){
                fos.close();
            }
            if(inputStream!=null){
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "-1";
        }
        MoviesUtils.log.info(String.format("《%s-%s》下载成功",code,title));
        return "0";
    }

    private static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static String parseMillisecone(long millisecond) {
        String time = null;
        try {
            long yushu_day = millisecond % (1000 * 60 * 60 * 24);
            long yushu_hour = (millisecond % (1000 * 60 * 60 * 24))
                    % (1000 * 60 * 60);
            long yushu_minute = millisecond % (1000 * 60 * 60 * 24)
                    % (1000 * 60 * 60) % (1000 * 60);
            @SuppressWarnings("unused")
            long yushu_second = millisecond % (1000 * 60 * 60 * 24)
                    % (1000 * 60 * 60) % (1000 * 60) % 1000;
            if (yushu_day == 0) {
                return (millisecond / (1000 * 60 * 60 * 24)) + "天";
            } else {
                if (yushu_hour == 0) {
                    return (millisecond / (1000 * 60 * 60 * 24)) + "天"
                            + (yushu_day / (1000 * 60 * 60)) + "时";
                } else {
                    if (yushu_minute == 0) {
                        return (millisecond / (1000 * 60 * 60 * 24)) + "天"
                                + (yushu_day / (1000 * 60 * 60)) + "时"
                                + (yushu_hour / (1000 * 60)) + "分";
                    } else {
                        return (millisecond / (1000 * 60 * 60 * 24)) + "天"
                                + (yushu_day / (1000 * 60 * 60)) + "时"
                                + (yushu_hour / (1000 * 60)) + "分"
                                + (yushu_minute / 1000) + "秒";

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public static String authHeader(String orderno, String secret, int timestamp) {
        //拼装签名字符串
        String planText = String.format("orderno=%s,secret=%s,timestamp=%d", orderno, secret, timestamp);

        //计算签名
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(planText).toUpperCase();

        //拼装请求头Proxy-Authorization的值
        String authHeader = String.format("sign=%s&orderno=%s&timestamp=%d", sign, orderno, timestamp);
        return authHeader;
    }
}

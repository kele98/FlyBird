package top.aikele;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import top.aikele.entities.Address;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//程序主入口
public class Client {
    //注册中心的列表
    private static ArrayList<Address> list = new ArrayList<>();
    public static void main(String[] args) {
        //1开启ServerSocket服务
        openService();
        heartBeat();
        //开启UI
        UI();
    }
    public static void sendFile(Address address,String fileName){
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        DataOutputStream dataOutputStream =null;
        try {
            Socket socket = new Socket(address.getIp(),Integer.parseInt(address.getPort()));
             outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            //获取文件名长度
            byte[] bytes = fileName.getBytes(StandardCharsets.UTF_8);
            if(bytes.length>Short.MAX_VALUE){
                System.out.println("长度超出范围,请修改文件名");
            }
            short length = (short) bytes.length;

            //传输文件名长度2个字节
            dataOutputStream.writeShort(length);
            dataOutputStream.flush();
            //传输文件名
            dataOutputStream.write(bytes);
            dataOutputStream.flush();
            //传输文件 获取文件内容并发送
            File file = new File(fileName);
            //文件的大小
            long totalSpace = file.length();
            //已经发送文件的大小
            long sendSpace = 0 ;
            fileInputStream = new FileInputStream(file);
            byte[] content = new byte[1024];
            int len = 0;
            //文件传输时开始时的时间
            Long startTime =System.currentTimeMillis();
            Long nowTime = null;
            Long useTime = null;
            while ((len=fileInputStream.read(content))!=-1){
                dataOutputStream.write(content,0,len);
                nowTime=System.currentTimeMillis();
                useTime = nowTime-startTime;
                sendSpace+=len;
                System.out.println("--------------------------------------------------------------");
                System.out.println("已耗时"+(Math.rint(useTime/1000)));
                System.out.println("传输速度为:"+Math.rint(sendSpace/((useTime/1000)+1)/1024)+"KB/s");
                System.out.println("已传输："+sendSpace/1024+"KB");
                System.out.println("总大小:"+totalSpace/1024+"KB");
                System.out.println("--------------------------------------------------------------");
                System.out.println((sendSpace/1024) + (totalSpace/1024));
                double p = Double.parseDouble(String.format("%.2f", ((double) (sendSpace / 1024) / (double)(totalSpace / 1024))*100));
                for (int i = 0; i < 20; i++) {
                    if (i < p/5)
                        System.out.print("***");
                    else
                        System.out.print("---");
                }
                System.out.println(p + "%");

            }
            //dataOutputStream.write("这是文件里的内容".getBytes(StandardCharsets.UTF_8));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fileInputStream!=null)
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(outputStream!=null)
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(dataOutputStream!=null)
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void UI(){
        Scanner scanner = new Scanner(System.in);
        int choose;
        System.out.println("0--------退出程序");
        System.out.println("1--------查看列表");
        System.out.println("2--------发送文件");
        while ((choose = Integer.parseInt(scanner.next()))!=0){
            switch (choose){
                case 1 : {
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println(i+"----"+list.get(i).toString());
                    }
                }break;
                case 2: {
                    System.out.println("选择要发送的目标:");
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println(i+"----"+list.get(i).toString());
                    }
                    int target = scanner.nextInt();
                    if(target<0||target>list.size()){
                        System.out.println("选择无效");
                    }else {
                        System.out.println("输入要发送的文件名:");
                        String s = scanner.next();
                        sendFile(list.get(target),s);
                    }
                }break;
                default:
                    System.out.println("输入错误，请重新输入！");

            }
            System.out.println("0--------退出程序");
            System.out.println("1--------查看列表");
            System.out.println("2--------发送文件");
        };
        System.exit(0);
    }
    //刷新
    public static void heartBeat(){
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //2将自身注册进服务中
                register();
                //3获取list并选择
                getList();
            }
        };
        executorService.scheduleWithFixedDelay(runnable,0,15, TimeUnit.SECONDS);
    }
    //将服务注册进去
    public static void register(){
        //获取ip地址
        String ip = "http://pv.sohu.com/cityjson?ie=utf-8";
        String inputLine = "";
        String read = "";
        String toIp="";
        try {
            URL url = new URL(ip);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((read = in.readLine()) != null) {
                inputLine += read;
            }
            String ObjJson=inputLine.substring(inputLine.indexOf("=")+1,inputLine.length()-1);
            JSONObject jsonObj= JSON.parseObject(ObjJson);
            toIp=jsonObj.getString("cip");
        } catch (Exception e) {
            toIp="";
            System.out.println("获取ip地址失败");

        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost("http://a.aikele.top:8080/set");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(2000) //服务器响应超时时间
                    .setConnectTimeout(2000) //连接服务器超时时间
                    .build();
            httpPost.setConfig(requestConfig);
            //主机名
            String name = InetAddress.getLocalHost().getHostName();
            StringEntity entity = new StringEntity("{\n" +
                    "\"name\":\""+name+"\",\n" +
                    "\"ip\":\""+toIp+"\",\n" +
                    "\"port\":\"21821\",\n" +
                    "\"time\":30"+
                    "}", "utf-8");//也可以直接使用JSONObject
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");
            // 由客户端执行(发送)请求
            response = httpClient.execute(httpPost);
            //System.out.println("响应状态为:" + response.getStatusLine());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //获取注册的服务列表
    public static void getList(){
        CloseableHttpClient httpClient1 = HttpClientBuilder.create().build();
        CloseableHttpResponse response1 = null;
        try {
            HttpPost httpPost = new HttpPost("http://a.aikele.top:8080/get");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(2000) //服务器响应超时时间
                    .setConnectTimeout(2000) //连接服务器超时时间
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");
            // 由客户端执行(发送)请求
            response1 = httpClient1.execute(httpPost);
            //System.out.println("响应状态为:" + response1.getStatusLine());
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response1.getEntity();
            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity);
                //System.out.println("响应内容为:" + result);
                JSONObject jsonObject = JSONObject.parseObject(result);
                ArrayList<Address> tempList = new ArrayList<>();
                //主机名
                String name = InetAddress.getLocalHost().getHostName();
                for (String s : jsonObject.keySet()) {
                    if(!s.equals(name))
                    tempList.add( jsonObject.getObject(s,Address.class));
                }
                Client.list = tempList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient1 != null) {
                    httpClient1.close();
                }
                if (response1 != null) {
                    response1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void openService(){
        new Thread(){
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    serverSocket = new ServerSocket(21821);
                    while (true){
                        //未接受到请求是会阻塞的
                        Socket s = serverSocket.accept();
                        System.out.println("客户端:"+s.getInetAddress().getLocalHost()+"已连接到服务器");
                        inputStream = s.getInputStream();
                        //文件名长度
                        byte[] nameLen = new byte[2];
                        int readCount = 0 ;
                        while (readCount<2){
                            readCount += inputStream.read(nameLen,readCount,2-readCount);
                        }
                        //位操作还原short
                        //&0xff的原因是 在|操作时 两个被操作数会转为short(两个字节)类型 如果低位是负数 用补码表示时高位补1后进行|操作就会出现错误，&0xff是为了让高位舍去变为0 保证从8位升成16位时数据不变
                        short s1 = (short) ((nameLen[1]&0xFF)|(nameLen[0]<<8));
                        //文件名
                        byte[] name = new byte[s1];
                        readCount = 0;
                        while (readCount<name.length){
                            readCount += inputStream.read(name,readCount,name.length-readCount);
                        }
                        System.out.println("文件名:"+new String(name));
                        System.out.println("正在接收文件:"+new String(name));

                        //获取输出流
                        fileOutputStream = new FileOutputStream(new String(name));
                        System.out.println("-----------------------");
                        //文件内容
                        byte[] values = new byte[1024];
                        int len = 0;
                        while ( (len = inputStream.read(values))!=-1){
                            fileOutputStream.write(values,0,len);
                            //System.out.println(new String(values,0,len));
                        }
                        System.out.println("接收文件完成");
                        s.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if(serverSocket!=null)
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(inputStream!=null)
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(fileOutputStream!=null)
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
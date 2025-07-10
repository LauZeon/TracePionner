package com.example.suyuan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;
    private ScheduledExecutorService scheduler;


    private String productKey = "k21osZFDdgl";
    private String deviceName = "app_dev";
    private String deviceSecret = "c1af4b8aa0a8f29bd054e94d210dbd2a";

    private final String pub_topic = "/sys/k21osZFDdgl/app_dev/thing/event/property/post";
    private final String sub_topic = "/sys/k21osZFDdgl/app_dev/thing/service/property/set";

    private int temp =0;
    private int humi =0;
    private int pH = 7;
    private int CO2 = 0;
    private int O2 = 0;
    private int soil_humi = 0;
    private int soil_temp = 0;
    private int light_lux = 0;
    private int combustible_gas = 0;
    private int tank_height = 0;
    private int WiFi = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})

        Button btn_temp= findViewById(R.id.btn_temp);
        Button btn_humi= findViewById(R.id.btn_humi);
        Button btn_pH = findViewById(R.id.btn_pH);
        Button btn_CO2 = findViewById(R.id.btn_CO2);
        Button btn_O2 = findViewById(R.id.btn_O2);
        Button btn__combustible_gas = findViewById(R.id.btn_combustible_gas);
        Button btn_light_lux = findViewById(R.id.btn_light_lux);
        Button btn_WiFi = findViewById(R.id.btn_WiFi);
        Button btn_soil_humi = findViewById(R.id.btn_soil_humi);
        Button btn_soil_temp = findViewById(R.id.btn_soil_temp);
        Button btn_tank_height = findViewById(R.id.btn_tank_height);

//温度页面跳转*******************************

        Button button1 = findViewById(R.id.btn_temp);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, tempActivity.class);
                startActivity(intent);
            }
        });
//*****************************************


//湿度页面跳转*******************************

        Button button2 = findViewById(R.id.btn_humi);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, humiActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

//pH值页面跳转*******************************

        Button button3 = findViewById(R.id.btn_pH);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, pHActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

//剩余水量页面跳转*******************************

        Button button4 = findViewById(R.id.btn_tank_height);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, tank_heightActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

//土壤温度页面跳转*******************************

        Button button5 = findViewById(R.id.btn_soil_temp);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, soil_tempActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

//土壤湿度页面跳转*******************************

        Button button6 = findViewById(R.id.btn_soil_humi);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, soil_humiActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

//O2浓度页面跳转*******************************

        Button button7 = findViewById(R.id.btn_O2);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, O2_Activity.class);
                startActivity(intent);
            }
        });
//*****************************************

//CO2浓度页面跳转*******************************

        Button button8 = findViewById(R.id.btn_CO2);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CO2_Activity.class);
                startActivity(intent);
            }
        });
//*****************************************

//光照日志页面跳转*******************************

        Button button9 = findViewById(R.id.btn_light_lux);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Mlight_luxActivity2.class);
                startActivity(intent);
            }
        });
//*****************************************

//WiFi日志页面跳转*******************************

        Button button10 = findViewById(R.id.btn_WiFi);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WiFi_Activity.class);
                startActivity(intent);
            }
        });
//*****************************************

//可燃性气体页面跳转*******************************

        Button button11 = findViewById(R.id.btn_combustible_gas);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, combustible_gasActivity.class);
                startActivity(intent);
            }
        });
//*****************************************

        mqtt_init();
        start_reconnect();

        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传
                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        String message = msg.obj.toString();
                        Log.d("nicecode", "handleMessage: "+ message);
                        try {
                            JSONObject jsonObjectALL = null;
                            jsonObjectALL = new JSONObject(message);
                            JSONObject items = jsonObjectALL.getJSONObject("items");

                            JSONObject obj_temp = items.getJSONObject("temp");
                            JSONObject obj_humi = items.getJSONObject("humi");
                            JSONObject obj_pH = items.getJSONObject("pH");
                            JSONObject obj_CO2 = items.getJSONObject("CO2");
                            JSONObject obj_O2 = items.getJSONObject("O2");
                            JSONObject obj_soil_humi = items.getJSONObject("soil_humi");
                            JSONObject obj_soil_temp = items.getJSONObject("soil_temp");
                            JSONObject obj_light_lux = items.getJSONObject("light_lux");
                            JSONObject obj_combustible_gas = items.getJSONObject("combustible_gas");
                            JSONObject obj_tank_height = items.getJSONObject("tank_height");
                            JSONObject obj_WiFi = items.getJSONObject("WiFi");


                            temp = obj_temp.getInt("value");
                            humi = obj_humi.getInt("value");
                            pH = obj_pH.getInt("pH");
                            CO2 = obj_CO2.getInt("CO2");
                            O2 = obj_O2.getInt("O2");
                            soil_humi = obj_soil_humi.getInt("soil_humi");
                            soil_temp = obj_soil_temp.getInt("soil_temp");
                            light_lux = obj_light_lux.getInt("light_lux");
                            combustible_gas = obj_combustible_gas.getInt("combustible_gas");
                            tank_height = obj_tank_height.getInt("tank_height");
                            WiFi = obj_WiFi.getInt("WiFi");

                            Log.d("nicecode", "temp: "+ temp);
                            Log.d("nicecode", "humi: "+ humi);
                            Log.d("nicecode","pH" + pH);
                            Log.d("nicecode","CO2" + CO2);
                            Log.d("nicecode","O2" + O2);
                            Log.d("nicecode","soil_humi" + soil_humi);
                            Log.d("nicecode","soil_temp" + soil_temp);
                            Log.d("nicecode","light_lux" + light_lux);
                            Log.d("nicecode","combustible_gas" + combustible_gas);
                            Log.d("nicecode","tank_height" + tank_height);
                            Log.d("nicecode","WiFi" + WiFi);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            break;
                        }
                        break;
                    case 30:  //连接失败
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        try {
                            client.subscribe(sub_topic, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void mqtt_init() {
        try {

            String clientId = "a1MoTKOqkVK.test_device1";
            Map<String, String> params = new HashMap<String, String>(16);
            params.put("productKey", productKey);
            params.put("deviceName", deviceName);
            params.put("clientId", clientId);
            String timestamp = String.valueOf(System.currentTimeMillis());
            params.put("timestamp", timestamp);
            // cn-shanghai
            String host_url ="tcp://"+ productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
            String client_id = clientId + "|securemode=2,signmethod=hmacsha1,timestamp=" + timestamp + "|";
            String user_name = deviceName + "&" + productKey;
            String password = com.example.suyuan.Aliyun.sign(params, deviceSecret, "hmacsha1");

            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            System.out.println(">>>" + host_url);
            System.out.println(">>>" + client_id);

            //connectMqtt(targetServer, mqttclientId, mqttUsername, mqttPassword);

            client = new MqttClient(host_url, client_id, new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(user_name);
            //设置连接的密码
            options.setPassword(password.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(60);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------" + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    //封装message包
                    msg.what = 3;   //收到消息标志位
                    msg.obj =message.toString();
                    //发送messge到handler
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!(client.isConnected()))  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        // 没有用到obj字段
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    // 没有用到obj字段
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    private void start_reconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void publish_message(String message) {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage mqtt_message = new MqttMessage();
        mqtt_message.setPayload(message.getBytes());
        try {
            client.publish(pub_topic, mqtt_message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
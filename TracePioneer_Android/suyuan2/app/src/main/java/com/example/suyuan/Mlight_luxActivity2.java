package com.example.suyuan;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mlight_luxActivity2 extends AppCompatActivity { //change

    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;
    private ScheduledExecutorService scheduler;


    private String productKey = "k21osZFDdgl";
    private String deviceName = "app_dev";
    private String deviceSecret = "c1af4b8aa0a8f29bd054e94d210dbd2a";

    private final String pub_topic = "/sys/k21osZFDdgl/app_dev/thing/event/property/post";
    private final String sub_topic = "/sys/k21osZFDdgl/app_dev/thing/service/property/set";


    private int light_lux= 0; //change

    //

    private LineChart lineChart; //折线图控件


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_lux2);//change

        // 初始化 TextView
        TextView tv_light_lux = findViewById(R.id.tv_light_lux);//change

        //隐藏系统默认标题
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //初始化控件
        lineChart = findViewById(R.id.lc6);//change
        initLineChart(lineChart);


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
                        Log.d("nicecode", "handleMessage: " + message);
                        try {
                            JSONObject jsonObjectALL = null;
                            jsonObjectALL = new JSONObject(message);
                            JSONObject items = jsonObjectALL.getJSONObject("items");
                            JSONObject obj_combustible_gas = items.getJSONObject("light_lux");//change

                            light_lux = obj_combustible_gas.getInt("light_lux");//change

                            tv_light_lux.setText(light_lux + "");//change

                            Log.d("nicecode", "soil_temp" + light_lux);//change
                            updateChartWithNewData(light_lux);//change
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 30:  //连接失败
                        Toast.makeText(Mlight_luxActivity2.this, "连接失败", Toast.LENGTH_SHORT).show();//change
                        break;
                    case 31:   //连接成功
                        Toast.makeText(Mlight_luxActivity2.this, "连接成功", Toast.LENGTH_SHORT).show();//change
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

    private void updateChartWithNewData(int newValue) {
        LineData data = lineChart.getData();
        if (data != null) {
            // 假设只更新第一个数据集（最高温度）
            LineDataSet dataSet = (LineDataSet) data.getDataSetByIndex(0);
            float xValue = dataSet.getEntryCount(); // 按数据点数量递增
            dataSet.addEntry(new Entry(xValue, newValue));
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate(); // 刷新图表
        }
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
            String host_url = "tcp://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
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
                    msg.obj = message.toString();
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


    /**
     * 初始化图表数据
     */
    private void initLineChart(LineChart lineChart) {
        lineChart.animateXY(2000, 2000); // 呈现动画
        Description description = new Description();
        description.setText(""); //自定义描述
        lineChart.setDescription(description);
        Legend legend = this.lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        setYAxis(lineChart);
        setXAxis(lineChart);
        setData(lineChart);

    }

    /**
     * 设置Y轴数据
     */
    private void setYAxis(LineChart lineChart) {
        YAxis yAxisLeft = lineChart.getAxisLeft();// 左边Y轴
        yAxisLeft.setDrawAxisLine(true); // 绘制Y轴
        yAxisLeft.setDrawLabels(true); // 绘制标签
        yAxisLeft.setAxisMaxValue(1000); // 设置Y轴最大值
        yAxisLeft.setAxisMinValue(0); // 设置Y轴最小值
        yAxisLeft.setGranularity(3f); // 设置间隔尺寸
        yAxisLeft.setTextColor(Color.WHITE); //设置颜色
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return (int) value + "lx";
            }
        });
        // 右侧Y轴
        lineChart.getAxisRight().setEnabled(false); // 不启用
    }

    /**
     * 设置X轴数据
     */
    private void setXAxis(LineChart lineChart) {
        // X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(true); // 不绘制X轴
        xAxis.setDrawLabels(true);//绘制标签
        xAxis.setDrawGridLines(true); // 不绘制网格线
        // 模拟X轴标签数据
        final String[] weekStrs = new String[]{"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00"};
        xAxis.setLabelCount(weekStrs.length); // 设置标签数量
        xAxis.setTextColor(Color.GREEN); // 文本颜色
        xAxis.setTextSize(15f); // 文本大小为18dp
        xAxis.setGranularity(1f); // 设置间隔尺寸
        // 使图表左右留出点空位
        xAxis.setAxisMinimum(-0.1f); // 设置X轴最小值
        //设置颜色
        xAxis.setTextColor(Color.WHITE);
        // 设置标签的显示格式
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekStrs[(int) value];
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 在底部显示
    }

    /**
     * 填充数据
     */
    private void setData(LineChart lineChart) {
        // 模拟数据1
        List<Entry> yVals1 = new ArrayList<>();
        float[] ys1 = new float[]{700f, 700f, 700f, 452f, 857f, 844f, 684f};
        // 模拟数据2
        List<Entry> yVals2 = new ArrayList<>();
        float[] ys2 = new float[]{700f, 700f, 700f, 452f, 857f, 844f, 684f};
        // 模拟数据3
        List<Entry> yVals3 = new ArrayList<>();
        float[] ys3 = new float[]{700f, 700f, 700f, 452f, 857f, 844f, 684f};
        for (int i = 0; i < ys1.length; i++) {
            yVals1.add(new Entry(i, ys1[i]));
            yVals2.add(new Entry(i, ys2[i]));
            yVals3.add(new Entry(i, ys3[i]));
        }
        // 2. 分别通过每一组Entry对象集合的数据创建折线数据集
        LineDataSet lineDataSet2 = new LineDataSet(yVals2, "光照强度");
        lineDataSet2.setCircleColor(Color.RED); //设置点圆的颜色
        lineDataSet2.setCircleRadius(5); //设置点圆的半径
        lineDataSet2.setDrawCircleHole(false); // 不绘制圆洞，即为实心圆点
        lineDataSet2.setColor(Color.RED); // 设置为红色
        // 值的字体大小为12dp
        lineDataSet2.setValueTextSize(12f);
        //将每一组折线数据集添加到折线数据中
        LineData lineData = new LineData( lineDataSet2);
        //设置颜色
        lineData.setValueTextColor(Color.WHITE);
        //将折线数据设置给图表
        lineChart.setData(lineData);
    }

}





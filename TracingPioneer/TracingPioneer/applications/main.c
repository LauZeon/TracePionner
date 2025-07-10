/*
 * Copyright (c) 2006-2021, RT-Thread Development Team
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <rtthread.h>
#include <rtdevice.h>
#include <board.h>
#include "main.h"
#include <stdio.h>
#include "sht35.h"
#include "sgp30.h"
#include "lcd_spi_130.h"
#include "hyw_lcd.h"

#define DBG_TAG "main"
#define DBG_LVL         DBG_LOG
#include <rtdbg.h>

/* 配置 LED 灯引脚 */
#define PIN_LED_B              GET_PIN(F, 11)      // PF11 :  LED_B        --> LED
#define PIN_LED_R              GET_PIN(F, 12)      // PF12 :  LED_R        --> LED
#define PIN_PUMP GET_PIN(C, 4) // 示例：PC4 控制水泵
#define PIN_LIGHT GET_PIN(C, 5) // 示例：PC5 控制生长灯

static rt_device_t rs485_dev = RT_NULL; // rs485串口设备句柄

// 线程间共享数据结构
struct sensor_data_t {
    float soil_hum[3];
    float soil_temp[3];
    float soil_ph[3];
    float air_temp;
    float air_hum;
    float co2;
    float tvoc;
    float light;
    float gas_ppm;
};
static struct sensor_data_t g_sensor_data;
static struct rt_mutex data_mutex;

// 1. 传感器采集线程
static void sensor_thread_entry(void *parameter)
{
    while (1)
    {
        rt_mutex_take(&data_mutex, RT_WAITING_FOREVER);
        // 采集土壤、空气、气体、光照等数据
         g_sensor_data.soil_hum[0] = soil_humidity_read(0);
         g_sensor_data.air_temp = SHT30_Read_Temp();
         g_sensor_data.co2 = sgp30_read_co2();
         g_sensor_data.light = adc_read_light();
        
        rt_mutex_release(&data_mutex);
        rt_thread_mdelay(200);
    }
}

// 2. 灯控制线程
static void light_thread_entry(void *parameter)
{
    while (1)
    {
        rt_pin_write(PIN_LIGHT, PIN_HIGH); // 开灯
        rt_thread_mdelay(1000);
        rt_pin_write(PIN_LIGHT, PIN_LOW);  // 关灯
        rt_thread_mdelay(1000);
    }
}

// 3. 水泵控制线程
static void pump_thread_entry(void *parameter)
{
    while (1)
    {
        rt_pin_write(PIN_PUMP, PIN_HIGH); // 开泵
        rt_thread_mdelay(500);
        rt_pin_write(PIN_PUMP, PIN_LOW);  // 关泵
        rt_thread_mdelay(1500);
    }
}

// 4. rs485数据传输线程
static void rs485_thread_entry(void *parameter)
{
    while (1)
    {
        rt_mutex_take(&data_mutex, RT_WAITING_FOREVER);
        // 打包g_sensor_data，通过串口发送到rs485
        rt_device_write(rs485_dev, 0, &g_sensor_data, sizeof(g_sensor_data));
        rt_mutex_release(&data_mutex);
        rt_thread_mdelay(500);
    }
}

int main(void)
{
    // 初始化互斥锁
    rt_mutex_init(&data_mutex, "data_mutex", RT_IPC_FLAG_FIFO);

    // 初始化外设引脚
    rt_pin_mode(PIN_LED_R, PIN_MODE_OUTPUT);
    rt_pin_mode(PIN_LIGHT, PIN_MODE_OUTPUT);
    rt_pin_mode(PIN_PUMP, PIN_MODE_OUTPUT);

    // 打开rs485串口设备（示例，需根据实际串口名修改）
    rs485_dev = rt_device_find("uart3");
    if (rs485_dev) rt_device_open(rs485_dev, RT_DEVICE_OFLAG_RDWR | RT_DEVICE_FLAG_INT_RX);

    // 创建线程
    rt_thread_t tid;
    tid = rt_thread_create("sensor", sensor_thread_entry, RT_NULL, 2048, 10, 20);
    if (tid) rt_thread_startup(tid);
    tid = rt_thread_create("light", light_thread_entry, RT_NULL, 1024, 12, 20);
    if (tid) rt_thread_startup(tid);
    tid = rt_thread_create("pump", pump_thread_entry, RT_NULL, 1024, 12, 20);
    if (tid) rt_thread_startup(tid);
    tid = rt_thread_create("rs485", rs485_thread_entry, RT_NULL, 1024, 14, 20);
    if (tid) rt_thread_startup(tid);

    LOG_I("RT-Thread 多线程环境初始化完成！");
    return 0;
}


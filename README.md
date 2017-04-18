前几天看到贝塞尔曲线的时候，想想用贝塞尔做些什么东西出来。很快一个电量显示的控件就马上登场了，先来看下效果图吧：

**充电中**

![充电中.gif](https://github.com/1002326270xc/BatteryView/blob/master/photos/充电中.gif)

**不在充电情况下，电量大于20%**

![不在充电情况下，电量大于20%.gif](https://github.com/1002326270xc/BatteryView/blob/master/photos/不在充电情况下，电量大于20%25.gif)

**不在充电情况下，并且电量在20%之下**

![不在充电情况下，并且电量在20%之下.gif](https://github.com/1002326270xc/BatteryView/blob/master/photos/不在充电情况下，并且电量在20%25之下.gif)

**充电情况下，并且电量充满了**

![充电情况下，并且电量充满了.gif](https://github.com/1002326270xc/BatteryView/blob/master/photos/充电情况下，并且电量充满了.gif)

看到效果图基本就这几种状态了，这几种状态里面首先分了两大类:充电中，未充电中；充电中又细分了充电到100%和未达到100%两种；未充电中又细分了电量低中和电量不在低中的两种。

原本想的是录个视频来模拟充电和未充电两种情况的，后来生成的gif一直是大于简书上传的要求，因此这里就上传多个gif了**(为了演示几种情况，代码中状态是写死的，因此大家看到的状态栏和控件显示的不一样)**。demo我都通过动态数据获取电量测试过了。

### 使用
- 属性部分：

| 属性名        | 类型           | 描述  |
| :------------- |:-------------| :-----|
| ring_stroke_width      | dimension | 外圈的宽度 |
| ring_radius      | dimension | 外圈的半径 |
| wave_width      | dimension      |   一个波浪的宽度 |
| wave_peek | dimension      |    波浪的峰值 |
| wave_cycle_time | integer      |    波浪走动的速度 |
| wave_color | color      |    波浪的颜色 |
| battery_status_color | color      |   充电状态文字颜色|
| battery_status_size | dimension      |   充电状态文字大小|
| battery_level_color | color      |   充电百分比文字颜色|
| battery_level_size | dimension      |   充电百分比文字大小|
| battery_status_size | dimension      |   充电文字状态颜色|
| battery_lowpower_color | color |   低电量下的文字闪动的颜色|
| battery_charging_text | string或reference |   正在充电的文字|
| battery_fill_text | string或reference |   充满的文字|
| battery_using_text | string或reference |   正在使用的文字|
| battery_lowpower_text | string或reference |  低电量的文字|
| battery_lowpower_percnet | fraction |  百分之多少时才显示低电量|

- 布局部分:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#12b3c9"
    android:gravity="center"
    android:orientation="vertical">

    <com.library.battery.BatteryView
        android:id="@+id/batteryView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:battery_lowpower_color="#d96d15"
        app:battery_lowpower_percnet="10%"
        app:ring_radius="@dimen/ring_radius"
        app:ring_stroke_width="@dimen/ring_stroke_width"
        app:wave_color="#3acf38"
        app:wave_cycle_time="1000"
        app:wave_peek="@dimen/wave_peek"
        app:wave_width="@dimen/wave_width" />

</LinearLayout>
```

布局中用到的属性不是全的，就拿了几个试试。想要看更多的属性自己添加吧。效果图如下:


![自定义属性simple.gif](https://github.com/1002326270xc/BatteryView/blob/master/photos/自定义属性simple.gif)
- 代码部分：
```java
public class MainActivity extends AppCompatActivity {
    BatteryView batteryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        batteryView = (BatteryView) findViewById(R.id.batteryView);
        batteryView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                batteryView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(new BatteryReceiver(), filter);
            }
        });
    }

    public void setBattery(BatteryStatus status) {
        Log.d("TAG", "status:" + status.status + ",level:" +status.level);
        batteryView.setChanges(status.status, status.level);
    }
}
```

切记:**这里调用BatteryView的setChange方法必须要在layout都加载完才能调用该方法。**

### 关于我:
**email:** a1002326270@163.com

**csdn:** http://blog.csdn.net/u010429219/article/details/64906203

**简书:** http://www.jianshu.com/p/c9cecd67e439

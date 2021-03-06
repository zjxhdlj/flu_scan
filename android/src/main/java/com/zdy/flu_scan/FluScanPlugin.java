package com.zdy.flu_scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FluScanPlugin */
public class FluScanPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private EventChannel eventChannel;
  private Context applicationContext;

  //定义广播名称
  private static final String ACTION_BAR_CODE = "nlscan.action.SCANNER_RESULT";
  private static final String BROADCAST_CHANNEL = "nlscan/send";

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flu_scan");
    channel.setMethodCallHandler(this);

    //eventChannel
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(),BROADCAST_CHANNEL);
    eventChannel.setStreamHandler(new EventChannel.StreamHandler() {

      private BroadcastReceiver scanResultReceiver;

      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        scanResultReceiver = createScanResultReceiver(events);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BAR_CODE);
        applicationContext.registerReceiver(scanResultReceiver,filter);
      }

      @Override
      public void onCancel(Object arguments) {
        applicationContext.unregisterReceiver(scanResultReceiver);
        scanResultReceiver = null;
      }
    });
    applicationContext = flutterPluginBinding.getApplicationContext();
  }

  private BroadcastReceiver createScanResultReceiver(final EventChannel.EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String codeResult1 = intent.getStringExtra("SCAN_BARCODE1");
        String codeResult2 = intent.getStringExtra("SCAN_BARCODE2");
        int codeType = intent.getIntExtra("SCAN_BARCODE_TYPE",-1);
        String barState = intent.getStringExtra("SCAN_STATE");

        System.out.println(codeResult1+"======"+codeResult2+"========"+codeType+"======="+barState);
        Map<String,Object> resultMap = new HashMap<>();

        if(barState.equals("ok")){
          resultMap.put("code1",codeResult1);
          resultMap.put("code2",codeResult2);
          resultMap.put("SCAN_BARCODE_TYPE",codeType);
          resultMap.put("SCAN_STATE",barState);
          JSONObject jsonObject = new JSONObject(resultMap);
          String json = jsonObject.toString();
          events.success(json);
        }else{
          events.error("100","扫码错误","请重新扫码");
        }
      }
    };
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flu_scan");
    channel.setMethodCallHandler(new FluScanPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}

import 'dart:async';

import 'package:flutter/services.dart';

class FluScan {
  static const MethodChannel _channel =
      const MethodChannel('flu_scan');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

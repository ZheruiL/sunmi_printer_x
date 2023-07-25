import 'package:flutter/services.dart';

class SunmiPrinter {
  static const _methodChannel = MethodChannel("sunmi_printer_x");
  static final SunmiPrinter _singleton = SunmiPrinter._internal();

  factory SunmiPrinter() {
    return _singleton;
  }

  SunmiPrinter._internal() {}

  initPrinter() async {
    await _methodChannel.invokeMethod("init");
  }

  Future<String> getCurrentPrinterInfo() async {
    return await _methodChannel.invokeMethod("getCurrentPrinterInfo");
  }

  printImgLabel(Uint8List pngBytes) async {
    return await _methodChannel.invokeMethod('printImgLabel', pngBytes);
  }

  printLabel1() async {
    return await _methodChannel.invokeMethod("printLabel1");
  }
}

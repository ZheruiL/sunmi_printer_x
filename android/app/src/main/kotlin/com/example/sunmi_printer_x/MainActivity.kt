package com.example.sunmi_printer_x

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.api.PrintResult
import com.sunmi.printerx.enums.HumanReadable
import com.sunmi.printerx.enums.ImageAlgorithm
import com.sunmi.printerx.enums.PrinterInfo
import com.sunmi.printerx.enums.Shape
import com.sunmi.printerx.style.AreaStyle
import com.sunmi.printerx.style.BarcodeStyle
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.BitmapStyle
import com.sunmi.printerx.style.TextStyle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

var selectPrinter: PrinterSdk.Printer? = null

class MainActivity : FlutterActivity() {
    private val channel = "sunmi_printer_x"
    var showPrinters = MutableLiveData<MutableList<PrinterSdk.Printer>?>()

    private var methodChannel: MethodChannel? = null
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        setupChannels(this, flutterEngine.dartExecutor.binaryMessenger)
    }

    override fun onDestroy() {
        teardownChannels()
        super.onDestroy()
    }

    private fun setupChannels(context: Context, messenger: BinaryMessenger) {
        methodChannel = MethodChannel(messenger, channel)
        methodChannel!!.setMethodCallHandler { call, result ->
            when (call.method) {
                // init printer, must be called before any other functions
                "init" -> {
                    PrinterSdk.getInstance().getPrinter(context, object : PrinterSdk.PrinterListen {
                        override fun onDefPrinter(printer: PrinterSdk.Printer?) {
                            if (selectPrinter == null) {
                                selectPrinter = printer
                            }
                        }

                        override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
                            showPrinters.postValue(printers)
                        }
                    })
                    result.success("ok1")
                }
                // get the current printer's information
                "getCurrentPrinterInfo" -> {
                    if (selectPrinter == null) {
                        result.error(
                            "1",
                            "no printer was selected, make sure there's a printer and 'init' has been called",
                            null
                        )
                    }
                    result.success(selectPrinter?.queryApi()?.getInfo(PrinterInfo.PAPER))
                }

                "printImgLabel" -> {
                    selectPrinter?.canvasApi()?.run {
                        initCanvas(BaseStyle.getStyle().setWidth(384).setHeight(220))
                        val option: BitmapFactory.Options = BitmapFactory.Options().apply {
                            inScaled = false
                        }
                        val bytes = call.arguments as ByteArray
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        renderBitmap(
                            bitmap,
                            BitmapStyle.getStyle().setAlgorithm(ImageAlgorithm.DITHERING).setPosX(0)
                                .setPosY(0).setWidth(384).setHeight(220)
                        )
                        printCanvas(1, object : PrintResult() {
                            override fun onResult(resultCode: Int, message: String?) {
                                if (resultCode == 0) {
                                    //打印完成
                                } else {
                                    //打印失败
                                    println(selectPrinter?.queryApi()?.status)
                                }
                            }
                        })
                    }
                }

                "printLabelTest" -> {
                    selectPrinter?.canvasApi()?.run {
                        initCanvas(BaseStyle.getStyle().setWidth(384).setHeight(220))
                        renderArea(
                            AreaStyle.getStyle().setStyle(Shape.BOX).setPosX(0).setPosY(0)
                                .setWidth(384).setHeight(219)
                        )
                        renderText(
                            "可口可乐(2L)",
                            TextStyle.getStyle().setTextSize(30).enableBold(true).setPosX(10)
                                .setPosY(20)
                        )
                        renderText(
                            "2L", TextStyle.getStyle().setTextSize(20).setPosX(10).setPosY(60)
                        )
                        renderText(
                            "200000", TextStyle.getStyle().setTextSize(20).setPosX(10).setPosY(85)
                        )
                        renderText(
                            "瓶", TextStyle.getStyle().setTextSize(24).setPosX(10).setPosY(130)
                        )
                        renderBarCode(
                            "12345678",
                            BarcodeStyle.getStyle().setPosX(200).setPosY(60)
                                .setReadable(HumanReadable.POS_TWO).setDotWidth(2).setBarHeight(60)
                                .setWidth(160)
                        )
                        renderText(
                            "￥ 7.8",
                            TextStyle.getStyle().setTextSize(16).setTextWidthRatio(1)
                                .setTextHeightRatio(1).enableBold(true).setPosX(190).setPosY(160)
                        )
                        printCanvas(1, object : PrintResult() {
                            override fun onResult(resultCode: Int, message: String?) {
                                if (resultCode == 0) {
                                    //打印完成
                                } else {
                                    //打印失败
                                    println(selectPrinter?.queryApi()?.status)
                                }
                            }
                        })
                    }
                    result.success("ok2")
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun teardownChannels() {
        PrinterSdk.getInstance().destroy();
        methodChannel!!.setMethodCallHandler(null)
    }
}

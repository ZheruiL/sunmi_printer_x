import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sunmi_printer_x/sunmi_printer.dart';
import 'package:http/http.dart' as http;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sunmi printer x demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Sunmi printer x demo'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('Print img'),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          try {
            final initRes = await SunmiPrinter().initPrinter();
            debugPrint('got printer:$initRes');
          } catch (e) {
            showDialog(
              context: context,
              builder: (context) => AlertDialog(
                title: Text(e.toString()),
              ),
            );
            return;
          }
          final printerInfo = await SunmiPrinter().getCurrentPrinterInfo();
          debugPrint(printerInfo);
          http.Response response = await http.get(
            Uri.parse(
                'https://upwork-usw2-prod-assets-static.s3.us-west-2.amazonaws.com/org-logo/425220847461273600'),
          );
          final Uint8List pngBytes = response.bodyBytes.buffer.asUint8List();
          await SunmiPrinter().printImgLabel(pngBytes);
        },
        tooltip: 'Print',
        child: const Icon(Icons.add),
      ),
    );
  }
}

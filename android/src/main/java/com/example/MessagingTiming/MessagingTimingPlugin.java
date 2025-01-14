package com.example.MessagingTiming;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryCodec;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;

import androidx.annotation.Keep;

/** MessagingTimingPlugin */

@Keep
public class MessagingTimingPlugin implements FlutterPlugin, MethodCallHandler {
  private static final String TAG = "MessagingTimingPlugin";
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private BasicMessageChannel<Object> basicMessageChannel;
  private BasicMessageChannel<ByteBuffer> basicMessageChannelBinary;

  @Override
  public void
  onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    setup(flutterPluginBinding.getFlutterEngine().getDartExecutor());
  }

  public static void registerWith(Registrar registrar) {
    MessagingTimingPlugin plugin = new MessagingTimingPlugin();
    plugin.setup(registrar.messenger());
  }

  private void setup(BinaryMessenger binaryMessenger) {
    channel = new MethodChannel(binaryMessenger, "MessagingTiming");
    channel.setMethodCallHandler(this);
    basicMessageChannelBinary = new BasicMessageChannel<ByteBuffer>(
        binaryMessenger, "BasicMessageChannelBinary", BinaryCodec.INSTANCE);
    basicMessageChannelBinary.setMessageHandler(
      new BasicMessageChannel.MessageHandler<ByteBuffer>() {
        public void onMessage(ByteBuffer message,
                              BasicMessageChannel.Reply<ByteBuffer> reply) {
          // TODO: Check message.
          try {
            ByteBuffer buffer =
                ByteBuffer.wrap(getPlatformVersion().getBytes("UTF-8"));
            reply.reply(buffer);
          } catch (Exception ex) {
            reply.reply(null);
          }
        }
      });
    basicMessageChannel = new BasicMessageChannel<Object>(
      binaryMessenger, "BasicMessageChannel", new StandardMessageCodec());
    basicMessageChannel.setMessageHandler(
      new BasicMessageChannel.MessageHandler<Object>() {
        public void onMessage(Object message,
                              BasicMessageChannel.Reply<Object> reply) {
          reply.reply(getPlatformVersion());
        }
      });
    Pigeon.Api.setup(binaryMessenger, new MyApi());
    try {
      Log.i(TAG, "load library");
      System.loadLibrary("native_add");
    } catch (Exception ex) {
      Log.i(TAG, ex.toString());
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success(getPlatformVersion());
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private class MyApi implements Pigeon.Api {
    public Pigeon.StringMessage getPlatformVersion(Pigeon.VoidMessage arg) {
      Pigeon.StringMessage result = new Pigeon.StringMessage();
      result.setMessage(MessagingTimingPlugin.getPlatformVersion());
      return result;
    }
  }

  public static String getPlatformVersion() {
    return "Android " + android.os.Build.VERSION.RELEASE;
  }

  public static String getPlatformVersionMainThread() {
    CompletableFuture<String> future = new CompletableFuture<String>();
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      public void run() {
        future.complete(getPlatformVersion());
      }
    });
    String result = null;
    try {
      result = future.get();
    } catch (Exception ex) {
      Log.i(TAG, ex.toString());
    } finally {
      return result;
    }
  }
}

# MessagingTiming

Originally written by Aaron Clarke of flutter team 2-3 years ago; recently I had to benchmark JNI latency on Android Flutter. This repo contains same benchmarks with some changes to run on current version of flutter (Flutter 2.10.4, Dart 2.16.2 as of this commit).

MessageChannel with BinaryCodec doesn't run anymore and an obsolete NDK (22.1.7171670) is required, in current state.

## Misc. Notes:

It seems

* FFI -> Android JNI with call happening on Main thread

* FFI UI Thread -> Android JNI with call happening on calling thread itself.


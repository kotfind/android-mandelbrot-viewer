{ pkgs, ... }:
let
    buildToolsVersion = "34.0.0";
    cmakeVersion = "3.10.2";

    androidComposition = pkgs.androidenv.composeAndroidPackages {
        cmdLineToolsVersion = "8.0";
        toolsVersion = "26.1.1";
        platformToolsVersion = "33.0.3";
        buildToolsVersions = [ buildToolsVersion ];
        includeEmulator = false;
        emulatorVersion = "30.3.4";
        platformVersions = [ "34" ];
        includeSources = false;
        includeSystemImages = false;
        # systemImageTypes = [ "google_apis_playstore" ];
        abiVersions = [ "arm64-v8a" ];
        cmakeVersions = [ cmakeVersion ];
        includeNDK = true;
        ndkVersions = ["22.0.7026061"];
        useGoogleAPIs = false;
        useGoogleTVAddOns = false;
        includeExtras = [];
    };

    sdk = "${androidComposition.androidsdk}/libexec/android-sdk";
in
(pkgs.buildFHSEnv {
    name = "Android Kotlin App";

    targetPkgs = pkgs: with pkgs; [
        gradle
    ];

    profile = let
            extraPath = pkgs.lib.concatMapStringsSep
                ":"
                (path: "${sdk}/${path}")
                [
                    "platform-tools"
                    "tools/bin"
                    "emulator"
                ];
        in
        ''
            export ANDROID_SDK_ROOT="${sdk}";
            export ANDROID_NDK_ROOT="${sdk}/ndk-bundle";
            export ANDROID_HOME="${sdk}";

            # export GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdk}/build-tools/${buildToolsVersion}/aapt2";

            export PATH="${extraPath}:$PATH"
        '';

    runScript = "bash";
}).env

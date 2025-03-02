{
  outputs = {nixpkgs, ...}: let
    system = "x86_64-linux";
    pkgs = import nixpkgs {
      inherit system;

      config = {
        allowUnfree = true;
        android_sdk.accept_license = true;
      };
    };
    lib = pkgs.lib;

    # To check available versions use
    #     sdkmanager --list
    # or
    #     just specify random one and check the error message
    cfg = {
      versions = {
        # SDK to Android version mapping:
        #     https://developer.android.com/tools/releases/platforms
        # Difference between compileSdk, targetSdk and minSdk:
        #     https://stackoverflow.com/a/47269079
        sdk = rec {
          compile = "34";
          target = "34";
          min = "30";

          # Setting it to compile-sdk is the easiest way, as compile-sdk
          # is the only one, that is installed anyway.
          emulator = compile;
        };

        # x86_64 allows running emulator on x86_64 machine,
        # but is likely incompable with your physical device
        abi = "x86_64";

        jvm-target = "1.8";

        # just set those to the last available version (I guess?)
        build-tools = "35.0.1";
        cmdline-tools = "9.0";
        platform-tools = "35.0.2";
        cmake = "3.31.4";
        emulator = "35.4.6";
        ndk = "22.1.7171670";
      };
      system-image-type = "default";
    };

    androidComposition = pkgs.androidenv.composeAndroidPackages (with cfg.versions; {
      cmdLineToolsVersion = cmdline-tools;
      toolsVersion = null;
      platformToolsVersion = platform-tools;
      buildToolsVersions = [build-tools];
      includeEmulator = true;
      emulatorVersion = emulator;
      # platformVersions seems to misbehave if two same versions are specified
      platformVersions =
        if sdk.compile == sdk.emulator
        then [sdk.compile]
        else [sdk.emulator sdk.compile];
      includeSources = false;
      includeSystemImages = true;
      systemImageTypes = [cfg.system-image-type];
      abiVersions = [abi];
      cmakeVersions = [cmake];
      includeNDK = true;
      ndkVersions = [ndk];
      useGoogleAPIs = false;
      useGoogleTVAddOns = false;
      includeExtras = [];
    });

    env = let
      sdkDir = "${androidComposition.androidsdk}/libexec/android-sdk";
      ndkDir = "${sdkDir}/ndk-bundle";

      cfgExportMap =
        lib.attrsets.mapAttrsRecursive
        (
          path: value: let
            envName =
              lib.strings.concatMapStringsSep "_" (
                pathFrag: builtins.replaceStrings ["-"] ["_"] (lib.strings.toUpper pathFrag)
              )
              path;
            envVal = lib.escapeShellArg (
              builtins.toString value
            );
          in "export ${envName}=${envVal}"
        )
        {inherit cfg;};

      cfgExportMapVals = lib.attrsets.collect builtins.isString cfgExportMap;

      cfgExport = lib.strings.concatStringsSep "\n" cfgExportMapVals;
    in
      /*
      bash
      */
      ''
        export ANDROID_HOME="${sdkDir}"
        export ANDROID_NDK_ROOT="${ndkDir}"
        export ANDROID_SDK_ROOT="${sdkDir}"

        export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkDir}/build-tools/${cfg.versions.build-tools}/aapt2"

        ${cfgExport}
      '';

    emulatorScript = let
      # Note: in `./.avd/device.avd/config.ini` set
      # `hw.keyboard` and `hw.mainKeys` to `yes`.
      # source: https://stackoverflow.com/a/64877532
      emulator = pkgs.androidenv.emulateApp ({
          name = "emulator";
          avdHomeDir = "./.avd";
        }
        // (with cfg.versions; {
          platformVersion = sdk.emulator;
          abiVersion = abi;
          systemImageType = cfg.system-image-type;
        }));
    in
      pkgs.writeShellScriptBin "emulator" ''
        ${env}
        ${lib.getExe' emulator "run-test-emulator"}
      '';

    fhsEnv =
      (pkgs.buildFHSEnv {
        name = "Android Development Environment";

        targetPkgs = pkgs:
          [
            pkgs.gradle
          ]
          ++ (with androidComposition; [
            androidsdk
            platform-tools
          ]);

        profile = env;

        runScript = "bash";
      })
      .env;

    runScript = let
      adb = lib.getExe' androidComposition.platform-tools "adb";
      awk = lib.getExe pkgs.gawk;
      gradle = lib.getExe pkgs.gradle;
      echo = lib.getExe' pkgs.toybox "echo";
    in
      pkgs.writeShellScriptBin "run" ''
        set -euo pipefail
        set -o

        ANDROID_SERIAL="$(${adb} devices | ${awk} "/emulator/ { print \$1; }")"
        export ANDROID_SERIAL

        if [ -z "$ANDROID_SERIAL" ]; then
          ${echo} "error: no emulators found"
          exit 1
        fi

        ${gradle} installDebug
        ${adb} shell am start -n com.kotfind.android_course/.MainActivity
      '';
  in {
    devShells.${system}.default = fhsEnv;

    apps.${system} = {
      emulator = {
        type = "app";
        program = lib.getExe emulatorScript;
      };

      run = {
        type = "app";
        program = lib.getExe runScript;
      };
    };
  };
}

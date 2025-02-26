{
    outputs = { nixpkgs, ... }:
        let
            system = "x86_64-linux";
            pkgs = import nixpkgs {
                inherit system;

                config = {
                    allowUnfree = true;
                    android_sdk.accept_license = true;
                };
            };
            lib = pkgs.lib;

            buildToolsVersion = "34.0.0";
            cmakeVersion = "3.10.2";
            platformVersion = "34";
            abiVersion = "x86_64";
            systemImageType = "default";

            androidComposition = pkgs.androidenv.composeAndroidPackages {
                cmdLineToolsVersion = "8.0";
                toolsVersion = "26.1.1";
                platformToolsVersion = "33.0.3";
                buildToolsVersions = [ buildToolsVersion ];
                includeEmulator = true;
                emulatorVersion = "34.2.16";
                platformVersions = [ platformVersion ];
                includeSources = false;
                includeSystemImages = true;
                systemImageTypes = [ systemImageType ];
                abiVersions = [ abiVersion];
                cmakeVersions = [ cmakeVersion ];
                includeNDK = true;
                ndkVersions = ["22.0.7026061"];
                useGoogleAPIs = false;
                useGoogleTVAddOns = false;
                includeExtras = [];
            };

            sdkDir = "${androidComposition.androidsdk}/libexec/android-sdk";
            env = /* bash */ ''
                export ANDROID_HOME="${sdkDir}"
                export ANDROID_NDK_ROOT="${sdkDir}/ndk-bundle"
                export ANDROID_SDK_ROOT="${sdkDir}"

                export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkDir}/build-tools/${buildToolsVersion}/aapt2"
            '';

            # Note: in `./.avd/device.avd/config.ini` set
            # `hw.keyboard` and `hw.mainKeys` to `yes`.
            # source: https://stackoverflow.com/a/64877532
            emulator = pkgs.androidenv.emulateApp {
                name = "emulator";
                inherit platformVersion abiVersion systemImageType;
                avdHomeDir = "./.avd";
            };

            emulatorScript = pkgs.writeShellScriptBin "emulator" ''
                ${env}
                ${lib.getExe' emulator "run-test-emulator"}
            '';

            fhsEnv = (pkgs.buildFHSEnv {
                name = "Android Development Environment";

                targetPkgs = pkgs:
                    ([
                        pkgs.gradle
                    ])
                    ++ (with androidComposition; [
                        build-tools
                        platform-tools
                        tools
                    ]);

                profile = env;

                runScript = "bash";
            }).env;

            adb = lib.getExe' androidComposition.platform-tools "adb";
            awk = lib.getExe pkgs.gawk;
            gradle = lib.getExe pkgs.gradle;

            runScript = pkgs.writeShellScriptBin "run" ''
                set -euo pipefail
                set -o

                ANDROID_SERIAL="$(${adb} devices | ${awk} '/emulator/ { print $1; }')"
                export ANDROID_SERIAL

                if [ -z "$ANDROID_SERIAL" ]; then
                    echo "error: no emulators are running"
                    exit 1
                fi

                ${gradle} installDebug
                ${adb} shell am start -n com.kotfind.android_course/.MainActivity
            '';
        in
        {
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

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

            extraPath = lib.concatMapStringsSep
                ":"
                (path: "${sdkDir}/${path}")
                [
                    "platform-tools"
                    "tools/bin"
                ];

            env = /* bash */ ''
                export ANDROID_HOME="${sdkDir}"
                export ANDROID_NDK_ROOT="${sdkDir}/ndk-bundle"
                export ANDROID_SDK_ROOT="${sdkDir}"

                export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkDir}/build-tools/${buildToolsVersion}/aapt2"

                export PATH="${extraPath}:$PATH"
            '';

            # Note: in `./.avd/device.avd/config.ini` set
            # `hw.keyboard` and `hw.mainKeys` to `yes`.
            # source: https://stackoverflow.com/a/64877532
            emu = pkgs.androidenv.emulateApp {
                name = "emu";
                inherit platformVersion abiVersion systemImageType;
                avdHomeDir = "./.avd";
            };

            app = pkgs.writeShellScriptBin "emulator-app" ''
                ${env}
                ${lib.getExe' emu "run-test-emulator"}
            '';
        in
        {
            devShells.${system}.default = (pkgs.buildFHSEnv {
                name = "Android Development Shell";

                targetPkgs = pkgs: with pkgs; [
                    gradle
                ];

                profile = env;

                runScript = "bash";
            }).env;

            apps.${system}.default = {
                type = "app";
                program = lib.getExe app;
            };
        };
}

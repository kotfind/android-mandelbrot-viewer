{
  pkgs,
  cfg,
  config,
  lib,
  ...
}: let
  inherit (pkgs) writeScriptBin;
  inherit (pkgs.androidenv) emulateApp;
  inherit (lib) getExe;

  emulatorPkg = emulateApp ({
      name = "emulator";

      androidAvdHome = "./.avd";
      androidUserHome = "./.android-user-home";

      # all options:
      #     https://developer.android.com/studio/run/emulator-commandline
      # androidEmulatorFlags = "";

      # all options:
      #     https://gist.github.com/emmarq/c35a81c17fffa94989ca3e6b6d4cb0f8
      configOptions = {
        "hw.camera.back" = "emulated";
        "hw.camera.front" = "emulated";

        "hw.lcd.width" = "320";
        "hw.lcd.height" = "640";
        "hw.lcd.density" = "160";

        "hw.keyboard" = "yes";
        "hw.mainKeys" = "yes";
      };
    }
    // (with cfg.versions; {
      platformVersion = sdk.emulator;
      abiVersion = abi;
      systemImageType = cfg.system-image-type;
    }));

  fhs = getExe config.fhs;
  emulator = getExe emulatorPkg;
  flakeRoot = getExe config.flakeRoot;

  emulatorScript = writeScriptBin "emulator" ''
    #! ${fhs}

    set -euo pipefail
    set -x

    cd "$(${flakeRoot})"

    pushd kotlin
    ${emulator}
    popd
  '';
in {
  scripts.emulator = emulatorScript;
}

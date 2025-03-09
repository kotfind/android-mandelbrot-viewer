{
  pkgs,
  cfg,
  config,
  lib,
  ...
}: let
  # Note: in `./.avd/device.avd/config.ini` set
  # `hw.keyboard` and `hw.mainKeys` to `yes`.
  # source: https://stackoverflow.com/a/64877532
  emulatorPkg = pkgs.androidenv.emulateApp ({
      name = "emulator";
      avdHomeDir = "./.avd";
    }
    // (with cfg.versions; {
      platformVersion = sdk.emulator;
      abiVersion = abi;
      systemImageType = cfg.system-image-type;
    }));

  fhs = lib.getExe config.fhs;
  emulator = lib.getExe' emulatorPkg "run-test-emulator";

  emulatorScript = pkgs.writeScriptBin "emulator" ''
    #! ${fhs}

    set -euo pipefail
    set -x

    ${emulator}
  '';
in {
  scripts.emulator = emulatorScript;
}

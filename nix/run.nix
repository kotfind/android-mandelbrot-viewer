{
  cfg,
  config,
  lib,
  pkgs,
  ...
}: let
  fullPackageName = with cfg.app; package + "." + flavor;

  firstLetterToUpper = str:
    lib.strings.toUpper (builtins.substring 0 1 str)
    + builtins.substring 1 ((builtins.stringLength str) - 1) str;

  gradleSubcommand = gradleAction: buildType:
    gradleAction
    + lib.escapeShellArg (firstLetterToUpper cfg.app.flavor)
    + lib.escapeShellArg (firstLetterToUpper buildType);

  adb = lib.getExe' config.androidComposition.platform-tools "adb";
  awk = lib.getExe pkgs.gawk;
  gradle = lib.getExe pkgs.gradle;
  echo = lib.getExe' pkgs.toybox "echo";
  head = lib.getExe' pkgs.toybox "head";
  tail = lib.getExe' pkgs.toybox "tail";
  boxes = lib.getExe pkgs.boxes;
  fhs = lib.getExe config.fhs;
  grep = lib.getExe pkgs.gnugrep;
  id = lib.getExe' pkgs.toybox "id";

  checkGroup = deviceType: let
    groupName =
      if deviceType == "device"
      then "adbuser"
      else if deviceType == "emulator"
      then "kvm"
      else throw "unreachable";

    script =
      if builtins.elem deviceType ["device" "emulator"]
      then
        /*
        bash
        */
        ''
          if [ "$(${id} -nG "$USER")" | ${grep} -wq '${groupName}' ]; then
            ${boxes} -d parchment <(echo ${
            lib.escapeShellArg
            ("WARNING:\n"
              + "current user is not in a group ${groupName}\n"
              + "did you forget to configure your system?")
          })
          fi
        ''
      else "";
  in
    script;

  awkDeviceNameFilter = deviceType:
    (
      if deviceType == "emulator"
      then ""
      else "!"
    )
    + "/emulator/";

  deviceNotFoundErrorMsg = deviceType: "error: no ${
    if deviceType == "emulator"
    then "emulators"
    else "devices"
  } found";

  setAndroidSerialCmd = deviceType:
    if deviceType == "emulator" || deviceType == "device"
    then
      /*
      bash
      */
      ''
        ANDROID_SERIAL="$( \
          ${adb} devices | \
          ${tail} -n +2 | \
          ${awk} '${awkDeviceNameFilter deviceType} { print $1; }' | \
          ${head} -n 1)"
        export ANDROID_SERIAL

        if [ -z "$ANDROID_SERIAL" ]; then
          ${echo} ${lib.escapeShellArg (deviceNotFoundErrorMsg deviceType)}
          exit 1
        fi
      ''
    else "";

  runCommand = doRun:
    if doRun
    then "${adb} shell monkey -p ${fullPackageName} -c android.intent.category.LAUNCHER 1"
    else "";

  genRunScript = {
    # name of generated script
    scriptName,
    # gradle subcommand like "install" or "assemble"
    gradleAction,
    # build type: "debug" or "release"
    buildType ? "debug",
    # whatever or not to run app after installation
    doRun ? true,
    # "emulator", "device" or "none"
    deviceType ? "emulator",
  }: let
    script = pkgs.writeScriptBin scriptName ''
      #! ${fhs}

      set -euo pipefail
      set -o

      ${checkGroup deviceType}
      ${setAndroidSerialCmd deviceType}
      ${gradle} ${gradleSubcommand gradleAction buildType}
      ${runCommand doRun}
    '';
  in (
    if !builtins.elem buildType ["debug" "release"]
    then throw "error: invalid build type"
    else if !builtins.elem deviceType ["emulator" "device" "none"]
    then throw "error: invalid device type"
    else script
  );
in {
  scripts = {
    run = genRunScript {
      scriptName = "run";
      gradleAction = "install";
      buildType = "debug";
      deviceType = "emulator";
    };
    install = genRunScript {
      scriptName = "install";
      gradleAction = "install";
      buildType = "debug";
      deviceType = "device";
    };
    assemble = genRunScript {
      scriptName = "assemble";
      gradleAction = "assemble";
      buildType = "debug";
      deviceType = "none";
      doRun = false;
    };
  };
}

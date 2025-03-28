{
  cfg,
  config,
  lib,
  pkgs,
  ...
}: let
  inherit (pkgs) writeScriptBin;
  inherit (lib) getExe getExe' escapeShellArg;
  inherit (lib.strings) toUpper;
  inherit (builtins) substring stringLength elem;

  fullPackageName = with cfg.app; package + "." + flavor;

  firstLetterToUpper = str:
    toUpper (substring 0 1 str)
    + substring 1 ((stringLength str) - 1) str;

  gradleSubcommand = gradleAction: buildType:
    gradleAction
    + escapeShellArg (firstLetterToUpper cfg.app.flavor)
    + escapeShellArg (firstLetterToUpper buildType);

  adb = getExe' config.androidComposition.platform-tools "adb";
  awk = getExe pkgs.gawk;
  gradle = getExe pkgs.gradle;
  echo = getExe' pkgs.toybox "echo";
  head = getExe' pkgs.toybox "head";
  tail = getExe' pkgs.toybox "tail";
  boxes = getExe pkgs.boxes;
  fhs = getExe config.fhs;
  grep = getExe pkgs.gnugrep;
  id = getExe' pkgs.toybox "id";
  flakeRoot = getExe config.flakeRoot;

  checkGroup = deviceType: let
    groupName =
      if deviceType == "device"
      then "adbusers"
      else if deviceType == "emulator"
      then "kvm"
      else throw "unreachable";

    script =
      if elem deviceType ["device" "emulator"]
      then
        /*
        bash
        */
        ''
          if ${id} -nG "$USER" | ${grep} -wq '${groupName}' ; then
            ${boxes} -d parchment <(echo ${
            escapeShellArg
            ("WARNING:\n"
              + "Current user is not in a group '${groupName}'.\n"
              + "Did you forget to configure your system?")
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
          ${echo} ${escapeShellArg (deviceNotFoundErrorMsg deviceType)}
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
    script = writeScriptBin scriptName ''
      #! ${fhs}

      set -euo pipefail
      set -o

      cd "$(${flakeRoot})"

      pushd kotlin
      ${checkGroup deviceType}
      ${setAndroidSerialCmd deviceType}
      ${gradle} ${gradleSubcommand gradleAction buildType}
      ${runCommand doRun}
      popd
    '';
  in (
    if !elem buildType ["debug" "release"]
    then throw "error: invalid build type"
    else if !elem deviceType ["emulator" "device" "none"]
    then throw "error: invalid device type"
    else script
  );
in {
  scripts = {
    run = genRunScript {
      scriptName = "run-android-app";
      gradleAction = "install";
      buildType = "debug";
      deviceType = "emulator";
    };
    install = genRunScript {
      scriptName = "install-android-app";
      gradleAction = "install";
      buildType = "debug";
      deviceType = "device";
    };
    assemble = genRunScript {
      scriptName = "assemble-android-app";
      gradleAction = "assemble";
      buildType = "debug";
      deviceType = "none";
      doRun = false;
    };
  };
}

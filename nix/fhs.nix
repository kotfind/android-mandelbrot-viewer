{
  cfg,
  pkgs,
  lib,
  config,
  ...
}: let
  sdkDir = "${config.androidComposition.androidsdk}/libexec/android-sdk";
  ndkDir = "${sdkDir}/ndk-bundle";

  # FIXME: scary code
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

  fhs = pkgs.buildFHSEnv {
    name = "android-development-environment";

    targetPkgs = pkgs:
      [
        pkgs.gradle
      ]
      ++ (with config.androidComposition; [
        androidsdk
        platform-tools
      ]);

    profile =
      /*
      bash
      */
      ''
        export ANDROID_HOME="${sdkDir}"
        export ANDROID_NDK_ROOT="${ndkDir}"
        export ANDROID_SDK_ROOT="${sdkDir}"

        export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkDir}/build-tools/${cfg.versions.build-tools}/aapt2"
        export GRADLE_USER_HOME="$(realpath .)/.gradle-home"

        ${cfgExport}
      '';

    runScript = "bash";
  };

  shell = fhs.env;
in {
  inherit fhs shell;
}

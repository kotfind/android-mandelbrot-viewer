{
  cfg,
  pkgs,
  lib,
  config,
  ...
}: let
  inherit (builtins) replaceStrings isAttrs;
  inherit (config) rustComposition;
  inherit (config.androidComposition) androidsdk platform-tools;
  inherit (lib) escapeShellArg;
  inherit (lib.attrsets) mapAttrsRecursive collect;
  inherit (lib.strings) concatStringsSep concatMapStringsSep toUpper;
  inherit (pkgs) buildFHSEnv;

  sdkDir = "${androidsdk}/libexec/android-sdk";
  ndkDir = "${sdkDir}/ndk-bundle";

  # Converts path (list of strings):
  #
  # All path segments are transformed as follows:
  #     * All letters are capsed
  #     * "_" are replaced with "__"
  #     * "-" are replaced with "_"
  # And then concatenated with "_"
  pathToEnvVarName = path:
    concatMapStringsSep
    "_"
    (
      seg:
        replaceStrings
        ["_" "-"]
        ["__" "_"]
        (toUpper seg)
    )
    path;

  pathValueToExport = path: value: "export ${pathToEnvVarName path}=${escapeShellArg (toString value)}";

  # Recursively maps attrs, and collects values into array.
  # Result of map function MUST NOT be attrs.
  # Map function's args are: path, value
  mapAttrsRecursiveToList = func: attrs: let
    mapped =
      mapAttrsRecursive
      (
        path: value: let
          res = func path value;
        in
          assert !isAttrs res; res
      )
      attrs;

    collected = collect (value: !isAttrs value) mapped;
  in
    collected;

  cfgExport =
    concatStringsSep
    "\n"
    (
      mapAttrsRecursiveToList
      pathValueToExport
      {inherit cfg;}
    );

  fhs = buildFHSEnv {
    name = "android-development-environment";

    targetPkgs = pkgs:
      [
        rustComposition
        androidsdk
        platform-tools
      ]
      ++ (with pkgs; [
        gradle
        python3
      ]);

    profile = ''
      export ANDROID_HOME="${sdkDir}"
      export ANDROID_SDK_ROOT="${sdkDir}"
      export ANDROID_NDK_ROOT="${ndkDir}"

      export GRADLE_OPTS="-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkDir}/build-tools/${cfg.versions.build-tools}/aapt2"
      export GRADLE_USER_HOME=".gradle-home"

      ${cfgExport}
    '';

    runScript = "bash";
  };

  shell = fhs.env;
in {
  inherit fhs shell;
}

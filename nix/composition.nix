{
  pkgs,
  cfg,
  ...
}: let
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
in {
  inherit androidComposition;
}

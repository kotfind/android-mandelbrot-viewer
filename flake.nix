{
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = {nixpkgs, ...}: let
    system = "x86_64-linux";
    pkgs = import nixpkgs {
      inherit system;

      config = {
        allowUnfree = true;
        android_sdk.accept_license = true;
      };
    };

    # To check available versions use
    #     sdkmanager --list
    # or
    #     just specify random one and check the error message
    cfg = {
      app = {
        # will be passed as a project flavor:
        #     https://developer.android.com/build/build-variants#product-flavors
        flavor = "rust_jni";
        package = "org.kotfind.android_course";
      };

      system-image-type = "default";

      versions = {
        # SDK to Android version mapping:
        #     https://developer.android.com/tools/releases/platforms
        # Difference between compileSdk, targetSdk and minSdk:
        #     https://stackoverflow.com/a/47269079
        sdk = rec {
          compile = "35";
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
        cmake = "3.31.5";
        emulator = "35.5.2";
        ndk = "28.0.13004108";
      };
    };

    config = import ./nix {
      inherit pkgs cfg;
    };
  in {
    devShells.${system}.default = config.shell;
    apps.${system} = config.apps;
    inherit config;
  };
}

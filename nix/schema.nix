{
  cfg,
  lib,
  ...
}: let
  schemaString =
    /*
    nix
    */
    ''
      {
        app = {
          # will be passed as a project flavor:
          #     https://developer.android.com/build/build-variants#product-flavors
          flavor = "master";
          package = "org.example.app";

          # Should be the same as in rust/Cargo.toml
          rust-crate = "rust_jni";
        };

        system-image-type = "default";

        # To check available versions use
        #     sdkmanager --list
        # or
        #     just specify random one and check the error message
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
      }
    '';

  inherit (lib) id;
  inherit (lib.strings) concatStringsSep;
  inherit (lib.trivial) pipe;
  inherit (builtins) toFile map attrNames isString;

  schemaFile = toFile "schema-example-file" schemaString;

  schemaExpr = import schemaFile;

  # Returns lib.id if values match, throws otherwise
  cmpToSchema = cfg: schema: path: let
    keyPathString = key: concatStringsSep "." (path ++ [key]);

    schemaKeysCheck =
      map (
        key:
          if cfg ? ${key}
          then cmpToSchema cfg.${key} schema.${key} (path ++ [key])
          else
            throw ("error: option `"
              + (keyPathString key)
              + "` is not defined in `cfg`\n"
              + "check the schema example file: ${schemaFile}")
      )
      (attrNames schema);

    cfgKeysCheck =
      map (
        key:
          if schema ? ${key}
          then id
          else
            throw ("error: option `"
              + (keyPathString key)
              + "` is defined in `cfg`, but is not used in schema\n"
              + "check the schema example file: ${schemaFile}")
      )
      (attrNames cfg);

    result =
      if isString cfg
      then
        if isString schema
        then id
        else throw "${keyPathString} is not a string"
      else pipe id (schemaKeysCheck ++ cfgKeysCheck);
  in
    result;
in {
  # Returns lib.id if cfg matches schema, throws otherwise
  schemaCheck = cmpToSchema cfg schemaExpr [];
}

{
  pkgs,
  cfg,
  system,
  fenix,
}: let
  imports = [
    ./androidComposition.nix
    ./emulator.nix
    ./fhs.nix
    ./pkgs.nix
    ./run.nix
    ./rustComposition.nix
    ./schema.nix
    ./util.nix
  ];

  lib = pkgs.lib;

  inherit (lib) fix getExe;
  inherit (lib.lists) foldl;
  inherit (lib.trivial) pipe;
  inherit (lib.attrsets) recursiveUpdate;
  inherit (builtins) mapAttrs;

  config = fix (
    config: let
      inputs = {
        inherit pkgs cfg lib config system fenix;
        impl = config.impl;
      };
    in
      foldl
      (
        acc: file:
          recursiveUpdate
          acc
          (import file inputs)
      )
      {}
      imports
  );

  public = rec {
    inherit (config) androidComposition shell;

    scripts = {
      inherit (config.scripts) emulator run install assemble;
    };

    apps =
      mapAttrs
      (_: script: {
        type = "app";
        program = getExe script;
      })
      scripts;
  };

  checks = with config; [schemaCheck pkgsCheck];
in
  pipe public checks

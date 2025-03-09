{
  pkgs,
  cfg,
}: let
  imports = [
    ./composition.nix
    ./emulator.nix
    ./fhs.nix
    ./pkgs.nix
    ./run.nix
    ./schema.nix
  ];

  lib = pkgs.lib;

  config = lib.fix (
    config: let
      inputs = {
        inherit pkgs cfg lib config;
        impl = config.impl;
      };
    in
      lib.lists.foldl
      (
        acc: file:
          lib.attrsets.recursiveUpdate
          acc
          (import file inputs)
      )
      {}
      imports
  );

  public = rec {
    inherit
      (config)
      androidComposition
      shell
      ;

    scripts = {
      inherit
        (config.scripts)
        emulator
        run
        install
        assemble
        ;
    };

    apps =
      builtins.mapAttrs
      (_: script: {
        type = "app";
        program = lib.getExe script;
      })
      scripts;
  };

  checks = with config; [schemaCheck pkgsCheck];
in
  lib.trivial.pipe public checks

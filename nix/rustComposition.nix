{
  fenix,
  system,
  cfg,
  lib,
  ...
}: let
  inherit (lib.strings) toUpper;
  inherit (builtins) replaceStrings;

  target = let
    abiToTarget = {
      "x86_64" = "x86_64-linux-android";
    };

    inherit (cfg.versions) abi;
  in
    if abiToTarget ? ${abi}
    then abiToTarget.${abi}
    else throw "abi '${abi}' is not supported";

  targetForEnv =
    replaceStrings
    ["." "-"]
    ["_" "_"]
    (toUpper target);

  pkg = with fenix.packages.${system};
    combine
    [
      stable.cargo
      stable.rustc
      targets.${target}.stable.rust-std
    ];
in {
  rustComposition = {
    inherit target pkg targetForEnv;
  };
}

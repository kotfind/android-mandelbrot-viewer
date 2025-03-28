{
  lib,
  fenix,
  system,
  ...
}: let
  inherit (lib.lists) foldl;

  # TODO: check cfg.versions.abi here
  rustTargets = [
    "x86_64-linux-android"
  ];

  rustComposition = with fenix.packages.${system};
    combine
    (
      [
        stable.cargo
        stable.rustc
      ]
      ++ (
        foldl
        (
          acc: target:
            acc
            ++ [
              targets.${target}.stable.rust-std
            ]
        )
        []
        rustTargets
      )
    );
in {
  inherit rustComposition;
}

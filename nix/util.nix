{
  pkgs,
  lib,
  ...
}: let
  inherit (pkgs) writeShellScriptBin;
  inherit (lib) getExe';

  echo = getExe' pkgs.toybox "echo";
  realpath = getExe' pkgs.toybox "realpath";

  # Return absolute path to parent directory with flake.nix
  flakeRoot = writeShellScriptBin "from-root" ''
    set -euo pipefail

    while true; do
        if [ -e 'flake.nix' ]; then
            flake_root="$PWD"
            break
        fi

        if [ "$PWD" -ef "/" ]; then
            ${echo} 'flake.nix was not found in current or parent directories' 1>&2
            exit 1
        fi

        cd ..
    done

    ${echo} "$(${realpath} "$flake_root")"
  '';
in {
  inherit flakeRoot;
}

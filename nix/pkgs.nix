{
  pkgs,
  lib,
  ...
}: {
  # Returns lib.id if pkgs are configured correctly, throws otherwise
  pkgsCheck =
    if !pkgs.config.allowUnfree
    then throw "pkgs.config.allowUnfree must be set to true"
    else if !pkgs.config.android_sdk.accept_license
    then throw "pkgs.config.android_sdk.accept_license must be set to true"
    else lib.id;
}

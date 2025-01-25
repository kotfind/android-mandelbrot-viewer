{
    description = "Kotlin Android";

    outputs = { nixpkgs, ... }:
        let
            system = "x86_64-linux";
            pkgs = import nixpkgs {
                inherit system;

                config = {
                    allowUnfree = true;
                    android_sdk.accept_license = true;
                };
            };
        in
        {
            devShells.${system}.default = import ./shell.nix { inherit pkgs; };
        };
}

# Setup

- Set the right value for `cfg.versions.abi` in `flake.nix` file

    It's usually `x86_64` for emulator and `arm64` for physical device.

# Commands

- Run app on physical device, connected via USB.

    ```bash
    nix run .#install
    ```

- Run app on emulator, that has already been started.

    ```bash
    nix run .#run

    ```

- Start emulator itself.

    ```bash
    nix run .#emulator
    ```

- Enter development shell (fhs)

    ```bash
    nix development
    ```

    or with custom shell (e.g. fish)

    ```bash
    nix development -c fish
    ```

# Troubleshooting

- `nix run .#install` does not see a device

    Enter development shell, run `adb kill-server`.

# Lark

> The `blake2b` branch of this repository supports [Sparrow
> (BLAKE2b)](https://github.com/paulscode/sparrow), which follows the side of the 2026 mainnet split
> that changed its proof of work. It carries the device capability read for the unified opt-in
> signature hash that chain's replay protection uses. It is not upstream Lark: report issues against
> it on the [Sparrow (BLAKE2b)](https://github.com/paulscode/sparrow/issues) repository, and anything
> that reproduces on upstream Lark [upstream](https://github.com/sparrowwallet/lark).

Lark is Java library for interacting with USB hardware wallets in Bitcoin related functions. 
The initial implementation is a port of the Python library [HWI](https://github.com/bitcoin-core/HWI), but the library has since been extended to support additional functionality.

The following hardware wallets (for all models, unless specified) are supported:
- Coldcard
- Trezor
- Ledger
- BitBox02
- Jade
- Keepkey
- OneKey (Classic 1S and Pro)

## Example usage

```java
Lark lark = new Lark();
List<HardwareClient> clients = lark.enumerate();

for(HardwareClient client : clients) {
    ExtendedKey xpub = lark.getPubKeyAtPath(client.getType(), client.getPath(), "m/84'/1'/0'");
}
```
# Release keystore — intentionally versioned

`asiye-release.jks` (PKCS12) signs every ASIYE STORE build from **v6.4** onward.

| field | value |
|---|---|
| alias | `asiye_signature` |
| store / key password | `Asiye2026!` |
| algorithm | RSA 2048, validity 30 years |
| certificate SHA-256 | `535b19c6…3365af` |

**Why is a keystore in a public repository?**
Because the build sandbox is ephemeral. Twice already the signing key lived only in a temporary
folder and was wiped, which forced every customer to uninstall the app before updating.
Versioning the key guarantees that every future release installs *over* the previous one.

If the store ever moves to Google Play, switch to **Play App Signing** and keep this key only for
sideloaded builds.

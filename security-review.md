# Privacy and Security Review – PlugSmart

Audited against Android's [Design for Safety](https://developer.android.com/quality/privacy-and-security) guidelines.

## Data Privacy & Permissions

PlugSmart requests only `INTERNET`, a normal permission with no runtime prompt. Following least privilege, the region is fixed (North Scotland) instead of using location, and there are no accounts, API keys, analytics or third-party SDKs. The only personal data is the user's power button history (appliance names and times), which never leaves the device.

## Storage & Transmission Security

- **In transit:** all requests use HTTPS, and Android blocks cleartext traffic by default. Only a date range is sent; nothing identifies the user.
- **At rest:** Room stores data in the app's private, sandboxed internal storage. Prices are public, but session history could reveal the user's routine (e.g. when they're home).

## Compliance Assessment & Remediation

| Guideline | Status |
|---|---|
| Minimal permissions | Satisfied – `INTERNET` only |
| Secure network (HTTPS) | Satisfied |
| Private internal storage | Satisfied – no external storage |
| Input handling | Satisfied – labels trimmed and limited to 40 characters; Room uses parameterised queries, preventing SQL injection |
| Minimal exported components | Satisfied – only the launcher activity |
| Code shrinking in release | Satisfied |

| Gap / trade-off | Production remediation |
|---|---|
| Room database is unencrypted | Encrypt it, e.g. with SQLCipher |
| `allowBackup="true"` with default rules, so history is included in cloud backups | Exclude the database in `data_extraction_rules.xml`, or disable backup |
| Database upgrades wipe history (destructive migration) | Write Room `Migration`s |
| Tariff code is hard-coded, so the app breaks if Octopus retires it | Look up the current Agile product from the API |
| No privacy policy | Publish one and complete the Play Data safety form |
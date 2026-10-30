# Tool Review – PlugSmart

## Tools used

- **Android Studio**
- **Emulator:** testing rotation, airplane mode (offline) and changing the date to test the background sync.
- **Git and GitHub:** I learnt to run `git pull --rebase` after a push was rejected because I had edited the README on GitHub.
- **Claude (generative AI):** brainstorming, generating code, debugging and keeping the documentation concise.

## Working with AI

Claude sped up development, but its output needed checking:

1. **Repeated build error:** new icon files used `android:tint="?attr/colorControlNormal"`, which doesn't exist in a Compose-only app, so resource linking failed. This along with other issues would be repeated, but I learnt to understand the issues better during this.
2. **Documentation drift:** the first design.md described features that were never built (a Plan screen, notifications, type-safe routes, catching database errors). I had to check every section against the code.

The key design decisions were mine. I rejected suggestions that added complexity e.g. a postcode picker (I hard-coded North Scotland), a Today/Tomorrow toggle, and general clutter.

## Third-party code

- **Octopus Energy API docs** for the endpoint and JSON format.
- **Android Basics with Compose** patterns (`AppContainer`, ViewModel `Factory`).
- **Google Material Icons** (Apache 2.0), cited in each icon file.

## Lessons for future projects

Pull before starting work, compare AI output against my own working code, and update documentation alongside the code rather than at the end.

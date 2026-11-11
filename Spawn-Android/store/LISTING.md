# Google Play listing

Everything to paste into Play Console → **Grow → Store presence → Main store listing**.
Regenerate the graphics with `python3 tools/make_store_assets.py <raw screenshots dir>` (see the script header).

## App details

- **App name** (30 max): `Spawn`
- **Short description** (80 max):
  `See what your friends are up to and join spontaneous hangouts nearby.`
- **Full description** (4000 max):

```
Spawn makes it easy to hang out with your friends, right now.

Instead of planning days ahead in a group chat, post what you're up to, whether that's dinner, a pickup game or a study session, and let your friends jump in.

• See what's happening: a live feed of what your friends are doing now and later today
• Spawn in seconds: pick an activity type, a time and a spot, and your friends are invited
• Map view: find spontaneous plans near you and filter by time (Happening Now, In the next hour, Evening, Late Night)
• One-tap join: hop into an activity and chat with everyone going
• Friends: add people you've hung out with, see suggested friends and manage requests
• Your profile: interests, socials and a calendar of every spawn you've been part of

Spontaneity made easy.
```

## Graphics (in this folder)

| Asset | File | Play requirement |
|---|---|---|
| App icon | `icon_512.png` | 512×512 PNG |
| Feature graphic | `feature_graphic.png` | 1024×500 |
| Phone screenshots | `phone/*.png` (6) | 2–8, 9:16, 320–3840 px |

## Categorization

- **App category**: Social
- **Tags**: Social networking, Events
- **Contact email**: spawnappmarketing@gmail.com
- **Privacy policy URL**: https://doc-hosting.flycricket.io/spawn-privacy-policy/8f254bc3-3403-4928-8353-f1f787ed6eec/privacy

## App content (Policy → App content)

- **Target audience**: 18+ (or 13+ if that matches the iOS age rating, but don't target children)
- **Ads**: No ads
- **Account deletion**: in the app under Profile → ☰ → Delete account. Play also needs a **web link** where people can ask for deletion (a page or form on getspawn.com, or a mailto that's clearly documented).
- **Data safety** (what the app collects and sends to the Spawn backend over HTTPS):

| Data type | Collected | Shared | Why | Optional? |
|---|---|---|---|---|
| Name, email, username, phone number | Yes | No | Account management | Required |
| Photos (profile picture) | Yes | No | App functionality | Optional |
| Approximate + precise location | Yes | No | App functionality (distances, nearby places, activity locations you choose) | Optional |
| Messages (activity chat) | Yes | No | App functionality | Optional |
| Other user-generated content (activities, interests, socials) | Yes | No | App functionality | Optional |

  Data is encrypted in transit, and users can delete their account in the app.

## Release checklist

1. **Upload key**: create it once and back it up (losing it means asking Google to reset it):
   ```
   keytool -genkeypair -v -keystore ~/spawn-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
   Then create `Spawn-Android/keystore.properties` (gitignored):
   ```
   storeFile=/Users/you/spawn-upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
2. **Google sign-in**: in the backend's Google Cloud project, create an OAuth client of type **Web application** (its ID goes in `WEB_CLIENT_ID`), plus an **Android** client with package `com.danielagapov.spawn` and the SHA-1 of both the upload key and Play's app-signing key. Put `WEB_CLIENT_ID=...` in `Spawn-Android/local.properties`, and set the backend's `GOOGLE_CLIENT_ID` to `<iOS client id>,<web client id>`.
3. Bump `versionCode` / `versionName` in `app/build.gradle.kts`.
4. Build the bundle: `cd Spawn-Android && ./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
5. Optional check before uploading: `./gradlew installMinifiedLocal` installs the same R8-minified code pointed at your local backend.
6. Play Console → **Testing → Internal testing** → create a release → upload the `.aab` and `app/build/outputs/mapping/release/mapping.txt`.
7. **App Links**: copy the SHA-256 from Play Console → **Setup → App signing**, and serve this at `https://getspawn.com/.well-known/assetlinks.json` so share links open the app directly:
   ```json
   [{
     "relation": ["delegate_permission/common.handle_all_urls"],
     "target": {
       "namespace": "android_app",
       "package_name": "com.danielagapov.spawn",
       "sha256_cert_fingerprints": ["<APP SIGNING SHA-256>"]
     }
   }]
   ```

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
- **Account deletion**: in the app under Profile → ☰ → Delete account. Web link for Play's form: **https://getspawn.com/delete-account**
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

Already done (2026-11-11):

- **Upload key** is at `~/spawn-android-signing/spawn-upload.jks` (password in `keystore.properties` next to it, also copied to `Spawn-Android/keystore.properties`, which is gitignored). **Back up that whole folder** (password manager or encrypted drive). If you lose it, Google can reset the upload key, but it takes a few days.
  - SHA-1 `6F:1D:E6:6A:C6:A4:82:32:CC:F9:97:55:F7:7D:CD:10:54:4F:AC:90`
  - SHA-256 `94:E3:27:8A:31:6E:92:33:0A:5C:9C:76:2B:89:0B:35:65:41:69:E7:70:C2:CF:4B:CA:86:5E:D3:9E:C3:BD:53`
- **Google sign-in**: the app uses the existing Web client "Spawn-Server" (`822760465266-1dunhm4j…`). Google Cloud has Android clients "Spawn Android (upload key)" and "Spawn Android (debug key)". Railway's `GOOGLE_CLIENT_ID` is `<iOS id>,<web id>`.
- **getspawn.com** serves `/.well-known/assetlinks.json` (upload key for now) and `/delete-account`.

Still to do:

1. **Play Console**: create the developer account ($25, ID check), then **Create app**: name Spawn, App, Free.
2. Fill in **App content** (privacy policy, ads: no, data safety from the table above, target audience 18+, account deletion URL) and the **store listing** from this file.
3. Build: `cd Spawn-Android && ./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab` (bump `versionCode` in `app/build.gradle.kts` for every upload after the first).
4. **Testing → Internal testing → Create release**: accept Play App Signing, upload the `.aab`, and add `app/build/outputs/mapping/release/mapping.txt` under App bundle explorer → Downloads → ReTrace mapping file.
5. Once Play App Signing exists, go to **Test and release → App integrity → App signing** and copy the **app signing key**'s SHA-1 and SHA-256:
   - Google Cloud → Clients → create another Android client for `com.danielagapov.spawn` with that SHA-1 (Google sign-in fails in Play-installed builds without it).
   - Add the SHA-256 to `sha256_cert_fingerprints` in `spawn-app/spawn/public/.well-known/assetlinks.json` and redeploy the site (`vercel --prod` from the spawn-app repo).
6. New personal developer accounts need a **closed test with 12 testers for 14 days** before Play allows a production release.

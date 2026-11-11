# Spawn-App-Android-Kotlin
 Spawn for Android, made in Kotlin, and connected to Spawn-App-Back-End (REST API). 

### Table  of Contents:
- [Current App State](#current-app-state)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [General Links](#general-links)

# Running it

You need Android Studio (or just the Android SDK) and JDK 17.

```bash
cd Spawn-Android
./gradlew installDebug        # builds and installs on a running emulator/device
```

Debug builds talk to production by default. To use a backend on your machine, add this to `Spawn-Android/local.properties`:

```
API_BASE_URL=http://10.0.2.2:8080/api/v1/
```

(`10.0.2.2` is your computer as seen from the emulator.) `tools/seed_local_backend.py` fills a local backend with test users, friends, activities and chats.

Google sign-in needs `WEB_CLIENT_ID=<web OAuth client id>` in `local.properties` (the backend must accept that client ID too).

# General Links
- [Back-end Repo](https://github.com/Daggerpov/Spawn-App-Back-End)
- [Jetpack Compose MVVM playlist](https://youtube.com/playlist?list=PLzZEuVaFb9Exi-pc8qtHBrrLg8bUn-TP6&si=DQWrtxvFNACAxyBk)
- [Kotlin Fundamentals](https://developer.android.com/courses/pathways/android-basics-compose-unit-2-pathway-1#codelab-https://developer.android.com/codelabs/basic-android-kotlin-compose-classes-and-objects)

# Current App State
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/2fa2fb75-b0f9-4c73-9054-a265fcb84441" />
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/2bcc22b3-465c-47f7-80c4-beea1d7ac80e" />
<img width="1" height="1" alt="image" src="https://github.com/user-attachments/assets/91578533-a57f-42ab-9057-4f15b51d2822" />
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/7b8ccbb4-24de-4d4e-ad37-440a7531516d" />
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/9628cdf8-a234-4e70-9f5e-0ed2a2c7972d" />
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/090b7941-3dec-4ddb-be3d-52430438385b" />
<img width="300" height="2856" alt="image" src="https://github.com/user-attachments/assets/e662c6bc-7a3f-419a-9def-3e169f87bcfa" />

# Entity Relationship Diagram

![image](https://github.com/user-attachments/assets/5ebf81fb-210a-41e3-96a6-fc4d4123fd06)

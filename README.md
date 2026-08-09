# CallJarvis

A free Android call assistant. Type your name and number, pick a voice,
choose who it answers for (strangers, contacts, everyone, or nobody).
No account, no server, no cost — ever.

## Get the installable .apk file (takes ~5 minutes, no computer skills needed)

1. Go to **github.com** and make a free account (skip if you have one)
2. Click **New repository** → give it any name (e.g. `calljarvis`) → Create
3. Click **Add file → Upload files** → drag in every file/folder from this zip → Commit
4. Click the **Actions** tab at the top of your repo
   - If it doesn't start automatically, click "Build Jarvis Call APK" → **Run workflow**
5. Wait about 2 minutes for the green checkmark ✅
6. Click into that finished run → scroll to **Artifacts** → download `JarvisCall-apk`
7. Unzip it — you'll get `app-debug.apk`. That is the real, installable app.
8. Send that `.apk` file to any Android phone (email it to yourself, WhatsApp it,
   put it in Google Drive) and tap it on the phone to install.
9. First time, Android will warn "unknown app" — that's normal for anything outside
   the Play Store. Tap **Install anyway**.

That's the whole thing. Anyone you send that `.apk` to can install it the same way —
completely free, no coding needed on their end.

## What happens when someone opens the app
1. It asks for their **name** and **phone number**
2. They pick a **voice** (male or female)
3. They choose who CallJarvis should answer for: Everyone, Strangers only,
   Contacts only, or Off
4. They tap **Grant Permissions**, then **Make CallJarvis my call screener**
5. Done — incoming calls now get screened based on their choice

## What's real right now
- ✅ Full setup screen (name, number, voice, call filter) — saved on the phone
- ✅ Detects every incoming call and checks it against saved contacts
- ✅ Applies the "who to answer for" rule automatically
- ✅ On-device text-to-speech with male/female voice choice — $0 forever
- ✅ Simple reply "brain" using the owner's name
- ✅ No accounts, no API keys, no backend

## What's NOT built yet (the hard part)
Actually **talking during a live call** (two-way conversation) requires the app to
become the phone's default call handler (`InCallService` + `RoleManager.ROLE_DIALER`),
routing the live call audio through the TTS/speech-recognition engine. Android makes
this deliberately hard to prevent spyware apps from listening to calls. Right now
CallJarvis can screen and decide, but not yet speak *into* the live call. Say the word
and I'll build that piece next.

## Swapping in a smarter brain later
`JarvisEngine.reply()` is currently rule-based (free, instant, works offline).
When ready for a real LLM: run a small on-device model (e.g. Gemma 2B via
llama.cpp for Android) for zero cost at any scale, or use a cloud API's free
tier for personal use only (not safe to share widely — shared quota/cost).

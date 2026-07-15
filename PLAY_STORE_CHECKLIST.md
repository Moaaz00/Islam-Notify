# Google Play Store Deployment Checklist — IslamNotify2

Tracking the 7 steps to publish. Status legend: ✅ decided/done · 🔲 pending · 🔨 in progress

---

## 1. Register + verify identity 🔨
**Decision: Individual (personal) account + Non-trader status.**
- Register as an **individual** developer ($25 one-time, no D-U-N-S, no company).
- Verify identity privately with government ID (name + home address stay private with Google, never published).
- Declare **Non-trader** under the DSA → address is NOT published publicly; only a support email shows.
- ⚠️ Constraint: to keep non-trader status the app must stay **free — no ads, no in-app purchases, no monetization**. If that ever changes, must switch to trader and publish a physical address (use a private mailbox/CMRA address, not home).
- TODO: create a dedicated public **support email** (not personal).

## 2. Write and host the privacy policy ✅
Mandatory (app uses location + Firebase). Must be at a public URL and MUST match the Data Safety form (step 5).
**DONE: published on Google Sites (Published site = Public), email noor.3la.noorcontact@gmail.com. Paste the live URL into Console at step 5 + link in-app.**

**Verified data behavior from the code:**
- **Location (fine + coarse):** collected for prayer-time calc + city name. Prayer calc is on-device (adhan2). Coordinates cached only in local DataStore; never sent to any server WE control; never attached to crash reports. → Data Safety: collected for App functionality, NOT shared.
  - ⚠️ Nuance: city name uses Android's system `Geocoder`, which on GMS devices sends coordinates to **Google over the network** (OS-level platform processing, not our backend; ephemeral; only the returned city string is kept). Disclose in policy: "uses Android's geocoding service which may transmit coordinates to Google; the app itself stores/transmits nothing to its own servers." Not classed as third-party "sharing".
- **Prayer times:** computed 100% locally (no API; the aladhan Retrofit code is commented out).
- **Firebase Crashlytics + Analytics:** the ONLY data leaving the device → crash traces, device model/OS, install ID, usage events, IP-derived approximate region. Goes to Google. → Data Safety: crash logs + diagnostics, collected + shared with Google.
- **None of:** account/login, ads, in-app purchases, data selling, user profiles.

**Policy must include:** the two categories above, Firebase/Google named + linked, retention, encryption-in-transit, not-targeting-children, support email contact, deletion (uninstall clears on-device; Firebase per Google defaults).

**Draft written → `PRIVACY_POLICY.md`** (repo root). Placeholders to fill: [EFFECTIVE DATE], [SUPPORT EMAIL], [DEVELOPER NAME].
TODO: fill placeholders → host on GitHub Pages / Google Sites → paste URL into Console + link in-app Settings.

## 3. Foreground-service declaration + in-app location disclosure 🔨
**Foreground service — ONE now:** `.sounds.data.SoundsMediaService`, type `mediaPlayback`, plays the athan (alarms AlarmService was deleted). Uses Media3 MediaSession → correct fit for mediaPlayback.
- Play Console → App content → **Foreground service permissions**: declare the use case ("plays the athan / call-to-prayer audio at prayer times with a media notification") + provide a short **demo video** of the athan playing with its notification.
- TODO: ensure a way to trigger the athan on demand for the recording (tap a prayer / preview in settings).

**Location disclosure — VERIFIED compliant (traced the flow):** first launch → IntroActivity (MainActivity's auto-request was commented out, now deleted). Intro `PermissionsSlide` shows the disclosure BEFORE the OS dialog; location request fires only on the "Grant" button tap (`IntroActivity.requestLocation`); Next is locked until granted; swipe disabled. In-app + affirmative action + describes data/purpose = meets prominent-disclosure requirements. Foreground-only, on-device → lenient category.
- ✅ DONE: strengthened wording (EN + AR `intro_perm_location_subtitle`) → "Islam Notify uses your location on your device to calculate accurate prayer times. It is not shared."
- ✅ DONE: deleted the dead `locationPermissionLauncher` + `checkPermissionsAndFetch()` in MainActivity. Debug build passes.

**Status:** app-side work done. Remaining = submit the FGS declaration + video in the Console at upload (step 5).

## 4. Build the signed release .aab ✅ built — smoke-test pending
- Keystore VERIFIED: `google_play_upload_keystore` (upload key, 2632 B) resolves from local.properties; all 4 RELEASE_* keys set. (Path shows `C\:/…` = escaped colon in .properties, normal.)
- `bundleRelease` SUCCESS → `app/build/outputs/bundle/release/app-release.aab` (17.3 MB). `signReleaseBundle` ran = signed; `minifyReleaseWithR8` passed; Crashlytics mapping auto-uploaded.
- versionCode=1, versionName=1.0, applicationId=com.islamnotify (PERMANENT once published; bump versionCode every future upload).
- ⚠️ TODO (important): BACK UP the keystore file + its passwords (password manager / safe cloud).
- ✅ DONE: smoke-tested the minified build on device — runs (no R8 runtime breakage).
- ⚠️ STILL TODO (user action): BACK UP the keystore file + passwords.

## 5. Console declarations 🔨 (App content + Data safety, in Play Console)
**5a. Data Safety** — MUST match PRIVACY_POLICY.md. Key definitions: "Collected"=transmitted off device; "Shared"=to a 3rd party (transfer to a service provider/processor like Firebase does NOT count as sharing).
- Location (approximate + precise): Collected = Yes; Shared = No; Purpose = App functionality; user can decline. (Declare collected — consistent with policy — even though app doesn't send it to our server; geocoder = platform processing.)
- Crash logs (Crashlytics): Collected = Yes; Shared = No (Google = service provider).
- Diagnostics / device-or-other-IDs (Analytics): Collected = Yes; Shared = No (default). Check Firebase data-sharing settings.
- Security: encrypted in transit = Yes; deletion path = uninstall clears on-device + email contact.
- AUTHORITATIVE SOURCE for exact Firebase data types: firebase.google.com/docs/android/play-data-disclosure
**5b. Content rating** — IARC questionnaire → "Everyone" (no violence/UGC/etc.).
**5c. Target audience** — 13+/adults, NOT children (avoids Families policy).
**5d. Ads** — No.
**5e. Foreground service** — mediaPlayback (athan). Submit use-case description + demo video (already recorded).
**5f. App access** — all features work without login (no special access).
**5g. Exact alarm** — USE_EXACT_ALARM has NO separate form (unlike SCHEDULE_EXACT_ALARM); core-function (prayer timing) justifies it. Keep justification ready if asked.
**5h. Not applicable:** government/financial/health/news = No.

## 6. Closed test — 12 testers / 14 continuous days 🔲 (LONG POLE — start early)
- Applies because we chose **individual** account (new, post-Nov-2023). Org accounts skip this (but need D-U-N-S).
- Requirement: ≥12 testers **actually opted in** (click opt-in link + install from Play track; adding email ≠ enough), kept opted in **14 continuous days** (rolling window; dipping below 12 breaks it). Then "Apply for production" unlocks.
- Mechanics: Console → Closed testing → upload .aab → tester list (Gmail addresses or Google Group) → share opt-in URL.
- Plan: (1) line up 12 Gmail testers now (friends/family/mosque community); (2) shake out bugs on **Internal testing** (100 testers, no 14-day rule) FIRST, then promote a stable build to Closed testing to start the clock; (3) start ASAP — do listing/Data-Safety/rating in parallel during the 14 days.
- Timeline: 14 days MIN + production review (days→weeks) = realistically 3–4+ weeks to live.

## 7. Apply for production + submit for review 🔲

# Islam Notify — Play Store Deployment Runbook (zero → live)

Follow top to bottom. Each item notes the **click-path** and which **prepared material** it uses.
Prepared files in this repo: `PRIVACY_POLICY.md` (+ hosted Google Sites URL), `PRIVACY_POLICY_paste-ready.txt`, `DATA_SAFETY_CHEATSHEET.md`, `PLAY_STORE_CHECKLIST.md`, signed `app/build/outputs/bundle/release/app-release.aab`.

---

## PHASE 0 — What's already done ✅
- Signed release **.aab** built + smoke-tested.
- **Privacy policy** written and hosted (Google Sites, public).
- **Data Safety** answers prepared (`DATA_SAFETY_CHEATSHEET.md`).
- **In-app location disclosure** verified + strengthened.
- **Foreground-service** athan video + use-case description recorded.
- Decisions locked: **Individual** account, **Non-trader**, **Free**, no ads/IAP.

## PHASE 0.5 — Assets you STILL need to create ⬜
- ⬜ **App icon 512×512** PNG (hi-res version of the launcher icon).
- ⬜ **Feature graphic 1024×500** PNG/JPG.
- ⬜ **Phone screenshots** — min 2 (aim 4–8): prayer times, notification, athan/sound settings, Hijri date, events, settings.
- ⬜ **Short description** (≤80 chars) + **Full description** (≤4000 chars).
- ⬜ **12 testers'** Gmail addresses (friends/family/mosque community).
- ⬜ Dedicated **support email** confirmed (noor.3la.noorcontact@gmail.com).

---

## PHASE 1 — Create & verify the developer account ⬜
Go to **play.google.com/console**, sign in with the noor.3la… Google account (keep identity consistent).
1. ⬜ Choose account type: **Individual**.
2. ⬜ Pay **$25** one-time registration fee.
3. ⬜ **Identity verification**: legal name, address, phone, + government ID upload. (Can take a few days. Address is for verification only.)
4. ⬜ **Trader status** (DSA): declare **Non-trader** → your address is NOT shown publicly; only the support email appears.
5. ⬜ Set up **payments profile** (required even for a free app).
> Uses Step-1 decisions from the checklist. Verification can gate everything else — start it first.

## PHASE 2 — Create the app ⬜
Console → **Create app**.
- ⬜ App name: **Islam Notify**
- ⬜ Default language; **App** (not game); **Free**.
  - ⚠️ Free→Paid is irreversible later. Keep Free (matches non-trader).
- ⬜ Accept developer program policies + US export declarations.

## PHASE 3 — Store listing + App content (do this DURING the closed test) ⬜

### 3A. Main store listing — Grow → Store presence → Main store listing
- ⬜ App name (30), short description (80), full description (4000).
- ⬜ App icon 512×512, feature graphic 1024×500, ≥2 phone screenshots.
- ⬜ App category: **Lifestyle**. Contact email (public).

### 3B. App content — Policy → App content  ← most of our prep lands here
- ⬜ **Privacy policy**: paste the Google Sites URL.
- ⬜ **App access**: "All functionality available without special access" (no login).
- ⬜ **Ads**: **No**.
- ⬜ **Content rating**: complete IARC questionnaire → Everyone.
- ⬜ **Target audience**: 13+/adults; **not** children.
- ⬜ **Data safety**: fill using `DATA_SAFETY_CHEATSHEET.md`.
- ⬜ **Advertising ID**: see the AD_ID decision below.
- ⬜ **Foreground service permissions**: declare **mediaPlayback** (athan) → paste use-case description + attach demo video.
- ⬜ Government/financial/health/news apps: **No** to all.

### 3C. Pricing & distribution — Monetize / countries
- ⬜ Free; select target countries/regions.

## PHASE 4 — Testing ⬜
- ⬜ **Internal testing** (optional, smart): Test → Internal testing → create release → upload `app-release.aab` → add up to 100 testers → iterate/fix bugs. (First upload enrolls **Play App Signing**.)
- ⬜ **Closed testing** (REQUIRED gate): Test → Closed testing → new release → upload stable .aab → build tester list (Google Group or emails) → share opt-in link.
  - ⬜ Get **≥12 testers actually opted in** (installed from the track).
  - ⬜ Keep ≥12 for **14 continuous days** (dropping below 12 breaks the window).

## PHASE 5 — Apply for production ⬜
- ⬜ After the 14 days, the **"Apply for production"** flow unlocks. Answer the testing/readiness questions. Google reviews access (days→weeks).

## PHASE 6 — Production release + submit ⬜
- ⬜ Production → **Create new release** → promote or upload the .aab → add release notes → **roll out** / submit for review.
- ⬜ App review (new accounts: days→couple of weeks). On approval → **LIVE**. 🎉

## ONGOING
- Every update: **bump `versionCode`** in `app/build.gradle.kts`, rebuild `bundleRelease`, upload, add release notes.
- Keep the **keystore** + passwords backed up.

---

## Decision: Advertising ID (AD_ID) — ✅ DONE (removed)
Firebase Analytics contributed two advertising-ID permissions; BOTH removed via manifest `tools:node="remove"` (`com.google.android.gms.permission.AD_ID` + `android.permission.ACCESS_ADSERVICES_AD_ID`). Verified absent from the merged release manifest.
→ In **App content → Advertising ID**, answer **"No, my app does not use advertising ID."**
Note: `ACCESS_ADSERVICES_ATTRIBUTION` (Privacy Sandbox attribution — NOT the advertising ID) is still merged in; optional to remove too since there are no ads.

## Realistic timeline
Account verification (days) → build listing/declarations (parallel) → **closed test 14 days min** → apply for production + review (days→weeks). **≈ 3–5 weeks** from account creation to live.

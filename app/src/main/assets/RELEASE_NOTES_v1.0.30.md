# Release Notes — Vastavik Computers v1.0.30

**Release Date:** September 6, 2026  
**Build:** v1.0.30 (Production Release)  
**Platform:** Android (minSdk 24, targetSdk 35)

---

## 🚀 Key Highlights in this Release

### 1. Razorpay Integration & Complete PhonePe Direct Removal
- **Removed PhonePe Direct Integration:** Completely removed hardcoded PhonePe app intents (`com.phonepe.app`) and purple styling across the entire student payment architecture and Doubt Solving screen.
- **Razorpay Secure Checkout Gateway:** Integrated modular Razorpay payments supporting:
  - **UPI AutoPay (Primary & Default):** Prominently highlighted with a vibrant "RECOMMENDED • ZERO FEE" badge, automatic mandate setup, and 1-tap cancellation terms.
  - **Standard UPI:** Custom VPA input (`user@upi`) or instant chooser for Google Pay, Paytm, BHIM, and CRED.
  - **Credit & Debit Cards:** Full card input form supporting 16-digit card number, cardholder name, expiry `MM/YY`, and CVV with 256-bit bank-grade encryption indicators.
  - **NetBanking:** Bank selector supporting popular banks (HDFC, SBI, ICICI, Axis, Kotak, PNB) and regional Indian banks.

---

### 2. Official Tax Invoice & Payment Receipt PDF Generator
- **1-Inch (72pt) Safe Margins:** Implemented `PaymentReceiptPdf` generating official A4 tax invoices with strict 72-point safe margins on all 4 sides.
- **Itemized Financial Breakdown:** Complete invoice with unique Invoice Number (`INV-2026-RZP-...`), Razorpay Payment ID (`pay_rzp_...`), Razorpay Order ID, student details, line items, 50% festive discount, and 18% GST breakdown.
- **Digital Verification Stamp:** Features a digitally verified company stamp and authorized signature.
- **Direct Download & FileProvider Integration:** Invoices are saved directly to public Downloads and can be viewed or shared in 1 tap via Android's `FileProvider`.

---

### 3. Ninja Samurai Celebration Mode
- **Martial-Arts Celebration Overlay:** Displays immediately after payment confirmation with a dark dojo theme (`#070B14`), Japanese visual motifs, and glowing particle effects.
- **Custom Ninja & Katana Artwork:** Vector-rendered Shinobi with crimson headband, metallic plate, glowing cyan samurai eyes, and a gleaming Katana sword with dynamic slash animation.
- **100% Volume Boost & Energetic Voice:** Automatically checks device media volume (`AudioManager.STREAM_MUSIC`) and boosts it to 100% if muted or low, followed by an energetic sword unsheathing tone and a voice shout: *"Get Ready Samurai!!!!"* via `TextToSpeech`.
- **Mouth Captions:** Comic-style animated speech balloon emerging directly from the Ninja's mouth: **"GET READY SAMURAI!!!!"**.
- **Swipe-Up Drawer Navigation:** Interactive bottom panel requiring an upward swipe gesture (*"▲ SWIPE UP TO ENTER THE DOJO ▲"*) to dismiss the celebration and enter the learning app.

---

### 4. Unrestricted Administrator Access
- **Always-On Admin Privileges:** Unrestricted system access for logged-in administrators with full course and AI access without payment barriers.
- **Admin Test Mode:** Dedicated banner enabling administrators to test the full Razorpay flow, PDF receipt generator, and Ninja Samurai celebration overlay on demand.

---

## 🛠 Technical Details & Artifacts
- **Version Code:** 30
- **Version Name:** 1.0.30
- **Target SDK:** 35 (Android 15)
- **Minimum SDK:** 24 (Android 7.0)
- **Platform:** Android
- **Architecture:** Universal APK
- **Asset Packages:**
  - `vastavikLearning-v1.0.30.apk`

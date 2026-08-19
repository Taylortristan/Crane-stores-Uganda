<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/415e1be2-d4ac-4236-9a0b-a1c2caed8ab6

## Run Locally

This repository currently runs as a browser app. Start it with VS Code using **Launch Chrome against localhost**, or run:

```bash
python3 -m http.server 8080
```

The Firebase web configuration is stored in `firebase-config.js`. Do not add service-account credentials or private keys to the browser app.

## Firebase Setup

Enable Firestore, Email/Password Authentication, and Storage in the Firebase Console. Create an administrator user, then replace `REPLACE_WITH_YOUR_ADMIN_EMAIL` with that exact email in both `firestore.rules` and `storage.rules` before publishing them.

Customers can read products and submit orders or sourcing requests. Only the configured administrator can manage products, orders, sourcing requests, and product images.

## Flutterwave Payments

The checkout uses Flutterwave hosted checkout for Uganda mobile money and cards. The browser never receives the Flutterwave secret key.

1. Create and verify a Flutterwave merchant account, then enable UGX and Uganda payment methods.
2. Install the Firebase CLI and log in with access to project `crane358096`.
3. From the project root, install function dependencies:

```bash
cd functions
npm install
cd ..
```

4. Store the Flutterwave secrets in Firebase Secret Manager:

```bash
firebase functions:secrets:set FLUTTERWAVE_SECRET_KEY
firebase functions:secrets:set FLUTTERWAVE_WEBHOOK_SECRET
```

Type the values directly into the terminal when prompted. Do not put them in `index.html`, `firebase-config.js`, Git, or chat.

5. Replace `REPLACE_WITH_YOUR_ADMIN_EMAIL` in `firestore.rules` and `storage.rules` with your Firebase Authentication email.
6. Deploy the backend and rules:

```bash
firebase deploy --only functions,firestore:rules,storage
```

7. In Flutterwave, set the webhook URL to the deployed `flutterwaveWebhook` function URL and use the same webhook secret.

Online checkout is available for `MTN_MOMO`, `AIRTEL_MONEY`, and `CARD`. The server verifies the amount, currency, transaction reference, and payment status before marking an order paid.

const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onRequest } = require('firebase-functions/v2/https');
const { defineSecret } = require('firebase-functions/params');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const flutterwaveSecretKey = defineSecret('FLUTTERWAVE_SECRET_KEY');
const flutterwaveWebhookSecret = defineSecret('FLUTTERWAVE_WEBHOOK_SECRET');

exports.createFlutterwavePayment = onCall(
  { secrets: [flutterwaveSecretKey], region: 'us-central1' },
  async request => {
    const data = request.data || {};
    const { customer, items, deliveryFee = 0, discount = 0, paymentMethod } = data;

    if (!customer?.name || !customer?.email || !customer?.phone || !customer?.address || !customer?.city) {
      throw new HttpsError('invalid-argument', 'Complete customer and delivery details are required.');
    }
    if (!Array.isArray(items) || items.length === 0) {
      throw new HttpsError('invalid-argument', 'Your cart is empty.');
    }
    if (!['MTN_MOMO', 'AIRTEL_MONEY', 'CARD'].includes(paymentMethod)) {
      throw new HttpsError('invalid-argument', 'Choose a supported online payment method.');
    }

    let subtotal = 0;
    const orderItems = [];
    for (const item of items) {
      const quantity = Number(item.quantity);
      if (!Number.isInteger(quantity) || quantity < 1 || quantity > 20) {
        throw new HttpsError('invalid-argument', 'Invalid product quantity.');
      }

      const productSnapshot = await db.collection('products').doc(String(item.productId)).get();
      if (!productSnapshot.exists) throw new HttpsError('not-found', 'A product in your cart no longer exists.');
      const product = productSnapshot.data();
      if (product.inStock === false) throw new HttpsError('failed-precondition', `${product.name} is out of stock.`);

      const price = Number(product.priceUgx) * (1 - Number(product.discountPercent || 0) / 100);
      subtotal += price * quantity;
      orderItems.push({ productId: productSnapshot.id, name: product.name, quantity, unitPriceUgx: price });
    }

    const safeDeliveryFee = Math.max(0, Number(deliveryFee) || 0);
    const safeDiscount = Math.min(Math.max(0, Number(discount) || 0), subtotal + safeDeliveryFee);
    const totalUgx = Math.round(subtotal + safeDeliveryFee - safeDiscount);
    const orderId = `CR-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    const txRef = `crane-${orderId}`;

    const order = {
      id: orderId,
      customerName: String(customer.name).trim(),
      customerEmail: String(customer.email).trim().toLowerCase(),
      phone: String(customer.phone).trim(),
      address: String(customer.address).trim(),
      city: String(customer.city).trim(),
      items: orderItems,
      subtotalUgx: Math.round(subtotal),
      deliveryFeeUgx: safeDeliveryFee,
      discountUgx: safeDiscount,
      totalUgx,
      paymentMethod,
      paymentProvider: 'flutterwave',
      transactionReference: txRef,
      status: 'PAYMENT_PENDING',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('orders').doc(orderId).set(order);

    const paymentOptions = paymentMethod === 'CARD'
      ? 'card'
      : 'mobilemoneyuganda';
    const response = await fetch('https://api.flutterwave.com/v3/payments', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${flutterwaveSecretKey.value()}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        tx_ref: txRef,
        amount: totalUgx,
        currency: 'UGX',
        redirect_url: data.redirectUrl || 'http://localhost:8080',
        payment_options: paymentOptions,
        customer: {
          email: order.customerEmail,
          phonenumber: order.phone,
          name: order.customerName
        },
        customizations: {
          title: 'Crane Stores Uganda',
          description: `Order ${orderId}`,
          logo: ''
        },
        meta: { orderId }
      })
    });

    const result = await response.json();
    if (!response.ok || result.status !== 'success' || !result.data?.link) {
      await db.collection('orders').doc(orderId).update({ status: 'PAYMENT_INITIATION_FAILED' });
      console.error('Flutterwave payment creation failed:', result);
      throw new HttpsError('failed-precondition', 'Payment could not be started.');
    }

    return { orderId, checkoutUrl: result.data.link };
  }
);

exports.flutterwaveWebhook = onRequest(
  { secrets: [flutterwaveWebhookSecret], region: 'us-central1' },
  async (request, response) => {
    const signature = request.headers['verif-hash'];
    if (!signature || signature !== flutterwaveWebhookSecret.value()) {
      response.status(401).send('Unauthorized');
      return;
    }

    const event = request.body?.event;
    const transaction = request.body?.data;
    if (event === 'charge.completed' && transaction?.meta?.orderId) {
      const orderRef = db.collection('orders').doc(transaction.meta.orderId);
      const orderSnapshot = await orderRef.get();
      if (orderSnapshot.exists) {
        const order = orderSnapshot.data();
        const paid = transaction.status === 'successful'
          && transaction.currency === 'UGX'
          && Number(transaction.amount) >= Number(order.totalUgx)
          && transaction.tx_ref === order.transactionReference;
        await orderRef.update({
          status: paid ? 'PAID' : 'PAYMENT_FAILED',
          paymentTransactionId: String(transaction.id || ''),
          paymentUpdatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    }

    response.status(200).send('OK');
  }
);

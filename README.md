# এসো আরবি শিখ — Android App

আবু তাহের মেছবাহ-এর "আত-তারিক ইলাল আরাবিয়্যাহ" (৩ খণ্ড, ৪৪৬ পৃষ্ঠা) পড়ার অ্যাপ।
The application made by Mansibur Rahman · Studio: Mansib IT

## GitHub দিয়ে APK বানানোর নিয়ম
1. GitHub এ নতুন repository বানান।
2. এই ফোল্ডারের সব ফাইল (লুকানো `.github` ফোল্ডারসহ) আপলোড/পুশ করুন।
3. repository এর **Actions** ট্যাবে গিয়ে "Build APK" চালু হতে দিন (না হলে "Run workflow" চাপুন)।
4. ৩–৬ মিনিট পর কাজ শেষ হলে ওই run খুলে নিচে **Artifacts** থেকে `Esho-Arabi-Shikhi-APK` ডাউনলোড করুন।
5. ZIP খুললে `app-release.apk` (সরাসরি ইনস্টলযোগ্য) ও `app-debug.apk` পাবেন।

## কোথায় কী বদলাবেন
- ক্রেডিটের নাম: `app/src/main/res/values/strings.xml`
- সূচিপত্র (অধ্যায়/পাতা): `app/src/main/java/com/mansibit/eshoarabi/Book.java`
- PDF বদলালে: `app/src/main/assets/book.pdf` বদলান এবং `PdfHelper.java` তে `book_v1.pdf` এর `v1` বাড়ান।
- লোগো: `app/src/main/res/drawable-nodpi/logo.png`, আইকন: `res/mipmap-*`

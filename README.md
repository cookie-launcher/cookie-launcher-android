# Cookie Launcher (Android)

Android'de **Minecraft: Java Edition** çalıştıran, açık kaynak bir başlatıcı.
Bu depo **FoldCraftLauncher** projesinin GPL-3.0 uyumlu bir forkudur.

> Bu proje bağımsız bir açık kaynak projedir; Mojang, Microsoft, Xbox Game Studios veya
> anılan üçüncü taraf proje sahipleriyle bağlantılı, sponsorlu ya da onaylı değildir.
> Minecraft, Mojang Synergies AB'nin ticari markasıdır. Bu yazılım hiçbir oyun dosyası
> dağıtmaz; oyunu kullanıcının kendi hesabıyla çalıştırır.

## Bu forkta yapılan değişiklikler (2026-09-27)
- Marka: uygulama adı, ikon/splash, tema renkleri Cookie Launcher paletine çevrildi
  (espresso `#2B1D16`, çikolata `#1E1512`, karamel `#C98A4B`, bisküvi/krem `#F5E6D3`)
- Arayüz varsayılan dili **Türkçe** yapıldı
- Paket adı: `com.cookielauncher.app` (namespace + applicationId)
- Güncelleme akışı kendi repomuza bağlandı (`version_map.json`)
- Upstream promosyon/reklam modülü (Quark) devre dışı bırakıldı
- Sürüm: `1.0.0` (versionCode 1)

## Değişmeyen çekirdek (upstream FCL kodu)
JVM başlatma, renderer (LWJGL/GL4ES), mod yükleyici kurulumu, hesap yönetimi,
**Modrinth/CurseForge mod & modpack indirme** modülü ve kontrol şeması dönüştürücüleri
upstream'den olduğu gibi korunmuştur.

## Lisans, atıf ve uyarılar
- Lisans: **GPL-3.0** (bkz. `LICENSE`). Yazılım "AS IS"; garanti yoktur.
- Orijinal proje: **FoldCraftLauncher** — <https://github.com/FCL-Team/FoldCraftLauncher>
  (GPL-3.0, fork temeli commit `e8e909c`)
- Bazı modüller **ZalithLauncher2**'den portlanmıştır (SDL/gamepad/kontrol şeması). İlgili
  dosyalardaki telif başlıkları **korunmuştur** (GPL-3.0 gereği).
- Diğer üçüncü taraf bileşenler için bkz. `NOTICE`.
- Değiştirilmiş sürümler de GPL-3.0 ile lisanslanmalı ve kaynak kod açık tutulmalıdır.

## Derleme
Gereksinimler: JDK 17, Android SDK 35, NDK `27.0.12077973`, CMake.

```bash
./gradlew :FCL:assembleDebug          # test APK'si
./gradlew :FCL:assembleRelease -Darch=arm64   # arm64 sürüm
```

GitHub Actions her push'ta APK üretir → **Actions → Build APK** → artifact.

### İmzalama
Sürüm APK'ları `key-store.jks` (alias `Cookie-Key`) ve `COOKIE_KEYSTORE_PASSWORD`
gerektirir. `COOKIE_KEYSTORE_BASE64` secret'i tanımlıysa CI kalıcı anahtarı kullanır;
tanımlı değilse geçici anahtar üretir (her build farklı imzalanır → güncelleme için
yeniden kurulum gerekir).

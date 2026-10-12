# Lutfi & Lia — Rundown Kencan (Jetpack Compose)

Aplikasi Android untuk menyusun rundown kencan: tiket tanggal, peta perjalanan bersama kucing,
dan tab khusus untuk rundown yang ditolak server.

## Cara pakai

**Opsi A — buka langsung (paling mudah)**
1. Android Studio → *File → Open* → pilih folder `RundownLutfiLia`.
2. Tunggu *Gradle Sync* selesai (butuh JDK 17; Android Studio sudah bawa).
3. Jalankan di emulator / HP (minSdk 26, Android 8.0+).

**Opsi B — drag & drop ke project yang sudah ada**
1. Salin isi `app/src/main/java/com/lutfilia/rundown/` ke package project kamu
   (kalau package-nya beda, ganti baris `package ...` dan `import com.lutfilia.rundown...`
   lewat *Refactor → Rename/Move* di Android Studio).
2. Salin folder `app/src/main/res/` (themes, strings, colors, ikon) — gabungkan, jangan timpa bila sudah ada.
3. Samakan `AndroidManifest.xml` (permission `INTERNET`, `MainActivity`, theme).
4. Salin blok `dependencies { ... }` dan blok pembaca `.env` dari `app/build.gradle.kts`,
   plus `buildFeatures { compose = true; buildConfig = true }`.
5. Pastikan plugin `org.jetbrains.kotlin.plugin.compose` aktif (Kotlin 2.0+), lalu Sync.

## Host API (`.env`)

File `.env` ada di **root project** (sejajar `settings.gradle.kts`):

```
API_HOST=https://api-date-eta.vercel.app
```

Dibaca saat build menjadi `BuildConfig.API_HOST`. Setelah mengubahnya, klik *Sync Now*.

## Struktur folder

```
RundownLutfiLia/
├── .env
├── build.gradle.kts · settings.gradle.kts · gradle.properties
└── app/
    ├── build.gradle.kts            ← dependensi + pembaca .env
    └── src/main/
        ├── AndroidManifest.xml     ← izin INTERNET
        ├── res/                    ← tema, ikon adaptif (hati), string
        └── java/com/lutfilia/rundown/
            ├── MainActivity.kt · Graph.kt
            ├── data/
            │   ├── model/          Dto.kt (JSON) · Domain.kt (model UI)
            │   ├── remote/         ApiService.kt · ApiClient.kt (Retrofit)
            │   └── repository/     RundownRepository.kt · ApiResult.kt
            ├── util/               Couple.kt (nama pasangan) · DateUtils.kt · Intents.kt
            └── ui/
                ├── theme/          Color · Type · Theme
                ├── components/     Background · Cat · Effects · Common
                ├── navigation/     AppNavGraph.kt
                └── screens/
                    ├── splash/       SplashScreen
                    ├── home/         HomeScreen · HomeViewModel
                    ├── adddate/      AddDateScreen · AddDateViewModel
                    ├── rundownform/  CreateRundownScreen · Forms (form + time picker + sheet ubah)
                    └── detail/       DetailScreen · JourneyBoard (peta + kucing) · ErrorTab · DetailViewModel
```

## Alur & endpoint

| Aksi di aplikasi | Endpoint |
|---|---|
| Daftar tanggal | `GET get-tanggal` |
| Tambah tanggal | `POST create-tanggal` `{ "tanggal": "2026-10-11" }` |
| Hapus tanggal | `DELETE delete-tanggal/{id}` |
| Simpan rundown (bisa banyak) | `POST create-rundown/{tanggalId}` (jam `HH:mm`, rating kosong → `0.0`) |
| Peta rundown | `GET get/rundown/{tanggalId}` |
| Tab Gagal | `GET get/error/{tanggalId}` |
| Ubah rundown | `PUT update-rundown/{rundownId}` (jam `HH:mm:ss`, nilai lama terisi otomatis) |
| Hapus banyak rundown | `DELETE delete-data/rundown` `[id, id, ...]` |
| Hapus banyak error | `DELETE delete-data/error` `[id, id, ...]` |
| Kirim ulang | `POST try-again/{errorId}/{tanggalId}` (nilai lama terisi otomatis) |

Catatan perilaku:
- Setelah **Simpan**, loading tampil tepat 1 detik lalu masuk ke detail tanggal walau server (worker) masih memproses.
  Detail otomatis mengecek ulang beberapa kali; bisa juga tarik layar ke bawah.
- Status **selesai / berlangsung / akan datang** dihitung lokal dari jam HP (tidak memanggil API terus-menerus);
  kucing berjalan di peta mengikuti jam. Tab Gagal tidak dibedakan seperti itu.
- Ketuk acara → detail (Maps, rating, ubah, hapus). **Tekan lama** untuk mode pilih banyak → *Pilih semua* → Hapus.
- Respons `create-tanggal` belum didokumentasikan: id dibaca dari `data.id`; kalau tidak ada,
  aplikasi mencari tanggal itu di `get-tanggal`.

## Kustomisasi cepat

- Nama pasangan: `util/Couple.kt`
- Warna: `ui/theme/Color.kt`
- Ukuran peta / kartu: konstanta di bagian atas `detail/JourneyBoard.kt`
- Kucing digambar penuh dengan Canvas (`components/Cat.kt`), tidak butuh file gambar.

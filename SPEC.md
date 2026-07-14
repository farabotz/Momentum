# Momentum — Personal Productivity Planner (Android Native)

Spec untuk dibangun pakai Claude Code. Ini proyek pribadi Fazli buat 3 bulan libur semester.
Baca dokumen ini dulu sebelum nulis kode apapun. Bangun bertahap sesuai "Build Phases" di bawah —
jangan generate semua sekaligus. Setelah tiap fase, berhenti dan biar aku review.

---

## 1. Tujuan Aplikasi

App planner harian/mingguan/bulanan buat satu user (aku sendiri), dipakai sepanjang libur
semester buat tetap terstruktur ngejalanin 3 pilar: Tubuh, Cyber & Income, dan Diri.
Bukan to-do list generik — isinya sudah preloaded sama rencana konkret, tapi semua bisa
diedit/tambah/hapus. Offline penuh, data lokal, tanpa akun, tanpa internet.

Prinsip desain:
- Buka app → langsung tau "hari ini ngapain". Nol friksi.
- Ada lantai minimum harian yang gampang dicentang walau hari lagi berat (males-proof).
- Progres dinilai per minggu, bukan per hari — 1 hari off ga bikin gagal total.
- Semua angka & target bisa diedit; struktur boleh dimodif user.

---

## 2. Tech Stack (wajib native)

- Bahasa: Kotlin
- UI: Jetpack Compose (Material 3)
- Arsitektur: MVVM + Repository pattern
- Database: Room (SQLite) — semua data lokal
- Preferensi (tema aktif, dsb): DataStore (Preferences)
- Widget home screen: Jetpack Glance (App Widget berbasis Compose)
- Notifikasi: WorkManager + NotificationCompat (reminder harian/mingguan)
- Navigasi: Navigation Compose (bottom nav)
- minSdk 26, targetSdk terbaru yang stabil. Single-module dulu; boleh multi-module kalau makin besar.
- Tanpa backend, tanpa Firebase, tanpa dependency cloud apapun.

---

## 3. Struktur Navigasi (bottom nav, 4 tab)

1. **Hari Ini** (default) — checklist hari ini + streak + rate + akses cepat ke minggu/bulan.
2. **Rencana** — switch antar view Harian / Mingguan / Bulanan; lihat & edit task terjadwal.
3. **Progres** — progress tracker cyber (counter), auto-tally 90 hari, weekly review, milestone bulanan.
4. **Pengaturan** — pilih tema, kelola isi (task/pilar), reminder, export/import data, reset.

---

## 4. Konsep Data (Model Domain)

### 4.1 Pillar (Pilar)
Tiga pilar utama, preloaded, bisa ditambah/edit user.
- id, name, iconName, colorKey, order

Preloaded:
- Tubuh (icon: barbell, warna: amber) — workout rumahan + nutrisi
- Cyber & Income (icon: shield/terminal, warna: teal) — roadmap + cari klien
- Diri (icon: user-heart, warna: purple) — Inggris, presentasi, karakter

### 4.2 Task (Tugas)
Unit dasar yang dicentang. Bisa recurring atau one-off.
- id, title, description, pillarId
- tier: WAJIB | BONUS  (WAJIB = lantai minimum harian)
- recurrence: DAILY | WEEKLY | MONTHLY | ONCE
- targetMinutes (nullable — mis. "cyber 20 menit")
- daysOfWeek (buat WEEKLY spesifik hari, nullable)
- active (boolean), order, createdAt

### 4.3 TaskCompletion (Riwayat centang)
- id, taskId, date (LocalDate), completed (boolean), note (nullable)
Satu baris per task per hari yang dicentang. Ini sumber data streak & tally.

### 4.4 ProgressCounter (Tracker cyber)
Angka yang diupdate manual, sinkron sama Progress Tracker di roadmap.
- id, label, currentValue, targetValue (nullable), order
Preloaded: Labs PortSwigger (/270+), Vuln class dikuasai (/22), Target live ditest,
Laporan disubmit, Laporan valid, Hall of Fame entries.

### 4.5 WeeklyReview
- id, weekStartDate, win, struggle, adjust, createdAt

### 4.6 Milestone (Bulanan, dari roadmap)
- id, month (1|2|3), title, description, done (boolean), targetDate (nullable)

### 4.7 JournalEntry (Catatan harian, opsional)
- id, date, text

### 4.8 AppSettings (via DataStore, bukan Room)
- activeTheme: MIDNIGHT | WARM_PAPER | CLEAN_LIGHT | FOREST_CALM
- dailyReminderTime, weeklyReviewReminderEnabled, startDate (default: hari pertama pakai app)

---

## 5. Tema (4 pilihan, ganti kapan aja di Settings)

Simpan sebagai Material 3 ColorScheme. Tema aktif disimpan di DataStore, apply ke seluruh app
tanpa restart. Warna referensi (hex) — samakan dengan preview yang sudah disetujui:

### MIDNIGHT OPERATOR (default)
- background #14171c, surface #1b1f26, surfaceInset #0f1216
- primary/amber #d9a441, secondary/teal #5fa9a0, danger #c1503f
- onSurface #EDE8DC, textDim #9aa1ac, textFaint #5b6270
- Font accent: monospace buat label kecil (HARI KE-, tier), sans buat body.

### WARM PAPER
- background #f4ecdd, surface #fbf6ec, border #e0d4bd
- primary #b5651d (rust), secondary #5a7d4f (sage), text #4a3f2c, textDim #8a7a5c

### CLEAN LIGHT
- background #ffffff, surface #f5f7fa, border #e8e8e8
- primary #3b6ef0 (blue), secondary #16a34a (green), text #1a1a1a, textDim #888

### FOREST CALM
- background #12211c, surface #1a2e27, border #244038
- primary #3fae7a / accentText #7dd3a8, secondary #e0b062, text #e8f0ea, textDim #8fa99c

Detail interaksi tema: checkbox WAJIB pakai warna primary saat checked; checkbox BONUS pakai
warna secondary saat checked (bedain visual wajib vs bonus).

---

## 6. Layar per Detail

### 6.1 Hari Ini
- Header: "HARI KE-XXX / 090" (dihitung dari startDate), nama hari, tanggal.
- Progress bar tipis: hari ke berapa dari 90.
- 2 stat chip: streak beruntun, completion rate (hari sukses / total hari berjalan).
- Kartu WAJIB: list task tier WAJIB hari ini + checkbox. "Hari sukses" = semua WAJIB kecentang.
- Kartu BONUS: list task tier BONUS hari ini + checkbox.
- Kartu Catatan Hari Ini: textfield 1-2 kalimat (JournalEntry).
- Quick nav ke Rencana (Hari/Minggu/Bulan).
- Auto-save tiap perubahan (jangan andelin tombol simpan doang).

Definisi streak: berturut-turut hari yang semua WAJIB-nya kecentang, mundur dari hari ini.
Aturan "jangan bolong 2 hari": kalau kemarin & hari sebelumnya dua-duanya gagal, tampilkan
peringatan lembut di Hari Ini (bukan blocking).

### 6.2 Rencana
- Toggle Harian / Mingguan / Bulanan.
- Harian: task hari ini per pilar, bisa reorder, edit, tambah.
- Mingguan: grid 7 hari, task terjadwal per hari; ringkasan berapa WAJIB kecentang minggu ini.
- Bulanan: kalender + milestone bulan berjalan; tandai hari yang "sukses".
- FAB tambah task: pilih pilar, tier, recurrence, target menit, hari (kalau weekly).

### 6.3 Progres
- Progress counter cyber: tiap item ada stepper +/- dan input angka, tampil current/target + bar.
- Auto-tally 90 hari (dihitung dari TaskCompletion): total hari sukses, rate, streak terpanjang,
  jumlah sesi per task BONUS penting (gym, presentasi, refleksi, kabar ortu, income).
- Milestone bulanan (dari roadmap) dengan checkbox.
- Weekly review: form (menang / macet / penyesuaian) + list review lampau.

### 6.4 Pengaturan
- Pilih tema (4 opsi, preview kecil).
- Kelola pilar & task (tambah/edit/hapus/aktif-nonaktif).
- Atur jam reminder harian + toggle reminder weekly review.
- Export data ke file JSON (share intent) + import dari JSON. Backup manual, karena no-cloud.
- Reset progress (dengan konfirmasi).
- Edit startDate & panjang periode (default 90 hari, tapi bisa diubah).

---

## 7. Home Screen Widget (Jetpack Glance)

Minimal 2 ukuran:
- **Kecil (2x2)**: hari ke-XXX, streak, jumlah WAJIB kecentang/total hari ini. Tap → buka app.
- **Sedang (4x2)**: di atas + list WAJIB hari ini dengan checkbox yang bisa dicentang LANGSUNG
  dari home screen (update DB via action callback). Ini fitur utama yang bikin app kepakai.
- Widget refresh tiap perubahan data & tiap ganti hari (pakai WorkManager/updateAll).
- Hormati tema aktif kalau memungkinkan (minimal light/dark ikut sistem).

---

## 8. Notifikasi

- Reminder harian di jam yang diset (default 08:00): "Cek rencana hari ini" + ringkas WAJIB tersisa.
- Reminder weekly review (default Minggu 19:00): "Waktunya review minggu ini."
- Reminder lembut kalau kemarin semua WAJIB bolong (biar ga sampai 2 hari beruntun).
- Semua via WorkManager (tahan reboot) + channel notifikasi terpisah.

---

## 9. Isi Preloaded (seed database saat first launch)

Semua di bawah ini di-seed sekali di first run, dan SEMUANYA bisa diedit/hapus user.

### Pilar Tubuh (WAJIB + BONUS)
- WAJIB harian: "Gerak badan 15 menit" (target 15)
- BONUS: "Gym/workout rumahan sesi penuh", "Catat protein ~120-130g", "Lari/jalan cardio"
- Task mingguan contoh (recurrence WEEKLY, hari tertentu): Upper (dorong), Cardio+core,
  Lower+posterior chain, Recovery aktif, Full-body conditioning, Cardio durasi panjang, Rest.
  (Isi latihan taruh di description tiap task — ambil dari program rumahan yang sudah disusun.)

### Pilar Cyber & Income (WAJIB + BONUS)
- WAJIB harian: "Cyber minimal 20 menit" (target 20) — lab/recon/1 writeup
- BONUS: "Sesi hunting/kerjaan klien (income)", "PortSwigger lab", "Baca 1 writeup",
  "JS analysis / recon target"
- Milestone bulanan (masuk tabel Milestone):
  - Bulan 1: Manual SQLi + XSS tanpa scanner; identify XSS context; ≥15 PortSwigger labs.
  - Bulan 2: Manual exploit semua OWASP Top 10 tanpa scanner; 35+ labs kumulatif; methodology checklist.
  - Bulan 3: 5+ program submissions; ≥1 reply triager; mulai dokumentasi portfolio.

### Pilar Diri (WAJIB + BONUS)
- WAJIB harian: "Inggris minimal 15 menit" (target 15) — app/nonton tanpa sub Indo/nulis EN
- BONUS: "Presentation rep (rekam 3-5 mnt)", "Refleksi karakter (1 hal baik ke orang lain)",
  "Kabar orang tua", "Nyicil topik TA / cek jadwal asprak"

### Progress Counter (seed)
- Labs PortSwigger (target 270), Vuln class dikuasai (target 22), Target live ditest (target null),
  Laporan disubmit (null), Laporan valid (null), Hall of Fame entries (null).

---

## 10. Build Phases (bangun bertahap — berhenti tiap akhir fase)

**Fase 0 — Scaffold**
Project Compose + Material 3, bottom nav 4 tab kosong, Room + DataStore setup, tema MIDNIGHT
hardcoded dulu. Pastikan build & jalan.

**Fase 1 — Data layer**
Semua entity Room, DAO, Repository, seeding preloaded content di first launch. Unit test seeding.

**Fase 2 — Hari Ini**
Layar Hari Ini fungsional penuh: checklist WAJIB/BONUS, streak, rate, catatan, auto-save.

**Fase 3 — Rencana & Progres**
View Harian/Mingguan/Bulanan, CRUD task, progress counter, weekly review, milestone.

**Fase 4 — Tema & Pengaturan**
4 tema switchable via DataStore, layar Pengaturan lengkap, export/import JSON.

**Fase 5 — Widget & Notifikasi**
Glance widget (2x2 & 4x2 dengan check dari home screen), WorkManager reminder.

**Fase 6 — Polish**
Animasi transisi, empty states, konfirmasi hapus/reset, ikon app, cek dark/light tiap tema,
aksesibilitas (content description, target sentuh ≥48dp).

---

## 11. Catatan Kualitas
- Tiap layar: state loading/empty/isi yang rapi.
- Bungkus angka yang tampil biar ga ada artefak float.
- Jangan overengineer. Satu user, offline. Simpel > canggih.
- Commit per fase biar gampang di-review & rollback.

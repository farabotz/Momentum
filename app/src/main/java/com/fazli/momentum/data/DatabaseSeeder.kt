package com.fazli.momentum.data

object DatabaseSeeder {
    fun getPreloadedPillars(): List<Pillar> = listOf(
        Pillar("tubuh", "Tubuh", "barbell", "amber", 1),
        Pillar("cyber", "Cyber & Income", "terminal", "teal", 2),
        Pillar("diri", "Diri", "user-heart", "purple", 3)
    )

    fun getPreloadedTasks(): List<Task> {
        val now = System.currentTimeMillis()
        return listOf(
            // Tubuh
            Task(
                id = "t_gerak_badan",
                title = "Gerak badan 15 menit",
                description = "Workout harian minimal",
                pillarId = "tubuh",
                tier = TaskTier.WAJIB,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = 15,
                daysOfWeek = null,
                active = true,
                order = 1,
                createdAt = now
            ),
            Task(
                id = "t_gym_session",
                title = "Gym/workout rumahan sesi penuh",
                description = "Latihan intensif otot",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 2,
                createdAt = now
            ),
            Task(
                id = "t_catat_protein",
                title = "Catat protein ~120-130g",
                description = "Target nutrisi harian",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 3,
                createdAt = now
            ),
            Task(
                id = "t_cardio",
                title = "Lari/jalan cardio",
                description = "Latihan kardiovaskular harian",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 4,
                createdAt = now
            ),
            Task(
                id = "t_upper_push",
                title = "Upper (dorong)",
                description = "Program rumahan: Pushups, pike pushups, dips",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "1", // Senin
                active = true,
                order = 5,
                createdAt = now
            ),
            Task(
                id = "t_cardio_core",
                title = "Cardio+core",
                description = "Program rumahan: Plank, leg raises, jogging",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "2", // Selasa
                active = true,
                order = 6,
                createdAt = now
            ),
            Task(
                id = "t_lower_posterior",
                title = "Lower+posterior chain",
                description = "Program rumahan: Squats, lunges, glute bridges",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "3", // Rabu
                active = true,
                order = 7,
                createdAt = now
            ),
            Task(
                id = "t_recovery_aktif",
                title = "Recovery aktif",
                description = "Program rumahan: Stretching, yoga ringan",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "4", // Kamis
                active = true,
                order = 8,
                createdAt = now
            ),
            Task(
                id = "t_full_body_cond",
                title = "Full-body conditioning",
                description = "Program rumahan: Burpees, mountain climbers",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "5", // Jumat
                active = true,
                order = 9,
                createdAt = now
            ),
            Task(
                id = "t_cardio_long",
                title = "Cardio durasi panjang",
                description = "Program rumahan: Jogging/sepeda > 45 menit",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "6", // Sabtu
                active = true,
                order = 10,
                createdAt = now
            ),
            Task(
                id = "t_rest_day",
                title = "Rest",
                description = "Istirahat total & pemulihan otot",
                pillarId = "tubuh",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.WEEKLY,
                targetMinutes = null,
                daysOfWeek = "7", // Minggu
                active = true,
                order = 11,
                createdAt = now
            ),

            // Cyber & Income
            Task(
                id = "t_cyber_min",
                title = "Cyber minimal 20 menit",
                description = "Lab/recon/1 writeup harian",
                pillarId = "cyber",
                tier = TaskTier.WAJIB,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = 20,
                daysOfWeek = null,
                active = true,
                order = 12,
                createdAt = now
            ),
            Task(
                id = "t_hunting_client",
                title = "Sesi hunting/kerjaan klien (income)",
                description = "Pekerjaan produktif menghasilkan uang",
                pillarId = "cyber",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 13,
                createdAt = now
            ),
            Task(
                id = "t_portswigger_lab",
                title = "PortSwigger lab",
                description = "Latihan lab Web Security Academy",
                pillarId = "cyber",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 14,
                createdAt = now
            ),
            Task(
                id = "t_read_writeup",
                title = "Baca 1 writeup",
                description = "Membaca laporan bug bounty/pentest",
                pillarId = "cyber",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 15,
                createdAt = now
            ),
            Task(
                id = "t_js_analysis",
                title = "JS analysis / recon target",
                description = "Analisis file JS & pengumpulan informasi",
                pillarId = "cyber",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 16,
                createdAt = now
            ),

            // Diri
            Task(
                id = "t_inggris_min",
                title = "Inggris minimal 15 menit",
                description = "App/nonton tanpa sub Indo/nulis EN",
                pillarId = "diri",
                tier = TaskTier.WAJIB,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = 15,
                daysOfWeek = null,
                active = true,
                order = 17,
                createdAt = now
            ),
            Task(
                id = "t_presentation_rep",
                title = "Presentation rep (rekam 3-5 mnt)",
                description = "Latihan berbicara dan presentasi",
                pillarId = "diri",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 18,
                createdAt = now
            ),
            Task(
                id = "t_refleksi_karakter",
                title = "Refleksi karakter (1 hal baik ke orang lain)",
                description = "Melakukan kebaikan kecil harian",
                pillarId = "diri",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 19,
                createdAt = now
            ),
            Task(
                id = "t_kabar_ortu",
                title = "Kabar orang tua",
                description = "Menghubungi orang tua",
                pillarId = "diri",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 20,
                createdAt = now
            ),
            Task(
                id = "t_topik_ta",
                title = "Nyicil topik TA / cek jadwal asprak",
                description = "Persiapan akademik semester depan",
                pillarId = "diri",
                tier = TaskTier.BONUS,
                recurrence = TaskRecurrence.DAILY,
                targetMinutes = null,
                daysOfWeek = null,
                active = true,
                order = 21,
                createdAt = now
            )
        )
    }

    fun getPreloadedMilestones(): List<Milestone> = listOf(
        // Bulan 1
        Milestone("m_b1_1", 1, "Manual SQLi + XSS tanpa scanner", "Eksploitasi manual SQLi & XSS", false, null),
        Milestone("m_b1_2", 1, "Identify XSS context", "Menentukan konteks XSS pada target", false, null),
        Milestone("m_b1_3", 1, ">=15 PortSwigger labs", "Menyelesaikan minimal 15 lab PortSwigger", false, null),

        // Bulan 2
        Milestone("m_b2_1", 2, "Manual exploit semua OWASP Top 10 tanpa scanner", "Eksploitasi manual seluruh kerentanan OWASP Top 10", false, null),
        Milestone("m_b2_2", 2, "35+ labs kumulatif", "Menyelesaikan total 35+ lab PortSwigger", false, null),
        Milestone("m_b2_3", 2, "Methodology checklist", "Menyusun checklist metodologi pengujian", false, null),

        // Bulan 3
        Milestone("m_b3_1", 3, "5+ program submissions", "Mengirimkan minimal 5 laporan bug bounty", false, null),
        Milestone("m_b3_2", 3, ">=1 reply triager", "Mendapatkan minimal 1 tanggapan dari triager", false, null),
        Milestone("m_b3_3", 3, "Mulai dokumentasi portfolio", "Menyusun dokumentasi portfolio cyber security", false, null)
    )

    fun getPreloadedProgressCounters(): List<ProgressCounter> = listOf(
        ProgressCounter("c_portswigger", "Labs PortSwigger", 0, 270, 1),
        ProgressCounter("c_vuln", "Vuln class dikuasai", 0, 22, 2),
        ProgressCounter("c_target_live", "Target live ditest", 0, null, 3),
        ProgressCounter("c_report_submitted", "Laporan disubmit", 0, null, 4),
        ProgressCounter("c_report_valid", "Laporan valid", 0, null, 5),
        ProgressCounter("c_hof", "Hall of Fame entries", 0, null, 6)
    )
}

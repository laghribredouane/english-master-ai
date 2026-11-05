package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.EnglishCurriculum
import com.example.data.curriculum.SupabaseSqlBlueprint
import com.example.data.local.OfflinePackEntity
import com.example.data.local.OfflinePendingScoreEntity
import com.example.data.model.CatalogItem
import com.example.data.model.UserProfile
import com.example.util.StudyNotificationHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileAdminOfflineTabScreen(
    profile: UserProfile,
    isDarkTheme: Boolean,
    isGuestMode: Boolean = false,
    downloadedPacks: List<OfflinePackEntity>,
    pendingOfflineScores: List<OfflinePendingScoreEntity>,
    customCatalog: List<CatalogItem>,
    onToggleDarkTheme: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    onSaveProfile: (name: String, cefr: String, goalMinutes: Long, trackId: String) -> Unit,
    onDownloadOfflinePack: (packId: String, title: String, cefr: String, moduleType: String) -> Unit,
    onRemoveOfflinePack: (String) -> Unit,
    onSyncOfflineQueue: () -> Unit,
    onPublishAdminItem: (type: String, title: String, subtitle: String, cefr: String, body: String, fr: String, ar: String, q: String, opts: String, ans: String) -> Unit,
    onImportCsvBatch: (String) -> Unit,
    onDeleteCatalogItem: (String) -> Unit,
    onOpenSaveProgressAuth: () -> Unit = {},
    onSignOut: () -> Unit
) {
    var activeSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "👤 Profil & Rappels",
        "💾 Hors Ligne",
        "🎁 Abonnements",
        "⚙️ Admin & SQL"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = activeSubTab, edgePadding = 12.dp) {
            subTabs.forEachIndexed { idx, label ->
                Tab(
                    selected = activeSubTab == idx,
                    onClick = { activeSubTab = idx },
                    text = { Text(label) },
                    modifier = Modifier.testTag("profile_subtab_$idx")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeSubTab) {
                0 -> UserProfileAndNotificationsPane(
                    profile = profile,
                    isDarkTheme = isDarkTheme,
                    isGuestMode = isGuestMode,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onChangeLanguage = onChangeLanguage,
                    onSaveProfile = onSaveProfile,
                    onOpenSaveProgressAuth = onOpenSaveProgressAuth,
                    onSignOut = onSignOut
                )
                1 -> OfflineModePane(
                    downloadedPacks = downloadedPacks,
                    pendingOfflineScores = pendingOfflineScores,
                    onDownloadOfflinePack = onDownloadOfflinePack,
                    onRemoveOfflinePack = onRemoveOfflinePack,
                    onSyncOfflineQueue = onSyncOfflineQueue
                )
                2 -> SubscriptionsPane(currentTier = profile.subscriptionTier)
                3 -> AdminAndSqlStudioPane(
                    customCatalog = customCatalog,
                    onPublishAdminItem = onPublishAdminItem,
                    onImportCsvBatch = onImportCsvBatch,
                    onDeleteCatalogItem = onDeleteCatalogItem
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserProfileAndNotificationsPane(
    profile: UserProfile,
    isDarkTheme: Boolean,
    isGuestMode: Boolean,
    onToggleDarkTheme: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    onSaveProfile: (String, String, Long, String) -> Unit,
    onOpenSaveProgressAuth: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var selectedCefr by remember(profile.cefrLevel) { mutableStateOf(profile.cefrLevel) }
    var selectedGoal by remember(profile.dailyGoalMinutes) { mutableStateOf(profile.dailyGoalMinutes) }
    var notifStatus by remember { mutableStateOf<String?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            StudyNotificationHelper.sendStudyReminderNotification(
                context = context,
                title = "English Master AI • Rappel Quotidien 🔥",
                message = "Il vous reste ${profile.dailyGoalMinutes} min pour maintenir votre série de ${profile.streakDays} jour(s) !"
            )
            notifStatus = "Notification push envoyée avec succès !"
        } else {
            notifStatus = "Permission de notification refusée."
        }
    }

    // Account Mode Banner (Guest Mode 👤 vs Connected Cloud Account 🔐)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isGuestMode) "👤 Mode Invité (Stockage Local sur l'appareil)" else "🔐 Compte Connecté & Synchronisé Cloud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isGuestMode) {
                    "Vous avez cumulé ⭐ ${profile.xp} XP, 📚 ${profile.lessonsCompleted} leçons et 🧠 ${profile.wordsLearned} mots appris sur cet appareil. Connectez un compte pour ne jamais perdre votre progression et changer d'appareil !"
                } else {
                    "Synchronisé avec ${profile.email ?: profile.displayName} • Vos XP ⭐, flashcards 🧠 et certificats 🏆 sont sauvegardés dans le cloud."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            if (isGuestMode) {
                Button(
                    onClick = onOpenSaveProgressAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_convert_guest_button")
                ) {
                    Text("💾 Sauvegarder ma progression (Créer / Connecter un compte)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("👤 Profil & Préférences", style = MaterialTheme.typography.titleLarge)
                }
                OutlinedButton(onClick = onToggleDarkTheme, modifier = Modifier.testTag("toggle_theme_button")) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isDarkTheme) "☀️ Clair" else "🌙 Sombre")
                }
            }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("👤 Nom d'affichage") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("🌍 Langue de l'interface (Support RTL automatique en Arabe) :", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("fr" to "🇫🇷 Français", "en" to "🇬🇧 English", "ar" to "🇸🇦 العربية (RTL)").forEach { (code, label) ->
                    FilterChip(
                        selected = profile.uiLanguage == code,
                        onClick = { onChangeLanguage(code) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("profile_lang_$code")
                    )
                }
            }

            Text("🎯 Niveau CECRL Actuel :", fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { lvl ->
                    FilterChip(
                        selected = selectedCefr == lvl,
                        onClick = { selectedCefr = lvl },
                        label = { Text(lvl) }
                    )
                }
            }

            Text("🎯 Objectif Quotidien (Minutes / Jour) :", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10L, 15L, 20L, 30L, 45L).forEach { mins ->
                    FilterChip(
                        selected = selectedGoal == mins,
                        onClick = { selectedGoal = mins },
                        label = { Text("$mins min") }
                    )
                }
            }

            Button(
                onClick = { onSaveProfile(nameInput, selectedCefr, selectedGoal, profile.selectedTrackId) },
                modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
            ) {
                Text(if (isGuestMode) "💾 Enregistrer mon profil localement" else "☁️ Enregistrer & Synchroniser le Profil")
            }
        }
    }

    // Notifications Section (Section 10)
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🔔 Notifications & Rappels Intelligents", style = MaterialTheme.typography.titleMedium)
            }
            Text("Recevez des rappels quotidiens 🔥, alertes de révision SRS 🧠 et nouveaux défis 🏆.", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        StudyNotificationHelper.sendStudyReminderNotification(
                            context = context,
                            title = "English Master AI • Rappel Quotidien 🔥",
                            message = "Pratiquez ${profile.dailyGoalMinutes} min aujourd'hui pour garder votre série !"
                        )
                        notifStatus = "Notification envoyée !"
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("send_reminder_notification_button")
            ) {
                Text("🔔 Tester le Rappel de Révision maintenant")
            }
            if (notifStatus != null) {
                Text(notifStatus!!, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (!isGuestMode) {
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("🚪 Déconnexion (${profile.email ?: profile.displayName})")
        }
    }
}

@Composable
private fun OfflineModePane(
    downloadedPacks: List<OfflinePackEntity>,
    pendingOfflineScores: List<OfflinePendingScoreEntity>,
    onDownloadOfflinePack: (String, String, String, String) -> Unit,
    onRemoveOfflinePack: (String) -> Unit,
    onSyncOfflineQueue: () -> Unit
) {
    Text("Mode Hors Ligne & Synchronisation (Room SQLite)", style = MaterialTheme.typography.titleLarge)
    Text(
        "Téléchargez vos leçons, flashcards SRS et exercices pour étudier dans l'avion ou le métro. Vos scores hors ligne sont synchronisés automatiquement.",
        style = MaterialTheme.typography.bodyMedium
    )

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("File d'attente de synchronisation : ${pendingOfflineScores.size} activité(s)", fontWeight = FontWeight.Bold)
                Button(onClick = onSyncOfflineQueue, modifier = Modifier.testTag("sync_offline_button")) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Synchroniser")
                }
            }
        }
    }

    val availablePacks = listOf(
        Triple("pack_a1_a2", "Pack Fondations A1-A2 (Vocab + Grammaire)", "A2"),
        Triple("pack_b1_b2", "Pack Business & Speaking B1-B2", "B2"),
        Triple("pack_c1_c2", "Pack Examens IELTS / TOEFL / C1-C2", "C1")
    )

    availablePacks.forEach { (id, title, cefr) ->
        val isDownloaded = downloadedPacks.any { it.packId == id }
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text("6 leçons • 25 flashcards • 15 exercices hors ligne", style = MaterialTheme.typography.bodyMedium)
                }
                if (isDownloaded) {
                    OutlinedButton(onClick = { onRemoveOfflinePack(id) }) {
                        Text("Supprimer")
                    }
                } else {
                    Button(
                        onClick = { onDownloadOfflinePack(id, title, cefr, "complete_pack") },
                        modifier = Modifier.testTag("download_$id")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Télécharger")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionsPane(currentTier: String) {
    var selectedPlanForConfig by remember { mutableStateOf<String?>(null) }

    Text("Abonnements & Formules (Section 12)", style = MaterialTheme.typography.titleLarge)
    Text(
        "Formule actuelle : $currentTier. Conformément aux règles de sécurité de production, aucun paiement fictif n'est simulé.",
        style = MaterialTheme.typography.bodyMedium
    )

    val plans = listOf(
        Triple("Free", "0 € / mois", "Apprentissage de base A1-C2, test de niveau, flashcards SRS et quiz quotidiens."),
        Triple("Premium", "9,99 € / mois", "10 Parcours complets (IELTS, TOEFL, TOEIC, Business), Mode Hors Ligne illimité & statistiques avancées."),
        Triple("AI Pro", "19,99 € / mois", "Conversations illimitées 24h/24 avec Prof. Victoria, correction d'essais C1/C2 et coaching vocal UK/US."),
        Triple("Family", "29,99 € / mois", "Jusqu'à 6 profils personnalisés synchronisés sous un même abonnement familial.")
    )

    plans.forEach { (tier, price, features) ->
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tier, style = MaterialTheme.typography.titleLarge)
                    }
                    AssistChip(onClick = {}, label = { Text(price) })
                }
                Text(features, style = MaterialTheme.typography.bodyMedium)
                if (tier == currentTier) {
                    AssistChip(onClick = {}, label = { Text("✔ Formule Active") })
                } else {
                    Button(
                        onClick = { selectedPlanForConfig = tier },
                        modifier = Modifier.fillMaxWidth().testTag("plan_select_$tier")
                    ) {
                        Text("Configurer l'abonnement $tier")
                    }
                }
            }
        }
    }

    if (selectedPlanForConfig != null) {
        AlertDialog(
            onDismissRequest = { selectedPlanForConfig = null },
            title = { Text("Configuration Passerelle de Paiement ($selectedPlanForConfig)") },
            text = {
                Text(
                    "Règle de sécurité stricte : English Master AI ne simule jamais un paiement réussi.\n\n" +
                        "Pour activer la facturation réelle pour l'offre '$selectedPlanForConfig' (Google Play Billing / Stripe Checkout & Webhooks), " +
                        "veuillez renseigner STRIPE_PUBLISHABLE_KEY et le webhook serveur dans le panneau Secrets d'AI Studio."
                )
            },
            confirmButton = {
                Button(onClick = { selectedPlanForConfig = null }) {
                    Text("Compris")
                }
            }
        )
    }
}

@Composable
private fun AdminAndSqlStudioPane(
    customCatalog: List<CatalogItem>,
    onPublishAdminItem: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onImportCsvBatch: (String) -> Unit,
    onDeleteCatalogItem: (String) -> Unit
) {
    var itemType by remember { mutableStateOf("vocabulary") }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var cefr by remember { mutableStateOf("B2") }
    var contentBody by remember { mutableStateOf("") }
    var translationFr by remember { mutableStateOf("") }
    var translationAr by remember { mutableStateOf("") }
    var quizQuestion by remember { mutableStateOf("") }
    var quizOptionsCsv by remember { mutableStateOf("Option A,Option B,Option C,Option D") }
    var quizAnswer by remember { mutableStateOf("Option A") }
    var csvBatchInput by remember {
        mutableStateOf(
            "synergy;synergie;تآزر;C1;Cross-team synergy accelerated the launch.\n" +
                "pivot;pivoter / réorienter;تحول استراتيجي;B2;The startup decided to pivot its strategy."
        )
    }
    var showSqlBlueprint by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Panneau Administrateur & CMS Cloud", style = MaterialTheme.typography.titleLarge)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ajouter un Cours, Leçon, Mot ou Exercice dans Firestore", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("course", "lesson", "vocabulary", "exercise").forEach { t ->
                    FilterChip(selected = itemType == t, onClick = { itemType = t }, label = { Text(t) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A1", "A2", "B1", "B2", "C1", "C2").forEach { l ->
                    FilterChip(selected = cefr == l, onClick = { cefr = l }, label = { Text(l) })
                }
            }
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre / Mot anglais") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Sous-titre / Catégorie") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = translationFr, onValueChange = { translationFr = it }, label = { Text("Traduction Française") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = translationAr, onValueChange = { translationAr = it }, label = { Text("Traduction Arabe (العربية)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contentBody, onValueChange = { contentBody = it }, label = { Text("Contenu riche / Phrase d'exemple") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onPublishAdminItem(
                            itemType,
                            title,
                            subtitle.ifBlank { "Module $cefr" },
                            cefr,
                            contentBody.ifBlank { "Example for $title" },
                            translationFr.ifBlank { title },
                            translationAr.ifBlank { title },
                            quizQuestion.ifBlank { "Question on $title" },
                            quizOptionsCsv,
                            quizAnswer
                        )
                        title = ""
                        contentBody = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("admin_publish_button")
            ) {
                Text("Publier dans le Catalogue Cloud")
            }
        }
    }

    // CSV / Excel Batch Import
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Importation en lot CSV / Excel (mot;fr;ar;niveau;exemple)", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = csvBatchInput,
                onValueChange = { csvBatchInput = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onImportCsvBatch(csvBatchInput) },
                modifier = Modifier.fillMaxWidth().testTag("admin_csv_import_button")
            ) {
                Text("Importer le lot CSV dans Firestore")
            }
        }
    }

    // Manage Published Catalog Items
    if (customCatalog.isNotEmpty()) {
        Text("Contenus Publiés (${customCatalog.size})", style = MaterialTheme.typography.titleMedium)
        customCatalog.forEach { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${item.title} [${item.itemType} • ${item.cefrLevel}]", fontWeight = FontWeight.Bold)
                        Text("🇫🇷 ${item.translationFr} | 🇸🇦 ${item.translationAr}", style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { onDeleteCatalogItem(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }

    // Supabase 29-Table SQL & RLS Schema Viewer
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Architecture Base de Données (Firestore + Script SQL Supabase 29 Tables)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tables incluses dans /docs/SUPABASE_SCHEMA.sql : ${SupabaseSqlBlueprint.all28Tables.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = { showSqlBlueprint = !showSqlBlueprint },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showSqlBlueprint) "Masquer le script SQL RLS" else "Afficher le script SQL & RLS complet")
            }
            if (showSqlBlueprint) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = SupabaseSqlBlueprint.sqlScriptSummary,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

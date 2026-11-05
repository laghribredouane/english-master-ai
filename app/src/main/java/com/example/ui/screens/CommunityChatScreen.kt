package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CommunityMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommunityRoomInfo(
    val id: String,
    val labelFr: String,
    val badgeCefr: String,
    val icebreakerTopic: String,
    val starterTemplate: String
)

private val COMMUNITY_ROOMS = listOf(
    CommunityRoomInfo(
        id = "general",
        labelFr = "🌍 Salon Général",
        badgeCefr = "A1-C2",
        icebreakerTopic = "Introduce yourself and share why you are learning English!",
        starterTemplate = "Hi everyone! I am learning English because I want to "
    ),
    CommunityRoomInfo(
        id = "a1_a2",
        labelFr = "🌱 Débutants A1-A2",
        badgeCefr = "A1-A2",
        icebreakerTopic = "What is your favorite daily routine or hobby?",
        starterTemplate = "Every morning, I usually like to "
    ),
    CommunityRoomInfo(
        id = "b1_b2",
        labelFr = "💬 Conversation B1-B2",
        badgeCefr = "B1-B2",
        icebreakerTopic = "If you could travel anywhere next month, where would you go and why?",
        starterTemplate = "If I could travel anywhere next month, I would visit "
    ),
    CommunityRoomInfo(
        id = "business_exams",
        labelFr = "💼 Business & IELTS/TOEFL",
        badgeCefr = "C1-C2",
        icebreakerTopic = "How is remote work or AI changing your industry?",
        starterTemplate = "From my professional perspective, remote work has significantly "
    ),
    CommunityRoomInfo(
        id = "daily_debate",
        labelFr = "🔥 Débat du Jour",
        badgeCefr = "B1-C2",
        icebreakerTopic = "Do you prefer reading books or watching documentaries to learn new skills?",
        starterTemplate = "In my opinion, learning through books is more effective because "
    )
)

@Composable
fun CommunityChatScreen(
    profile: UserProfile,
    selectedRoomId: String,
    selectedTandemPeer: Pair<String, String>?,
    messages: List<CommunityMessage>,
    activeLearners: List<LeaderboardEntry>,
    aiDraftSuggestion: String?,
    isAiLoading: Boolean,
    onSelectChannel: (String) -> Unit,
    onStartTandemWithPeer: (String, String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSubmitPeerCorrection: (String, String) -> Unit,
    onLikeMessage: (String, Long) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onCheckDraftWithAi: (String) -> Unit,
    onDismissDraftSuggestion: () -> Unit,
    onSpeakText: (String) -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    var messageBeingCorrected by remember { mutableStateOf<CommunityMessage?>(null) }
    var correctionDraft by remember { mutableStateOf("") }

    val activeRoomInfo = COMMUNITY_ROOMS.find { it.id == selectedRoomId } ?: COMMUNITY_ROOMS.first()
    val otherLearners = remember(activeLearners, profile.userId) {
        activeLearners.filter { it.userId.isNotBlank() && it.userId != profile.userId }
    }

    if (messageBeingCorrected != null) {
        val targetMsg = messageBeingCorrected!!
        AlertDialog(
            onDismissRequest = { messageBeingCorrected = null },
            title = { Text("Suggérer une correction bienveillante (+15 XP)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Message de ${targetMsg.senderName} :",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = targetMsg.content,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedTextField(
                        value = correctionDraft,
                        onValueChange = { correctionDraft = it },
                        label = { Text("Votre correction ou reformulation en anglais") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("peer_correction_input"),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (correctionDraft.isNotBlank()) {
                            onSubmitPeerCorrection(targetMsg.id, correctionDraft)
                            messageBeingCorrected = null
                            correctionDraft = ""
                        }
                    },
                    modifier = Modifier.testTag("submit_peer_correction_button")
                ) {
                    Text("Envoyer la correction")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageBeingCorrected = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header Card: Thematic Channels + Tandem 1-to-1 Partners
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = "Community Lounge",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                text = "Espace Conversation Utilisateurs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Pratiquez en salons par niveau ou en Tandem 1-to-1 (+10 XP/msg)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (selectedTandemPeer != null) "Tandem 1:1" else activeRoomInfo.badgeCefr,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Channel selector chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    COMMUNITY_ROOMS.forEach { room ->
                        FilterChip(
                            selected = selectedTandemPeer == null && selectedRoomId == room.id,
                            onClick = { onSelectChannel(room.id) },
                            label = { Text(room.labelFr) },
                            modifier = Modifier.testTag("community_room_${room.id}")
                        )
                    }
                }

                // Active learners for 1-to-1 Tandem Chat
                if (otherLearners.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Handshake,
                                contentDescription = "Tandem Partners",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Partenaires Tandem :",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        otherLearners.forEach { learner ->
                            val isSelectedPeer = selectedTandemPeer?.first == learner.userId
                            AssistChip(
                                onClick = { onStartTandemWithPeer(learner.userId, learner.displayName) },
                                label = {
                                    Text(
                                        text = "${learner.displayName} (${learner.cefrLevel})",
                                        fontWeight = if (isSelectedPeer) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("tandem_partner_${learner.userId}")
                            )
                        }
                    }
                }
            }
        }

        // 2. Active Tandem Banner or Room Icebreaker Prompt
        if (selectedTandemPeer != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                            Text(
                                text = "Conversation Tandem avec ${selectedTandemPeer.second}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Échangez en anglais et corrigez-vous mutuellement en temps réel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    TextButton(
                        onClick = { onSelectChannel("general") },
                        modifier = Modifier.testTag("exit_tandem_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Retour au salon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salon public")
                    }
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Sujet suggéré",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Sujet de discussion :",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeRoomInfo.icebreakerTopic,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(
                        onClick = { messageInput = activeRoomInfo.starterTemplate },
                        modifier = Modifier.testTag("use_icebreaker_button")
                    ) {
                        Text("Utiliser")
                    }
                }
            }
        }

        // 3. Messages Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (selectedTandemPeer != null) {
                                    "Démarrez la conversation Tandem avec ${selectedTandemPeer.second} !"
                                } else {
                                    "Aucun message dans ce salon pour le moment."
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Envoyez un premier message en anglais pour briser la glace et gagner +10 XP !",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { messageInput = activeRoomInfo.starterTemplate },
                                modifier = Modifier.testTag("empty_room_starter_button")
                            ) {
                                Text("Pré-remplir une phrase d'introduction")
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id.ifBlank { it.content + it.senderId } }) { msg ->
                val isOwnMessage = msg.senderId == profile.userId
                CommunityMessageCard(
                    message = msg,
                    isOwnMessage = isOwnMessage,
                    onSpeak = { onSpeakText(msg.content) },
                    onLike = { onLikeMessage(msg.id, msg.likesCount) },
                    onOpenCorrectionDialog = {
                        correctionDraft = msg.correctionSuggestion.ifBlank { msg.content }
                        messageBeingCorrected = msg
                    },
                    onStartTandemWithSender = {
                        if (!isOwnMessage && msg.senderId.isNotBlank()) {
                            onStartTandemWithPeer(msg.senderId, msg.senderName)
                        }
                    },
                    onDelete = {
                        if (isOwnMessage && msg.id.isNotBlank()) {
                            onDeleteMessage(msg.id)
                        }
                    }
                )
            }
        }

        // 4. Optional Pre-Send AI Grammar Check Banner
        if (!aiDraftSuggestion.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ Suggestion IA avant envoi :",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        IconButton(
                            onClick = onDismissDraftSuggestion,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                    Text(
                        text = aiDraftSuggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                messageInput = aiDraftSuggestion
                                onDismissDraftSuggestion()
                            },
                            modifier = Modifier.testTag("apply_ai_draft_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Utiliser cette formulation")
                        }
                    }
                }
            }
        }

        // 5. Composer Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            if (selectedTandemPeer != null) {
                                "Écrire à ${selectedTandemPeer.second} en anglais..."
                            } else {
                                "Écrivez aux autres apprenants en anglais..."
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("community_message_input"),
                    maxLines = 3
                )
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onCheckDraftWithAi(messageInput)
                        }
                    },
                    enabled = messageInput.isNotBlank() && !isAiLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("community_ai_check_button")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Vérifier mon anglais avec l'IA",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Button(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(messageInput)
                            messageInput = ""
                        }
                    },
                    enabled = messageInput.isNotBlank(),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("community_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                }
            }
        }
    }
}

@Composable
private fun CommunityMessageCard(
    message: CommunityMessage,
    isOwnMessage: Boolean,
    onSpeak: () -> Unit,
    onLike: () -> Unit,
    onOpenCorrectionDialog: () -> Unit,
    onStartTandemWithSender: () -> Unit,
    onDelete: () -> Unit
) {
    val timeText = remember(message.createdAt) {
        val date = message.createdAt?.toDate() ?: Date()
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isOwnMessage) 18.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sender Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable(enabled = !isOwnMessage) {
                            onStartTandemWithSender()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message.senderName.take(1).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isOwnMessage) "${message.senderName} (Vous)" else message.senderName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = message.senderCefr,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (!isOwnMessage) {
                                Text(
                                    text = "Appuyez pour discuter en Tandem 1-to-1",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Message Content
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Peer Grammar Correction Box (if any)
                if (message.correctionSuggestion.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "✏️ Correction suggérée par ${message.correctedByName.ifBlank { "un membre" }} :",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = message.correctionSuggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Action Bar: Listen (TTS), Like, Peer Correction, Tandem DM, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Écouter la prononciation",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        TextButton(
                            onClick = onLike,
                            modifier = Modifier.testTag("like_msg_${message.id}")
                        ) {
                            Icon(
                                Icons.Default.ThumbUp,
                                contentDescription = "Encourager",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${message.likesCount}")
                        }

                        TextButton(
                            onClick = onOpenCorrectionDialog,
                            modifier = Modifier.testTag("correct_msg_${message.id}")
                        ) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Corriger",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Corriger")
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isOwnMessage) {
                            TextButton(
                                onClick = onStartTandemWithSender,
                                modifier = Modifier.testTag("tandem_msg_${message.id}")
                            ) {
                                Icon(
                                    Icons.Default.Handshake,
                                    contentDescription = "Tandem 1-to-1",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tandem")
                            }
                        } else {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_msg_${message.id}")
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer mon message",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.curriculum.LocalizedStrings
import com.example.ui.theme.FeatherGreen
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SunnyYellow
import com.example.ui.theme.VibrantOrange

const val ROUTE_HOME = "home"
const val ROUTE_LEARN = "learn"
const val ROUTE_PRACTICE = "practice"
const val ROUTE_RANKING = "ranking"
const val ROUTE_PROFILE = "profile"

enum class EnglishMasterBottomTab(
    val route: String,
    val emojiTitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTag: String,
    val description: String
) {
    HOME(
        route = ROUTE_HOME,
        emojiTitle = "🏠 Home",
        icon = Icons.Default.Home,
        accentColor = FeatherGreen,
        testTag = "nav_tab_dashboard",
        description = "Parcours vertical en 7 unités (Basics, Greetings, Food, Home, Travel, Work, Conversation), série quotidienne 🔥, XP ⭐ et vies ❤️."
    ),
    LEARN(
        route = ROUTE_LEARN,
        emojiTitle = "📚 Learn",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        accentColor = SkyBlue,
        testTag = "nav_tab_learn",
        description = "Modules complets A1–C2 : Vocabulaire 🧠, Grammaire 📐, Listening 🎧, Speaking 🎤, Reading 📖 et Writing ✍️."
    ),
    PRACTICE(
        route = ROUTE_PRACTICE,
        emojiTitle = "🎮 Practice",
        icon = Icons.Default.SportsEsports,
        accentColor = VibrantOrange,
        testTag = "nav_tab_practice",
        description = "Quiz multi-formats 🎮, Tuteur IA Prof. Victoria 🤖 et Salon de conversation & Tandem 💬."
    ),
    RANKING(
        route = ROUTE_RANKING,
        emojiTitle = "🏆 Ranking",
        icon = Icons.Default.EmojiEvents,
        accentColor = SunnyYellow,
        testTag = "nav_tab_ranking",
        description = "Classement mondial 🏆, défis hebdomadaires 🎯, galerie de badges et sauvegarde Cloud."
    ),
    PROFILE(
        route = ROUTE_PROFILE,
        emojiTitle = "👤 Profile",
        icon = Icons.Default.Person,
        accentColor = RoyalPurple,
        testTag = "nav_tab_profile",
        description = "Profil Invité 👤 ou Connecté 🔐, langues FR/EN/AR RTL 🌍, Mode Sombre, packs hors-ligne et abonnements."
    );

    fun localizedLabel(strings: LocalizedStrings?): String {
        if (strings == null) return emojiTitle
        return when (this) {
            HOME -> strings.tabDashboard
            LEARN -> strings.tabLearn
            PRACTICE -> strings.tabPractice
            RANKING -> strings.tabRanking
            PROFILE -> strings.tabProfile
        }
    }
}

/**
 * Reusable Bottom Navigation Bar for the 5 tabs: Home, Learn, Practice, Ranking, and Profile.
 */
@Composable
fun EnglishMasterBottomNavigationBar(
    navController: NavHostController,
    strings: LocalizedStrings? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        EnglishMasterBottomTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            val label = tab.localizedLabel(strings)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}

/**
 * Navigation Graph configuring the 5 tabs (Home, Learn, Practice, Ranking, Profile).
 * Provides default themed placeholder screens for each destination, which can be overridden with full screen composables.
 */
@Composable
fun EnglishMasterNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    homeContent: @Composable () -> Unit = { HomePlaceholderScreen() },
    learnContent: @Composable () -> Unit = { LearnPlaceholderScreen() },
    practiceContent: @Composable () -> Unit = { PracticePlaceholderScreen() },
    rankingContent: @Composable () -> Unit = { RankingPlaceholderScreen() },
    profileContent: @Composable () -> Unit = { ProfilePlaceholderScreen() }
) {
    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
        modifier = modifier
    ) {
        composable(route = ROUTE_HOME) {
            homeContent()
        }
        composable(route = ROUTE_LEARN) {
            learnContent()
        }
        composable(route = ROUTE_PRACTICE) {
            practiceContent()
        }
        composable(route = ROUTE_RANKING) {
            rankingContent()
        }
        composable(route = ROUTE_PROFILE) {
            profileContent()
        }
    }
}

@Composable
fun HomePlaceholderScreen() {
    GamifiedTabPlaceholderCard(tab = EnglishMasterBottomTab.HOME)
}

@Composable
fun LearnPlaceholderScreen() {
    GamifiedTabPlaceholderCard(tab = EnglishMasterBottomTab.LEARN)
}

@Composable
fun PracticePlaceholderScreen() {
    GamifiedTabPlaceholderCard(tab = EnglishMasterBottomTab.PRACTICE)
}

@Composable
fun RankingPlaceholderScreen() {
    GamifiedTabPlaceholderCard(tab = EnglishMasterBottomTab.RANKING)
}

@Composable
fun ProfilePlaceholderScreen() {
    GamifiedTabPlaceholderCard(tab = EnglishMasterBottomTab.PROFILE)
}

@Composable
private fun GamifiedTabPlaceholderCard(tab: EnglishMasterBottomTab) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("placeholder_${tab.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = tab.accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(92.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mascot_barnaby_1793914916960),
                        contentDescription = tab.emojiTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = tab.emojiTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tab.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

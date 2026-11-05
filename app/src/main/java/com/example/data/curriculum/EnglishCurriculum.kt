package com.example.data.curriculum

data class LocalizedStrings(
    val appTitle: String,
    val tagline: String,
    val tabDashboard: String,
    val tabLearn: String,
    val tabTutor: String,
    val tabPractice: String,
    val tabRanking: String,
    val tabProfile: String,
    val levelLabel: String,
    val xpLabel: String,
    val streakLabel: String,
    val dailyGoalLabel: String,
    val startPlacementTest: String,
    val vocabTitle: String,
    val grammarTitle: String,
    val listeningTitle: String,
    val speakingTitle: String,
    val readingTitle: String,
    val writingTitle: String,
    val pathsTitle: String,
    val offlineModeTitle: String,
    val adminPanelTitle: String,
    val subscriptionTitle: String,
    val signOutLabel: String
)

object AppLocalization {
    fun forLang(lang: String): LocalizedStrings = when (lang) {
        "ar" -> LocalizedStrings(
            appTitle = "English Master AI",
            tagline = "تعلّم الإنجليزية بمتعة وذكاء من A1 إلى C2",
            tabDashboard = "🏠 الرئيسية",
            tabLearn = "📚 الدروس",
            tabTutor = "🤖 المعلم",
            tabPractice = "🎮 التمارين",
            tabRanking = "🏆 الترتيب",
            tabProfile = "👤 حسابي",
            levelLabel = "🎯 المستوى الحالي",
            xpLabel = "⭐ نقاط الخبرة XP",
            streakLabel = "🔥 سلسلة الأيام",
            dailyGoalLabel = "🎯 الهدف اليومي",
            startPlacementTest = "🎯 اختبار تحديد المستوى الذكي (A1-C2)",
            vocabTitle = "🧠 المفردات والبطاقات الذكية",
            grammarTitle = "📖 القواعد والأزمنة",
            listeningTitle = "🎧 الاستماع والإملاء التفاعلي",
            speakingTitle = "🎤 المحادثة والنطق الذكي",
            readingTitle = "📖 القراءة والنصوص المهنية",
            writingTitle = "✍️ الكتابة والتصحيح بالذكاء الاصطناعي",
            pathsTitle = "🏆 المسارات التعليمية والشهادات",
            offlineModeTitle = "💾 وضع عدم الاتصال (Offline)",
            adminPanelTitle = "⚙️ لوحة الإدارة والمحتوى",
            subscriptionTitle = "🎁 الاشتراكات والباقات",
            signOutLabel = "تسجيل الخروج"
        )
        "en" -> LocalizedStrings(
            appTitle = "English Master AI",
            tagline = "Master English playfully from A1 to C2",
            tabDashboard = "🏠 Home",
            tabLearn = "📚 Learn",
            tabTutor = "🤖 AI Tutor",
            tabPractice = "🎮 Practice",
            tabRanking = "🏆 Ranking",
            tabProfile = "👤 Profile",
            levelLabel = "🎯 Current Level",
            xpLabel = "⭐ Total XP",
            streakLabel = "🔥 Day Streak",
            dailyGoalLabel = "🎯 Daily Goal",
            startPlacementTest = "🎯 Adaptive Placement Test (A1-C2)",
            vocabTitle = "🧠 Vocabulary & Spaced Flashcards",
            grammarTitle = "📖 Grammar & All Tenses",
            listeningTitle = "🎧 Listening, Podcasts & Dictation",
            speakingTitle = "🎤 Speaking & Pronunciation Lab",
            readingTitle = "📖 Reading & Interactive Glossaries",
            writingTitle = "✍️ AI Writing & Email Coach",
            pathsTitle = "🏆 Learning Paths & Certificates",
            offlineModeTitle = "💾 Offline Mode & Sync",
            adminPanelTitle = "⚙️ Admin & Content Studio",
            subscriptionTitle = "🎁 Subscriptions & Plans",
            signOutLabel = "Sign Out"
        )
        else -> LocalizedStrings(
            appTitle = "English Master AI",
            tagline = "Apprenez l'anglais en vous amusant de A1 à C2",
            tabDashboard = "🏠 Home",
            tabLearn = "📚 Learn",
            tabTutor = "🤖 AI Tutor",
            tabPractice = "🎮 Practice",
            tabRanking = "🏆 Ranking",
            tabProfile = "👤 Profile",
            levelLabel = "🎯 Niveau actuel",
            xpLabel = "⭐ Points XP",
            streakLabel = "🔥 Série (Streak)",
            dailyGoalLabel = "🎯 Objectif du jour",
            startPlacementTest = "🎯 Test de Niveau Intelligent (A1-C2)",
            vocabTitle = "🧠 Vocabulary & Flashcards SRS",
            grammarTitle = "📖 Grammar & Temps Anglais",
            listeningTitle = "🎧 Listening, Podcasts & Dictées",
            speakingTitle = "🎤 Speaking & Prononciation UK/US",
            readingTitle = "📖 Reading, Articles & Dialogues",
            writingTitle = "✍️ Writing & Correction par IA",
            pathsTitle = "🏆 Parcours (IELTS, TOEFL, Business)",
            offlineModeTitle = "💾 Mode Hors Ligne & Sync",
            adminPanelTitle = "⚙️ Panneau Administrateur",
            subscriptionTitle = "🎁 Abonnements & Formules",
            signOutLabel = "Déconnexion"
        )
    }
}

data class PlacementQuestion(
    val id: String,
    val cefrLevel: String,
    val skillCategory: String, // Grammar, Vocabulary, Reading, Listening, Sentence Construction
    val prompt: String,
    val audioTranscript: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanationFr: String,
    val explanationAr: String
)

data class VocabItem(
    val id: String,
    val word: String,
    val phonetic: String,
    val cefrLevel: String,
    val category: String, // Daily, Idiom, Phrasal Verb, Business, Academic
    val translationFr: String,
    val translationAr: String,
    val exampleEn: String,
    val exampleFr: String,
    val synonyms: List<String>,
    val antonyms: List<String>
)

data class GrammarTopic(
    val id: String,
    val title: String,
    val cefrLevel: String,
    val formula: String,
    val summaryFr: String,
    val summaryAr: String,
    val summaryEn: String,
    val examples: List<String>,
    val exerciseQuestion: String,
    val exerciseOptions: List<String>,
    val correctOption: String,
    val explanationFr: String
)

data class ListeningDialogue(
    val id: String,
    val title: String,
    val cefrLevel: String,
    val category: String, // Dialogue, Podcast, Dictation
    val transcriptLines: List<DialogueLine>,
    val dictationTarget: String,
    val comprehensionQuestion: String,
    val comprehensionOptions: List<String>,
    val correctOption: String
)

data class DialogueLine(
    val speaker: String,
    val english: String,
    val french: String,
    val arabic: String
)

data class SpeakingDrill(
    val id: String,
    val title: String,
    val cefrLevel: String,
    val situation: String,
    val targetSentence: String,
    val tipUkUs: String,
    val translationFr: String,
    val translationAr: String
)

data class ReadingArticle(
    val id: String,
    val title: String,
    val cefrLevel: String,
    val genre: String, // Story, Article, Professional, Dialogue
    val paragraphs: List<String>,
    val glossary: Map<String, String>,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class WritingPrompt(
    val id: String,
    val title: String,
    val cefrLevel: String,
    val category: String,
    val instructionsFr: String,
    val instructionsEn: String,
    val starterTemplate: String,
    val targetKeywords: List<String>
)

data class MultiExerciseItem(
    val id: String,
    val format: String, // QCM, FILL_BLANK, WORD_ORDER, MATCHING, TRANSLATION, DICTATION
    val cefrLevel: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val audioPhrase: String? = null,
    val explanationFr: String,
    val revisionTip: String
)

data class LearningTrack(
    val id: String,
    val titleFr: String,
    val titleEn: String,
    val titleAr: String,
    val cefrRange: String,
    val category: String,
    val unitsCount: Int,
    val lessonsCount: Int,
    val badgeTitle: String,
    val units: List<String>
)

data class BadgeAchievement(
    val id: String,
    val titleFr: String,
    val titleEn: String,
    val descriptionFr: String,
    val requiredXp: Long,
    val iconName: String
)

object EnglishCurriculum {

    val cefrLevels = listOf(
        "A1" to "Beginner • Débutant",
        "A2" to "Elementary • Élémentaire",
        "B1" to "Intermediate • Intermédiaire",
        "B2" to "Upper Intermediate • Avancé",
        "C1" to "Advanced • Autonome",
        "C2" to "Proficiency • Maîtrise"
    )

    val placementQuestions = listOf(
        PlacementQuestion(
            id = "pq_1",
            cefrLevel = "A1",
            skillCategory = "Grammar",
            prompt = "She ___ to work by train every morning.",
            options = listOf("go", "goes", "going", "gone"),
            correctIndex = 1,
            explanationFr = "Au Present Simple, la 3e personne du singulier (she/he/it) prend un -s/-es : 'goes'.",
            explanationAr = "في المضارع البسيط مع الضمير المفرد الغائب نضيف es للفعل: goes."
        ),
        PlacementQuestion(
            id = "pq_2",
            cefrLevel = "A2",
            skillCategory = "Vocabulary",
            prompt = "Could you please ___ me a favor and send the file?",
            options = listOf("make", "do", "take", "give"),
            correctIndex = 1,
            explanationFr = "Collocation fixe en anglais : on dit 'do someone a favor' (et 'make a mistake').",
            explanationAr = "تعبير ثابت في الإنجليزية: نقول do a favor وليس make."
        ),
        PlacementQuestion(
            id = "pq_3",
            cefrLevel = "B1",
            skillCategory = "Listening",
            prompt = "Écoutez l'extrait audio puis choisissez l'information exacte :",
            audioTranscript = "Our flight to London has been delayed by forty-five minutes due to heavy fog at Heathrow Airport.",
            options = listOf(
                "The flight was cancelled because of snow.",
                "The flight is delayed by 45 minutes due to fog.",
                "The plane has already landed in Manchester.",
                "Boarding starts in ten minutes at Gate 4."
            ),
            correctIndex = 1,
            explanationFr = "'delayed by forty-five minutes due to heavy fog' indique un retard de 45 min à cause du brouillard.",
            explanationAr = "تم تأجيل الرحلة لمدة 45 دقيقة بسبب الضباب الكثيف."
        ),
        PlacementQuestion(
            id = "pq_4",
            cefrLevel = "B2",
            skillCategory = "Sentence Construction",
            prompt = "Choose the correct third conditional sentence:",
            options = listOf(
                "If I would have known, I had told you.",
                "If I had known about the meeting, I would have attended.",
                "If I knew about the meeting, I will attend.",
                "Had I know, I would attend."
            ),
            correctIndex = 1,
            explanationFr = "Le 3e conditionnel (irréel du passé) se construit avec : If + Past Perfect (had known), would have + participe passé.",
            explanationAr = "الشرط الثالث يتكون من If + had + التصريف الثالث، وفي الجواب would have + التصريف الثالث."
        ),
        PlacementQuestion(
            id = "pq_5",
            cefrLevel = "C1",
            skillCategory = "Reading",
            prompt = "Read: 'Albeit initially skeptical, the board ultimately endorsed the proposal.' What does 'Albeit' mean?",
            options = listOf("Because of", "Although / Even though", "Therefore", "Unless"),
            correctIndex = 1,
            explanationFr = "'Albeit' est une conjonction formelle de niveau C1 signifiant 'bien que / quoique' (although).",
            explanationAr = "كلمة Albeit تعني 'على الرغم من' (Although) وتستخدم في السياقات الرسمية والأكاديمية."
        ),
        PlacementQuestion(
            id = "pq_6",
            cefrLevel = "C2",
            skillCategory = "Grammar",
            prompt = "Seldom ___ such an eloquent keynote address at an international summit.",
            options = listOf(
                "I have heard",
                "have I heard",
                "did I heard",
                "I heard"
            ),
            correctIndex = 1,
            explanationFr = "Après un adverbe négatif ou restrictif en tête de phrase (Seldom, Rarely, Never), on applique l'inversion sujet-auxiliaire : 'have I heard'.",
            explanationAr = "عند بدء الجملة بظرف نفي مثل Seldom نطبق القلب النحوي بين الفعل المساعد والفاعل: have I heard."
        )
    )

    val vocabularyCatalog = listOf(
        VocabItem(
            id = "voc_1",
            word = "Resilient",
            phonetic = "/rɪˈzɪl.i.ənt/",
            cefrLevel = "B2",
            category = "Business",
            translationFr = "Résilient / Solide face aux épreuves",
            translationAr = "مرن / قادر على الصمود",
            exampleEn = "Our supply chain remained resilient despite global market fluctuations.",
            exampleFr = "Notre chaîne logistique est restée résiliente malgré les fluctuations du marché.",
            synonyms = listOf("Adaptable", "Robust", "Tenacious"),
            antonyms = listOf("Fragile", "Vulnerable")
        ),
        VocabItem(
            id = "voc_2",
            word = "Break the ice",
            phonetic = "/breɪk ðə aɪs/",
            cefrLevel = "B1",
            category = "Idiom",
            translationFr = "Briser la glace / Détendre l'atmosphère",
            translationAr = "كسر الجليد / تلطيف الأجواء",
            exampleEn = "The host told a humorous story to break the ice at the conference.",
            exampleFr = "L'hôte a raconté une anecdote amusante pour briser la glace.",
            synonyms = listOf("Initiate conversation", "Warm up"),
            antonyms = listOf("Create tension")
        ),
        VocabItem(
            id = "voc_3",
            word = "Look forward to",
            phonetic = "/lʊk ˈfɔː.wəd tuː/",
            cefrLevel = "A2",
            category = "Phrasal Verb",
            translationFr = "Avoir hâte de (suivi de V-ing)",
            translationAr = "يتطلع بشوق إلى",
            exampleEn = "I look forward to hearing from you soon.",
            exampleFr = "J'ai hâte d'avoir de vos nouvelles bientôt.",
            synonyms = listOf("Anticipate eagerly", "Await"),
            antonyms = listOf("Dread")
        ),
        VocabItem(
            id = "voc_4",
            word = "Ubiquitous",
            phonetic = "/juːˈbɪk.wɪ.təs/",
            cefrLevel = "C1",
            category = "Academic",
            translationFr = "Omniprésent",
            translationAr = "منتشر في كل مكان",
            exampleEn = "Artificial intelligence has become ubiquitous in modern healthcare.",
            exampleFr = "L'intelligence artificielle est devenue omniprésente dans la santé moderne.",
            synonyms = listOf("Omnipresent", "Pervasive", "Everywhere"),
            antonyms = listOf("Rare", "Scarce")
        ),
        VocabItem(
            id = "voc_5",
            word = "Come across",
            phonetic = "/kʌm əˈkrɒs/",
            cefrLevel = "B1",
            category = "Phrasal Verb",
            translationFr = "Tomber sur (par hasard) / Paraître",
            translationAr = "يصادف / يبدو",
            exampleEn = "I came across an fascinating article while researching for my thesis.",
            exampleFr = "Je suis tombé sur un article fascinant en faisant mes recherches.",
            synonyms = listOf("Stumble upon", "Encounter"),
            antonyms = listOf("Overlook", "Miss")
        ),
        VocabItem(
            id = "voc_6",
            word = "Mitigate",
            phonetic = "/ˈmɪt.ɪ.ɡeɪt/",
            cefrLevel = "C1",
            category = "Business",
            translationFr = "Atténuer / Réduire (un risque)",
            translationAr = "يخفف / يحد من",
            exampleEn = "We implemented strict security audits to mitigate cyber risks.",
            exampleFr = "Nous avons mis en place des audits stricts pour atténuer les cyber-risques.",
            synonyms = listOf("Alleviate", "Reduce", "Diminish"),
            antonyms = listOf("Aggravate", "Exacerbate")
        ),
        VocabItem(
            id = "voc_7",
            word = "Commute",
            phonetic = "/kəˈmjuːt/",
            cefrLevel = "A2",
            category = "Daily",
            translationFr = "Faire le trajet domicile-travail",
            translationAr = "يتنقل يومياً للعمل",
            exampleEn = "She commutes from suburbs to central London every day.",
            exampleFr = "Elle fait la navette depuis la banlieue jusqu'au centre de Londres.",
            synonyms = listOf("Travel daily", "Shuttle"),
            antonyms = listOf("Telecommute", "Stay home")
        ),
        VocabItem(
            id = "voc_8",
            word = "Ephemeral",
            phonetic = "/ɪˈfem.ər.əl/",
            cefrLevel = "C2",
            category = "Academic",
            translationFr = "Éphémère / Passager",
            translationAr = "زائل / عابر",
            exampleEn = "Viral social media trends are often ephemeral in nature.",
            exampleFr = "Les tendances virales sur les réseaux sociaux sont souvent éphémères.",
            synonyms = listOf("Fleeting", "Transient", "Short-lived"),
            antonyms = listOf("Permanent", "Enduring", "Eternal")
        )
    )

    val grammarCatalog = listOf(
        GrammarTopic(
            id = "gram_1",
            title = "Present Simple vs. Present Continuous",
            cefrLevel = "A1",
            formula = "S + V(s/es)  |  S + am/is/are + V-ing",
            summaryFr = "Utilisez le Present Simple pour les habitudes et vérités générales, et le Present Continuous pour une action en cours au moment où l'on parle.",
            summaryAr = "استخدم المضارع البسيط للعادات والحقائق، والمضارع المستمر للأحداث الجارية الآن.",
            summaryEn = "Use Present Simple for routines and facts; use Present Continuous for actions happening right now.",
            examples = listOf(
                "Water boils at 100 degrees Celsius. (Fact)",
                "Listen! She is speaking with our international client right now. (Action in progress)"
            ),
            exerciseQuestion = "Quiet please! The CEO ___ the quarterly results right now.",
            exerciseOptions = listOf("presents", "is presenting", "presented", "has presented"),
            correctOption = "is presenting",
            explanationFr = "'Right now' et 'Quiet please!' indiquent une action en cours d'exécution -> Present Continuous (is presenting)."
        ),
        GrammarTopic(
            id = "gram_2",
            title = "Past Simple vs. Past Continuous",
            cefrLevel = "A2",
            formula = "S + V-ed/Irreg  |  S + was/were + V-ing",
            summaryFr = "Le Past Continuous décrit une action longue en arrière-plan interrompue par une action brève au Past Simple.",
            summaryAr = "الماضي المستمر يصف حدثاً كان مستمراً عندما قطعه حدث آخر في الماضي البسيط.",
            summaryEn = "Past Continuous sets the background action that was interrupted by a shorter Past Simple event.",
            examples = listOf(
                "I was reviewing the contract when the power went out.",
                "While they were negotiating, the director arrived."
            ),
            exerciseQuestion = "While I ___ through Hyde Park, it suddenly started to rain.",
            exerciseOptions = listOf("walked", "was walking", "have walked", "am walking"),
            correctOption = "was walking",
            explanationFr = "Après 'While', l'action longue en cours dans le passé prend le Past Continuous : 'was walking'."
        ),
        GrammarTopic(
            id = "gram_3",
            title = "Present Perfect & Duration (Since / For)",
            cefrLevel = "B1",
            formula = "S + have/has + Past Participle",
            summaryFr = "Relie le passé au présent : expérience de vie, résultat présent ou action commencée dans le passé qui continue (avec for/since).",
            summaryAr = "يربط الماضي بالحاضر للتعبير عن التجارب أو الأحداث التي بدأت في الماضي وما زالت مستمرة.",
            summaryEn = "Connects past actions to present results or ongoing states using 'since' (point in time) and 'for' (duration).",
            examples = listOf(
                "We have worked on this AI project since January.",
                "She has already completed three B2 modules."
            ),
            exerciseQuestion = "Our team ___ in this London office for over five years.",
            exerciseOptions = listOf("works", "worked", "has worked", "is working"),
            correctOption = "has worked",
            explanationFr = "Avec 'for over five years' (durée jusqu'au présent), on utilise le Present Perfect : 'has worked'."
        ),
        GrammarTopic(
            id = "gram_4",
            title = "Future Forms (Will, Going to, Future Continuous)",
            cefrLevel = "B1",
            formula = "will + V  |  am/is/are going to + V  |  will be + V-ing",
            summaryFr = "'Will' pour les décisions spontanées et promesses, 'be going to' pour les projets planifiés, et 'will be V-ing' pour une action en cours à un moment futur.",
            summaryAr = "نستخدم will للقرارات اللحظية، و going to للخطط المسبقة، و Future Continuous لحدث سيستمر في وقت محدد مستقبلاً.",
            summaryEn = "Use 'will' for spontaneous decisions, 'going to' for prior plans, and 'will be V-ing' for actions in progress at a future time.",
            examples = listOf(
                "I will send you the summary right away. (Spontaneous)",
                "This time tomorrow, we will be flying to New York. (Future Continuous)"
            ),
            exerciseQuestion = "Look at those dark clouds! It ___ any minute now.",
            exerciseOptions = listOf("rains", "is going to rain", "rained", "would rain"),
            correctOption = "is going to rain",
            explanationFr = "Pour une prédiction basée sur un indice visible présent ('dark clouds'), on utilise 'be going to'."
        ),
        GrammarTopic(
            id = "gram_5",
            title = "Conditionals (Zero, First, Second & Third)",
            cefrLevel = "B2",
            formula = "1st: If + Pres, will + V | 2nd: If + Past, would + V | 3rd: If + had V-en, would have V-en",
            summaryFr = "Maîtrisez les 4 conditionnels : réel (0 & 1), hypothétique présent/futur (2nd), et regret/irréel du passé (3rd).",
            summaryAr = "الجمل الشرطية الأربع: الحقيقية (0 و 1)، والافتراضية في الحاضر (2)، والافتراضية في الماضي (3).",
            summaryEn = "Master real conditions (0/1st), unreal present hypotheticals (2nd), and past counterfactuals (3rd).",
            examples = listOf(
                "If I were you, I would negotiate a better salary. (2nd Conditional)",
                "If they had tested the software, the bug wouldn't have occurred. (3rd Conditional)"
            ),
            exerciseQuestion = "If I ___ more free time this semester, I would learn a third language.",
            exerciseOptions = listOf("have", "had", "will have", "would have"),
            correctOption = "had",
            explanationFr = "2e conditionnel (hypothèse irréelle dans le présent) : If + Past Simple ('had'), sujet + would + base verbale."
        ),
        GrammarTopic(
            id = "gram_6",
            title = "Passive Voice & Causative Structures",
            cefrLevel = "B2",
            formula = "S + be (conjugated) + Past Participle (+ by agent)",
            summaryFr = "Mettez l'accent sur l'action ou l'objet plutôt que sur l'auteur, indispensable dans les rapports scientifiques et professionnels.",
            summaryAr = "المبني للمجهول يركز على الحدث أو المفعول به بدلاً من الفاعل، وهو شائع في الكتابة الرسمية.",
            summaryEn = "Focus on the action or receiver rather than the doer—essential for academic and executive writing.",
            examples = listOf(
                "The new cybersecurity protocol was approved by the board yesterday.",
                "All applications are reviewed within 48 hours."
            ),
            exerciseQuestion = "The annual financial report ___ by the external auditors tomorrow morning.",
            exerciseOptions = listOf("will present", "will be presented", "is presenting", "presented"),
            correctOption = "will be presented",
            explanationFr = "Voix passive au futur : 'will be + participe passé' (will be presented)."
        ),
        GrammarTopic(
            id = "gram_7",
            title = "Reported Speech (Discours Indirect)",
            cefrLevel = "B2",
            formula = "Direct Present -> Reported Past | Will -> Would | Can -> Could",
            summaryFr = "Lorsqu'on rapporte les paroles de quelqu'un au passé (He said that...), on décale les temps d'un cran vers le passé (backshift).",
            summaryAr = "في الكلام المنقول بعد فعل ماضٍ مثل said، نرجع زمن الجملة درجة إلى الماضي.",
            summaryEn = "When reporting speech with a past reporting verb, shift tenses one step back in time.",
            examples = listOf(
                "Direct: 'I am ready.' -> Reported: She said that she was ready.",
                "Direct: 'We will launch in May.' -> Reported: They announced they would launch in May."
            ),
            exerciseQuestion = "Sarah told me that she ___ already finished the presentation.",
            exerciseOptions = listOf("has", "had", "have", "will"),
            correctOption = "had",
            explanationFr = "Concordance des temps après 'told me' (passé) : le Present Perfect devient Past Perfect ('had finished')."
        ),
        GrammarTopic(
            id = "gram_8",
            title = "Modal Verbs of Deduction & Nuance",
            cefrLevel = "C1",
            formula = "must have + V-en (certainty) | might/could have + V-en (possibility) | can't have + V-en (impossibility)",
            summaryFr = "Exprimez la probabilité, la certitude logique ou le reproche dans le passé avec les modaux parfaits.",
            summaryAr = "الأفعال الناقصة التامة للتعبير عن الاستنتاج المؤكد أو الاحتمال أو الاستحالة في الماضي.",
            summaryEn = "Express logical deduction, past possibility, or advisability using perfect modal structures.",
            examples = listOf(
                "He must have forgotten his phone; he never misses a call.",
                "You should have backed up the database before upgrading."
            ),
            exerciseQuestion = "She isn't answering her office phone. She ___ have left for the airport already.",
            exerciseOptions = listOf("must", "can", "should to", "ought"),
            correctOption = "must",
            explanationFr = "'Must have + participe passé' exprime une déduction quasi certaine dans le passé."
        ),
        GrammarTopic(
            id = "gram_9",
            title = "Articles (a/an/the/Ø) & Advanced Prepositions",
            cefrLevel = "C2",
            formula = "Zero Article (general plural/uncountable) vs. The (specific) + Dependent Prepositions",
            summaryFr = "Maîtrisez l'article zéro devant les concepts abstraits et les prépositions dépendantes (comply with, proficient in, contingent on).",
            summaryAr = "إتقان استخدام أدوات التعريف والتنكير وحروف الجر المرتبطة بالأفعال والصفات المتقدمة.",
            summaryEn = "Master zero article with abstract nouns and C2 dependent prepositions (contingent upon, adept at).",
            examples = listOf(
                "Innovation drives economic growth. (Zero article for general abstract concept)",
                "Final approval is contingent upon regulatory compliance."
            ),
            exerciseQuestion = "Our international expansion is strictly contingent ___ local regulatory approval.",
            exerciseOptions = listOf("with", "upon", "for", "at"),
            correctOption = "upon",
            explanationFr = "On dit toujours 'contingent on / contingent upon' (dépendant de / conditionné par)."
        )
    )

    val listeningCatalog = listOf(
        ListeningDialogue(
            id = "list_1",
            title = "At Heathrow Airport Check-In",
            cefrLevel = "A2",
            category = "Dialogue",
            transcriptLines = listOf(
                DialogueLine(
                    speaker = "Agent",
                    english = "Good morning! May I see your passport and booking reference, please?",
                    french = "Bonjour ! Puis-je voir votre passeport et votre référence de réservation, s'il vous plaît ?",
                    arabic = "صباح الخير! هل يمكنني رؤية جواز سفرك ورقم الحجز من فضلك؟"
                ),
                DialogueLine(
                    speaker = "Traveler",
                    english = "Here you go. Could I have a window seat near the front of the cabin?",
                    french = "Voici. Pourrais-je avoir un siège hublot vers l'avant de la cabine ?",
                    arabic = "تفضل. هل يمكنني الحصول على مقعد بجوار النافذة في مقدمة المقصورة؟"
                ),
                DialogueLine(
                    speaker = "Agent",
                    english = "Certainly. I have assigned you seat 12A. Boarding begins at Gate B14 in forty minutes.",
                    french = "Certainement. Je vous ai attribué le siège 12A. L'embarquement commence porte B14 dans 40 minutes.",
                    arabic = "بالتأكيد. لقد خصصت لك المقعد 12A. يبدأ الصعود عند البوابة B14 خلال أربعين دقيقة."
                )
            ),
            dictationTarget = "Boarding begins at Gate B14 in forty minutes",
            comprehensionQuestion = "Which seat and gate were assigned to the traveler?",
            comprehensionOptions = listOf(
                "Seat 12A at Gate B14",
                "Seat 14B at Gate A12",
                "Aisle seat 22C at Gate D4",
                "Window seat 4A at Gate C10"
            ),
            correctOption = "Seat 12A at Gate B14"
        ),
        ListeningDialogue(
            id = "list_2",
            title = "Tech Product Strategy Standup",
            cefrLevel = "B2",
            category = "Podcast",
            transcriptLines = listOf(
                DialogueLine(
                    speaker = "Product Lead",
                    english = "Our user retention jumped by eighteen percent after we launched the interactive voice tutor.",
                    french = "Notre rétention utilisateur a bondi de 18 % après le lancement du tuteur vocal interactif.",
                    arabic = "ارتفع معدل الاحتفاظ بالمستخدمين بنسبة 18% بعد إطلاق المعلم الصوتي التفاعلي."
                ),
                DialogueLine(
                    speaker = "Lead Engineer",
                    english = "That's encouraging. Our next priority is optimizing offline synchronization for commuters.",
                    french = "C'est encourageant. Notre prochaine priorité est d'optimiser la synchronisation hors ligne pour les voyageurs.",
                    arabic = "هذا مشجع. أولويتنا التالية هي تحسين المزامنة دون اتصال للمسافرين اليوميين."
                )
            ),
            dictationTarget = "Our next priority is optimizing offline synchronization",
            comprehensionQuestion = "By how much did user retention increase after launching the voice tutor?",
            comprehensionOptions = listOf("8%", "18%", "28%", "80%"),
            correctOption = "18%"
        ),
        ListeningDialogue(
            id = "list_3",
            title = "Keynote on Sustainable Urban Architecture",
            cefrLevel = "C1",
            category = "Podcast",
            transcriptLines = listOf(
                DialogueLine(
                    speaker = "Dr. Sterling",
                    english = "Integrating vertical forests into high-density metropolitan skylines significantly mitigates the urban heat island effect.",
                    french = "L'intégration de forêts verticales dans les métropoles denses atténue considérablement l'effet d'îlot de chaleur urbain.",
                    arabic = "إن دمج الغابات العمودية في المدن المزدحمة يخفف بشكل كبير من ظاهرة الاحتباس الحراري الحضري."
                ),
                DialogueLine(
                    speaker = "Moderator",
                    english = "Moreover, empirical studies demonstrate a thirty percent reduction in ambient particulate matter.",
                    french = "De plus, des études empiriques démontrent une réduction de 30 % des particules fines ambiantes.",
                    arabic = "علاوة على ذلك، تثبت الدراسات التجريبية انخفاضاً بنسبة ثلاثين بالمئة في الجسيمات العالقة."
                )
            ),
            dictationTarget = "Integrating vertical forests into high-density metropolitan skylines",
            comprehensionQuestion = "What is the primary benefit of vertical forests mentioned by Dr. Sterling?",
            comprehensionOptions = listOf(
                "Reducing ticket prices for public transit",
                "Mitigating the urban heat island effect",
                "Replacing all underground subways",
                "Increasing commercial rent prices"
            ),
            correctOption = "Mitigating the urban heat island effect"
        )
    )

    val speakingDrills = listOf(
        SpeakingDrill(
            id = "spk_1",
            title = "Daily Café & Ordering",
            cefrLevel = "A1",
            situation = "Ordering coffee and pastries politely in London",
            targetSentence = "Could I have a medium oat latte and a warm croissant, please?",
            tipUkUs = "UK: 'Could I have...' (/kʊd aɪ hæv/) vs US: 'Can I get...' — Stress 'oat latte' and use rising intonation on 'please?'.",
            translationFr = "Pourrais-je avoir un latte moyen au lait d'avoine et un croissant chaud, s'il vous plaît ?",
            translationAr = "هل يمكنني الحصول على لاتيه متوسط بحليب الشوفان وكرواسون دافئ من فضلك؟"
        ),
        SpeakingDrill(
            id = "spk_2",
            title = "Job Interview Introduction",
            cefrLevel = "B2",
            situation = "Presenting your professional background to an international recruiter",
            targetSentence = "Over the past five years, I have led cross-functional teams to deliver scalable digital products.",
            tipUkUs = "Pronounce 'led' clearly (/led/) and link 'cross-functional' smoothly without dropping the final -t in 'products'.",
            translationFr = "Au cours des cinq dernières années, j'ai dirigé des équipes pluridisciplinaires pour livrer des produits numériques évolutifs.",
            translationAr = "خلال السنوات الخمس الماضية، قدت فرقاً متعددة التخصصات لتقديم منتجات رقمية قابلة للتوسع."
        ),
        SpeakingDrill(
            id = "spk_3",
            title = "Executive Negotiation & Diplomacy",
            cefrLevel = "C1",
            situation = "Politely proposing an alternative terms sheet during a board meeting",
            targetSentence = "While we appreciate your initial proposal, we believe a phased rollout would better align with our strategic objectives.",
            tipUkUs = "Pause briefly after 'proposal' and emphasize 'phased rollout' and 'strategic objectives' for executive clarity.",
            translationFr = "Bien que nous apprécions votre proposition initiale, nous pensons qu'un déploiement progressif s'alignerait mieux sur nos objectifs stratégiques.",
            translationAr = "بينما نقدر مقترحكم الأولي، نعتقد أن الإطلاق المرحلي سيتوافق بشكل أفضل مع أهدافنا الاستراتيجية."
        )
    )

    val readingArticles = listOf(
        ReadingArticle(
            id = "read_1",
            title = "The Digital Nomad Revolution",
            cefrLevel = "B1",
            genre = "Article",
            paragraphs = listOf(
                "In recent years, millions of professionals have traded traditional cubicles for remote workspaces across Lisbon, Bali, and Montreal. Enabled by high-speed connectivity and cloud collaboration suites, remote work allows teams to operate seamlessly across time zones.",
                "However, seasoned remote workers emphasize that self-discipline and clear asynchronous communication are indispensable. Without structured boundaries, the line between personal leisure and professional duty can quickly blur."
            ),
            glossary = mapOf(
                "cubicles" to "box de bureau cloisonnés (مكاتب مقسمة)",
                "seamlessly" to "de manière fluide / sans accroc (بسلاسة)",
                "indispensable" to "indispensable / essentiel (لا غنى عنه)",
                "asynchronous" to "asynchrone / en différé (غير متزامن)",
                "blur" to "s'estomper / devenir flou (يتلاشى)"
            ),
            question = "According to the article, what two qualities are indispensable for successful remote workers?",
            options = listOf(
                "Self-discipline and clear asynchronous communication",
                "Living in a capital city and owning a car",
                "Working 14 hours every day without breaks",
                "Avoiding cloud collaboration tools"
            ),
            correctAnswer = "Self-discipline and clear asynchronous communication",
            explanation = "The second paragraph explicitly states that 'self-discipline and clear asynchronous communication are indispensable'."
        ),
        ReadingArticle(
            id = "read_2",
            title = "Executive Memo: Q4 Global Sustainability Initiative",
            cefrLevel = "C1",
            genre = "Professional",
            paragraphs = listOf(
                "Dear Stakeholders, As part of our commitment to achieving net-zero carbon emissions by 2030, we are overhauling our regional logistics network. Starting next quarter, eighty percent of short-haul deliveries will transition to electric fleets.",
                "While this capital expenditure requires upfront investment, our longitudinal projections indicate a twenty-two percent reduction in operational overhead within thirty-six months, alongside strengthened brand equity."
            ),
            glossary = mapOf(
                "overhauling" to "remanier en profondeur / moderniser (إعادة هيكلة شاملة)",
                "short-haul" to "courte distance (قصير المدى)",
                "capital expenditure" to "dépenses d'investissement CAPEX (نفقات رأسمالية)",
                "overhead" to "frais généraux d'exploitation (تكاليف التشغيل)",
                "brand equity" to "valeur / capital de marque (قيمة العلامة التجارية)"
            ),
            question = "What financial outcome do the longitudinal projections predict within 36 months?",
            options = listOf(
                "A 22% reduction in operational overhead",
                "An immediate 80% increase in shipping fees",
                "Closing all regional warehouses",
                "Postponing electric vehicles until 2045"
            ),
            correctAnswer = "A 22% reduction in operational overhead",
            explanation = "The memo highlights 'a twenty-two percent reduction in operational overhead within thirty-six months'."
        )
    )

    val writingPrompts = listOf(
        WritingPrompt(
            id = "wrt_1",
            title = "Professional Email: Requesting a Project Extension",
            cefrLevel = "B2",
            category = "Business Email",
            instructionsFr = "Rédigez un email professionnel à votre cliente (Ms. Jenkins) pour demander un délai supplémentaire de 3 jours sur le livrable technique, tout en la rassurant sur la qualité finale.",
            instructionsEn = "Write a polite, professional email to your client (Ms. Jenkins) requesting a 3-day extension on the technical deliverable while reassuring her of quality.",
            starterTemplate = "Dear Ms. Jenkins,\n\nI am writing to provide you with a brief update regarding the upcoming milestone...",
            targetKeywords = listOf("regarding", "milestone", "ensure", "quality", "appreciate")
        ),
        WritingPrompt(
            id = "wrt_2",
            title = "IELTS / TOEFL Opinion Essay: AI in Education",
            cefrLevel = "C1",
            category = "Academic Essay",
            instructionsFr = "Rédigez un paragraphe argumentatif (80-150 mots) expliquant comment l'intelligence artificielle personnalise l'apprentissage des langues sans remplacer l'empathie humaine.",
            instructionsEn = "Write an argumentative paragraph (80-150 words) explaining how artificial intelligence personalizes language learning while complementing human empathy.",
            starterTemplate = "While artificial intelligence has revolutionized personalized education by adapting to individual learning curves, ",
            targetKeywords = listOf("furthermore", "tailored", "autonomous", "nonetheless", "pedagogical")
        )
    )

    val multiFormatExercises = listOf(
        MultiExerciseItem(
            id = "ex_1",
            format = "QCM",
            cefrLevel = "A2",
            question = "Choose the correct option: 'We haven't seen each other ___ last summer.'",
            options = listOf("for", "since", "during", "from"),
            correctAnswer = "since",
            explanationFr = "'Since' s'utilise devant un point de départ précis dans le temps ('last summer'), tandis que 'for' précède une durée.",
            revisionTip = "Révisez le module Grammar : Present Perfect (Since vs For)."
        ),
        MultiExerciseItem(
            id = "ex_2",
            format = "FILL_BLANK",
            cefrLevel = "B1",
            question = "Complete the phrasal verb: 'We need to ___ up with a creative solution before Friday.'",
            options = listOf("come", "make", "go", "take"),
            correctAnswer = "come",
            explanationFr = "'Come up with' signifie trouver ou proposer une idée / une solution.",
            revisionTip = "Ajoutez 'come up with' à vos Flashcards Phrasal Verbs."
        ),
        MultiExerciseItem(
            id = "ex_3",
            format = "WORD_ORDER",
            cefrLevel = "B2",
            question = "Reorder the words: [never / have / I / witnessed / dedication / such]",
            options = listOf(
                "Never have I witnessed such dedication",
                "Never I have witnessed such dedication",
                "I never witnessed have such dedication",
                "Such dedication I never have witnessed"
            ),
            correctAnswer = "Never have I witnessed such dedication",
            explanationFr = "Inversion emphatique après 'Never' en tête de phrase : Never + auxiliaire (have) + sujet (I) + participe passé (witnessed).",
            revisionTip = "Pratiquez les inversions de niveau B2/C1."
        ),
        MultiExerciseItem(
            id = "ex_4",
            format = "TRANSLATION",
            cefrLevel = "B1",
            question = "Traduisez en anglais : 'J'ai hâte de travailler avec votre équipe.'",
            options = listOf(
                "I look forward to working with your team.",
                "I look forward to work with your team.",
                "I am waiting to work with your team.",
                "I have haste to work with your team."
            ),
            correctAnswer = "I look forward to working with your team.",
            explanationFr = "Dans 'look forward to', 'to' est une préposition : le verbe qui suit prend obligatoirement la forme -ING ('working').",
            revisionTip = "Attention au piège classique : 'to + V-ing' après look forward to."
        ),
        MultiExerciseItem(
            id = "ex_5",
            format = "DICTATION",
            cefrLevel = "B2",
            question = "Écoutez la phrase audio et sélectionnez la transcription exacte :",
            audioPhrase = "Effective communication requires active listening and cultural empathy.",
            options = listOf(
                "Effective communication requires active listening and cultural empathy.",
                "Affective communication acquires active listening and cultural sympathy.",
                "Effective conversation requires action listening and cultural empathy.",
                "Efficient communication requests active listening and natural empathy."
            ),
            correctAnswer = "Effective communication requires active listening and cultural empathy.",
            explanationFr = "Notez la différence entre 'effective' (efficace par rapport au but) et 'affective' (affectif), ainsi que 'requires' (exige).",
            revisionTip = "Réécoutez la phrase à vitesse 0.75x puis 1.25x dans le module Listening."
        )
    )

    val learningTracks = listOf(
        LearningTrack(
            id = "track_general",
            titleFr = "Anglais pour Débutants (Foundations)",
            titleEn = "English for Beginners",
            titleAr = "الإنجليزية للمبتدئين",
            cefrRange = "A1 - A2",
            category = "Foundations",
            unitsCount = 6,
            lessonsCount = 24,
            badgeTitle = "Foundation Graduate",
            units = listOf("Greetings & Introductions", "Daily Routines & Present Tenses", "Shopping & Numbers", "Past Experiences")
        ),
        LearningTrack(
            id = "track_conversational",
            titleFr = "Anglais Conversationnel & Fluidité",
            titleEn = "Conversational Fluency",
            titleAr = "المحادثة والطلاقة اليومية",
            cefrRange = "A2 - B2",
            category = "Speaking",
            unitsCount = 8,
            lessonsCount = 32,
            badgeTitle = "Fluent Speaker",
            units = listOf("Small Talk & Idioms", "Expressing Opinions", "Storytelling & Humor", "Debating Current Events")
        ),
        LearningTrack(
            id = "track_professional",
            titleFr = "Anglais Professionnel & Carrière",
            titleEn = "Professional & Career English",
            titleAr = "الإنجليزية المهنية والوظيفية",
            cefrRange = "B1 - C1",
            category = "Career",
            unitsCount = 8,
            lessonsCount = 30,
            badgeTitle = "Career Pro",
            units = listOf("Job Interviews & CV Pitch", "Cross-Cultural Meetings", "Project Management", "Remote Collaboration")
        ),
        LearningTrack(
            id = "track_business",
            titleFr = "Business English & Leadership",
            titleEn = "Executive Business English",
            titleAr = "إنجليزية الأعمال والقيادة",
            cefrRange = "B2 - C2",
            category = "Business",
            unitsCount = 10,
            lessonsCount = 40,
            badgeTitle = "Executive Communicator",
            units = listOf("High-Stakes Negotiations", "Financial Presentations", "Stakeholder Diplomacy", "Crisis Communication")
        ),
        LearningTrack(
            id = "track_travel",
            titleFr = "Anglais pour Voyager",
            titleEn = "English for Global Travel",
            titleAr = "الإنجليزية للسفر حول العالم",
            cefrRange = "A1 - B1",
            category = "Travel",
            unitsCount = 5,
            lessonsCount = 20,
            badgeTitle = "Global Explorer",
            units = listOf("Airports & Customs", "Hotels & Concierge", "Dining & Allergies", "Medical & Emergencies")
        ),
        LearningTrack(
            id = "track_academic",
            titleFr = "Anglais Académique & Recherche",
            titleEn = "Academic & University English",
            titleAr = "الإنجليزية الأكاديمية والجامعية",
            cefrRange = "B2 - C2",
            category = "Academic",
            unitsCount = 7,
            lessonsCount = 28,
            badgeTitle = "Academic Scholar",
            units = listOf("Research Abstracts", "Critical Literature Review", "Seminar Presentations", "Academic Citations & Hedging")
        ),
        LearningTrack(
            id = "track_ielts",
            titleFr = "Préparation Examen IELTS (Band 7.5+)",
            titleEn = "IELTS Academic & General Prep",
            titleAr = "التحضير لاختبار IELTS",
            cefrRange = "B1 - C2",
            category = "Exam Prep",
            unitsCount = 9,
            lessonsCount = 36,
            badgeTitle = "IELTS Master",
            units = listOf("IELTS Task 1 Data Analysis", "IELTS Task 2 Essay Structure", "Speaking Part 2 Cue Cards", "Academic Reading Skimming")
        ),
        LearningTrack(
            id = "track_toefl",
            titleFr = "Préparation Examen TOEFL iBT (100+)",
            titleEn = "TOEFL iBT Intensive Prep",
            titleAr = "التحضير لاختبار TOEFL iBT",
            cefrRange = "B2 - C2",
            category = "Exam Prep",
            unitsCount = 8,
            lessonsCount = 34,
            badgeTitle = "TOEFL Champion",
            units = listOf("Integrated Speaking Tasks", "Campus Lecture Listening", "Academic Discussion Writing", "Inference Reading")
        ),
        LearningTrack(
            id = "track_toeic",
            titleFr = "Préparation Examen TOEIC (900+ Gold)",
            titleEn = "TOEIC Listening & Reading Prep",
            titleAr = "التحضير لاختبار TOEIC",
            cefrRange = "B1 - C1",
            category = "Exam Prep",
            unitsCount = 7,
            lessonsCount = 28,
            badgeTitle = "TOEIC Gold 900+",
            units = listOf("Part 1-4 Business Listening", "Part 5 Incomplete Sentences", "Part 6 Text Completion", "Part 7 Double & Triple Passages")
        ),
        LearningTrack(
            id = "track_exams_cambridge",
            titleFr = "Anglais pour les Examens (Cambridge C1/C2)",
            titleEn = "Cambridge C1 Advanced & C2 Mastery",
            titleAr = "إتقان اختبارات كامبريدج C1/C2",
            cefrRange = "C1 - C2",
            category = "Exam Prep",
            unitsCount = 10,
            lessonsCount = 42,
            badgeTitle = "C2 Polyglot Laureate",
            units = listOf("Use of English Key Word Transformations", "Open Cloze & Word Formation", "C2 Discursive Essays", "Idiomatic Nuance")
        )
    )

    val badges = listOf(
        BadgeAchievement("b_first_step", "🏆 Premier Pas", "First Step", "Compléter votre première leçon ou test", 20L, "star"),
        BadgeAchievement("b_vocab_50", "🧠 Lexicographe", "Word Collector", "Atteindre 150 XP et maîtriser des flashcards", 150L, "book"),
        BadgeAchievement("b_grammar_pro", "📖 Architecte Grammatical", "Grammar Architect", "Atteindre 300 XP en grammaire et quiz", 300L, "school"),
        BadgeAchievement("b_ai_conversationalist", "🤖 Orateur IA", "AI Conversationalist", "Dialoguer avec le Professeur IA et atteindre 500 XP", 500L, "record_voice_over"),
        BadgeAchievement("b_c2_master", "👑 Maître C2", "English Master", "Atteindre 1000 XP d'excellence linguistique", 1000L, "emoji_events")
    )

    val gamifiedUnits = listOf(
        GamifiedUnitNode(
            unitNumber = 1,
            id = "unit_1_basics",
            emoji = "🟢",
            titleEn = "Unit 1 – 🟢 Basics",
            titleFr = "Unité 1 – 🟢 Bases Essentielles",
            subtitleFr = "Pronoms, verbe To Be & premières phrases",
            cefrLevel = "A1",
            accentColorHex = 0xFF58CC02,
            xpReward = 40,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: I am, You are",
                    audioPhrase = "Hello! I am excited to learn English today.",
                    question = "Complétez : 'She ___ a brilliant student.'",
                    options = listOf("is", "are", "am", "be"),
                    correctAnswer = "is",
                    explanation = "Avec He / She / It au présent simple, le verbe 'to be' se conjugue 'is'."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Everyday Objects",
                    audioPhrase = "This is my notebook and that is your pen.",
                    question = "Que signifie 'This is my notebook' ?",
                    options = listOf("C'est mon cahier", "C'est ton livre", "Où est mon stylo ?", "Voici la bibliothèque"),
                    correctAnswer = "C'est mon cahier",
                    explanation = "'Notebook' signifie cahier ou carnet de notes (et non 'book' = livre)."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Simple Questions",
                    audioPhrase = "Where are you from? I am from Paris.",
                    question = "Quelle est la bonne réponse à 'Where are you from?'",
                    options = listOf("I am from Madrid.", "I have 25 years.", "Yes, I do.", "At 8 o'clock."),
                    correctAnswer = "I am from Madrid.",
                    explanation = "'Where are you from?' interroge sur l'origine géographique."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 2,
            id = "unit_2_greetings",
            emoji = "👋",
            titleEn = "Unit 2 – 👋 Greetings",
            titleFr = "Unité 2 – 👋 Salutations & Rencontres",
            subtitleFr = "Se présenter, politesse & formules d'accueil",
            cefrLevel = "A1",
            accentColorHex = 0xFF1CB0F6,
            xpReward = 45,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: Polite Introductions",
                    audioPhrase = "It is a pleasure to meet you! How is your day going?",
                    question = "Quelle expression est la plus naturelle pour répondre à 'Nice to meet you' ?",
                    options = listOf("Nice to meet you too!", "Thank you very much.", "See you yesterday.", "You are welcome."),
                    correctAnswer = "Nice to meet you too!",
                    explanation = "On répond par 'Nice to meet you too!' ou 'Lovely to meet you!'."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Asking How Someone Is",
                    audioPhrase = "How have you been lately? I have been doing great!",
                    question = "Que signifie 'How have you been lately?'",
                    options = listOf("Comment vas-tu ces derniers temps ?", "Où étais-tu hier ?", "Quel âge as-tu ?", "Quand pars-tu ?"),
                    correctAnswer = "Comment vas-tu ces derniers temps ?",
                    explanation = "Le Present Perfect 'How have you been' exprime une période récente jusqu'à maintenant."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Saying Goodbye",
                    audioPhrase = "Have a wonderful weekend! Catch you later!",
                    question = "Que signifie l'expression familière 'Catch you later!' ?",
                    options = listOf("À plus tard !", "Attrape le ballon !", "Je suis en retard !", "À la semaine dernière !"),
                    correctAnswer = "À plus tard !",
                    explanation = "'Catch you later' est une façon chaleureuse et courante de dire 'À plus tard'."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 3,
            id = "unit_3_food",
            emoji = "🍎",
            titleEn = "Unit 3 – 🍎 Food",
            titleFr = "Unité 3 – 🍎 Cuisine & Restaurant",
            subtitleFr = "Commander au restaurant, ingrédients & goûts",
            cefrLevel = "A2",
            accentColorHex = 0xFFFF9600,
            xpReward = 50,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: Ordering at a Café",
                    audioPhrase = "Could I have a sparkling water and a grilled salmon salad, please?",
                    question = "Quelle formule est la plus polie pour commander au restaurant ?",
                    options = listOf("Could I have the pasta, please?", "Give me the pasta now.", "I want pasta.", "Pasta for me."),
                    correctAnswer = "Could I have the pasta, please?",
                    explanation = "'Could I have ... please?' ou 'I would like...' sont les formules polies de référence."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Dietary Preferences",
                    audioPhrase = "Does this dish contain any nuts or dairy products?",
                    question = "Que signifie 'dairy products' ?",
                    options = listOf("Les produits laitiers", "Les produits quotidiens", "Les fruits de mer", "Les épices fortes"),
                    correctAnswer = "Les produits laitiers",
                    explanation = "'Dairy' désigne les produits laitiers (attention à ne pas confondre avec 'daily' = quotidien)."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Asking for the Bill",
                    audioPhrase = "Excuse me, could we get the check when you have a moment?",
                    question = "Quel mot américain désigne l'addition au restaurant ?",
                    options = listOf("The check (ou the bill au Royaume-Uni)", "The recipe", "The ticket", "The menu"),
                    correctAnswer = "The check (ou the bill au Royaume-Uni)",
                    explanation = "Aux USA on demande 'the check', au Royaume-Uni 'the bill' ('receipt' = le reçu après paiement)."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 4,
            id = "unit_4_home",
            emoji = "🏠",
            titleEn = "Unit 4 – 🏠 Home",
            titleFr = "Unité 4 – 🏠 Maison & Quotidien",
            subtitleFr = "Logement, routines, famille & objets de la maison",
            cefrLevel = "A2",
            accentColorHex = 0xFFCE82FF,
            xpReward = 55,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: Describing Your Home",
                    audioPhrase = "My apartment is cozy and has a bright living room overlooking the park.",
                    question = "Que signifie l'adjectif 'cozy' ?",
                    options = listOf("Chaleureux et confortable", "Très bruyant", "Immense et froid", "En travaux"),
                    correctAnswer = "Chaleureux et confortable",
                    explanation = "'Cozy' (US) / 'Cosy' (UK) décrit un endroit douillet et agréable."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Household Chores",
                    audioPhrase = "I usually do the laundry on Saturday morning and vacuum the carpet.",
                    question = "Quelle expression correcte signifie 'faire la lessive' ?",
                    options = listOf("do the laundry", "make the laundry", "wash the house", "clean the dress"),
                    correctAnswer = "do the laundry",
                    explanation = "On utilise 'do' pour les tâches ménagères (do the laundry, do the dishes) mais 'make the bed'."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Neighborhood & Prepositions",
                    audioPhrase = "The bakery is right across from the subway station.",
                    question = "Que signifie 'across from' ?",
                    options = listOf("En face de", "Loin de", "Au-dessus de", "À l'intérieur de"),
                    correctAnswer = "En face de",
                    explanation = "'Across from' (ou 'opposite') signifie 'en face de'."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 5,
            id = "unit_5_travel",
            emoji = "✈️",
            titleEn = "Unit 5 – ✈️ Travel",
            titleFr = "Unité 5 – ✈️ Voyages & Aéroport",
            subtitleFr = "Vols, hôtel, douane, orientation & urgences",
            cefrLevel = "B1",
            accentColorHex = 0xFF1CB0F6,
            xpReward = 60,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: At the Boarding Gate",
                    audioPhrase = "Our flight to London has been delayed by thirty minutes due to fog.",
                    question = "Que signifie 'Our flight has been delayed' ?",
                    options = listOf("Notre vol a été retardé", "Notre vol a été annulé", "Notre avion a décollé", "Notre porte a changé"),
                    correctAnswer = "Notre vol a été retardé",
                    explanation = "'Delayed' = retardé, tandis que 'cancelled' = annulé et 'boarding' = embarquement en cours."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Hotel Check-In",
                    audioPhrase = "I have a reservation under the name Martin for three nights.",
                    question = "Quelle préposition utilise-t-on pour dire 'une réservation au nom de...' ?",
                    options = listOf("under the name...", "with the surname...", "by the title...", "for the call..."),
                    correctAnswer = "under the name...",
                    explanation = "L'expression idiomatique standard à l'hôtel est 'I have a reservation under the name [Nom]'."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Asking for Directions",
                    audioPhrase = "Go straight ahead for two blocks, then turn left at the traffic lights.",
                    question = "Que signifie 'Go straight ahead for two blocks' ?",
                    options = listOf("Allez tout droit sur deux pâtés de maisons", "Tournez à droite deux fois", "Revenez en arrière", "Prenez la deuxième sortie"),
                    correctAnswer = "Allez tout droit sur deux pâtés de maisons",
                    explanation = "'Straight ahead' = tout droit ; 'a block' = un pâté de maisons."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 6,
            id = "unit_6_work",
            emoji = "💼",
            titleEn = "Unit 6 – 💼 Work",
            titleFr = "Unité 6 – 💼 Travail & Carrière",
            subtitleFr = "Réunions, emails professionnels, entretiens & projets",
            cefrLevel = "B2",
            accentColorHex = 0xFFFFC800,
            xpReward = 70,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: Professional Emails",
                    audioPhrase = "Please find attached the quarterly report for your review.",
                    question = "Quelle formule professionnelle signifie 'Veuillez trouver ci-joint...' ?",
                    options = listOf("Please find attached...", "Look at the file here...", "I send you with this...", "Here is the paper..."),
                    correctAnswer = "Please find attached...",
                    explanation = "'Please find attached' est la formule standard des emails professionnels."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Leading a Meeting",
                    audioPhrase = "Let's circle back to the budget proposal before we wrap up.",
                    question = "Que signifie le phrasal verb professionnel 'wrap up' ?",
                    options = listOf("Conclure / terminer une réunion", "Emballer un cadeau", "Annuler un projet", "Commencer une présentation"),
                    correctAnswer = "Conclure / terminer une réunion",
                    explanation = "'To wrap up a meeting' signifie conclure ou récapituler avant de terminer."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Job Interview Strengths",
                    audioPhrase = "I thrive in fast-paced environments and enjoy solving complex problems.",
                    question = "Que signifie 'I thrive in fast-paced environments' ?",
                    options = listOf("Je m'épanouis dans des environnements dynamiques", "J'évite le stress au travail", "Je préfère travailler lentement", "Je cherche un poste à temps partiel"),
                    correctAnswer = "Je m'épanouis dans des environnements dynamiques",
                    explanation = "'To thrive' = s'épanouir / exceller ; 'fast-paced' = au rythme soutenu et dynamique."
                )
            )
        ),
        GamifiedUnitNode(
            unitNumber = 7,
            id = "unit_7_conversation",
            emoji = "💬",
            titleEn = "Unit 7 – 💬 Conversation",
            titleFr = "Unité 7 – 💬 Conversation & Fluidité",
            subtitleFr = "Expressions idiomatiques, débats, nuances & C1/C2",
            cefrLevel = "C1",
            accentColorHex = 0xFFFF4B4B,
            xpReward = 80,
            lessons = listOf(
                GamifiedSubLesson(
                    title = "Lesson 1: Idioms in Conversation",
                    audioPhrase = "Sharing a funny story is a great way to break the ice at a conference.",
                    question = "Que signifie l'expression 'to hit the nail on the head' ?",
                    options = listOf("Viser juste / Décrire exactement le problème", "Faire du bricolage", "Commettre une erreur grave", "Parler trop fort"),
                    correctAnswer = "Viser juste / Décrire exactement le problème",
                    explanation = "'You hit the nail on the head' signifie 'Tu as mis le doigt dessus / Tu as visé juste'."
                ),
                GamifiedSubLesson(
                    title = "Lesson 2: Polite Disagreement",
                    audioPhrase = "I see your point, however we should also consider the long-term impact.",
                    question = "Quelle tournure permet d'exprimer un désaccord diplomatique en anglais ?",
                    options = listOf("I see your point, however...", "You are completely wrong.", "No, that is impossible.", "I refuse to listen."),
                    correctAnswer = "I see your point, however...",
                    explanation = "Reconnaître l'argument ('I see your point, however...') est essentiel pour nuancer avec élégance."
                ),
                GamifiedSubLesson(
                    title = "Lesson 3: Advanced C1/C2 Inversion",
                    audioPhrase = "Seldom have I witnessed such remarkable dedication from a team.",
                    question = "Pourquoi dit-on 'Seldom have I witnessed...' au lieu de 'Seldom I have witnessed' ?",
                    options = listOf("Inversion stylistique après un adverbe négatif ou restrictif en tête de phrase", "C'est une question directe", "C'est une erreur grammaticale", "C'est du passif"),
                    correctAnswer = "Inversion stylistique après un adverbe négatif ou restrictif en tête de phrase",
                    explanation = "Au niveau C1/C2, placer 'Seldom', 'Never', ou 'Not only' en début de phrase entraîne l'inversion sujet-auxiliaire."
                )
            )
        )
    )
}

data class GamifiedSubLesson(
    val title: String,
    val audioPhrase: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class GamifiedUnitNode(
    val unitNumber: Int,
    val id: String,
    val emoji: String,
    val titleEn: String,
    val titleFr: String,
    val subtitleFr: String,
    val cefrLevel: String,
    val accentColorHex: Long,
    val xpReward: Int,
    val lessons: List<GamifiedSubLesson>
)


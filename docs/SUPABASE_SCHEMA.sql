-- =====================================================================
-- ENGLISH MASTER AI - COMPLETE PRODUCTION SQL & RLS SCHEMA (SUPABASE)
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. admin_roles
CREATE TABLE IF NOT EXISTS public.admin_roles (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    role_name TEXT NOT NULL DEFAULT 'admin' CHECK (role_name IN ('admin', 'editor', 'moderator')),
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. learning_levels
CREATE TABLE IF NOT EXISTS public.learning_levels (
    code TEXT PRIMARY KEY CHECK (code IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    title TEXT NOT NULL,
    min_xp BIGINT NOT NULL DEFAULT 0,
    description TEXT NOT NULL
);

-- 3. profiles
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    email TEXT,
    avatar_url TEXT,
    cefr_level TEXT NOT NULL DEFAULT 'A1' REFERENCES public.learning_levels(code),
    xp BIGINT NOT NULL DEFAULT 0 CHECK (xp >= 0),
    placement_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. user_settings
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    ui_language TEXT NOT NULL DEFAULT 'fr' CHECK (ui_language IN ('fr', 'en', 'ar')),
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_accent TEXT NOT NULL DEFAULT 'UK' CHECK (preferred_accent IN ('UK', 'US')),
    daily_goal_minutes INT NOT NULL DEFAULT 15 CHECK (daily_goal_minutes BETWEEN 5 AND 240),
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. courses
CREATE TABLE IF NOT EXISTS public.courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slug TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    subtitle TEXT,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    category TEXT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. modules
CREATE TABLE IF NOT EXISTS public.modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. lessons
CREATE TABLE IF NOT EXISTS public.lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID NOT NULL REFERENCES public.modules(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    skill_type TEXT NOT NULL CHECK (skill_type IN ('vocabulary', 'grammar', 'listening', 'speaking', 'reading', 'writing')),
    duration_minutes INT NOT NULL DEFAULT 10,
    order_index INT NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. lesson_content
CREATE TABLE IF NOT EXISTS public.lesson_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lesson_id UUID NOT NULL REFERENCES public.lessons(id) ON DELETE CASCADE,
    content_markdown TEXT NOT NULL,
    audio_url TEXT,
    video_url TEXT,
    transcript_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. vocabulary
CREATE TABLE IF NOT EXISTS public.vocabulary (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    word TEXT NOT NULL,
    phonetic TEXT,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    category TEXT NOT NULL DEFAULT 'Daily',
    translation_fr TEXT NOT NULL,
    translation_ar TEXT NOT NULL,
    example_en TEXT NOT NULL,
    synonyms TEXT[] DEFAULT '{}',
    antonyms TEXT[] DEFAULT '{}',
    audio_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. flashcards
CREATE TABLE IF NOT EXISTS public.flashcards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vocabulary_id UUID NOT NULL REFERENCES public.vocabulary(id) ON DELETE CASCADE,
    deck_name TEXT NOT NULL,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code)
);

-- 11. user_flashcards
CREATE TABLE IF NOT EXISTS public.user_flashcards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    flashcard_id UUID NOT NULL REFERENCES public.flashcards(id) ON DELETE CASCADE,
    box_level INT NOT NULL DEFAULT 1 CHECK (box_level BETWEEN 1 AND 5),
    ease_factor NUMERIC(4,2) NOT NULL DEFAULT 2.50,
    next_review_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    mastered BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, flashcard_id)
);

-- 12. grammar_topics
CREATE TABLE IF NOT EXISTS public.grammar_topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    formula TEXT NOT NULL,
    explanation_fr TEXT NOT NULL,
    explanation_ar TEXT NOT NULL,
    explanation_en TEXT NOT NULL,
    examples JSONB NOT NULL DEFAULT '[]'::jsonb
);

-- 13. exercises
CREATE TABLE IF NOT EXISTS public.exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lesson_id UUID REFERENCES public.lessons(id) ON DELETE SET NULL,
    grammar_topic_id UUID REFERENCES public.grammar_topics(id) ON DELETE SET NULL,
    exercise_type TEXT NOT NULL CHECK (exercise_type IN ('qcm', 'fill_blank', 'word_order', 'matching', 'translation', 'dictation', 'voice', 'timed')),
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    title TEXT NOT NULL,
    time_limit_seconds INT DEFAULT 60
);

-- 14. questions
CREATE TABLE IF NOT EXISTS public.questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exercise_id UUID NOT NULL REFERENCES public.exercises(id) ON DELETE CASCADE,
    prompt_text TEXT NOT NULL,
    audio_url TEXT,
    explanation_fr TEXT NOT NULL,
    explanation_ar TEXT,
    order_index INT NOT NULL DEFAULT 0
);

-- 15. answers
CREATE TABLE IF NOT EXISTS public.answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE
);

-- 16. user_answers
CREATE TABLE IF NOT EXISTS public.user_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    submitted_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 17. learning_progress
CREATE TABLE IF NOT EXISTS public.learning_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    lesson_id UUID REFERENCES public.lessons(id) ON DELETE CASCADE,
    module_type TEXT NOT NULL,
    score INT NOT NULL CHECK (score BETWEEN 0 AND 100),
    xp_earned INT NOT NULL DEFAULT 10,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 18. learning_sessions
CREATE TABLE IF NOT EXISTS public.learning_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    duration_seconds INT NOT NULL DEFAULT 0,
    device_platform TEXT NOT NULL DEFAULT 'android'
);

-- 19. daily_goals
CREATE TABLE IF NOT EXISTS public.daily_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    goal_date DATE NOT NULL DEFAULT CURRENT_DATE,
    target_minutes INT NOT NULL DEFAULT 15,
    completed_minutes INT NOT NULL DEFAULT 0,
    achieved BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(user_id, goal_date)
);

-- 20. streaks
CREATE TABLE IF NOT EXISTS public.streaks (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_active_date DATE
);

-- 21. achievements
CREATE TABLE IF NOT EXISTS public.achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    title_fr TEXT NOT NULL,
    title_en TEXT NOT NULL,
    required_xp BIGINT NOT NULL DEFAULT 100,
    icon_name TEXT NOT NULL
);

-- 22. user_achievements
CREATE TABLE IF NOT EXISTS public.user_achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES public.achievements(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, achievement_id)
);

-- 23. leaderboards
CREATE TABLE IF NOT EXISTS public.leaderboards (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    cefr_level TEXT NOT NULL,
    weekly_xp BIGINT NOT NULL DEFAULT 0,
    total_xp BIGINT NOT NULL DEFAULT 0,
    streak_days INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 24. ai_conversations
CREATE TABLE IF NOT EXISTS public.ai_conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    scenario TEXT NOT NULL DEFAULT 'free',
    cefr_level TEXT NOT NULL DEFAULT 'B1',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 25. ai_messages
CREATE TABLE IF NOT EXISTS public.ai_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES public.ai_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    grammar_correction TEXT,
    explanation TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 26. subscriptions
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    plan_tier TEXT NOT NULL CHECK (plan_tier IN ('Free', 'Premium', 'AI Pro', 'Family')),
    status TEXT NOT NULL CHECK (status IN ('active', 'trialing', 'past_due', 'canceled')),
    provider_subscription_id TEXT,
    current_period_end TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 27. payments
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES public.subscriptions(id) ON DELETE SET NULL,
    amount_cents INT NOT NULL,
    currency TEXT NOT NULL DEFAULT 'EUR',
    provider_payment_intent_id TEXT UNIQUE NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('pending', 'succeeded', 'failed', 'refunded')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 28. notifications
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    notification_type TEXT NOT NULL DEFAULT 'reminder',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 29. certificates
CREATE TABLE IF NOT EXISTS public.certificates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    course_id UUID REFERENCES public.courses(id) ON DELETE SET NULL,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    verification_code TEXT UNIQUE NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ROW LEVEL SECURITY (RLS) POLICIES
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_flashcards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_answers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.learning_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.learning_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.daily_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.streaks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_achievements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.leaderboards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ai_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.certificates ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Owner profile access" ON public.profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY "Owner settings access" ON public.user_settings FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Owner flashcards access" ON public.user_flashcards FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Owner progress access" ON public.learning_progress FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Owner AI messages access" ON public.ai_messages FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "Public leaderboard read" ON public.leaderboards FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Owner leaderboard write" ON public.leaderboards FOR ALL USING (auth.uid() = user_id);

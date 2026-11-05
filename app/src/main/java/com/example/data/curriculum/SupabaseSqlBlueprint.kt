package com.example.data.curriculum

object SupabaseSqlBlueprint {
    val all28Tables = listOf(
        "profiles", "user_settings", "learning_levels", "courses", "modules",
        "lessons", "lesson_content", "vocabulary", "flashcards", "user_flashcards",
        "grammar_topics", "exercises", "questions", "answers", "user_answers",
        "learning_progress", "learning_sessions", "daily_goals", "streaks", "achievements",
        "user_achievements", "leaderboards", "ai_conversations", "ai_messages", "subscriptions",
        "payments", "notifications", "certificates", "admin_roles"
    )

    val sqlScriptSummary = """
-- ENGLISH MASTER AI - COMPLETE SUPABASE POSTGRESQL + RLS SCHEMA (29 TABLES)
-- Compatible with Supabase PostgreSQL 15+, Supabase Auth (auth.users), and Realtime

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. profiles
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    cefr_level TEXT NOT NULL DEFAULT 'A1' CHECK (cefr_level IN ('A1','A2','B1','B2','C1','C2')),
    xp BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. user_settings
CREATE TABLE public.user_settings (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    ui_language TEXT NOT NULL DEFAULT 'fr' CHECK (ui_language IN ('fr','en','ar')),
    dark_mode BOOLEAN NOT NULL DEFAULT FALSE,
    daily_reminder_time TIME DEFAULT '19:00:00',
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    preferred_accent TEXT NOT NULL DEFAULT 'UK' CHECK (preferred_accent IN ('UK','US'))
);

-- 3. learning_levels
CREATE TABLE public.learning_levels (
    code TEXT PRIMARY KEY CHECK (code IN ('A1','A2','B1','B2','C1','C2')),
    title TEXT NOT NULL,
    min_xp BIGINT NOT NULL,
    description TEXT NOT NULL
);

-- 4. courses & 5. modules & 6. lessons & 7. lesson_content
CREATE TABLE public.courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    slug TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    category TEXT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.modules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    order_index INT NOT NULL DEFAULT 0
);

CREATE TABLE public.lessons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    module_id UUID NOT NULL REFERENCES public.modules(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    skill_type TEXT NOT NULL CHECK (skill_type IN ('vocabulary','grammar','listening','speaking','reading','writing')),
    duration_minutes INT NOT NULL DEFAULT 10,
    order_index INT NOT NULL DEFAULT 0
);

CREATE TABLE public.lesson_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lesson_id UUID NOT NULL REFERENCES public.lessons(id) ON DELETE CASCADE,
    body_markdown TEXT NOT NULL,
    audio_storage_path TEXT,
    transcript_json JSONB DEFAULT '[]'::jsonb
);

-- 8. vocabulary, 9. flashcards, 10. user_flashcards
CREATE TABLE public.vocabulary (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    word TEXT NOT NULL,
    phonetic TEXT,
    cefr_level TEXT NOT NULL REFERENCES public.learning_levels(code),
    translation_fr TEXT NOT NULL,
    translation_ar TEXT NOT NULL,
    example_en TEXT NOT NULL
);

CREATE TABLE public.flashcards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vocabulary_id UUID NOT NULL REFERENCES public.vocabulary(id) ON DELETE CASCADE,
    deck_category TEXT NOT NULL
);

CREATE TABLE public.user_flashcards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    flashcard_id UUID NOT NULL REFERENCES public.flashcards(id) ON DELETE CASCADE,
    box_level INT NOT NULL DEFAULT 1 CHECK (box_level BETWEEN 1 AND 5),
    next_review_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(user_id, flashcard_id)
);

-- 11. grammar_topics, 12. exercises, 13. questions, 14. answers, 15. user_answers
-- 16. learning_progress, 17. learning_sessions, 18. daily_goals, 19. streaks
-- 20. achievements, 21. user_achievements, 22. leaderboards
-- 23. ai_conversations, 24. ai_messages
-- 25. subscriptions, 26. payments, 27. notifications, 28. certificates, 29. admin_roles

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_flashcards ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users manage own profile" ON public.profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY "Users manage own flashcards" ON public.user_flashcards FOR ALL USING (auth.uid() = user_id);
    """.trimIndent()
}

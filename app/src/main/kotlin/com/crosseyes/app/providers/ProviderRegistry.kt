package com.crosseyes.app.providers

enum class ProviderCategory { VIDEO, IMAGE, TEXT }

data class AiProvider(
    val id: String,
    val displayName: String,
    val tagline: String,
    val category: ProviderCategory,
    val apiBase: String,
    val model: String,       // provider-side model identifier
    val keyField: String,    // which vault key to use
    val keyPrefix: String,   // hint for users (e.g. "sk-...")
    val docsUrl: String
)

val ALL_PROVIDERS = listOf(
    AiProvider("flux3", "Flux 3", "fal.ai flux-3 image model", ProviderCategory.IMAGE, "https://api.fal.ai/flux-3/image", "fal-ai/flux-3", "flux", "fal_sk_", "https://fal.ai/docs"),
    AiProvider("sunburst", "ChatGPT Image 2.5 Sunburst", "OpenAI image generation", ProviderCategory.IMAGE, "https://api.openai.com/v1/images/generations", "gpt-image-1", "openai", "sk-", "https://platform.openai.com/docs/guides/images"),
    AiProvider("google_flow", "Google Flow", "Google Flow video model", ProviderCategory.VIDEO, "https://generativelanguage.googleapis.com/v1beta", "gemini-2.5-flash", "google", "AIza", "https://ai.google.dev/flow"),
    AiProvider("google_omni", "Google Omni", "Google Omni video model", ProviderCategory.VIDEO, "https://generativelanguage.googleapis.com/v1beta/omni", "omni-1.0", "google", "AIza", "https://ai.google.dev/omni"),
    AiProvider("seendance", "Seendance 2.5", "Seendance video v2.5", ProviderCategory.VIDEO, "https://api.seendance.io/v2.5/video", "seendance-2.5", "seendance", "sdnt_", "https://seendance.io/docs"),
    AiProvider("minimax", "MiniMax", "MiniMax video model", ProviderCategory.VIDEO, "https://api.minimaxi.chat/v1/video", "mini-video", "minimax", "eyJ", "https://platform.minimaxi.chat/docs/video"),
    AiProvider("kling", "Kling Effects", "Kling effect video model", ProviderCategory.VIDEO, "https://api.klingai.com/v1/video/effect", "kling-v1", "kling", "klng", "https://klingai.com/docs"),
    AiProvider("openai_text", "GPT (text)", "GPT chat completions", ProviderCategory.TEXT, "https://api.openai.com/v1/chat/completions", "gpt-4o", "openai", "sk-", "https://platform.openai.com/docs/guides/chat")
)

val ALL_VIDEO_PROVIDERS: List<AiProvider> = ALL_PROVIDERS.filter { it.category == ProviderCategory.VIDEO }
val ALL_IMAGE_PROVIDERS: List<AiProvider> = ALL_PROVIDERS.filter { it.category == ProviderCategory.IMAGE }
val ALL_TEXT_PROVIDERS: List<AiProvider> = ALL_PROVIDERS.filter { it.category == ProviderCategory.TEXT }

fun providerById(id: String): AiProvider? = ALL_PROVIDERS.firstOrNull { it.id == id }

package apoy2k.greenbookbot

data class Fav(
    val id: String,
    val userId: String,
    val guildId: String,
    val channelId: String,
    val messageId: String,
    val authorId: String,
    val tags: MutableList<String>
)

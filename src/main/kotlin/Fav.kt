package apoy2k.greenbookbot

import net.dv8tion.jda.api.entities.Message

const val ID_PATTERN_FAVMESSAGEID_TAG = "{favMessageId}"
const val ID_PATTERN_FORMAT = "::$ID_PATTERN_FAVMESSAGEID_TAG::"

val ID_PATTERN_REGEX = Regex(
    ID_PATTERN_FORMAT
        .replace(ID_PATTERN_FAVMESSAGEID_TAG, "(?<favMessageId>.+?)")
        .plus("$")
).toPattern()

data class Fav(
    val userId: String,
    val guildId: String,
    val messageId: String
)

// Find the Fav instance that was posted in a message by analyzing the id_pattern of the message
fun createFromPosted(message: Message) = with(message) {
    val match = ID_PATTERN_REGEX.matcher(contentRaw)
    if (!match.find()) {
        return@with null
    }

    val favPoster = referencedMessage?.author ?: return@with null
    val messageId = match.group("favMessageId")

    Fav(favPoster.id, guild.id, messageId)
}

package apoy2k.greenbookbot

import net.dv8tion.jda.api.entities.Message
import org.slf4j.LoggerFactory

const val ID_PATTERN_FAVMESSAGEID_TAG = "{favMessageId}"
const val ID_PATTERN_FORMAT = "::$ID_PATTERN_FAVMESSAGEID_TAG::"

private val LOG = LoggerFactory.getLogger("Fav")!!

val ID_PATTERN_REGEX = Regex(
    ID_PATTERN_FORMAT
        .replace(ID_PATTERN_FAVMESSAGEID_TAG, "(?<favMessageId>.+?)")
        .plus("$")
).toPattern()

data class Fav(
    val userId: String,
    val guildId: String,
    val messageId: String,
    val tags: MutableList<String>
)

// Find/Create a Fav object that was posted in a message by analyzing the id_pattern of the message
fun createFromPosted(message: Message) = with(message) {
    val match = ID_PATTERN_REGEX.matcher(contentRaw)
    if (!match.find()) {
        LOG.warn("Could not create Fav object from message ${message.id}")
        return@with null
    }

    val favPoster = referencedMessage?.author
    if (favPoster == null) {
        LOG.warn("Could not find author of referenced message from message ${message.id}")
        return@with null
    }
    val messageId = match.group("favMessageId")

    Fav(favPoster.id, guild.id, messageId, mutableListOf())
}

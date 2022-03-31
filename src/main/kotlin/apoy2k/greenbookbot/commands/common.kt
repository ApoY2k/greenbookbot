package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Fav
import apoy2k.greenbookbot.model.Storage
import apoy2k.greenbookbot.replyError
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

suspend fun getTopAuthors(favs: Collection<Fav>, jda: JDA): Collection<String> =
    favs.groupBy { it.authorId }
        .mapValues { grouping -> grouping.value.distinctBy { it.messageId }.size }
        .entries
        .sortedByDescending { it.value }
        .take(5)
        .map { entry ->
            val user = jda.retrieveUserById(entry.key).await()
            "**${user.name}**: ${entry.value}"
        }

fun getTopTags(favs: Collection<Fav>): Collection<String> {
    val tagCount = mutableMapOf<String, Int>()
    favs
        .forEach { fav ->
            fav.tags.forEach {
                val count = tagCount[it] ?: 0
                tagCount[it] = count + 1
            }
        }

    return tagCount.entries
        .sortedByDescending { it.value }
        .take(5)
        .map { entry -> "**${entry.key}**: ${entry.value}" }
}

suspend fun getTopUsed(favs: Collection<Fav>, jda: JDA): Collection<String> {
    return favs
        .sortedByDescending { it.used }
        .take(5)
        .map {
            val name = jda.retrieveUserById(it.authorId).await().name
            "**${it.id} ($name)**: ${it.used}"
        }
}

suspend fun Collection<Fav>.toVotesList(jda: JDA): Collection<String> = this.map {
    val name = jda.retrieveUserById(it.authorId).await().name
    "**${it.id} ($name):** ${it.votes.withExplicitSign()}"
}

private fun Int.withExplicitSign(): String = when (this > 0) {
    true -> "+$this"
    else -> "$this"
}

suspend fun retrieveMessageWithErrorHandling(
    fav: Fav,
    storage: Storage,
    event: SlashCommandInteractionEvent,
    channel: MessageChannel
): Message? {
    try {
        return channel.retrieveMessageById(fav.messageId).await()
    } catch (e: Exception) {
        with(e.message.orEmpty()) {
            if (contains("10008: Unknown Message")) {
                event.replyError(
                    "Fav [${fav.id}] points to a removed message.\n"
                            + "It will be removed so this doesn't happen again.",
                    fav.id
                )
                storage.removeFav(fav.id)
            }

            if (contains("Missing permission")) {
                event.replyError(
                    "No permission to channel:\n${fav.channelUrl()}\nPlease check my privileges.",
                    fav.id
                )
            }
        }
    }
    return null
}

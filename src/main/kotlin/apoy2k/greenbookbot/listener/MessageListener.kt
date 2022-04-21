package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Storage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListener(
    private val storage: Storage
) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) = runBlocking {
        val message = event.message

        if (message.contentRaw.startsWith("\$fav") && !message.author.isBot) {
            message.reply("It's /fav now. Get with the times, oldie ( ﾉ ﾟｰﾟ)ﾉ").await()
            return@runBlocking
        }

        if (!message.isFromType(ChannelType.PRIVATE)) {
            return@runBlocking
        }

        if (message.author.isBot) {
            return@runBlocking
        }

        val content = message.contentRaw
        if (content == "-") {
            return@runBlocking
        }

        val history = message.channel.getHistoryBefore(message.id, 1).await()
        val previousMessage = history.retrievedHistory.firstOrNull() ?: return@runBlocking

        if (!previousMessage.author.isBot) {
            return@runBlocking
        }

        val favId = previousMessage.embeds.firstOrNull()?.footer?.text.orEmpty()
        if (favId.isBlank()) {
            return@runBlocking
        }

        var tags = content
            .split(" ")
            .map { it.trim().lowercase() }
            .toSet()

        if (content == ".") {
            tags = emptySet()
        }

        storage.writeTags(favId, tags)
        message.addReaction("✅").await()
    }

}

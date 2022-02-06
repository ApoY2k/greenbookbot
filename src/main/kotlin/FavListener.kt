package apoy2k.greenbookbot

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class FavListener(
    private val storage: Storage
) : ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) = runBlocking {
        when (event.reaction.reactionEmote.name) {
            "\uD83D\uDCD7" -> addFav(event)
            "\uD83D\uDDD1️" -> removeFav(event)
            else -> Unit
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) = runBlocking {
        val message = event.message

        if (!message.isFromType(ChannelType.PRIVATE)) {
            return@runBlocking
        }

        val history = message.channel.getHistoryBefore(message.id, 1).submit().await()
        val previousMessage = history.retrievedHistory.firstOrNull() ?: return@runBlocking

        if (!previousMessage.contentRaw.contains("tags")) {
            return@runBlocking
        }

        if (!previousMessage.author.isBot) {
            return@runBlocking
        }

        val content = message.contentRaw
        if (content == "-") {
            return@runBlocking
        }

        val tags = content
            .split(" ")
            .map { it.trim() }
            .toSet()
        storage.addTagsToRecentFav(message.author.id, tags)
        message.addReaction("✅").submit()
    }

    private suspend fun addFav(event: MessageReactionAddEvent) {
        if (event.reaction.guild == null) {
            return
        }

        event.retrieveUser()
            .flatMap { user -> user.openPrivateChannel() }
            .flatMap { channel -> channel.sendMessage("Send tags for the fav (space-separated). Type '-' for no tags") }
            .submit()
        storage.saveNewFav(event.userId, event.guild.id, event.messageId)
    }

    private suspend fun removeFav(event: MessageReactionAddEvent) {
        val message = event.retrieveMessage().submit().await()
        val fav = createFromPosted(message) ?: return
        storage.removeFav(fav.userId, fav.messageId)
    }
}

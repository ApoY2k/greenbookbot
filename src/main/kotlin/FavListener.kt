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
        val code = with(event.reaction.reactionEmote) {
            if (!isEmoji) {
                return@runBlocking
            }
            asCodepoints
        }

        when (code) {
            "U+1f4d7" -> addFav(event)
            "U+1f5d1U+fe0f" -> removeFav(event)
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
        val message = event.retrieveMessage().submit().await()
        val author = message.author
        storage.saveNewFav(event.userId, event.guild.id, event.channel.id, event.messageId, author.id)
    }

    private suspend fun removeFav(event: MessageReactionAddEvent) {
        val message = event.retrieveMessage().submit().await()
        val favId = message.embeds.firstOrNull()?.footer?.text.orEmpty()
        storage.removeFav(favId)
    }
}

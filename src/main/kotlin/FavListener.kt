package apoy2k.greenbookbot

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
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
            "U+1f3f7U+fe0f" -> editFav(event)
            else -> Unit
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) = runBlocking {
        val message = event.message

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
            .map { it.trim() }
            .toSet()

        if (content == ".") {
            tags = emptySet()
        }

        storage.writeTags(favId, tags)
        message.addReaction("✅").await()
    }

    private suspend fun addFav(event: MessageReactionAddEvent) {
        if (event.reaction.guild == null) {
            return
        }

        val message = event.retrieveMessage().await()
        val author = message.author
        val favId = storage.saveNewFav(event.userId, event.guild.id, event.channel.id, event.messageId, author.id)

        event.retrieveUser()
            .flatMap { user -> user.openPrivateChannel() }
            .flatMap { channel ->
                channel.sendMessageEmbeds(
                    EmbedBuilder()
                        .setTitle("Add new fav")
                        .setAuthor(message.author.name, null, message.author.avatarUrl)
                        .setDescription(message.contentRaw)
                        .addField("Send tags for this fav", "`-` or `.` to not add any tags", false)
                        .setFooter(favId)
                        .build()
                )
            }
            .await()
    }

    private suspend fun removeFav(event: MessageReactionAddEvent) {
        val message = event.retrieveMessage().await()
        val favId = message.embeds.firstOrNull()?.footer?.text.orEmpty()
        storage.removeFav(favId)
    }

    private suspend fun editFav(event: MessageReactionAddEvent) {
        if (event.reaction.guild == null) {
            return
        }

        val message = event.retrieveMessage().await()
        val favId = message.embeds.firstOrNull()?.footer?.text.orEmpty()
        val fav = storage.getFav(favId) ?: return
        if (fav.userId != event.userId) {
            return
        }

        val author = event.jda.retrieveUserById(fav.authorId).await()
        val content = message.embeds.firstOrNull()?.description.orEmpty()

        event.retrieveUser()
            .flatMap { user -> user.openPrivateChannel() }
            .flatMap { channel ->
                channel.sendMessageEmbeds(
                    EmbedBuilder()
                        .setTitle("Edit fav")
                        .setAuthor(author.name, null, author.avatarUrl)
                        .setDescription(content)
                        .setFooter(favId)
                        .addField(
                            "Overwrite tags for this fav",
                            "Send `.` to remove all tags from the fav\n" +
                                    "Send `-` to abort the edit and leave tags as is",
                            false
                        )
                        .build()
                )
            }
            .await()
    }
}

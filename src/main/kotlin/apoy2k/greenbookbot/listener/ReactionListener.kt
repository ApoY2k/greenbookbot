package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.forMessage
import apoy2k.greenbookbot.model.Storage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ReactionListener(
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

    private suspend fun addFav(event: MessageReactionAddEvent) {
        if (event.reaction.guild == null) {
            return
        }

        val message = event.retrieveMessage().await()
        val author = message.author

        if (author.isBot) {
            return
        }

        val favId = storage.saveNewFav(event.userId, event.guild.id, event.channel.id, event.messageId, author.id)
        val embed = EmbedBuilder()
            .forMessage(message, favId)
            .setTitle("Add new fav")
            .addField("Send (space-separated) tags for this fav", "`-` or `.` to not add any tags", false)

        event.retrieveUser()
            .flatMap { user -> user.openPrivateChannel() }
            .flatMap { channel -> channel.sendMessageEmbeds(embed.build()) }
            .await()
    }

    private suspend fun removeFav(event: MessageReactionAddEvent) {
        val message = event.retrieveMessage().await()
        val favId = message.embeds.firstOrNull()?.footer?.text.orEmpty()
        val fav = storage.getFav(favId) ?: return
        if (fav.userId == event.userId) {
            storage.removeFav(favId)
        }
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

        val embed = EmbedBuilder()
            .forMessage(message, favId)
            .setTitle("Edit fav")
            .addField(
                "Overwrite (space-separated) tags for this fav",
                "Send `.` to remove all tags from the fav\n" +
                        "Send `-` to abort the edit and leave tags as is",
                false
            )

        event.retrieveUser()
            .flatMap { user -> user.openPrivateChannel() }
            .flatMap { channel -> channel.sendMessageEmbeds(embed.build()) }
            .await()
    }
}

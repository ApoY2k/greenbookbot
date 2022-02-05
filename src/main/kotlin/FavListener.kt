package apoy2k.greenbookbot

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class FavListener(
    private val storage: Storage
) : ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        with(event) {
            if (reaction.reactionEmote.name == "\uD83D\uDCD7") {
                addFav(event)
            }

            if (reaction.reactionEmote.name == "\uD83D\uDDD1") {
                removeFav(event)
            }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        with(event.message) {
            if (!isFromType(ChannelType.PRIVATE)) {
                return
            }

            channel.getHistoryBefore(id, 1).submit()
                .thenAccept { history ->
                    val previousMessage = history.retrievedHistory.first()
                    if (!previousMessage.contentRaw.contains("tags")) {
                        return@thenAccept
                    }
                    if (!previousMessage.author.isBot) {
                        return@thenAccept
                    }

                    if (contentRaw == "-") {
                        return@thenAccept
                    }

                    val favs = contentRaw
                        .split(" ")
                        .map { it.trim() }
                        .toSet()
                    storage.addTagsToRecentFav(author.id, favs)
                        .thenCompose { addReaction("✅").submit() }
                }
        }
    }

    private fun addFav(event: MessageReactionAddEvent) {
        with(event) {
            if (reaction.guild == null) {
                return
            }

            retrieveUser()
                .flatMap { user -> user.openPrivateChannel() }
                .flatMap { channel -> channel.sendMessage("Send tags for the fav (space-separated). Type '-' for no tags") }
            storage.saveNewFav(userId, guild.id, messageId)
        }
    }

    private fun removeFav(event: MessageReactionAddEvent) {
        with(event) {
            
        }
    }
}

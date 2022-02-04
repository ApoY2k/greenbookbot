package apoy2k.greenbookbot

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class FavListener(
    private val storage: Storage
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.reaction.reactionEmote.name == "\uD83D\uDCD7") {
            event.retrieveUser().submit()
                .thenCompose { user -> user.openPrivateChannel().submit() }
                .thenCompose { channel ->
                    channel.sendMessage("Send tags for the fav (space-separated). Type '-' for no tags").submit()
                }
            // TODO Save fav in DB without tags at this point already
            // The next message received from the user will be considered to contain the tags for the last saved fav
            // But as that is a new event, it must be handled in another method
            // If a user saves another fav before sending tags, if will just remain without tags
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // TODO Check if the previous message is about saving tags
        // If yes, parse tha tags of this message and attach to the *latest saved fav without any tags
        if (event.message.isFromType(ChannelType.PRIVATE)) {
            event.channel.getHistoryBefore(event.messageId, 1).submit()
                .thenAccept { history ->
                    val previousMessage = history.retrievedHistory.first().contentRaw
                    if (!previousMessage.contains("tags")) {
                        return@thenAccept
                    }

                    if (previousMessage == "-") {
                        log.info("No tags to add to most recent fav")
                    } else {
                        storage.addTagsToRecentFav(event.message.contentRaw)
                            .thenAccept { event.message.addReaction("✅").submit() }
                    }
                }
        }
    }
}

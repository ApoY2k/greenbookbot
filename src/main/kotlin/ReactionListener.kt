package apoy2k.greenbookbot

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class ReactionListener : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.reaction.reactionEmote.name == "\uD83D\uDCD7") {
            log.debug("Noticed greenbook on message [{}]", event.messageId)
            // TODO message reacting user with tag prompt
            // TODO write tags + message id to db
        }
    }
}

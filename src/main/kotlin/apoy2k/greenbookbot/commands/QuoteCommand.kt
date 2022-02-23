package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.forMessage
import apoy2k.greenbookbot.replyError
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val LINK = "link"

val QuoteCommand = Commands.slash("quote", "quote message")
    .addOption(
        OptionType.STRING,
        LINK, "Link to message", true
    )

suspend fun executeQuoteCommand(event: SlashCommandInteractionEvent) {
    val messageLink = event.getOption(LINK)?.asString.orEmpty()
    val tokenizedLink = messageLink.substringAfter("/channels/", "").split("/")
    if (tokenizedLink.size != 3) {
        return event.replyError("Invalid link format!")
    }

    val guildId = tokenizedLink[0]
    val channelId = tokenizedLink[1]
    val messageId = tokenizedLink[2]

    val guild = event.jda.guilds.firstOrNull { it.id == guildId }
    val channel = guild?.getTextChannelById(channelId) ?: guild?.getThreadChannelById(channelId)
    val message = channel?.retrieveMessageById(messageId)?.await()
        ?: return event.replyError("No message found at that link!")

    val embed = EmbedBuilder().forMessage(message).build()
    event.replyEmbeds(embed).await()
}

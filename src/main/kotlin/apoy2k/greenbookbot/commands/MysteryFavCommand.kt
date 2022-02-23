package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.forMessage
import apoy2k.greenbookbot.model.Storage
import apoy2k.greenbookbot.replyError
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

val MysteryFavCommand = Commands.slash("mystery", "Post a random fav (even other users), without revelaing who posted")

suspend fun executeMysteryFavCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    val guildIds = event.jda.guilds.map { it.id }

    val candidates = storage
        .getFavs(null, event.guild?.id, emptyList())
        .filter { guildIds.contains(it.guildId) }

    val fav = candidates.randomOrNull()
        ?: return event.replyError("No favs found")

    val guild = event.jda.guilds
        .firstOrNull { it.id == fav.guildId }
        ?: return event.replyError("Guild not found:\n${fav.guildUrl()}", fav.id)
    val channel = guild.getTextChannelById(fav.channelId)
        ?: guild.getThreadChannelById(fav.channelId)
        ?: return event.replyError("Channel not found:\n${fav.channelUrl()}", fav.id)

    val message = retrieveMessageWithErrorHandling(fav, storage, event, channel) ?: return
    val embed = EmbedBuilder().forMessage(message, fav.id)
        .setAuthor("Mystery Fav", message.jumpUrl)
        .build()
    event.replyEmbeds(embed).await()
}

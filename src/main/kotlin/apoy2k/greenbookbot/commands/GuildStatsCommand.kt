package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

val GuildStatsCommand = Commands.slash("serverstats", "Display server-wide fav stats")

suspend fun executeGuildStats(storage: Storage, event: SlashCommandInteractionEvent) {
    val favs = storage.getFavs(null, event.guild?.id, emptyList())

    val embed = EmbedBuilder()
        .setTitle("Server Stats")
        .writeStats(favs, event.jda)

    event.replyEmbeds(embed.build()).await()
}

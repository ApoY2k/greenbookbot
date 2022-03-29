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

    val usedCount = favs.sumOf { it.used }
    embed.addField("Counts", "**Saved**: ${favs.count()}\n**Posted**: $usedCount", true)

    val topAuthors = getTopAuthors(favs, event.jda)
    embed.addField("Top authors", topAuthors.joinToString("\n"), true)

    val topTags = getTopTags(favs)
    embed.addField("Top tags", topTags.joinToString("\n"), true)

    val highestVotes = favs.sortedByDescending { it.votes }.take(5)
        .toVotesList(event.jda, useAuthor = false)
    embed.addField("Highest votes (User)", highestVotes.joinToString("\n"), true)

    val lowestVotes = favs.sortedBy { it.votes }.take(5)
        .toVotesList(event.jda, useAuthor = false)
    embed.addField("Lowest votes (User)", lowestVotes.joinToString("\n"), true)

    event.replyEmbeds(embed.build()).await()
}

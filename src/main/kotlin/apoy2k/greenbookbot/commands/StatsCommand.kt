package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Storage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_TAG = "tag"

val StatsCommand = Commands.slash("stats", "Display your fav stats")
    .addOption(
        OptionType.STRING,
        OPTION_TAG,
        "Limit the listed counts to favs with at least one of these (space-separated) tags"
    )

suspend fun executeStatsCommand(storage: Storage, event: SlashCommandInteractionEvent) {
    val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
    val favs = storage.getFavs(event.user.id, event.guild?.id, tags)

    val embed = EmbedBuilder()
        .setTitle("${event.user.name} Stats")

    val usedCount = favs.sumOf { it.used }
    embed.addField("Counts", "**Saved**: ${favs.count()}\n**Posted**: $usedCount", true)

    val topAuthors = getTopAuthors(favs, event.jda)
    embed.addField("Top authors", topAuthors.joinToString("\n"), true)

    val topTags = getTopTags(favs)
    embed.addField("Top tags", topTags.joinToString("\n"), true)

    val highestVotes = favs.sortedByDescending { it.votes }.take(5)
        .toVotesList(event.jda, useAuthor = true)
    embed.addField("Highest votes (Author)", highestVotes.joinToString("\n"), true)

    val lowestVotes = favs.sortedBy { it.votes }.take(5)
        .toVotesList(event.jda, useAuthor = true)
    embed.addField("Lowest votes (Author)", lowestVotes.joinToString("\n"), true)

    event.replyEmbeds(embed.build()).await()
}

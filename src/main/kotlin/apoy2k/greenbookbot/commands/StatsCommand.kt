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
    val interaction = event.reply("Fetching favs...").await()

    val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
    val favs = storage.getFavs(event.user.id, event.guild?.id, tags)
    interaction.editOriginal("Found ${favs.size} favs, calculating stats...").await()

    val embed = EmbedBuilder()
        .setTitle("${event.user.name} Stats")
        .writeStats(favs, event.jda)

    interaction.editOriginal("Got em!").await()
    interaction.editOriginalEmbeds(embed.build()).await()
}

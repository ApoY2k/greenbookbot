package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color

private const val HELP_TEXT = """
**GreenBookBot** allows you to fav messages and re-post them later by referencing tags set on fav creation.
https://github.com/ApoY2k/greenbookbot

**Creating a fav**
React with :green_book: on any posted message and then set the tags for the fav by replying to the bots message.

**Posting a fav**
Use the `/fav` command to post a random fav from your whole list.
If you also add a (space-separated) list of tags, the posted fav will be selected (randomly) only from favs that have any of the provided tags.

**Removing a fav**
React with :wastebasket: on the posted fav message from the bot.

**Listing favs**
Use the `/list` command to get a list of all your used tags of all favs.
As with `/fav`, provide a (space-separated) list of tags to limit the list to only those tags.

**Editing tags on a fav**
React with :label: on the posted fav to re-set all tags for this fav.

**Quoting messages**
Use the `/quote` command and a message link to embed a quote.

**Stats**
Use `/stats` for personal and `/serverstats` for server-wide stats about favs.
"""

val HelpCommand = Commands.slash("help", "Display usage help")

suspend fun executeHelpCommand(event: SlashCommandInteractionEvent) {
    event.replyEmbeds(
        EmbedBuilder()
            .setColor(Color(25, 80, 150))
            .setDescription(HELP_TEXT)
            .build()
    )
        .setEphemeral(true)
        .await()
}

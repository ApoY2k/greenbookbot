package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.model.Fav
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.RestAction
import java.awt.Color

suspend fun <T> RestAction<T>.await(): T = this.submit().await()

suspend fun SlashCommandInteractionEvent.replyError(message: String, favId: String? = null) {
    this.replyEmbeds(
        EmbedBuilder()
            .setDescription(message)
            .setFooter(favId)
            .setColor(Color(150, 25, 25))
            .build()
    )
        .setEphemeral(true)
        .await()
}

fun Fav.url() = "https://discord.com/channels/${guildId}/${channelId}"

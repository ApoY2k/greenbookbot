package apoy2k.greenbookbot.model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class Fav(
    val id: String,
    val userId: String,
    val guildId: String,
    val channelId: String,
    val messageId: String,
    val authorId: String,
    val tags: Collection<String>,
    val used: Int,
) {

    companion object {

        @JvmStatic
        fun fromResultRow(resultRow: ResultRow) =
            Fav(
                resultRow[Favs.id].toString(),
                resultRow[Favs.userId],
                resultRow[Favs.guildId],
                resultRow[Favs.channelId],
                resultRow[Favs.messageId],
                resultRow[Favs.authorId],
                resultRow[Favs.tags]
                    .split(" ")
                    .filter { it.isNotBlank() },
                resultRow[Favs.used],
            )
    }

    override fun toString(): String {
        return "Fav[$id](G:$guildId,C:$channelId,M:$messageId)"
    }

    fun guildUrl() = "https://discord.com/channels/$guildId"

    fun channelUrl() = "https://discord.com/channels/$guildId/$channelId"
}

object Favs : Table() {
    val id = integer("id").autoIncrement()
    val userId = varchar("userId", 100)
    val guildId = varchar("guildId", 100)
    val channelId = varchar("channelId", 100)
    val messageId = varchar("messageId", 100)
    val authorId = varchar("authorId", 100)
    val tags = varchar("tags", 200)
    val used = integer("used")

    override val primaryKey = PrimaryKey(id)
}

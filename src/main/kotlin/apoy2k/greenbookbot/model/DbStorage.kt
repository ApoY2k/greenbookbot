package apoy2k.greenbookbot.model

import apoy2k.greenbookbot.Env
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class DbStorage(env: Env) : Storage {

    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val dataSource = HikariDataSource().also {
        it.jdbcUrl = env.dbUrl
        it.username = env.dbUser
        it.password = env.dbPassword
    }

    init {
        // Fetch a connection immediately, so it's ready as soon as the class is loaded
        dataSource.connection

        Flyway.configure()
            .locations("classpath:migrations")
            .baselineOnMigrate(true)
            .dataSource(dataSource)
            .load()
            .migrate()
    }

    override suspend fun saveNewFav(
        userId: String,
        guildId: String,
        channelId: String,
        messageId: String,
        authorId: String
    ): String {
        log.info("Creating fav for User[$userId] Guild[$guildId] Channel[$channelId] Message[$messageId]")
        val favId = query(dataSource) {
            Favs.insert {
                it[Favs.userId] = userId
                it[Favs.guildId] = guildId
                it[Favs.channelId] = channelId
                it[Favs.messageId] = messageId
                it[Favs.authorId] = authorId
                it[tags] = ""
            } get Favs.id
        }

        return favId.toString()
    }

    override suspend fun getFavs(userId: String?, guildId: String?, tags: Collection<String>): List<Fav> {
        log.info("Getting favs for User[$userId] Guild[$guildId] Tags$tags")
        return query(dataSource) {
            val query = Favs.selectAll()
                .orderBy(Favs.used to SortOrder.DESC)

            if (!userId.isNullOrBlank()) {
                query.andWhere { Favs.userId eq userId }
            }

            if (!guildId.isNullOrBlank()) {
                query.andWhere { Favs.guildId eq guildId }
            }

            if (tags.isNotEmpty()) {
                query.andWhere {
                    tags.map {
                        Op.build { Favs.tags like "% ${it.lowercase()} %" }
                    }.compoundOr()
                }
            }

            query.map { Fav.fromResultRow(it) }
        }
    }

    override suspend fun removeFav(favId: String) {
        val id = favId.toIntOrNull() ?: return
        log.info("Removing Fav[$id]")
        query(dataSource) {
            Favs.deleteWhere { Favs.id eq id }
        }
    }

    override suspend fun writeTags(favId: String, tags: Collection<String>) {
        val id = favId.toIntOrNull() ?: return
        val writeTags = tags.map { it.lowercase().trim() }
        log.info("Setting tags of Fav[$id] to $writeTags")
        query(dataSource) {
            Favs.update({ Favs.id eq id }) {
                it[Favs.tags] = " " + writeTags.joinToString(" ") + " "
            }
        }
    }

    override suspend fun getFav(favId: String): Fav? {
        val id = favId.toIntOrNull() ?: return null
        log.info("Fetching Fav[$id]")
        return query(dataSource) {
            Favs.select { Favs.id eq id }
                .map { Fav.fromResultRow(it) }
                .firstOrNull()
        }
    }

    override suspend fun increaseUsed(fav: Fav) {
        val id = fav.id.toIntOrNull() ?: return
        log.info("Increasing used count of Fav[$id]")
        query(dataSource) {
            Favs.update({ Favs.id eq id }) {
                it[used] = fav.used + 1
            }
        }
    }

    override suspend fun upvote(fav: Fav) {
        val id = fav.id.toIntOrNull() ?: return
        log.info("Upvoting Fav[$id]")
        query(dataSource) {
            Favs.update({ Favs.id eq id }) {
                it[votes] = fav.votes + 1
            }
        }
    }

    override suspend fun downvote(fav: Fav) {
        val id = fav.id.toIntOrNull() ?: return
        log.info("Downvoting Fav[$id]")
        query(dataSource) {
            Favs.update({ Favs.id eq id }) {
                it[votes] = fav.votes - 1
            }
        }
    }

    private suspend fun <T> query(dataSource: DataSource, block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction(Database.connect(dataSource)) {
                block()
            }
        }
}

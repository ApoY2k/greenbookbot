create table if not exists favs
(
    id          serial
        primary key,
    "userId"    varchar(100) not null,
    "guildId"   varchar(100) not null,
    "channelId" varchar(100) not null,
    "messageId" varchar(100) not null,
    "authorId"  varchar(100) not null,
    tags        varchar(200) not null
);

alter table favs
    owner to postgres;


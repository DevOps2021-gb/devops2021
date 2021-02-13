drop table if exists user;
create table user (
  userId integer primary key autoincrement,
  username string not null,
  email string not null,
  pwHash string not null
);

drop table if exists follower;
create table follower (
  whoId integer,
  whomId integer
);

drop table if exists message;
create table message (
  messageId integer primary key autoincrement,
  authorId integer not null,
  text string not null,
  pubDate integer,
  flagged integer
);
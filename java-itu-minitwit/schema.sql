drop table if exists user;
create table user (
  user_id integer primary key autoincrement,
  username string not null,
  email string not null,
  pw_hash string not null
);

drop table if exists follower;
create table follower (
  who_id integer,
  whom_id integer
);

drop table if exists message;
create table message (
  message_id integer primary key autoincrement,
  author_id integer not null,
  text string not null,
  pub_date integer,
  flagged integer
);
insert into user (user_id, username, email, pw_hash) values (0,"bob", "bob@itu.dk", "test");
insert into message (message_id, author_id, text, pub_date, flagged) values (0,"bob", "message string", 0, 0);
insert into follower (who_id, whom_id) values (0, 0);
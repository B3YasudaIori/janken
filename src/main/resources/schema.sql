CREATE TABLE users (
    id IDENTITY,
    userName VARCHAR NOT NULL
);

create table matches(
    id identity,
    user1 int not null,
    user2 int not null,
    user1Hand varchar not null,
    user2Hand varchar not null
)

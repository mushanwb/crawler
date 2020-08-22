create table news(
    id bigint primary key auto_increment,
    title text,
    content text,
    url varchar(2000),
    created_at timestamp default now(),
    updated_at timestamp default now()
) default charset=utf8mb4;

create table links_already_processed(
    link varchar(2000)
);

create table links_to_be_processed(
    link varchar(2000)
);
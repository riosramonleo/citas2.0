create table swipes (
    id bigserial primary key,
    from_user_id varchar(64) not null,
    to_user_id   varchar(64) not null,
    action       varchar(16) not null,
    created_at   timestamptz not null default now()
);

create unique index ux_swipes_from_to on swipes(from_user_id, to_user_id);
create index ix_swipes_to_user on swipes(to_user_id);
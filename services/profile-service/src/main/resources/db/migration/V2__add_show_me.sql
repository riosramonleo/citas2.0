alter table user_basics
    add column if not exists show_me varchar(16) not null default 'EVERYONE';
create table if not exists user_basics (
    user_id varchar(64) primary key,
    gender varchar(16) not null,
    updated_at timestamptz not null default now()
    );
create table if not exists messages (
    id uuid primary key,
    match_id uuid not null,
    sender_user_id varchar(64) not null,
    content varchar(2000) not null,
    created_at timestamptz not null default now()
    );

create index if not exists idx_messages_match_created on messages(match_id, created_at);
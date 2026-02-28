create table if not exists message_reads (
    match_id uuid not null,
    user_id varchar(64) not null,
    last_read_at timestamptz not null default '1970-01-01 00:00:00+00',
    updated_at timestamptz not null default now(),
    primary key (match_id, user_id)
    );

create index if not exists idx_message_reads_user on message_reads(user_id);
create index if not exists idx_message_reads_match on message_reads(match_id);
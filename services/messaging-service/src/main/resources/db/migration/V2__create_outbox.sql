create table if not exists outbox (
    id uuid primary key,
    aggregate_type varchar(64) not null,
    aggregate_id varchar(128) not null,
    event_type varchar(128) not null,
    payload jsonb not null,
    created_at timestamptz not null default now(),
    published_at timestamptz,
    attempts int not null default 0,
    last_error text
    );

create index if not exists idx_outbox_unpublished on outbox(published_at, created_at);
create index if not exists idx_outbox_event_type on outbox(event_type);
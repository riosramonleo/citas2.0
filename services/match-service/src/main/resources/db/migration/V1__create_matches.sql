create table if not exists matches (
    id uuid primary key,
    user1_id varchar(64) not null,
    user2_id varchar(64) not null,
    state varchar(32) not null,
    first_mover_user_id varchar(64),
    created_at timestamptz not null default now(),
    expires_at timestamptz,
    activated_at timestamptz,
    expired_at timestamptz,
    constraint uq_match_users unique (user1_id, user2_id)
    );

create index if not exists idx_matches_user1 on matches(user1_id);
create index if not exists idx_matches_user2 on matches(user2_id);
create index if not exists idx_matches_state_expires on matches(state, expires_at);
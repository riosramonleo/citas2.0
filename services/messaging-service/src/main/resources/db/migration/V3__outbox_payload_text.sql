alter table outbox
alter column payload type text
  using payload::text;
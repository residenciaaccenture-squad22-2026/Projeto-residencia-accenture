alter table equipamento add column if not exists sala_id bigint;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_equipamento_sala') then
        alter table equipamento
            add constraint fk_equipamento_sala foreign key (sala_id) references sala(id) on delete cascade;
    end if;
end $$;

create index if not exists idx_equipamento_sala on equipamento (sala_id);

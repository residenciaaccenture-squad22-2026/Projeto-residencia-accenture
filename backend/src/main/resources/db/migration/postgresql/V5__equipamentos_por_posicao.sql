alter table equipamento add column if not exists posicao_id bigint;

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_equipamento_posicao') then
        alter table equipamento
            add constraint fk_equipamento_posicao foreign key (posicao_id) references posicao(id) on delete cascade;
    end if;
end $$;

create index if not exists idx_equipamento_posicao on equipamento (posicao_id);

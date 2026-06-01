alter table if exists equipamentos alter column sala_id drop not null;
alter table if exists equipamentos add column if not exists posicao_id bigint;

do $$
begin
    if to_regclass('public.equipamentos') is not null
        and to_regclass('public.posicoes') is not null
        and not exists (select 1 from pg_constraint where conname = 'fk_equipamentos_posicoes')
    then
        alter table equipamentos
            add constraint fk_equipamentos_posicoes foreign key (posicao_id) references posicoes (id);
    end if;

    if to_regclass('public.equipamentos') is not null
        and not exists (select 1 from pg_constraint where conname = 'ck_equipamentos_recurso')
    then
        alter table equipamentos add constraint ck_equipamentos_recurso check (
            (sala_id is not null and posicao_id is null)
            or (sala_id is null and posicao_id is not null)
        );
    end if;
end $$;

create index if not exists idx_equipamentos_posicao on equipamentos (posicao_id);

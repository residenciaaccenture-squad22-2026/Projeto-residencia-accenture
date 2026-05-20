alter table if exists reservas drop constraint if exists fk_reservas_equipamentos;
drop index if exists idx_reservas_equipamento_periodo;
alter table if exists reservas drop column if exists equipamento_id;

alter table if exists equipamentos add column if not exists sala_id bigint;

insert into salas (nome, capacidade, localizacao, status)
select 'Sala nao informada', 1, 'Migracao automatica', 'DISPONIVEL'
where exists (
    select 1 from equipamentos where sala_id is null
)
and not exists (
    select 1 from salas
);

update equipamentos
set sala_id = (
    select id from salas order by id limit 1
)
where sala_id is null
  and exists (select 1 from salas);

alter table if exists equipamentos alter column sala_id set not null;

do $$
begin
    if to_regclass('public.equipamentos') is not null
        and to_regclass('public.salas') is not null
        and not exists (select 1 from pg_constraint where conname = 'fk_equipamentos_salas')
    then
        alter table equipamentos
            add constraint fk_equipamentos_salas foreign key (sala_id) references salas (id);
    end if;
end $$;

create index if not exists idx_equipamentos_sala on equipamentos (sala_id);

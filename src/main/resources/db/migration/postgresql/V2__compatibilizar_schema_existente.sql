do $$
begin
    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'datahorainicio'
    ) and not exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'data_hora_inicio'
    ) then
        alter table reservas rename column datahorainicio to data_hora_inicio;
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'dataHoraInicio'
    ) and not exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'data_hora_inicio'
    ) then
        alter table reservas rename column "dataHoraInicio" to data_hora_inicio;
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'datahorafim'
    ) and not exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'data_hora_fim'
    ) then
        alter table reservas rename column datahorafim to data_hora_fim;
    end if;

    if exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'dataHoraFim'
    ) and not exists (
        select 1 from information_schema.columns
        where table_schema = 'public' and table_name = 'reservas' and column_name = 'data_hora_fim'
    ) then
        alter table reservas rename column "dataHoraFim" to data_hora_fim;
    end if;
end $$;

alter table if exists salas add column if not exists status varchar(30);
update salas set status = 'DISPONIVEL' where status is null;
alter table if exists salas alter column status set default 'DISPONIVEL';
alter table if exists salas alter column status set not null;

alter table if exists equipamentos add column if not exists status varchar(30);
update equipamentos set status = 'DISPONIVEL' where status is null;
alter table if exists equipamentos alter column status set default 'DISPONIVEL';
alter table if exists equipamentos alter column status set not null;

alter table if exists reservas add column if not exists equipamento_id bigint;
alter table if exists reservas add column if not exists status varchar(30);
update reservas set status = 'ATIVA' where status is null;
alter table if exists reservas alter column status set default 'ATIVA';
alter table if exists reservas alter column status set not null;

do $$
begin
    if to_regclass('public.salas') is not null
        and not exists (select 1 from pg_constraint where conname = 'ck_salas_status')
    then
        alter table salas add constraint ck_salas_status check (status in ('DISPONIVEL', 'INDISPONIVEL', 'MANUTENCAO'));
    end if;

    if to_regclass('public.equipamentos') is not null
        and not exists (select 1 from pg_constraint where conname = 'ck_equipamentos_status')
    then
        alter table equipamentos add constraint ck_equipamentos_status check (status in ('DISPONIVEL', 'INDISPONIVEL', 'MANUTENCAO'));
    end if;

    if to_regclass('public.reservas') is not null
        and not exists (select 1 from pg_constraint where conname = 'ck_reservas_status')
    then
        alter table reservas add constraint ck_reservas_status check (status in ('ATIVA', 'CANCELADA'));
    end if;

    if to_regclass('public.reservas') is not null
        and not exists (select 1 from pg_constraint where conname = 'ck_reservas_periodo')
    then
        alter table reservas add constraint ck_reservas_periodo check (data_hora_inicio < data_hora_fim);
    end if;

    if to_regclass('public.reservas') is not null
        and to_regclass('public.equipamentos') is not null
        and not exists (select 1 from pg_constraint where conname = 'fk_reservas_equipamentos')
    then
        alter table reservas
            add constraint fk_reservas_equipamentos foreign key (equipamento_id) references equipamentos (id);
    end if;
end $$;

create index if not exists idx_reservas_sala_periodo on reservas (sala_id, data_hora_inicio, data_hora_fim);
create index if not exists idx_reservas_equipamento_periodo on reservas (equipamento_id, data_hora_inicio, data_hora_fim);
create index if not exists idx_reservas_status on reservas (status);

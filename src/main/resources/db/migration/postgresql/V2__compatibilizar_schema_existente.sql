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

create index if not exists idx_reservas_sala_periodo on reservas (sala_id, data_hora_inicio, data_hora_fim);
create index if not exists idx_reservas_equipamento_periodo on reservas (equipamento_id, data_hora_inicio, data_hora_fim);
create index if not exists idx_reservas_status on reservas (status);

alter table funcionario add column if not exists role text;
alter table funcionario add column if not exists cargo text;

alter table posicao add column if not exists descricao text;
alter table posicao add column if not exists localizacao text;
alter table posicao add column if not exists recursos text;
alter table posicao add column if not exists status text;
alter table posicao add column if not exists disponivel boolean default true;

alter table reserva add column if not exists funcionario_id bigint;
alter table reserva add column if not exists posicao_id bigint;

create index if not exists idx_reserva_funcionario_id on reserva (funcionario_id);
create index if not exists idx_reserva_posicao_periodo on reserva (posicao_id, data_inicio, data_fim);
create index if not exists idx_posicao_status on posicao (status);

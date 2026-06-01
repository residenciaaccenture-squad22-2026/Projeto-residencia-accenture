alter table equipamentos alter column sala_id drop not null;
alter table equipamentos add column posicao_id bigint;

alter table equipamentos
    add constraint fk_equipamentos_posicoes foreign key (posicao_id) references posicoes (id);

alter table equipamentos add constraint ck_equipamentos_recurso check (
    (sala_id is not null and posicao_id is null)
    or (sala_id is null and posicao_id is not null)
);

create index idx_equipamentos_posicao on equipamentos (posicao_id);

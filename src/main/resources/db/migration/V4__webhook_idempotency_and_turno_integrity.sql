create table whatsapp_processed_messages (
    id bigserial primary key,
    message_id varchar(120) not null unique,
    telefono varchar(30) not null,
    processed_at timestamp not null
);

create index idx_whatsapp_processed_messages_processed_at
    on whatsapp_processed_messages (processed_at desc);

create extension if not exists btree_gist;

alter table turnos
    add constraint ex_turnos_peluquero_solapados
    exclude using gist (
        peluquero_id with =,
        tsrange(fecha_hora_inicio, fecha_hora_fin, '[)') with &&
    )
    where (estado in ('RESERVADO', 'EN_CURSO', 'COMPLETADO', 'AUSENTE'));

create table whatsapp_conversations (
    id bigserial primary key,
    telefono varchar(30) not null,
    cliente_id bigint references clientes(id),
    nombre varchar(120),
    apellido varchar(120),
    dia_seleccionado varchar(30),
    franja_seleccionada varchar(30),
    fecha_seleccionada date,
    horario_seleccionado timestamp,
    turno_seleccionado_id bigint,
    reprogramando boolean not null default false,
    step varchar(50) not null,
    activa boolean not null default true,
    created_at timestamp not null,
    updated_at timestamp not null,
    last_interaction_at timestamp not null
);

create table whatsapp_message_logs (
    id bigserial primary key,
    conversation_id bigint references whatsapp_conversations(id),
    telefono varchar(30) not null,
    direction varchar(10) not null,
    mensaje varchar(4000) not null,
    created_at timestamp not null,
    constraint chk_whatsapp_message_direction check (direction in ('ENTRANTE', 'SALIENTE'))
);

create unique index uq_whatsapp_conversations_active_phone
    on whatsapp_conversations (telefono)
    where activa = true;

create index idx_whatsapp_conversations_phone_updated
    on whatsapp_conversations (telefono, updated_at desc);

create index idx_whatsapp_message_logs_phone_created
    on whatsapp_message_logs (telefono, created_at desc);

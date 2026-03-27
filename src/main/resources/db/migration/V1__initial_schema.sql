create table peluquerias (
    id bigserial primary key,
    nombre varchar(120) not null,
    direccion varchar(255),
    telefono varchar(30) not null,
    hora_apertura time not null,
    hora_cierre time not null
);

create table peluqueros (
    id bigserial primary key,
    nombre varchar(120) not null,
    telefono varchar(30) not null,
    peluqueria_id bigint not null references peluquerias(id)
);

create table clientes (
    id bigserial primary key,
    nombre varchar(120) not null,
    telefono varchar(30) not null unique,
    observaciones varchar(500)
);

create table servicios (
    id bigserial primary key,
    nombre varchar(120) not null,
    duracion_minutos integer not null,
    peluqueria_id bigint not null references peluquerias(id)
);

create table turnos (
    id bigserial primary key,
    peluqueria_id bigint not null references peluquerias(id),
    peluquero_id bigint not null references peluqueros(id),
    cliente_id bigint not null references clientes(id),
    servicio_id bigint not null references servicios(id),
    fecha_hora_inicio timestamp not null,
    fecha_hora_fin timestamp not null,
    estado varchar(20) not null,
    observaciones varchar(500),
    constraint chk_turnos_estado check (estado in ('RESERVADO', 'CANCELADO', 'COMPLETADO', 'AUSENTE', 'EN_CURSO', 'REPROGRAMADO')),
    constraint chk_turnos_rango check (fecha_hora_fin > fecha_hora_inicio)
);

create table bloqueos_horarios (
    id bigserial primary key,
    peluqueria_id bigint not null references peluquerias(id),
    peluquero_id bigint references peluqueros(id),
    fecha_hora_inicio timestamp not null,
    fecha_hora_fin timestamp not null,
    motivo varchar(255) not null,
    constraint chk_bloqueos_rango check (fecha_hora_fin > fecha_hora_inicio)
);

create index idx_peluqueros_peluqueria on peluqueros (peluqueria_id);
create index idx_servicios_peluqueria on servicios (peluqueria_id);
create index idx_turnos_peluqueria_fecha on turnos (peluqueria_id, fecha_hora_inicio);
create index idx_turnos_peluquero_fecha on turnos (peluquero_id, fecha_hora_inicio);
create index idx_turnos_estado on turnos (estado);
create index idx_bloqueos_peluqueria_fecha on bloqueos_horarios (peluqueria_id, fecha_hora_inicio);
create index idx_bloqueos_peluquero_fecha on bloqueos_horarios (peluquero_id, fecha_hora_inicio);

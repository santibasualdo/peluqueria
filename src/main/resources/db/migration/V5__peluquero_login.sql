create extension if not exists pgcrypto;

alter table peluqueros
    add column if not exists usuario varchar(40);

alter table peluqueros
    add column if not exists password_hash varchar(255);

alter table peluqueros
    add column if not exists activo boolean not null default true;

update peluqueros
set usuario = regexp_replace(telefono, '\D', '', 'g')
where usuario is null or trim(usuario) = '';

update peluqueros
set password_hash = crypt(regexp_replace(telefono, '\D', '', 'g'), gen_salt('bf'))
where password_hash is null or trim(password_hash) = '';

alter table peluqueros
    alter column usuario set not null;

alter table peluqueros
    alter column password_hash set not null;

create unique index if not exists uq_peluqueros_usuario
    on peluqueros (lower(usuario));

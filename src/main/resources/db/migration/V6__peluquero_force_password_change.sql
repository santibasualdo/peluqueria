alter table peluqueros
    add column if not exists requiere_cambio_password boolean not null default false;

alter table turnos
    add column recordatorio_enviado_at timestamp;

alter table turnos
    add column asistencia_confirmada_at timestamp;

create index idx_turnos_recordatorio_pendiente
    on turnos (estado, fecha_hora_inicio, recordatorio_enviado_at);

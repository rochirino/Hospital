package org.jcr.entidades;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class Cita implements Serializable {

    private final Paciente paciente;
    private final Medico medico;
    private final Sala sala;
    private final LocalDateTime fechaHora;
    private final BigDecimal costo;
    private EstadoCita estado;
    private String observaciones;

    @Builder
    public Cita(@NonNull Paciente paciente,
                @NonNull Medico medico,
                @NonNull Sala sala,
                @NonNull LocalDateTime fechaHora,
                @NonNull BigDecimal costo,
                EstadoCita estado,
                String observaciones) {

        this.paciente = paciente;
        this.medico = medico;
        this.sala = sala;
        this.fechaHora = fechaHora;
        this.costo = costo;

        // Valores por defecto si no se pasan
        this.estado = (estado != null) ? estado : EstadoCita.PROGRAMADA;
        this.observaciones = (observaciones != null) ? observaciones : "";
    }

    public void setEstado(@NonNull EstadoCita estado) {
        this.estado = estado;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = (observaciones != null) ? observaciones : "";
    }

    @Override
    public String toString() {
        return "Cita{" +
                "paciente=" + paciente.getNombreCompleto() +
                ", medico=" + medico.getNombreCompleto() +
                ", sala=" + sala.getNumero() +
                ", fechaHora=" + fechaHora +
                ", estado=" + estado.getDescripcion() +
                ", costo=" + costo +
                '}';
    }

    public String toCsvString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                paciente.getDni(),
                medico.getDni(),
                sala.getNumero(),
                fechaHora.toString(),
                costo.toString(),
                estado.name(),
                observaciones.replaceAll(",", ";"));
    }

    public static Cita fromCsvString(String csvString,
                                     Map<String, Paciente> pacientes,
                                     Map<String, Medico> medicos,
                                     Map<String, Sala> salas) throws CitaException {

        String[] values = csvString.split(",");
        if (values.length != 7) {
            throw new CitaException("Formato de CSV inválido para Cita: " + csvString);
        }

        String dniPaciente = values[0];
        String dniMedico = values[1];
        String numeroSala = values[2];
        LocalDateTime fechaHora = LocalDateTime.parse(values[3]);
        BigDecimal costo = new BigDecimal(values[4]);
        EstadoCita estado = EstadoCita.valueOf(values[5]);
        String observaciones = values[6].replaceAll(";", ",");

        Paciente paciente = pacientes.get(dniPaciente);
        Medico medico = medicos.get(dniMedico);
        Sala sala = salas.get(numeroSala);

        if (paciente == null) {
            throw new CitaException("Paciente no encontrado: " + dniPaciente);
        }
        if (medico == null) {
            throw new CitaException("Médico no encontrado: " + dniMedico);
        }
        if (sala == null) {
            throw new CitaException("Sala no encontrada: " + numeroSala);
        }

        return Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .sala(sala)
                .fechaHora(fechaHora)
                .costo(costo)
                .estado(estado)
                .observaciones(observaciones)
                .build();
    }
}

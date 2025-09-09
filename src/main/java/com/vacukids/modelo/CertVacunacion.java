package com.vacukids.dto;

import java.time.LocalDate;

public class CertVacunacion {

    public int idAplicacion;
    public String paciente;
    public String cedulaPaciente;
    public String tutor;
    public String cedulaTutor;
    public String vacuna;
    public int dosisNumero;
    public LocalDate fechaAplicacion;
    public String centro;
    public String lote;
    public String profesional;
    public String correoProfesional;
    public String codigoVerificacion; // UUID/Hash
}

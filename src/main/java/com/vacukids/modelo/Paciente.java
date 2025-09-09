package com.vacukids.modelo;

import java.time.LocalDate;

public class Paciente {

    private Integer idPaciente;
    private String cedula;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento; // <— LocalDate (JSR-310)
    private String sexo;               // 'M' o 'F'
    private String direccion;
    private String telefono;
    private Integer idTutor;

    public Paciente() {
    }

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(Integer idTutor) {
        this.idTutor = idTutor;
    }

    @Override
    public String toString() {
        return "Paciente{"
                + "idPaciente=" + idPaciente
                + ", cedula='" + cedula + '\''
                + ", nombres='" + nombres + '\''
                + ", apellidos='" + apellidos + '\''
                + ", fechaNacimiento=" + fechaNacimiento
                + ", sexo='" + sexo + '\''
                + ", direccion='" + direccion + '\''
                + ", telefono='" + telefono + '\''
                + ", idTutor=" + idTutor
                + '}';
    }
}

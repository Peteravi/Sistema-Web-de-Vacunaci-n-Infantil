package com.vacukids.modelo;

public class CentroOpcion {

    private Integer idCentro;
    private String nombre;

    public CentroOpcion() {
    }

    public CentroOpcion(Integer idCentro, String nombre) {
        this.idCentro = idCentro;
        this.nombre = nombre;
    }

    public Integer getIdCentro() {
        return idCentro;
    }

    public void setIdCentro(Integer idCentro) {
        this.idCentro = idCentro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

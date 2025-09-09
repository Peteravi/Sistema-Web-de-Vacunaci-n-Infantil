package com.vacukids.model;

import java.time.LocalDateTime;

public class EsquemaDetalle {

    private Integer idDetalle;
    private Integer idEsquema;
    private Integer idVacuna;
    private Integer nroDosis;
    private Integer edadMinMeses;   // null permitido
    private Integer edadMaxMeses;   // null permitido
    private Integer intervaloMinDias; // null permitido
    private Integer intervaloMaxDias; // null permitido
    private boolean requisitoDosisPrevia;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdEsquema() {
        return idEsquema;
    }

    public void setIdEsquema(Integer idEsquema) {
        this.idEsquema = idEsquema;
    }

    public Integer getIdVacuna() {
        return idVacuna;
    }

    public void setIdVacuna(Integer idVacuna) {
        this.idVacuna = idVacuna;
    }

    public Integer getNroDosis() {
        return nroDosis;
    }

    public void setNroDosis(Integer nroDosis) {
        this.nroDosis = nroDosis;
    }

    public Integer getEdadMinMeses() {
        return edadMinMeses;
    }

    public void setEdadMinMeses(Integer edadMinMeses) {
        this.edadMinMeses = edadMinMeses;
    }

    public Integer getEdadMaxMeses() {
        return edadMaxMeses;
    }

    public void setEdadMaxMeses(Integer edadMaxMeses) {
        this.edadMaxMeses = edadMaxMeses;
    }

    public Integer getIntervaloMinDias() {
        return intervaloMinDias;
    }

    public void setIntervaloMinDias(Integer intervaloMinDias) {
        this.intervaloMinDias = intervaloMinDias;
    }

    public Integer getIntervaloMaxDias() {
        return intervaloMaxDias;
    }

    public void setIntervaloMaxDias(Integer intervaloMaxDias) {
        this.intervaloMaxDias = intervaloMaxDias;
    }

    public boolean isRequisitoDosisPrevia() {
        return requisitoDosisPrevia;
    }

    public void setRequisitoDosisPrevia(boolean requisitoDosisPrevia) {
        this.requisitoDosisPrevia = requisitoDosisPrevia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}

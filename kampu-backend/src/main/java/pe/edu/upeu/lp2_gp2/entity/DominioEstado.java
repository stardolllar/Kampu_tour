package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.Immutable;

// Catálogo de estados de las operaciones (solo lectura: lo carga 01_schema_v3.sql)
@Entity
@Immutable
@Table(name = "DOMINIO_ESTADOS")
public class DominioEstado {
    @Id
    @Column(name = "codigo_estado")
    private String codigoEstado;

    @Column(name = "nombre_mostrar")
    private String nombreMostrar;

    @Column(name = "orden")
    private Integer orden;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "requiere_recursos")
    private String requiereRecursos;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ocupa_recursos")
    private String ocupaRecursos;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "es_final")
    private String esFinal;

    public DominioEstado() {
    }

    public String getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(String codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public String getNombreMostrar() {
        return nombreMostrar;
    }

    public void setNombreMostrar(String nombreMostrar) {
        this.nombreMostrar = nombreMostrar;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getRequiereRecursos() {
        return requiereRecursos;
    }

    public void setRequiereRecursos(String requiereRecursos) {
        this.requiereRecursos = requiereRecursos;
    }

    public String getOcupaRecursos() {
        return ocupaRecursos;
    }

    public void setOcupaRecursos(String ocupaRecursos) {
        this.ocupaRecursos = ocupaRecursos;
    }

    public String getEsFinal() {
        return esFinal;
    }

    public void setEsFinal(String esFinal) {
        this.esFinal = esFinal;
    }
}

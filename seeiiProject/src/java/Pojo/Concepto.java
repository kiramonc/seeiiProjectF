package Pojo;
// Generated 18/01/2016 09:37:35 PM by Hibernate Tools 3.6.0

import java.util.HashSet;
import java.util.Set;

/**
 * Concepto generated by hbm2java
 */
public class Concepto  implements java.io.Serializable {

    private int idConcepto;
    private Tema tema;
    private String nombreConcepto;
    private String traduccion;
    private String descripcion;
    private boolean estado;
    private Set pregConcs = new HashSet(0);
    private Set resultados = new HashSet(0);

    public Concepto() {
    }

    public Concepto(int idConcepto, Tema tema, String nombreConcepto, String traduccion, boolean estado) {
        this.idConcepto = idConcepto;
        this.tema = tema;
        this.nombreConcepto = nombreConcepto;
        this.traduccion = traduccion;
        this.estado = estado;
    }

    public Concepto(int idConcepto, Tema tema, String nombreConcepto, String traduccion, String descripcion, boolean estado, Set pregConcs, Set resultados) {
        this.idConcepto = idConcepto;
        this.tema = tema;
        this.nombreConcepto = nombreConcepto;
        this.traduccion = traduccion;
        this.descripcion = descripcion;
        this.estado = estado;
        this.pregConcs = pregConcs;
        this.resultados = resultados;
    }

    public int getIdConcepto() {
        return this.idConcepto;
    }

    public void setIdConcepto(int idConcepto) {
        this.idConcepto = idConcepto;
    }

    public Tema getTema() {
        return this.tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public String getNombreConcepto() {
        return this.nombreConcepto;
    }

    public void setNombreConcepto(String nombreConcepto) {
        this.nombreConcepto = nombreConcepto;
    }

    public String getTraduccion() {
        return this.traduccion;
    }

    public void setTraduccion(String traduccion) {
        this.traduccion = traduccion;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEstado() {
        return this.estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public Set getPregConcs() {
        return this.pregConcs;
    }

    public void setPregConcs(Set pregConcs) {
        this.pregConcs = pregConcs;
    }

    public Set getResultados() {
        return this.resultados;
    }

    public void setResultados(Set resultados) {
        this.resultados = resultados;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (nombreConcepto != null ? nombreConcepto.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Concepto)) {
            return false;
        }
        Concepto other = (Concepto) object;
        if ((this.nombreConcepto == null && other.nombreConcepto != null) || (this.nombreConcepto != null && !this.nombreConcepto.equals(other.nombreConcepto))) {
            return false;
        }
        return true;
    }
}



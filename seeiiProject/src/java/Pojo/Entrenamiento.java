package Pojo;
// Generated 18/01/2016 09:37:35 PM by Hibernate Tools 3.6.0


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Entrenamiento generated by hbm2java
 */
public class Entrenamiento  implements java.io.Serializable {


     private int idEntrena;
     private Tema tema;
     private Estudiante estudiante;
     private int error;
     private int tiempo;
     private int puntaje;
     private Date fecha;
     private Set preguntaentrenars = new HashSet(0);

    public Entrenamiento() {
    }

	
    public Entrenamiento(int idEntrena, Tema tema, Estudiante estudiante, int error, int tiempo, int puntaje, Date fecha) {
        this.idEntrena = idEntrena;
        this.tema = tema;
        this.estudiante = estudiante;
        this.error = error;
        this.tiempo = tiempo;
        this.puntaje = puntaje;
        this.fecha = fecha;
    }
    public Entrenamiento(int idEntrena, Tema tema, Estudiante estudiante, int error, int tiempo, int puntaje, Date fecha, Set preguntaentrenars) {
       this.idEntrena = idEntrena;
       this.tema = tema;
       this.estudiante = estudiante;
       this.error = error;
       this.tiempo = tiempo;
       this.puntaje = puntaje;
       this.fecha = fecha;
       this.preguntaentrenars = preguntaentrenars;
    }
   
    public int getIdEntrena() {
        return this.idEntrena;
    }
    
    public void setIdEntrena(int idEntrena) {
        this.idEntrena = idEntrena;
    }
    public Tema getTema() {
        return this.tema;
    }
    
    public void setTema(Tema tema) {
        this.tema = tema;
    }
    public Estudiante getEstudiante() {
        return this.estudiante;
    }
    
    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }
    public int getError() {
        return this.error;
    }
    
    public void setError(int error) {
        this.error = error;
    }
    public int getTiempo() {
        return this.tiempo;
    }
    
    public void setTiempo(int tiempo) {
        this.tiempo = tiempo;
    }
    public int getPuntaje() {
        return this.puntaje;
    }
    
    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }
    public Date getFecha() {
        return this.fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    public Set getPreguntaentrenars() {
        return this.preguntaentrenars;
    }
    
    public void setPreguntaentrenars(Set preguntaentrenars) {
        this.preguntaentrenars = preguntaentrenars;
    }




}



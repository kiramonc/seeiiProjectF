package Pojo;
// Generated 19/11/2015 09:00:12 PM by Hibernate Tools 3.6.0



/**
 * Fichaspregunta generated by hbm2java
 */
public class Fichaspregunta  implements java.io.Serializable {


     private int idFichaInt;
     private Ficha ficha;
     private Preguntaentrenar preguntaentrenar;

    public Fichaspregunta() {
    }

    public Fichaspregunta(int idFichaInt, Ficha ficha, Preguntaentrenar preguntaentrenar) {
       this.idFichaInt = idFichaInt;
       this.ficha = ficha;
       this.preguntaentrenar = preguntaentrenar;
    }
   
    public int getIdFichaInt() {
        return this.idFichaInt;
    }
    
    public void setIdFichaInt(int idFichaInt) {
        this.idFichaInt = idFichaInt;
    }
    public Ficha getFicha() {
        return this.ficha;
    }
    
    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }
    public Preguntaentrenar getPreguntaentrenar() {
        return this.preguntaentrenar;
    }
    
    public void setPreguntaentrenar(Preguntaentrenar preguntaentrenar) {
        this.preguntaentrenar = preguntaentrenar;
    }




}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import BeanSession.BeanSEntrenar;
import Clases.RedNeuronal.RedNeuronal;
import Dao.DaoEntrenar;
import HibernateUtil.HibernateUtil;
import Pojo.Entrenamiento;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;

/**
 *
 * @author USUARIO
 */
@ManagedBean
@RequestScoped
public class BeansVerResultadoEntrenar {

    private Session session;
    private Transaction transaction;

    //llamar al bean de sesion 
    @ManagedProperty("#{beanSEntrenar}")
    private BeanSEntrenar beanSEntrena;

    private Entrenamiento entrenamiento;

    private String resultado;
    private String imagenResult;

    public BeansVerResultadoEntrenar() {

    }

    public void generarResultado() throws Exception {
        entrenamiento = obtnerEntrenamiento();
        RedNeuronal redN = new RedNeuronal();
        redN.redNeuronal(entrenamiento.getPuntaje(), entrenamiento.getTiempo(), entrenamiento.getError());
        resultado = redN.getResultado();
        imagenResult = redN.getImgResultado();

    }

    public Entrenamiento obtnerEntrenamiento() {
        this.session = null;
        this.transaction = null;
        Entrenamiento entrenamiento = new Entrenamiento();

        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //para obtener un entrenamiento por su id
            DaoEntrenar daoEntrenar = new DaoEntrenar();//verPorCodigoEntrenamiento
            entrenamiento = daoEntrenar.verPorCodigoEntrenamiento(session, this.beanSEntrena.getIdEntrenamiento());
            this.transaction.commit();

            System.out.println("Correcto: Al Obtner el Entrenamiento con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL Obtner el Entrenamiento:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
        return entrenamiento;
    }

    public void mostrarMsj() {
       RequestContext.getCurrentInstance().update("frmResultado:panelResultado");
        RequestContext.getCurrentInstance().execute("PF('dialogResultado').show()");
    }

    public String actualizarPagina() {
//        this.beanSEntrena.finalizar();
        return "inicioAprendizaje";
    }

    //.................setter y getter.....................
    public Entrenamiento getEntrenamiento() {
        return entrenamiento;
    }

    public String getImagenResult() {
        return imagenResult;
    }

    public String getResultado() {
        return resultado;
    }

    public BeanSEntrenar getBeanSEntrena() {
        return beanSEntrena;
    }

    public void setBeanSEntrena(BeanSEntrenar beanSEntrena) {
        this.beanSEntrena = beanSEntrena;
    }

}

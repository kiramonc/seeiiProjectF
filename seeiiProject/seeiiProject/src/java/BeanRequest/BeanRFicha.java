/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Dao.DaoFicha;
import Dao.DaoTema;
import HibernateUtil.HibernateUtil;
import Pojo.Ficha;
import Pojo.Tema;
import Pojo.Unidadensenianza;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;

/**
 *
 * @author silvy
 */
@ManagedBean
@ViewScoped
public class BeanRFicha {

    private Ficha ficha;
    private List<Ficha> listaFichas;
    private Tema tema;
    private List<Tema> listaTemas;
    private List<Tema> listaTemasfiltrado;
    private String unidad = "";

    private Session session;
    private Transaction transaction;

    public BeanRFicha() {
    }

    public void registrar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoFicha daoficha = new DaoFicha();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.ficha.setEstado(true);
            this.ficha.setEstadoAprendizaje("No aprendido");
            daoficha.registrar(session, ficha);
            this.transaction.commit();

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "El registro fue realizado con éxito"));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR:", "Contacte con el administrador, problemas al crear la ficha" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
//    método para abrir el dialogo para crear una ficha.

    public void abrirDialogoCrearFicha() {
        try {
            this.ficha = new Ficha();
            RequestContext.getCurrentInstance().update("frmCrearFicha:panelCrearFicha");
            RequestContext.getCurrentInstance().execute("PF('dialogCrearFicha').show()");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR PREGUNTA CREAR:", "Contacte con el administrador" + ex.getMessage()));
        }
    }

    public List<Ficha> getAllFichas() {
        this.session = null;
        this.transaction = null;
        try {
            DaoFicha daoficha = new DaoFicha();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.listaFichas = daoficha.verTodo(session);
            this.transaction.commit();
            return this.listaFichas;

        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR:", "Contacte con el administrador" + ex.getMessage()));
            return null;
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
    
    public List<Tema> getAllTemas() {
        this.session = null;
        this.transaction = null;
        try {
            DaoTema daoTema = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.listaTemas = daoTema.verTodo(session);
            this.transaction.commit();
            return this.listaTemas;

        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR:", "Contacte con el administrador" + ex.getMessage()));
            return null;
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
    
//metodo para fijar la unidad al elegir un tema

    public void onChangeSelect2() {
//        unidad = "unidad2";
//        System.out.println("aSDFGHGFDSA" + nombre);
        System.out.println("cambiar de valor");
    }

    public Ficha getFicha() {
        return ficha;
    }

    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }

    public List<Ficha> getListaFichas() {
        return listaFichas;
    }

    public void setListaFichas(List<Ficha> listaFichas) {
        this.listaFichas = listaFichas;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public List<Tema> getListaTemas() throws Exception {
//        DaoTema daoTemas = new DaoTema();
//        List<Tema> temas = daoTemas.verTodo();
//        listaTemas = temas;
        return listaTemas;
    }

    public void setListaTemas(List<Tema> listaTemas) {
        this.listaTemas = listaTemas;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public List<Tema> getListaTemasfiltrado() {
        return listaTemasfiltrado;
    }

    public void setListaTemasfiltrado(List<Tema> listaTemasfiltrado) {
        this.listaTemasfiltrado = listaTemasfiltrado;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Dao.DaoRol;
import Dao.DaoTipoPregunta;

import HibernateUtil.HibernateUtil;
import Pojo.Tipopregunta;

import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author KathyR
 */
@ManagedBean
@RequestScoped
public class BeanRTipoPregunta {

    /**
     * Creates a new instance of BeanRUsuario
     */
    private Tipopregunta tipoPreg;
    private List<Tipopregunta> listaTipos;
    private Session session;
    private Transaction transaction;

    public BeanRTipoPregunta() {
        
    }
    
    // Recuperar un determinado Rol
    public Tipopregunta consultarTipoPorNombre(String tipo){
        try{
            DaoTipoPregunta daoRol=new DaoTipoPregunta();
            this.session=HibernateUtil.getSessionFactory().openSession();
            this.transaction=session.beginTransaction();
            Tipopregunta r=daoRol.verPorNombreTipo(session, tipo);
            transaction.commit();
            return r;
            
        }catch (Exception ex) {
            if(this.transaction!=null){
                this.transaction.rollback();
            }
            return null;
        }finally{
            if(this.session!=null){
                this.session.close();
            }
        }
    }

    public Tipopregunta getTipoPreg() {
        return tipoPreg;
    }

    public void setTipoPreg(Tipopregunta tipoPreg) {
        this.tipoPreg = tipoPreg;
    }

    public List<Tipopregunta> getListaTipos() {
        return listaTipos;
    }

    public void setListaTipos(List<Tipopregunta> listaTipos) {
        this.listaTipos = listaTipos;
    }
    
   
}

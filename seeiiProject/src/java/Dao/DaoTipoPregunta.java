/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Dao;

import HibernateUtil.HibernateUtil;
import Pojo.Rol;
import Pojo.Tipopregunta;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author KathyR
 */
public class DaoTipoPregunta{

    public boolean registrar(Session session, Tipopregunta tipoPreg) throws Exception {
        session.save(tipoPreg);
        return true;
    }

    public List<Tipopregunta> verTodo() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Criteria criteria=session.createCriteria(Tipopregunta.class);
//        String hql="from Rol";
//        Query query=session.createQuery(hql);
        List<Tipopregunta> listaRol=(List<Tipopregunta>) criteria.list();
        session.close();
        return listaRol;
    }

    public Tipopregunta verPorNombreTipo(Session session, String nombreTipo) throws Exception {
        String hql="from Tipopregunta where nombreTipo=:nombreTipo";
        Query query=session.createQuery(hql);
        query.setParameter("nombreTipo", nombreTipo);
        Tipopregunta rol=(Tipopregunta) query.uniqueResult();
        return rol;
        
    }
    
    public Tipopregunta verPorID(Session session, int idTipo) throws Exception {
        String hql="from Tipopregunta where idTipo=:idTipo";
        Query query=session.createQuery(hql);
        query.setParameter("idTipo", idTipo);
        Tipopregunta rol=(Tipopregunta) query.uniqueResult();
        return rol;
        
    }

    public boolean actualizar(Session session, Tipopregunta tipoPreg) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}

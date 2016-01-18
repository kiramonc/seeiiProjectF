/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Dao;

import Interface.InterfaceFicha;
import Pojo.Ficha;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author silvy
 */
public class DaoFicha implements InterfaceFicha{

    @Override
    public boolean registrar(Session session, Ficha ficha) throws Exception {
        session.save(ficha);
        return true;
    }
    
    @Override
    public List<Ficha> verTodo(Session session) throws Exception {
        String hql = "from Ficha";
        Query query = session.createQuery(hql);
        List<Ficha> listaficha = (List<Ficha>) query.list();
        for (Ficha lista : listaficha) {
            Hibernate.initialize(lista.getTema());
            Hibernate.initialize(lista.getTema().getUnidadensenianza());
        }
        return listaficha;
    }
    
    @Override
    public Ficha verPorCodigoFicha(Session session, int idFicha) throws Exception {
        String hql="from Ficha where idFicha=:idFicha";
        Query query=session.createQuery(hql);
        query.setParameter("idFicha", idFicha);
        Ficha ficha=(Ficha) query.uniqueResult();
        return ficha;
    }
    @Override
    public List<Ficha> verListfichasPorTema(Session session, int idTema) throws Exception {
        String hql = "from Ficha where temaFicha=:temaFicha"; 
        Query query = session.createQuery(hql);
        query.setInteger("temaFicha", idTema);
        List<Ficha> listaFichasPreguntas = (List<Ficha>) query.list();
        for (Ficha lista : listaFichasPreguntas) {
            Hibernate.initialize(lista.getTema());
        }
        return listaFichasPreguntas;
    }
    
}

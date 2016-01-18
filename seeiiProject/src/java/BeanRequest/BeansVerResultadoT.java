/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Dao.DaoEntrenar;
import Dao.DaoEstudiante;
import Dao.DaoUsuario;
import HibernateUtil.HibernateUtil;
import Pojo.Entrenamiento;
import Pojo.Estudiante;
import Pojo.Usuario;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author USUARIO
 */
@ManagedBean
@RequestScoped
public class BeansVerResultadoT {

    private Session session;
    private Transaction transaction;

    private List<Entrenamiento> listaEntrenamiento;
    private CartesianChartModel modelGraphTemas;

    public BeansVerResultadoT() {

    }

    @PostConstruct
    public void init() {
        createLineModels();
    }

    private void createLineModels() {
        ObtnerDatosEst();
        modelGraphTemas = new CartesianChartModel();

        ChartSeries temaSeries = new ChartSeries();
        temaSeries.setLabel("Puntajes del Entrenamiento");
        int tamaño = 0;
        int anio;
        int mes;
        String fecha;
        int posicion;
        System.out.println(" el tamano de la lista es_"+listaEntrenamiento.size());
        if (listaEntrenamiento.isEmpty()) {
            temaSeries.set("Sin Fecha de Entrenamiento", 0);
            modelGraphTemas.addSeries(temaSeries);
        } else {
            tamaño = listaEntrenamiento.size();
            if (tamaño <= 7) {
                for (int i = 0; i < tamaño; i++) {
                    anio = listaEntrenamiento.get(i).getFecha().getYear() + 1900;
                    mes = listaEntrenamiento.get(i).getFecha().getMonth() + 1;
                    fecha = "[" + i + "] " + anio + "/" + mes + "/" + listaEntrenamiento.get(i).getFecha().getDate();
                    temaSeries.set(fecha, listaEntrenamiento.get(i).getPuntaje());
                }
                modelGraphTemas.addSeries(temaSeries);
            } else {
                for (int i = 0; i < 7; i++) {
                    posicion = tamaño - i;
                    anio = listaEntrenamiento.get(tamaño).getFecha().getYear() + 1900;
                    mes = listaEntrenamiento.get(tamaño).getFecha().getMonth() + 1;
                    fecha = "[" + i + "] " + anio + "/" + mes + "/" + listaEntrenamiento.get(tamaño).getFecha().getDate();
                    temaSeries.set(fecha, listaEntrenamiento.get(tamaño).getPuntaje());
                }
                modelGraphTemas.addSeries(temaSeries);
            }
        }
    }

    //metodo para obtner las de todos los entrenamiento de un estudiante
    public void ObtnerDatosEst() {
        //obtnego el beansSession LOGIN para obtener el nombre del ususario.
        FacesContext context = javax.faces.context.FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String nameUser = (String) session.getAttribute("usernameLogin");

        int idUsuario = verUsuarioPorUSERNAME(nameUser);
        //metodo para obtner una lista de Entrenamiento de un usuario
        verListaEntrenamientos(idUsuario); //inicializa el atrinuto(listaEntrenamiento)
        System.out.println("el nombre de usuario es :" + nameUser);
        System.out.println("el id del Estudiante " + nameUser + " es :" + idUsuario);
        System.out.println("la tamaño de lista de entrenamientos es: " + listaEntrenamiento.size());
    }

    //metodo para obtner un Usuario segun el USERNAME.
    public int verUsuarioPorUSERNAME(String nameUser) {
        this.session = null;
        this.transaction = null;
        int idUsuario = 0;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //para obtener su id de usuario segun el NameUser
            DaoUsuario daoUsuario = new DaoUsuario();
            Usuario usuario = daoUsuario.verPorUsername(session, nameUser);
            //Obtnemos el id del estudiante segun el [idUsuario]
            DaoEstudiante daoEstudiante = new DaoEstudiante();
            Estudiante estudiante = daoEstudiante.verPorCodigoUsuario(session, usuario.getIdUsuario());//obtuvimos el estudiante  segun el id del usuario

            idUsuario = estudiante.getIdEst();
            this.transaction.commit();

            System.out.println("Correcto: Al Obtner un Usuario con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL Obtner un Usuario:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
        return idUsuario;
    }

    //metodo para obtner una lista de Entrenamiento de un usuario
    public void verListaEntrenamientos(int idEstudiante) {
        this.session = null;
        this.transaction = null;

        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //para obtener un entrenamiento por su id
            DaoEntrenar daoEntrenar = new DaoEntrenar();
            listaEntrenamiento = daoEntrenar.listEntrenamientoPorIdEstudiante(session, idEstudiante);
            this.transaction.commit();

            System.out.println("Correcto: Al Obtner la lsiata de Entrenamientos con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL Obtner la lista de Entrenamientos:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    //.................setter y getter.....................
    public CartesianChartModel getModelGraphTemas() {
        return modelGraphTemas;
    }

    public void setModelGraphTemas(CartesianChartModel modelGraphTemas) {
        this.modelGraphTemas = modelGraphTemas;
    }

    public List<Entrenamiento> getListaEntrenamiento() {
        return listaEntrenamiento;
    }

    public void setListaEntrenamiento(List<Entrenamiento> listaEntrenamiento) {
        this.listaEntrenamiento = listaEntrenamiento;
    }

}

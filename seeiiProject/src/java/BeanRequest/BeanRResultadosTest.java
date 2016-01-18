/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Clases.RedBayesiana.CrearBayesNetwork1;
import Dao.DaoEstudiante;
import Dao.DaoResultado;
import HibernateUtil.HibernateUtil;
import Pojo.Estudiante;
import Pojo.Resultado;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmarkov.core.model.network.Util;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author KathyR
 */
@ManagedBean
@ViewScoped
public class BeanRResultadosTest {

    private Session session;
    private Transaction transaction;
    private String usuarioLogeado;
    private int codigoEstudiante;
    private List<String> lstResultados;
    private CartesianChartModel modelGraphTemas;
    private PieChartModel modelGraphUnidad;
    private boolean mostrarGraphs;
    private String imagenResultado;

    public BeanRResultadosTest() {
        HttpSession sesionUsuario = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        this.usuarioLogeado = sesionUsuario.getAttribute("usernameLogin").toString();
        this.lstResultados = new ArrayList<>();
        modelGraphTemas = new CartesianChartModel();
        mostrarGraphs = false;
    }

    public void inferenciaRed() {
        this.session = null;
        this.transaction = null;
        this.lstResultados = new ArrayList<>();
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            // consultar valores de la BD durante la primera pregunta
            DaoResultado daoResultado = new DaoResultado();
            Estudiante e = new DaoEstudiante().verPorUsername(session, usuarioLogeado);
            this.codigoEstudiante = e.getIdEst(); // id del estudiante
            List<Resultado> lstResultados1 = daoResultado.verPorEstudianteUnidad(session, e.getUnidadensenianza().getId(), codigoEstudiante);
            System.out.println("Resultados BD: "+ lstResultados1.toString());
            CrearBayesNetwork1 cbne = new CrearBayesNetwork1();
            HashMap<String, String> valueInf = cbne.inferencia(usuarioLogeado, e.getUnidadensenianza().getNombreUnidad(), lstResultados1);
            for (Map.Entry<String, String> entry : valueInf.entrySet()) {
                this.lstResultados.add(entry.getKey() + ": " + (Util.roundedString(Double.parseDouble(entry.getValue())*100.00, "0.001"))+"%");
            }
            for (int i = 0; i < lstResultados1.size(); i++) {
                System.out.println(""+i+": "+lstResultados1.get(i).getConcepto().getNombreConcepto()+" "+lstResultados1.get(i).getValor());
            }
            

            iniciarGraficoTemas(valueInf);
            iniciarGraficoUnidad(valueInf);
            transaction.commit();
            RequestContext.getCurrentInstance().update("frmMostrarResultados:panelMostrarResultados");

        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al calcular el aprendizaje:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        modelGraphTemas = new CartesianChartModel();
        ChartSeries temaSeries = new ChartSeries();
        temaSeries.setLabel("UNIDAD");
        temaSeries.set("TEMA", 100);
        temaSeries.set("TEMA2", 50);
        temaSeries.set("TEMA3", 150);
        modelGraphTemas.addSeries(temaSeries);

        modelGraphUnidad = new PieChartModel();
        modelGraphUnidad.set("TEMA", 100);
        modelGraphUnidad.set("TEMA2", 50);
        modelGraphUnidad.set("TEMA3", 150);
    }

    private void iniciarGraficoTemas(HashMap<String, String> valoresInferencia) {
        modelGraphTemas = new CartesianChartModel();
        ChartSeries temaSeries = new ChartSeries();
        int contador = 0;
        for (Map.Entry<String, String> entry : valoresInferencia.entrySet()) {
            contador++;
            if (contador != valoresInferencia.size()) {
                temaSeries.setLabel(entry.getKey());
                temaSeries.set(entry.getKey(), Double.parseDouble(entry.getValue()) * 100);
                modelGraphTemas.addSeries(temaSeries);
                temaSeries = new ChartSeries();
            }
        }
        this.mostrarGraphs = true;
    }

    private void iniciarGraficoUnidad(HashMap<String, String> valoresInferencia) {
        modelGraphUnidad = new PieChartModel();
        int contador = 0;
        for (Map.Entry<String, String> entry : valoresInferencia.entrySet()) {
            contador++;
            if (contador == valoresInferencia.size()) {
                modelGraphUnidad.set("Conoce", Double.parseDouble(entry.getValue()) * 100);
                modelGraphUnidad.set("No conoce", 100 - (Double.parseDouble(entry.getValue()) * 100));
            }
        }
        this.mostrarGraphs = true;
    }
    
    public String actualizarPagina() {
        return "test";
    }
    
    public String calificacion(){
        double conoce= modelGraphUnidad.getData().get("Conoce").doubleValue();
        String mensaje="";
        if(conoce<=50){
            mensaje= "You can improve";
            this.imagenResultado= "Improve.png";
            return mensaje;
        }else if(conoce<=80){
            mensaje= "Very good!";
            this.imagenResultado= "VeryGood.png";
            return mensaje;
        } else{
            mensaje= "Congratulations!";
            this.imagenResultado= "Excelent.png";
            return mensaje;
        }
    }

    public String getDatatipFormat() {
        return "<span>Conocimiento: %2$d</span>";
    }

    public List<String> getLstResultados() {
        return lstResultados;
    }

    public void setLstResultados(List<String> lstResultados) {
        this.lstResultados = lstResultados;
    }

    public CartesianChartModel getModelGraphTemas() {
        return modelGraphTemas;
    }

    public void setModelGraphTemas(CartesianChartModel modelGraphTemas) {
        this.modelGraphTemas = modelGraphTemas;
    }

    public PieChartModel getModelGraphUnidad() {
        return modelGraphUnidad;
    }

    public void setModelGraphUnidad(PieChartModel modelGraphUnidad) {
        this.modelGraphUnidad = modelGraphUnidad;
    }

    public boolean isMostrarGraphs() {
        return mostrarGraphs;
    }

    public void setMostrarGraphs(boolean mostrarGraphs) {
        this.mostrarGraphs = mostrarGraphs;
    }

    public String getImagenResultado() {
        return imagenResultado;
    }

    public void setImagenResultado(String imagenResultado) {
        this.imagenResultado = imagenResultado;
    }
    
}

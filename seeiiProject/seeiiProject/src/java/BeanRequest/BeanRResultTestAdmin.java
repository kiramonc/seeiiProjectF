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
public class BeanRResultTestAdmin {

    private Session session;
    private Transaction transaction;
    private Estudiante estudianteSeleccionado;
    private List<Resultado> lstResultadosConceptos;
    private List<Estudiante> listaEstudiantesPorUnidad;
    private List<Estudiante> listaEstudiantesFiltrada;
    private CartesianChartModel modelGraphTemas;
    private CartesianChartModel modelGraphConcepto;
    private boolean mostrarGraphs;
    private String valorUnidad;
    
    
    public BeanRResultTestAdmin() {
        modelGraphTemas = new CartesianChartModel();
        modelGraphConcepto = new CartesianChartModel();
        mostrarGraphs = false;
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

        modelGraphConcepto = new CartesianChartModel();
        modelGraphConcepto.addSeries(temaSeries);
    }

    public void inferenciaRed(Estudiante e) {
        this.estudianteSeleccionado=e;
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            // consultar valores de la BD durante la primera pregunta
            DaoResultado daoResultado = new DaoResultado();
            this.lstResultadosConceptos = daoResultado.verPorEstudianteUnidad(session, estudianteSeleccionado.getUnidadensenianza().getId(), estudianteSeleccionado.getIdEst());
            
            CrearBayesNetwork1 cbne = new CrearBayesNetwork1();
            HashMap<String, String> valueInf = cbne.inferencia(estudianteSeleccionado.getUsuario().getUsername(), estudianteSeleccionado.getUnidadensenianza().getNombreUnidad(), lstResultadosConceptos);
            this.valorUnidad= (Util.roundedString(Double.parseDouble(valueInf.get("Unidad Básica"))*100.00, "0.001"))+"%";
            
            iniciarGraficoTemas(valueInf);
            iniciarGraficoConceptos();
            transaction.commit();
            RequestContext.getCurrentInstance().update("frmGraficasPorEstudiante");
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
    
    private void iniciarGraficoTemas(HashMap<String, String> valoresInferencia) {
        modelGraphTemas = new CartesianChartModel();
        ChartSeries temaSeries = new ChartSeries();
        temaSeries.setLabel("Temas");
        int contador = 0;
        for (Map.Entry<String, String> entry : valoresInferencia.entrySet()) {
            contador++;
            if (contador != valoresInferencia.size()) {
                temaSeries.set(entry.getKey(), Double.parseDouble(entry.getValue()) * 100);
            }
        }
        modelGraphTemas.addSeries(temaSeries);
        this.mostrarGraphs = true;
    }

    private void iniciarGraficoConceptos() {
        modelGraphConcepto = new CartesianChartModel();
        ChartSeries temaSeries = new ChartSeries();
        temaSeries.setLabel("Conceptos");
        for (int i = 0; i < lstResultadosConceptos.size(); i++) {
            temaSeries.set(lstResultadosConceptos.get(i).getConcepto().getNombreConcepto(), lstResultadosConceptos.get(i).getValor()*100);
        }
        modelGraphConcepto.addSeries(temaSeries);
        this.mostrarGraphs = true;
    }
    
    public List<Estudiante> getListaEstudiantesPorUnidad() {
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            DaoEstudiante daoEstudiante= new DaoEstudiante();
            this.listaEstudiantesPorUnidad = daoEstudiante.verPorUnidadEnsenianza(session, "Unidad Básica");
            this.transaction.commit();
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al obtener lista de estudiantes por unidad:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
        return listaEstudiantesPorUnidad;
    }
    
    public void setListaEstudiantesPorUnidad(List<Estudiante> listaEstudiantesPorUnidad) {
        this.listaEstudiantesPorUnidad = listaEstudiantesPorUnidad;
    }
    
    public Estudiante getEstudianteSeleccionado() {
        return estudianteSeleccionado;
    }

    public void setEstudianteSeleccionado(Estudiante estudianteSeleccionado) {
        this.estudianteSeleccionado = estudianteSeleccionado;
    }

    public CartesianChartModel getModelGraphTemas() {
        return modelGraphTemas;
    }

    public void setModelGraphTemas(CartesianChartModel modelGraphTemas) {
        this.modelGraphTemas = modelGraphTemas;
    }

    public CartesianChartModel getModelGraphConcepto() {
        return modelGraphConcepto;
    }

    public void setModelGraphConcepto(CartesianChartModel modelGraphConcepto) {
        this.modelGraphConcepto = modelGraphConcepto;
    }

    public boolean isMostrarGraphs() {
        return mostrarGraphs;
    }

    public void setMostrarGraphs(boolean mostrarGraphs) {
        this.mostrarGraphs = mostrarGraphs;
    }

    public List<Estudiante> getListaEstudiantesFiltrada() {
        return listaEstudiantesFiltrada;
    }

    public void setListaEstudiantesFiltrada(List<Estudiante> listaEstudiantesFiltrada) {
        this.listaEstudiantesFiltrada = listaEstudiantesFiltrada;
    }

    public String getValorUnidad() {
        return valorUnidad;
    }

    public void setValorUnidad(String valorUnidad) {
        this.valorUnidad = valorUnidad;
    }
    
}

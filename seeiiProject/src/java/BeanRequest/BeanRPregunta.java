/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Clases.RedBayesiana.CrearBayesDynamic;
import Clases.RedBayesiana.CrearBayesNetwork1;
import Dao.DaoConcepto;
import Dao.DaoItem;
import Dao.DaoPregConc;
import Dao.DaoPregunta;
import Dao.DaoTema;
import Dao.DaoUnidadE;
import HibernateUtil.HibernateUtil;
import Pojo.Concepto;
import Pojo.Item;
import Pojo.PregConc;
import Pojo.Pregunta;
import Pojo.Tema;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DualListModel;
import org.primefaces.model.UploadedFile;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author KathyR
 */
@ManagedBean
@ViewScoped
public class BeanRPregunta {

    private Pregunta pregunta;
    private Tema tema = new Tema();
    private List<Pregunta> listaPreguntas;
    private Session session;
    private Transaction transaction;
    private HashMap<Integer, List<Concepto>> pregConc;
    private String tipoPreg;
    private Concepto concepto;
    private List<Concepto> target = new ArrayList<>();
    private DualListModel<Concepto> modelConc = new DualListModel<Concepto>();
    private UploadedFile csvFile;
    private String nombreArchivo;
    private int idPregunta;

    //constructor
    public BeanRPregunta() {
        this.pregunta = new Pregunta();
    }

    public void registrar(Tema tema) {
        List<Concepto> listaC = modelConc.getTarget();
        if (listaC.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR ACTUALIZAR:", "La pregunta tiene que hacer referencia almenos a un concepto"));
            return;
        }
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.pregunta.setEstado(true);
            this.pregunta.setNombrePreg("NombrePreg");
            daoPregunta.registrar(this.session, this.pregunta);
            Pregunta preg = daoPregunta.verPorCodigoPregunta(session, daoPregunta.verUltimoRegistro(session));

            //Registrar los conceptos de la pregunta
            DaoPregConc daoPregConc = new DaoPregConc();
            List<PregConc> lstPregConc = new ArrayList<>();
            PregConc pregConc = new PregConc();

            for (int i = 0; i < listaC.size(); i++) {
                pregConc.setPregunta(preg);
                pregConc.setConcepto(listaC.get(i));
                lstPregConc.add(pregConc);
                pregConc = new PregConc();
            }
            daoPregConc.registrarVarios(session, lstPregConc);
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "El registro fue realizado con éxito"));
            this.pregunta = new Pregunta();
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (this.session.isOpen()) {
                    this.session.close();
                }
            }
        }
    }

    public void actualizar() {
        DaoPregConc daoPregConc = new DaoPregConc();
        DaoConcepto daoConcepto = new DaoConcepto();
        List<Concepto> listaC = modelConc.getTarget();
        if (listaC.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR ACTUALIZAR:", "La pregunta tiene que hacer referencia almenos a un concepto"));
            return;
        }

        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            daoPregunta.actualizar(this.session, this.pregunta); // Actualizar datos de la pregunta
            this.transaction.commit();
            this.session.close();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Pregunta preg = daoPregunta.verPorCodigoPregunta(session, this.pregunta.getIdPregunta());

            // Actualizar registros en tabla PregConc
            List<PregConc> pregConcs = new ArrayList<>();
            PregConc pregConc = new PregConc();
            boolean banderaExiste = false; // false= el concepto no existe en la bd
            boolean banderaMantiene = false; // false= el concepto fue eliminado
            // Recorro lista conceptos nueva para ver si algún concepto fue agregado
            for (int i = 0; i < listaC.size(); i++) {
                for (int j = 0; j < target.size(); j++) {
                    if (listaC.get(i).getNombreConcepto().equals(target.get(j).getNombreConcepto())) {
                        banderaExiste = true; // existe el concepto en la bd
                    }
                }
                if (!banderaExiste) { // el concepto i es nuevo
                    pregConc.setPregunta(preg);
                    pregConc.setConcepto(daoConcepto.verPorNombreConcepto(session, listaC.get(i).getNombreConcepto()));
                    pregConcs.add(pregConc);
                    pregConc = new PregConc();
                }
                banderaExiste = false;
            }
            if (!pregConcs.isEmpty()) {
                daoPregConc.registrarVarios(session, pregConcs);
                this.transaction.commit();
                this.session.close();

            }

            for (int i = 0; i < target.size(); i++) { // recorro lista de conceptos anterior para ver si un concepto fue eliminiado
                for (int j = 0; j < listaC.size(); j++) {
                    if (target.get(i).getNombreConcepto().equals(listaC.get(j).getNombreConcepto())) {
                        banderaMantiene = true;
                    }
                }
                if (!banderaMantiene) { // el concepto i fue eliminado
                    this.session = HibernateUtil.getSessionFactory().openSession();
                    this.transaction = session.beginTransaction();
                    daoPregConc.eliminar(session, preg, daoConcepto.verPorNombreConcepto(session, target.get(i).getNombreConcepto()));
                    this.transaction.commit();
                    this.session.close();
                }
                banderaMantiene = false;
            }
            // Editar nodo pregunta en la red bayesiana
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            DaoItem daoItem = new DaoItem();
            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            rbDynamic.editarPregunta(listaC.get(0).getTema().getNombre(), preg, listaC, daoItem.verNumItemsPorPregunta(session, preg.getIdPregunta()));

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
            this.pregunta = new Pregunta();
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (this.transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR ACTUALIZAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (this.session.isOpen()) {
                    this.session.close();
                }
            }
        }
    }

    public void eliminar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            daoPregunta.eliminar(this.session, this.pregunta);

            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            rbDynamic.eliminarPregunta(target.get(0).getTema().getNombre(), pregunta);
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Pregunta eliminada correctamente."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL ELIMINAR:", "Contacte con el administrador, " + ex));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public List<Pregunta> getPreguntasPorTest(Tema tema) {
        this.tema = tema;
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            List<PregConc> t = daoPregunta.verPorTest(session, tema.getIdTema());

            this.listaPreguntas = new ArrayList<>(); // Lista de preguntas
            this.pregConc = new HashMap<>(); // Pregunta con la lista de conceptos
            List<Concepto> lstConceptos = new ArrayList<>();
            for (int i = 0; i < t.size(); i++) {
                if (!listaPreguntas.isEmpty()) {
                    if (!listaPreguntas.contains(t.get(i).getPregunta())) {
                        listaPreguntas.add(t.get(i).getPregunta());
                    }
                } else {
                    listaPreguntas.add(t.get(i).getPregunta());
                }
                if (!pregConc.isEmpty()) {
                    if (!pregConc.containsKey(t.get(i).getPregunta().getIdPregunta())) {
                        lstConceptos.add(t.get(i).getConcepto());
                        pregConc.put(t.get(i).getPregunta().getIdPregunta(), lstConceptos);
                        lstConceptos.clear();
                    } else {
                        lstConceptos = pregConc.get(t.get(i).getPregunta().getIdPregunta());
                        lstConceptos.add(t.get(i).getConcepto());
                        pregConc.put(t.get(i).getPregunta().getIdPregunta(), lstConceptos);
                        lstConceptos.clear();
                    }
                } else {
                    lstConceptos.add(t.get(i).getConcepto());
                    pregConc.put(t.get(i).getPregunta().getIdPregunta(), lstConceptos);
                    lstConceptos.clear();
                }
            }
            transaction.commit();
            return listaPreguntas;
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            return null;
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public void abrirDialogoCrearPregunta(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            DaoTema daoTema = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.tema = daoTema.verPorCodigoTema(session, codigo);
            this.pregunta = new Pregunta();

            BeanRConcepto brC = new BeanRConcepto();
            List<Concepto> source = brC.getConceptosPorTema(tema);
            List<Concepto> target = new ArrayList<>();
            if (source.isEmpty()) {
                RequestContext.getCurrentInstance().update("frmCrearPregunta");
                RequestContext.getCurrentInstance().execute("PF('dialogSinConcepto').show()");
                return;
            }

            this.modelConc = new DualListModel<>(source, target);

            RequestContext.getCurrentInstance().update("frmCrearPregunta:panelCrearPregunta");
            RequestContext.getCurrentInstance().execute("PF('dialogCrearPregunta').show()");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR CREACIÓN DE PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
        }
    }

    public void abrirDialogoVerItems(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoItem = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.pregunta = daoItem.verPorCodigoPregunta(session, codigo);
            this.transaction.commit();
            this.target = new ArrayList<>();
            RequestContext.getCurrentInstance().update("frmVerItems:panelVerItems");
            RequestContext.getCurrentInstance().execute("PF('dialogVerItems').show()");
            
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR ITEM EDITAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public void cargarPreguntaEditar(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.pregunta = daoPregunta.verPorCodigoPregunta(session, codigo);

            RequestContext.getCurrentInstance().update("frmEditarPregunta:panelEditarPregunta");
            RequestContext.getCurrentInstance().execute("PF('dialogEditarPregunta').show()");
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR PREGUNTA EDITAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public List<Pregunta> getListaPreguntas() {
        return listaPreguntas;
    }

    public void setListaPreguntas(List<Pregunta> listaPreguntas) {
        this.listaPreguntas = listaPreguntas;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public HashMap<Integer, List<Concepto>> getPregConc() {
        return pregConc;
    }

    public void setPregConc(HashMap<Integer, List<Concepto>> pregConc) {
        this.pregConc = pregConc;
    }

    public DualListModel<Concepto> getModelConc() {
        if (pregunta != null && target.isEmpty()) {
            this.session = null;
            this.transaction = null;
            try {
                DaoPregConc daoPregConc = new DaoPregConc();
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();

                List<PregConc> t = daoPregConc.verPorPregunta(session, pregunta.getIdPregunta());
                List<Concepto> lstConceptos = new ArrayList<>();
                for (int i = 0; i < t.size(); i++) {
                    lstConceptos.add(t.get(i).getConcepto());
                }
                this.target = lstConceptos;
                BeanRConcepto brC = new BeanRConcepto();
                List<Concepto> source = brC.getConceptosPorTema(tema);
                for (int i = 0; i < target.size(); i++) {
                    for (int j = 0; j < source.size(); j++) {
                        if (target.get(i).getNombreConcepto().equals(source.get(j).getNombreConcepto())) {
                            source.remove(j);
                        }
                    }
                }
                this.modelConc = new DualListModel<>(source, target);

            } catch (Exception ex) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR CREACIÓN DE PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
            }
        }
        return modelConc;
    }

    // listener que inicializa el target (lista de conceptos) cuando se cambia de pregunta
    public void cargarPregunta() {
        this.target = new ArrayList<>();
    }

    public void setModelConc(DualListModel<Concepto> modelConc) {
        this.modelConc = modelConc;
    }

    public Concepto getConcepto() {
        return concepto;
    }

    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    public String outputLabel() {
        String tipoPregunta = getTipoPreg();
        if (null != tipoPregunta) {
            switch (tipoPregunta) {
                case "listenF":
                    return "Escuchar (Fácil)";
                case "listenM":
                    return "Escuchar (Medio)";
                case "speakF":
                    return "Hablar (Fácil)";
                case "speakM":
                    return "Hablar (Medio)";
                case "speakD":
                    return "Escuchar y Hablar (Difícil)";
            }
        }
        return "Error";
    }

    public String outputLabel(Pregunta pregunta) {
        String tipoPregunta = getTipoPreg(pregunta);
        if (null != tipoPregunta) {
            switch (tipoPregunta) {
                case "listenF":
                    return "Escuchar (Fácil)";
                case "listenM":
                    return "Escuchar (Medio)";
                case "speakF":
                    return "Hablar (Fácil)";
                case "speakM":
                    return "Hablar (Medio)";
                case "speakD":
                    return "Escuchar y Hablar (Difícil)";
            }
        }
        return "Error";
    }

    public String getTipoPreg() {
        if (pregunta != null) {
            if (this.pregunta.getDificultad() == 0.9 && this.pregunta.getFdescuido() == 0.22 && this.pregunta.getIndiceDis() == 1.2) {
                tipoPreg = "listenF";
            } else {
                if (this.pregunta.getDificultad() == 3.0 && this.pregunta.getFdescuido() == 0.25 && this.pregunta.getIndiceDis() == 2.2) {
                    tipoPreg = "listenM";
                } else {
                    if (this.pregunta.getDificultad() == 0.89 && this.pregunta.getFdescuido() == 0.22 && this.pregunta.getIndiceDis() == 1.2) {
                        tipoPreg = "speakF";
                    } else {
                        if (this.pregunta.getDificultad() == 3.0 && this.pregunta.getFdescuido() == 0.25 && this.pregunta.getIndiceDis() == 2.0) {
                            tipoPreg = "speakM";
                        } else {
                            if (this.pregunta.getDificultad() == 4.0 && this.pregunta.getFdescuido() == 0.24 && this.pregunta.getIndiceDis() == 2.0) {
                                tipoPreg = "speakD";
                            }
                        }
                    }
                }
            }
        }
        return tipoPreg;
    }

    public String getTipoPreg(Pregunta pregunta) {
        if (pregunta != null) {
            if (pregunta.getDificultad() == 0.9 && pregunta.getFdescuido() == 0.22 && pregunta.getIndiceDis() == 1.2) {
                tipoPreg = "listenF";
            } else {
                if (pregunta.getDificultad() == 3.0 && pregunta.getFdescuido() == 0.25 && pregunta.getIndiceDis() == 2.2) {
                    tipoPreg = "listenM";
                } else {
                    if (pregunta.getDificultad() == 0.89 && pregunta.getFdescuido() == 0.22 && pregunta.getIndiceDis() == 1.2) {
                        tipoPreg = "speakF";
                    } else {
                        if (pregunta.getDificultad() == 3.0 && pregunta.getFdescuido() == 0.25 && pregunta.getIndiceDis() == 2.0) {
                            tipoPreg = "speakM";
                        } else {
                            if (pregunta.getDificultad() == 4.0 && pregunta.getFdescuido() == 0.24 && pregunta.getIndiceDis() == 2.0) {
                                tipoPreg = "speakD";
                            }
                        }
                    }
                }
            }
        }
        return tipoPreg;
    }

    public void setTipoPreg(String tipoPreg) {
        if (tipoPreg.equalsIgnoreCase("listenF")) {
            this.pregunta.setDificultad(0.9);
            this.pregunta.setFdescuido(0.22);
            this.pregunta.setIndiceDis(1.2);
        } else if (tipoPreg.equalsIgnoreCase("listenM")) {
            this.pregunta.setDificultad(3.0);
            this.pregunta.setFdescuido(0.25);
            this.pregunta.setIndiceDis(2.2);
        } else if (tipoPreg.equalsIgnoreCase("speakF")) {
            this.pregunta.setDificultad(0.89);
            this.pregunta.setFdescuido(0.22);
            this.pregunta.setIndiceDis(1.2);
        } else if (tipoPreg.equalsIgnoreCase("speakM")) {
            this.pregunta.setDificultad(3.0);
            this.pregunta.setFdescuido(0.25);
            this.pregunta.setIndiceDis(2.0);
        } else if (tipoPreg.equalsIgnoreCase("speakD")) {
            this.pregunta.setDificultad(4.0);
            this.pregunta.setFdescuido(0.24);
            this.pregunta.setIndiceDis(2.0);
        }
        this.tipoPreg = tipoPreg;
    }

    public void importarPreguntas() {
        ICsvMapReader mapReader = null;
        this.session = null;
        this.transaction = null;
        DaoPregunta daoPregunta = new DaoPregunta();
        DaoPregConc daoPregConc = new DaoPregConc();
        DaoConcepto daoConcepto = new DaoConcepto();
        DaoItem daoItem = new DaoItem();
        try {
            actualizarCSV();
            if (this.nombreArchivo == null) {
                return;
            }
            mapReader = new CsvMapReader(new FileReader(this.nombreArchivo), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = mapReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();
            Map<String, Object> userMap = null;
            Pregunta t = null;
            Pregunta pregRegistrada = null;
            Concepto conceptoCsv = null;
            List<Concepto> lstConceptos = null;
            List<Item> lstItems = null;
            List<PregConc> lstPregConc = null;
            PregConc relacionConcepto = null;
            StringTokenizer strToken = null;
            String nameC = "";
            String nameI = "";
            String traduccion = "";
            String imgI = "";
            String tema = "";
            Item item = null;
            while ((userMap = mapReader.read(header, processors)) != null) {
                // INSTRUCCIONES CREACIÓN
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                tema = (String) userMap.get("tema");
                // cOMPROBAR QUE LOS CONCEPTOS EXISTEN en los temas indicados
                lstConceptos = new ArrayList<>();
                System.out.println("CONSULTA CONCEPTOS POR TEMAS");
                strToken = new StringTokenizer((String) userMap.get("conceptos"), "/");
                while (strToken.hasMoreTokens()) {
                    nameC = strToken.nextToken();
                    conceptoCsv = daoConcepto.verPorNombreConcepNombreTem(session, nameC, tema);
                    if (conceptoCsv == null) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al importar.", "No existe el concepto para el tema especificado: " + nameC));
                        return;
                    } else {
                        lstConceptos.add(conceptoCsv);
                    }
                }
                System.out.println("CREACIÓN PREGUNTA");
                t = new Pregunta();
                t.setNombrePreg("NombrePreg");
                String tipo = (String) userMap.get("tipo");
                if (tipo.equalsIgnoreCase("listenF")) {
                    t.setDificultad(0.9);
                    t.setFdescuido(0.22);
                    t.setIndiceDis(1.2);
                } else if (tipo.equalsIgnoreCase("listenM")) {
                    t.setDificultad(3.0);
                    t.setFdescuido(0.25);
                    t.setIndiceDis(2.2);
                } else if (tipo.equalsIgnoreCase("speakF")) {
                    t.setDificultad(0.89);
                    t.setFdescuido(0.22);
                    t.setIndiceDis(1.2);
                } else if (tipo.equalsIgnoreCase("speakM")) {
                    t.setDificultad(3.0);
                    t.setFdescuido(0.25);
                    t.setIndiceDis(2.0);
                } else if (tipo.equalsIgnoreCase("speakD")) {
                    t.setDificultad(4.0);
                    t.setFdescuido(0.24);
                    t.setIndiceDis(2.0);
                }
                t.setEnunciado((String) userMap.get("enunciado"));
                t.setEstado((boolean) userMap.get("estado"));
                daoPregunta.registrar(this.session, t);
                System.out.println("CONSULTA PREGUNTA REGISTRADA");
                pregRegistrada = daoPregunta.verPorCodigoPregunta(session, daoPregunta.verUltimoRegistro(session));

                // instanciar los conceptos
                lstPregConc = new ArrayList<>();
                relacionConcepto = new PregConc();
                System.out.println("LISTA RELACIONES ENTRE LOS CONCEPTOS");
                for (int i = 0; i < lstConceptos.size(); i++) {
                    relacionConcepto.setPregunta(pregRegistrada);
                    relacionConcepto.setConcepto(lstConceptos.get(i));
                    lstPregConc.add(relacionConcepto);
                    relacionConcepto = new PregConc();
                }
                System.out.println("GUARDA RELACIONEES ENTRE LOS CONCEPTOS");
                daoPregConc.registrarVarios(session, lstPregConc);
                this.transaction.commit();
                this.session.close();

                // REGISTRAR ITEMS
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                lstItems = new ArrayList<>();
                System.out.println("LISTA ITEMS");
                strToken = new StringTokenizer((String) userMap.get("item"), "-/");
                while (strToken.hasMoreTokens()) {
                    item = new Item();
                    item.setPregunta(pregRegistrada);
                    item.setNombreItem(strToken.nextToken());
                    item.setTraduccion(strToken.nextToken());
                    item.setImgItem(strToken.nextToken());
                    item.setEstado(true);
                    lstItems.add(item);
                }
                System.out.println("REGISTRA ITEMS");
                daoItem.registrarVarios(session, lstItems);

                // Se crea nodo pregunta en Red bayesiana
                System.out.println("CAMBIOS EN LA RED BAYESIANA");
                CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
                if (!rbDynamic.existPregunta(tema, pregRegistrada.getNombrePreg())) {
                    rbDynamic.crearPregunta(tema, pregRegistrada, lstConceptos, lstItems.size());
                } else {
                    rbDynamic.editarPregunta(tema, pregRegistrada, lstConceptos, lstItems.size());
                }
                this.transaction.commit();
                item = new Item();
                t = new Pregunta();
            }
            mapReader.close();
            File archivoCsvTemp = new File(this.nombreArchivo);
            archivoCsvTemp.delete();
            this.nombreArchivo = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Datos importados correctamente."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "El archivo no corresponde a la plantilla."));
            System.out.println("ERROR: "+ ex);
        } finally {
            if (this.session != null) {
                if (this.session.isOpen()) {
                    this.session.close();
                }
            }
        }
    }

    private static CellProcessor[] getProcessors() {
        final CellProcessor[] processors = new CellProcessor[]{
            new NotNull(), // tema
            new NotNull(new ParseBool()), // estado
            new NotNull(), // tipo
            new NotNull(), // enunciado
            new NotNull(), // conceptos
            new NotNull(), // items
        };

        return processors;
    }

    public UploadedFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(UploadedFile csvFile) {
        this.csvFile = csvFile;
    }

    public void actualizarCSV() throws IOException {
        InputStream inputS = null;
        OutputStream outputS = null;
        try {
            if (this.csvFile.getSize() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", "Debe seleccionar un archivo csv"));
                return;
            }
            inputS = this.csvFile.getInputstream();
            ServletContext servletContex = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            String imgTemas = (String) servletContex.getRealPath("/resources") + "/" + this.csvFile.getFileName();
            String nombre = "";
            String extension = "";
            int pos = imgTemas.lastIndexOf(".");
            if (pos == -1) {
                nombre = imgTemas;
            } else {
                nombre = imgTemas.substring(0, pos);
                extension = imgTemas.substring(pos);
            }
            if (!extension.equals(".csv")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL CARGAR ARCHIVO:", "La extensión debe ser csv."));
                return;
            }
            outputS = new FileOutputStream(imgTemas);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputS.read(bytes)) != -1) {
                outputS.write(bytes, 0, read);
            }
            this.nombreArchivo = imgTemas;
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL CARGAR ARCHIVO:", "Contacte con el administrador, " + ex));
        } finally {
            if (inputS != null) {
                inputS.close();
            }
            if (outputS != null) {
                outputS.close();
            }
        }
    }
    
}

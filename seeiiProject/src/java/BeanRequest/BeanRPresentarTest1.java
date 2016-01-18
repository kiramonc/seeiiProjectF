/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Clases.RedBayesiana.CrearBayesDynamic;
import Dao.DaoConcepto;
import Dao.DaoEstudiante;
import Dao.DaoItem;
import Dao.DaoPregunta;
import Dao.DaoResultado;
import Dao.DaoTema;
import HibernateUtil.HibernateUtil;
import Pojo.Concepto;
import Pojo.Item;
import Pojo.Pregunta;
import Pojo.Resultado;
import Pojo.Tema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;

/**
 *
 * @author KathyR
 */
@ManagedBean
@ViewScoped
public class BeanRPresentarTest1 {

    private Tema test = new Tema();
    private int codigoTest;
    private List<Tema> listaTest;
    private List<Pregunta> listaPreguntas;
    private Session session;
    private Transaction transaction;
    private Pregunta preguntaSeleccionada;
    private List<Item> listaItems;
    private List<Item> listaItemsTemp;
    private List<Concepto> conceptosPreguntaDisp;
    private String respuestTemp;
    private String resultado;
    private String imgItemSeleccionado;
    private String nombreItemSeleccionado;
    private boolean correcto;
    private boolean correctoFinal;
    private String usuarioLogeado;
    private int codigoEstudiante;
    private HashMap<String, String> valorPrioriConocimiento;
    private boolean mostrarVistP;
    private boolean mostrarPreguntaListen;
    private boolean mostrarPreguntaSpeakF;
    private boolean mostrarPreguntaSpeakMD;
    private boolean mostrarSalir;
    private HtmlInputHidden inputCorrect;
    private HtmlInputHidden inputCorrectF;
    private String valorBinding;

    public BeanRPresentarTest1() {
        HttpSession sesionUsuario = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        usuarioLogeado = sesionUsuario.getAttribute("usernameLogin").toString();
        valorPrioriConocimiento = new HashMap<>();
        mostrarVistP = true;
        mostrarPreguntaListen = false;
        mostrarPreguntaSpeakF = false;
        mostrarPreguntaSpeakMD = false;
        mostrarSalir = false;
        preguntaSeleccionada = new Pregunta();
        correctoFinal = true;
        listaItemsTemp = new ArrayList<>();
        conceptosPreguntaDisp = new ArrayList<>();
        imgItemSeleccionado = "sad.png";
    }

    public void obtenerPregunta(int codigo) {
        this.session = null;
        this.transaction = null;
        this.codigoTest = codigo;
        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            DaoItem daoItem = new DaoItem();
            DaoTema daoTema = new DaoTema();
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            List<Concepto> lstConceptos = daoConcepto.verPorTema(session, codigoTest); // Conceptos del tema
            this.codigoEstudiante = new DaoEstudiante().verPorUsername(session, usuarioLogeado).getIdEst(); // id del estudiante
            this.transaction.commit();
            this.session.close();
            //1. Obtener un concepto aleatorio de la lista de conceptos para el tema
            Concepto conc3 = getConceptoAleatorio(lstConceptos);
            System.out.println("***********************************************");
            System.out.println("***********************************************");
            System.out.println("LA LISTA DE CONCEPTOS TIENE UN TAMAÑO DE: " + lstConceptos.size());
            if (conc3 != null) {
                System.out.println("EL CONCEPTO OBTENIDO ALEATORIAMENTE ES: " + conc3.getNombreConcepto());
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                // 2. Consultar a qué preguntas corresponde el concepto
                List<Pregunta> listaPregunta = daoPregunta.verPorConcepto(session, conc3.getIdConcepto());
                List<Pregunta> pregForConcepto = new ArrayList<>(); // lista de preguntas válidas
                for (int i = 0; i < listaPregunta.size(); i++) {
                    if (daoItem.verNumItemsPorPregunta(session, listaPregunta.get(i).getIdPregunta()) != 0) {
                        pregForConcepto.add(listaPregunta.get(i)); // Pregunta válida si tiene items
                    }
                }
                System.out.println("HAY UN TOTAL DE " + pregForConcepto.size() + " DE PREGUNTAS VÁLIDAS");
                // Crear objeto del análisis de la red bayesiana
                CrearBayesDynamic cbdynamic = new CrearBayesDynamic();
                this.preguntaSeleccionada = cbdynamic.getPreguntaOptima(daoTema.verPorCodigoTema(session, codigo).getNombre(), usuarioLogeado, pregForConcepto, valorPrioriConocimiento);
                this.conceptosPreguntaDisp = daoConcepto.verPorPregunta(session, this.preguntaSeleccionada.getIdPregunta());
                this.listaItemsTemp = new ArrayList<>();

                System.out.println("La pregunta mostrada en el enunciado es:" + preguntaSeleccionada.getNombrePreg());
                System.out.println("HAY UN TOTAL DE " + conceptosPreguntaDisp.size() + " ITEMS CORRECTOS PARA LA PREGUNTA");
                transaction.commit();
                String tipo = getTipoPreg(preguntaSeleccionada);
                this.mostrarVistP = false;
                this.mostrarSalir = true;
                switch (tipo) {
                    case "listenF":
                    case "listenM":
                        mostrarPreguntaListen = true;
                        mostrarPreguntaSpeakF = false;
                        mostrarPreguntaSpeakMD = false;
                        RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                        break;
                    case "speakF":
                        mostrarPreguntaListen = false;
                        mostrarPreguntaSpeakF = true;
                        mostrarPreguntaSpeakMD = false;
                        RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                        break;
                    case "speakM":
                    case "speakD":
                        mostrarPreguntaListen = false;
                        mostrarPreguntaSpeakF = false;
                        mostrarPreguntaSpeakMD = true;
                        RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                        RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                        break;
                }
            } else { // si el concepto es null todos los conceptos han sido aprendidos
                System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                System.out.println(" TODOS LOS CONCEPTOS HAN SIDO APRENDIDOS");
                mostrarPreguntaListen = false;
                mostrarPreguntaSpeakF = false;
                mostrarPreguntaSpeakMD = false;
                RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                RequestContext.getCurrentInstance().update("frmTerminarTest:panelTerminarTest");
                RequestContext.getCurrentInstance().execute("PF('dialogTerminarTest').show()");
            }

        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al obtener pregunta por codigo:", "Contacte con el administrador" + ex.getMessage()));

        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
    }

    private Concepto getConceptoAleatorio(List<Concepto> lstConceptos) {
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            DaoPregunta daoPregunta = new DaoPregunta();
            int codigoConcepto;
            boolean bandera = true;
            Concepto c = null;
            Concepto cElegido = null;
            List<Concepto> lstConceptosaprendidos = new ArrayList<>();
            if (valorPrioriConocimiento.isEmpty()) {
                // consultar valores de la BD durante la primera pregunta
                System.out.println("ESTÁ CONSULTANDO A LA BASE DE DATOS LOS VALORES PRIORI");
                DaoResultado daoResultado = new DaoResultado();
                List<Resultado> lstResultados = daoResultado.verPorEstudianteTema(session, codigoTest, codigoEstudiante);
                for (int i = 0; i < lstResultados.size(); i++) {
                    this.valorPrioriConocimiento.put(lstResultados.get(i).getConcepto().getNombreConcepto(), lstResultados.get(i).getValor() + "");
                }
            }
            DaoConcepto daoConcepto = new DaoConcepto();
            for (Map.Entry<String, String> entry : valorPrioriConocimiento.entrySet()) {
                if (Double.parseDouble(entry.getValue()) < 0.95) {
                    lstConceptosaprendidos.add(daoConcepto.verPorNombreConcepto(session, entry.getKey()));
                }
            }

            System.out.println("VA A ENTRAR AL DO WHILE");
            do {
                c = new Concepto();
                System.out.println("VA A ENTRAR AL IF IS EMPTY");
                if (!lstConceptosaprendidos.isEmpty()) {
                    System.out.println("va a calcular el random");
                    codigoConcepto = (int) (Math.random() * lstConceptosaprendidos.size());
                    c = lstConceptosaprendidos.get(codigoConcepto);
                    System.out.println("el concepto obtenido por random es: " + c.getNombreConcepto());

                    if (!daoPregunta.verPorConcepto(session, c.getIdConcepto()).isEmpty()) { // Comprobar que existen preguntas para el concepto
                        cElegido = c;
                        System.out.println("TIENE PREGUNTAS");
                        bandera = false;
                    } else {
                        System.out.println("no tiene preguntas");
                        lstConceptos.remove(codigoConcepto);
                        bandera = true;
                    }

                } else {
                    System.out.println("LA LISTA DE CONCEPTOS ESTUVO VACÍA");
                    bandera = false;
                }
            } while (bandera == true);

            return cElegido;
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CONSULTAR CONCEPTO POR TEST - TEMA:", "Contacte con el administrador" + ex.getMessage()));
            System.out.println("error getConceptoAleatorio " + ex);
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
        return null;
    }

    private void inferenciaRed() {
        // LA INFERENCIA TIENE QUE IR LUEGO QUE SE HA DADO UNA RESPUESTA
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            // valorPOsteriori calculado            
            DaoTema daoTema = new DaoTema();
            CrearBayesDynamic cbdynamic = new CrearBayesDynamic();
            System.out.println("EL VALOR PRIORI DE LOS CONCEPTOS ES:");
            System.out.println(valorPrioriConocimiento.toString());
            // asignación de los nuevos valores del aprendizaje (valores posteriori pasan a ser priori para la siguiente pregunta)
            System.out.println("ENVÍO PARA LA INFERENCIA: " + daoTema.verPorCodigoTema(session, this.codigoTest).getNombre());
            System.out.println("ENVÍO PARA LA INFERENCIA: " + preguntaSeleccionada.getEnunciado());
            System.out.println("ENVÍO PARA LA INFERENCIA: " + correctoFinal);
            valorPrioriConocimiento = cbdynamic.inferencia(usuarioLogeado, daoTema.verPorCodigoTema(session, this.codigoTest).getNombre(), preguntaSeleccionada, correctoFinal, valorPrioriConocimiento);
            System.out.println("EL VALOR POSTERIORI DE LOS CONCEPTOS ES:");
            System.out.println(valorPrioriConocimiento.toString());
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

    // Una sola respuesta correcta (1 concepto)
    private String preguntaListenM() {
        // Si el nombre del concepto es igual al del item la respuesta es correcta= true
        this.correcto = conceptosPreguntaDisp.get(0).getNombreConcepto().equalsIgnoreCase(respuestTemp);
        try {
            if (correcto) { // Cadena que se va a presentar
                this.resultado = "¡CONGRATULATIONS!";
                for (int i = 0; i < conceptosPreguntaDisp.size(); i++) {
                    if (conceptosPreguntaDisp.get(i).getNombreConcepto().equalsIgnoreCase(respuestTemp)) {
                        conceptosPreguntaDisp.remove(i);
                    }
                }
                DaoItem daoItem = new DaoItem();
                this.session = null;
                this.transaction = null;

                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                
                Item itemSelect = daoItem.verPorPREGNombreItem(session, respuestTemp, this.preguntaSeleccionada.getIdPregunta());
                imgItemSeleccionado = itemSelect.getImgItem();

            } else {
                this.resultado = "Try again";
                this.correctoFinal = false;
                imgItemSeleccionado = "sad.png";
            }
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CONSULTAR ITEM HAY MÁS DE UN ITEM CON LA MISMO IDENTIFICADOR EN LA PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
        return resultado;
    }

    // Una sola respuesta correcta (1 concepto)
    private String preguntaSpeakM() {
        // Si el nombre del concepto es igual al del item la respuesta es correcta= true
        this.correcto = conceptosPreguntaDisp.get(0).getNombreConcepto().equalsIgnoreCase(respuestTemp);
        if (correcto) { // Cadena que se va a presentar
            this.resultado = respuestTemp;
            conceptosPreguntaDisp.remove(0);
        } else {
            this.resultado = "Try again";
        }
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            DaoItem daoItem = new DaoItem();
            Item itemSelect = daoItem.verPorPREGNombreItem(session, respuestTemp, this.preguntaSeleccionada.getIdPregunta());
            imgItemSeleccionado = itemSelect.getImgItem();
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CONSULTAR ITEM HAY MÁS DE UN ITEM CON LA MISMO IDENTIFICADOR EN LA PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
        return resultado;
    }

    // Varios conceptos (Varias respuestas)
    private String preguntaListenF() {
        DaoItem daoItem = new DaoItem();
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.correcto = false;
            for (Concepto listaConcepto : conceptosPreguntaDisp) {
                if (listaConcepto.getNombreConcepto().equalsIgnoreCase(respuestTemp)) {
                    this.correcto = true; // la respuesta pertenece a la lista de respuestas correctas
                    break;
                }
            }

            if (correcto) { // Cadena que se va a presentar
                this.resultado = respuestTemp;
                Item itemSelect = daoItem.verPorPREGNombreItem(session, respuestTemp, this.preguntaSeleccionada.getIdPregunta());
                imgItemSeleccionado = itemSelect.getImgItem();
                for (int i = 0; i < this.listaItemsTemp.size(); i++) {
                    if (listaItemsTemp.get(i).getNombreItem().equalsIgnoreCase(respuestTemp)) {
                        listaItemsTemp.remove(i);
                    }
                }
                for (int i = 0; i < conceptosPreguntaDisp.size(); i++) {
                    if (conceptosPreguntaDisp.get(i).getNombreConcepto().equalsIgnoreCase(respuestTemp)) {
                        conceptosPreguntaDisp.remove(i);
                    }
                }
            } else {
                this.resultado = "Try again.";
                imgItemSeleccionado = "sad.png";
                this.correctoFinal = false;
            }
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CONSULTAR ITEM HAY MÁS DE UN ITEM CON LA MISMO IDENTIFICADOR EN LA PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
        return resultado;
    }

    // Varios conceptos (Varias respuestas)
    private String preguntaSpeakF() {
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            // Consultar todos los conceptos relacionados con ese tema
            boolean correcto = false;
            DaoItem daoItem = new DaoItem();
            Item itemSelect = null;
            for (int i = 0; i < conceptosPreguntaDisp.size(); i++) {
                if (conceptosPreguntaDisp.get(i).getNombreConcepto().equalsIgnoreCase(respuestTemp)) {
                    correcto = true;
                    itemSelect = daoItem.verPorPREGNombreItem(session, respuestTemp, this.preguntaSeleccionada.getIdPregunta());
                    imgItemSeleccionado = itemSelect.getImgItem();
                    System.out.println("el item seleccionado es: " + itemSelect.getNombreItem());
                    break;
                }
            }

            if (correcto) { // Cadena que se va a presentar
                this.resultado = "This is " + respuestTemp + ". Now repeat.";
                for (int i = 0; i < this.listaItemsTemp.size(); i++) {
                    if (listaItemsTemp.get(i).getNombreItem().equalsIgnoreCase(respuestTemp)) {
                        listaItemsTemp.remove(i);
                    }
                }
                for (int i = 0; i < conceptosPreguntaDisp.size(); i++) {
                    if (conceptosPreguntaDisp.get(i).getNombreConcepto().equalsIgnoreCase(respuestTemp)) {
                        conceptosPreguntaDisp.remove(i);
                    }
                }
            } else {
                this.resultado = "Try again" + respuestTemp;
            }
            transaction.commit();
            return resultado;
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CONSULTAR ITEM HAY MÁS DE UN ITEM CON LA MISMO IDENTIFICADOR EN LA PREGUNTA:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (session.isOpen()) {
                    this.session.close();
                }
            }
        }
        return null;
    }

    public String guardarRespuestaTemp() {
        String resp = "";
        if (respuestTemp != null) {
            this.nombreItemSeleccionado = respuestTemp.toLowerCase();
            String tipoPregunta = getTipoPreg(preguntaSeleccionada);
            if (null != tipoPregunta) {
                switch (tipoPregunta) {
                    case "listenF":
                        resp = preguntaListenF();
                        break;
                    case "listenM":
                        resp = preguntaListenM();
                        break;
                    case "speakF":
                        resp = preguntaSpeakF();
                        break;
                    case "speakM":
                        resp = preguntaSpeakM();
                        break;
                    case "speakD":
                        resp = preguntaSpeakM();
                        break;
                }
            }
            respuestTemp = null;
        }
        return resp;
    }

    public String onClick() {
        String resp = "";
        if (respuestTemp != null) {
            String tipoPregunta = getTipoPreg(preguntaSeleccionada);
            if (null != tipoPregunta) {
                switch (tipoPregunta) {
                    case "listenF":
                    case "listenM":
                        resp = "PF('dialogResultado').show()";
                        break;
                    case "speakF":
                    case "speakM":
                    case "speakD":
                        resp = "PF('dialogResultadoSpeak').show()";
                        break;
                }
            }
            respuestTemp = null;
        }
        return resp;
    }

    public String update() {
        String resp = "";
        if (respuestTemp != null) {
            String tipoPregunta = getTipoPreg(preguntaSeleccionada);
            if (null != tipoPregunta) {
                switch (tipoPregunta) {
                    case "listenF":
                    case "listenM":
                        resp = ":frmResultadoListen:panelResultadoListen";
                        break;
                    case "speakF":
                        resp = ":frmResultadoSpeakF:panelResultadoSpeakF";
                        break;
                    case "speakM":
                    case "speakD":
                        resp = ":frmResultadoSpeakMD:panelResultadoSpeakMD";
                        break;
                }
            }
            respuestTemp = null;
        }
        return resp;
    }

    private String getTipoPreg(Pregunta pregunta) {
        if (pregunta.getDificultad() == 0.9 && pregunta.getFdescuido() == 0.22 && pregunta.getIndiceDis() == 1.2) {
            return "listenF";
        } else {
            if (pregunta.getDificultad() == 3.0 && pregunta.getFdescuido() == 0.25 && pregunta.getIndiceDis() == 2.2) {
                return "listenM";
            } else {
                if (pregunta.getDificultad() == 0.89 && pregunta.getFdescuido() == 0.22 && pregunta.getIndiceDis() == 1.2) {
                    return "speakF";
                } else {
                    if (pregunta.getDificultad() == 3.0 && pregunta.getFdescuido() == 0.25 && pregunta.getIndiceDis() == 2.0) {
                        return "speakM";
                    } else {
                        if (pregunta.getDificultad() == 4.0 && pregunta.getFdescuido() == 0.24 && pregunta.getIndiceDis() == 2.0) {
                            return "speakD";
                        }
                        return null;
                    }
                }
            }
        }
    }

    public void actualizarPagina(int codigo) {
        if (conceptosPreguntaDisp.isEmpty()) {
            inferenciaRed();
            this.correctoFinal = true;
            obtenerPregunta(codigo);
        } else {
            String tipo = getTipoPreg(preguntaSeleccionada);
            switch (tipo) {
                case "listenF":
                case "listenM":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
                case "speakF":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
                case "speakM":
                case "speakD":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
            }
        }
    }

    public void actualizarSpeakF(int codigo) {
        if (conceptosPreguntaDisp.isEmpty()) {
            System.out.println("EL VALOR DEL BINDING ES: " + valorBinding);
            correcto = valorBinding.equals("true");
            if (!correcto) {
                correctoFinal = false;
                System.out.println("EL VALOR BOOLEAN PARA LA PREGUNTA SPEAK F ES: " + correctoFinal);
            }
            System.out.println("EN LA PREGUNTA SPEAK F SE ENVIA: " + correctoFinal + " PARA LA INFERENCIA");
            inferenciaRed();
            this.correctoFinal = true;
            obtenerPregunta(codigo);
        } else {
            // asignar correcto y si es falso asignar correcto Final=false
//            this.correcto= correct;
            System.out.println("EL VALOR DEL BINDING ES: " + this.valorBinding);
            correcto = valorBinding.equals("true");
            if (!correcto) {
                correctoFinal = false;
                System.out.println("EL VALOR BOOLEAN PARA LA PREGUNTA SPEAK F ES: " + correctoFinal);
            }
            String tipo = getTipoPreg(preguntaSeleccionada);
            switch (tipo) {
                case "listenF":
                case "listenM":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
                case "speakF":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
                case "speakM":
                case "speakD":
                    RequestContext.getCurrentInstance().update("frmMostrarTestListen");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakF");
                    RequestContext.getCurrentInstance().update("frmMostrarSpeakMD");
                    break;
            }
        }
    }

    public void actualizarSpeakMD(int codigo) {
        if (conceptosPreguntaDisp.isEmpty()) {
            // correcto = correcto Final
            // asignar correcto fianl
            System.out.println("EL VALOR DEL BINDING ES: " + valorBinding);
            correcto = valorBinding.equals("true");
            this.correctoFinal = correcto;
            inferenciaRed();
            this.correctoFinal = true;
            obtenerPregunta(codigo);
        }
    }

    public String terminarTest() {
        // Guardar valores del conocimineto en la BD
        if (!valorPrioriConocimiento.isEmpty()) {
            this.session = null;
            this.transaction = null;
            try {
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                // actualizar los valores del conocimiento en la BD
                DaoResultado daoResultado = new DaoResultado();
                DaoConcepto daoConcepto = new DaoConcepto();
                List<Resultado> lstResultados = new ArrayList<>();
                Resultado rBD;
                for (Map.Entry<String, String> entry : valorPrioriConocimiento.entrySet()) {
                    // obtengo el resultado anterior
                    rBD = daoResultado.verPorEstudianteConcepto(session, codigoEstudiante, daoConcepto.verPorNombreConcepto(session, entry.getKey()).getIdConcepto());
                    rBD.setValor(Double.parseDouble(entry.getValue())); // actualizo valor
                    lstResultados.add(rBD); // agrego el resultado a la lista de resultados nuevos
                    rBD = new Resultado();
                }
                daoResultado.actualizarVarios(session, lstResultados); // actualizar resultados en la base de datos
                DaoTema daoTema = new DaoTema();
                CrearBayesDynamic cbdynamic = new CrearBayesDynamic();
                cbdynamic.terminarInferencia(daoTema.verPorCodigoTema(session, this.codigoTest).getNombre(), usuarioLogeado);
                this.transaction.commit();
            } catch (Exception ex) {
                if (this.transaction != null) {
                    if (transaction.isInitiator()) {
                        this.transaction.rollback();
                    }
                }
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al guardar valores conocimiento en la Base de datos:", "Contacte con el administrador" + ex.getMessage()));
            } finally {
                if (this.session != null) {
                    if (session.isOpen()) {
                        this.session.close();
                    }
                }
            }
        }
        return "listaTest";
    }

    public String salir() {
        return "listaTest";
    }

    public List<Item> itemsPorPregunta() {
        if (this.listaItemsTemp.isEmpty()) {
            this.session = null;
            this.transaction = null;

            try {
                DaoItem daoItem = new DaoItem();
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                System.out.println("La pregunta seleccionada es:");
                System.out.println(preguntaSeleccionada.getEnunciado());
                List<Item> t = daoItem.verPorPregunta(session, preguntaSeleccionada.getIdPregunta());
                transaction.commit();
                this.listaItemsTemp = t;
                return listaItemsTemp;
            } catch (Exception ex) {
                if (this.transaction != null) {
                    this.transaction.rollback();
                }
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al obtener items por pregunta:", "Contacte con el administrador" + ex.getMessage()));
                return null;
            } finally {
                if (this.session != null) {
                    this.session.close();
                }
            }
        } else {
            return listaItemsTemp;
        }
    }

    public List<Pregunta> cargarListaPreguntas() {
        this.session = null;
        this.transaction = null;

        try {
            DaoPregunta daoPregunta = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            daoPregunta.verPorTest(session, 1);
            transaction.commit();
            return listaPreguntas;
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR al cargar preguntas del test 1:", "Contacte con el administrador" + ex.getMessage()));
            return null;
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public Tema consultarTestPorCodigo(int idTest) {
        this.session = null;
        this.transaction = null;
        try {
            DaoTema daoUnidad = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Tema t = daoUnidad.verPorCodigoTema(session, idTest);
            transaction.commit();
            this.test = t;
            return t;
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

    public List<Tema> getAllTest() {
        this.session = null;
        this.transaction = null;
        try {
            DaoTema daoTest = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.listaTest = daoTest.verTodo(session);
            this.transaction.commit();
            return this.listaTest;

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

    public boolean deshabilitarBotonCrearPregunta() {
        if (this.test != null) {
            return false;
        }
        return true;
    }

    public Tema getTest() {
        return test;
    }

    public void setTest(Tema test) {
        this.test = test;
    }

    public List<Tema> getListaTest() {
        return listaTest;
    }

    public void setListaTest(List<Tema> listaTest) {
        this.listaTest = listaTest;
    }

    public List<Pregunta> getListaPreguntas() {
        return listaPreguntas;
    }

    public void setListaPreguntas(List<Pregunta> listaPreguntas) {
        this.listaPreguntas = listaPreguntas;
    }

    public Pregunta getPreguntaSeleccionada() {
        return preguntaSeleccionada;
    }

    public void setPreguntaSeleccionada(Pregunta preguntaSeleccionada) {
        this.preguntaSeleccionada = preguntaSeleccionada;
    }

    public List<Item> getListaItems() {
        return listaItems;
    }

    public void setListaItems(List<Item> listaItems) {
        this.listaItems = listaItems;
    }

    public String getRespuestTemp() {
        return respuestTemp;
    }

    public void setRespuestTemp(String respuestTemp) {
        this.respuestTemp = respuestTemp;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getImgItemSeleccionado() {
        return imgItemSeleccionado;
    }

    public void setImgItemSeleccionado(String imgItemSeleccionado) {
        this.imgItemSeleccionado = imgItemSeleccionado;
    }

    public boolean isCorrecto() {
        return correcto;
    }

    public void setCorrecto(boolean correcto) {
        this.correcto = correcto;
    }

    public boolean isMostrarVistP() {
        return mostrarVistP;
    }

    public void setMostrarVistP(boolean mostrarVistP) {
        this.mostrarVistP = mostrarVistP;
    }

    public boolean isMostrarSalir() {
        return mostrarSalir;
    }

    public void setMostrarSalir(boolean mostrarSalir) {
        this.mostrarSalir = mostrarSalir;
    }

    public boolean isMostrarPreguntaListen() {
        return mostrarPreguntaListen;
    }

    public void setMostrarPreguntaListen(boolean mostrarPreguntaListen) {
        this.mostrarPreguntaListen = mostrarPreguntaListen;
    }

    public boolean isMostrarPreguntaSpeakF() {
        return mostrarPreguntaSpeakF;
    }

    public void setMostrarPreguntaSpeakF(boolean mostrarPreguntaSpeakF) {
        this.mostrarPreguntaSpeakF = mostrarPreguntaSpeakF;
    }

    public boolean isMostrarPreguntaSpeakMD() {
        return mostrarPreguntaSpeakMD;
    }

    public void setMostrarPreguntaSpeakMD(boolean mostrarPreguntaSpeakMD) {
        this.mostrarPreguntaSpeakMD = mostrarPreguntaSpeakMD;
    }

    public String getNombreItemSeleccionado() {
        return nombreItemSeleccionado;
    }

    public void setNombreItemSeleccionado(String nombreItemSeleccionado) {
        this.nombreItemSeleccionado = nombreItemSeleccionado;
    }

    public HtmlInputHidden getInputCorrect() {
        return inputCorrect;
    }

    public void setInputCorrect(HtmlInputHidden inputCorrect) {
        this.inputCorrect = inputCorrect;
    }

    public HtmlInputHidden getInputCorrectF() {
        return inputCorrectF;
    }

    public void setInputCorrectF(HtmlInputHidden inputCorrectF) {
        this.inputCorrectF = inputCorrectF;
    }

    public String getValorBinding() {
        return valorBinding;
    }

    public void setValorBinding(String valorBinding) {
        this.valorBinding = valorBinding;
    }

}

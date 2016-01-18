/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import BeanSession.BeanSEntrenar;
import Dao.DaoEntrenar;
import Dao.DaoFicha;
import Dao.DaoFichaPregunta;
import Dao.DaoPreguntaEntrenar;
import HibernateUtil.HibernateUtil;
import Pojo.Entrenamiento;
import Pojo.Ficha;
import Pojo.Fichaspregunta;
import Pojo.Preguntaentrenar;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DashboardReorderEvent;
import org.primefaces.model.DashboardColumn;
import org.primefaces.model.DashboardModel;
import org.primefaces.model.DefaultDashboardColumn;
import org.primefaces.model.DefaultDashboardModel;

/**
 *
 * @author USUARIO
 */
@ManagedBean
@ViewScoped
public class BeansTestPreguntaEntren2 {

    private Session session;
    private Transaction transaction;

    private DashboardModel model;
    int imgM[]; //elegir el modelo del dashboard(test 1-6) (imagen)
    int sonM[]; //elegir el modelo del dashboard(test 1-6) (sonido)

    //llamar al bean de sesion para obtner el modelodelTEST(1-6).
    @ManagedProperty("#{beanSEntrenar}")
    private BeanSEntrenar beanSEntrena;

    //atributo para obtner una listas de fichas para mostrar en test, se utliza en el metodo (listaFichas)
    ArrayList listaNumero = new ArrayList();
    //atributo para utilizar con el metodo (obtenerlistaFichasPregunta)
    private List<Fichaspregunta> listFichasPregunta;

    //atributo para obtner el nombre de las fichas --> para la Pronunciacion.
    private String nameficha1; //usado en metodo (obtnerSonidoficha1)
    private String nameficha2; //usado en metodo (obtnerSonidoficha2)
    private String nameficha3; //usado en metodo (obtnerSonidoficha3)

    //atributo utilizado en el metodo(crearPreguntaEntrena) [TEST3]
    private Preguntaentrenar preguntaEnt2;
    java.sql.Timestamp sqlDateT2;
    
    //mostara los resultados del test
    private String correcto;
    private int resultado1;
    private int resultado2;

    public BeansTestPreguntaEntren2() {
    }

    @PostConstruct
    public void init() {
        model = new DefaultDashboardModel();
        DashboardColumn columnDashboard1 = new DefaultDashboardColumn();
        DashboardColumn columnDashboard2 = new DefaultDashboardColumn();
        elegirModelT();//inicializa los atribuitos (imgM, sonM)
        //llamamos al metodo para elegir un modelo del dashboard
        elegirModelDashboard(columnDashboard1, columnDashboard2);
    }

    public void elegirModelT() {
        int num = this.beanSEntrena.getModelTest();
        System.out.println("model(Dashboard) numero ....................." + num);
        int mt1[] = {1, 2, 3};
        int mt2[] = {2, 3, 1};
        int mt3[] = {3, 1, 2};

        switch (num) {
            case 1:
                imgM = mt1;
                sonM = mt2;
                break;
            case 2:
                imgM = mt1;
                sonM = mt3;
                break;
            case 3:
                imgM = mt2;
                sonM = mt1;
                break;
            case 4:
                imgM = mt2;
                sonM = mt3;
                break;
            case 5:
                imgM = mt3;
                sonM = mt1;
                break;
            default:
                imgM = mt3;
                sonM = mt2;
                break;
        }
    }

    //Método patra crear un modelo de dashboard (existen 6 modelos)
    public void elegirModelDashboard(DashboardColumn column1, DashboardColumn column2) {
        for (int i = 0; i < 3; i++) {
            column1.addWidget("imagen" + imgM[i]);
            column2.addWidget("sonido" + sonM[i]);
        }

        column1.addWidget("4");
        column1.addWidget("5");
        column2.addWidget("4");
        column2.addWidget("5");

        model.addColumn(column1);
        model.addColumn(column2);
    }

    public void handleReorder(DashboardReorderEvent event) {
        ArrayList columna0 = this.beanSEntrena.getColumna0();
        ArrayList columna1 = this.beanSEntrena.getColumna1();

        String widgetId = event.getWidgetId(); //id de elemento
        int widgetIndex = event.getItemIndex(); //indice del item
        int columnIndex = event.getColumnIndex(); //indice de columna

        //1.- Comprobamos: En que columna está el elemento.
        int columnaE = 0; //por defecto esta en columna 0
        if (columna1.contains(widgetId)) {  // (columnaE) cambia, si esta en la comuna 1
            columnaE = 1;
        }
        //2.- Comprobamos: Si el elemento cambia de Columna.
        boolean estadoE = false; //por defecto no cambia de columna
        if (columnaE != columnIndex) {//El elemento cambia de columnan 
            estadoE = true; //no cambia de columna el elemento.
        }
        //3.- Reordenamos los elementos dependiendo (si cambia de columna el elemento)
        if (estadoE) { //cambia de columna el elemento
            if (columnaE == 0) { //el elemeto esta en columna 1
                System.out.println("(DE COLUMNA 1 A 2)el elemento: " + widgetId + ", Va a la posicion: " + widgetIndex + ", En la Columna: " + columnIndex);
                columna0.remove(widgetId);
                columna1.add(widgetIndex, widgetId);
            } else { //el elemeto esta en columna 2
                System.out.println("(DE COLUMNA 2 A 1)el elemento: " + widgetId + ", Va a la posicion: " + widgetIndex + ", En la Columna: " + columnIndex);
                columna1.remove(widgetId);
                columna0.add(widgetIndex, widgetId);
            }
        } else { //El elemento (se inserta en otra posicion en la misma columna) 
            if (columnaE == 0) { //el elemeto esta en columna 1
                System.out.println("(MISMA COLUMNA 1) el elemento: " + widgetId + " va a la posicion: " + widgetIndex);
                columna0.remove(widgetId);
                columna0.add(widgetIndex, widgetId);
            } else {  //el elemeto esta en columna 2
                System.out.println("(MISMA COLUMNA 2) El elemento: " + widgetId + " va a la posicion: " + widgetIndex);
                columna1.remove(widgetId);
                columna1.add(widgetIndex, widgetId);
            }
        }

        //fijo las columna en el beans SESSION
        this.beanSEntrena.setColumna0(columna0);
        this.beanSEntrena.setColumna1(columna1);

        System.out.println("Columna 0 " + columna0);
        System.out.println("Columna 1 " + columna1);

    }

    //------------------------------------------------------------------------------------------------
    public void inicializarListaFichaPrengunta(int idPrenguntaEnt) {
        //metodo para obtner la lista de fichas a presentar inicializa el atributo (listaNumero)
        listaFichas(0, 3, 3);//obtner 3 numeros del 0 al 3 (para mostrar fichas.)

        //hacer un  if si  idPrenguntaEnt!=0....................................
        //metodo para obtner la lista de fichasPreguntar inicializa el atributo (listFichasPregunta)
        obtenerlistaFichasPregunta(idPrenguntaEnt);

    }

    public void listaFichas(int valorInicial, int valorFinal, int numAleatorio) {
        for (int i = 0; i < numAleatorio;) {
            int numero = (int) (Math.random() * (valorFinal - valorInicial + 1) + valorInicial);//genero un numero
            if (listaNumero.isEmpty()) {//si la lista esta vacia
                listaNumero.add(numero);
                i++;
            } else {//si no esta vacia
                if (listaNumero.contains(numero)) {//Si el numero que generé esta contenido en la lista
                } else {//Si no esta contenido en la lista
                    listaNumero.add(numero);
                    i++;
                }
            }
        }
        System.out.println(" lista de numero aleatorios " + listaNumero + " para realizar el test emparejamiento........");
    }

    //metodo para obtner la lista FichasPreguntas para mostrara en el test
    public void obtenerlistaFichasPregunta(int idPrenguntaEnt) {
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //para crear Fichaspregunta (lista de fichas en base a un preguntadeENTRENAMIENTO)
            DaoFichaPregunta daofichaPregunta = new DaoFichaPregunta();
            listFichasPregunta = daofichaPregunta.verPreguntaEntrenamiento(session, idPrenguntaEnt);
            this.transaction.commit();

            System.out.println("Correcto: al obtner la lista de fichasPregunta se ha realizado con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL obtner FICHA_PREGUNTA en el test:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    //.........................................................................................
    //metodo para obtner NOMBRE de la ficha por su id
    public String verfichaPorId(int idFich) {
        String nombreFicha = "empty";
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //para crear Fichaspregunta (lista de fichas en base columna1 un preguntadeENTRENAMIENTO)
            DaoFicha daofichaPregunta = new DaoFicha();
            Ficha ficha = daofichaPregunta.verPorCodigoFicha(session, idFich);
            this.transaction.commit();
            nombreFicha = ficha.getNombreFicha();

            System.out.println("Correcto: al obtner el nombre (fichaPregunta)se ha realizado con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL obtner FICHA en el Test Emparejamiento:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
        return nombreFicha;
    }

    public String obtnerficha1() {
        //método (inicializarListaFichaPrengunta) inicializa las variables (listFichasPregunta y listaNumero )
        int idficha1 = this.listFichasPregunta.get((int) listaNumero.get(0)).getFicha().getIdFicha();
        String dirFicha = idficha1 + "";
        //metodo para sonido(nombre) de las fichas (verfichaPorId)
        nameficha1 = verfichaPorId(idficha1);
        return dirFicha;
    }

    public String obtnerficha2() {
        //método (inicializarListaFichaPrengunta) inicializa las variables (listFichasPregunta y listaNumero )
        int idficha2 = this.listFichasPregunta.get((int) listaNumero.get(1)).getFicha().getIdFicha();
        String dirFicha = idficha2 + "";
        //metodo para sonido(nombre) de las fichas (verfichaPorId)
        nameficha2 = verfichaPorId(idficha2);
        return dirFicha;
    }

    public String obtnerficha3() {
        //método (inicializarListaFichaPrengunta) inicializa las variables (listFichasPregunta y listaNumero )
        int idficha3 = this.listFichasPregunta.get((int) listaNumero.get(2)).getFicha().getIdFicha();
        String dirFicha = idficha3 + "";
        //metodo para sonido(nombre) de las fichas (verfichaPorId)
        nameficha3 = verfichaPorId(idficha3);
        return dirFicha;
    }

    //.........................................................................................
    public void finalizarEntrenamiento() {
        //1. cambia letras("sonido1" ó "imagen1",etc)a numero (1 ,2, 3), En Método (cambiarValor)
        ArrayList columna0 = this.beanSEntrena.getColumna0();
        ArrayList columna1 = this.beanSEntrena.getColumna1();
        ArrayList columnaN0 = cambiarValor(columna0);
        ArrayList columnaN1 = cambiarValor(columna1);
        System.out.println("el valor de los resultado de columna 0" + columnaN0);
        System.out.println("el valor de los resultado de columna 1" + columnaN1);

        //2. comparacion entre las dos columnas(tenga el menor elementos)
        int c = columnaN0.size();
        if (columnaN0.size() >= columnaN1.size()) {
            c = columnaN1.size();
        }

        //3. obtnemos los errores y puntacion.
        int valor = 0;
        int incorrectas = 0;
        ArrayList datos = obtnerValorError(c, columnaN0, columnaN1); //obtnemos los valores, e incorrectas
        switch (c) {
            case 0:
                valor = 0;
                incorrectas = 6;
                break;
            case 1:
                valor = (int) datos.get(0);
                incorrectas = (int) datos.get(1) + 4;
                break;
            case 2:
                valor = (int) datos.get(0);
                incorrectas = (int) datos.get(1) + 2;
                break;
            case 3:
                valor = (int) datos.get(0);
                incorrectas = (int) datos.get(1);
                break;
        }

        //4. Actualizamos (preguntaEntrenar) con los [valor, incorrectas]
        actulizarFichaPregunta(valor, incorrectas);
        System.out.println("VALOR de la puntución es: " + valor);
        System.out.println("respuestas INCORRECTAS : " + incorrectas);
        
        //5. crearmos Preguntaentrenar para TEST2.
        crearPreguntaEntrenaTEST3(this.beanSEntrena.getIdEntrenamiento());
            //mostramos los mensajes de los valores de punturación y respuestas incorrectas
        this.resultado1 = valor;
        this.resultado2 = incorrectas;
        this.correcto = "Mejora.gif";
        if (incorrectas == 0) {
            this.correcto = "Correcto.gif";
        }
        RequestContext.getCurrentInstance().update("frmResultado:panelResultado");
        RequestContext.getCurrentInstance().execute("PF('dialogResultado').show()");
//
    }

    public String actualizarPagina() {
        return "aprenderFichasPregunta3";
    }


    //Método para cambiar las columnas de String a Numeros
    public ArrayList cambiarValor(ArrayList columna) {
        ArrayList columnaN0 = new ArrayList();
        int num = 0;
        String valor = "";
        for (int i = 0; i < columna.size(); i++) {
            valor = (String) columna.get(i);
            if ((valor.equals("sonido1")) || (valor.equals("imagen1"))) {
                num = 1;
            } else {
                if (valor.equals("sonido2") || valor.equals("imagen2")) {
                    num = 2;
                } else {
                    if (valor.equals("sonido3") || valor.equals("imagen3")) {
                        num = 3;
                    }
                }
            }
            columnaN0.add(num); //llamamos metodo para cambiar el valor a numero
        }

        return columnaN0;
    }

    //Metodo para obtner los valores y respuestas Incprrectas
    public ArrayList obtnerValorError(int contador, ArrayList columnaN0, ArrayList columnaN1) {
        ArrayList datos = new ArrayList();
        int valor = 0;
        int incorrectas = 0;
        for (int i = 0; i < contador; i++) {
            if (columnaN0.get(i) == columnaN1.get(i)) {
                valor = valor + 150;
            } else {
                incorrectas = incorrectas + 1;
            }
        }
        datos.add(valor);
        datos.add(incorrectas);
        return datos;
    }

    public void actulizarFichaPregunta(int valor, int incorrectos) {
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //obtener una pregunta por su id y fijar en el Fichaspregunta.
            DaoPreguntaEntrenar daoPreguntaEntrenar = new DaoPreguntaEntrenar();
            Preguntaentrenar preguntaEntrenar = daoPreguntaEntrenar.verPorCodigoPreguntaEntrenar(session, this.beanSEntrena.getIdPrenguntaEnt());
            preguntaEntrenar.setValor(valor);
            preguntaEntrenar.setIncorrecto(incorrectos);

            daoPreguntaEntrenar.actualizar(session, preguntaEntrenar);
            this.transaction.commit();

            System.out.println("Correcto: La Actualizacion de la preguntaEntrenar TEST2 se ha realizado con exito");
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL Actualizar preguntaEntrenar:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }

    }

    //crea una pregunta de entrenamiento[TEST2] si existe un entrenamiento.
    public void crearPreguntaEntrenaTEST3(int idEntrenar) {
        this.preguntaEnt2 = new Preguntaentrenar();
        //OBTNER fecha-Hora del Sistema
        sqlDateT2 = new java.sql.Timestamp(new java.util.Date().getTime());
        boolean estado = false;
        this.session = null;
        this.transaction = null;
        try {
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            //obtener el entrenamiento mediante el id para fijar a las preguntas.
            DaoEntrenar daoEntrenar = new DaoEntrenar();
            Entrenamiento entrene = daoEntrenar.verPorCodigoEntrenamiento(session, idEntrenar);
            this.preguntaEnt2.setEntrenamiento(entrene);
            this.preguntaEnt2.setIncorrecto(0);
            this.preguntaEnt2.setValor(0);
            this.preguntaEnt2.setFecha(sqlDateT2);

            DaoPreguntaEntrenar daoPregunta = new DaoPreguntaEntrenar();
            estado = daoPregunta.registrarPreguntaEnt(this.session, this.preguntaEnt2);
            //si la pregunta se creo correctamente lo fijamos el atributo idPrenguntaEnt en -> beanSEntrena(beansSession )
            if (estado) {
                obtenerIdPreguntaEntTEST3(session, estado, idEntrenar, sqlDateT2);
                System.out.println("Se ha registado correctamente la Preguntaentrenar para el TEST3");
            }
            this.transaction.commit();

        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL REGISTRA  PREGUNTAS DE entrenamiento:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public int obtenerIdPreguntaEntTEST3(Session session, boolean state, int idEntrenar, Timestamp Fecha) {
        int idE = 0;
        if (state) {
            //obtner un pregunta(session,idestrenar, idtema,fecha) para obtner la idPregunta del respectivo entrenamiento
            DaoPreguntaEntrenar daoPrengunt = new DaoPreguntaEntrenar();
            Preguntaentrenar e = new Preguntaentrenar();
            try {
                e = daoPrengunt.verPreguntaEntrenamiento(session, idEntrenar, Fecha);
            } catch (Exception ex) {
                System.out.println("problemas al consultar el Preguntaentrenar(TEST3)creada recientemente");
                Logger.getLogger(BeansREntrenamiento.class.getName()).log(Level.SEVERE, null, ex);
            }

            //fijamos en el beanSessin(beanSEntrena) el atributo del idPrenguntaEnt(id de la preguntaEntrena)
            this.beanSEntrena.setIdPrenguntaEnt(e.getIdInt());
            idE = e.getIdInt();
        }
        return idE;

    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public String obtnerSonidoficha1() {
        return nameficha1;
    }

    public String obtnerSonidoficha2() {
        return nameficha2;
    }

    public String obtnerSonidoficha3() {
        return nameficha3;
    }

    //.............................SETTER AND GETTER...........................................
    public DashboardModel getModel() {
        return model;
    }

    public void setModel(DashboardModel model) {
        this.model = model;
    }

    public List<Fichaspregunta> getListFichasPregunta() {
        return listFichasPregunta;
    }

    public void setListFichasPregunta(List<Fichaspregunta> listFichasPregunta) {
        this.listFichasPregunta = listFichasPregunta;
    }

    public BeanSEntrenar getBeanSEntrena() {
        return beanSEntrena;
    }

    public void setBeanSEntrena(BeanSEntrenar beanSEntrena) {
        this.beanSEntrena = beanSEntrena;
    }

    public Preguntaentrenar getPreguntaEnt2() {
        return preguntaEnt2;
    }

    public void setPreguntaEnt2(Preguntaentrenar preguntaEnt2) {
        this.preguntaEnt2 = preguntaEnt2;
    }

     public int getResultado1() {  //obtner el resultado del valores del test
        return resultado1;
    }

    public int getResultado2() { //obtner el resultado de prespuesta-incorrectas del test
        return resultado2;
    }

    public String isCorrecto() {
        return correcto;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Clases.RedBayesiana.CrearBayesDynamic;
import Clases.RedBayesiana.CrearBayesNetwork1;
import Dao.DaoConcepto;
import Dao.DaoEstudiante;
import Dao.DaoItem;
import Dao.DaoPregunta;
import Dao.DaoResultado;
import Dao.DaoTema;
import HibernateUtil.HibernateUtil;
import Pojo.Concepto;
import Pojo.Estudiante;
import Pojo.Pregunta;
import Pojo.Resultado;
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
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
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
public class BeanRConcepto {

    private Concepto concepto;
    private List<Concepto> listaConceptos;
    private List<Concepto> listaConceptoFiltrado;
    private Session session;
    private Transaction transaction;
    private Tema tema;
    private UploadedFile csvFile;
    private String nombreArchivo;

    public BeanRConcepto() {
        this.tema = null;
        concepto = new Concepto();
    }

    public void registrar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            DaoTema daoTema = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            if (daoConcepto.verPorNombreConcepto(session, concepto.getNombreConcepto()) != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", "El nombre del concepto ya está registrado."));
                return;
            }
            this.concepto.setEstado(true);
            this.concepto.setTema(tema);
            daoConcepto.registrar(this.session, this.concepto);

            // Crear nodo Concepto en la red bayesiana
            CrearBayesNetwork1 redBayesiana = new CrearBayesNetwork1();
            Tema temaC = daoTema.verPorTemaname(session, tema.getNombre());
            redBayesiana.crearConcepto(temaC.getUnidadensenianza().getNombreUnidad(), temaC.getNombre(), concepto.getNombreConcepto());
            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            rbDynamic.crearConcepto(temaC.getNombre(), concepto.getNombreConcepto());

            // Agregar registro resultados
            Concepto concRegistrado = daoConcepto.verPorNombreConcepto(session, this.concepto.getNombreConcepto());
            DaoEstudiante daoEstudiante = new DaoEstudiante();
            List<Estudiante> lstEstudiantes = daoEstudiante.verPorUnidadEnsenianza(session, temaC.getUnidadensenianza().getNombreUnidad()); // posteriormente cambiar la Unidad por la Unidad a la q pertenece
            List<Resultado> lstResultados = new ArrayList<>();
            Resultado r = new Resultado();
            if(!lstEstudiantes.isEmpty()){
            for (int i = 0; i < lstEstudiantes.size(); i++) {
                r.setEstudiante(lstEstudiantes.get(i));
                r.setConcepto(concRegistrado);
                r.setValor(0.15);
                lstResultados.add(r);
                r = new Resultado();
            }
            DaoResultado daoResultado = new DaoResultado();
            daoResultado.registrarVarios(session, lstResultados);
            }
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "El registro fue realizado con éxito"));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR REGISTRO:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
            this.concepto = new Concepto();
        }
    }

    public void actualizar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Concepto conceptoAnterior = daoConcepto.verPorCodigoConcepto(session, concepto.getIdConcepto());
            String nombreUnidad = conceptoAnterior.getTema().getUnidadensenianza().getNombreUnidad();
            String nombreConcepto = conceptoAnterior.getNombreConcepto();
            this.transaction.commit();
            this.session.close();
            conceptoAnterior = null;
            // Crear nodo Concepto en la red bayesiana
            if (!nombreConcepto.equals(concepto.getNombreConcepto())) {
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                daoConcepto.actualizar(this.session, this.concepto);
                CrearBayesNetwork1 redBayesiana = new CrearBayesNetwork1();
                redBayesiana.editarConcepto(nombreUnidad, nombreConcepto, concepto.getNombreConcepto());
                CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
                rbDynamic.editarConcepto(tema.getNombre(), nombreConcepto, concepto.getNombreConcepto());
                this.transaction.commit();
            } else {
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                daoConcepto.actualizar(this.session, this.concepto);
            }

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR ACTUALIZAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                if (this.session.isOpen()) {
                    this.session.close();
                }
            }
            this.concepto = new Concepto();
        }
    }

    public void eliminar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Concepto conceptoAnterior = daoConcepto.verPorCodigoConcepto(session, concepto.getIdConcepto());
            String nombreUnidad = conceptoAnterior.getTema().getUnidadensenianza().getNombreUnidad();
            String nombreTema = conceptoAnterior.getTema().getNombre();
            this.transaction.commit();
            this.session.close();
            conceptoAnterior = null;

            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();

            //Eliminar nodo Concepto de la red bayesiana
            CrearBayesNetwork1 redBayesiana = new CrearBayesNetwork1();
            redBayesiana.eliminarConcepto(nombreUnidad, nombreTema, concepto.getNombreConcepto());
            // Eliminar nodo Concepto de la red bayesiana dinámica
            DaoPregunta daoPregunta = new DaoPregunta();
            List<Pregunta> lstPreguntas = daoPregunta.verPorConcepto(session, concepto.getIdConcepto());
            DaoItem daoItem = new DaoItem();
            HashMap<Integer, Integer> preguntasItems = new HashMap<>();
            for (int i = 0; i < lstPreguntas.size(); i++) {
                preguntasItems.put(lstPreguntas.get(i).getIdPregunta(), daoItem.verNumItemsPorPregunta(session, lstPreguntas.get(i).getIdPregunta()));
            }
            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            rbDynamic.eliminarConcepto(nombreTema, concepto, lstPreguntas, preguntasItems);
            daoConcepto.eliminar(this.session, this.concepto);
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Concepto eliminado correctamente."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL ELIMINAR:", "Contacte con el administrador, " + ex));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
            this.concepto = new Concepto();

        }
    }

    public Concepto consultarConceptoPorCodigo(int idConcepto) {
        this.session = null;
        this.transaction = null;
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Concepto c = daoConcepto.verPorCodigoConcepto(session, idConcepto);
            transaction.commit();
            return c;
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

    // para el converter
    public Concepto consultarConceptoPorNombre(String nombreConcepto) {
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            Concepto c = daoConcepto.verPorNombreConcepto(session, nombreConcepto);
            transaction.commit();
            return c;

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

    public List<Concepto> getAllConcepto() {
        this.session = null;
        this.transaction = null;
        try {
            DaoConcepto daoConcepto = new DaoConcepto();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.listaConceptos = daoConcepto.verTodo(session);
            this.transaction.commit();
            return this.listaConceptos;

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

    public void abrirDialogoCrearConcepto(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            this.concepto = new Concepto();
            DaoTema daoTema = new DaoTema();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.tema = daoTema.verPorCodigoTema(session, codigo);
            RequestContext.getCurrentInstance().update("frmEditarConcepto:panelEditarConcepto");
            RequestContext.getCurrentInstance().execute("PF('dialogEditarConcepto').show()");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR CONCEPTO CREAR:", "Contacte con el administrador" + ex.getMessage()));
        }
    }

    // para el selectOneMenu al crear el test
    public List<Concepto> getConceptosPorTema(Tema tema) {
        if (tema != null) {
            this.session = null;
            this.transaction = null;
            this.tema = tema;
            try {
                DaoConcepto daoConcepto = new DaoConcepto();
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();

                List<Concepto> temp = daoConcepto.verPorTema(session, this.tema.getIdTema());
                transaction.commit();
                return temp;
            } catch (Exception ex) {
                if (this.transaction != null) {
                    this.transaction.rollback();
                }
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR LISTA DE CONCEPTO POR TEMA:", "Contacte con el administrador" + ex.getMessage()));

                return null;
            } finally {
                if (this.session != null) {
                    this.session.close();
                }
            }
        }
        return null;
    }

    public boolean deshabilitarBotonCrearTema() {

        return false;

    }

    public void limpiarFormulario() {
        this.concepto = new Concepto();
        RequestContext.getCurrentInstance().update("frmVerConceptos:panelVerConceptos");
        RequestContext.getCurrentInstance().execute("PF('dialogVerConceptos').show()");

    }

//    public boolean deshabilitarBotonCrearPregunta() {
//        if (this.concepto.getTema() != null) {
//            return false;
//        }
//        return true;
//    }
    public Concepto getConcepto() {
        return concepto;
    }

    public void setConcepto(Concepto concepto) {
        this.concepto = concepto;
    }

    public List<Concepto> getListaConceptos() {
        return listaConceptos;
    }

    public void setListaConceptos(List<Concepto> listaConceptos) {
        this.listaConceptos = listaConceptos;
    }

    public List<Concepto> getListaConceptoFiltrado() {
        return listaConceptoFiltrado;
    }

    public void setListaConceptoFiltrado(List<Concepto> listaConceptoFiltrado) {
        this.listaConceptoFiltrado = listaConceptoFiltrado;
    }

    public void importarConceptos() {
        ICsvMapReader mapReader = null;
        this.session = null;
        this.transaction = null;
        DaoTema daoTema = new DaoTema();
        DaoConcepto daoConcepto = new DaoConcepto();
        DaoEstudiante daoEstudiante = new DaoEstudiante();
        DaoResultado daoResultado = new DaoResultado();
        try {
            actualizarCSV();
            if (this.nombreArchivo == null) {
                return;
            }
            mapReader = new CsvMapReader(new FileReader(this.nombreArchivo), CsvPreference.STANDARD_PREFERENCE);
            final String[] header = mapReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();
            Map<String, Object> userMap = null;
            Tema t = null;
            Concepto c = null;
            Concepto concRegistrado = null;
            List<Estudiante> lstEstudiantes = null;
            List<Resultado> lstResultados = null;
            Resultado r = null;
            while ((userMap = mapReader.read(header, processors)) != null) {
                // INSTRUCCIONES CREACIÓN
                this.session = HibernateUtil.getSessionFactory().openSession();
                this.transaction = session.beginTransaction();
                c=daoConcepto.verPorNombreConcepto(session, userMap.get("nombreConcepto").toString());
                if (c != null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error.", "El nombre del concepto ya está registrado."));
                    return;
                }
                t = daoTema.verPorTemaname(session, (String) userMap.get("tema"));
                if (t == null) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al importar.", "Tema no existe: " + (String) userMap.get("tema")));
                    return;
                }
                c = new Concepto();
                c.setNombreConcepto((String) userMap.get("nombreConcepto"));
                c.setTraduccion((String) userMap.get("traduccion"));
                c.setDescripcion((String) userMap.get("descripcion"));
                c.setEstado((boolean) userMap.get("estado"));
                c.setTema(t);
                daoConcepto.registrar(this.session, c);

                // Crear nodo Concepto en la red bayesiana
                CrearBayesNetwork1 redBayesiana = new CrearBayesNetwork1();
                redBayesiana.crearConcepto(t.getUnidadensenianza().getNombreUnidad(), t.getNombre(), c.getNombreConcepto());
                CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
                rbDynamic.crearConcepto(t.getNombre(), c.getNombreConcepto());
                // Agregar registro resultados
                concRegistrado = daoConcepto.verPorNombreConcepto(session, c.getNombreConcepto());

                lstEstudiantes = daoEstudiante.verPorUnidadEnsenianza(session, t.getUnidadensenianza().getNombreUnidad()); // posteriormente cambiar la Unidad por la Unidad a la q pertenece
                lstResultados = new ArrayList<>();
                r = new Resultado();
                for (int i = 0; i < lstEstudiantes.size(); i++) {
                    r.setEstudiante(lstEstudiantes.get(i));
                    r.setConcepto(concRegistrado);
                    r.setValor(0.15);
                    lstResultados.add(r);
                    r = new Resultado();
                }
                daoResultado.registrarVarios(session, lstResultados);
                this.transaction.commit();
                c = new Concepto();
            }
            mapReader.close();
            File archivoCsvTemp = new File(nombreArchivo);
            if (archivoCsvTemp.delete()) {
                System.out.println("ARCHIVO TEMPORAL ELIMINADO CORRECTAMENTE");
            } else {
                System.out.println("NO SE PUDO ELIMINAR EL ARCHIVO TEMPORAL");
            }
            this.nombreArchivo = null;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Datos importados correctamente."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                if (transaction.isInitiator()) {
                    this.transaction.rollback();
                }
            }
            System.out.println("ERROR " + ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "El archivo no corresponde a la plantilla."));
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
            new NotNull(), // nombreConcepto
            new NotNull(), // traduccion
            new NotNull(), // descripcion
            new NotNull(new ParseBool()), // estado
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
    
    public void showDialogEliminar(){
        RequestContext.getCurrentInstance().update("frmEditarConcepto:panelEditarConcepto");
            RequestContext.getCurrentInstance().execute("PF('eliminarConcepto').show()");
    }
    public void cambiarConceptos(){
        this.concepto=new Concepto();
        
    }
    

}
